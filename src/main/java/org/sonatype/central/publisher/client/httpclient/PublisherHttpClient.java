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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;

import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.net.URIBuilder;

public class PublisherHttpClient
{
  public static String sendRequest(
      final AuthProvider authProvider,
      final String endpointUrl,
      final Map<String, String> params,
      final Path body,
      final RequestType requestType) throws IOException
  {
    try {
      URIBuilder uriBuilder = new URIBuilder(endpointUrl);
      params.forEach(uriBuilder::addParameter);

      switch (requestType) {
        case POST: {
          HttpPost httpPost = new HttpPost(uriBuilder.build());
          authProvider.getAuthHeaders().forEach(httpPost::addHeader);

          if (body != null) {
            File file = body.toFile();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.LEGACY);
            builder.addBinaryBody("bundle", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
            httpPost.setEntity(builder.build());
          }

          try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(httpPost, new BasicHttpClientResponseHandler());
          }
        }
        case GET:
        default: {
          HttpGet httpGet = new HttpGet(uriBuilder.build());
          authProvider.getAuthHeaders().forEach(httpGet::addHeader);
          try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(httpGet, new BasicHttpClientResponseHandler());
          }
        }
      }
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }
}
