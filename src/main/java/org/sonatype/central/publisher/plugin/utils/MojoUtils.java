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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;

public interface MojoUtils
{
  boolean isThisLastProjectWithThisMojoInExecution(
      final MavenSession mavenSession,
      final MojoExecution mojoExecution,
      final String pluginGroupId,
      final String pluginArtifactId,
      final boolean failOnBuildFailure);

  boolean isThisFirstProjectWithThisMojoInExecution(
      final MavenSession mavenSession,
      final MojoExecution mojoExecution,
      final String pluginGroupId,
      final String pluginArtifactId);

  File getWorkDirectoryRoot(
      final String relativePath,
      final MavenSession mavenSession,
      final String pluginGroupId,
      final String pluginArtifactId,
      final String goal);
}
