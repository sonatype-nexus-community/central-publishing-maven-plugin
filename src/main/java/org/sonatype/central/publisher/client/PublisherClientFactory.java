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
import org.sonatype.central.publisher.client.httpclient.auth.AuthProviderFactory;

public final class PublisherClientFactory
{
  private PublisherClientFactory() {
  }

  public static PublisherClient createPublisherClient() {
    return new PublisherClientImpl(new UploadPublisherEndpoint(), new StatusPublisherEndpoint(),
        new ComponentPublishedEndpoint(), new AuthProviderFactory());
  }
}
