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
package org.sonatype.central.publisher.plugin.config;

import org.sonatype.central.publisher.client.PublisherClient;
import org.sonatype.central.publisher.client.PublisherClientFactory;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

@Component(role = PlexusContextConfig.class)
public class PlexusContextConfigImpl implements PlexusContextConfig, Contextualizable
{
  @Override
  public void contextualize(final Context context) throws ContextException {
    PlexusContainer plexusContainer = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    PublisherClient publisherClient = PublisherClientFactory.createPublisherClient();
    plexusContainer.addComponent(publisherClient, PublisherClient.class.getName());
  }
}
