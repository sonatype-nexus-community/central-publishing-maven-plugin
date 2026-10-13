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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.sonatype.central.publisher.plugin.Constants.CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID;
import static org.sonatype.central.publisher.plugin.Constants.CENTRAL_PUBLISHING_PLUGIN_GROUP_ID;
import static org.sonatype.central.publisher.plugin.Constants.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID;
import static org.sonatype.central.publisher.plugin.Constants.MAVEN_DEPLOY_PLUGIN_GROUP_ID;
import static org.sonatype.central.publisher.plugin.Constants.NEXUS_STAGING_PLUGIN_ARTIFACT_ID;
import static org.sonatype.central.publisher.plugin.Constants.NEXUS_STAGING_PLUGIN_GROUP_ID;

public class DeployLifecycleParticipantTest
{
  private StringBufferLogger testLogger;

  private DeployLifecycleParticipant defaultLifecycleParticipant;

  @Before
  public void before() {
    testLogger = new StringBufferLogger("test");
    this.defaultLifecycleParticipant = new DeployLifecycleParticipant();
    this.defaultLifecycleParticipant.enableLogging(testLogger);
  }

  @Test
  public void replaceMavenDeployPluginExecution() throws MavenExecutionException {
    final Model model = createModel("simple");

    model.getBuild().getPlugins().add(
        createPlugin(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy"));

    // our plugin just declared as extension. no goals
    model.getBuild().getPlugins().add(createPlugin(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID,
        CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    List<Plugin> plugins = mockSession.getProjects().get(0).getModel().getBuild().getPlugins();
    assertEquals(2, plugins.size());

    assertMavenDeployPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    assertTrue(testLogger.getLoggedStuff().contains("Installing Central Publishing"));
  }

  @Test
  public void replaceNexusStagingPluginExecution()
      throws MavenExecutionException
  {
    final Model model = createModel("simple");

    model.getBuild().getPlugins().add(
        createPlugin(NEXUS_STAGING_PLUGIN_GROUP_ID, NEXUS_STAGING_PLUGIN_ARTIFACT_ID, "deploy"));

    // our plugin just declared as extension, nothing more. no goals
    model.getBuild().getPlugins().add(createPlugin(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID,
        CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    List<Plugin> plugins = mockSession.getProjects().get(0).getModel().getBuild().getPlugins();
    assertEquals(2, plugins.size());

    assertNexusStagingPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    assertTrue(testLogger.getLoggedStuff().contains("Installing Central Publishing"));
  }

  @Test
  public void replaceBothMavenDeployAndNexusStagingPluginExecution()
      throws MavenExecutionException
  {
    final Model model = createModel("simple");

    model.getBuild().getPlugins().add(
        createPlugin(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy"));
    model.getBuild().getPlugins().add(
        createPlugin(NEXUS_STAGING_PLUGIN_GROUP_ID, NEXUS_STAGING_PLUGIN_ARTIFACT_ID, "deploy"));

    // our plugin just declared as extension, nothing more. no goals
    model.getBuild().getPlugins().add(createPlugin(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID,
        CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    List<Plugin> plugins = mockSession.getProjects().get(0).getModel().getBuild().getPlugins();
    assertEquals(3, plugins.size());

    assertNexusStagingPlugin(plugins, 0);
    assertMavenDeployPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    assertTrue(testLogger.getLoggedStuff().contains("Installing Central Publishing"));
  }

  @Test
  public void notReplacingWhenManuallySetPlugin()
      throws MavenExecutionException
  {
    final Model model = createModel("simple-but-manually-set");

    model.getBuild().getPlugins().add(
        createPlugin(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy"));
    model.getBuild().getPlugins().add(
        createPlugin(NEXUS_STAGING_PLUGIN_GROUP_ID, NEXUS_STAGING_PLUGIN_ARTIFACT_ID, "deploy"));

    // out plugin declared as extension with manually set execution
    model.getBuild().getPlugins().add(createPlugin(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID,
        CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, "publish"));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    List<Plugin> plugins = mockSession.getProjects().get(0).getModel().getBuild().getPlugins();
    assertEquals(3, plugins.size());

    assertNexusStagingPlugin(plugins, 1);
    assertMavenDeployPlugin(plugins, 1);
    assertCentralPublishingPlugin(plugins, 1);

    assertTrue(testLogger.getLoggedStuff().contains("Not installing Central Publishing"));
    assertTrue(testLogger.getLoggedStuff().contains("Preexisting publish related goal bindings found in 1 modules."));
  }

  @Test
  public void notReplacingWhenNoMavenDeployOrNexusStagingExecutions()
      throws MavenExecutionException
  {
    final Model model = createModel("test");
    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    List<Plugin> plugins = mockSession.getProjects().get(0).getModel().getBuild().getPlugins();
    assertTrue(plugins.isEmpty());
  }

  @Test
  public void replacePluginInMultiModuleStructure()
      throws MavenExecutionException
  {
    final Model aggregatorPom = createModel("aggregator");

    // parent-A:
    final Model parentA = createModel("parent-a");
    parentA.getBuild().getPlugins().add(
        createPlugin(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy"));
    // our plugin declared as extension, no execution and A specific param
    parentA.getBuild().getPlugins().add(
        createPluginAndSetConfig(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, null,
            "profile-a"));

    // module-A:
    final Model moduleA = createModel("module-a");
    moduleA.getBuild().getPlugins().add(
        createPlugin(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy"));
    // our plugin declared as extension, no execution and A specific param
    moduleA.getBuild().getPlugins().add(
        createPluginAndSetConfig(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, null,
            "profile-a"));

    // parent-B:
    final Model parentB = createModel("parent-b");
    parentB.getBuild().getPlugins().add(
        createPlugin(NEXUS_STAGING_PLUGIN_GROUP_ID, NEXUS_STAGING_PLUGIN_ARTIFACT_ID, "deploy"));
    // our plugin declared as extension, no execution and A specific param
    parentB.getBuild().getPlugins().add(
        createPluginAndSetConfig(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, null,
            "profile-b"));

    // module-B:
    final Model moduleB = createModel("module-b");
    moduleB.getBuild().getPlugins().add(
        createPlugin(NEXUS_STAGING_PLUGIN_GROUP_ID, NEXUS_STAGING_PLUGIN_ARTIFACT_ID, "deploy"));
    // our plugin declared as extension, no execution and A specific param
    moduleB.getBuild().getPlugins().add(
        createPluginAndSetConfig(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, null,
            "profile-b"));

    final MavenSession mockSession =
        createSessionWithProjectsForModels(aggregatorPom, parentA, moduleA, parentB, moduleB);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    // parent-A:
    List<Plugin> plugins = mockSession.getProjects().get(1).getModel().getBuild().getPlugins();
    assertEquals(2, plugins.size());
    assertMavenDeployPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    // module-A:
    plugins = mockSession.getProjects().get(2).getModel().getBuild().getPlugins();
    assertEquals(2, plugins.size());
    assertMavenDeployPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    // parent-B:
    plugins = mockSession.getProjects().get(3).getModel().getBuild().getPlugins();
    assertEquals(2, plugins.size());
    assertNexusStagingPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    // module-B:
    plugins = mockSession.getProjects().get(4).getModel().getBuild().getPlugins();
    assertEquals(2, plugins.size());
    assertNexusStagingPlugin(plugins, 0);
    assertCentralPublishingPlugin(plugins, 1);

    assertTrue(testLogger.getLoggedStuff().contains("Installing Central Publishing"));
  }

  @Test
  public void throwsExceptionWhenMultipleDeclarations() {
    final Model model = createModel("bad");
    model.getBuild().getPlugins().add(
        createPlugin(MAVEN_DEPLOY_PLUGIN_GROUP_ID, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID, "deploy"));
    // our plugin declared MULTIPLE TIMES
    model.getBuild().getPlugins().add(
        createPluginAndSetConfig(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, null,
            "profile-a"));
    model.getBuild().getPlugins().add(
        createPluginAndSetConfig(CENTRAL_PUBLISHING_PLUGIN_GROUP_ID, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID, null,
            "profile-a"));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    Exception exception = assertThrows(
        MavenExecutionException.class,
        () -> defaultLifecycleParticipant.afterProjectsRead(mockSession)
    );
    assertEquals(
        "The build contains multiple versions of plugin " + CENTRAL_PUBLISHING_PLUGIN_GROUP_ID + ":" +
            CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID,
        exception.getMessage()
    );
  }

  private Plugin createPlugin(String groupId, String artifactId) {
    final Plugin plugin = new Plugin();
    plugin.setGroupId(groupId);
    plugin.setArtifactId(artifactId);
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    plugin.setConfiguration(configuration);
    return plugin;
  }

  private Plugin createPlugin(String groupId, String artifactId, String goal) {
    final Plugin plugin = createPlugin(groupId, artifactId);
    if (goal != null && goal.trim().length() > 0) {
      final PluginExecution execution = new PluginExecution();
      execution.setId("default-" + goal);
      execution.setPhase("deploy");
      execution.getGoals().add(goal);
      plugin.getExecutions().add(execution);
    }
    return plugin;
  }

  private Plugin createPluginAndSetConfig(String groupId, String artifactId, String goal, String configValue) {
    final Plugin plugin = createPlugin(groupId, artifactId, goal);
    plugin.setGroupId(groupId);
    plugin.setArtifactId(artifactId);
    Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
    Xpp3Dom foo = new Xpp3Dom("aSwitch");
    foo.setValue(configValue);
    configuration.addChild(foo);
    return plugin;
  }

  private MavenSession createSessionWithProjectsForModels(Model... models) {
    final MavenSession mockSession = Mockito.mock(MavenSession.class);
    final ArrayList<MavenProject> projects = new ArrayList<MavenProject>();
    for (Model model : models) {
      final MavenProject mockProject = Mockito.mock(MavenProject.class);
      Mockito.when(mockProject.getModel()).thenReturn(model);
      Mockito.when(mockProject.getGroupId()).thenReturn(model.getGroupId());
      Mockito.when(mockProject.getArtifactId()).thenReturn(model.getArtifactId());
      Mockito.when(mockProject.getVersion()).thenReturn(model.getVersion());
      projects.add(mockProject);
    }
    Mockito.when(mockSession.getProjects()).thenReturn(projects);
    return mockSession;
  }

  private Model createModel(String artifactId) {
    final Model model = new Model();
    model.setGroupId("org.foo");
    model.setArtifactId(artifactId);
    model.setVersion("1.0");
    model.setBuild(new Build());
    return model;
  }

  private void assertMavenDeployPlugin(final List<Plugin> plugins, final int executionsCount) {
    Optional<Plugin> plugin = getByArtifactId(plugins, MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID);
    assertPlugin(plugin, executionsCount);
  }

  private void assertNexusStagingPlugin(final List<Plugin> plugins, final int executionsCount) {
    Optional<Plugin> plugin = getByArtifactId(plugins, NEXUS_STAGING_PLUGIN_ARTIFACT_ID);
    assertPlugin(plugin, executionsCount);
  }

  private void assertCentralPublishingPlugin(final List<Plugin> plugins, final int executionsCount) {
    Optional<Plugin> plugin = getByArtifactId(plugins, CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID);
    assertPlugin(plugin, executionsCount);
  }

  private void assertPlugin(Optional<Plugin> plugin, final int executionsCount) {
    assertTrue(plugin.isPresent());
    assertEquals(executionsCount, plugin.get().getExecutions().size());
  }

  private Optional<Plugin> getByArtifactId(final List<Plugin> plugins, final String artifactId) {
    return plugins.stream().filter(plugin -> artifactId.equals(plugin.getArtifactId())).findAny();
  }
}
