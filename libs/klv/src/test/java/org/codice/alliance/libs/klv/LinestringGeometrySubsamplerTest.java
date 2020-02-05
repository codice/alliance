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
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class LinestringGeometrySubsamplerTest {

  @Test
  public void testBelowCount() throws ParseException {

    String wkt = generate(999);

    Geometry geometry = new WKTReader().read(wkt);

    LinestringGeometrySubsampler linestringGeometrySubsampler = new LinestringGeometrySubsampler();

    GeometryOperator.Context context = new GeometryOperator.Context();
    context.setSubsampleCount(1000);

    Geometry actual = linestringGeometrySubsampler.apply(geometry, context);

    assertThat(actual.getCoordinates().length, is(999));
  }

  @Test
  public void testAboveCount() throws ParseException {

    String wkt = generate(1001);

    Geometry geometry = new WKTReader().read(wkt);

    LinestringGeometrySubsampler linestringGeometrySubsampler = new LinestringGeometrySubsampler();

    GeometryOperator.Context context = new GeometryOperator.Context();
    context.setSubsampleCount(1000);

    Geometry actual = linestringGeometrySubsampler.apply(geometry, context);

    assertThat(actual.getCoordinates().length, is(1000));
  }

  @Test
  public void testNonLineString() throws ParseException {

    String wkt = "POLYGON (( 0 0, 1 1, 2 2, 0 0))";

    Geometry geometry = new WKTReader().read(wkt);

    LinestringGeometrySubsampler linestringGeometrySubsampler = new LinestringGeometrySubsampler();

    GeometryOperator.Context context = new GeometryOperator.Context();
    context.setSubsampleCount(1000);

    Geometry actual = linestringGeometrySubsampler.apply(geometry, context);

    assertThat(actual, is(geometry));
  }

  @Test(expected = IllegalStateException.class)
  public void testNullSubsampleCount() throws ParseException {

    String wkt = generate(1001);

    Geometry geometry = new WKTReader().read(wkt);

    LinestringGeometrySubsampler linestringGeometrySubsampler = new LinestringGeometrySubsampler();

    GeometryOperator.Context context = new GeometryOperator.Context();
    context.setSubsampleCount(null);

    linestringGeometrySubsampler.apply(geometry, context);
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
