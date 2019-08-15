/*
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
package org.codice.alliance.ddms.wkt;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GeometryCollectionBuilderTest {

  @Test
  public void testPolygonRightTriangle() {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    geometryCollectionBuilder.polygon(0.0, 0.0, 3.0, 0.0, 3.0, 4.0);

    assertThat(
        geometryCollectionBuilder.toString(),
        is("GEOMETRYCOLLECTION (POLYGON ((0.0 0.0, 3.0 0.0, 3.0 4.0)))"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPolygonOddCoordinateCount() {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    geometryCollectionBuilder.polygon(1.0, 2.1, 3.0, 4.0, 5.0, 6.0, 7.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPolygonTooFewPoints() {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    geometryCollectionBuilder.polygon(1.0, 1.1, 2.0, 2.1, 3.0);
  }

  @Test
  public void testAddWkt() {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    geometryCollectionBuilder.addWkt("LINESTRING (30 10, 10 30, 40 40)");

    assertThat(
        geometryCollectionBuilder.toString(),
        is("GEOMETRYCOLLECTION (LINESTRING (30 10, 10 30, 40 40))"));
  }

  @Test
  public void testPolgonAndAddWkt() {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    geometryCollectionBuilder.polygon(0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0);
    geometryCollectionBuilder.addWkt("POINT (0 0)");

    assertThat(
        geometryCollectionBuilder.toString(),
        is("GEOMETRYCOLLECTION (POLYGON ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0)), POINT (0 0))"));
  }

  @Test
  public void testAddBlankWkt() {
    GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder();
    geometryCollectionBuilder.polygon(0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0);
    String wkt = geometryCollectionBuilder.toString();
    geometryCollectionBuilder.addWkt(null);
    geometryCollectionBuilder.addWkt("");

    assertThat(geometryCollectionBuilder.toString(), is(wkt));
  }
}
