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
package org.sonatype.central.publisher.client.httpclient.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import static java.util.stream.Collectors.toSet;
import static org.sonatype.central.publisher.client.utils.PathUtils.PathOf;

public class PublisherBundle
{
  private final String name;

  private final Path path;

  private Set<Path> files = new HashSet<>();

  public PublisherBundle(final String name, final Path path) {
    this.name = name;
    this.path = path;
  }

  private PublisherBundle(final BundleBuilder bundleBuilder) {
    this.name = bundleBuilder.bundleName;
    this.path = bundleBuilder.bundlePath;
    this.files = bundleBuilder.files;
  }

  public String getName() {
    return name;
  }

  public Path getPath() {
    return path;
  }

  public List<Path> getFiles() {
    return new ArrayList<>(files);
  }

  public static class BundleBuilder
  {
    private Path sourcePath;

    private Path destPath;

    private Path bundlePath;

    private String bundleName;

    private final Set<Path> files;

    public BundleBuilder(final Path sourcePath) {
      this.sourcePath = sourcePath;
      this.destPath = sourcePath;
      this.bundleName = "bundle.zip";
      this.files = new HashSet<>();
    }

    public BundleBuilder sourcePath(Path sourcePath) {
      this.sourcePath = sourcePath;
      return this;
    }

    public BundleBuilder destPath(Path destPath) {
      this.destPath = destPath;
      return this;
    }

    public BundleBuilder bundleName(String bundleName) {
      this.bundleName = bundleName;
      return this;
    }

    public BundleBuilder add(Path filePath) {
      this.files.add(filePath);
      return this;
    }

    public BundleBuilder addAll(Set<Path> files) {
      this.files.addAll(files);
      return this;
    }

    public BundleBuilder addAllSourceFiles() {
      Set<Path> files;
      try (Stream<Path> paths = Files.walk(this.sourcePath)) {
        files = paths.filter(path -> path.toFile().isFile()).collect(toSet());
        this.files.addAll(files);
      }
      catch (IOException e) {
        throw new RuntimeException("Error on source dir traversal", e);
      }
      return this;
    }

    public PublisherBundle build() {
      this.bundlePath = createBundle();
      return new PublisherBundle(this);
    }

    private Path createBundle() {
      Path newBundle = PathOf(this.destPath.toString(), this.bundleName);

      try {
        Files.createDirectories(newBundle.getParent());
        Files.deleteIfExists(newBundle);
        Files.createFile(newBundle);
      }
      catch (IOException e) {
        throw new RuntimeException("File already exists: " + newBundle);
      }
      try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(newBundle.toFile()))) {
        for (Path filePath : this.files) {
          Path fileRelativePath = this.sourcePath.relativize(filePath);
          File bundleEntry = filePath.toFile();

          if (bundleEntry.isFile()) {
            zipOutputStream.putNextEntry(new ZipEntry(fileRelativePath.toString()));
            try (FileInputStream in = new FileInputStream(bundleEntry)) {
              IOUtils.copy(in, zipOutputStream);
            }

            zipOutputStream.closeEntry();
          }
        }

        zipOutputStream.finish();
      }
      catch (Exception e) {
        throw new RuntimeException("Error on bundle creation", e);
      }

      return newBundle;
    }
  }
}
