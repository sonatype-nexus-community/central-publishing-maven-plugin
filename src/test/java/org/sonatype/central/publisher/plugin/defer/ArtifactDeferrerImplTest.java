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
package org.sonatype.central.publisher.plugin.defer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sonatype.central.publisher.plugin.model.ArtifactWithFile;
import org.sonatype.central.publisher.plugin.model.DeferArtifactRequest;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.lang.String.format;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.central.publisher.plugin.Constants.DEFAULT_DEFERRED_DIR_NAME;
import static org.sonatype.central.publisher.plugin.Constants.LOCAL_STAGING_REPOSITORY_NAME;
import static org.sonatype.central.publisher.plugin.defer.ArtifactDeferrerImpl.INDEX_FILE_NAME;
import static org.sonatype.central.publisher.plugin.defer.ArtifactDeferrerImpl.INDEX_LINE_FORMAT;

public class ArtifactDeferrerImplTest
{
  @Mock
  private ArtifactRepositoryFactory artifactRepositoryFactory;

  @Mock
  @SuppressWarnings("deprecation")
  private ArtifactRepositoryLayout artifactRepositoryLayout;

  @Mock
  private ArtifactInstaller artifactInstaller;

  @Mock
  private ArtifactDeployer artifactDeployer;

  @Mock
  private MavenSession mavenSession;

  @Mock
  private MavenProject mavenProject;

  @Mock
  @SuppressWarnings("deprecation")
  private ArtifactRepository distributionManagementArtifactRepository;

  @Mock
  @SuppressWarnings("deprecation")
  private ArtifactRepository deferredRepository;

  @Mock
  private Artifact artifact;

  @Component
  private ArtifactDeferrer artifactDeferrer;

