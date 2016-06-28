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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import org.codice.alliance.security.banner.marking.BannerCommonMarkingExtractor;
import org.codice.alliance.security.banner.marking.Dod520001MarkingExtractor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;

import ddf.catalog.Constants;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import ddf.common.test.BeforeExam;
import ddf.test.itests.AbstractIntegrationTest;

/**
 * Tests the {@link BannerCommonMarkingExtractor} and {@link Dod520001MarkingExtractor}
 * content extractors.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestBannerMarkings extends AbstractIntegrationTest {
    private static final String[] REQUIRED_APPS =
            {"catalog-app", "solr-app", "spatial-app", "alliance-security-app"};

    @Override
    protected Option[] configureDistribution() {
        return options(karafDistributionConfiguration(maven().groupId(
                "org.codice.alliance.distribution")
                .artifactId("alliance")
                .type("zip")
                .versionAsInProject()
                .getURL(), "alliance", KARAF_VERSION).unpackDirectory(new File("target/exam"))
                .useDeployFolder(false));
    }

    @Override
    protected Option[] configureCustom() {
        return options(wrappedBundle(mavenBundle("ddf.test.itests", "test-itests-ddf").classifier(
                "tests")
                .versionAsInProject()).bundleSymbolicName("test-itests-ddf")
                .exports("ddf.test.itests.*"), keepRuntimeFolder());
    }

    @BeforeExam
    public void beforeAllianceTest() throws Exception {
        try {
            basePort = getBasePort();
            getAdminConfig().setLogLevels();

            getServiceManager().waitForRequiredApps(REQUIRED_APPS);
            getServiceManager().waitForAllBundles();
            getCatalogBundle().waitForCatalogProvider();

            configureSecurityStsClient();

        } catch (Exception e) {
            LOGGER.error("Failed in @BeforeExam: ", e);
            fail("Failed in @BeforeExam: " + e.getMessage());
        }
    }

    @Test
    public void testUsMarkingSimpleText() throws Exception {
        Metacard metacard = getMetacard("secret_us.txt");

        Attribute attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION);
        assertThat(attribute.getValue(), equalTo("S"));

        attribute = getAttribute(metacard, BannerCommonMarkingExtractor.SECURITY_OWNER_PRODUCER);
        assertThat(attribute.getValue(), equalTo("USA"));

        attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION_SYSTEM);
        assertThat(attribute.getValue(), equalTo("USA"));
    }

    @Test
    public void testUsMarkingSimpleWordDoc() throws Exception {
        Metacard metacard = getMetacard("topsecret_us.docx");

        Attribute attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION);
        assertThat(attribute.getValue(), equalTo("TS"));

        attribute = getAttribute(metacard, BannerCommonMarkingExtractor.SECURITY_OWNER_PRODUCER);
        assertThat(attribute.getValue(), equalTo("USA"));

        attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION_SYSTEM);
        assertThat(attribute.getValue(), equalTo("USA"));
    }

    @Test
    public void testCosmicMarkingSimplePdf() throws Exception {
        Metacard metacard = getMetacard("topsecret_cosmic.pdf");

        Attribute attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION);
        assertThat(attribute.getValue(), equalTo("CTS-B"));

        attribute = getAttribute(metacard, BannerCommonMarkingExtractor.SECURITY_OWNER_PRODUCER);
        assertThat(attribute.getValue(), equalTo("COSMIC"));

        attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION_SYSTEM);
        assertThat(attribute.getValue(), equalTo("COSMIC"));
    }

    @Test
    public void testUsMarkingCodewordsAndDissem() throws Exception {
        Metacard metacard = getMetacard("us_codewords_dissem.txt");

        Attribute attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION);
        assertThat(attribute.getValue(), equalTo("TS"));

        attribute = getAttribute(metacard, BannerCommonMarkingExtractor.SECURITY_OWNER_PRODUCER);
        assertThat(attribute.getValue(), equalTo("USA"));

        attribute = getAttribute(metacard, BannerCommonMarkingExtractor.SECURITY_CODEWORDS);
        List<Serializable> attributes = attribute.getValues();
        assertThat(attributes.size(), is(2));
        assertThat(attributes, containsInAnyOrder("TK-ABC X Y Z", "COMINT"));

        attribute = getAttribute(metacard,
                BannerCommonMarkingExtractor.SECURITY_DISSEMINATION_CONTROLS);
        attributes = attribute.getValues();
        assertThat(attributes.size(), is(1));
        assertThat(attributes, containsInAnyOrder("ORCON"));

        attribute = getAttribute(metacard, Dod520001MarkingExtractor.SECURITY_DOD5200_OTHER_DISSEM);
        attributes = attribute.getValues();
        assertThat(attributes.size(), is(1));
        assertThat(attributes, containsInAnyOrder("LIMDIS"));
    }

    @Test
    public void testInvalidMarkings() throws Exception {
        // The invalid marking has the HCS-X SCI marking without NOFORN
        Metacard metacard = getMetacard("invalid.txt");

        Attribute attribute = metacard.getAttribute(
                BannerCommonMarkingExtractor.SECURITY_CLASSIFICATION);
        assertNull(attribute);
    }

    private Metacard getMetacard(String fileName)
            throws InvalidSyntaxException, IOException, CatalogTransformerException {
        String transformerId = fileName.endsWith(".pdf") ? "pdf" : "tika";

        Optional<InputTransformer> inputTransformer = getServiceManager().getServiceReferences(
                InputTransformer.class,
                String.format("(%s=%s)", Constants.SERVICE_ID, transformerId))
                .stream()
                .findFirst()
                .map(sr -> getServiceManager().getService(sr));
        if (!inputTransformer.isPresent()) {
            fail(String.format("Error finding input transformer with id %s", transformerId));
        }

        InputTransformer xformer = inputTransformer.get();
        return xformer.transform(
                getClass().getResourceAsStream(String.format("/markings/%s", fileName)));
    }

    private Attribute getAttribute(Metacard metacard, String attrName) {
        Attribute attribute = metacard.getAttribute(attrName);
        assertNotNull(attribute);
        assertNotNull(attribute.getValue());
        return attribute;
    }

    private void configureSecurityStsClient() throws IOException, InterruptedException {
        Configuration stsClientConfig = configAdmin.getConfiguration(
                "ddf.security.sts.client.configuration.cfg", null);
        Dictionary<String, Object> properties = new Hashtable<>();

        properties.put("address", DynamicUrl.SECURE_ROOT + HTTPS_PORT.getPort()
                + "/services/SecurityTokenService?wsdl");
        stsClientConfig.update(properties);
    }
}
