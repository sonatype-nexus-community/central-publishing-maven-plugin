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

import org.sonatype.central.publisher.plugin.utils.MojoUtils;
import org.sonatype.central.publisher.plugin.utils.ProjectUtils;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractPublisherMojo
    extends AbstractMojo
{
  @Parameter(property = "skipPublishing", defaultValue = "false", required = true)
  private boolean skipPublishing;

  @Parameter(property = "failOnBuildFailure", defaultValue = "true")
  private boolean failOnBuildFailure;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  private MavenSession mavenSession;

  @Parameter(defaultValue = "${plugin.groupId}", readonly = true, required = true)
  private String pluginGroupId;

  @Parameter(defaultValue = "${plugin.artifactId}", readonly = true, required = true)
  private String pluginArtifactId;

  @Parameter(defaultValue = "${plugin.version}", readonly = true, required = true)
  private String pluginVersion;

  @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
  private MojoExecution mojoExecution;

  @Component
  private ArtifactFactory artifactFactory;

  @Component
  private MojoUtils mojoUtils;

  @Component
  private ProjectUtils projectUtils;

  protected MavenSession getMavenSession() {
    return mavenSession;
  }

  protected MojoExecution getMojoExecution() {
    return mojoExecution;
  }

  protected ArtifactFactory getArtifactFactory() {
    return artifactFactory;
  }

  protected MojoUtils getMojoUtils() {
    return mojoUtils;
  }

  protected ProjectUtils getProjectUtils() {
    return projectUtils;
  }

  protected String getPluginGroupId() {
    return pluginGroupId;
  }

  protected String getPluginArtifactId() {
    return pluginArtifactId;
  }

  protected boolean isFailOnBuildFailure() {
    return failOnBuildFailure;
  }

  protected boolean isSkipPublishing() {
    return skipPublishing;
  }

  protected String getPluginGav() {
    return pluginGroupId + ":" + pluginArtifactId + ":" + pluginVersion;
  }

  @Override
  public void execute()
      throws MojoExecutionException
  {
    validateParameters();
    doExecute();
  }

  public void validateParameters() throws MojoExecutionException {
    doValidateParameters();
  }

  protected abstract void doExecute() throws MojoExecutionException;

  protected abstract void doValidateParameters() throws MojoExecutionException;
}
