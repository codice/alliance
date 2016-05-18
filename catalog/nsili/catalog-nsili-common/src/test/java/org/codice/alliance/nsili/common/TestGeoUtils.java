/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class TestGeoUtils {

    /* Test data based on https://en.wikipedia.org/wiki/Longitude#Length_of_a_degree_of_longitude */

    @Test
    public void testZeroLatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(0);
        assertThat(latDist, is(110574L));
    }

    @Test
    public void testZeroLatitudeLonDist() {
        long lonDist = GeoUtils.getLongLengthAtLatitude(0);
        assertThat(lonDist, is(111319L));
    }

    @Test
    public void test15LatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(15);
        assertThat(latDist, is(110649L));
    }

    @Test
    public void test15LatitudeLonDist() {
        long lonDist = GeoUtils.getLongLengthAtLatitude(15);
        assertThat(lonDist, is(107550L));
    }

    @Test
    public void test30LatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(30);
        assertThat(latDist, is(110852L));
    }

    @Test
    public void test30LatitudeLonDist() {
        long lonDist = GeoUtils.getLongLengthAtLatitude(30);
        assertThat(lonDist, is(96486L));
    }

    @Test
    public void test45LatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(45);
        assertThat(latDist, is(111132L));
    }

    @Test
    public void test45LatitudeLonDist() {
        long lonDist = GeoUtils.getLongLengthAtLatitude(45);
        assertThat(lonDist, is(78847L));
    }

    @Test
    public void test60LatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(60);
        assertThat(latDist, is(111412L));
    }

    @Test
    public void test60LatitudeLonDist() {
        long lonDist = GeoUtils.getLongLengthAtLatitude(60);
        assertThat(lonDist, is(55800L));
    }

    @Test
    public void test75LatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(75);
        assertThat(latDist, is(111618L));
    }

    @Test
    public void test75LatitudeLonDist() {
        long lonDist = GeoUtils.getLongLengthAtLatitude(75);
        assertThat(lonDist, is(28902L));
    }

    @Test
    public void test90LatitudeLatDist() {
        long latDist = GeoUtils.getLatLengthAtLatitude(90);
        assertThat(latDist, is(111694L));
    }

    @Test
    public void test90LatitudeLonDist() {
        long latDist = GeoUtils.getLongLengthAtLatitude(90);
        assertThat(latDist, is(0L));
    }
}
