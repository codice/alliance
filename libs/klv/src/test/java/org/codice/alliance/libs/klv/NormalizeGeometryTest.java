/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

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
    assertThat(
        normalizeGeometry.apply(geometry, new GeometryOperator.Context()), is(normalizedGeometry));
  }

  @Test
  public void testToString() {
    assertThat(normalizeGeometry.toString(), notNullValue());
  }
}
