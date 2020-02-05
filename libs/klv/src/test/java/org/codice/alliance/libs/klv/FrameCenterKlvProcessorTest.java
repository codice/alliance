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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Media;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class FrameCenterKlvProcessorTest {

  private FrameCenterKlvProcessor frameCenterKlvProcessor;

  private Map<String, KlvHandler> handlerMap;

  private Attribute attribute;

  @Before
  public void setup() {
    frameCenterKlvProcessor = new FrameCenterKlvProcessor();
    attribute = mock(Attribute.class);

    LatitudeLongitudeHandler klvHandler = mock(LatitudeLongitudeHandler.class);
    when(klvHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(klvHandler.asSubsampledHandler(Mockito.anyInt())).thenCallRealMethod();

    handlerMap = new HashMap<>();
    handlerMap.put(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE, klvHandler);
    handlerMap.put(Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE, klvHandler);
  }

  @Test
  public void testNullAttribute() {
    GeometryOperator geometryOperator = mock(GeometryOperator.class);
    when(geometryOperator.apply(Matchers.any(), Matchers.any())).thenReturn(null);
    FrameCenterKlvProcessor processor = new FrameCenterKlvProcessor(geometryOperator);
    Metacard metacard = mock(Metacard.class);
    KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();
    configuration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 100);
    processor.process(handlerMap, metacard, configuration);
    verify(metacard, never()).setAttribute(Matchers.any());
  }

  /**
   * FrameCenterKlvProcessor requires that the configuration object contain the subsample count.
   * Make sure it returns without processing. The results of this test are only valid if {@link
   * #testOneCoordinate()} and {@link #testMultipleCoordinates()} pass.
   */
  @Test
  public void testMissingSubsampleConfiguration() {

    when(attribute.getValues()).thenReturn(Arrays.asList("POINT(0 0)", "POINT(1 1)", "POINT(2 2)"));

    Metacard metacard = mock(Metacard.class);

    KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();

    frameCenterKlvProcessor.process(handlerMap, metacard, configuration);

    verify(metacard, times(0)).setAttribute(any());
  }

  /**
   * FrameCenterKlvProcessor requires that the configuration object contains a subsample count >=
   * {@link FrameCenterKlvProcessor#MIN_SUBSAMPLE_COUNT}. Make sure it returns without processing.
   * The results of this test are only valid if {@link #testOneCoordinate()} and {@link
   * #testMultipleCoordinates()} pass.
   */
  @Test
  public void testMinSubsampleConfiguration() {

    when(attribute.getValues()).thenReturn(Arrays.asList("POINT(0 0)", "POINT(1 1)", "POINT(2 2)"));

    Metacard metacard = mock(Metacard.class);

    KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();
    configuration.set(
        KlvProcessor.Configuration.SUBSAMPLE_COUNT,
        FrameCenterKlvProcessor.MIN_SUBSAMPLE_COUNT - 1);

    frameCenterKlvProcessor.process(handlerMap, metacard, configuration);

    verify(metacard, times(0)).setAttribute(any());
  }

  @Test
  public void testMultipleCoordinates() throws ParseException {
    verifyFrameCenter(
        Arrays.asList("POINT(0 0)", "POINT(1 1)", "POINT(2 2)"), "LINESTRING(0 0, 1 1, 2 2)");
  }

  @Test
  public void testOneCoordinate() throws ParseException {
    verifyFrameCenter(Collections.singletonList("POINT(1 2)"), "POINT(1 2)");
  }

  private void verifyFrameCenter(List<Serializable> coordinates, String frameCenterWkt)
      throws ParseException {
    when(attribute.getValues()).thenReturn(coordinates);

    Metacard metacard = new MetacardImpl();

    KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();
    configuration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 100);

    frameCenterKlvProcessor.process(handlerMap, metacard, configuration);

    assertThat(metacard.getAttribute(Media.FRAME_CENTER).getValue(), is(normalize(frameCenterWkt)));
  }

  private String normalize(String wkt) throws ParseException {
    return new WKTWriter().write(new WKTReader().read(wkt).norm());
  }

  @Test
  public void testAccept() {
    KlvProcessor.Visitor visitor = mock(KlvProcessor.Visitor.class);
    frameCenterKlvProcessor.accept(visitor);
    verify(visitor).visit(frameCenterKlvProcessor);
  }
}
