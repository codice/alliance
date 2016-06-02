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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

public class TestGeometryFunctionList {

    private NormalizeGeometry childGeometryFunction;

    private GeometryFunctionList geometryFunctionList;

    @Before
    public void setup() {
        childGeometryFunction = mock(NormalizeGeometry.class);
        geometryFunctionList = new GeometryFunctionList(Collections.singletonList(
                childGeometryFunction));
    }

    @Test
    public void testAccept() {
        GeometryFunction.Visitor visitor = mock(GeometryFunction.Visitor.class);
        geometryFunctionList.accept(visitor);
        verify(childGeometryFunction).accept(visitor);
    }

    @Test
    public void testToString() {
        assertThat(geometryFunctionList.toString(), notNullValue());
    }

    @Test
    public void testApplyNullArg() {
        assertThat(geometryFunctionList.apply(null), nullValue());
    }

    @Test
    public void testApply() {
        Geometry geometry = mock(Geometry.class);
        Geometry newGeometry = mock(Geometry.class);
        when(childGeometryFunction.apply(geometry)).thenReturn(newGeometry);
        Geometry result = geometryFunctionList.apply(geometry);
        assertThat(result, is(newGeometry));
    }
}