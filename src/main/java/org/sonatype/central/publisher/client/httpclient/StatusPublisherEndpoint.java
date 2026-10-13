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
import java.util.Map;

import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;
import org.sonatype.central.publisher.client.model.DeploymentApiResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.HttpResponseException;

import static org.sonatype.central.publisher.client.PublisherConstants.STATUS_ENDPOINT_URL;
import static org.sonatype.central.publisher.client.httpclient.PublisherHttpClient.sendRequest;
import static org.sonatype.central.publisher.client.httpclient.utils.HttpResponseUtil.toContentString;

public class StatusPublisherEndpoint
{
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public DeploymentApiResponse call(
      final String baseUrl,
      final AuthProvider authProvider,
      final Map<String, String> params)
  {
    try {
      String response = sendRequest(authProvider, baseUrl + STATUS_ENDPOINT_URL, params, null, RequestType.POST);
      return objectMapper.readValue(response, DeploymentApiResponse.class);
    }
    catch (HttpResponseException e) {
      throw new RuntimeException("Cannot get deployment status. Response status code: "
          + e.getStatusCode()
          + " response message: "
          + toContentString(e));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
