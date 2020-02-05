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
package org.codice.alliance.transformer.nitf.gmti;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.codice.alliance.transformer.nitf.MetacardFactory;
import org.codice.alliance.transformer.nitf.NitfTestCommons;
import org.codice.alliance.transformer.nitf.TreUtilityTest;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderTransformer;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryFactory;

public class NitfGmtiTransformerTest {

  private static final String GMTI_TEST_NITF = "src/test/resources/gmti-test.ntf";

  private static final String GMTI_METACARD = "isr.gmti";

  private final List<MetacardType> metacardTypeList = new ArrayList<>();

  private MetacardFactory metacardFactory;

  private NitfHeaderTransformer nitfHeaderTransformer;

  private NitfGmtiTransformer nitfGmtiTransformer;

  @Before
  public void setUp() {
    this.metacardFactory = new MetacardFactory();
    metacardFactory.setMetacardType(new MetacardTypeImpl(GMTI_METACARD, metacardTypeList));
    this.nitfHeaderTransformer = new NitfHeaderTransformer();
    this.nitfGmtiTransformer = new NitfGmtiTransformer();
    this.nitfGmtiTransformer.setGeometryFactory(new GeometryFactory());

    TreUtilityTest.createFileIfNecessary(GMTI_TEST_NITF, TreUtilityTest::createNitfNoImageTres);
    NitfTestCommons.setupNitfUtilities("US", Collections.singletonList("USA"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNitfSegmentsFlow() {
    nitfGmtiTransformer.transform(null, new MetacardImpl());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMetacard() {
    NitfSegmentsFlow nitfSegmentsFlow = mock(NitfSegmentsFlow.class);
    nitfGmtiTransformer.transform(nitfSegmentsFlow, null);
  }

  @Test
  public void testTre() throws IOException, NitfFormatException {
    NitfSegmentsFlow nitfSegmentsFlow =
        new NitfParserInputFlowImpl().inputStream(getInputStream(GMTI_TEST_NITF)).allData();

    Metacard metacard = metacardFactory.createMetacard("101");
    nitfSegmentsFlow = nitfHeaderTransformer.transform(nitfSegmentsFlow, metacard);
    metacard = nitfGmtiTransformer.transform(nitfSegmentsFlow, metacard);

    assertNotNull(metacard);

    validateDate(metacard.getCreatedDate(), "2016-06-22 23:39:22");
    validateDate(metacard.getEffectiveDate(), "2016-06-22 23:39:22");
    validateDate(metacard.getModifiedDate(), "2016-06-22 23:39:22");
    assertThat(metacard.getMetacardType().getName(), is("isr.gmti"));

    Map<NitfAttribute, String> mtirpbAttributesMap = initMtirpbAttributes();
    assertMtirpbAttributes(metacard, mtirpbAttributesMap);
  }

  @Test
  public void testIndexedMtirpbAttributeLongNames() {
    IndexedMtirpbAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getLongName(), notNullValue()));
  }

  @Test
  public void testClassificationCategory() throws NitfFormatException {
    Tre tre = mock(Tre.class);
    when(tre.getFieldValue(
            IndexedMtirpbAttribute.INDEXED_TARGET_CLASSIFICATION_CATEGORY_ATTRIBUTE.getShortName()))
        .thenReturn(null);
    String value =
        IndexedMtirpbAttribute.INDEXED_TARGET_CLASSIFICATION_CATEGORY_ATTRIBUTE
            .getAccessorFunction()
            .apply(tre)
            .toString();
    assertThat(value, is("Unknown"));
  }

  private void validateDate(Date date, String expectedDate) {
    assertNotNull(date);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertThat(formatter.format(date), is(expectedDate));
  }

  private InputStream getInputStream(String filename) throws FileNotFoundException {
    return new FileInputStream(new File(filename));
  }

  private void assertMtirpbAttributes(Metacard metacard, Map<NitfAttribute, String> map) {
    for (Map.Entry<NitfAttribute, String> entry : map.entrySet()) {
      for (AttributeDescriptor attributeDescriptor :
          (Set<AttributeDescriptor>) entry.getKey().getAttributeDescriptors()) {
        Attribute attribute = metacard.getAttribute(attributeDescriptor.getName());
        if (attribute != null) {
          assertThat(attribute.getValue(), is(entry.getValue()));
        } else {
          assertThat(attribute, nullValue());
        }
      }
    }
  }

  private Map<NitfAttribute, String> initMtirpbAttributes() {
    // key value pair of nitf attributes and expected getAttributes
    Map<NitfAttribute, String> map = new HashMap<>();
    map.put(MtirpbAttribute.AIRCRAFT_ALTITUDE_ATTRIBUTE, "150000");
    map.put(MtirpbAttribute.NUMBER_OF_VALID_TARGETS_ATTRIBUTE, "001");
    map.put(MtirpbAttribute.AIRCRAFT_ALTITUDE_UNITS_ATTRIBUTE, "m");
    map.put(MtirpbAttribute.AIRCRAFT_HEADING_ATTRIBUTE, "000");
    map.put(MtirpbAttribute.AIRCRAFT_LOCATION_ATTRIBUTE, "POINT (+52.123456 -004.123456)");
    map.put(MtirpbAttribute.COSINE_OF_GRAZE_ANGLE_ATTRIBUTE, "0.03111");
    map.put(MtirpbAttribute.DESTINATION_POINT_ATTRIBUTE, "00");
    map.put(MtirpbAttribute.PATCH_NUMBER_ATTRIBUTE, "0001");
    map.put(MtirpbAttribute.SCAN_DATE_AND_TIME_ATTRIBUTE, "20141108235219");
    map.put(MtirpbAttribute.WIDE_AREA_MTI_BAR_NUMBER_ATTRIBUTE, "1");
    map.put(MtirpbAttribute.WIDE_AREA_MTI_FRAME_NUMBER_ATTRIBUTE, "00001");
    map.put(IndexedMtirpbAttribute.INDEXED_TARGET_AMPLITUDE_ATTRIBUTE, "06");
    map.put(IndexedMtirpbAttribute.INDEXED_TARGET_CLASSIFICATION_CATEGORY_ATTRIBUTE, "Unknown");
    map.put(IndexedMtirpbAttribute.INDEXED_TARGET_GROUND_SPEED_ATTRIBUTE, "000");
    map.put(IndexedMtirpbAttribute.INDEXED_TARGET_HEADING_ATTRIBUTE, "000");
    map.put(
        IndexedMtirpbAttribute.INDEXED_TARGET_LOCATION_ATTRIBUTE,
        "MULTIPOINT ((52.1234567 -4.1234567))");
    map.put(IndexedMtirpbAttribute.INDEXED_TARGET_LOCATION_ACCURACY_ATTRIBUTE, "000.00");
    map.put(IndexedMtirpbAttribute.INDEXED_TARGET_RADIAL_VELOCITY_ATTRIBUTE, "+013");
    return map;
  }
}
