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

public class AuthProviderFactory
{
  public AuthProvider get(
      final AuthProviderType authProviderType,
      final String organizationId,
      final String userId,
      final String principal,
      final String credential)
  {
    if (authProviderType == null) {
      throw new IllegalArgumentException("Must provide a valid authProviderType for authentication!");
    }
    if (organizationId == null) {
      throw new IllegalArgumentException("Must provide a valid organizationId for authentication!");
    }
    if (userId == null) {
      throw new IllegalArgumentException("Must provide a valid userId for authentication!");
    }
    if (principal == null) {
      throw new IllegalArgumentException("Must provide a valid principal for authentication!");
    }
    if (credential == null) {
      throw new IllegalArgumentException("Must provide a valid credential for authentication!");
    }

    return new UserTokenAuthProvider(organizationId, userId, principal, credential);
  }
}
