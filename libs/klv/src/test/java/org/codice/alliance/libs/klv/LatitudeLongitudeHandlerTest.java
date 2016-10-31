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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.junit.Before;
import org.junit.Test;

public class LatitudeLongitudeHandlerTest {

    private static final String LAT = "lat";

    private static final String LON = "lon";

    private static final double EPSILON = 0.01;

    private LatitudeLongitudeHandler klvHandler;

    @Before
    public void setup() {
        klvHandler = new LatitudeLongitudeHandler("field", LAT, LON);
    }

    @Test
    public void testGetLongitudeFieldName() {
        assertThat(klvHandler.getLongitudeFieldName(), is(LON));
    }

    @Test
    public void testGetLatitudeFieldName() {
        assertThat(klvHandler.getLatitudeFieldName(), is(LAT));
    }

    @Test
    public void testGetRawGeoData() throws KlvDecodingException {
        klvHandler.accept(KlvUtilities.createTestFloat(LAT, 10.0));
        assertThat(klvHandler.getRawGeoData()
                .get(LAT)
                .get(0), closeTo(10.0, EPSILON));
    }

    @Test
    public void testEmpty() {
        assertThat(klvHandler.asAttribute()
                .isPresent(), is(false));
    }

    @Test
    public void testAcceptingData() throws KlvDecodingException {

        double expectedLatitude = 33;
        double expectedLongitude = -112;

        klvHandler.accept(KlvUtilities.createTestFloat(LAT, expectedLatitude));
        klvHandler.accept(KlvUtilities.createTestFloat(LON, expectedLongitude));

        String r = (String) klvHandler.asAttribute()
                .get()
                .getValue();

        Pattern p = Pattern.compile("POINT \\(([^ ]+) ([^)]+)\\)");

        Matcher m = p.matcher(r);

        assertThat(m.matches(), is(true));
        assertThat(m.groupCount(), is(2));

        assertThat(Double.parseDouble(m.group(1)), is(closeTo(expectedLongitude, EPSILON)));
        assertThat(Double.parseDouble(m.group(2)), is(closeTo(expectedLatitude, EPSILON)));

    }

    @Test
    public void testAcceptWrongType() {

        KlvInt klvInt = mock(KlvInt.class);

        klvHandler.accept(klvInt);

        assertThat(klvHandler.asAttribute()
                .isPresent(), is(false));

    }

    @Test
    public void testTrim() throws KlvDecodingException {
        double lat = 1;
        double lon = 2;

        klvHandler.accept(KlvUtilities.createTestFloat(LAT, lat));
        klvHandler.accept(KlvUtilities.createTestFloat(LON, lon));

        // this is the value that should get trimmed
        klvHandler.accept(KlvUtilities.createTestFloat(LAT, 3));

        klvHandler.trim();

        Map<String, List<Double>> data = klvHandler.getRawGeoData();

        assertThat(data.get(LAT), hasSize(1));
        assertThat(data.get(LON), hasSize(1));

        assertThat(data.get(LAT)
                .get(0), is(closeTo(lat, EPSILON)));
        assertThat(data.get(LON)
                .get(0), is(closeTo(lon, EPSILON)));
    }

    @Test
    public void testLatLonSubsample() throws KlvDecodingException {

        int subsampleCount = 50;

        String lat = "lat";
        String lon = "lon";

        int start = subsampleCount + 1;
        int end = subsampleCount * 10;

        for (int originalSize = start; originalSize < end; originalSize++) {

            for (int i = 0; i < originalSize; i++) {
                klvHandler.accept(KlvUtilities.createTestFloat(LAT, i));
                klvHandler.accept(KlvUtilities.createTestFloat(LON, i));
            }

            LatitudeLongitudeHandler reducedLatLonHandler = klvHandler.asSubsampledHandler(
                    subsampleCount);

            Map<String, List<Double>> reducedRawData = reducedLatLonHandler.getRawGeoData();

            assertThatCount(reducedRawData, lat, subsampleCount);
            assertThatCount(reducedRawData, lon, subsampleCount);

        }
    }

    /**
     * Test the condition where the subsample count is greater than the number of data points
     * in the handler.
     */
    @Test
    public void testLatLonSubsampleUnderflow() throws KlvDecodingException {

        int subsampleCount = 50;

        String lat = "lat";
        String lon = "lon";

        int count = 10;

        for (int i = 0; i < count; i++) {
            klvHandler.accept(KlvUtilities.createTestFloat(LAT, i));
            klvHandler.accept(KlvUtilities.createTestFloat(LON, i));
        }

        LatitudeLongitudeHandler reducedLatLonHandler = klvHandler.asSubsampledHandler(
                subsampleCount);

        Map<String, List<Double>> reducedRawData = reducedLatLonHandler.getRawGeoData();

        assertThatCount(reducedRawData, lat, count);
        assertThatCount(reducedRawData, lon, count);

    }

    private void assertThatCount(Map<String, List<Double>> rawData, String name, int count) {
        assertThat(rawData.get(name), hasSize(count));
    }

}
