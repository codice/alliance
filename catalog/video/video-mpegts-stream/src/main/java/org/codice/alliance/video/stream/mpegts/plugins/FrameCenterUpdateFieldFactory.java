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
package org.codice.alliance.video.stream.mpegts.plugins;

import org.codice.alliance.libs.klv.GeometryOperator;
import org.locationtech.jts.geom.GeometryFactory;

/** Create a {@link FrameCenterUpdateField} object. */
public class FrameCenterUpdateFieldFactory implements UpdateParentFactory.Factory {

  private final GeometryOperator geometryOperator;

  private final GeometryFactory geometryFactory;

  /**
   * @param geometryOperator passed to the constructor {@link
   *     FrameCenterUpdateField#FrameCenterUpdateField(GeometryOperator, GeometryFactory)}
   * @param geometryFactory passed to the constructor {@link
   *     FrameCenterUpdateField#FrameCenterUpdateField(GeometryOperator, GeometryFactory)}
   */
  public FrameCenterUpdateFieldFactory(
      GeometryOperator geometryOperator, GeometryFactory geometryFactory) {
    this.geometryOperator = geometryOperator;
    this.geometryFactory = geometryFactory;
  }

  @Override
  public UpdateParent.UpdateField build() {
    return new FrameCenterUpdateField(geometryOperator, geometryFactory);
  }

  public GeometryOperator getGeometryOperator() {
    return geometryOperator;
  }
}
