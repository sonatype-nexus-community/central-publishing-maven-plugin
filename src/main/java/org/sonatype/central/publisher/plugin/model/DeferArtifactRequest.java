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
package org.sonatype.central.publisher.plugin.model;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;

public class DeferArtifactRequest
{
  private final MavenSession mavenSession;

  private final List<ArtifactWithFile> artifactWithFiles;

  private final File deferredDirectory;

  private final String centralSnapshotsUrl;

  private final String serverId;

  /**
   * Constructor
   *
   * @param mavenSession        - {@link MavenSession}
   * @param artifactWithFiles   - {@link List} of {@link ArtifactWithFile}
   * @param deferredDirectory   - {@link File} directory to defer the artifacts
   * @param centralSnapshotsUrl - {@link String} URL of the central snapshots
   * @param serverId  - {@link String} server id
   */
  public DeferArtifactRequest(
      final MavenSession mavenSession,
      final List<ArtifactWithFile> artifactWithFiles,
      final File deferredDirectory,
      final String centralSnapshotsUrl,
      final String serverId)
  {
    this.mavenSession = mavenSession;
    this.artifactWithFiles = artifactWithFiles;
    this.deferredDirectory = deferredDirectory;
    this.centralSnapshotsUrl = centralSnapshotsUrl;
    this.serverId = serverId;
  }

  public MavenSession getMavenSession() {
    return mavenSession;
  }

  public List<ArtifactWithFile> getArtifactWithFiles() {
    return artifactWithFiles;
  }

  public File getDeferredDirectory() {
    return deferredDirectory;
  }

  public String getCentralSnapshotsUrl() {
    return centralSnapshotsUrl;
  }

  public String getServerId() {
    return serverId;
  }
}
