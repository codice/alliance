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
package org.codice.alliance.nsili.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.data.types.Core;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class ResultDAGConverterTest {
  private static final String CARD_TITLE = "Test Title";

  private static final String CARD_SOURCE = "Test Source";

  private static final String STATUS_ATTR_NAME =
      NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "." + NsiliConstants.STATUS;

  private static final Date TEST_CREATE_DATE = new Date(1000);

  private ORB orb;

  private POA rootPOA;

  private Metacard metacard;

  @Before
  public void setUp() throws AdapterInactive, InvalidName {
    orb = ORB.init(new String[0], null);
    rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    rootPOA.the_POAManager().activate();
    metacard = getTestCard();
  }

  @Test
  public void testResultAttributes() throws Exception {
    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    String sourceAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_CARD
            + "."
            + NsiliConstants.SOURCE_LIBRARY;

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(checkDagContains(dag, sourceAttr), is(true));

    List<String> singleAttrList = new ArrayList<>();
    singleAttrList.add(
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_CARD
            + "."
            + NsiliConstants.IDENTIFIER);
    DAG oneAttrDAG =
        ResultDAGConverter.convertResult(result, orb, rootPOA, singleAttrList, new HashMap<>());
    assertThat(checkDagContains(oneAttrDAG, sourceAttr), is(false));
  }

  @Test
  public void testAdvancedGeospatial() throws Exception {
    metacard.setAttribute(new AttributeImpl(Core.LOCATION, "POLYGON((1 1,1 2,2 2,2 1,1 1))"));

    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    String advGeoAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_PART
            + ":"
            + NsiliConstants.NSIL_COVERAGE
            + "."
            + NsiliConstants.ADVANCED_GEOSPATIAL;

    String boundingGeoAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_PART
            + ":"
            + NsiliConstants.NSIL_COVERAGE
            + "."
            + NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX;

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(checkDagContains(dag, advGeoAttr), is(true));
    assertThat(checkDagContains(dag, boundingGeoAttr), is(true));
  }

  @Test(expected = DagParsingException.class)
  public void testMandatoryAttributesFail() throws Exception {
    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    Map<String, List<String>> mandatoryAttrs = new HashMap<>();
    mandatoryAttrs.put(
        NsiliConstants.NSIL_COMMON, Collections.singletonList(NsiliConstants.IDENTIFIER_MISSION));
    ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), mandatoryAttrs);
  }

  @Test
  public void testMandatoryAttributesSuccess() throws Exception {
    metacard.setAttribute(new AttributeImpl(Core.RESOURCE_DOWNLOAD_URL, "http://test/file.jpg"));

    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    Map<String, List<String>> mandatoryAttrs = new HashMap<>();
    mandatoryAttrs.put(
        NsiliConstants.NSIL_CARD, Collections.singletonList(NsiliConstants.IDENTIFIER));
    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), mandatoryAttrs);
    assertThat(dag, notNullValue());

    String fileArchiveAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_FILE
            + "."
            + NsiliConstants.ARCHIVED;
    assertThat(checkDagContains(dag, fileArchiveAttr), is(true));

    String securityClassAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_SECURITY
            + "."
            + NsiliConstants.CLASSIFICATION;
    assertThat(checkDagContains(dag, securityClassAttr), is(true));

    String securityPolicyAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_SECURITY
            + "."
            + NsiliConstants.POLICY;
    assertThat(checkDagContains(dag, securityPolicyAttr), is(true));

    String securityReleasabilityAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_SECURITY
            + "."
            + NsiliConstants.RELEASABILITY;
    assertThat(checkDagContains(dag, securityReleasabilityAttr), is(true));

    String metadataSecurityClassAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_METADATA_SECURITY
            + "."
            + NsiliConstants.CLASSIFICATION;
    assertThat(checkDagContains(dag, metadataSecurityClassAttr), is(true));

    String metadataSecurityPolicyAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_METADATA_SECURITY
            + "."
            + NsiliConstants.POLICY;
    assertThat(checkDagContains(dag, metadataSecurityPolicyAttr), is(true));

    String metadataSecurityReleasabilityAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_METADATA_SECURITY
            + "."
            + NsiliConstants.RELEASABILITY;
    assertThat(checkDagContains(dag, metadataSecurityReleasabilityAttr), is(true));
  }

  @Test
  public void testCreateAttribute() throws Exception {
    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    String value = ResultDAGConverter.getAttributeMap(dag).get(STATUS_ATTR_NAME);
    assertThat(value, is(NsiliCardStatus.NEW.name()));
  }

  @Test
  public void testChangeAttribute() throws Exception {
    Date testModifiedDate = new Date(2000);
    metacard.setAttribute(new AttributeImpl(Core.METACARD_MODIFIED, testModifiedDate));
    metacard.setAttribute(
        new AttributeImpl(MetacardVersion.ACTION, MetacardVersion.Action.VERSIONED));
    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    String value = ResultDAGConverter.getAttributeMap(dag).get(STATUS_ATTR_NAME);
    assertThat(value, is(NsiliCardStatus.CHANGED.name()));

    metacard.setAttribute(
        new AttributeImpl(MetacardVersion.ACTION, MetacardVersion.Action.VERSIONED_CONTENT));
    result = new ResultImpl();
    result.setMetacard(metacard);

    dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    value = ResultDAGConverter.getAttributeMap(dag).get(STATUS_ATTR_NAME);
    assertThat(value, is(NsiliCardStatus.CHANGED.name()));
  }

  @Test
  public void testImageryAttributes() throws Exception {
    metacard.setAttribute(new AttributeImpl(Core.DATATYPE, "Image"));
    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    String cloudCoverAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_PART
            + ":"
            + NsiliConstants.NSIL_IMAGERY
            + "."
            + NsiliConstants.CLOUD_COVER_PCT;
    String niirsAttr =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_PART
            + ":"
            + NsiliConstants.NSIL_IMAGERY
            + "."
            + NsiliConstants.NIIRS;
    DAG cloudCoverDag =
        ResultDAGConverter.convertResult(
            result, orb, rootPOA, Arrays.asList(niirsAttr, cloudCoverAttr), new HashMap<>());
    assertThat(checkDagContains(cloudCoverDag, cloudCoverAttr), is(true));
    assertThat(checkDagContains(cloudCoverDag, niirsAttr), is(true));
  }

  @Test
  public void testCbrnAttributes() throws Exception {
    final String OPERATION_NAME = "Operation Name";
    final String INCIDENT_NUMBER = "Incident Number";
    final String TYPE = NsiliCbrnEvent.CHEMICAL.getSpecName();
    final String CATEGORY = "Category";
    final String SUBSTANCE = "Substance";
    final String ALARM_CLASSIFICATION = NsiliCbrnAlarmClassification.BELOW_THRESHOLD.getSpecName();

    metacard.setAttribute(
        new AttributeImpl(
            Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_OPERATION_NAME, OPERATION_NAME));
    metacard.setAttribute(
        new AttributeImpl(
            Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_INCIDENT_NUMBER, INCIDENT_NUMBER));
    metacard.setAttribute(
        new AttributeImpl(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_TYPE, TYPE));
    metacard.setAttribute(
        new AttributeImpl(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_CATEGORY, CATEGORY));
    metacard.setAttribute(
        new AttributeImpl(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_SUBSTANCE, SUBSTANCE));
    metacard.setAttribute(
        new AttributeImpl(
            Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_ALARM_CLASSIFICATION,
            ALARM_CLASSIFICATION));
    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    final String NSIL_CBRN_PART =
        NsiliConstants.NSIL_PRODUCT
            + ":"
            + NsiliConstants.NSIL_PART
            + ":"
            + NsiliConstants.NSIL_CBRN;
    final String CBRN_OPERATION_NAME = NSIL_CBRN_PART + "." + NsiliConstants.OPERATION_NAME;
    final String CBRN_INCIDENT_NUMBER = NSIL_CBRN_PART + "." + NsiliConstants.INCIDENT_NUM;
    final String CBRN_EVENT_TYPE = NSIL_CBRN_PART + "." + NsiliConstants.EVENT_TYPE;
    final String CBRN_CATEGORY = NSIL_CBRN_PART + "." + NsiliConstants.CBRN_CATEGORY;
    final String CBRN_SUBSTANCE = NSIL_CBRN_PART + "." + NsiliConstants.SUBSTANCE;
    final String CBRN_ALARM_CLASSIFICATION =
        NSIL_CBRN_PART + "." + NsiliConstants.ALARM_CLASSIFICATION;

    Map<String, String> attributeMap = ResultDAGConverter.getAttributeMap(dag);
    assertThat(attributeMap.get(CBRN_OPERATION_NAME), is(OPERATION_NAME));
    assertThat(attributeMap.get(CBRN_INCIDENT_NUMBER), is(INCIDENT_NUMBER));
    assertThat(attributeMap.get(CBRN_EVENT_TYPE), is(TYPE));
    assertThat(attributeMap.get(CBRN_CATEGORY), is(CATEGORY));
    assertThat(attributeMap.get(CBRN_SUBSTANCE), is(SUBSTANCE));
    assertThat(attributeMap.get(CBRN_ALARM_CLASSIFICATION), is(ALARM_CLASSIFICATION));
  }

  @Test
  public void testDeleteAttributeWithDownloadUrl() throws Exception {
    Date testModifiedDate = new Date(2000);
    metacard.setAttribute(new AttributeImpl(Core.MODIFIED, testModifiedDate));
    metacard.setAttribute(
        new AttributeImpl(Core.RESOURCE_DOWNLOAD_URL, "https://example.com/download/{id}"));
    metacard.setAttribute(
        new AttributeImpl(MetacardVersion.ACTION, MetacardVersion.Action.DELETED.getKey()));

    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    String value = ResultDAGConverter.getAttributeMap(dag).get(STATUS_ATTR_NAME);
    assertThat(value, is(NsiliCardStatus.OBSOLETE.name()));

    assertThat(checkDagContains(dag, NsiliConstants.PRODUCT_URL), is(false));
  }

  @Test
  public void testDeleteAttributeWithoutDownloadUrl() throws Exception {
    Date testModifiedDate = new Date(2000);
    metacard.setAttribute(new AttributeImpl(Core.MODIFIED, testModifiedDate));
    metacard.setAttribute(
        new AttributeImpl(MetacardVersion.ACTION, MetacardVersion.Action.DELETED.getKey()));

    ResultImpl result = new ResultImpl();
    result.setMetacard(metacard);

    DAG dag =
        ResultDAGConverter.convertResult(result, orb, rootPOA, new ArrayList<>(), new HashMap<>());
    assertThat(dag, notNullValue());

    String value = ResultDAGConverter.getAttributeMap(dag).get(STATUS_ATTR_NAME);
    assertThat(value, is(NsiliCardStatus.OBSOLETE.name()));

    assertThat(checkDagContains(dag, NsiliConstants.PRODUCT_URL), is(false));
  }

  @Test
  public void testModifyURL() throws Exception {
    ResultDAGConverter converter = new ResultDAGConverter();
    String url = "https://hostname:1234/ABCD";
    assertThat(
        ResultDAGConverter.modifyUrl(url, "name"),
        is("https://hostname:1234/ABCD&nsiliFilename=name"));
    ResultDAGConverter.setForceHttp(true);
    assertThat(
        ResultDAGConverter.modifyUrl(url, "name"),
        is("http://hostname:8181/ABCD&nsiliFilename=name"));
    url = "https://noport/ABCD";
    assertThat(
        ResultDAGConverter.modifyUrl(url, "name"),
        is("http://noport:8181/ABCD&nsiliFilename=name"));
  }

  private static boolean checkDagContains(DAG dag, String attribute) {
    List<String> dagAttrs = ResultDAGConverter.getAttributes(dag);
    return dagAttrs.contains(attribute);
  }

  private MetacardImpl getTestCard() {
    String id = UUID.randomUUID().toString();

    MetacardImpl metacard = new MetacardImpl();
    metacard.setId(id);
    metacard.setTitle(CARD_TITLE);
    metacard.setSourceId(CARD_SOURCE);
    metacard.setAttribute(new AttributeImpl(Core.METACARD_CREATED, TEST_CREATE_DATE));
    metacard.setCreatedDate(TEST_CREATE_DATE);
    metacard.setAttribute(new AttributeImpl(Core.METACARD_MODIFIED, TEST_CREATE_DATE));
    metacard.setModifiedDate(TEST_CREATE_DATE);
    metacard.setAttribute(new AttributeImpl(Isr.CLOUD_COVER, 1.0));
    metacard.setAttribute(
        new AttributeImpl(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE, 1.0));

    return metacard;
  }
}
