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

public class TestGeometryOperatorList {

    private NormalizeGeometry childGeometryFunction;

    private GeometryOperatorList geometryOperatorList;

    @Before
    public void setup() {
        childGeometryFunction = mock(NormalizeGeometry.class);
        geometryOperatorList = new GeometryOperatorList(Collections.singletonList(
                childGeometryFunction));
    }

    @Test
    public void testAccept() {
        GeometryOperator.Visitor visitor = mock(GeometryOperator.Visitor.class);
        geometryOperatorList.accept(visitor);
        verify(childGeometryFunction).accept(visitor);
    }

    @Test
    public void testToString() {
        assertThat(geometryOperatorList.toString(), notNullValue());
    }

    @Test
    public void testApplyNullArg() {
        assertThat(geometryOperatorList.apply(null), nullValue());
    }

    @Test
    public void testApply() {
        Geometry geometry = mock(Geometry.class);
        Geometry newGeometry = mock(Geometry.class);
        when(childGeometryFunction.apply(geometry)).thenReturn(newGeometry);
        Geometry result = geometryOperatorList.apply(geometry);
        assertThat(result, is(newGeometry));
    }
}