  private File deferredDirectory;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);

    when(artifact.getGroupId()).thenReturn("com.sonatype");
    when(artifact.getArtifactId()).thenReturn("test");
    when(artifact.getVersion()).thenReturn("1.0.0-SNAPSHOT");
    when(artifact.getClassifier()).thenReturn(null);
    when(artifact.getType()).thenReturn("pom");
    when(artifact.getArtifactHandler()).thenReturn(null);

    deferredDirectory = createTempDirectory(DEFAULT_DEFERRED_DIR_NAME).toFile();

    artifactDeferrer = new ArtifactDeferrerImpl(
        artifactRepositoryFactory,
        artifactRepositoryLayout,
        artifactInstaller,
        artifactDeployer
    );
  }

  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(deferredDirectory);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void when_Request_Then_Install() throws MojoExecutionException, ArtifactInstallationException {
    setupForValidInstall();

    List<ArtifactWithFile> list = createArtifactWithFileList();
    artifactDeferrer.install(new DeferArtifactRequest(mavenSession, list, deferredDirectory, null, null));

    verify(artifactInstaller).install(any(), any(Artifact.class), any(ArtifactRepository.class));

    assertLineInIndex(1, expectedIndexLine("central", "http://distributiourl/snapshots/"));

    // proof that giving a custom centralSnapshotsUrl will override the DistributionManagementArtifactRepository url
    when(artifactRepositoryFactory
        .createDeploymentArtifactRepository(
            eq("central"),
            anyString(),
            eq(artifactRepositoryLayout),
            eq(true)))
        .thenReturn(deferredRepository);
    when(deferredRepository.getId()).thenReturn("central");
    when(deferredRepository.getUrl()).thenReturn("http://deferurl/snapshots/");

    artifactDeferrer.install(
        new DeferArtifactRequest(mavenSession, list, deferredDirectory, "http://deferurl/snapshots/", "central")
    );

    assertLineInIndex(2, expectedIndexLine("central", "http://deferurl/snapshots/"));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void when_RequestInvalid_Then_DoNotInstall()
      throws MojoExecutionException, ArtifactInstallationException
  {
    artifactDeferrer.install(new DeferArtifactRequest(null, null, null, null, null));

    verify(artifactInstaller, never()).install(any(File.class), any(Artifact.class), any(ArtifactRepository.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void when_RequestHasInvalidStagingDirectory_Then_DoNotInstall()
      throws ArtifactInstallationException
  {
    List<ArtifactWithFile> artifactWithFiles = createArtifactWithFileList();
    DeferArtifactRequest request = new DeferArtifactRequest(null, artifactWithFiles, null, null, null);
    MojoExecutionException thrown = assertThrows(MojoExecutionException.class, () -> artifactDeferrer.install(request));

    assertThat(thrown.getMessage(), is("Staging failed: staging directory is null!"));
    verify(artifactInstaller, never()).install(any(), any(Artifact.class), any(ArtifactRepository.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void when_RequestHasMissingMavenSession_Then_DoNotInstall()
      throws ArtifactInstallationException, IOException
  {
    List<ArtifactWithFile> artifactWithFiles = createArtifactWithFileList();
    File deferredDirectory = createTempDirectory(DEFAULT_DEFERRED_DIR_NAME).toFile();

    DeferArtifactRequest request = new DeferArtifactRequest(null, artifactWithFiles, deferredDirectory, null, null);
    MojoExecutionException thrown = assertThrows(MojoExecutionException.class, () -> artifactDeferrer.install(request));

    assertThat(thrown.getMessage(), is("Deployment failed, missing maven session."));
    verify(artifactInstaller, never()).install(any(), any(Artifact.class), any(ArtifactRepository.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void when_RequestHasMissingSnapshotUrl_Then_DoNotInstall()
      throws ArtifactInstallationException, IOException
  {
    List<ArtifactWithFile> artifactWithFiles = createArtifactWithFileList();
    File deferredDirectory = createTempDirectory(DEFAULT_DEFERRED_DIR_NAME).toFile();

    when(mavenSession.getCurrentProject()).thenReturn(mavenProject);

    DeferArtifactRequest request =
        new DeferArtifactRequest(mavenSession, artifactWithFiles, deferredDirectory, null, null);
    MojoExecutionException thrown = assertThrows(MojoExecutionException.class, () -> artifactDeferrer.install(request));

    String msg = "Deployment failed, missing snapshots url:\n" +
        "centralSnapshotsUrl element was not specified in the inside configuration element\n" +
        "or the repository element was not specified in the POM inside distributionManagement element";
    
    assertThat(thrown.getMessage(), is(msg));
    verify(artifactInstaller, never()).install(any(), any(Artifact.class), any(ArtifactRepository.class));
  }

  @Test
  public void when_IndexFileNoExist_Then_DoNotDeployUp() throws ArtifactDeploymentException {
    FileNotFoundException thrown =
        assertThrows(FileNotFoundException.class, () -> artifactDeferrer.deployUp(null, null, null));

    assertThat(thrown.getMessage(), is(".index (No such file or directory)"));
    verify(artifactDeployer, never()).deploy(any(), any(), any(), any());
  }

  @Test
  public void when_Installed_Then_CanDeployUp()
      throws ArtifactDeploymentException, MojoExecutionException, ArtifactInstallationException, IOException
  {
    setupForValidInstall();

    MavenArtifactRepository localRepository = new MavenArtifactRepository();
    when(mavenSession.getLocalRepository()).thenReturn(localRepository);

    artifactDeferrer.install(
        new DeferArtifactRequest(mavenSession, createArtifactWithFileList(), deferredDirectory, null, null)
    );

    artifactDeferrer.deployUp(mavenSession, deferredDirectory, distributionManagementArtifactRepository);

    verify(artifactDeployer)
        .deploy(eq(deferredDirectory.toPath().resolve(expectedArtifactPath()).toFile()),
            any(DefaultArtifact.class),
            eq(distributionManagementArtifactRepository),
            eq(localRepository)
        );

    // proof that not providing a remote repository will create one from the index
    when(artifactRepositoryFactory
        .createDeploymentArtifactRepository(
            eq("central"),
            anyString(),
            eq(artifactRepositoryLayout),
            eq(true)))
        .thenReturn(deferredRepository);

    artifactDeferrer.deployUp(mavenSession, deferredDirectory, null);

    verify(artifactDeployer)
        .deploy(eq(deferredDirectory.toPath().resolve(expectedArtifactPath()).toFile()),
            any(DefaultArtifact.class),
            eq(deferredRepository),
            eq(localRepository)
        );
  }

  private void setupForValidInstall() {
    when(mavenSession.getCurrentProject()).thenReturn(mavenProject);
    when(mavenProject.getDistributionManagementArtifactRepository()).thenReturn(
        distributionManagementArtifactRepository);
    when(distributionManagementArtifactRepository.getId()).thenReturn("central");
    when(distributionManagementArtifactRepository.getUrl()).thenReturn("http://distributiourl/snapshots/");
    when(artifactRepositoryFactory.createDeploymentArtifactRepository(eq(LOCAL_STAGING_REPOSITORY_NAME), anyString(),
        eq(artifactRepositoryLayout), eq(true))).thenReturn(deferredRepository);
    String expectedArtifactPath = expectedArtifactPath();
    when(deferredRepository.pathOf(eq(artifact))).thenReturn(expectedArtifactPath);
  }

  private List<ArtifactWithFile> createArtifactWithFileList() {
    ArtifactWithFile artifactWithFile = new ArtifactWithFile(null, artifact);
    List<ArtifactWithFile> list = new ArrayList<>();
    list.add(artifactWithFile);
    return list;
  }

  private String expectedIndexLine(final String serverId, final String snapshotsUrl) {
    return format(INDEX_LINE_FORMAT,
        expectedArtifactPath(),
        artifact.getGroupId(),
        artifact.getArtifactId(),
        artifact.getVersion(),
        "n/a",
        artifact.getType(),
        "n/a",
        "n/a",
        "n/a",
        serverId,
        snapshotsUrl
    );
  }

  private String expectedArtifactPath() {
    return format("%s/%s/%s/%s-%s.%s",
        artifact.getGroupId().replaceAll("\\.", "/"),
        artifact.getArtifactId(),
        artifact.getVersion(),
        artifact.getArtifactId(),
        artifact.getVersion(),
        artifact.getType()
    );
  }

  private void assertLineInIndex(int lineNumber, String expected) {
    Optional<String> optionalLine = readLineInIndex(lineNumber);
    assertTrue(optionalLine.isPresent());
    assertThat(optionalLine.get(), is(expected));
  }

  private Optional<String> readLineInIndex(int lineNumber) {
    try {
      return ofNullable(readIndex().get(lineNumber - 1));
    }
    catch (Exception e) {
      return empty();
    }
  }

  private List<String> readIndex() {
    try {
      return Files.readAllLines(deferredDirectory.toPath().resolve(INDEX_FILE_NAME));
    }
    catch (IOException e) {
      return emptyList();
    }
  }
}
