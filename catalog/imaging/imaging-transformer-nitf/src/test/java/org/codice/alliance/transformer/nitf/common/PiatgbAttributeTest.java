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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class PiatgbAttributeTest {

  private Tre tre;

  @Before
  public void setup() {
    tre = mock(Tre.class);
  }

  @Test
  public void testTargetUtm() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.TARGET_UTM_ATTRIBUTE.getShortName()))
        .thenReturn("55HFA9359093610");
    Serializable actual = PiatgbAttribute.TARGET_UTM_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("55HFA9359093610"));
  }

  @Test
  public void testPiaTargetIdentification() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.PIA_TARGET_IDENTIFICATION_ATTRIBUTE.getShortName()))
        .thenReturn("ABCDEFGHIJUVWXY");
    Serializable actual =
        PiatgbAttribute.PIA_TARGET_IDENTIFICATION_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("ABCDEFGHIJUVWXY"));
  }

  @Test
  public void testPiaCountry() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.PIA_COUNTRY_ATTRIBUTE.getShortName())).thenReturn("AS");
    Serializable actual = PiatgbAttribute.PIA_COUNTRY_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("AS"));
  }

  @Test
  public void testPiaCategory() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.PIA_CATEGORY_ATTRIBUTE.getShortName()))
        .thenReturn("702XX");
    Serializable actual = PiatgbAttribute.PIA_CATEGORY_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("702XX"));
  }

  @Test
  public void testTargetGeographicCoordinates() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.TARGET_GEOGRAPHIC_COORDINATES_ATTRIBUTE.getShortName()))
        .thenReturn("351655S1490742E");
    Serializable actual =
        PiatgbAttribute.TARGET_GEOGRAPHIC_COORDINATES_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("351655S1490742E"));
  }

  @Test
  public void testTargetCoordinateDatum() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.TARGET_COORDINATE_DATUM_ATTRIBUTE.getShortName()))
        .thenReturn("WGE");
    Serializable actual =
        PiatgbAttribute.TARGET_COORDINATE_DATUM_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("WGE"));
  }

  @Test
  public void testTargetName() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.TARGET_NAME_ATTRIBUTE.getShortName()))
        .thenReturn("Canberra Hill                         ");
    Serializable actual = PiatgbAttribute.TARGET_NAME_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("Canberra Hill"));
  }

  @Test
  public void testPercentageOfCoverage() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.PERCENTAGE_OF_COVERAGE_ATTRIBUTE.getShortName()))
        .thenReturn("57");
    Integer percentageOfCoverage =
        (Integer) PiatgbAttribute.PERCENTAGE_OF_COVERAGE_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(percentageOfCoverage, is(57));
  }

  @Test
  public void testTargetLatitude() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.TARGET_LATITUDE_ATTRIBUTE.getShortName()))
        .thenReturn("-35.30812 ");
    Float targetLatitude =
        (Float) PiatgbAttribute.TARGET_LATITUDE_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(targetLatitude, is(-35.30812F));
  }

  @Test
  public void testTargetLongitude() throws NitfFormatException {
    when(tre.getFieldValue(PiatgbAttribute.TARGET_LONGITUDE_ATTRIBUTE.getShortName()))
        .thenReturn("+149.12447 ");
    Float targetLongitude =
        (Float) PiatgbAttribute.TARGET_LONGITUDE_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(targetLongitude, is(149.12447F));
  }
}
