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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class WKTUtil {
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  public static Geometry getWKTBoundingRectangle(String wktString) throws ParseException {
    Geometry boundingGeo = null;
    WKTReader wktReader = new WKTReader(GEOMETRY_FACTORY);
    Geometry geo = wktReader.read(wktString);

    if (geo instanceof Point) {
      Point pt = (Point) geo;
      Coordinate[] coordinates = new Coordinate[5];
      Coordinate ptCoord = pt.getCoordinate();
      Coordinate lowerLeftCoord = ptCoord;
      Coordinate upperLeftCoord = ptCoord;
      Coordinate upperRightCoord = ptCoord;
      Coordinate lowerRightCoord = ptCoord;

      coordinates[0] = lowerLeftCoord;
      coordinates[1] = upperLeftCoord;
      coordinates[2] = upperRightCoord;
      coordinates[3] = lowerRightCoord;
      coordinates[4] = lowerLeftCoord;

      LinearRing shell = GEOMETRY_FACTORY.createLinearRing(coordinates);
      boundingGeo = new Polygon(shell, null, GEOMETRY_FACTORY);
    } else {
      boundingGeo = geo.getEnvelope();
    }

    return boundingGeo;
  }
}
