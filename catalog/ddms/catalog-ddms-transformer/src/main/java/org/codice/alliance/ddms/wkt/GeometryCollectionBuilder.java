/*
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.ddms.wkt;

import org.apache.commons.lang.StringUtils;

public class GeometryCollectionBuilder {
  private final StringBuilder stringBuilder;
  private boolean notEmpty;

  public GeometryCollectionBuilder() {
    stringBuilder = new StringBuilder();
    stringBuilder.append("GEOMETRYCOLLECTION (");
  }

  public GeometryCollectionBuilder polygon(double... points) {
    if (points.length % 2 != 0) {
      throw new IllegalArgumentException("Must have even number of points");
    }

    if (points.length < 6) {
      throw new IllegalArgumentException("Must have at least 3 pairs of points");
    }

    if (notEmpty) {
      stringBuilder.append(", ");
    }

    stringBuilder.append("POLYGON ((");

    for (int i = 0; i < points.length - 1; i++) {
      stringBuilder.append(points[i]);

      if (i % 2 != 0) {
        stringBuilder.append(", ");
      } else {
        stringBuilder.append(" ");
      }
    }

    stringBuilder.append(points[points.length - 1]).append("))");
    notEmpty = true;
    return this;
  }

  public GeometryCollectionBuilder addWkt(String wkt) {
    if (StringUtils.isBlank(wkt)) {
      return this;
    }

    if (notEmpty) {
      stringBuilder.append(", ");
    }

    stringBuilder.append(wkt);
    notEmpty = true;
    return this;
  }

  @Override
  public String toString() {
    if (notEmpty) {
      return stringBuilder.toString() + ")";
    } else {
      return "";
    }
  }
}
