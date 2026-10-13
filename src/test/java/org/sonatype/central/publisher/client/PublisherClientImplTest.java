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

import org.sonatype.central.publisher.client.httpclient.ComponentPublishedEndpoint;
import org.sonatype.central.publisher.client.httpclient.StatusPublisherEndpoint;
import org.sonatype.central.publisher.client.httpclient.UploadPublisherEndpoint;
import org.sonatype.central.publisher.client.httpclient.auth.AuthProvider;
import org.sonatype.central.publisher.client.httpclient.auth.AuthProviderFactory;
import org.sonatype.central.publisher.client.httpclient.auth.UserTokenAuthProvider;
import org.sonatype.central.publisher.client.model.DeploymentApiResponse;
import org.sonatype.central.publisher.client.model.PublishingType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.sonatype.central.publisher.client.PublisherConstants.DEFAULT_ORGANIZATION_ID;
import static org.sonatype.central.publisher.client.httpclient.auth.AuthProviderType.USERTOKEN;
import static org.sonatype.central.publisher.client.utils.PathUtils.PathOf;

public class PublisherClientImplTest
{
  @Mock
  private UploadPublisherEndpoint uploadPublisherEndpoint;

  @Mock
  private StatusPublisherEndpoint statusPublisherEndpoint;

  @Mock
  private ComponentPublishedEndpoint componentPublishedEndpoint;

  @Mock
  private AuthProviderFactory authProviderFactory;

  @Mock
  private PublisherClientImpl underTest;

  private AutoCloseable mockAnnotations;

  @Before
  public void setup() {
    mockAnnotations = openMocks(this);
    underTest = new PublisherClientImpl(uploadPublisherEndpoint, statusPublisherEndpoint, componentPublishedEndpoint,
        authProviderFactory);
    when(authProviderFactory.get(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode")).thenReturn(
        new UserTokenAuthProvider(DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode"));
  }

  @After
  public void cleanup() throws Exception {
    if (mockAnnotations != null) {
      mockAnnotations.close();
    }
  }

  @Test
  public void testSetAuthProvider_usertokenAuth() {
    AuthProvider authProvider =
        underTest.setAuthProvider(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode");
    assertThat(authProvider, instanceOf(UserTokenAuthProvider.class));
    assertThat(authProvider.getOrganizationId(), is(DEFAULT_ORGANIZATION_ID));
    assertThat(authProvider.getUserId(), is("username"));
    assertThat(authProvider.getPrincipal(), is("namecode"));
    assertThat(authProvider.getCredential(), is("passcode"));
  }

  @Test
  public void testUpload_usertokenAuth() {
    AuthProvider authProvider =
        underTest.setAuthProvider(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode");
    when(uploadPublisherEndpoint.call(any(), eq(authProvider), any(), any())).thenReturn("OK");
    assertThat(underTest.upload("deploymentName", PathOf("a/path"), PublishingType.USER_MANAGED), is("OK"));
  }

  @Test
  public void testStatus_usertokenAuth() {
    AuthProvider authProvider =
        underTest.setAuthProvider(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode");
    DeploymentApiResponse deploymentApiResponse = new DeploymentApiResponse();
    deploymentApiResponse.setDeploymentId("deploymentId");
    when(statusPublisherEndpoint.call(any(), eq(authProvider), any())).thenReturn(deploymentApiResponse);
    assertThat(underTest.status("deploymentName").getDeploymentId(), is("deploymentId"));
  }

  @Test
  public void testPublished_usertokenAuth() {
    AuthProvider authProvider =
        underTest.setAuthProvider(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode");
    when(componentPublishedEndpoint.call(any(), eq(authProvider), any())).thenReturn(true);
    assertThat(underTest.isPublished("namespace1", "name1", "1.0.0"), is(true));
  }
}
