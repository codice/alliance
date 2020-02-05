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
package org.codice.alliance.nsili.common;

import org.codice.alliance.nsili.common.UCO.Coordinate2d;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class NsiliGeomUtil {
  public static Rectangle getRectangle(Geometry boundingGeo) {
    Rectangle boundingRect = null;

    if (boundingGeo.getCoordinates().length == 5) {

      // JTS bounding GEO is in the form of a Polygon whose points are (minx, miny), (maxx, miny),
      // (maxx, maxy), (minx, maxy), (minx, miny).
      Coordinate upperLeft = boundingGeo.getCoordinates()[3];
      Coordinate lowerRight = boundingGeo.getCoordinates()[1];
      boundingRect = new Rectangle();
      boundingRect.lower_right = new Coordinate2d(lowerRight.x, lowerRight.y);
      boundingRect.upper_left = new Coordinate2d(upperLeft.x, upperLeft.y);
    }

    return boundingRect;
  }
}
