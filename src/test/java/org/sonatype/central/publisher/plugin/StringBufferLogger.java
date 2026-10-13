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
package org.sonatype.central.publisher.plugin;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public class StringBufferLogger
    extends AbstractLogger
{
  private final StringBuilder stringBuilder = new StringBuilder();

  public String getLoggedStuff() {
    return stringBuilder.toString();
  }

  public StringBufferLogger(final String name) {
    super(LEVEL_INFO, name);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    stringBuilder.append(message).append("\n");
  }

  @Override
  public void info(String message, Throwable throwable) {
    stringBuilder.append(message).append("\n");
  }

  @Override
  public void fatalError(String message, Throwable throwable) {
    stringBuilder.append(message).append("\n");
  }

  @Override
  public void error(String message, Throwable throwable) {
    stringBuilder.append(message).append("\n");
  }

  @Override
  public void debug(String message, Throwable throwable) {
    stringBuilder.append(message).append("\n");
  }

  @Override
  public Logger getChildLogger(String name) {
    return this;
  }
}
