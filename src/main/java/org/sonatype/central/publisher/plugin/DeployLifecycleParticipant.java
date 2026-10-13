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

import java.util.Collection;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import static org.sonatype.central.publisher.plugin.Constants.CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID;
import static org.sonatype.central.publisher.plugin.Constants.CENTRAL_PUBLISHING_PLUGIN_GROUP_ID;
import static org.sonatype.central.publisher.plugin.Constants.DEPLOY_PHASE;
import static org.sonatype.central.publisher.plugin.Constants.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID;
import static org.sonatype.central.publisher.plugin.Constants.MAVEN_DEPLOY_PLUGIN_GROUP_ID;
import static org.sonatype.central.publisher.plugin.Constants.NEXUS_STAGING_PLUGIN_GROUP_ID;
import static org.sonatype.central.publisher.plugin.Constants.NEXUS_STAGING_PLUGIN_ARTIFACT_ID;
import static org.sonatype.central.publisher.plugin.Constants.PUBLISH_GOAL;
import static org.sonatype.central.publisher.plugin.Constants.PUBLISH_GOAL_ID;

@Component(
    role = AbstractMavenLifecycleParticipant.class,
    hint = "org.sonatype.central.publisher.plugin.DeployLifecycleParticipant"
)
public class DeployLifecycleParticipant
    extends AbstractMavenLifecycleParticipant
    implements LogEnabled
{
  protected Logger logger;

  @Override
  public void enableLogging(final Logger logger) {
    this.logger = logger;
  }

  public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
    try {
      logger.info("Inspecting build with total of " + session.getProjects().size() + " modules");

      long publishGoalsFoundInModules = getCentralPublishingPluginExecutions(session.getProjects());
      if (publishGoalsFoundInModules > 0) {
        logger.info("Not installing Central Publishing features. " +
            "Preexisting publish related goal bindings found in " + publishGoalsFoundInModules + " modules.");
        return;
      }

      logger.info("Installing Central Publishing features");
      for (MavenProject project : session.getProjects()) {
        final Plugin centralPublishingPlugin = getCentralPublishingPlugin(project.getModel());

        if (centralPublishingPlugin != null) {
          maybeSkipMavenDeployPlugin(project.getModel(), centralPublishingPlugin);
          maybeSkipNexusStagingPlugin(project.getModel(), centralPublishingPlugin);
        }
      }
    }
    catch (IllegalStateException e) {
      throw new MavenExecutionException(e.getMessage(), e);
    }
  }

  private long getCentralPublishingPluginExecutions(final List<MavenProject> projects) {
    return projects.stream()
        .map(project -> getCentralPublishingPlugin(project.getModel()))
        .filter(this::existsWithExecutions)
        .map(Plugin::getExecutions)
        .flatMap(Collection::stream)
        .map(PluginExecution::getGoals)
        .filter(goal -> goal.contains(PUBLISH_GOAL))
        .count();
  }

  private boolean existsWithExecutions(final Plugin plugin) {
    return plugin != null && !plugin.getExecutions().isEmpty();
  }

  private void maybeSkipMavenDeployPlugin(final Model model, final Plugin centralPublishingPlugin) {
    final Plugin mavenDeployPlugin = getMavenDeployPlugin(model);
    if (mavenDeployPlugin != null) {
      mavenDeployPlugin.getExecutions().clear();
      setUpCentralPublishingExecution(centralPublishingPlugin);
    }
  }

  private void maybeSkipNexusStagingPlugin(final Model model, final Plugin centralPublishingPlugin) {
    final Plugin nexusStagingPlugin = getNexusStagingPlugin(model);
    if (nexusStagingPlugin != null) {
      nexusStagingPlugin.getExecutions().clear();
      setUpCentralPublishingExecution(centralPublishingPlugin);
    }
  }

  private void setUpCentralPublishingExecution(final Plugin centralPublishingPlugin) {
    final PluginExecution execution = new PluginExecution();
    execution.setId(PUBLISH_GOAL_ID);
    execution.getGoals().add(PUBLISH_GOAL);
    execution.setPhase(DEPLOY_PHASE);
    execution.setConfiguration(centralPublishingPlugin.getConfiguration());

    if (centralPublishingPlugin.getExecutions().stream().noneMatch(item -> execution.getId().equals(item.getId()))) {
      centralPublishingPlugin.getExecutions().add(execution);
    }
  }

  private Plugin getNexusStagingPlugin(final Model model) {
    if (model.getBuild() != null) {
      return getPluginByCoordinatesFromContainer(NEXUS_STAGING_PLUGIN_GROUP_ID, NEXUS_STAGING_PLUGIN_ARTIFACT_ID,
          model.getBuild());
    }
    return null;
  }

  private Plugin getMavenDeployPlugin(final Model model) {
    if (model.getBuild() != null) {
      return getPluginByCoordinatesFromContainer(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID,
          model.getBuild());
    }
    return null;
  }

  private Plugin getCentralPublishingPlugin(final Model model) {
    if (model.getBuild() != null) {
      return getPluginByCoordinatesFromContainer(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID,
          CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID,
          model.getBuild());
    }
    return null;
  }

  private Plugin getPluginByCoordinatesFromContainer(
      final String groupId,
      final String artifactId,
      final PluginContainer pluginContainer)
  {
    Plugin result = null;
    for (Plugin plugin : pluginContainer.getPlugins()) {
      if (nullToEmpty(groupId).equals(nullToEmpty(plugin.getGroupId())) &&
          nullToEmpty(artifactId).equals(nullToEmpty(plugin.getArtifactId()))) {
        if (result != null) {
          throw new IllegalStateException(
              "The build contains multiple versions of plugin " + groupId + ":" + artifactId);
        }
        result = plugin;
      }
    }
    return result;
  }

  private String nullToEmpty(String value) {
    if (value == null || value.trim().length() == 0) {
      return "";
    }
    return value;
  }
}
