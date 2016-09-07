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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.AttributeImpl;

public class GeometryUtilityTest {

    private static final String FIELD = "field";

    private WKTReader wktReader;

    private WKTWriter wktWriter;

    @Before
    public void setup() {
        wktReader = new WKTReader();
        wktWriter = new WKTWriter();
    }

    @Test
    public void testBasicUnion() throws ParseException {

        Attribute attribute = new AttributeImpl(FIELD,
                Arrays.asList("POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0))",
                        "POLYGON (( 5 5, 15 5, 15 15, 5 15, 5 5))"));

        Optional<String> optionalWkt = GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute);

        Geometry actual = wktReader.read(optionalWkt.get())
                .norm();

        Geometry expected = wktReader.read(
                "POLYGON (( 0 0, 10 0, 10 5, 15 5, 15 15, 5 15, 5 10, 0 10, 0 0 ))")
                .norm();

        assertThat(actual, is(expected));

    }

    @Test
    public void testEmptyData() {

        Attribute attribute = new AttributeImpl(FIELD, Collections.emptyList());

        Optional<String> optionalWkt = GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute);

        assertThat(optionalWkt.isPresent(), is(false));

    }

    @Test
    public void testBadData() {
        Attribute attribute = new AttributeImpl(FIELD,
                Collections.singletonList("POLYGON (( x 0, 10 0, 10 10, 0 10, 0 0))"));

        Optional<String> optionalWkt = GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute);

        assertThat(optionalWkt.isPresent(), is(false));

    }

    @Test
    public void testGoodDataFollowedByBadData() throws ParseException {

        Attribute attribute = new AttributeImpl(FIELD,
                Arrays.asList("POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0))",
                        "POLYGON (( x 5, 15 5, 15 15, 5 15, 5 5))"));

        Optional<String> optionalWkt = GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute);

        Geometry actual = wktReader.read(optionalWkt.get())
                .norm();

        Geometry expected = wktReader.read("POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0))")
                .norm();

        assertThat(actual, is(expected));

    }

    @Test
    public void testBadDataFollowedByGoodData() throws ParseException {

        Attribute attribute = new AttributeImpl(FIELD,
                Arrays.asList("POLYGON (( x 0, 10 0, 10 10, 0 10, 0 0))",
                        "POLYGON (( 5 5, 15 5, 15 15, 5 15, 5 5))"));

        Optional<String> optionalWkt = GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute);

        Geometry actual = wktReader.read(optionalWkt.get())
                .norm();

        Geometry expected = wktReader.read("POLYGON (( 5 5, 15 5, 15 15, 5 15, 5 5))")
                .norm();

        assertThat(actual, is(expected));

    }

    @Test
    public void testBadDataFollowedByBadData() throws ParseException {

        Attribute attribute = new AttributeImpl(FIELD,
                Arrays.asList("POLYGON (( x 0, 10 0, 10 10, 0 10, 0 0))",
                        "POLYGON (( x 5, 15 5, 15 15, 5 15, 5 5))"));

        Optional<String> optionalWkt = GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute);

        assertThat(optionalWkt.isPresent(), is(false));

    }

}
