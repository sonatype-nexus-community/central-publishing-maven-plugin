/*
 * Copyright (c) 2022-present Sonatype, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.central.publisher.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.central.publisher.AbstractPlexusInjectionTest;
import org.sonatype.central.publisher.plugin.bundler.ArtifactBundler;
import org.sonatype.central.publisher.plugin.defer.ArtifactDeferrer;
import org.sonatype.central.publisher.plugin.model.ArtifactWithFile;
import org.sonatype.central.publisher.plugin.model.DeferArtifactRequest;
import org.sonatype.central.publisher.plugin.model.StageArtifactRequest;
import org.sonatype.central.publisher.plugin.stager.ArtifactStager;
import org.sonatype.central.publisher.plugin.uploader.ArtifactUploader;
import org.sonatype.central.publisher.plugin.watcher.DeploymentPublishedWatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class PublishMojoTest extends AbstractPlexusInjectionTest {
  @Mock
  private ArtifactBundler artifactBundler;

  @Mock
  private ArtifactUploader artifactUploader;

  @Mock
  private ArtifactStager artifactStager;

  @Mock
  DeploymentPublishedWatcher deploymentPublishedWatcher;

  @Mock
  MavenSession mavenSession;

  @Mock
  ArtifactDeferrer artifactDeferrer;

  private File mockStagingDirectory;
  private File mockOutputDirectory;

  PublishMojo realMojo;

  @Before
  public void setUp() throws IllegalAccessException {
    MockitoAnnotations.openMocks(this); // Initialize mocks
    realMojo = new PublishMojo();

    // Mock MavenSession and its behavior
    MavenProject mockMavenProject = mock(MavenProject.class);
    when(mavenSession.getCurrentProject()).thenReturn(mockMavenProject);

    // Inject the mocks into the realMojo instance
    injectMocks(realMojo);

    mockStagingDirectory = mock(File.class);
    mockOutputDirectory = mock(File.class);
  }

  @Test
  public void testPostProcessRelease_NoFilesInStagingDirectory() {
    // Call the method
    realMojo.postProcessRelease(mockStagingDirectory, mockOutputDirectory, "testDeployment");

    // Verify that no further processing occurs
    verify(artifactBundler, never()).bundle(any());
    verify(artifactUploader, never()).upload(any());
  }

  @Test
  public void testPostProcessRelease_WithFilesInStagingDirectory() throws IOException {
    // Create a temporary directory to simulate the staging directory with files
    File tempDir = createTempStagingDir();
    // Call the method
    realMojo.postProcessRelease(tempDir, mockOutputDirectory, "testDeployment");

    // Verify that bundling and uploading are called
    verify(artifactBundler, times(1)).bundle(any());
    verify(artifactUploader, times(1)).upload(any());
  }

  @Test
  public void testProcessSnapshot_SkipPublishing() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();

    // Set up mocks
    List<ArtifactWithFile> mockArtifacts = getMockArtifacts("group1", "artifact1", "1.0.0");

    // Configure the PublishMojo instance to skip publishing
    injectValue(realMojo, "skipPublishing", true);

    // Call the method
    realMojo.processSnapshot(mockArtifacts, tempDir);

    ArgumentCaptor<DeferArtifactRequest> captor = ArgumentCaptor.forClass(DeferArtifactRequest.class);
    verify(artifactDeferrer, times(1)).install(captor.capture());
    DeferArtifactRequest capturedRequest = captor.getValue();
    List<ArtifactWithFile> artifact = capturedRequest.getArtifactWithFiles();
    assertTrue(artifact.isEmpty());
  }

  @Test
  public void testProcessSnapshot_DoNotSkipPublishing() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();

    // Set up mocks
    List<ArtifactWithFile> mockArtifacts = getMockArtifacts("group1", "artifact1", "1.0.0");

    // Configure the PublishMojo instance to NOT skip publishing
    injectValue(realMojo, "skipPublishing", false);

    // Call the method
    realMojo.processSnapshot(mockArtifacts, tempDir);

    ArgumentCaptor<DeferArtifactRequest> captor = ArgumentCaptor.forClass(DeferArtifactRequest.class);
    verify(artifactDeferrer, times(1)).install(captor.capture());
    DeferArtifactRequest capturedRequest = captor.getValue();
    List<ArtifactWithFile> artifacts = capturedRequest.getArtifactWithFiles();
    assertFalse(artifacts.isEmpty());
  }

  @Test
  public void testProcessSnapshot_SkipSomePublishing() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();

    // Process with a skipped publishing flag
    injectValue(realMojo, "skipPublishing", true);
    List<ArtifactWithFile> mockArtifacts = getMockArtifacts("group1", "artifact1", "1.0.0");
    realMojo.processSnapshot(mockArtifacts, tempDir);

    // Process twice without skipping
    injectValue(realMojo, "skipPublishing", false);

    mockArtifacts = getMockArtifacts("group1", "artifact2", "1.0.0");
    realMojo.processSnapshot(mockArtifacts, tempDir);

    mockArtifacts = getMockArtifacts("group1", "artifact3", "1.0.0");
    realMojo.processSnapshot(mockArtifacts, tempDir);

    // Check results
    ArgumentCaptor<DeferArtifactRequest> captor = ArgumentCaptor.forClass(DeferArtifactRequest.class);
    verify(artifactDeferrer, times(3)).install(captor.capture());
    List<DeferArtifactRequest> capturedRequests = captor.getAllValues();

    List<ArtifactWithFile> firstInvocationArtifacts = capturedRequests.get(0).getArtifactWithFiles();
    assertTrue(firstInvocationArtifacts.isEmpty());

    List<ArtifactWithFile> secondInvocationArtifacts = capturedRequests.get(1).getArtifactWithFiles();
    assertEquals(1, secondInvocationArtifacts.size());
    assertEquals("artifact2", secondInvocationArtifacts.get(0).getArtifact().getArtifactId());

    List<ArtifactWithFile> thirdInvocationArtifacts = capturedRequests.get(2).getArtifactWithFiles();
    assertEquals(1, thirdInvocationArtifacts.size());
    assertEquals("artifact3", thirdInvocationArtifacts.get(0).getArtifact().getArtifactId());
  }

  @Test
  public void testProcessRelease_SkipPublishing() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();

    // Set up mocks
    List<ArtifactWithFile> mockArtifacts = getMockArtifacts("group1", "artifact1", "1.0.0");

    // Configure the PublishMojo instance to skip publishing
    injectValue(realMojo, "skipPublishing", true);

    // Call the method
    realMojo.processRelease(mockArtifacts, tempDir);

    ArgumentCaptor<StageArtifactRequest> captor = ArgumentCaptor.forClass(StageArtifactRequest.class);
    verify(artifactStager, times(1)).stageArtifact(captor.capture());
    StageArtifactRequest capturedRequest = captor.getValue();
    List<ArtifactWithFile> artifact = capturedRequest.getArtifactWithFiles();
    assertTrue(artifact.isEmpty());
  }

  @Test
  public void testProcessRelease_DoNotSkipPublishing() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();

    // Set up mocks
    List<ArtifactWithFile> mockArtifacts = getMockArtifacts("group1", "artifact1", "1.0.0");

    // Configure the PublishMojo instance to NOT skip publishing
    injectValue(realMojo, "skipPublishing", false);

    // Call the method
    realMojo.processRelease(mockArtifacts, tempDir);

    ArgumentCaptor<StageArtifactRequest> captor = ArgumentCaptor.forClass(StageArtifactRequest.class);
    verify(artifactStager, times(1)).stageArtifact(captor.capture());
    StageArtifactRequest capturedRequest = captor.getValue();
    List<ArtifactWithFile> artifact = capturedRequest.getArtifactWithFiles();
    assertFalse(artifact.isEmpty());
  }

  @Test
  public void testProcessRelease_SkipSomePublishing() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();

    // Process with a skipped publishing flag
    injectValue(realMojo, "skipPublishing", true);
    List<ArtifactWithFile> mockArtifacts = getMockArtifacts("group1", "artifact1", "1.0.0");
    realMojo.processRelease(mockArtifacts, tempDir);

    // Process twice without skipping
    injectValue(realMojo, "skipPublishing", false);

    mockArtifacts = getMockArtifacts("group1", "artifact2", "1.0.0");
    realMojo.processRelease(mockArtifacts, tempDir);

    mockArtifacts = getMockArtifacts("group1", "artifact3", "1.0.0");
    realMojo.processRelease(mockArtifacts, tempDir);

    // Check results
    ArgumentCaptor<StageArtifactRequest> captor = ArgumentCaptor.forClass(StageArtifactRequest.class);
    verify(artifactStager, times(3)).stageArtifact(captor.capture());
    List<StageArtifactRequest> capturedRequests = captor.getAllValues();

    List<ArtifactWithFile> firstInvocationArtifacts = capturedRequests.get(0).getArtifactWithFiles();
    assertTrue(firstInvocationArtifacts.isEmpty());

    List<ArtifactWithFile> secondInvocationArtifacts = capturedRequests.get(1).getArtifactWithFiles();
    assertEquals(1, secondInvocationArtifacts.size());
    assertEquals("artifact2", secondInvocationArtifacts.get(0).getArtifact().getArtifactId());

    List<ArtifactWithFile> thirdInvocationArtifacts = capturedRequests.get(2).getArtifactWithFiles();
    assertEquals(1, thirdInvocationArtifacts.size());
    assertEquals("artifact3", thirdInvocationArtifacts.get(0).getArtifact().getArtifactId());
  }

  @Test
  public void testPostProcessRelease_SkipUpload_BuildsTheBundleButDoesNotUploadIt() throws Exception {
    File tempDir = createTempStagingDir();
    when(artifactBundler.bundle(any())).thenReturn(new File(tempDir, "central-bundle.zip").toPath());
    injectValue(realMojo, "skipUpload", true);

    realMojo.postProcessRelease(tempDir, mockOutputDirectory, "testDeployment");

    verify(artifactBundler, times(1)).bundle(any());
    verify(artifactUploader, never()).upload(any());
    verify(deploymentPublishedWatcher, never()).waitForDeploymentState(any());
  }

  @Test
  public void testPostProcessRelease_DoNotSkipUpload_StillUploads() throws Exception {
    File tempDir = createTempStagingDir();
    injectValue(realMojo, "skipUpload", false);

    realMojo.postProcessRelease(tempDir, mockOutputDirectory, "testDeployment");

    verify(artifactBundler, times(1)).bundle(any());
    verify(artifactUploader, times(1)).upload(any());
  }

  @Test
  public void testPostProcessRelease_SkipUpload_NoFilesInStagingDirectory_DoesNothing() throws Exception {
    injectValue(realMojo, "skipUpload", true);

    realMojo.postProcessRelease(mockStagingDirectory, mockOutputDirectory, "testDeployment");

    verify(artifactBundler, never()).bundle(any());
    verify(artifactUploader, never()).upload(any());
  }

  @Test
  public void testPostProcessSnapshot_SkipUpload_DoesNotDeployTheDeferredArtifacts() throws Exception {
    File deferredDir = createTempStagingDir();
    Files.createFile(new File(deferredDir, ".index").toPath());
    injectValue(realMojo, "skipUpload", true);

    realMojo.postProcessSnapshot(deferredDir);

    verify(artifactDeferrer, never()).deployUp(any(), any(), any());
  }

  @Test
  public void testPostProcessSnapshot_DoNotSkipUpload_StillDeploys() throws Exception {
    File deferredDir = createTempStagingDir();
    Files.createFile(new File(deferredDir, ".index").toPath());
    injectValue(realMojo, "skipUpload", false);

    realMojo.postProcessSnapshot(deferredDir);

    verify(artifactDeferrer, times(1)).deployUp(any(), any(), any());
  }

  /**
   * skipUpload is about the upload, not about which artifacts are staged. Unlike skipPublishing, it must leave the
   * artifacts in the staging directory so that a bundle can be built from them.
   */
  @Test
  public void testProcessRelease_SkipUpload_StillStagesTheArtifacts() throws Exception {
    File tempDir = Files.createTempDirectory("testStagingDir").toFile();
    injectValue(realMojo, "skipUpload", true);

    realMojo.processRelease(getMockArtifacts("group1", "artifact1", "1.0.0"), tempDir);

    ArgumentCaptor<StageArtifactRequest> captor = ArgumentCaptor.forClass(StageArtifactRequest.class);
    verify(artifactStager, times(1)).stageArtifact(captor.capture());
    assertEquals(1, captor.getValue().getArtifactWithFiles().size());
  }

  @Test
  public void testProcessSnapshot_SkipUpload_StillStagesTheArtifacts() throws Exception {
    File tempDir = Files.createTempDirectory("testDeferredDir").toFile();
    injectValue(realMojo, "skipUpload", true);

    realMojo.processSnapshot(getMockArtifacts("group1", "artifact1", "1.0.0"), tempDir);

    ArgumentCaptor<DeferArtifactRequest> captor = ArgumentCaptor.forClass(DeferArtifactRequest.class);
    verify(artifactDeferrer, times(1)).install(captor.capture());
    assertEquals(1, captor.getValue().getArtifactWithFiles().size());
  }

  private File createTempStagingDir() throws IOException {
    // Instead of mocking the staging directory, lets make a temporary directory. The mock is empty.
    File tempDir = Files.createTempDirectory("testStagingDir").toFile();
    tempDir.deleteOnExit();
    // Create a temporary file to simulate files in the directory
    File tempFile = new File(tempDir, "tempFile.txt");
    tempFile.createNewFile();
    tempFile.deleteOnExit();
    return tempDir;
  }

  private List<ArtifactWithFile> getMockArtifacts(String groupId, String artifactId, String version) {
    List<ArtifactWithFile> mockArtifacts = new ArrayList<>();
    Artifact artifact = mock(Artifact.class);
    when(artifact.getGroupId()).thenReturn(groupId);
    when(artifact.getArtifactId()).thenReturn(artifactId);
    when(artifact.getVersion()).thenReturn(version);
    when(artifact.getClassifier()).thenReturn("");
    when(artifact.getType()).thenReturn("jar");
    mockArtifacts.add(new ArtifactWithFile(null, artifact));
    return mockArtifacts;
  }
}