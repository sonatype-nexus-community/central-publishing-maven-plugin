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

import java.nio.file.Path;

import org.sonatype.central.publisher.client.model.PublishingType;

public class UploadArtifactRequest
{
  private final String deploymentName;

  private final Path bundleFile;

  private final PublishingType publishingType;

  public UploadArtifactRequest(
      final String deploymentName,
      final Path bundleFile,
      final PublishingType publishingType
  )
  {
    this.deploymentName = deploymentName;
    this.bundleFile = bundleFile;
    this.publishingType = publishingType;
  }

  public String getDeploymentName() {
    return deploymentName;
  }

  public Path getBundleFile() {
    return bundleFile;
  }

  public PublishingType getPublishingType() {
    return publishingType;
  }
}
