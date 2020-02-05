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

import java.util.stream.IntStream;
import javax.annotation.concurrent.ThreadSafe;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert the subpolygons in a geometry to envelopes. If the geometry only contains one geometry,
 * then return the original geometry.
 */
@ThreadSafe
public class ConvertSubpolygonsToEnvelopes implements GeometryOperator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSubpolygonsToEnvelopes.class);

  @Override
  public Geometry apply(Geometry geometry, Context context) {

    if (geometry == null || geometry.getNumGeometries() <= 1) {
      return geometry;
    }

    LOGGER.trace("Converting geometry: {}", geometry);

    Geometry envelopePolygons =
        IntStream.range(0, geometry.getNumGeometries())
            .mapToObj(geometry::getGeometryN)
            .map(Geometry::getEnvelope)
            .reduce(Geometry::union)
            .orElse(geometry);

    LOGGER.trace(
        "Converted geometry: {}\n isValid? {}", envelopePolygons, envelopePolygons.isValid());

    return envelopePolygons.isValid() ? envelopePolygons : geometry;
  }
}
