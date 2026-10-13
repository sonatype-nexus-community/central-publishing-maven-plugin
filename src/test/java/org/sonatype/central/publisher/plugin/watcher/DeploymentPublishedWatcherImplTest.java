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
package org.sonatype.central.publisher.plugin.watcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.central.publisher.client.PublisherClient;
import org.sonatype.central.publisher.client.model.DeploymentApiResponse;
import org.sonatype.central.publisher.client.model.DeploymentState;
import org.sonatype.central.publisher.plugin.exceptions.DeploymentPublishFailedException;
import org.sonatype.central.publisher.plugin.exceptions.DeploymentPublishTimedOutException;
import org.sonatype.central.publisher.plugin.model.WaitForDeploymentStateRequest;
import org.sonatype.central.publisher.plugin.model.WaitUntilRequest;
import org.sonatype.central.publisher.plugin.utils.PurlUtils;

import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.central.publisher.plugin.Constants.CENTRAL_BASE_URL_DEFAULT_VALUE;
import static org.sonatype.central.publisher.plugin.Constants.WAIT_MAX_TIME_DEFAULT_VALUE;
import static org.sonatype.central.publisher.plugin.Constants.WAIT_POLLING_INTERVAL_DEFAULT_VALUE;
import static org.sonatype.central.publisher.plugin.model.WaitUntilRequest.PUBLISHED;
import static org.sonatype.central.publisher.plugin.model.WaitUntilRequest.UPLOADED;
import static org.sonatype.central.publisher.plugin.model.WaitUntilRequest.VALIDATED;

public class DeploymentPublishedWatcherImplTest
{
  public static final String TEST_DEPLOYMENT_ID = "test-deployment";

  public static final int waitMaxTimeDefaultValue = Integer.parseInt(WAIT_MAX_TIME_DEFAULT_VALUE);

  public static final int waitPollingIntervalDefaultValue = Integer.parseInt(WAIT_POLLING_INTERVAL_DEFAULT_VALUE);

  // timeout for deployment methods, adds 1 second to the default polling interval
  public static final int DEFAULT_DEPLOYMENT_TIMEOUT = 60000;

  @Mock
  PublisherClient publisherClient;

  @Mock
  PurlUtils purlUtils;

  @Mock
  Logger logger;

