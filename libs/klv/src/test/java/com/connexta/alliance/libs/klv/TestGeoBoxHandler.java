/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.libs.klv;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.junit.Before;
import org.junit.Test;

public class TestGeoBoxHandler {

    private static final String LAT1 = "lat1";

    private static final String LON1 = "lon1";

    private static final String LAT2 = "lat2";

    private static final String LON2 = "lon2";

    private static final String LAT3 = "lat3";

    private static final String LON3 = "lon3";

    private static final String LAT4 = "lat4";

    private static final String LON4 = "lon4";

    private GeoBoxHandler geoBoxHandler;

    @Before
    public void setup() {
        geoBoxHandler = new GeoBoxHandler("attributeName",
                LAT1,
                LON1,
                LAT2,
                LON2,
                LAT3,
                LON3,
                LAT4,
                LON4);
    }

    @Test
    public void testGetRawGeoData() {
        geoBoxHandler.accept(LAT1, 10.0);
        assertThat(geoBoxHandler.getRawGeoData()
                .get(LAT1)
                .get(0), closeTo(10.0, 0.01));
    }

    @Test
    public void testGetLatitude1() {
        assertThat(geoBoxHandler.getLatitude1(), is(LAT1));
    }

    @Test
    public void testGetLongitude1() {
        assertThat(geoBoxHandler.getLongitude1(), is(LON1));
    }

    @Test
    public void testGetLatitude2() {
        assertThat(geoBoxHandler.getLatitude2(), is(LAT2));
    }

    @Test
    public void testGetLongitude2() {
        assertThat(geoBoxHandler.getLongitude2(), is(LON2));
    }

    @Test
    public void testGetLatitude3() {
        assertThat(geoBoxHandler.getLatitude3(), is(LAT3));
    }

    @Test
    public void testGetLongitude3() {
        assertThat(geoBoxHandler.getLongitude3(), is(LON3));
    }

    @Test
    public void testGetLatitude4() {
        assertThat(geoBoxHandler.getLatitude4(), is(LAT4));
    }

    @Test
    public void testGetLongitude4() {
        assertThat(geoBoxHandler.getLongitude4(), is(LON4));
    }

    @Test
    public void testEmpty() {
        assertThat(geoBoxHandler.asAttribute()
                .isPresent(), is(false));
    }

    @Test
    public void testAcceptWrongType() {

        KlvInt klvInt = mock(KlvInt.class);

        geoBoxHandler.accept(klvInt);

        assertThat(geoBoxHandler.asAttribute()
                .isPresent(), is(false));

    }

    @Test
    public void testAcceptData() throws KlvDecodingException {

        geoBoxHandler.accept(KlvUtilities.createTestFloat(LAT1, 1));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LON1, 2));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LAT2, 3));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LON2, 4));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LAT3, 5));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LON3, 6));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LAT4, 7));
        geoBoxHandler.accept(KlvUtilities.createTestFloat(LON4, 8));

        assertThat(geoBoxHandler.asAttribute()
                        .get()
                        .getValue(),
                is("POLYGON ((2.000000 1.000000, 4.000000 3.000000, 6.000000 5.000000, 8.000000 7.000000, 2.000000 1.000000))"));

    }
}
