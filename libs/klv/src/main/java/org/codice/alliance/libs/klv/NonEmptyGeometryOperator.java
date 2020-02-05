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

import org.locationtech.jts.geom.Geometry;

/** If the geometry is empty, then return {@code null}. Otherwise, return the geometry. */
public class NonEmptyGeometryOperator implements GeometryOperator {
  @Override
  public Geometry apply(Geometry geometry, Context context) {
    return (geometry == null || geometry.isEmpty()) ? null : geometry;
  }
}
