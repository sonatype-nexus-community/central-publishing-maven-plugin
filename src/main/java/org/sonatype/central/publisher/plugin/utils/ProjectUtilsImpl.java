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
package org.sonatype.central.publisher.plugin.utils;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sonatype.central.publisher.plugin.model.ArtifactWithFile;
import org.sonatype.central.publisher.plugin.model.ChecksumRequest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import static java.util.stream.Collectors.toList;
import static org.sonatype.central.publisher.plugin.Constants.POM_FILE_EXTENSION;

@Component(role = ProjectUtils.class)
public class ProjectUtilsImpl
    extends AbstractLogEnabled
    implements ProjectUtils
{
  public static final String MAVEN_METADATA_CENTRAL_STAGING_XML = "maven-metadata-central-staging.xml";

  @Requirement
  private HashUtils hashUtils;

  public List<ArtifactWithFile> getArtifacts(
      final MavenProject mavenProject,
      final ArtifactFactory artifactFactory)
      throws MojoExecutionException
  {
    final List<ArtifactWithFile> artifactWithFiles = new ArrayList<>();

    if (Objects.equals(mavenProject.getPackaging(), POM_FILE_EXTENSION)) {
      artifactWithFiles.addAll(getArtifactsFromPomProject(mavenProject));
    }
    else {
      artifactWithFiles.addAll(getArtifactsFromNonPomProject(mavenProject, artifactFactory));
    }

    return artifactWithFiles;
  }

  @Override
  public void deleteGroupArtifactMavenMetadataCentralStagingXml(final MavenProject project, final Path sourceDir) {
    if (project.getParent() != null) {
      deleteGroupArtifactMavenMetadataCentralStagingXml(project.getParent(), sourceDir);
    }

    deleteMavenMetadataCentralStagingXml(project, sourceDir);
  }

  @Override
  public void createChecksumFiles(final MavenProject project, final Path sourceDir, final ChecksumRequest checksumRequest) {
    Path path = getProjectGroupArtifactVersionPath(project);
    getLogger().info("Generate checksums for dir: " + path.toString());
    File gavDirectory = sourceDir.resolve(path).toFile();

    if (!gavDirectory.exists() || !gavDirectory.isDirectory()) {
      return;
    }

    File[] files = gavDirectory.listFiles((dir, name) -> !name.equalsIgnoreCase(MAVEN_METADATA_CENTRAL_STAGING_XML));

    if (files != null) {
      for (File file : files) {
        if (!hashUtils.isChecksumFile(file) && !hashUtils.isSignatureFile(file)) {
          switch (checksumRequest) {
            case ALL:
              hashUtils.createChecksumFile(file, HashAlgorithm.SHA256);
              hashUtils.createChecksumFile(file, HashAlgorithm.SHA512);
            case REQUIRED:
              hashUtils.createChecksumFile(file, HashAlgorithm.MD5);
              hashUtils.createChecksumFile(file, HashAlgorithm.SHA1);
              break;
            case NONE:
            default:
              break;
          }
        }
      }
    }
  }

  /**
   * Return a {@link Path} based on the {@link MavenSession#getCurrentProject()}'s Group ID. Example if a Groups is
   * org.sonatype.publishing, a {@link Path} will be returned containing "/org/sonatype/publishing"
   *
   * @return Path of Group ID.
   */
  public Path getProjectGroupPath(final MavenProject project) {
    String[] groupPathParts = project.getGroupId().split("\\.");

    Path path = FileSystems.getDefault().getPath("");

    for (String groupPathPart : groupPathParts) {
      path = path.resolve(groupPathPart);
    }

    return path;
  }

  /**
   * Return a {@link Path} based on the {@link MavenSession#getCurrentProject()}'s Group ID and Artifact ID. Example if
   * a GA is org.sonatype.publishing:test, a {@link Path} will be returned containing "/org/sonatype/publishing/test"
   *
   * @return Path of Group ID and Artifact ID.
   */
  public Path getProjectGroupArtifactPath(final MavenProject project) {
    return getProjectGroupPath(project).resolve(project.getArtifactId());
  }

  /**
   * Return a {@link Path} based on the {@link MavenSession#getCurrentProject()}'s Group ID, Artifact ID and Version.
   * Example if a GAV is org.sonatype.publishing:test:1.0.0, a {@link Path} will be returned containing
   * "/org/sonatype/publishing/test/1.0.0"
   *
   * @return Path of Group ID and Artifact ID.
   */
  public Path getProjectGroupArtifactVersionPath(final MavenProject project) {
    return getProjectGroupPath(project)
        .resolve(project.getArtifactId())
        .resolve(project.getVersion());
  }

  private List<ArtifactWithFile> getArtifactsFromPomProject(final MavenProject mavenProject) {
    List<ArtifactWithFile> artifactWithFiles = new ArrayList<>();
    artifactWithFiles.add(new ArtifactWithFile(mavenProject.getFile(), mavenProject.getArtifact()));

    artifactWithFiles.addAll(getAttachedArtifacts(mavenProject));

    return artifactWithFiles;
  }

  private List<ArtifactWithFile> getArtifactsFromNonPomProject(
      final MavenProject mavenProject,
      final ArtifactFactory artifactFactory)
      throws MojoExecutionException
  {
    Artifact artifact = mavenProject.getArtifact();
    List<Artifact> attachedArtifacts = mavenProject.getAttachedArtifacts();
    List<ArtifactWithFile> artifactWithFiles = new ArrayList<>();

    artifact.addMetadata(new ProjectArtifactMetadata(artifact, mavenProject.getFile()));

    final File file = artifact.getFile();

    if (file != null && file.isFile()) {
      artifactWithFiles.add(new ArtifactWithFile(file, artifact));
    }
    else if (!attachedArtifacts.isEmpty()) {
      getLogger().info("No primary artifact to deploy, deploying attached artifacts instead.");

      final Artifact pomArtifact =
          artifactFactory.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(),
              artifact.getBaseVersion());
      pomArtifact.setFile(mavenProject.getFile());

      artifactWithFiles.add(new ArtifactWithFile(pomArtifact.getFile(), pomArtifact));

      artifact.setResolvedVersion(pomArtifact.getVersion());
    }
    else {
      throw new MojoExecutionException(
          "The packaging for this project did not assign a file to the build artifact");
    }

    artifactWithFiles.addAll(getAttachedArtifacts(mavenProject));

    return artifactWithFiles;
  }

  private List<ArtifactWithFile> getAttachedArtifacts(final MavenProject mavenProject) {
    return mavenProject.getAttachedArtifacts().stream()
        .map(artifact -> new ArtifactWithFile(artifact.getFile(), artifact)).collect(toList());
  }

  private void deleteMavenMetadataCentralStagingXml(final MavenProject project, final Path sourceDir) {
    deleteMavenMetadataCentralStagingXml(sourceDir, getProjectGroupPath(project));
    deleteMavenMetadataCentralStagingXml(sourceDir, getProjectGroupArtifactPath(project));
    deleteMavenMetadataCentralStagingXml(sourceDir, getProjectGroupArtifactVersionPath(project));
  }

  private void deleteMavenMetadataCentralStagingXml(final Path sourceDir, final Path path) {
    File file = sourceDir.resolve(path).resolve(MAVEN_METADATA_CENTRAL_STAGING_XML).toFile();
    getLogger().debug("Pre Bundling - deleting " + file.getPath());

    if (!file.exists()) {
      getLogger().debug("Pre Bundling - does not exist " + file.getPath());
      return;
    }

    if (file.delete()) {
      getLogger().info(String.format("Pre Bundling - deleted %s", file.getPath()));
    }
    else {
      getLogger().error(String.format("Pre Bundling - failed to delete %s", file.getPath()));
    }
  }
}
