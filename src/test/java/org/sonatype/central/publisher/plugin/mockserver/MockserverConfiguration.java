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
package org.sonatype.central.publisher.plugin.mockserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.central.publisher.client.model.DeploymentState;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockserver.client.MockServerClient;

import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.MediaType.APPLICATION_JSON;
import static org.sonatype.central.publisher.client.PublisherConstants.COMPONENT_NAMESPACE_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.COMPONENT_NAME_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.COMPONENT_VERSION_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.DEPLOYMENT_ID_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.HTTP_AUTHORIZATION_HEADER;
import static org.sonatype.central.publisher.client.PublisherConstants.HTTP_USERTOKEN_AUTH_SCHEME;
import static org.sonatype.central.publisher.client.PublisherConstants.PUBLISHED_ENDPOINT_URL;
import static org.sonatype.central.publisher.client.PublisherConstants.STATUS_ENDPOINT_URL;
import static org.sonatype.central.publisher.client.PublisherConstants.UPLOAD_ENDPOINT_URL;
import static org.sonatype.central.publisher.client.model.DeploymentState.PUBLISHED;
import static org.sonatype.central.publisher.client.model.DeploymentState.PUBLISHING;
import static org.sonatype.central.publisher.client.model.DeploymentState.VALIDATED;

public class MockserverConfiguration
{
  private static final String PUBLISH_SINGLE_MODULE_IT_AUTH =
      HTTP_USERTOKEN_AUTH_SCHEME + " dGVzdF91c2VyOnRlc3RfcGFzc3dvcmQ=";

  private static final String PUBLISH_USER_TOKEN_IT_AUTH =
      HTTP_USERTOKEN_AUTH_SCHEME + " dGVzdF9uYW1lY29kZTp0ZXN0X3Bhc3Njb2Rl";

  private static final String PUBLISH_WAIT_FOR_DEPLOYED_IT_AUTH =
      HTTP_USERTOKEN_AUTH_SCHEME + " dGVzdF91c2VyX3dhaXRfZm9yX3B1Ymxpc2g6dGVzdF9wYXNzd29yZF93YWl0X2Zvcl9wdWJsaXNo";

  private static final String PUBLISH_SINGLE_MODULE_WITHOUT_CHECKSUMS_IT_AUTH =
      HTTP_USERTOKEN_AUTH_SCHEME + " dGVzdF91c2VyX3dpdGhvdXRfY2hlY2tzdW1zOnRlc3RfcGFzc3dvcmRfd2l0aG91dF9jaGVja3N1bXM=";

  private static final String PUBLISH_SINGLE_MODULE_WITH_REQUIRED_CHECKSUMS_IT_AUTH =
      HTTP_USERTOKEN_AUTH_SCHEME + " dGVzdF91c2VyX3dpdGhfcmVxdWlyZWRfY2hlY2tzdW1zOnRlc3RfcGFzc3dvcmRfd2l0aF9yZXF1aXJlZF9jaGVja3N1bXM=";

  private static final String PUBLISH_POM_MODULE_IT_AUTH =
      HTTP_USERTOKEN_AUTH_SCHEME + " dGVzdF91c2VyX3BvbV9tb2R1bGU6dGVzdF9wYXNzd29yZF9wb21fbW9kdWxl";

  private static final String PUBLISH_SINGLE_MODULE_IT_DEPLOYMENT_ID = "a_deployment_id";

  private static final String PUBLISH_POM_MODULE_IT_DEPLOYMENT_ID = "a_deployment_id_pom_module";

  private static final String PUBLISH_USER_TOKEN_IT_DEPLOYMENT_ID = "a_deployment_id_user_token_auth";

  private static final String PUBLISH_WAIT_FOR_DEPLOYED_IT_DEPLOYMENT_ID = "a_deployment_id_wait_for_publish";

  private static final String PUBLISH_SINGLE_MODULE_WITHOUT_CHECKSUMS_IT_DEPLOYMENT_ID = "a_deployment_id_without_checksums";

  private static final String PUBLISH_SINGLE_MODULE_WITH_REQUIRED_CHECKSUMS_IT_AUTH_IT_DEPLOYMENT_ID = "a_deployment_id_with_required_checksums";

  private static final String COMPONENT_PUBLISHED_TRUE = "{\"published\":true}";

  private static final String COMPONENT_PUBLISHED_FALSE = "{\"published\":false}";

