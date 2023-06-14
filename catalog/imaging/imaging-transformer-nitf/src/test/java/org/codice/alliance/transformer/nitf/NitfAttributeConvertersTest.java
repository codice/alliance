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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Date;
import org.codice.imaging.nitf.core.common.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NitfAttributeConvertersTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testValidFipsToSingleIso() {
    // when
    String convertedValue = NitfAttributeConverters.fipsToStandardCountryCode("US");

    // then
    assertThat(convertedValue, is("USA"));
  }

  @Test
  public void testNullFipsValue() {
    // then
    assertThat(NitfAttributeConverters.fipsToStandardCountryCode(null), is(nullValue()));
  }

  @Test
  public void testConvertNitfDate() {
    // setup
    DateTime dateTime = NitfTestCommons.createNitfDateTime(1997, 12, 17, 10, 26, 30);

    // when
    Date convertedDate = NitfAttributeConverters.nitfDate(dateTime);

    // then
    assertThat(dateTime.getZonedDateTime().toInstant(), is(convertedDate.toInstant()));
  }

  @Test
  public void testConvertNitfDateWithNull() {
    // when
    Date convertedDate = NitfAttributeConverters.nitfDate(null);

    // then
    assertThat(convertedDate, is(nullValue()));
  }

  @Test
  public void testConvertNitfDateWithNullZonedTime() {
    // setup
    DateTime mockDateTime = mock(DateTime.class);
    doReturn(null).when(mockDateTime).getZonedDateTime();

    // when
    Date convertedDate = NitfAttributeConverters.nitfDate(mockDateTime);

    // then
    assertThat(convertedDate, is(nullValue()));
  }
}
