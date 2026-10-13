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

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.central.publisher.client.PublisherConstants.DEFAULT_ORGANIZATION_ID;
import static org.sonatype.central.publisher.client.PublisherConstants.HTTP_AUTHORIZATION_HEADER;
import static org.sonatype.central.publisher.client.PublisherConstants.ORGANIZATION_ID_QUERY_PARAM;
import static org.sonatype.central.publisher.client.PublisherConstants.USER_ID_QUERY_PARAM;

public class UserTokenAuthProviderTest
{
  private UserTokenAuthProvider underTest;

  @Before
  public void setup() {
    underTest = new UserTokenAuthProvider(DEFAULT_ORGANIZATION_ID, "testUsername", "testNameCode", "testPassCode");
  }

  @Test
  public void testGetAuthScheme() {
    assertThat(underTest.getAuthScheme(), is("UserToken"));
  }

  @Test
  public void testGetAuthHeaders() {
    HashMap<String, String> map = new HashMap<>();
    map.put(HTTP_AUTHORIZATION_HEADER, "UserToken dGVzdE5hbWVDb2RlOnRlc3RQYXNzQ29kZQ==");
    assertThat(underTest.getAuthHeaders(), is(map));
  }

  @Test
  public void testGetQueryParams() {
    HashMap<String, String> map = new HashMap<>();
    map.put(ORGANIZATION_ID_QUERY_PARAM, "org");
    map.put(USER_ID_QUERY_PARAM, "testUsername");
    assertThat(underTest.getQueryParams(), is(map));
  }
}
