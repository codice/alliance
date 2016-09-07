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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class NormalizeGeometryTest {

    private NormalizeGeometry normalizeGeometry;

    @Before
    public void setup() {
        normalizeGeometry = new NormalizeGeometry();
    }

    @Test
    public void testApply() throws ParseException {
        Geometry geometry = new WKTReader().read("LINESTRING( 0 0, 1 1, 2 2)");
        Geometry normalizedGeometry = geometry.norm();
        assertThat(normalizeGeometry.apply(geometry), is(normalizedGeometry));
    }

    @Test
    public void testToString() {
        assertThat(normalizeGeometry.toString(), notNullValue());
    }

    @Test
    public void testAccept() {
        GeometryOperator.Visitor visitor = mock(GeometryOperator.Visitor.class);
        normalizeGeometry.accept(visitor);
        verify(visitor).visit(normalizeGeometry);
    }
}
