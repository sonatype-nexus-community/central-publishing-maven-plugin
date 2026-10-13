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

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ComponentPublishedCheckerImplTest
{
  @Mock
  PublisherClient publisherClient;

  @Requirement
  ComponentPublishedCheckerImpl componentPublishedChecker;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    componentPublishedChecker = new ComponentPublishedCheckerImpl(publisherClient);
    componentPublishedChecker.enableLogging(new ConsoleLogger());
  }

  @Test
  public void isComponentPublished_Published() {
    String namespace = "namespace1";
    String name = "name1";
    String version = "1.0.0";
    when(publisherClient.isPublished(namespace, name, version)).thenReturn(true);
    boolean result = componentPublishedChecker.isComponentPublished(namespace, name, version);
    assertTrue(result);
  }

  @Test
  public void isComponentPublished_NotPublished() {
    String namespace = "namespace1";
    String name = "name1";
    String version = "1.0.0";
    when(publisherClient.isPublished(namespace, name, version)).thenReturn(false);
    boolean result = componentPublishedChecker.isComponentPublished(namespace, name, version);
    assertFalse(result);
  }
}