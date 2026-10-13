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
import java.util.HashMap;
import java.util.Map;

import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.HttpResponseException;

import static org.sonatype.central.publisher.client.PublisherConstants.PUBLISHED_ENDPOINT_URL;
import static org.sonatype.central.publisher.client.httpclient.PublisherHttpClient.sendRequest;
import static org.sonatype.central.publisher.client.httpclient.utils.HttpResponseUtil.toContentString;

public class ComponentPublishedEndpoint
{
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public boolean call(
      final String baseUrl,
      final AuthProvider authProvider,
      final Map<String, String> params)
  {
    try {
      String response = sendRequest(authProvider, baseUrl + PUBLISHED_ENDPOINT_URL, params, null, RequestType.GET);
      Map<String, Boolean> result = objectMapper.readValue(response, new TypeReference<HashMap<String, Boolean>>() { });
      return result.get("published");
    }
    catch (HttpResponseException e) {
      throw new RuntimeException("Cannot get component published status. Response status code: "
          + e.getStatusCode()
          + " response message: "
          + toContentString(e));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
