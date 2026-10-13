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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sonatype.central.publisher.plugin.StringBufferLogger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertTrue;

/**
 * Maven 3.10 treats the {@code file://} staging repository as a local repository, and writes bookkeeping files into
 * it that Central rejects ("Bundle has content that does NOT have a .pom file"). These tests use the layout that
 * Maven 3.10.0 produced for a pom module, observed with a real build.
 */
public class ProjectUtilsImplLocalRepositoryFilesTest
{
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private ProjectUtilsImpl subject;

  private Path stagingDirectory;

  @Before
  public void setUp() throws Exception {
    subject = new ProjectUtilsImpl();
    subject.enableLogging(new StringBufferLogger("test"));
    stagingDirectory = temporaryFolder.newFolder("central-staging").toPath();
  }

  @Test
  public void itShouldRemoveTheFilesMaven310WritesIntoTheStagingDirectory() throws Exception {
    Path artifactDir = stagingDirectory.resolve("org/example/repro-bom");
    Path versionDir = artifactDir.resolve("1.0.0");
    touch(versionDir.resolve("repro-bom-1.0.0.pom"));
    touchWithChecksums(versionDir.resolve("repro-bom-1.0.0.pom"));
    // what Maven 3.10.0 adds
    touch(artifactDir.resolve("maven-metadata-local.xml"));
    touch(versionDir.resolve("_remote.repositories"));
    touchWithChecksums(versionDir.resolve("_remote.repositories"));
    Files.createDirectories(stagingDirectory.resolve(".locks"));
    touch(stagingDirectory.resolve(".locks/some.lock"));

    subject.deleteLocalRepositoryFiles(stagingDirectory);

    assertThat(relativeFiles(), containsInAnyOrder(
        "org/example/repro-bom/1.0.0/repro-bom-1.0.0.pom",
        "org/example/repro-bom/1.0.0/repro-bom-1.0.0.pom.md5",
        "org/example/repro-bom/1.0.0/repro-bom-1.0.0.pom.sha1",
        "org/example/repro-bom/1.0.0/repro-bom-1.0.0.pom.sha256",
        "org/example/repro-bom/1.0.0/repro-bom-1.0.0.pom.sha512"));
    assertTrue("the .locks directory should be gone", Files.notExists(stagingDirectory.resolve(".locks")));
  }

  @Test
  public void itShouldCoverEveryModuleOfAMultiModuleBuild() throws Exception {
    for (String module : new String[]{"parent", "child1", "child2"}) {
      Path artifactDir = stagingDirectory.resolve("org/example/" + module);
      touch(artifactDir.resolve("1.0.0/" + module + "-1.0.0.pom"));
      touch(artifactDir.resolve("maven-metadata-local.xml"));
      touch(artifactDir.resolve("1.0.0/_remote.repositories"));
    }

    subject.deleteLocalRepositoryFiles(stagingDirectory);

    assertThat(relativeFiles(), containsInAnyOrder(
        "org/example/parent/1.0.0/parent-1.0.0.pom",
        "org/example/child1/1.0.0/child1-1.0.0.pom",
        "org/example/child2/1.0.0/child2-1.0.0.pom"));
  }

  @Test
  public void itShouldLeaveAMaven39StagingDirectoryAlone() throws Exception {
    Path versionDir = stagingDirectory.resolve("org/example/lib/1.0.0");
    touch(versionDir.resolve("lib-1.0.0.pom"));
    touch(versionDir.resolve("lib-1.0.0.jar"));
    touch(versionDir.resolve("lib-1.0.0.jar.asc"));
    List<String> before = relativeFiles();

    subject.deleteLocalRepositoryFiles(stagingDirectory);

    assertThat(relativeFiles(), containsInAnyOrder(before.toArray(new String[0])));
  }

  @Test
  public void itShouldNotTouchAnArtifactWhoseNameOnlyStartsLikeOneOfThoseFiles() throws Exception {
    Path versionDir = stagingDirectory.resolve("org/example/maven-metadata-tools/1.0.0");
    touch(versionDir.resolve("maven-metadata-tools-1.0.0.pom"));
    touch(versionDir.resolve("maven-metadata-tools-1.0.0.jar"));

    subject.deleteLocalRepositoryFiles(stagingDirectory);

    assertThat(relativeFiles(), containsInAnyOrder(
        "org/example/maven-metadata-tools/1.0.0/maven-metadata-tools-1.0.0.pom",
        "org/example/maven-metadata-tools/1.0.0/maven-metadata-tools-1.0.0.jar"));
  }

  @Test
  public void itShouldRemoveALocksDirectoryThatIsNotAtTheTop() throws Exception {
    touch(stagingDirectory.resolve("org/example/lib/1.0.0/lib-1.0.0.pom"));
    touch(stagingDirectory.resolve("org/example/.locks/some.lock"));

    subject.deleteLocalRepositoryFiles(stagingDirectory);

    assertThat(relativeFiles(), containsInAnyOrder("org/example/lib/1.0.0/lib-1.0.0.pom"));
    assertTrue(Files.notExists(stagingDirectory.resolve("org/example/.locks")));
  }

  @Test
  public void itShouldCopeWithAStagingDirectoryThatDoesNotExist() throws Exception {
    Path missing = temporaryFolder.getRoot().toPath().resolve("never-created");

    subject.deleteLocalRepositoryFiles(missing);

    assertTrue(Files.notExists(missing));
  }

  @Test
  public void itShouldLeaveNothingBehindWhenOnlyLocalRepositoryFilesExist() throws Exception {
    touch(stagingDirectory.resolve("maven-metadata-local.xml"));

    subject.deleteLocalRepositoryFiles(stagingDirectory);

    assertThat(relativeFiles(), empty());
  }

  private void touch(final Path file) throws IOException {
    Files.createDirectories(file.getParent());
    Files.write(file, "x".getBytes("UTF-8"));
  }

  private void touchWithChecksums(final Path file) throws IOException {
    for (String algorithm : new String[]{"md5", "sha1", "sha256", "sha512"}) {
      touch(file.resolveSibling(file.getFileName() + "." + algorithm));
    }
  }

  private List<String> relativeFiles() throws IOException {
    try (Stream<Path> walk = Files.walk(stagingDirectory)) {
      return walk.filter(Files::isRegularFile)
          .map(path -> stagingDirectory.relativize(path).toString().replace('\\', '/'))
          .collect(Collectors.toList());
    }
  }
}
