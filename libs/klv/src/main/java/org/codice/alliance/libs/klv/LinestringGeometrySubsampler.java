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

import javax.annotation.concurrent.ThreadSafe;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * This is meant for subsample LINESTRING geometries. It will ignore non-LINESTRING geometries. The
 * method {@link Context#getSubsampleCount()} must return a non-null value.
 */
@ThreadSafe
public class LinestringGeometrySubsampler implements GeometryOperator {

  @Override
  public Geometry apply(Geometry geometry, Context context) {

    if (!(geometry instanceof LineString)) {
      return geometry;
    }

    if (context.getSubsampleCount() == null) {
      throw new IllegalStateException("subsampleCount must be set in the GeometryOperator.Context");
    }

    int subsampleCount = context.getSubsampleCount();

    Coordinate[] input = geometry.getCoordinates();

    int inputSize = input.length;

    if (input.length <= subsampleCount) {
      return geometry;
    }

    Coordinate[] output = new Coordinate[subsampleCount];

    for (int i = 0; i < subsampleCount; i++) {
      output[i] = input[i * inputSize / subsampleCount];
    }

    return new GeometryFactory().createLineString(output);
  }
}
