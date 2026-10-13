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

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

@Component(role = PurlUtils.class)
public class PurlUtilsImpl
    extends AbstractLogEnabled
    implements PurlUtils
{
  private static final String REPO1_URL = "https://repo1.maven.org/maven2/";

  @Override
  public Optional<String> toRepo1Url(final String purl) {
    try {
      PackageURL packageURL = new PackageURL(purl);
      if (null != packageURL.getNamespace() && null != packageURL.getName() && null != packageURL.getVersion()) {
        String url = REPO1_URL +
            packageURL.getNamespace().replaceAll("\\.", "/") + "/" +
            packageURL.getName() + "/" +
            packageURL.getVersion() + "/";
        return Optional.of(url);
      }
    }
    catch (MalformedPackageURLException e) {
      if (null != getLogger()) {
        getLogger().debug("Invalid package URL returned: " + purl, e);
      }
    }
    return Optional.empty();
  }
}
