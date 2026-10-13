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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.sonatype.central.publisher.plugin.model.ArtifactWithFile;
import org.sonatype.central.publisher.plugin.model.DeferArtifactRequest;

import com.google.common.io.Closeables;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.metadata.GroupRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersion;
import static org.sonatype.central.publisher.plugin.Constants.CENTRAL_SNAPSHOTS_URL_DEFAULT_VALUE;
import static org.sonatype.central.publisher.plugin.Constants.LOCAL_STAGING_REPOSITORY_NAME;

/**
 * Implementation of {@link ArtifactDeferrer}.
 * <p>
 * Highly inspired by the  <a href="https://github.com/sonatype/nexus-maven-plugins">nexus-maven-plugin</a>,
 * specifically the DeferredDeployStrategy and the AbstractDeployStrategy classes
 * <p>
 * Portions of this class are derived from the Nexus Maven Plugins project, Copyright (c) 2007-2015 Sonatype, Inc.,
 * which is made available under the Eclipse Public License Version 1.0
 * (<a href="http://www.eclipse.org/legal/epl-v10.html">http://www.eclipse.org/legal/epl-v10.html</a>). See the NOTICE
 * file at the root of this repository.
 */
@Component(role = ArtifactDeferrer.class)
public class ArtifactDeferrerImpl
    extends AbstractLogEnabled
    implements ArtifactDeferrer
{
  public static final String INDEX_FILE_NAME = ".index";

  // G:A:V:C:P:Ext:PomFileName:PluginPrefix:repoId:repoUrl
  protected static final Pattern INDEX_PROPS =
      Pattern.compile("([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):(.*)");

  protected static final String INDEX_LINE_FORMAT = "%s=%s:%s:%s:%s:%s:%s:%s:%s:%s:%s";

  private static final Object PARALLEL_LOCK = new Object();

  @Requirement
  private final ArtifactRepositoryFactory artifactRepositoryFactory;

  @Requirement
  @SuppressWarnings("deprecation")
  private final ArtifactRepositoryLayout artifactRepositoryLayout;

  @Requirement
  private final ArtifactInstaller artifactInstaller;

  @Requirement
  private final ArtifactDeployer artifactDeployer;

  @Inject
  @SuppressWarnings("deprecation")
  public ArtifactDeferrerImpl(
      final ArtifactRepositoryFactory artifactRepositoryFactory,
      final ArtifactRepositoryLayout artifactRepositoryLayout,
      final ArtifactInstaller artifactInstaller,
      final ArtifactDeployer artifactDeployer)
  {
    this.artifactRepositoryFactory = artifactRepositoryFactory;
    this.artifactRepositoryLayout = artifactRepositoryLayout;
    this.artifactInstaller = artifactInstaller;
    this.artifactDeployer = artifactDeployer;
  }

  /**
   * Installs the artifacts into the staging repository.
   *
   * @param request - {@link DeferArtifactRequest}
   * @throws ArtifactInstallationException - if the artifact installation fails
   * @throws MojoExecutionException        - if the staging directory is null or the artifact repo cannot be created
   * @see ArtifactDeferrer#install(File, Artifact, ArtifactRepository, File, ArtifactRepository)
   */
  @Override
  @SuppressWarnings("deprecation")
  public void install(final DeferArtifactRequest request)
      throws ArtifactInstallationException, MojoExecutionException
  {
    List<ArtifactWithFile> artifactWithFiles = request.getArtifactWithFiles();
    if (null != artifactWithFiles && !artifactWithFiles.isEmpty()) {
      for (ArtifactWithFile artifactWithFile : artifactWithFiles) {

        // deploys always to same stagingDirectory
        File stagingDirectory = request.getDeferredDirectory();
        ArtifactRepository stagingRepository = getArtifactRepositoryForDirectory(stagingDirectory);
        ArtifactRepository deploymentRepository = getDeploymentRepository(
            request.getMavenSession(),
            request.getCentralSnapshotsUrl(),
            request.getServerId()
        );

        install(
            artifactWithFile.getFile(),
            artifactWithFile.getArtifact(),
            stagingRepository,
            stagingDirectory,
            deploymentRepository
        );
      }
    }
  }

  /**
   * Performs an "install" into the staging repository. It will retain snapshot versions, and no metadata is created at
   * all. In short: performs a simple file copy.
   * <p/>
   * This one single method is not thread safe, as it performs IO and appends to the "index" file. Hence, this one
   * method is executed in a synchronized block of a static object's monitor, to prevent multiple parallel installation
   * actions to happen.
   */
  @SuppressWarnings("deprecation")
  private void install(
      final File source,
      final Artifact artifact,
      final ArtifactRepository stagingRepository,
      final File stagingDirectory,
      final ArtifactRepository remoteRepository)
      throws ArtifactInstallationException
  {
    synchronized (PARALLEL_LOCK) {
      String path = stagingRepository.pathOf(artifact);
      try {
        ofNullable(getLogger()).ifPresent(logger -> logger.debug(
            format("Installing artifact %s into staging repository\n%s", artifact, stagingRepository)
        ));

        artifactInstaller.install(source, artifact, stagingRepository);

        String pluginPrefix = null;
        for (ArtifactMetadata artifactMetadata : artifact.getMetadataList()) {
          if (artifactMetadata instanceof GroupRepositoryMetadata) {
            Plugin plugin = ((GroupRepositoryMetadata) artifactMetadata).getMetadata().getPlugins().get(0);
            pluginPrefix = plugin.getPrefix();
          }
        }

        // append the index file
        try (FileOutputStream fos = new FileOutputStream(new File(stagingDirectory, INDEX_FILE_NAME), true);
             OutputStreamWriter osw = new OutputStreamWriter(fos, ISO_8859_1);
             PrintWriter pw = new PrintWriter(osw)) {

          String pomFileName = null;
          for (ArtifactMetadata artifactMetadata : artifact.getMetadataList()) {
            if (artifactMetadata instanceof ProjectArtifactMetadata) {
              pomFileName = (artifactMetadata).getLocalFilename(stagingRepository);
            }
          }

          pw.println(
              format(INDEX_LINE_FORMAT,
                  path,
                  artifact.getGroupId(),
                  artifact.getArtifactId(),
                  artifact.getVersion(),
                  isBlank(artifact.getClassifier()) ? "n/a" : artifact.getClassifier(),
                  artifact.getType(),
                  artifact.getArtifactHandler() == null ? "n/a" : artifact.getArtifactHandler().getExtension(),
                  isBlank(pomFileName) ? "n/a" : pomFileName,
                  isBlank(pluginPrefix) ? "n/a" : pluginPrefix,
                  remoteRepository != null ? remoteRepository.getId() : "n/a",
                  remoteRepository != null ? remoteRepository.getUrl() : "n/a"
              ));

          pw.flush();
        }
      }
      catch (IOException e) {
        throw new ArtifactInstallationException("Cannot locally stage and maintain the index file!", e);
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void deployUp(
      final MavenSession mavenSession,
      final File sourceDirectory,
      final ArtifactRepository remoteRepository)
      throws ArtifactDeploymentException, IOException
  {
    // Need Aether RepoSystem and create one huge DeployRequest will _all_ artifacts (would be FAST as it would
    // go parallel), but we need to work in Maven2 too, so old compat and slow method remains: deploy one by one...
    FileInputStream fis = new FileInputStream(new File(sourceDirectory, INDEX_FILE_NAME));
    Properties index = new Properties();

    try {
      index.load(fis);
    }
    finally {
      Closeables.closeQuietly(fis);
    }

    ArtifactRepository repoToUse = remoteRepository;
    for (String includedFilePath : index.stringPropertyNames()) {
      File includedFile = new File(sourceDirectory, includedFilePath);
      String includedFileProps = index.getProperty(includedFilePath);
      Matcher matcher = INDEX_PROPS.matcher(includedFileProps);

      if (!matcher.matches()) {
        throw new ArtifactDeploymentException(
            format("Internal error! Line \"%s\" does not match pattern \"%s\"?", includedFileProps, INDEX_PROPS)
        );
      }

      String groupId = matcher.group(1);
      String artifactId = matcher.group(2);
      String version = matcher.group(3);
      String classifier = "n/a".equals(matcher.group(4)) ? null : matcher.group(4);
      String packaging = matcher.group(5);
      String extension = matcher.group(6);
      String pomFileName = "n/a".equals(matcher.group(7)) ? null : matcher.group(7);
      String pluginPrefix = "n/a".equals(matcher.group(8)) ? null : matcher.group(8);
      String repoId = "n/a".equals(matcher.group(9)) ? null : matcher.group(9);
      String repoUrl = "n/a".equals(matcher.group(10)) ? null : matcher.group(10);

      if (remoteRepository == null) {
        if (repoUrl != null && repoId != null) {
          repoToUse = createDeploymentArtifactRepository(repoId, repoUrl);
        }
        else {
          throw new ArtifactDeploymentException("Internal error! Remote repository for deployment not defined.");
        }
      }

      // just a synthetic one, to properly set extension
      FakeArtifactHandler artifactHandler = new FakeArtifactHandler(packaging, extension);

      DefaultArtifact artifact = new DefaultArtifact(
          groupId,
          artifactId,
          createFromVersion(version),
          null,
          packaging,
          classifier,
          artifactHandler
      );

      if (pomFileName != null) {
        addPomMetaData(includedFile, pomFileName, artifact);
        maybeAddMavenPluginMetaData(artifact, groupId, pluginPrefix, artifactId);
      }

      ofNullable(getLogger()).ifPresent(logger -> logger.debug(
          format("Deploying: %s:%s:%s:%s:%s:%s - file %s:%s: to server id: %s: url: %s",
              groupId, artifactId, version, classifier, packaging,
              extension, pomFileName, pluginPrefix, repoId, repoUrl)
      ));

      artifactDeployer.deploy(includedFile, artifact, repoToUse, mavenSession.getLocalRepository());
    }
  }

  @SuppressWarnings("deprecation")
  protected ArtifactRepository getArtifactRepositoryForDirectory(final File stagingDirectory)
      throws MojoExecutionException
  {
    if (stagingDirectory == null) {
      throw new MojoExecutionException("Staging failed: staging directory is null!");
    }

    try {
      Files.createDirectories(stagingDirectory.toPath());
      String url = stagingDirectory.getCanonicalFile().toURI().toURL().toExternalForm();
      return createStagingArtifactRepository(url);
    }
    catch (IOException e) {
      throw new MojoExecutionException(
          "Staging failed: could not create ArtifactRepository in staging directory " + stagingDirectory, e);
    }
  }

  @SuppressWarnings("deprecation")
  protected ArtifactRepository getDeploymentRepository(
      final MavenSession mavenSession,
      final String centralSnapshotsUrl,
      final String publishingServerId)
      throws MojoExecutionException
  {
    ArtifactRepository repo;

    if (isNotBlank(publishingServerId) && isNotBlank(centralSnapshotsUrl)) {
      return createDeploymentArtifactRepository(publishingServerId, centralSnapshotsUrl);
    }

    // should not happen, but is theoretically possible in tests
    if (null == mavenSession) {
      throw new MojoExecutionException("Deployment failed, missing maven session.");
    }

    // if we have a repo defined in the POM, use it
    repo = mavenSession.getCurrentProject().getDistributionManagementArtifactRepository();

    // if we miss on the third and final options we have, we fail.
    if (null == repo) {
      // final attempt, use the default snapshots url if we do have a publishing server id given
      if (isNotBlank(publishingServerId)) {
        return createDeploymentArtifactRepository(publishingServerId, CENTRAL_SNAPSHOTS_URL_DEFAULT_VALUE);
      }

      String msg = "Deployment failed, missing snapshots url:\n" +
          "centralSnapshotsUrl element was not specified in the inside configuration element\n" +
          "or the repository element was not specified in the POM inside distributionManagement element";

      throw new MojoExecutionException(msg);
    }

    if (isBlank(repo.getId())) {
      String msg = "Deployment failed, missing server id:\n" +
          "repository element was specified in the POM inside distributionManagement " +
          "element but is missing element id";

      throw new MojoExecutionException(msg);
    }

    if (isBlank(repo.getUrl())) {
      String msg = "Deployment failed, missing snapshots url:\n" +
          "repository element was specified in the POM inside distributionManagement " +
          "element but is missing element url";

      throw new MojoExecutionException(msg);
    }

    return repo;
  }

  @SuppressWarnings("deprecation")
  protected ArtifactRepository createStagingArtifactRepository(final String url) {
    return createDeploymentArtifactRepository(LOCAL_STAGING_REPOSITORY_NAME, url);
  }

  @SuppressWarnings("deprecation")
  protected ArtifactRepository createDeploymentArtifactRepository(final String id, final String url) {
    return artifactRepositoryFactory.createDeploymentArtifactRepository(id, url, artifactRepositoryLayout, true);
  }

  private void addPomMetaData(final File includedFile, final String pomFileName, final DefaultArtifact artifact) {
    File pomFile = new File(includedFile.getParentFile(), pomFileName);
    ProjectArtifactMetadata pom = new ProjectArtifactMetadata(artifact, pomFile);
    artifact.addMetadata(pom);
  }

  private void maybeAddMavenPluginMetaData(
      final DefaultArtifact artifact,
      final String groupId,
      final String pluginPrefix,
      final String artifactId)
  {
    if ("maven-plugin".equals(artifact.getType())) {
      // if we have a "main" artifact with type of "maven-plugin"
      // it's a Maven Plugin, Group level MD needs to be added too
      artifact.addMetadata(getGroupRepositoryMetadata(groupId, pluginPrefix, artifactId));
    }
  }

  private static GroupRepositoryMetadata getGroupRepositoryMetadata(
      final String groupId,
      final String pluginPrefix,
      final String artifactId)
  {
    GroupRepositoryMetadata groupMetadata = new GroupRepositoryMetadata(groupId);
    // Note: we "simulate" the name with artifactId, same what maven-plugin-plugin
    // would do. Impact is minimal, as we don't know any tool that _uses_ the name
    // from Plugin entries. Once the "index file" is properly solved,
    // or, we are able to properly persist Artifact instances above
    // (to preserve attached metadatas like this G level, and reuse
    // deployer without reimplementing it), all this will become unneeded.
    groupMetadata.addPluginMapping(pluginPrefix, artifactId, artifactId);
    return groupMetadata;
  }

  /**
   * Just a "fake" synthetic handler, to overcome Maven2/3 differences (no extension setter in M2 but there is in M3 on
   * {@link org.apache.maven.artifact.handler.DefaultArtifactHandler}.
   */
  public static class FakeArtifactHandler
      extends DefaultArtifactHandler
  {
    private final String extension;

    public FakeArtifactHandler(final String type, final String extension) {
      super(checkNotNull(type));
      this.extension = checkNotNull(extension);
    }

    @Override
    public String getExtension() {
      return extension;
    }
  }
}
