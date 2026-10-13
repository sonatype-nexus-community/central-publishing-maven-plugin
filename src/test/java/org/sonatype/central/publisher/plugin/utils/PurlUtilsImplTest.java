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
package org.sonatype.central.publisher.plugin.utils;

import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PurlUtilsImplTest
{
  @Test
  public void itShouldHandleInvalidPURLs(){
    PurlUtils subject = new PurlUtilsImpl();
    assertEquals(Optional.empty(), subject.toRepo1Url(null));
    assertEquals(Optional.empty(), subject.toRepo1Url(""));
    assertEquals(Optional.empty(), subject.toRepo1Url("invalid"));
  }

  @Test
  public void itShouldConvertValidPURLsToRepo1URls(){
    PurlUtils subject = new PurlUtilsImpl();
    assertEquals(Optional.of("https://repo1.maven.org/maven2/test/group/test-package/0.1.0/"), subject.toRepo1Url("pkg:maven/test.group/test-package@0.1.0"));
  }
}