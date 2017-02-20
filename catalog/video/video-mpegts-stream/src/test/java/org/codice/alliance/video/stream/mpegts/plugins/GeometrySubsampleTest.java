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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometrySubsampleTest {

    @Test
    public void testBelowCount() throws ParseException {

        String wkt = generate(999);

        Geometry geometry = new WKTReader().read(wkt);

        GeometrySubsample geometrySubsample = new GeometrySubsample(1000);

        Geometry actual = geometrySubsample.apply(geometry);

        assertThat(actual.getCoordinates().length, is(999));

    }

    @Test
    public void testAboveCount() throws ParseException {

        String wkt = generate(1001);

        Geometry geometry = new WKTReader().read(wkt);

        GeometrySubsample geometrySubsample = new GeometrySubsample(1000);

        Geometry actual = geometrySubsample.apply(geometry);

        assertThat(actual.getCoordinates().length, is(1000));

    }


    private String generate(int count) {

        List<String> points = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            double value = ((double) i) / 100;
            points.add(String.format("%f %f", value, value));
        }

        return "LINESTRING (" + StringUtils.join(points, ",") + ")";
    }

}
