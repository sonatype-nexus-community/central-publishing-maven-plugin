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
package org.sonatype.central.publisher.plugin.uploader;

import org.sonatype.central.publisher.client.PublisherClient;
import org.sonatype.central.publisher.client.PublisherClientFactory;
import org.sonatype.central.publisher.client.model.PublishingType;
import org.sonatype.central.publisher.plugin.exceptions.DeploymentPublishFailedException;
import org.sonatype.central.publisher.plugin.model.UploadArtifactRequest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import static java.lang.String.format;

@Component(role = ArtifactUploader.class)
public class ArtifactUploaderImpl
    extends AbstractLogEnabled
    implements ArtifactUploader
{
  @Requirement
  private PublisherClient publisherClient;

  @SuppressWarnings("unused") // used via reflection by Plexus
  public ArtifactUploaderImpl() { }

  ArtifactUploaderImpl(final PublisherClient publisherClient) {
    this.publisherClient = publisherClient != null ? publisherClient : PublisherClientFactory.createPublisherClient();
  }

  @Override
  public String upload(final UploadArtifactRequest uploadArtifactRequest) {
    if (getLogger() != null) {
      getLogger().info("Going to upload " + uploadArtifactRequest.getBundleFile());
    }

    try {
      String deploymentId = publisherClient.upload(
          uploadArtifactRequest.getDeploymentName(),
          uploadArtifactRequest.getBundleFile(),
          uploadArtifactRequest.getPublishingType()
      );

      if (getLogger() != null) {
        getLogger().info(
            format("Uploaded bundle successfully, deployment name: %s, deploymentId: %s. Deployment will %s",
                uploadArtifactRequest.getDeploymentName(), deploymentId,
                toPublishingTypeMessage(uploadArtifactRequest))
        );
      }

      return deploymentId;
    }
    catch (Exception e) {
      if (getLogger() != null) {
        getLogger().error("Unable to upload bundle for deployment: " + uploadArtifactRequest.getDeploymentName(), e);
      }

      throw new DeploymentPublishFailedException(uploadArtifactRequest.getDeploymentName());
    }
  }

  private String toPublishingTypeMessage(final UploadArtifactRequest uploadArtifactRequest) {
    PublishingType publishingType = uploadArtifactRequest.getPublishingType();

    switch (publishingType) {
      case USER_MANAGED:
        return "require manual publishing";
      case AUTOMATIC:
        return "publish automatically";
    }

    // should never be reached
    return "";
  }
}
