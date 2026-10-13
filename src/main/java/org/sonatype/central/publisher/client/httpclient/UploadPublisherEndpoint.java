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
import java.nio.file.Path;
import java.util.Map;

import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;

import org.apache.hc.client5.http.HttpResponseException;

import static org.sonatype.central.publisher.client.PublisherConstants.UPLOAD_ENDPOINT_URL;
import static org.sonatype.central.publisher.client.httpclient.PublisherHttpClient.sendRequest;
import static org.sonatype.central.publisher.client.httpclient.RequestType.POST;
import static org.sonatype.central.publisher.client.httpclient.utils.HttpResponseUtil.toContentString;

public class UploadPublisherEndpoint
{
  public String call(
      final String baseUrl,
      final AuthProvider authProvider,
      final Map<String, String> params,
      final Path body)
  {
    try {
      return sendRequest(authProvider, baseUrl + UPLOAD_ENDPOINT_URL, params, body, POST);
    }
    catch (HttpResponseException e) {
      throw new RuntimeException(
          "Invalid request. Status: " + e.getStatusCode() + " Response body: " + toContentString(e));
    }
    catch (IOException e) {
      throw new RuntimeException("Invalid request. " + e.getMessage());
    }
  }
}
