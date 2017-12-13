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

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import org.codice.alliance.transformer.nitf.NitfAttributeTransformException;
import org.codice.alliance.transformer.nitf.NitfTestCommons;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StdidcAttributeTest {

  private Tre tre;

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    tre = mock(Tre.class);
  }

  @Test
  public void testValidCountryCode() throws Exception {
    NitfTestCommons.setupNitfUtilities("US", Collections.singletonList("USA"));
    when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn("US");
    Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("USA"));
    actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("US"));
  }

  @Test
  public void testMultiIso3Codes() throws Exception {
    NitfTestCommons.setupNitfUtilities("WE", Collections.singletonList("PSE"));
    when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn("WE");
    Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("PSE"));
    actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("WE"));
  }

  @Test
  public void testInvalidCountryCode() throws Exception {
    NitfTestCommons.setupNitfUtilities("0", Collections.emptyList());
    when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn("0");
    Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, nullValue());
    actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is("0"));
  }

  @Test
  public void testEmptyCountryCode() throws Exception {
    NitfTestCommons.setupNitfUtilities(null, Collections.emptyList());
    when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn(null);
    Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is(nullValue()));
    actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, nullValue());
  }

  @Test
  public void testMultipleConvertedCountryCodes() throws Exception {
    NitfTestCommons.setupNitfUtilities("US", Arrays.asList("ABC", "XYZ"));
    when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn("US");

    expectedException.expect(NitfAttributeTransformException.class);
    expectedException.expect(hasProperty("originalValue", is("US")));

    StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction().apply(tre);
  }
}
