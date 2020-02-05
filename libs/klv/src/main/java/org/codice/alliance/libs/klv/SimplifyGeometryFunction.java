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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The method {@link Context#getDistanceTolerance()} should return a non-null value if the
 * simplification should include a distance tolerance. See {@link
 * TopologyPreservingSimplifier#simplify(Geometry, double)}. If a distance tolerance is not set,
 * then {@link TopologyPreservingSimplifier#TopologyPreservingSimplifier(Geometry)} will be used.
 */
@ThreadSafe
public class SimplifyGeometryFunction implements GeometryOperator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyGeometryFunction.class);

  @Override
  public Geometry apply(Geometry geometry, Context context) {

    if (geometry == null || geometry.isEmpty()) {
      return geometry;
    }

    LOGGER.trace(
        "simplifying geometry: {}\n {} coordinates, isValid? {}",
        geometry,
        geometry.getCoordinates().length,
        geometry.isValid());

    Double distanceTolerance = context.getDistanceTolerance();

    Geometry simplifiedGeometry;
    if (distanceTolerance != null) {
      simplifiedGeometry = TopologyPreservingSimplifier.simplify(geometry, distanceTolerance);
    } else {
      simplifiedGeometry = new TopologyPreservingSimplifier(geometry).getResultGeometry();
    }

    LOGGER.trace(
        "simplified geometry: {}\n {} coordinates, isValid? {}",
        simplifiedGeometry,
        simplifiedGeometry.getCoordinates().length,
        simplifiedGeometry.isValid());

    return simplifiedGeometry.isValid() ? simplifiedGeometry : geometry;
  }

  @Override
  public String toString() {
    return "SimplifyGeometryFunction{}";
  }
}