  DeploymentPublishedWatcherImpl deploymentPublishedWatcher;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    deploymentPublishedWatcher = new DeploymentPublishedWatcherImpl(publisherClient, purlUtils);
    deploymentPublishedWatcher.enableLogging(logger);
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPending_Should_FinishIfWaitingForDeploymentUploaded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(pendingResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(UPLOADED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentValidating_Should_FinishIfWaitingForDeploymentUploaded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(validatingResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(UPLOADED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentValidated_Should_FinishIfWaitingForDeploymentUploaded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(validatedResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(UPLOADED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublishing_Should_FinishIfWaitingForDeploymentUploaded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishingResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(UPLOADED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublished_Should_FinishIfWaitingForDeploymentUploaded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishedResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(UPLOADED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentValidating_Expect_ExceptionIfWaitingForDeploymentValidatedAndMaxTimeoutExceeded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(pendingResponse(), validatingResponse());

    // wait twice the length it takes to poll
    int amountOfTimesToChecksStatus = 2;
    int waitMaxTimeInSeconds = (waitPollingIntervalDefaultValue * amountOfTimesToChecksStatus);

    assertThrows(
        DeploymentPublishTimedOutException.class,
        () -> deploymentPublishedWatcher.waitForDeploymentState(
            buildWaitForDeploymentStateRequest(VALIDATED, waitMaxTimeInSeconds, waitPollingIntervalDefaultValue))
    );

    verify(publisherClient, times(amountOfTimesToChecksStatus)).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentValidated_Should_FinishIfWaitingForDeploymentValidated() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(validatedResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(VALIDATED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublishing_Should_FinishIfWaitingForDeploymentValidated() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishingResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(VALIDATED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublished_Should_FinishIfWaitingForDeploymentValidated() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishedResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(VALIDATED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublishing_Expect_ExceptionIfWaitingForDeploymentPublishedAndMaxTimeoutExceeded() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID))
        .thenReturn(pendingResponse(), validatingResponse(), validatedResponse(), publishingResponse());

    // wait four times the length it takes to poll
    int amountOfTimesToChecksStatus = 4;
    int waitMaxTimeInSeconds = (waitPollingIntervalDefaultValue * amountOfTimesToChecksStatus);

    assertThrows(
        DeploymentPublishTimedOutException.class,
        () -> deploymentPublishedWatcher.waitForDeploymentState(
            buildWaitForDeploymentStateRequest(PUBLISHED, waitMaxTimeInSeconds, waitPollingIntervalDefaultValue))
    );

    verify(publisherClient, times(amountOfTimesToChecksStatus)).status(eq(TEST_DEPLOYMENT_ID));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublished_Should_FinishIfWaitingForDeploymentPublished() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishedResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(PUBLISHED));
  }

  @Test(expected = DeploymentPublishFailedException.class, timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentFailed_Expect_Exception() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(failureResponse());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(UPLOADED));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentValidated_WithWarnings_Should_FinishAndLogWarnings() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(validatedResponseWithWarnings());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(VALIDATED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
    verify(logger).warn(any(String.class));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublished_WithWarnings_Should_FinishAndLogWarnings() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishedResponseWithWarnings());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(PUBLISHED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
    verify(logger).warn(any(String.class));
  }

  @Test(timeout = DEFAULT_DEPLOYMENT_TIMEOUT)
  public void when_DeploymentPublishing_WithWarnings_Should_FinishAndLogWarnings() {
    when(publisherClient.status(TEST_DEPLOYMENT_ID)).thenReturn(publishingResponseWithWarnings());
    deploymentPublishedWatcher.waitForDeploymentState(buildWaitForDeploymentStateRequest(VALIDATED));

    verify(publisherClient).status(eq(TEST_DEPLOYMENT_ID));
    verify(logger).warn(any(String.class));
  }

  private static DeploymentApiResponse pendingResponse() {
    return deploymentResponse(DeploymentState.PENDING);
  }

  private static DeploymentApiResponse validatedResponse() {
    return deploymentResponse(DeploymentState.VALIDATED);
  }

  private static DeploymentApiResponse validatingResponse() {
    return deploymentResponse(DeploymentState.VALIDATING);
  }

  private static DeploymentApiResponse publishingResponse() {
    return deploymentResponse(DeploymentState.PUBLISHING);
  }

  private static DeploymentApiResponse publishedResponse() {
    DeploymentApiResponse successResponse = deploymentResponse(DeploymentState.PUBLISHED);
    successResponse.setPurls(Collections.singletonList("pkg:maven/org.sonatype.central/example@1.0.0"));
    successResponse.setErrors(null);
    return successResponse;
  }

  private static DeploymentApiResponse failureResponse() {
    DeploymentApiResponse failureResponse = deploymentResponse(DeploymentState.FAILED);
    failureResponse.setPurls(null);

    List<String> list1 = new ArrayList<>();
    list1.add("Failure A");
    list1.add("Failure 2");

    List<String> list2 = new ArrayList<>();
    list2.add("Specific Failure A");
    list2.add("Specific Failure 2");

    Map<String, List<String>> map1 = new HashMap<>();
    map1.put("common", list1);
    map1.put("specific", list2);
    failureResponse.setErrors(map1);
    return failureResponse;
  }

  private static DeploymentApiResponse validatedResponseWithWarnings() {
    DeploymentApiResponse response = deploymentResponse(DeploymentState.VALIDATED);
    response.setWarnings(Collections.singletonList(
        "Your organization acme-corp has used 90% of its monthly Release Count publishing limit. " +
            "Review usage before enforcement begins August 1, 2026. " +
            "View usage: https://central.sonatype.com/publishing/usage?org=acme-corp"));
    return response;
  }

  private static DeploymentApiResponse publishedResponseWithWarnings() {
    DeploymentApiResponse response = publishedResponse();
    response.setWarnings(Collections.singletonList(
        "Your organization acme-corp is over its monthly Release Count publishing limit. " +
            "Enforcement begins August 1, 2026. " +
            "View usage: https://central.sonatype.com/publishing/usage?org=acme-corp"));
    return response;
  }

  private static DeploymentApiResponse publishingResponseWithWarnings() {
    DeploymentApiResponse response = publishingResponse();
    response.setWarnings(Collections.singletonList(
        "Your organization acme-corp has used 90% of its monthly Release Count publishing limit. " +
            "Review usage before enforcement begins August 1, 2026. " +
            "View usage: https://central.sonatype.com/publishing/usage?org=acme-corp"));
    return response;
  }

  private static DeploymentApiResponse deploymentResponse(DeploymentState deploymentState) {
    DeploymentApiResponse successResponse = new DeploymentApiResponse();
    successResponse.setDeploymentId(TEST_DEPLOYMENT_ID);
    successResponse.setDeploymentName("Test Deployment");
    successResponse.setDeploymentState(deploymentState);
    return successResponse;
  }

  private WaitForDeploymentStateRequest buildWaitForDeploymentStateRequest(
      final WaitUntilRequest waitUntilRequest)
  {
    return buildWaitForDeploymentStateRequest(
        waitUntilRequest,
        waitMaxTimeDefaultValue,
        waitPollingIntervalDefaultValue);
  }

  private WaitForDeploymentStateRequest buildWaitForDeploymentStateRequest(
      final WaitUntilRequest waitUntilRequest,
      final int waitMaxTimeInSeconds,
      final int waitPollingIntervalInSeconds)
  {
    return new WaitForDeploymentStateRequest(
        CENTRAL_BASE_URL_DEFAULT_VALUE,
        TEST_DEPLOYMENT_ID,
        waitUntilRequest,
        waitMaxTimeInSeconds,
        waitPollingIntervalInSeconds
    );
  }
}
