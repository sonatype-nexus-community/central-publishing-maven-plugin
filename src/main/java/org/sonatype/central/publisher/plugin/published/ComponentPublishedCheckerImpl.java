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
package org.sonatype.central.publisher.plugin.published;

import org.sonatype.central.publisher.client.PublisherClient;
import org.sonatype.central.publisher.client.PublisherClientFactory;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.console.ConsoleLogger;

@Component(role = ComponentPublishedChecker.class)
public class ComponentPublishedCheckerImpl
    extends AbstractLogEnabled
    implements ComponentPublishedChecker
{
  @Requirement
  private PublisherClient publisherClient;

  public ComponentPublishedCheckerImpl() {
  }

  public ComponentPublishedCheckerImpl(final PublisherClient publisherClient) {
    this.publisherClient = publisherClient != null ? publisherClient : PublisherClientFactory.createPublisherClient();
    if (this.getLogger() == null) {
      this.enableLogging(new ConsoleLogger());
    }
  }

  @Override
  public boolean isComponentPublished(final String groupId, final String artifactId, final String version) {
    getLogger().info(
        "Check component published status for component: groupId:" + groupId + " artifactId:" + artifactId +
            " version:" + version);
    boolean published = publisherClient.isPublished(groupId, artifactId, version);
    if (published) {
      getLogger().info("Excluding component: groupId:" + groupId + " artifactId:" + artifactId + " version:" + version +
          " as a published");
    }
    return published;
  }
}
