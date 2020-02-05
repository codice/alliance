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

import java.util.function.BiFunction;
import org.locationtech.jts.geom.Geometry;

public interface GeometryOperator extends BiFunction<Geometry, GeometryOperator.Context, Geometry> {

  GeometryOperator IDENTITY = (geometry, context) -> geometry;

  /**
   * Some GeometryOperator implementations may need additional configuration values. Those values
   * should be stored in this object.
   */
  class Context {

    private Double distanceTolerance;

    private Integer subsampleCount;

    public Integer getSubsampleCount() {
      return subsampleCount;
    }

    public void setSubsampleCount(Integer subsampleCount) {
      this.subsampleCount = subsampleCount;
    }

    public Double getDistanceTolerance() {
      return distanceTolerance;
    }

    public void setDistanceTolerance(Double distanceTolerance) {
      this.distanceTolerance = distanceTolerance;
    }
  }
}
