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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

public class TestGeometryFunction {

    @Test
    public void testIdentity() {
        Geometry geometry = mock(Geometry.class);
        Geometry newGeometry = GeometryFunction.IDENTITY.apply(geometry);
        assertThat(geometry, is(newGeometry));
    }

    /**
     * Since the implementation for the accept method is an empty body, we just call and make sure
     * none of the visitor methods are called.
     */
    @Test
    public void testAccept() {
        GeometryFunction.Visitor visitor = mock(GeometryFunction.Visitor.class);
        GeometryFunction.IDENTITY.accept(visitor);
        verify(visitor, never()).visit(any(NormalizeGeometry.class));
        verify(visitor, never()).visit(any(SimplifyGeometryFunction.class));
    }

}
