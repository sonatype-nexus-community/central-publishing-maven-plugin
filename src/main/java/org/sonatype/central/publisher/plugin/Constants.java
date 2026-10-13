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

public class Constants
{
  public static final String DEFAULT_BUILD_DIR_NAME = "target";

  public static final String DEFAULT_STAGING_DIR_NAME = "central-staging";

  public static final String DEFAULT_DEFERRED_DIR_NAME = "central-deferred";

  public static final String LOCAL_STAGING_REPOSITORY_NAME = "central-staging";

  public static final String CLI_EXECUTION_ID = "default-cli";

  public static final String POM_FILE_EXTENSION = "pom";

  public static final String DEFAULT_BUNDLE_OUTPUT_DIR_NAME = "central-publishing";

  public static final String DEFAULT_BUNDLE_OUTPUT_FILENAME = "central-bundle.zip";

  public static final String DEFAULT_DEPLOYMENT_NAME = "Deployment";

  public static final String PUBLISH_GOAL = "publish";

  public static final String DEPLOY_PHASE = "deploy";

  public static final String PUBLISH_GOAL_ID = "injected-central-publishing";

  public static final String CENTRAL_PUBLISHING_PLUGIN_GROUP_ID = "org.sonatype.central";

  public static final String CENTRAL_PUBLISHING_PLUGIN_ARTIFACT_ID = "central-publishing-maven-plugin";

  public static final String NEXUS_STAGING_PLUGIN_GROUP_ID = "org.sonatype.plugins";

  public static final String NEXUS_STAGING_PLUGIN_ARTIFACT_ID = "nexus-staging-maven-plugin";

  public static final String MAVEN_DEPLOY_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

  public static final String MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID = "maven-deploy-plugin";

  public static final String PUBLISHING_SERVER_ID_NAME = "publishingServerId";

  public static final String PUBLISHING_SERVER_ID_DEFAULT_VALUE = "central";

  public static final String AUTO_PUBLISH_NAME = "autoPublish";

  public static final String AUTO_PUBLISH_DEFAULT_VALUE = "false";

  public static final String WAIT_UNTIL_NAME = "waitUntil";

  public static final String WAIT_UNTIL_DEFAULT_VALUE = "VALIDATED";

  public static final String WAIT_MAX_TIME_NAME = "waitMaxTime";

  public static final String WAIT_MAX_TIME_DEFAULT_VALUE = "1800"; // in seconds, 30 minutes.

  public static final String WAIT_POLLING_INTERVAL_NAME = "waitPollingInterval";

  public static final String WAIT_POLLING_INTERVAL_DEFAULT_VALUE = "5"; // in seconds.

  public static final String CHECKSUMS_NAME = "checksums";

  public static final String CHECKSUMS_DEFAULT_VALUE = "ALL";

  public static final String PUBLISH_COMPLETION_POLL_INTERVAL_NAME = "publishCompletionPollInterval";

  public static final String PUBLISH_COMPLETION_POLL_INTERVAL_DEFAULT_VALUE = "1000";

  public static final String CENTRAL_BASE_URL_NAME = "centralBaseUrl";

  public static final String CENTRAL_SNAPSHOTS_URL_NAME = "centralSnapshotsUrl";

  public static final String CENTRAL_BASE_URL_DEFAULT_VALUE = "https://central.sonatype.com";

  public static final String CENTRAL_SNAPSHOTS_URL_DEFAULT_VALUE = CENTRAL_BASE_URL_DEFAULT_VALUE + "/repository/maven-snapshots/";

  public static final String IGNORE_PUBLISHED_COMPONENTS_NAME = "ignorePublishedComponents";

  public static final String IGNORE_PUBLISHED_COMPONENTS_DEFAULT_VALUE = "false";

  public static final String SKIP_UPLOAD_NAME = "skipUpload";

  public static final String SKIP_UPLOAD_DEFAULT_VALUE = "false";

  public static final String EXCLUDE_ARTIFACTS_NAME = "excludeArtifacts";

  public static final String WAIT_FOR_PUBLISH_COMPLETION_NAME = "waitForPublishCompletion";

  public static final String WAIT_FOR_PUBLISH_COMPLETION_DEFAULT_VALUE = "false";
}
