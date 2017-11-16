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
package org.codice.alliance.catalog.mgmp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;
import org.codice.ddf.spatial.ogc.csw.catalog.converter.CswUnmarshallHelper;
import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class MgmpConverterTest {

  private static final String GSS_NAMESPACE = "http://www.isotc211.org/2005/gss";

  private static final String GTS_NAMESPACE = "http://www.isotc211.org/2005/gts";

  private static final String GSR_NAMESPACE = "http://www.isotc211.org/2005/gsr";

  private static final String SRV_NAMESPACE = "http://www.isotc211.org/2005/srv";

  private static final Logger LOGGER = LoggerFactory.getLogger(MgmpConverterTest.class);

  private MetacardImpl testMetacard1, testMetacard2, testMetacard3;

  private MgmpConverter mgmpConverter;

  private Schema schema;

  @Before
  public void setup() throws SAXException {
    generateTestMetacard1();
    generateTestMetacard2();
    generateTestMetacard3();
    mgmpConverter = new MgmpConverter();
    mgmpConverter.setSecurityMappingList(MgmpUtilityTest.SECURITY_LIST);
    mgmpConverter.setGmlIdSupplier(new GmlIdSupplier());

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    schemaFactory.setResourceResolver(new ResourceResolver());

    Source mgmpSchemaSource = new StreamSource(getClass().getResourceAsStream("/schemas/mgmp.xsd"));

    schema = schemaFactory.newSchema(new Source[] {mgmpSchemaSource});
  }

  @Test
  public void testSchemaComplianceWithoutDescription() {
    testMetacard1.setAttribute(Core.DESCRIPTION, (Serializable) Collections.emptyList());
    validateXml(generateMgmpXmlFromMetacard(1));
  }

  @Test
  public void testSchemaCompliance() {
    validateXml(generateMgmpXmlFromMetacard(1));
  }

  @Test
  public void testSchemaComplianceEmptyPublisher() {
    testMetacard1.setAttribute(Contact.PUBLISHER_NAME, (Serializable) Collections.emptyList());
    testMetacard1.setAttribute(Contact.PUBLISHER_PHONE, (Serializable) Collections.emptyList());
    testMetacard1.setAttribute(Contact.PUBLISHER_ADDRESS, (Serializable) Collections.emptyList());
    testMetacard1.setAttribute(Contact.PUBLISHER_EMAIL, (Serializable) Collections.emptyList());

    validateXml(generateMgmpXmlFromMetacard(1));
  }

  @Test
  public void testSchemaComplianceEmptyPointOfContact() {
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_NAME, (Serializable) Collections.emptyList());
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_PHONE, (Serializable) Collections.emptyList());
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_ADDRESS, (Serializable) Collections.emptyList());
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_EMAIL, (Serializable) Collections.emptyList());

    validateXml(generateMgmpXmlFromMetacard(1));
  }

  @Test
  public void testSchemaComplianceMetacard2() throws IOException {
    validateXml(generateMgmpXmlFromMetacard(2));
  }

  @Test
  public void testSchemaComplianceMetacard3() throws IOException {
    validateXml(generateMgmpXmlFromMetacard(3));
  }

  @Test
  public void testSchemaComplianceEmptyKeywords() {
    testMetacard1.setAttribute(Topic.KEYWORD, (Serializable) Collections.emptyList());

    validateXml(generateMgmpXmlFromMetacard(1));
  }

  @Test
  public void testSchemaComplianceEmptyTopics() {
    testMetacard1.setAttribute(Topic.CATEGORY, (Serializable) Collections.emptyList());

    validateXml(generateMgmpXmlFromMetacard(1));
  }

  @Test
  public void testMultiplePointOfContactNames() {
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_NAME, (Serializable) Arrays.asList("Name1", "Name2"));

    validateXml(generateMgmpXmlFromMetacard(1));
  }

  /** Test that the generated xml matches the reference xml. */
  @Test
  public void testMarshalMetacard1() throws IOException, SAXException {
    String compareString;
    try (InputStream input = getClass().getResourceAsStream("/xmldiff-reference-mgmp.xml")) {
      compareString = IOUtils.toString(input);
    }

    String xml = generateMgmpXmlFromMetacard(1);

    Diff diff = new Diff(compareString, xml);

    LOGGER.info("diff:\n" + diff.toString());
    LOGGER.info("{}", xml);
    assertThat(diff.identical(), is(true));
  }

  /** Test that the generated xml matches the reference xml. */
  @Test
  public void testMarshalMetacard2() throws IOException, SAXException {
    String compareString;
    try (InputStream input = getClass().getResourceAsStream("/xmldiff-reference2-mgmp.xml")) {
      compareString = IOUtils.toString(input);
    }

    String xml = generateMgmpXmlFromMetacard(2);
    LOGGER.info("{}", xml);
    Diff diff = new Diff(compareString, xml);

    LOGGER.info("diff:\n" + diff.toString());
    assertThat(diff.identical(), is(true));
  }

  /** Test that the generated xml matches the reference xml. */
  @Test
  public void testMarshalMetacard3() throws IOException, SAXException {
    String compareString;
    try (InputStream input = getClass().getResourceAsStream("/xmldiff-reference3-mgmp.xml")) {
      compareString = IOUtils.toString(input);
    }

    String xml = generateMgmpXmlFromMetacard(3);
    LOGGER.info("{}", xml);
    Diff diff = new Diff(compareString, xml);

    LOGGER.info("diff:\n" + diff.toString());
    assertThat(diff.identical(), is(true));
  }

  private static class ResourceResolver implements LSResourceResolver {
    private Map<Pair<String, String>, String> schemaLocations;

    private Map<Pair<String, String>, LSInput> inputs;

    {
      inputs = new HashMap<>();

      schemaLocations = new HashMap<>();

      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "gmd.xsd"), "/schemas/iso/19139/20070417/gmd/gmd.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "metadataEntity.xsd"),
          "/schemas/iso/19139/20070417/gmd/metadataEntity.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "metadataApplication.xsd"),
          "/schemas/iso/19139/20070417/gmd/metadataApplication.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "spatialRepresentation.xsd"),
          "/schemas/iso/19139/20070417/gmd/spatialRepresentation.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "citation.xsd"),
          "/schemas/iso/19139/20070417/gmd/citation.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "referenceSystem.xsd"),
          "/schemas/iso/19139/20070417/gmd/referenceSystem.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "extent.xsd"),
          "/schemas/iso/19139/20070417/gmd/extent.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "metadataExtension.xsd"),
          "/schemas/iso/19139/20070417/gmd/metadataExtension.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "content.xsd"),
          "/schemas/iso/19139/20070417/gmd/content.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "applicationSchema.xsd"),
          "/schemas/iso/19139/20070417/gmd/applicationSchema.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "portrayalCatalogue.xsd"),
          "/schemas/iso/19139/20070417/gmd/portrayalCatalogue.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "dataQuality.xsd"),
          "/schemas/iso/19139/20070417/gmd/dataQuality.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "identification.xsd"),
          "/schemas/iso/19139/20070417/gmd/identification.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "constraints.xsd"),
          "/schemas/iso/19139/20070417/gmd/constraints.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "distribution.xsd"),
          "/schemas/iso/19139/20070417/gmd/distribution.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "maintenance.xsd"),
          "/schemas/iso/19139/20070417/gmd/maintenance.xsd");
      schemaLocations.put(
          pair(GmdConstants.GMD_NAMESPACE, "freeText.xsd"),
          "/schemas/iso/19139/20070417/gmd/freeText.xsd");

      schemaLocations.put(
          pair(GmdConstants.GCO_NAMESPACE, "gco.xsd"), "/schemas/iso/19139/20070417/gco/gco.xsd");
      schemaLocations.put(
          pair(GmdConstants.GCO_NAMESPACE, "gcoBase.xsd"),
          "/schemas/iso/19139/20070417/gco/gcoBase.xsd");
      schemaLocations.put(
          pair(GmdConstants.GCO_NAMESPACE, "basicTypes.xsd"),
          "/schemas/iso/19139/20070417/gco/basicTypes.xsd");

      schemaLocations.put(
          pair("http://www.w3.org/1999/xlink", "xlink.xsd"), "/schemas/xlink/xlink.xsd");
      schemaLocations.put(
          pair("http://www.w3.org/XML/1998/namespace", "xml.xsd"), "/schemas/xml/2001/xml.xsd");

      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "feature.xsd"), "/schemas/gml/3.2.1/feature.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "geometryAggregates.xsd"),
          "/schemas/gml/3.2.1/geometryAggregates.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "geometryPrimitives.xsd"),
          "/schemas/gml/3.2.1/geometryPrimitives.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "geometryBasic2d.xsd"),
          "/schemas/gml/3.2.1/geometryBasic2d.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "geometryBasic0d1d.xsd"),
          "/schemas/gml/3.2.1/geometryBasic0d1d.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "measures.xsd"), "/schemas/gml/3.2.1/measures.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "units.xsd"), "/schemas/gml/3.2.1/units.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "dictionary.xsd"), "/schemas/gml/3.2.1/dictionary.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "gmlBase.xsd"), "/schemas/gml/3.2.1/gmlBase.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "gml.xsd"), "/schemas/gml/3.2.1/gml.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "dynamicFeature.xsd"),
          "/schemas/gml/3.2.1/dynamicFeature.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "temporal.xsd"), "/schemas/gml/3.2.1/temporal.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "direction.xsd"), "/schemas/gml/3.2.1/direction.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "topology.xsd"), "/schemas/gml/3.2.1/topology.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "geometryComplexes.xsd"),
          "/schemas/gml/3.2.1/geometryComplexes.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "coverage.xsd"), "/schemas/gml/3.2.1/coverage.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "valueObjects.xsd"),
          "/schemas/gml/3.2.1/valueObjects.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "grids.xsd"), "/schemas/gml/3.2.1/grids.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "coordinateReferenceSystems.xsd"),
          "/schemas/gml/3.2.1/coordinateReferenceSystems.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "coordinateSystems.xsd"),
          "/schemas/gml/3.2.1/coordinateSystems.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "referenceSystems.xsd"),
          "/schemas/gml/3.2.1/referenceSystems.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "datums.xsd"), "/schemas/gml/3.2.1/datums.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "coordinateOperations.xsd"),
          "/schemas/gml/3.2.1/coordinateOperations.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "observation.xsd"),
          "/schemas/gml/3.2.1/observation.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "temporalReferenceSystems.xsd"),
          "/schemas/gml/3.2.1/temporalReferenceSystems.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "temporalTopology.xsd"),
          "/schemas/gml/3.2.1/temporalTopology.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "deprecatedTypes.xsd"),
          "/schemas/gml/3.2.1/deprecatedTypes.xsd");
      schemaLocations.put(
          pair(MgmpConstants.GML_NAMESPACE, "basicTypes.xsd"), "/schemas/gml/3.2.1/basicTypes.xsd");

      schemaLocations.put(
          pair(GSS_NAMESPACE, "gss.xsd"), "/schemas/iso/19139/20070417/gss/gss.xsd");
      schemaLocations.put(
          pair(GSS_NAMESPACE, "geometry.xsd"), "/schemas/iso/19139/20070417/gss/geometry.xsd");

      schemaLocations.put(
          pair(GTS_NAMESPACE, "gts.xsd"), "/schemas/iso/19139/20070417/gts/gts.xsd");
      schemaLocations.put(
          pair(GTS_NAMESPACE, "temporalObjects.xsd"),
          "/schemas/iso/19139/20070417/gts/temporalObjects.xsd");

      schemaLocations.put(
          pair(GSR_NAMESPACE, "gsr.xsd"), "/schemas/iso/19139/20070417/gsr/gsr.xsd");
      schemaLocations.put(
          pair(GSR_NAMESPACE, "spatialReferencing.xsd"),
          "/schemas/iso/19139/20070417/gsr/spatialReferencing.xsd");

      schemaLocations.put(
          pair(SRV_NAMESPACE, "srv.xsd"), "/schemas/iso/19139/20060504/srv/srv.xsd");
      schemaLocations.put(
          pair(SRV_NAMESPACE, "serviceMetadata.xsd"),
          "/schemas/iso/19139/20060504/srv/serviceMetadata.xsd");
      schemaLocations.put(
          pair(SRV_NAMESPACE, "serviceModel.xsd"),
          "/schemas/iso/19139/20060504/srv/serviceModel.xsd");
    }

    private ImmutablePair<String, String> pair(String namespace, String resource) {
      return new ImmutablePair<>(namespace, resource);
    }

    @Override
    public LSInput resolveResource(
        String type, String namespaceURI, String publicId, String systemId, String baseURI) {

      String fileName = new java.io.File(systemId).getName();

      Pair<String, String> key = pair(namespaceURI, fileName);

      if (inputs.containsKey(key)) {
        return inputs.get(key);
      }

      LSInput input = new DOMInputImpl();

      InputStream is = getClass().getResourceAsStream(schemaLocations.get(key));
      input.setByteStream(is);
      input.setBaseURI(baseURI);
      input.setSystemId(systemId);
      inputs.put(key, input);
      return input;
    }
  }

  private static class GmlIdSupplier implements Supplier<String> {

    private int index = 0;

    private List<String> values =
        Arrays.asList(
            "GMLID_69002c53-8110-4c7b-8966-c7d6560ac234",
            "GMLID_da6b4a59-3b6e-45a2-ae45-07ccb4346f71",
            "GMLID_5eff2dae-80df-4125-8450-f43a065bdfc5");

    @Override
    public String get() {
      return values.get(index++);
    }
  }

  private String generateMgmpXmlFromMetacard(int metacardNumber) {
    StringWriter stringWriter = new StringWriter();
    PrettyPrintWriter writer = new PrettyPrintWriter(stringWriter, new NoNameCoder());
    TreeMarshaller context = new TreeMarshaller(writer, null, null);

    switch (metacardNumber) {
      case 1:
        mgmpConverter.marshal(testMetacard1, writer, context);
        break;

      case 2:
        mgmpConverter.marshal(testMetacard2, writer, context);
        break;

      case 3:
        mgmpConverter.marshal(testMetacard3, writer, context);
        break;
    }
    return stringWriter.toString();
  }

  private void validateXml(String xml) {
    try {
      schema.newValidator().validate(new StreamSource(new StringReader(xml)));
    } catch (SAXException | IOException e) {
      fail("Generated MGMPv2 Response does not conform to Schema: " + e.getMessage());
    }
  }

  private MetacardImpl generateBaseTestMetacard() {
    MetacardImpl testMetacard1 = new MetacardImpl(MgmpTransformerTest.getMgmpMetacardType());
    String referenceDateIso = "1972-02-12T21:30:00.000Z";

    Date referenceDate = CswUnmarshallHelper.convertToDate(referenceDateIso);

    testMetacard1.setId("22407eee80044d92afedcac07fff661b");
    testMetacard1.setAttribute(
        new AttributeImpl(Isr.COMMENTS, Arrays.asList("comment1", "comment2")));
    testMetacard1.setAttribute(
        new AttributeImpl(Isr.CATEGORY, Arrays.asList("category1", "category2")));
    testMetacard1.setAttribute(Location.COUNTRY_CODE, (Serializable) Arrays.asList("USA", "GBR"));
    Date end1 = new Date(referenceDate.getTime() + 10000);
    Date start2 = new Date(referenceDate.getTime() + 50000);
    testMetacard1.setAttribute(DateTime.START, (Serializable) Arrays.asList(referenceDate, start2));
    testMetacard1.setAttribute(DateTime.END, (Serializable) Arrays.asList(end1, start2));
    testMetacard1.setAttribute(Location.ALTITUDE, (Serializable) Arrays.asList(1D, 2D, 3D, 4D, 5D));
    testMetacard1.setModifiedDate(referenceDate);
    testMetacard1.setCreatedDate(referenceDate);
    testMetacard1.setExpirationDate(referenceDate);
    testMetacard1.setAttribute(Core.METACARD_MODIFIED, referenceDate);
    testMetacard1.setAttribute(Core.METACARD_CREATED, new Date(referenceDate.getTime() + 10000));
    testMetacard1.setAttribute(
        Contact.PUBLISHER_NAME, (Serializable) Arrays.asList("pub1", "pub2"));
    testMetacard1.setAttribute(
        Contact.PUBLISHER_PHONE, (Serializable) Arrays.asList("phone1", "phone2"));
    testMetacard1.setAttribute(
        Contact.PUBLISHER_EMAIL, (Serializable) Arrays.asList("email1", "email2"));
    testMetacard1.setAttribute(
        Contact.PUBLISHER_ADDRESS, (Serializable) Arrays.asList("addr1", "addr2"));
    testMetacard1.setAttribute(Core.RESOURCE_DOWNLOAD_URL, "http://127.0.0.1/foo/bar/index.html");
    testMetacard1.setAttribute(Associations.RELATED, "22407eee80044d92afedcac07fff661c");
    testMetacard1.setAttribute(
        new AttributeImpl(Topic.CATEGORY, Arrays.asList("farming", "biota", "boundaries")));
    testMetacard1.setAttribute(
        new AttributeImpl(Topic.KEYWORD, Arrays.asList("keyword1", "keyword2", "keyword3")));
    testMetacard1.setAttribute(Media.FORMAT, "format");
    testMetacard1.setAttribute(Media.FORMAT_VERSION, "format-version");
    testMetacard1.setTitle("theTitle");
    testMetacard1.setContentTypeName("text/plain");
    testMetacard1.setAttribute(Security.METADATA_DISSEMINATION, MgmpConstants.RELEASABLE_TO);
    testMetacard1.setAttribute(
        Security.METADATA_RELEASABILITY, (Serializable) Arrays.asList("USA", "AUS"));
    testMetacard1.setAttribute(Security.RESOURCE_DISSEMINATION, "Releasable to");
    testMetacard1.setAttribute(
        Security.RESOURCE_RELEASABILITY, (Serializable) Arrays.asList("USA", "AUS"));
    testMetacard1.setAttribute(
        Location.COORDINATE_REFERENCE_SYSTEM_CODE,
        (Serializable) Arrays.asList("MGMP:001", "MGMP:002"));
    testMetacard1.setAttribute(Isr.CLOUD_COVER, 30);
    testMetacard1.setAttribute(Contact.POINT_OF_CONTACT_NAME, "point-of-contact-name-1");
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_ADDRESS,
        (Serializable) Arrays.asList("point-of-contact-addr-1", "point-of-contact-addr-2"));
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_PHONE,
        (Serializable) Arrays.asList("point-of-contact-phone-1", "point-of-contact-phone-2"));
    testMetacard1.setAttribute(
        Contact.POINT_OF_CONTACT_EMAIL,
        (Serializable) Arrays.asList("point-of-contact-email-1", "point-of-contact-email-2"));
    testMetacard1.setAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE, 2);
    testMetacard1.setAttribute(Core.DESCRIPTION, "this is the description string");
    testMetacard1.setEffectiveDate(referenceDate);
    testMetacard1.setAttribute(MgmpConstants.PUBLISHED_DATE, referenceDate);
    return testMetacard1;
  }

  private void generateTestMetacard1() {
    testMetacard1 = generateBaseTestMetacard();
    testMetacard1.setSourceId("Alliance");
    testMetacard1.setAttribute(new AttributeImpl(Core.LANGUAGE, Arrays.asList("eng", "fr", "de")));
    testMetacard1.setAttribute(GmdConstants.RESOURCE_STATUS, "completed");
    testMetacard1.setLocation("POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0 ))");
    testMetacard1.setAttribute(Security.RESOURCE_CLASSIFICATION, "SECRET");
    testMetacard1.setAttribute(Security.METADATA_CLASSIFICATION, "SECRET");
    testMetacard1.setAttribute(
        Security.RESOURCE_ORIGINATOR_CLASSIFICATION, "security-resource-originator-classification");
    testMetacard1.setAttribute(
        Security.METADATA_ORIGINATOR_CLASSIFICATION, "security-metadata-originator-classification");
  }

  private void generateTestMetacard2() {
    testMetacard2 = generateBaseTestMetacard();
    testMetacard2.setSourceId("Alliance");
    testMetacard2.setAttribute(Security.CLASSIFICATION, "UNCLASSIFIED");
    testMetacard2.setAttribute(Security.METADATA_ORIGINATOR_CLASSIFICATION, "invalid");
    testMetacard2.setAttribute(Security.RESOURCE_ORIGINATOR_CLASSIFICATION, "UNCLASSIFIED");
  }

  private void generateTestMetacard3() {
    testMetacard3 = generateBaseTestMetacard();
    testMetacard3.setSourceId("Alliance");
    testMetacard3.setAttribute(Security.CLASSIFICATION, "UNCLASSIFIED");
  }
}
