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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.sonatype.central.publisher.client.utils.PathUtils.PathOf;

public class PublisherBundleTest
{
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

  @Test
  public void createBundle_ChooseFiles() throws IOException {

    String bundleDir = "/bundle1";
    Path sourcePath = PathOf(TEMP_DIR, bundleDir);
    List<Path> paths = createTestFilesForBundle(sourcePath);
    PublisherBundle bundle = null;
    try {
      bundle = new PublisherBundle.BundleBuilder(sourcePath).bundleName("test1.zip")
          .add(paths.get(0)).add(paths.get(1)).add(paths.get(2)).add(paths.get(3))
          .add(paths.get(8)).add(paths.get(9)).add(paths.get(10)).add(paths.get(11))
          .build();

      assertNotNull(bundle);
      assertEquals("test1.zip", bundle.getName());
      assertTrue(bundle.getPath().toString().startsWith(sourcePath.toString()));
      assertTrue(bundle.getPath().toString().endsWith("test1.zip"));
      assertTrue(bundle.getPath().toFile().exists());
      assertThat(bundle.getFiles(),
          containsInAnyOrder(paths.get(0), paths.get(1), paths.get(2), paths.get(3), paths.get(8), paths.get(9),
              paths.get(10), paths.get(11)));
    }
    finally {
      for (Path path : paths) {
        Files.deleteIfExists(path);
      }
      if (bundle != null) {
        Files.deleteIfExists(bundle.getPath());
      }
    }
  }

  @Test
  public void createBundle_AllFiles() throws IOException {

    String bundleDir = "/bundle1";
    Path sourcePath = PathOf(TEMP_DIR, bundleDir);
    List<Path> paths = createTestFilesForBundle(sourcePath);
    PublisherBundle bundle = null;
    try {
      bundle = new PublisherBundle.BundleBuilder(sourcePath)
          .addAllSourceFiles()
          .build();

      assertNotNull(bundle);
      assertEquals("bundle.zip", bundle.getName());
      assertTrue(bundle.getPath().toString().startsWith(sourcePath.toString()));
      assertTrue(bundle.getPath().toString().endsWith("bundle.zip"));
      assertTrue(bundle.getPath().toFile().exists());
      assertThat(bundle.getFiles(), containsInAnyOrder(paths.toArray()));
    }
    finally {
      for (Path path : paths) {
        Files.deleteIfExists(path);
      }
      if (bundle != null) {
        Files.deleteIfExists(bundle.getPath());
      }
    }
  }

  @Test
  public void createBundle_AllFiles_WithDestination() throws IOException {

    String bundleDir = "/bundle1";
    Path sourcePath = PathOf(TEMP_DIR, bundleDir);
    Path destPath = PathOf(TEMP_DIR, "/output");
    List<Path> paths = createTestFilesForBundle(sourcePath);
    PublisherBundle bundle = null;
    try {
      bundle = new PublisherBundle.BundleBuilder(sourcePath)
          .destPath(destPath)
          .addAllSourceFiles()
          .build();

      assertNotNull(bundle);
      assertEquals("bundle.zip", bundle.getName());
      assertTrue(bundle.getPath().toString().startsWith(destPath.toString()));
      assertTrue(bundle.getPath().toString().endsWith("bundle.zip"));
      assertTrue(bundle.getPath().toFile().exists());
      assertThat(bundle.getFiles(), containsInAnyOrder(paths.toArray()));
    }
    finally {
      for (Path path : paths) {
        Files.deleteIfExists(path);
      }
      if (bundle != null) {
        Files.deleteIfExists(bundle.getPath());
      }
    }
  }

  private List<Path> createTestFilesForBundle(Path sourcePath) throws IOException {
    List<Path> paths = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      if (i == 0) {
        paths.add(sourcePath.resolve(PathOf("file.pom")));
        paths.add(sourcePath.resolve(PathOf("file.jar")));
        paths.add(sourcePath.resolve(PathOf("file.pom.md5")));
        paths.add(sourcePath.resolve(PathOf("file.jar.md5")));
      }
      else {
        paths.add(sourcePath.resolve(PathOf("subdir" + i, "file" + i + ".pom")));
        paths.add(sourcePath.resolve(PathOf("subdir" + i, "file" + i + ".jar")));
        paths.add(sourcePath.resolve(PathOf("subdir" + i, "file" + i + ".pom.md5")));
        paths.add(sourcePath.resolve(PathOf("subdir" + i, "file" + i + ".jar.md5")));
      }
    }
    for (Path path : paths) {
      Files.deleteIfExists(path);
      Files.createDirectories(path.getParent());
      Files.createFile(path);
    }
    return paths;
  }
}
