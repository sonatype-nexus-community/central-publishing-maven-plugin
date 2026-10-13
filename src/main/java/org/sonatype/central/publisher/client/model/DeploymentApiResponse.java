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
package org.sonatype.central.publisher.client.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeploymentApiResponse
{
  private String deploymentId;

  private String deploymentName;

  private DeploymentState deploymentState;

  private List<String> purls;

  private Map<String, List<String>> errors;

  private List<String> warnings;

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(final String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getDeploymentName() {
    return deploymentName;
  }

  public void setDeploymentName(final String deploymentName) {
    this.deploymentName = deploymentName;
  }

  public DeploymentState getDeploymentState() {
    return deploymentState;
  }

  public void setDeploymentState(final DeploymentState deploymentState) {
    this.deploymentState = deploymentState;
  }

  public List<String> getPurls() {
    return purls;
  }

  public void setPurls(final List<String> purls) {
    this.purls = purls;
  }

  public Map<String, List<String>> getErrors() {
    return errors;
  }

  public void setErrors(final Map<String, List<String>> errors) {
    this.errors = errors;
  }

  public List<String> getWarnings() {
    return warnings == null ? Collections.emptyList() : warnings;
  }

  public void setWarnings(final List<String> warnings) {
    this.warnings = warnings;
  }
}
