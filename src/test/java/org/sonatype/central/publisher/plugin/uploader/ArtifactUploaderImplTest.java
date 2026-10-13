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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sonatype.central.publisher.client.PublisherClient;
import org.sonatype.central.publisher.client.model.PublishingType;
import org.sonatype.central.publisher.plugin.model.UploadArtifactRequest;

import org.apache.maven.plugins.annotations.Component;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ArtifactUploaderImplTest
{
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

  @Mock
  PublisherClient publisherClient;

  @Component
  ArtifactUploader artifactUploader;

  private Path testBundle;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    artifactUploader = new ArtifactUploaderImpl(publisherClient);

    testBundle = Files.createTempFile(FileSystems.getDefault().getPath(TEMP_DIR), "bundle", "zip");
  }

  @After
  public void cleanup() throws IOException {
    if (testBundle != null) {
      Files.deleteIfExists(testBundle);
    }
  }

  @Test
  public void uploadTest_Success()  {
    String expectedDeploymentId = "test-deployment-id";

    when(publisherClient.upload("deployment-name", testBundle, PublishingType.USER_MANAGED)).thenReturn(expectedDeploymentId);

    UploadArtifactRequest uploadArtifactRequest =
        new UploadArtifactRequest("deployment-name", testBundle, PublishingType.USER_MANAGED);

    String deploymentId = artifactUploader.upload(uploadArtifactRequest);
    assertEquals(expectedDeploymentId, deploymentId);
  }

  @Test
  public void uploadWhenUserTokenAuth() {
    String expectedDeploymentId = "test-deployment-id";

    when(publisherClient.upload("deployment-name", testBundle, PublishingType.USER_MANAGED)).thenReturn(expectedDeploymentId);

    UploadArtifactRequest uploadArtifactRequest =
        new UploadArtifactRequest("deployment-name", testBundle, PublishingType.USER_MANAGED);

    String deploymentId = artifactUploader.upload(uploadArtifactRequest);
    assertEquals(expectedDeploymentId, deploymentId);
  }
}
