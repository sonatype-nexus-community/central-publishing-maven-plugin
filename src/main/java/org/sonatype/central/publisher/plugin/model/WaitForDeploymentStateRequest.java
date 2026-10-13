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
package org.sonatype.central.publisher.plugin.model;

/**
 * Simple class to wrap common request values for requesting to wait of a Deployment state.
 */
public class WaitForDeploymentStateRequest
{
  private final String centralBaseUrl;

  private final String deploymentId;

  private final WaitUntilRequest waitUntilRequest;

  private final int waitMaxTimeInSeconds;

  private final int waitPollingIntervalInSeconds;

  public WaitForDeploymentStateRequest(
      final String centralBaseUrl,
      final String deploymentId,
      final WaitUntilRequest waitUntilRequest,
      final int waitMaxTimeInSeconds,
      final int waitPollingIntervalInSeconds
  )
  {
    this.centralBaseUrl = centralBaseUrl;
    this.deploymentId = deploymentId;
    this.waitUntilRequest = waitUntilRequest;
    this.waitMaxTimeInSeconds = waitMaxTimeInSeconds;
    this.waitPollingIntervalInSeconds = waitPollingIntervalInSeconds;
  }

  public String getCentralBaseUrl() {
    return centralBaseUrl;
  }

  public String waitTypeName() {
    return waitUntilRequest != null ? waitUntilRequest.name().toLowerCase() : "";
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public WaitUntilRequest getWaitUntilRequest() {
    return waitUntilRequest;
  }

  public int getWaitMaxTimeInSeconds() {
    return waitMaxTimeInSeconds;
  }

  public int getWaitPollingIntervalInSeconds() {
    return waitPollingIntervalInSeconds;
  }
}
