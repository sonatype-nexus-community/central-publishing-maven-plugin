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
package org.sonatype.central.publisher.client;

import java.nio.file.Path;
import java.util.Map;

import org.sonatype.central.publisher.client.httpclient.ComponentPublishedEndpoint;
import org.sonatype.central.publisher.client.httpclient.StatusPublisherEndpoint;
import org.sonatype.central.publisher.client.httpclient.UploadPublisherEndpoint;
import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;
import org.sonatype.central.publisher.client.httpclient.auth.AuthProviderFactory;
import org.sonatype.central.publisher.client.httpclient.auth.AuthProviderType;
import org.sonatype.central.publisher.client.httpclient.utils.PublisherBundle;
import org.sonatype.central.publisher.client.httpclient.utils.PublisherBundle.BundleBuilder;
import org.sonatype.central.publisher.client.model.DeploymentApiResponse;
import org.sonatype.central.publisher.client.model.PublishingType;

import static org.sonatype.central.publisher.client.PublisherConstants.COMPONENT_NAMESPACE_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.COMPONENT_NAME_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.COMPONENT_VERSION_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.DEFAULT_CENTRAL_BASEURL;
import static org.sonatype.central.publisher.client.PublisherConstants.DEPLOYMENT_ID_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.DEPLOYMENT_NAME_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.DEPLOYMENT_PUBLISHING_TYPE_QUERY_PARAM;

class PublisherClientImpl
    implements PublisherClient
{
  private String centralBaseUrl;

  private AuthProvider authProvider;

  private final UploadPublisherEndpoint uploadPublisherEndpoint;

  private final StatusPublisherEndpoint statusPublisherEndpoint;

  private final ComponentPublishedEndpoint componentPublishedEndpoint;

  private final AuthProviderFactory authProviderFactory;

  public PublisherClientImpl(
      final UploadPublisherEndpoint uploadPublisherEndpoint,
      final StatusPublisherEndpoint statusPublisherEndpoint,
      final ComponentPublishedEndpoint componentPublishedEndpoint,
      final AuthProviderFactory authProviderFactory)
  {
    this.uploadPublisherEndpoint = uploadPublisherEndpoint;
    this.statusPublisherEndpoint = statusPublisherEndpoint;
    this.componentPublishedEndpoint = componentPublishedEndpoint;
    this.authProviderFactory = authProviderFactory;
  }

  @Override
  public PublisherBundle compose(final Path sourceDir, final String bundleFileName) {
    return new BundleBuilder(sourceDir).bundleName(bundleFileName).addAllSourceFiles().build();
  }

  @Override
  public PublisherBundle compose(final Path sourceDir, final Path destDir, final String bundleFileName) {
    return new BundleBuilder(sourceDir).destPath(destDir).bundleName(bundleFileName).addAllSourceFiles()
        .build();
  }

  @Override
  public BundleBuilder getBuilder(final Path sourceDir) {
    return new BundleBuilder(sourceDir);
  }

  @Override
  public String upload(final String name, final Path body, final PublishingType publishingType) {
    Map<String, String> queryParams = authProvider().getQueryParams();
    queryParams.put(DEPLOYMENT_NAME_QUERY_PARAM, name);
    queryParams.put(DEPLOYMENT_PUBLISHING_TYPE_QUERY_PARAM, publishingType.name());
    return uploadPublisherEndpoint.call(centralBaseUrl(), authProvider(), queryParams, body);
  }

  @Override
  public DeploymentApiResponse status(final String deploymentId) {
    Map<String, String> queryParams = authProvider().getQueryParams();
    queryParams.put(DEPLOYMENT_ID_QUERY_PARAM, deploymentId);
    return statusPublisherEndpoint.call(centralBaseUrl(), authProvider(), queryParams);
  }

  @Override
  public boolean isPublished(final String namespace, final String name, final String version) {
    Map<String, String> queryParams = authProvider().getQueryParams();
    queryParams.put(COMPONENT_NAMESPACE_QUERY_PARAM, namespace);
    queryParams.put(COMPONENT_NAME_QUERY_PARAM, name);
    queryParams.put(COMPONENT_VERSION_QUERY_PARAM, version);
    return componentPublishedEndpoint.call(centralBaseUrl(), authProvider(), queryParams);
  }

  @Override
  public AuthProvider setAuthProvider(
      final AuthProviderType authProviderType,
      final String organizationId,
      final String userId,
      final String principal,
      final String credential)
  {
    authProvider = authProviderFactory.get(authProviderType, organizationId, userId, principal, credential);
    return authProvider;
  }

  private AuthProvider authProvider() {
    if (authProvider == null) {
      throw new IllegalStateException("An AuthProvider has not been set!");
    }
    return authProvider;
  }

  @Override
  public void setCentralBaseUrl(final String centralBaseUrl) {
    this.centralBaseUrl = centralBaseUrl;
  }

  private String centralBaseUrl() {
    return centralBaseUrl != null ? centralBaseUrl : DEFAULT_CENTRAL_BASEURL;
  }
}
