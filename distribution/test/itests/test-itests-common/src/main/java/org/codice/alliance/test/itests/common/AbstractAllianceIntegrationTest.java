/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.test.itests.common;

import static io.restassured.RestAssured.when;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import org.codice.ddf.itests.common.AbstractIntegrationTest;
import org.ops4j.pax.exam.Option;

public abstract class AbstractAllianceIntegrationTest extends AbstractIntegrationTest {

  // The DEFAULT_ALLIANCE_APPS should include all alliance apps. The system will verify
  // that all of these apps can be started.
  protected static final String[] DEFAULT_ALLIANCE_APPS = {
    "catalog-app", "catalog-solr-app", "spatial-app", "security-app", "imaging-app", "video-app"
  };

  @Override
  protected String[] getDefaultRequiredApps() {
    return Arrays.copyOf(DEFAULT_ALLIANCE_APPS, DEFAULT_ALLIANCE_APPS.length);
  }

  @Override
  public void waitForBaseSystemFeatures() {
    try {
      super.waitForBaseSystemFeatures();

      configureRestForGuest("/services/secure,/services/public");
      getServiceManager().waitForAllBundles();

    } catch (Exception e) {
      throw new IllegalStateException("Failed to start up required features.", e);
    }
  }

  @Override
  protected Option[] configureDistribution() {
    return options(
        karafDistributionConfiguration(
                maven()
                    .groupId("org.codice.alliance.distribution")
                    .artifactId("alliance")
                    .type("zip")
                    .versionAsInProject()
                    .getURL(),
                "alliance",
                KARAF_VERSION)
            .unpackDirectory(new File("target/exam"))
            .useDeployFolder(false));
  }

  @Override
  protected Option[] configureStartScript() {
    // add test dependencies to the test-dependencies-app instead of here
    return options(
        junitBundles(),
        features(
            maven()
                .groupId("org.codice.alliance.test.itests")
                .artifactId("test-itests-dependencies-app")
                .type("xml")
                .classifier("features")
                .versionAsInProject(),
            "alliance-itest-dependencies"),
        features(
            maven()
                .groupId("org.codice.alliance.distribution")
                .artifactId("sdk-app")
                .type("xml")
                .classifier("features")
                .versionAsInProject()));
  }

  public static InputStream getAllianceItestResourceAsStream(String filePath) {
    return getFileContentAsStream(filePath, AbstractAllianceIntegrationTest.class);
  }

  public static String getAllianceItestResource(String filePath) {
    return getAllianceItestResource(filePath, ImmutableMap.of());
  }

  public static String getAllianceItestResource(String filePath, ImmutableMap params) {
    return getFileContent(filePath, params, AbstractAllianceIntegrationTest.class);
  }

  protected ValidatableResponse executeOpenSearch(String format, String... query) {
    StringBuilder buffer =
        new StringBuilder(OPENSEARCH_PATH.getUrl()).append("?").append("format=").append(format);

    for (String term : query) {
      buffer.append("&").append(term);
    }

    String url = buffer.toString();
    LOGGER.info("Getting response to {}", url);

    return when().get(url).then();
  }
}
