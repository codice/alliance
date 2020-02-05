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
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class ConvertSubpolygonsToEnvelopesTest {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  @Test
  public void testNullSubpolygon() throws ParseException {

    Geometry geometry = null;

    ConvertSubpolygonsToEnvelopes convertSubpolygonsToEnvelopes =
        new ConvertSubpolygonsToEnvelopes();

    Geometry actual = convertSubpolygonsToEnvelopes.apply(geometry, new GeometryOperator.Context());

    assertThat(actual, nullValue());
  }

  @Test
  public void testEmptySubpolygon() throws ParseException {

    Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(null);

    ConvertSubpolygonsToEnvelopes convertSubpolygonsToEnvelopes =
        new ConvertSubpolygonsToEnvelopes();

    Geometry actual = convertSubpolygonsToEnvelopes.apply(geometry, new GeometryOperator.Context());

    assertThat(actual.isEmpty(), is(true));
  }

  @Test
  public void testSingleSubpolygon() throws ParseException {

    String wkt = "POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))";

    WKTReader wktReader = new WKTReader();

    Geometry geometry = wktReader.read(wkt);

    ConvertSubpolygonsToEnvelopes convertSubpolygonsToEnvelopes =
        new ConvertSubpolygonsToEnvelopes();

    Geometry actual = convertSubpolygonsToEnvelopes.apply(geometry, new GeometryOperator.Context());

    assertThat(actual, is(geometry));
  }

  @Test
  public void testTwoSubpolygons() throws ParseException {

    String wkt =
        "MULTIPOLYGON (((0 0, 2 10, 10 20, 20 20, 20 0, 0 0)),((0 40, 2 50, 10 60, 20 60, 20 40, 0 40)))";

    WKTReader wktReader = new WKTReader();

    Geometry geometry = wktReader.read(wkt);

    ConvertSubpolygonsToEnvelopes convertSubpolygonsToEnvelopes =
        new ConvertSubpolygonsToEnvelopes();

    Geometry actual = convertSubpolygonsToEnvelopes.apply(geometry, new GeometryOperator.Context());

    Geometry expected =
        wktReader.read(
            "MULTIPOLYGON (((0 0, 0 20, 20 20, 20 0, 0 0)), ((0 40, 0 60, 20 60, 20 40, 0 40)))");

    assertThat(actual, is(expected));
  }
}
