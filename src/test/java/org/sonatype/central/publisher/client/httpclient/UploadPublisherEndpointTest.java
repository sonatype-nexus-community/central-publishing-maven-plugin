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
package org.sonatype.central.publisher.client.httpclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;
import org.sonatype.central.publisher.client.httpclient.auth.UserTokenAuthProvider;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.sonatype.central.publisher.client.PublisherConstants.DEFAULT_ORGANIZATION_ID;

public class UploadPublisherEndpointTest
{
  @Test
  public void call_InvalidRequest() throws IOException {
    UploadPublisherEndpoint endpoint = new UploadPublisherEndpoint();
    AuthProvider authProvider =
        new UserTokenAuthProvider(DEFAULT_ORGANIZATION_ID, "testUsername", "testNamecode", "testPassCode");

    Path bodyFile = Files.createTempFile("body", ".txt");
    try {
      Files.write(bodyFile, "test string".getBytes());
      Exception exception = assertThrows(RuntimeException.class,
          () -> endpoint.call("https://central.sonatype.com", authProvider, authProvider.getQueryParams(), bodyFile));
      assertTrue(exception.getMessage().startsWith("Invalid request. Status:"));
    }
    finally {
      Files.delete(bodyFile);
    }
  }

  //@Test
  //@Disabled("for debugging/testing purpose only. Update test credentials to valid values before run")
  public void call_ValidRequest() throws IOException {
    UploadPublisherEndpoint endpoint = new UploadPublisherEndpoint();
    AuthProvider authProvider =
        new UserTokenAuthProvider(DEFAULT_ORGANIZATION_ID, "testUsername", "testNamecode", "testPassCode");
    Path bodyFile = Files.createTempFile("body", ".txt");
    try {
      Files.write(bodyFile, "test string".getBytes());
      String response = endpoint.call("https://central.sonatype.com", authProvider, authProvider.getQueryParams(), bodyFile);
      assertNotNull(response);
    }
    finally {
      Files.delete(bodyFile);
    }
  }
}
