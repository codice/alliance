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
package org.codice.alliance.test.itests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import ddf.catalog.Constants;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import org.codice.alliance.catalog.core.api.types.Dod520001;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.test.itests.common.AbstractAllianceIntegrationTest;
import org.codice.ddf.test.common.annotations.BeforeExam;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;

/**
 * Tests the {@link org.codice.alliance.security.banner.marking.BannerCommonMarkingExtractor} and
 * {@link org.codice.alliance.security.banner.marking.Dod520001MarkingExtractor} content extractors.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BannerMarkingsTest extends AbstractAllianceIntegrationTest {

  @BeforeExam
  public void beforeAllianceTest() throws Exception {
    try {
      waitForSystemReady();
      getSecurityPolicy().configureRestForGuest();
      waitForSystemReady();

    } catch (Exception e) {
      LOGGER.error("Failed in @BeforeExam: ", e);
      fail("Failed in @BeforeExam: " + e.getMessage());
    }
  }

  @After
  public void tearDown() {
    clearCatalog();
  }

  @Test
  public void testUsMarkingSimpleText() throws Exception {
    Metacard metacard = getMetacard("secret_us.txt");

    Attribute attribute = getAttribute(metacard, Security.CLASSIFICATION);
    assertThat(attribute.getValue(), equalTo("S"));

    attribute = getAttribute(metacard, Security.OWNER_PRODUCER);
    assertThat(attribute.getValue(), equalTo("USA"));

    attribute = getAttribute(metacard, Security.CLASSIFICATION_SYSTEM);
    assertThat(attribute.getValue(), equalTo("USA"));
  }

  @Test
  public void testUsMarkingSimpleWordDoc() throws Exception {
    Metacard metacard = getMetacard("topsecret_us.docx");

    Attribute attribute = getAttribute(metacard, Security.CLASSIFICATION);
    assertThat(attribute.getValue(), equalTo("TS"));

    attribute = getAttribute(metacard, Security.OWNER_PRODUCER);
    assertThat(attribute.getValue(), equalTo("USA"));

    attribute = getAttribute(metacard, Security.CLASSIFICATION_SYSTEM);
    assertThat(attribute.getValue(), equalTo("USA"));
  }

  @Test
  public void testCosmicMarkingSimplePdf() throws Exception {
    Metacard metacard = getMetacard("topsecret_cosmic.pdf");

    Attribute attribute = getAttribute(metacard, Security.CLASSIFICATION);
    assertThat(attribute.getValue(), equalTo("CTS-B"));

    attribute = getAttribute(metacard, Security.OWNER_PRODUCER);
    assertThat(attribute.getValue(), equalTo("NATO"));

    attribute = getAttribute(metacard, Security.CLASSIFICATION_SYSTEM);
    assertThat(attribute.getValue(), equalTo("NATO"));
  }

  @Test
  public void testUsMarkingCodewordsAndDissem() throws Exception {
    Metacard metacard = getMetacard("us_codewords_dissem.txt");

    Attribute attribute = getAttribute(metacard, Security.CLASSIFICATION);
    assertThat(attribute.getValue(), equalTo("TS"));

    attribute = getAttribute(metacard, Security.OWNER_PRODUCER);
    assertThat(attribute.getValue(), equalTo("USA"));

    attribute = getAttribute(metacard, Security.CODEWORDS);
    List<Serializable> attributes = attribute.getValues();
    assertThat(attributes.size(), is(2));
    assertThat(attributes, containsInAnyOrder("TK-ABC X Y Z", "COMINT"));

    attribute = getAttribute(metacard, Security.DISSEMINATION_CONTROLS);
    attributes = attribute.getValues();
    assertThat(attributes.size(), is(1));
    assertThat(attributes, containsInAnyOrder("ORCON"));

    attribute = getAttribute(metacard, Dod520001.SECURITY_DOD5200_OTHER_DISSEM);
    attributes = attribute.getValues();
    assertThat(attributes.size(), is(1));
    assertThat(attributes, containsInAnyOrder("LIMDIS"));
  }

  @Test
  public void testInvalidMarkings() throws Exception {
    // The invalid marking has the HCS-X SCI marking without NOFORN
    Metacard metacard = getMetacard("invalid.txt");

    Attribute attribute = metacard.getAttribute(Security.CLASSIFICATION);
    assertNull(attribute);
  }

  @Test(expected = RuntimeException.class)
  public void testBannerMarkingMismatch() throws Exception {
    // start up the sample extractor
    getServiceManager().startFeature(true, "sample-content-metadata-extractor");

    try {
      getMetacard("topsecret_cosmic.pdf");
    } finally {
      getServiceManager().stopFeature(true, "sample-content-metadata-extractor");
    }
  }

  private Metacard getMetacard(String fileName)
      throws InvalidSyntaxException, IOException, CatalogTransformerException {
    String transformerId = fileName.endsWith(".pdf") ? "pdf" : "tika";

    Optional<InputTransformer> inputTransformer =
        getServiceManager()
            .getServiceReferences(
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
        getAllianceItestResourceAsStream(String.format("/markings/%s", fileName)));
  }

  private Attribute getAttribute(Metacard metacard, String attrName) {
    Attribute attribute = metacard.getAttribute(attrName);
    assertNotNull(attribute);
    assertNotNull(attribute.getValue());
    return attribute;
  }

  private void configureSecurityStsClient() throws IOException, InterruptedException {
    Configuration stsClientConfig =
        configAdmin.getConfiguration("ddf.security.sts.client.configuration.cfg", null);
    Dictionary<String, Object> properties = new Hashtable<>();

    properties.put(
        "address",
        DynamicUrl.SECURE_ROOT + HTTPS_PORT.getPort() + "/services/SecurityTokenService?wsdl");
    stsClientConfig.update(properties);
  }
}
