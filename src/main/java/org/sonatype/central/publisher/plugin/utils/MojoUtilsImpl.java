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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import static org.sonatype.central.publisher.plugin.Constants.CLI_EXECUTION_ID;
import static org.sonatype.central.publisher.plugin.Constants.DEFAULT_BUILD_DIR_NAME;

@Component(role = MojoUtils.class)
public class MojoUtilsImpl
    extends AbstractLogEnabled
    implements MojoUtils
{
  @Override
  public boolean isThisLastProjectWithThisMojoInExecution(
      final MavenSession mavenSession,
      final MojoExecution mojoExecution,
      final String pluginGroupId,
      final String pluginArtifactId,
      final boolean failOnBuildFailure)
  {
    boolean result;
    if (Objects.equals(mojoExecution.getExecutionId(), CLI_EXECUTION_ID)) {
      result = isCurrentTheLastProjectInExecution(mavenSession);
    }
    else {
      result = isCurrentTheLastProjectWithMojoInExecution(mavenSession, pluginGroupId, pluginArtifactId,
          mojoExecution.getGoal());
    }
    if (result) {
      if (mavenSession.isParallel()) {
        waitForOtherProjectsIfNeeded(mavenSession);
      }

      if (mavenSession.getResult().hasExceptions()) {
        if (failOnBuildFailure) {
          getLogger().info("Earlier build failures detected. Central publishing will not continue.");
          return false;
        }
        else {
          getLogger().warn(
              "Earlier build failures detected. Central publishing is configured to not detect build failures, continuing...");
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean isThisFirstProjectWithThisMojoInExecution(
      final MavenSession mavenSession,
      final MojoExecution mojoExecution,
      final String pluginGroupId,
      final String pluginArtifactId)
  {
    final MavenProject currentProject = mavenSession.getCurrentProject();
    final MavenProject firstProject =
        getFirstProjectWithThisPluginDefined(mavenSession, pluginGroupId, pluginArtifactId, null);
    return currentProject == firstProject;
  }

  @Override
  public File getWorkDirectoryRoot(
      final String relativePath,
      final MavenSession mavenSession,
      final String pluginGroupId,
      final String pluginArtifactId,
      final String goal)
  {
    final MavenProject firstWithThisMojo =
        getFirstProjectWithThisPluginDefined(mavenSession, pluginGroupId, pluginArtifactId, goal);
    if (firstWithThisMojo != null) {
      final File firstWithThisMojoBuildDir;
      if (firstWithThisMojo.getBuild() != null && firstWithThisMojo.getBuild().getDirectory() != null) {
        firstWithThisMojoBuildDir =
            new File(firstWithThisMojo.getBuild().getDirectory()).getAbsoluteFile();
      }
      else {
        firstWithThisMojoBuildDir = new File(firstWithThisMojo.getBasedir().getAbsoluteFile(), DEFAULT_BUILD_DIR_NAME);
      }
      return new File(firstWithThisMojoBuildDir, relativePath);
    }

    return FileSystems
        .getDefault()
        .getPath(mavenSession.getExecutionRootDirectory(), DEFAULT_BUILD_DIR_NAME, relativePath).toFile();
  }

  private MavenProject getFirstProjectWithThisPluginDefined(
      final MavenSession mavenSession,
      final String pluginGroupId,
      final String pluginArtifactId,
      final String goal)
  {
    return mavenSession.getProjects().stream()
        .filter(mavenProject -> findPlugin(mavenProject.getBuild(), pluginGroupId, pluginArtifactId, goal) != null)
        .findFirst().orElse(null);
  }

  private boolean isCurrentTheLastProjectInExecution(final MavenSession mavenSession) {
    final MavenProject currentProject = mavenSession.getCurrentProject();
    final MavenProject lastProject = mavenSession.getProjects().get(mavenSession.getProjects().size() - 1);

    return currentProject == lastProject;
  }

  private boolean isCurrentTheLastProjectWithMojoInExecution(
      final MavenSession mavenSession,
      final String pluginGroupId,
      final String pluginArtifactId,
      final String goal)
  {
    return mavenSession.getCurrentProject() ==
        getLastProjectWithMojoInExecution(mavenSession, pluginGroupId, pluginArtifactId, goal);
  }

  private MavenProject getLastProjectWithMojoInExecution(
      final MavenSession mavenSession,
      final String pluginGroupId,
      final String pluginArtifactId,
      final String goal)
  {
    final ArrayList<MavenProject> projects = new ArrayList<>(mavenSession.getProjects());
    Collections.reverse(projects);
    return projects.stream()
        .filter(project -> findPlugin(project.getBuild(), pluginGroupId, pluginArtifactId, goal) != null).findFirst()
        .orElse(null);
  }

  private Plugin findPlugin(
      final PluginContainer container,
      final String pluginGroupId,
      final String pluginArtifactId,
      final String goal)
  {
    if (container != null) {
      return container.getPlugins().stream()
          .filter(plugin -> pluginMatches(plugin, pluginGroupId, pluginArtifactId, goal)).findFirst().orElse(null);
    }
    return null;
  }

  private boolean pluginMatches(
      final Plugin plugin,
      final String expectedPluginGroupId,
      final String expectedPluginArtifactId,
      final String expectedPluginGoal)
  {
    if (expectedPluginGroupId.equals(plugin.getGroupId()) && expectedPluginArtifactId.equals(plugin.getArtifactId())) {
      if (expectedPluginGoal != null) {
        return plugin.getExecutions().stream()
            .anyMatch(pluginExecution -> pluginExecution.getGoals().contains(expectedPluginGoal));
      }
      return true;
    }
    return false;
  }

  private void waitForOtherProjectsIfNeeded(final MavenSession mavenSession) {
    final MavenProject currentProject = mavenSession.getCurrentProject();

    while (true) {
      boolean done = true;
      for (MavenProject project : mavenSession.getProjects()) {
        if (currentProject != project && mavenSession.getResult().getBuildSummary(project) == null) {
          done = false;
          break;
        }
        else if (currentProject == project) {
          // we need to break, as "lastProjectWithThisMojo might != lastProjectInReactor"
          // and in that case we would block here indefinitely
          break;
        }
      }
      if (!done) {
        getLogger().info("Waiting for other projects build to finish...");
        try {
          TimeUnit.SECONDS.sleep(2L);
        }
        catch (InterruptedException e) {
          // nothing?
        }
      }
      else {
        return;
      }
    }
  }
}
