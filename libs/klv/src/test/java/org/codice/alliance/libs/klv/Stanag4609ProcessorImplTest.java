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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.Klv;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.junit.Before;
import org.junit.Test;

public class Stanag4609ProcessorImplTest {

  private static final String FIELD_NAME = "field";

  private Stanag4609Processor stanag4609Processor;

  private KlvHandler klvHandler;

  private KlvHandler defaultKlvHandler;

  private KlvIntegerEncodedFloatingPoint klvIntegerEncodedFloatingPoint;

  private Map<String, KlvDataElement> dataElements;

  @Before
  public void setup() throws KlvDecodingException {
    stanag4609Processor = new Stanag4609ProcessorImpl(mock(PostProcessor.class));
    klvHandler = mock(KlvHandler.class);
    defaultKlvHandler = mock(KlvHandler.class);
    klvIntegerEncodedFloatingPoint = KlvUtilities.createTestFloat(FIELD_NAME, 100);
    dataElements = new HashMap<>();
  }

  @Test
  public void testRegularHandlerForCallDataElementHandlers() throws KlvDecodingException {

    stanag4609Processor.callDataElementHandlers(
        Collections.singletonMap(FIELD_NAME, klvHandler),
        defaultKlvHandler,
        klvIntegerEncodedFloatingPoint,
        dataElements);

    verify(klvHandler, atLeastOnce()).accept(klvIntegerEncodedFloatingPoint);
  }

  @Test
  public void testDefaultHandlerForCallDataElementHandlers() throws KlvDecodingException {

    KlvIntegerEncodedFloatingPoint otherKlvIntegerEncodedFloatingPoint =
        KlvUtilities.createTestFloat("someOtherField", 100);

    stanag4609Processor.callDataElementHandlers(
        Collections.singletonMap(FIELD_NAME, klvHandler),
        defaultKlvHandler,
        otherKlvIntegerEncodedFloatingPoint,
        dataElements);

    verify(defaultKlvHandler, atLeastOnce()).accept(otherKlvIntegerEncodedFloatingPoint);
  }

  @Test
  public void testHandleWithKlvContext() throws KlvDecodingException {

    KlvContext klvContext =
        new KlvContext(
            Klv.KeyLength.ONE_BYTE,
            Klv.LengthEncoding.ONE_BYTE,
            Collections.singleton(klvIntegerEncodedFloatingPoint));

    stanag4609Processor.handle(
        Collections.singletonMap(FIELD_NAME, klvHandler),
        defaultKlvHandler,
        klvContext,
        dataElements);

    verify(klvHandler, atLeastOnce()).accept(klvIntegerEncodedFloatingPoint);
  }

  @Test
  public void testHandleWithKlvLocalSet() throws KlvDecodingException {

    KlvContext klvContext =
        new KlvContext(
            Klv.KeyLength.ONE_BYTE,
            Klv.LengthEncoding.ONE_BYTE,
            Collections.singleton(klvIntegerEncodedFloatingPoint));

    KlvDataElement klvLocalSet = mock(KlvLocalSet.class);
    when(klvLocalSet.getValue()).thenReturn(klvContext);

    stanag4609Processor.handle(
        Collections.singletonMap(FIELD_NAME, klvHandler),
        defaultKlvHandler,
        klvLocalSet,
        dataElements);

    verify(klvHandler, atLeastOnce()).accept(klvIntegerEncodedFloatingPoint);
  }

  @Test
  public void testHandleWithKlvDataElement() throws KlvDecodingException {

    stanag4609Processor.handle(
        Collections.singletonMap(FIELD_NAME, klvHandler),
        defaultKlvHandler,
        klvIntegerEncodedFloatingPoint,
        dataElements);

    verify(klvHandler, atLeastOnce()).accept(klvIntegerEncodedFloatingPoint);
  }

  @Test
  public void testHandleWithStanagMetadata() throws KlvDecodingException {

    DecodedKLVMetadataPacket p2 = mock(DecodedKLVMetadataPacket.class);
    when(p2.getDecodedKLV())
        .thenReturn(
            new KlvContext(
                Klv.KeyLength.ONE_BYTE,
                Klv.LengthEncoding.ONE_BYTE,
                Collections.singleton(klvIntegerEncodedFloatingPoint)));

    Map<String, KlvHandler> handlers = Collections.singletonMap(FIELD_NAME, klvHandler);
    Map<Integer, List<DecodedKLVMetadataPacket>> stanag =
        Collections.singletonMap(1, Collections.singletonList(p2));

    stanag4609Processor.handle(handlers, defaultKlvHandler, stanag);

    verify(klvHandler, atLeastOnce()).accept(klvIntegerEncodedFloatingPoint);
  }
}
