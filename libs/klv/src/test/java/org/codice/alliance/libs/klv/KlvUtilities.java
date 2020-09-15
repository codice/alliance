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
package org.codice.alliance.libs.klv;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.Klv;
import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.mockito.ArgumentCaptor;

class KlvUtilities {

  /**
   * Helper for testing KlvProcessor implementations that derive from DistinctKlvProcessor.
   *
   * @param klvProcessor the processor being tested
   * @param input list of serializable values that are fed to the processor
   * @return an argument captor of the attribute created by the processor
   */
  public static ArgumentCaptor<Attribute> testKlvProcessor(
      KlvProcessor klvProcessor, String stangField, List<Serializable> input) {

    Attribute attribute = mock(Attribute.class);

    when(attribute.getValues()).thenReturn(input);

    KlvHandler klvHandler = mock(KlvHandler.class);

    when(klvHandler.asAttribute()).thenReturn(Optional.of(attribute));

    Metacard metacard = mock(Metacard.class);
    MetacardType metacardType = mock(MetacardType.class);
    AttributeDescriptor attributeDescriptor = mock(AttributeDescriptor.class);
    AttributeType attributeType = mock(AttributeType.class);

    when(metacard.getMetacardType()).thenReturn(metacardType);
    when(metacardType.getAttributeDescriptor(anyString())).thenReturn(attributeDescriptor);
    when(attributeDescriptor.getType()).thenReturn(attributeType);
    when(attributeType.getBinding()).thenReturn(input.get(0).getClass());

    KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();

    klvProcessor.process(Collections.singletonMap(stangField, klvHandler), metacard, configuration);

    ArgumentCaptor<Attribute> argumentCaptor = ArgumentCaptor.forClass(Attribute.class);

    verify(metacard).setAttribute(argumentCaptor.capture());

    return argumentCaptor;
  }

  /**
   * value should be between -180 and 180
   *
   * @param name name of the klv data element
   * @param value value of the klv data element
   * @return klv data element
   * @throws KlvDecodingException
   */
  public static KlvIntegerEncodedFloatingPoint createTestFloat(String name, double value)
      throws KlvDecodingException {

    long encodedMin = -180;
    long encodedMax = 180;

    double actualRange = encodedMax - encodedMin;

    double scaledValue = (value - encodedMin) / actualRange;

    long encodedRangeMin = ((long) Integer.MIN_VALUE) + 1;

    long encodedRange = ((long) Integer.MAX_VALUE) - encodedRangeMin;

    long encodedValue = (long) (scaledValue * encodedRange + encodedRangeMin);

    byte byte1 = (byte) ((encodedValue & 0xFF000000) >> 24);
    byte byte2 = (byte) ((encodedValue & 0xFF0000) >> 16);
    byte byte3 = (byte) ((encodedValue & 0xFF00) >> 8);
    byte byte4 = (byte) (encodedValue & 0xFF);

    final byte[] klvBytes = {-8, 4, byte1, byte2, byte3, byte4};
    final KlvInt klvInt = new KlvInt(new byte[] {-8}, name);
    final KlvIntegerEncodedFloatingPoint sensorRelativeElevationAngle =
        new KlvIntegerEncodedFloatingPoint(
            klvInt,
            // Short.MIN_VALUE is an "out of range" indicator, so it is not included in the range.
            Integer.MIN_VALUE + 1,
            Integer.MAX_VALUE,
            encodedMin,
            encodedMax);
    final KlvContext decodedKlvContext =
        decodeKLV(
            Klv.KeyLength.ONE_BYTE,
            Klv.LengthEncoding.ONE_BYTE,
            sensorRelativeElevationAngle,
            klvBytes);

    return (KlvIntegerEncodedFloatingPoint) decodedKlvContext.getDataElementByName(name);
  }

  private static KlvContext decodeKLV(
      final Klv.KeyLength keyLength,
      final Klv.LengthEncoding lengthEncoding,
      final KlvDataElement dataElement,
      final byte[] encodedBytes)
      throws KlvDecodingException {
    final KlvContext klvContext = new KlvContext(keyLength, lengthEncoding);
    klvContext.addDataElement(dataElement);
    return new KlvDecoder(klvContext).decode(encodedBytes);
  }
}
