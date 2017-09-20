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
package org.codice.alliance.transformer.nitf.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class PiaprdAttributeTest {

  private Tre tre;

  @Before
  public void setUp() {
    tre = mock(Tre.class);
  }

  @Test
  public void testPiaprdAttribute() {
    PiaprdAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getShortName(), notNullValue()));
    PiaprdAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getLongName(), notNullValue()));
  }

  @Test
  public void testAccessId() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.ACCESS_ID_ATTRIBUTE.getShortName()))
        .thenReturn("THIS IS AN IPA FILE.                                       -END-");

    String accessId = (String) PiaprdAttribute.ACCESS_ID_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(accessId, is("THIS IS AN IPA FILE.                                       -END-"));
  }

  @Test
  public void testFmControlNumber() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.FM_CONTROL_NUMBER_ATTRIBUTE.getShortName()))
        .thenReturn("PXX                        -END-");

    String fmControlNumber =
        (String) PiaprdAttribute.FM_CONTROL_NUMBER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(fmControlNumber, is("PXX                        -END-"));
  }

  @Test
  public void testSubjectiveDetail() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.SUBJECTIVE_DETAIL_ATTRIBUTE.getShortName()))
        .thenReturn("P");

    String subjectiveDetail =
        (String) PiaprdAttribute.SUBJECTIVE_DETAIL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(subjectiveDetail, is("P"));
  }

  @Test
  public void testProductCode() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.PRODUCT_CODE_ATTRIBUTE.getShortName())).thenReturn("YY");

    String productCode =
        (String) PiaprdAttribute.PRODUCT_CODE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(productCode, is("YY"));
  }

  @Test
  public void testProducerSupplement() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.PRODUCER_SUPPLEMENT_ATTRIBUTE.getShortName()))
        .thenReturn("UNKNOW");

    String producerSupplement =
        (String) PiaprdAttribute.PRODUCER_SUPPLEMENT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(producerSupplement, is("UNKNOW"));
  }

  @Test
  public void testProductIdNumber() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.PRODUCT_ID_NUMBER_ATTRIBUTE.getShortName()))
        .thenReturn("X211           -END-");

    String productIdNumber =
        (String) PiaprdAttribute.PRODUCT_ID_NUMBER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(productIdNumber, is("X211           -END-"));
  }

  @Test
  public void testProductShortName() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.PRODUCT_SHORT_NAME_ATTRIBUTE.getShortName()))
        .thenReturn("JUNK FILE.");

    String productShortName =
        (String) PiaprdAttribute.PRODUCT_SHORT_NAME_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(productShortName, is("JUNK FILE."));
  }

  @Test
  public void testProducerCode() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.PRODUCER_CODE_ATTRIBUTE.getShortName()))
        .thenReturn("27");

    String producerCode =
        (String) PiaprdAttribute.PRODUCER_CODE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(producerCode, is("27"));
  }

  @Test
  public void testProducerCreateTime() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.PRODUCER_CREATE_TIME_ATTRIBUTE.getShortName()))
        .thenReturn("26081023ZOCT95");

    String producerCreateTime =
        (String) PiaprdAttribute.PRODUCER_CREATE_TIME_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(producerCreateTime, is("26081023ZOCT95"));
  }

  @Test
  public void testMapId() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.MAP_ID_ATTRIBUTE.getShortName()))
        .thenReturn("132                                -END-");

    String mapId = (String) PiaprdAttribute.MAP_ID_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(mapId, is("132                                -END-"));
  }

  @Test
  public void testKeywordRepetitions() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.KEYWORD_REPETITIONS_ATTRIBUTE.getShortName()))
        .thenReturn("01");

    Integer keywordRepetitions =
        (Integer) PiaprdAttribute.KEYWORD_REPETITIONS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(keywordRepetitions, is(Integer.parseInt("01")));
  }

  @Test
  public void testKeyword() throws NitfFormatException {
    when(tre.getFieldValue(PiaprdAttribute.KEYWORD_ATTRIBUTE.getShortName())).thenReturn(null);

    String keyword = (String) PiaprdAttribute.KEYWORD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(keyword, nullValue());
  }
}
