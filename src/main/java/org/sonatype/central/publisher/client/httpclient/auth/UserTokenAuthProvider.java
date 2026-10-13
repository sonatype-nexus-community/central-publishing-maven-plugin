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

import static org.sonatype.central.publisher.client.PublisherConstants.HTTP_USERTOKEN_AUTH_SCHEME;

/**
 * An AuthProvider that will add the Authorization header to the request, using the UserToken AuthScheme as needed by
 * Central's UserToken authentication implementation
 */
public class UserTokenAuthProvider
    extends AbstractAuthProvider
{
  public UserTokenAuthProvider(
      final String organizationId,
      final String userId,
      final String nameCode,
      final String passCode)
  {
    super(organizationId, userId, nameCode, passCode);
  }

  @Override
  protected String getAuthScheme() {
    return HTTP_USERTOKEN_AUTH_SCHEME;
  }
}
