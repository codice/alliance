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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import org.codice.alliance.transformer.nitf.NitfAttributeConverters;
import org.codice.alliance.transformer.nitf.NitfAttributeTransformException;
import org.codice.alliance.transformer.nitf.NitfTestCommons;
import org.codice.ddf.internal.country.converter.api.CountryCodeConverter;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.security.FileSecurityMetadata;
import org.junit.Before;
import org.junit.Test;

public class NitfHeaderAttributeTest {

  private NitfHeader nitfHeader;

  @Before
  public void setUp() {
    nitfHeader = mock(NitfHeader.class);
  }

  @Test
  public void testValidFileClassificationSystem() {
    NitfTestCommons.setupNitfUtilities("US", Collections.singletonList("ABC"));

    FileSecurityMetadata fsmMock = mock(FileSecurityMetadata.class);
    when(nitfHeader.getFileSecurityMetadata()).thenReturn(fsmMock);
    when(nitfHeader.getFileSecurityMetadata().getSecurityClassificationSystem()).thenReturn("US");
    Serializable value =
        NitfHeaderAttribute.FILE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE
            .getAccessorFunction()
            .apply(nitfHeader);

    assertThat(value, is("ABC"));
  }

  @SuppressWarnings("ReturnValueIgnored")
  @Test(expected = NitfAttributeTransformException.class)
  public void testMultipleConvertedCountryCodesForFileClassificationSystem() {
    NitfTestCommons.setupNitfUtilities("US", Arrays.asList("ABC", "XYZ"));

    FileSecurityMetadata fsmMock = mock(FileSecurityMetadata.class);
    when(nitfHeader.getFileSecurityMetadata()).thenReturn(fsmMock);
    when(nitfHeader.getFileSecurityMetadata().getSecurityClassificationSystem()).thenReturn("US");
    NitfHeaderAttribute.FILE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE
        .getAccessorFunction()
        .apply(nitfHeader);
  }

  @Test
  public void testNitfFileReleasabilityWithMultipleSpaces() {
    CountryCodeConverter mockCountryCodeConverter = mock(CountryCodeConverter.class);
    doReturn(Collections.singletonList("USA"))
        .when(mockCountryCodeConverter)
        .convertFipsToIso3("US");
    doReturn(Collections.singletonList("GBR"))
        .when(mockCountryCodeConverter)
        .convertFipsToIso3("GB");
    new NitfAttributeConverters(mockCountryCodeConverter);

    FileSecurityMetadata fsmMock = mock(FileSecurityMetadata.class);
    when(nitfHeader.getFileSecurityMetadata()).thenReturn(fsmMock);
    when(nitfHeader.getFileSecurityMetadata().getReleaseInstructions()).thenReturn("US    GB");
    Serializable value =
        NitfHeaderAttribute.FILE_RELEASING_INSTRUCTIONS_ATTRIBUTE
            .getAccessorFunction()
            .apply(nitfHeader);

    assertThat(value, is(equalTo("USA GBR")));
  }
}
