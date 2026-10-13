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
import java.io.IOException;

import org.sonatype.central.publisher.plugin.model.DeferArtifactRequest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Interface for deferring artifact installation and deployment.
 */
public interface ArtifactDeferrer
{
  /**
   * Install artifact with files.
   *
   * @param artifactWithFiles artifact with files to install
   * @throws ArtifactInstallationException if installation fails
   * @throws MojoExecutionException        if execution fails
   */
  void install(DeferArtifactRequest artifactWithFiles)
      throws ArtifactInstallationException, MojoExecutionException;

  /**
   * Deploy artifact.
   *
   * @param mavenSession     - {@link MavenSession}
   * @param sourceDirectory  - {@link File}
   * @param remoteRepository - {@link ArtifactRepository}
   * @throws ArtifactDeploymentException if deployment fails
   * @throws IOException                 if reading of index fails
   */
  @SuppressWarnings("deprecation")
  void deployUp(MavenSession mavenSession, File sourceDirectory, ArtifactRepository remoteRepository)
      throws ArtifactDeploymentException, IOException;
}
