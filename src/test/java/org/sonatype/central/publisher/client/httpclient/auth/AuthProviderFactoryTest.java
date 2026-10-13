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
package org.sonatype.central.publisher.client.httpclient.auth;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.sonatype.central.publisher.client.PublisherConstants.DEFAULT_ORGANIZATION_ID;
import static org.sonatype.central.publisher.client.httpclient.auth.AuthProviderType.USERTOKEN;

public class AuthProviderFactoryTest
{
  private AuthProviderFactory underTest;

  @Before
  public void setup() {
    underTest = new AuthProviderFactory();
  }

  @Test
  public void testGet_UserTokenAuthProvider() {
    AuthProvider authProvider = underTest.get(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "namecode", "passcode");
    assertThat(authProvider, instanceOf(UserTokenAuthProvider.class));
  }

  @Test
  public void testGet_NullAuthProviderType() {
    IllegalArgumentException thrown = assertThrows(
        "IllegalArgumentException was expected",
        IllegalArgumentException.class,
        () -> underTest.get(null, DEFAULT_ORGANIZATION_ID, "username", "username", "password"));

    assertThat(thrown.getMessage(), is("Must provide a valid authProviderType for authentication!"));
  }

  @Test
  public void testGet_NullOrganizationId() {
    IllegalArgumentException thrown = assertThrows(
        "IllegalArgumentException was expected",
        IllegalArgumentException.class,
        () -> underTest.get(USERTOKEN, null, "username", "username", "password"));

    assertThat(thrown.getMessage(), is("Must provide a valid organizationId for authentication!"));
  }

  @Test
  public void testGet_NullUserId() {
    IllegalArgumentException thrown = assertThrows(
        "IllegalArgumentException was expected",
        IllegalArgumentException.class,
        () -> underTest.get(USERTOKEN, DEFAULT_ORGANIZATION_ID, null, "username", "password"));

    assertThat(thrown.getMessage(), is("Must provide a valid userId for authentication!"));
  }

  @Test
  public void testGet_NullPrincipal() {
    IllegalArgumentException thrown = assertThrows(
        "IllegalArgumentException was expected",
        IllegalArgumentException.class,
        () -> underTest.get(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", null, "password"));

    assertThat(thrown.getMessage(), is("Must provide a valid principal for authentication!"));
  }

  @Test
  public void testGet_NullCredential() {
    IllegalArgumentException thrown = assertThrows(
        "IllegalArgumentException was expected",
        IllegalArgumentException.class,
        () -> underTest.get(USERTOKEN, DEFAULT_ORGANIZATION_ID, "username", "username", null));

    assertThat(thrown.getMessage(), is("Must provide a valid credential for authentication!"));
  }
}
