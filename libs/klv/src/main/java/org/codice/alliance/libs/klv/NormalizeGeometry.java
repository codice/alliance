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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThreadSafe
public class NormalizeGeometry implements GeometryOperator {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizeGeometry.class);

  @Override
  public Geometry apply(Geometry geometry, Context context) {
    LOGGER.debug("normalizing geometry object");
    return geometry == null ? null : geometry.norm();
  }

  @Override
  public String toString() {
    return "NormalizeGeometry{}";
  }
}
