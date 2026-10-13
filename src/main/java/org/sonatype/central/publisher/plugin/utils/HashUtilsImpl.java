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
import java.util.Optional;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Implementation of {@link HashUtils}
 */
@Component(role = HashUtils.class)
public class HashUtilsImpl
    extends AbstractLogEnabled
    implements HashUtils
{
  @Override
  public String hash(final File file, final HashAlgorithm algorithm) {
    try {
      return algorithm.function().hashBytes(Files.readAllBytes(file.toPath())).toString();
    }
    catch (IOException e) {
      getLogger().error("Failed to generate hash from " + file.getAbsolutePath() + " using algorithm " + algorithm, e);
    }

    return null;
  }

  @Override
  public File createChecksumFile(final File file, final HashAlgorithm algorithm) {
    File hashFile = new File(file.getParentFile(), file.getName() + "." + algorithm.name());

    try {
      String hash = hash(file, algorithm);
      if (hash != null) {
        return Files.write(hashFile.toPath(), hash.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING).toFile();
      }
    }
    catch (IOException e) {
      getLogger().error("Failed to generate checksum file at " + file.getAbsolutePath() + " using algorithm " + algorithm, e);
    }

    return null;
  }

  @Override
  public boolean isChecksumFile(final File file) {
    if (file.exists() && file.isFile()) {
      Optional<String> optionalExtension = getExtension(file.getName());

      if (optionalExtension.isPresent()) {
        switch (optionalExtension.get()) {
          case HashAlgorithm.MD5_NAME:
          case HashAlgorithm.SHA1_NAME:
          case HashAlgorithm.SHA256_NAME:
          case HashAlgorithm.SHA512_NAME:
            return true;
          default:
            return false;
        }
      }
    }

    return false;
  }

  @Override
  public boolean isSignatureFile(final File file) {
    if (file.exists() && file.isFile()) {
      return file.getName().endsWith(".asc");
    }
    return false;
  }

  private Optional<String> getExtension(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }
}
