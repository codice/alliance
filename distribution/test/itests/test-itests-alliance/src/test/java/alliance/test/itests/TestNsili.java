/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package alliance.test.itests;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.codice.alliance.nsili.mockserver.server.MockNsili;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.cm.Configuration;

import com.jayway.restassured.response.ValidatableResponse;

import ddf.common.test.AfterExam;
import ddf.common.test.BeforeExam;
import ddf.test.itests.AbstractIntegrationTest;


/**
 * Tests the Alliance additions to DDF framework components.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestNsili extends AbstractIntegrationTest {

    private Thread mockServerThread;

    private static final String CORBA_DEFAULT_PORT_PROPERTY = "org.codice.alliance.corba_default_port";

    private static final DynamicPort CORBA_DEFAULT_PORT = new DynamicPort(CORBA_DEFAULT_PORT_PROPERTY, 6);

    private static final DynamicPort HTTP_WEB_PORT = new DynamicPort("org.codice.alliance.corba_web_port", 7);

    private static final DynamicPort FTP_WEB_PORT = new DynamicPort("org.codice.alliance.corba_ftp_web_port", 8);

    private static final DynamicPort CORBA_PORT = new DynamicPort("org.codice.alliance.corba_port", 9);

    private static final String[] REQUIRED_APPS =
            {"catalog-app", "solr-app", "spatial-app", "nsili-app"};

    private static final String HTTP_NSILI_SOURCE_ID = "httpNsiliSource";

    private static final String FTP_NSILI_SOURCE_ID = "ftpNsiliSource";

    @Override
    protected Option[] configureDistribution() {
        return options(
                karafDistributionConfiguration(
                        maven().groupId("org.codice.alliance.distribution").artifactId("alliance")
                                .type("zip").versionAsInProject().getURL(),
                        "alliance", KARAF_VERSION).unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false));
    }

    @Override
    protected Option[] configureCustom() {
        return options(
                wrappedBundle(mavenBundle("ddf.test.itests", "test-itests-ddf").classifier("tests")
                        .versionAsInProject()).bundleSymbolicName("test-itests-ddf")
                        .exports("ddf.test.itests.*"),
                wrappedBundle(mavenBundle().groupId("org.codice.alliance.distribution")
                        .artifactId("sample-nsili-server").versionAsInProject()),
                keepRuntimeFolder());
    }

    @BeforeExam
    public void beforeAllianceTest() throws Exception {
        try {
            basePort = getBasePort();
            setLogLevels();

            System.setProperty(CORBA_DEFAULT_PORT_PROPERTY, CORBA_DEFAULT_PORT.getPort());

            getServiceManager().waitForRequiredApps(REQUIRED_APPS);
            getServiceManager().waitForAllBundles();
            getCatalogBundle().waitForCatalogProvider();

            configureSecurityStsClient();

            startMockResources();
            configureHttpNsiliSource();
            configureFtpNsiliSource();
        } catch (Exception e) {
            LOGGER.error("Failed in @BeforeExam: ", e);
            fail("Failed in @BeforeExam: " + e.getMessage());
        }
    }

    /**
     * Determine if the HTTP Nsili Source has been configured in Alliance and available
     *
     * @throws Exception
     */
    @Test
    public void testNsiliHttpSourceAvailable() throws Exception {
        // @formatter:off
        given().auth().basic("admin", "admin").when().get(ADMIN_ALL_SOURCES_PATH.getUrl()).then()
                .log().all().assertThat().body(containsString("\"id\":\"httpNsiliSource\""));
        // @formatter:on
    }

    /**
     * Determine if the FTP Nsili Source has been configured in Alliance and available
     *
     * @throws Exception
     */
    @Test
    public void testNsiliFtpSourceAvailable() throws Exception {
        // @formatter:off
        given().auth().basic("admin", "admin").when().get(ADMIN_ALL_SOURCES_PATH.getUrl()).then()
                .log().all().assertThat().body(containsString("\"id\":\"ftpNsiliSource\""));
        // @formatter:on
    }

    /**
     * Simple search query to assert # records returned from mock source
     *
     * @throws Exception
     */
    @Test
    public void testNsiliHttpSourceOpenSearchGetAll() throws Exception {
        ValidatableResponse response = executeOpenSearch("xml", "q=*", "src=" + HTTP_NSILI_SOURCE_ID, "count=100");
        response.log().all().body("metacards.metacard.size()", equalTo(11));
    }

    /**
     * Simple search query to assert # records returned from mock source
     *
     * @throws Exception
     */
    @Test
    public void testNsiliFtpSourceOpenSearchGetAll() throws Exception {
        ValidatableResponse response = executeOpenSearch("xml", "q=*", "src=" + FTP_NSILI_SOURCE_ID, "count=100");
        response.log().all().body("metacards.metacard.size()", equalTo(11));
    }

    /**
     * Perform query with location filtering
     *
     * @throws Exception
     */
    @Test
    public void testNsiliHttpSourceOpenSearchLocation() throws Exception {
        ValidatableResponse response = executeOpenSearch("xml", "q=*", "lat=-53.0", "lon=-111.0",
                "radius=50", "src=" + HTTP_NSILI_SOURCE_ID, "count=100");
        response.log().all().body("metacards.metacard.size()", equalTo(11));
    }

    /**
     * Perform query with location filtering
     *
     * @throws Exception
     */
    @Test
    public void testNsiliFtpSourceOpenSearchLocation() throws Exception {
        ValidatableResponse response = executeOpenSearch("xml", "q=*", "lat=-53.0", "lon=-111.0",
                "radius=50", "src=" + FTP_NSILI_SOURCE_ID, "count=100");
        response.log().all().body("metacards.metacard.size()", equalTo(11));
    }

    @AfterExam
    public void afterAllianceTest() throws Exception {
        if (mockServerThread != null) {
            mockServerThread.interrupt();
        }
        mockServerThread = null;
    }

    private void startMockResources() throws Exception {
        MockNsiliRunnable mockServer =
                new MockNsiliRunnable(Integer.parseInt(HTTP_WEB_PORT.getPort()),
                        Integer.parseInt(FTP_WEB_PORT.getPort()),
                        Integer.parseInt(CORBA_PORT.getPort()));
        mockServerThread = new Thread(mockServer, "mockServer");
        mockServerThread.start();
    }

    private void configureHttpNsiliSource() throws IOException {
        String iorUrl = DynamicUrl.INSECURE_ROOT + Integer.parseInt(HTTP_WEB_PORT.getPort()) + "/data/ior.txt";
        NsiliSourceProperties sourceProperties = new NsiliSourceProperties(HTTP_NSILI_SOURCE_ID, iorUrl);

        getServiceManager().createManagedService(NsiliSourceProperties.FACTORY_PID,
                sourceProperties);
    }

    private void configureFtpNsiliSource() throws IOException {
        String iorUrl = "ftp://localhost:" + Integer.parseInt(FTP_WEB_PORT.getPort()) + "/data/ior.txt";
        NsiliSourceProperties sourceProperties = new NsiliSourceProperties(FTP_NSILI_SOURCE_ID, iorUrl);

        sourceProperties.put("serverUsername", MockNsili.MOCK_SERVER_USERNAME);
        sourceProperties.put("serverPassword", MockNsili.MOCK_SERVER_PASSWORD);

        getServiceManager().createManagedService(NsiliSourceProperties.FACTORY_PID,
                sourceProperties);
    }

    private void configureSecurityStsClient() throws IOException, InterruptedException {
        Configuration stsClientConfig = configAdmin
                .getConfiguration("ddf.security.sts.client.configuration.cfg", null);
        Dictionary<String, Object> properties = new Hashtable<>();

        properties.put("address", DynamicUrl.SECURE_ROOT + HTTPS_PORT.getPort()
                + "/services/SecurityTokenService?wsdl");
        stsClientConfig.update(properties);
    }

    private ValidatableResponse executeOpenSearch(String format, String... query) {
        StringBuilder buffer = new StringBuilder(OPENSEARCH_PATH.getUrl()).append("?")
                .append("format=").append(format);

        for (String term : query) {
            buffer.append("&").append(term);
        }

        String url = buffer.toString();
        LOGGER.info("Getting response to {}", url);

        return when().get(url).then();
    }

    public class NsiliSourceProperties extends HashMap<String, Object> {
        public static final String SYMBOLIC_NAME = "catalog-nsili-source";

        public static final String FACTORY_PID = "NSILI_Federated_Source";

        public NsiliSourceProperties(String sourceId, String iorUrl) {
            this.putAll(getMetatypeDefaults(SYMBOLIC_NAME, FACTORY_PID));
            this.put("id", sourceId);
            this.put("iorUrl", iorUrl);
            this.put("pollInterval", 1);
            this.put("maxHitCount", 250);
            this.put("numberWorkerThreads", 4);
            this.put("excludeSortOrder", false);
        }
    }
}