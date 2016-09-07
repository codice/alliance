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
package org.codice.alliance.video.stream.mpegts.metacard;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.codice.alliance.libs.klv.GeometryOperator;
import org.junit.Test;

public class LocationMetacardUpdaterTest {

    @Test
    public void testDefaultCtor() {
        LocationMetacardUpdater locationMetacardUpdater = new LocationMetacardUpdater();
        assertThat(locationMetacardUpdater.getPostUnionGeometryOperator(),
                is(GeometryOperator.IDENTITY));
        assertThat(locationMetacardUpdater.getPreUnionGeometryOperator(),
                is(GeometryOperator.IDENTITY));
    }

    @Test
    public void testToString() {
        assertThat(new LocationMetacardUpdater().toString(), notNullValue());
    }

    @Test
    public void testGetGeometryFunction() {
        GeometryOperator preUnionGeometryOperator = mock(GeometryOperator.class);
        GeometryOperator postUnionGeometryOperator = mock(GeometryOperator.class);
        LocationMetacardUpdater updater = new LocationMetacardUpdater(preUnionGeometryOperator,
                postUnionGeometryOperator);
        assertThat(updater.getPreUnionGeometryOperator(), is(preUnionGeometryOperator));
        assertThat(updater.getPostUnionGeometryOperator(), is(postUnionGeometryOperator));
    }

    @Test
    public void testAccept() {
        MetacardUpdater.Visitor visitor = mock(MetacardUpdater.Visitor.class);
        LocationMetacardUpdater updater = new LocationMetacardUpdater();
        updater.accept(visitor);
        verify(visitor).visit(updater);
    }

}