  private static final String HTTP_POST_METHOD = "POST";

  private static final String HTTP_GET_METHOD = "GET";

  private static final String HTTP_PUT_METHOD = "PUT";

  private static final int HTTP_STATUS_OK = 200;

  private static final int HTTP_STATUS_CREATED = 201;

  private final String baseUrl;

  private final int port;

  private final ObjectMapper objectMapper;

  public MockserverConfiguration(final String baseUrl, final int port) {
    this.baseUrl = baseUrl;
    this.port = port;
    objectMapper = new ObjectMapper();
  }

  public void apply() {
    try {
      MockServerClient mockServerClient = new MockServerClient(baseUrl, port);
      // for publish-single-module-it
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(UPLOAD_ENDPOINT_URL)
              .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_SINGLE_MODULE_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_CREATED).withBody(PUBLISH_SINGLE_MODULE_IT_DEPLOYMENT_ID));
      // for publish-pom-module-no-local-repository-files-it
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(UPLOAD_ENDPOINT_URL)
              .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_POM_MODULE_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_CREATED).withBody(PUBLISH_POM_MODULE_IT_DEPLOYMENT_ID));
      // for publish-single-module-with-usertoken-it
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(UPLOAD_ENDPOINT_URL)
              .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_USER_TOKEN_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_CREATED).withBody(PUBLISH_USER_TOKEN_IT_DEPLOYMENT_ID));
      // for publish-single-module-wait-for-publish-it
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(UPLOAD_ENDPOINT_URL)
              .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_WAIT_FOR_DEPLOYED_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_CREATED).withBody(PUBLISH_WAIT_FOR_DEPLOYED_IT_DEPLOYMENT_ID));
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(STATUS_ENDPOINT_URL)
          .withQueryStringParameter(DEPLOYMENT_ID_QUERY_PARAM, PUBLISH_WAIT_FOR_DEPLOYED_IT_DEPLOYMENT_ID)
          .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_WAIT_FOR_DEPLOYED_IT_AUTH), once()).respond(
          response().withStatusCode(HTTP_STATUS_OK)
              .withBody(objectMapper.writeValueAsString(buildDeploymentApiResponse(VALIDATED)), APPLICATION_JSON));
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(STATUS_ENDPOINT_URL)
          .withQueryStringParameter(DEPLOYMENT_ID_QUERY_PARAM, PUBLISH_WAIT_FOR_DEPLOYED_IT_DEPLOYMENT_ID)
          .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_WAIT_FOR_DEPLOYED_IT_AUTH), once()).respond(
          response().withStatusCode(HTTP_STATUS_OK)
              .withBody(objectMapper.writeValueAsString(buildDeploymentApiResponse(PUBLISHING)), APPLICATION_JSON));
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(STATUS_ENDPOINT_URL)
          .withQueryStringParameter(DEPLOYMENT_ID_QUERY_PARAM, PUBLISH_WAIT_FOR_DEPLOYED_IT_DEPLOYMENT_ID)
          .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_WAIT_FOR_DEPLOYED_IT_AUTH), once()).respond(
          response().withStatusCode(HTTP_STATUS_OK)
              .withBody(objectMapper.writeValueAsString(buildDeploymentApiResponse(PUBLISHED)), APPLICATION_JSON));

      // for publish-single-module-without-checksums-it
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(UPLOAD_ENDPOINT_URL)
              .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_SINGLE_MODULE_WITHOUT_CHECKSUMS_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_CREATED)
              .withBody(PUBLISH_SINGLE_MODULE_WITHOUT_CHECKSUMS_IT_DEPLOYMENT_ID));

      // for publish-single-module-with-required-checksums-it
      mockServerClient.when(request().withMethod(HTTP_POST_METHOD).withPath(UPLOAD_ENDPOINT_URL)
              .withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_SINGLE_MODULE_WITH_REQUIRED_CHECKSUMS_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_CREATED)
              .withBody(PUBLISH_SINGLE_MODULE_WITH_REQUIRED_CHECKSUMS_IT_AUTH_IT_DEPLOYMENT_ID));

      // for ignore published components
      mockServerClient.when(request().withMethod(HTTP_GET_METHOD).withPath(PUBLISHED_ENDPOINT_URL)
              .withQueryStringParameter(COMPONENT_NAMESPACE_QUERY_PARAM, "org.sonatype.central")
              .withQueryStringParameter(COMPONENT_NAME_QUERY_PARAM, "bundle-bom-parent-module-it-published")
              .withQueryStringParameter(COMPONENT_VERSION_QUERY_PARAM, "1.0.0"))
          .respond(response().withStatusCode(HTTP_STATUS_OK)
              .withBody(COMPONENT_PUBLISHED_TRUE));
      mockServerClient.when(request().withMethod(HTTP_GET_METHOD).withPath(PUBLISHED_ENDPOINT_URL)
              .withQueryStringParameter(COMPONENT_NAMESPACE_QUERY_PARAM, "org.sonatype.central")
              .withQueryStringParameter(COMPONENT_NAME_QUERY_PARAM, "child11")
              .withQueryStringParameter(COMPONENT_VERSION_QUERY_PARAM, "1.0.0"))
          .respond(response().withStatusCode(HTTP_STATUS_OK)
              .withBody(COMPONENT_PUBLISHED_FALSE));
      mockServerClient.when(request().withMethod(HTTP_GET_METHOD).withPath(PUBLISHED_ENDPOINT_URL)
              .withQueryStringParameter(COMPONENT_NAMESPACE_QUERY_PARAM, "org.sonatype.central")
              .withQueryStringParameter(COMPONENT_NAME_QUERY_PARAM, "child22")
              .withQueryStringParameter(COMPONENT_VERSION_QUERY_PARAM, "1.0.0"))
          .respond(response().withStatusCode(HTTP_STATUS_OK)
              .withBody(COMPONENT_PUBLISHED_FALSE));
      mockServerClient.when(request().withMethod(HTTP_GET_METHOD).withPath(PUBLISHED_ENDPOINT_URL)
              .withQueryStringParameter(COMPONENT_NAMESPACE_QUERY_PARAM, "org.sonatype.central")
              .withQueryStringParameter(COMPONENT_NAME_QUERY_PARAM, " bundle-bom-module-it")
              .withQueryStringParameter(COMPONENT_VERSION_QUERY_PARAM, "1.0.0"))
          .respond(response().withStatusCode(HTTP_STATUS_OK)
              .withBody(COMPONENT_PUBLISHED_FALSE));
      mockServerClient.when(request().withMethod(HTTP_GET_METHOD).withPath(PUBLISHED_ENDPOINT_URL)
              .withQueryStringParameter(COMPONENT_NAMESPACE_QUERY_PARAM, "org.sonatype.central"))
          .respond(response().withStatusCode(HTTP_STATUS_OK)
              .withBody(COMPONENT_PUBLISHED_FALSE));

      // for publish-snapshots-single-module-it
      mockServerClient
          .when(request()
              .withMethod(HTTP_PUT_METHOD)
              .withPath("/snapshots/.*"))
          //.withHeader(HTTP_AUTHORIZATION_HEADER, PUBLISH_SINGLE_MODULE_IT_AUTH), once())
          .respond(response().withStatusCode(HTTP_STATUS_OK)
              .withBody(PUBLISH_SINGLE_MODULE_IT_DEPLOYMENT_ID));
    }
    catch (IOException e) {
      System.out.println("FAILED TO CONFIGURE MOCKSERVER!" + e.getMessage());
      e.printStackTrace();
    }
  }

  private Map<String, Object> buildDeploymentApiResponse(final DeploymentState deploymentState) {
    Map<String, Object> deploymentApiResponse = new HashMap<>();
    deploymentApiResponse.put("deploymentId", PUBLISH_WAIT_FOR_DEPLOYED_IT_DEPLOYMENT_ID);
    deploymentApiResponse.put("deploymentName", "Deployment");
    deploymentApiResponse.put("deploymentState", deploymentState);
    deploymentApiResponse.put("purls", Collections.singletonList("pkg:maven/namespace/name@1.0"));
    deploymentApiResponse.put("errors", new HashMap<>());
    deploymentApiResponse.put("warnings", new ArrayList<>());
    return deploymentApiResponse;
  }

  public static void main(String[] args) {
    if (args == null || args.length < 1) {
      throw new IllegalArgumentException("Must supply the port of the mockserver as an argument!");
    }

    int mockserverPort = Integer.parseInt(args[0]);

    MockserverConfiguration mockserverConfiguration = new MockserverConfiguration("localhost", mockserverPort);
    mockserverConfiguration.apply();
  }
}
