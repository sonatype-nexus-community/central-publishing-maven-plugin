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

import org.apache.maven.project.MavenProject;

public class BundleArtifactRequest
{
  private final MavenProject project;

  private final File stagingDirectory;

  private final File outputDirectory;

  private final String outputFilename;

  private final ChecksumRequest checksumRequest;

  public BundleArtifactRequest(
      final MavenProject project,
      final File stagingDirectory,
      final File outputDirectory,
      final String outputFilename,
      final ChecksumRequest checksumRequest
  )
  {
    this.project = project;
    this.stagingDirectory = stagingDirectory;
    this.outputDirectory = outputDirectory;
    this.outputFilename = outputFilename;
    this.checksumRequest = checksumRequest;
  }

  public MavenProject getProject() {
    return project;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public File getStagingDirectory() {
    return stagingDirectory;
  }

  public String getOutputFilename() {
    return outputFilename;
  }

  public ChecksumRequest getChecksumRequest() {
    return checksumRequest;
  }
}
