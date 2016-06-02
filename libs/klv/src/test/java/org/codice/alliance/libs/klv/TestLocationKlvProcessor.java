/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.MetacardImpl;

public class TestLocationKlvProcessor {

    private String wkt;

    private LocationKlvProcessor locationKlvProcessor;

    private GeoBoxHandler klvHandler;

    private Metacard metacard;

    private KlvProcessor.Configuration klvConfiguration;

    private Map<String, KlvHandler> handlers;

    private GeometryFunction geometryFunction;

    @Before
    public void setup() {
        wkt = "POLYGON ((0 0, 5 0, 5 5, 0 5, 0 0))";
        geometryFunction = GeometryFunction.IDENTITY;
        locationKlvProcessor = new LocationKlvProcessor(geometryFunction);
        klvHandler = mock(GeoBoxHandler.class);
        Attribute attribute = mock(Attribute.class);
        when(attribute.getValues()).thenReturn(Collections.singletonList(wkt));
        when(klvHandler.asAttribute()).thenReturn(Optional.of(attribute));
        when(klvHandler.getAttributeName()).thenReturn(AttributeNameConstants.CORNER);
        metacard = new MetacardImpl(BasicTypes.BASIC_METACARD);
        klvConfiguration = new KlvProcessor.Configuration();
        handlers = Collections.singletonMap(AttributeNameConstants.CORNER, klvHandler);
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

    @Test
    public void testProcess() {

        klvConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT, 50);

        locationKlvProcessor.process(handlers, metacard, klvConfiguration);

        assertThat(metacard.getLocation(), is(wkt));

    }

    /**
     * Test where the subsample count is missing from the configuration.
     */
    @Test
    public void testMissingConfiguration() {

        locationKlvProcessor.process(handlers, metacard, klvConfiguration);

        assertThat(metacard.getLocation(), nullValue());

    }

    /**
     * Test a subsample count that is below the minimum required by LocationKlvProcessor.
     */
    @Test
    public void testBadConfiguration() {

        klvConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT,
                LocationKlvProcessor.MIN_SUBSAMPLE_COUNT - 1);

        locationKlvProcessor.process(handlers, metacard, klvConfiguration);

        assertThat(metacard.getLocation(), nullValue());

    }

    /**
     * This test iterates through a wide range of subsample inputs to make sure they all reduce to the
     * subsample target and that there are no rounding issues.
     */
    @Test
    public void testSubsample() {

        int subsampleCount = 50;

        String lat1 = "lat1";
        String lon1 = "lon1";
        String lat2 = "lat2";
        String lon2 = "lon2";
        String lat3 = "lat3";
        String lon3 = "lon3";
        String lat4 = "lat4";
        String lon4 = "lon4";

        int start = subsampleCount + 1;
        int end = subsampleCount * 10;

        when(klvHandler.getLatitude1()).thenReturn(lat1);
        when(klvHandler.getLongitude1()).thenReturn(lon1);
        when(klvHandler.getLatitude2()).thenReturn(lat2);
        when(klvHandler.getLongitude2()).thenReturn(lon2);
        when(klvHandler.getLatitude3()).thenReturn(lat3);
        when(klvHandler.getLongitude3()).thenReturn(lon3);
        when(klvHandler.getLatitude4()).thenReturn(lat4);
        when(klvHandler.getLongitude4()).thenReturn(lon4);

        for (int originalSize = start; originalSize < end; originalSize++) {

            Map<String, List<Double>> rawData = new HashMap<>();

            when(klvHandler.getRawGeoData()).thenReturn(rawData);

            rawData.put(lat1, new ArrayList<>());
            rawData.put(lon1, new ArrayList<>());
            rawData.put(lat2, new ArrayList<>());
            rawData.put(lon2, new ArrayList<>());
            rawData.put(lat3, new ArrayList<>());
            rawData.put(lon3, new ArrayList<>());
            rawData.put(lat4, new ArrayList<>());
            rawData.put(lon4, new ArrayList<>());

            for (int i = 0; i < originalSize; i++) {
                add(rawData, lat1, i);
                add(rawData, lon1, i);
                add(rawData, lat2, i);
                add(rawData, lon2, i);
                add(rawData, lat3, i);
                add(rawData, lon3, i);
                add(rawData, lat4, i);
                add(rawData, lon4, i);
            }

            GeoBoxHandler subsampledGeoBoxHandler = locationKlvProcessor.subsample(klvHandler,
                    subsampleCount);

            Map<String, List<Double>> newRawData = subsampledGeoBoxHandler.getRawGeoData();

            assertThatCount(newRawData, lat1, subsampleCount);
            assertThatCount(newRawData, lon1, subsampleCount);
            assertThatCount(newRawData, lat2, subsampleCount);
            assertThatCount(newRawData, lon2, subsampleCount);
            assertThatCount(newRawData, lat3, subsampleCount);
            assertThatCount(newRawData, lon3, subsampleCount);
            assertThatCount(newRawData, lat4, subsampleCount);
            assertThatCount(newRawData, lon4, subsampleCount);

        }
    }

    private void add(Map<String, List<Double>> rawData, String name, int value) {
        rawData.get(name)
                .add((double) value);
    }

    private void assertThatCount(Map<String, List<Double>> rawData, String name, int count) {
        assertThat(rawData.get(name), hasSize(count));
    }
}
