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

import static com.jayway.restassured.RestAssured.when;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ValidatableResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import org.codice.ddf.itests.common.AbstractIntegrationTest;
import org.ops4j.pax.exam.Option;

public abstract class AbstractAllianceIntegrationTest extends AbstractIntegrationTest {

  private static final String MVN_LOCAL_REPO = "maven.repo.local";

  private static final String PAX_URL_MVN_LOCAL_REPO = "org.ops4j.pax.url.mvn.localRepository";

  // The DEFAULT_ALLIANCE_APPS should include all alliance apps. The system will verify
  // that all of these apps can be started.
  protected static final String[] DEFAULT_ALLIANCE_APPS = {
    "catalog-app",
    "solr-app",
    "spatial-app",
    "security-app",
    "imaging-app",
    "video-app",
    "nsili-app"
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
                .groupId("ddf.distribution")
                .artifactId("sdk-app")
                .type("xml")
                .classifier("features")
                .versionAsInProject()),
        features(
            maven()
                .groupId("org.codice.alliance.distribution")
                .artifactId("sdk-app")
                .type("xml")
                .classifier("features")
                .versionAsInProject()));
  }

  @Override
  protected Option[] configureMavenRepos() {
    return options(
        editConfigurationFilePut(
            "etc/org.ops4j.pax.url.mvn.cfg",
            "org.ops4j.pax.url.mvn.repositories",
            "https://repo1.maven.org/maven2@id=central,"
                + "https://oss.sonatype.org/content/repositories/snapshots@snapshots@noreleases@id=sonatype-snapshot,"
                + "https://oss.sonatype.org/content/repositories/ops4j-snapshots@snapshots@noreleases@id=ops4j-snapshot,"
                + "https://repository.apache.org/content/groups/snapshots-group@snapshots@noreleases@id=apache,"
                + "https://svn.apache.org/repos/asf/servicemix/m2-repo@id=servicemix,"
                + "https://repository.springsource.com/maven/bundles/release@id=springsource,"
                + "https://repository.springsource.com/maven/bundles/external@id=springsourceext,"
                + "https://oss.sonatype.org/content/repositories/releases/@id=sonatype,"
                + "https://artifacts.codice.org/content/repositories/releases@id=codice-releases,"
                + "https://artifacts.codice.org/content/repositories/thirdparty@id=codice-thirdparty"),
        when(System.getProperty(MVN_LOCAL_REPO) != null)
            .useOptions(
                editConfigurationFilePut(
                    "etc/org.ops4j.pax.url.mvn.cfg",
                    PAX_URL_MVN_LOCAL_REPO,
                    System.getProperty(MVN_LOCAL_REPO))));
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
