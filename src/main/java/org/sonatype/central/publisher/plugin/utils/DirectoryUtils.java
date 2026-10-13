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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DirectoryUtils
{
  /**
   * Checks if the given directory contains any files.
   *
   * @param directory the directory to check
   * @return true if the directory contains files, false otherwise
   */
  public static boolean hasFiles(final File directory) {
    try {
      if (directory.exists()) {
        Path path = directory.toPath();
        if (Files.exists(path) && Files.isDirectory(path)) {
          try (Stream<Path> paths = Files.list(path)) {
            return paths.findFirst().isPresent();
          }
        }
      }
    }
    catch(IOException ignored){
    }
    return false;
  }
}