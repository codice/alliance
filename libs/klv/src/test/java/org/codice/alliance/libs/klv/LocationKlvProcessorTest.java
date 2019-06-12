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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LocationKlvProcessorTest {

  private String wkt;

  private String wktLineString;

  private LocationKlvProcessor locationKlvProcessor;

  private GeoBoxHandler geoBoxHandler;

  private LatitudeLongitudeHandler latLonHandler;

  private Metacard metacard;

  private KlvProcessor.Configuration klvConfiguration;

  private Map<String, KlvHandler> handlers;

  private GeometryOperator geometryFunction;

  @Before
  public void setup() {
    wkt = "POLYGON ((0 0, 5 0, 5 5, 0 5, 0 0))";
    wktLineString = "LINESTRING (0 0, 5 5, 10 10)";
    geometryFunction = GeometryOperator.IDENTITY;
    locationKlvProcessor = new LocationKlvProcessor(GeometryOperator.IDENTITY, geometryFunction);
    geoBoxHandler = mock(GeoBoxHandler.class);
    latLonHandler = mock(LatitudeLongitudeHandler.class);

    Attribute attribute = mock(Attribute.class);

    when(attribute.getValues()).thenReturn(Collections.singletonList(wkt));
    when(geoBoxHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(geoBoxHandler.getAttributeName()).thenReturn(AttributeNameConstants.CORNER);
    when(geoBoxHandler.asSubsampledHandler(Mockito.anyInt())).thenReturn(geoBoxHandler);

    attribute = mock(Attribute.class);
    when(attribute.getValues()).thenReturn(Collections.emptyList());
    when(latLonHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(latLonHandler.getAttributeName()).thenReturn(AttributeNameConstants.FRAME_CENTER);
    when(latLonHandler.asSubsampledHandler(Mockito.anyInt())).thenReturn(latLonHandler);

    metacard = new MetacardImpl();
    klvConfiguration = new KlvProcessor.Configuration();
    handlers = new HashMap<>();
    handlers.put(AttributeNameConstants.CORNER, geoBoxHandler);
    handlers.put(AttributeNameConstants.FRAME_CENTER, latLonHandler);
  }

  /**
   * + * Simulate the situation where the post-union geometry function returns null, and + * verify
   * that the location on the metacard is not set. +
   */
  @Test
  public void testNullGeometry() {
    metacard = mock(Metacard.class);

    geometryFunction = mock(GeometryOperator.class);
    when(geometryFunction.apply(any(), any())).thenReturn(null);
    locationKlvProcessor = new LocationKlvProcessor(GeometryOperator.IDENTITY, geometryFunction);

    klvConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 50);

    Attribute attribute = mock(Attribute.class);

    when(attribute.getValues()).thenReturn(Collections.emptyList());
    when(geoBoxHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(geoBoxHandler.getAttributeName()).thenReturn(AttributeNameConstants.CORNER);

    attribute = mock(Attribute.class);
    when(attribute.getValues())
        .thenReturn(Arrays.asList("POINT(0 0)", "POINT(5 5)", "POINT(10 10)"));
    when(latLonHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(latLonHandler.getAttributeName()).thenReturn(AttributeNameConstants.FRAME_CENTER);

    locationKlvProcessor.process(handlers, metacard, klvConfiguration);

    verify(metacard, never()).setAttribute(any());
  }

  @Test
  public void testMissingHandlers() {

    klvConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 50);

    locationKlvProcessor.process(Collections.emptyMap(), metacard, klvConfiguration);

    assertThat(metacard.getLocation(), nullValue());
  }

  @Test
  public void testGetGeometryFunction() {
    assertThat(locationKlvProcessor.getGeometryFunction(), is(geometryFunction));
  }

  @Test
  public void testAccept() {
    KlvProcessor.Visitor visitor = mock(KlvProcessor.Visitor.class);
    locationKlvProcessor.accept(visitor);
    verify(visitor).visit(locationKlvProcessor);
  }

  /** Test the case of when corner data is available. */
  @Test
  public void testProcessCorner() {

    klvConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 50);

    locationKlvProcessor.process(handlers, metacard, klvConfiguration);

    assertThat(metacard.getLocation(), is(wkt));
  }

  /** Test the case of then corner data is not availabe, but frame center data is available. */
  @Test
  public void testProcessFrameCenter() {
    klvConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 50);

    Attribute attribute = mock(Attribute.class);

    when(attribute.getValues()).thenReturn(Collections.emptyList());
    when(geoBoxHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(geoBoxHandler.getAttributeName()).thenReturn(AttributeNameConstants.CORNER);

    attribute = mock(Attribute.class);
    when(attribute.getValues())
        .thenReturn(Arrays.asList("POINT(0 0)", "POINT(5 5)", "POINT(10 10)"));
    when(latLonHandler.asAttribute()).thenReturn(Optional.of(attribute));
    when(latLonHandler.getAttributeName()).thenReturn(AttributeNameConstants.FRAME_CENTER);

    locationKlvProcessor.process(handlers, metacard, klvConfiguration);

    assertThat(metacard.getLocation(), is(wktLineString));
  }

  /** Test where the subsample count is missing from the configuration. */
  @Test
  public void testMissingConfiguration() {

    locationKlvProcessor.process(handlers, metacard, klvConfiguration);

    assertThat(metacard.getLocation(), nullValue());
  }

  /** Test a subsample count that is below the minimum required by LocationKlvProcessor. */
  @Test
  public void testBadConfiguration() {

    klvConfiguration.set(
        KlvProcessor.Configuration.SUBSAMPLE_COUNT, LocationKlvProcessor.MIN_SUBSAMPLE_COUNT - 1);

    locationKlvProcessor.process(handlers, metacard, klvConfiguration);

    assertThat(metacard.getLocation(), nullValue());
  }
}
