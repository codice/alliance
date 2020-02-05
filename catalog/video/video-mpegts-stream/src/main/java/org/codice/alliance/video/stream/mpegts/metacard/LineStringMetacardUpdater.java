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
package org.codice.alliance.video.stream.mpegts.metacard;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.io.Serializable;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.codice.alliance.libs.klv.GeometryOperator;
import org.codice.alliance.libs.klv.GeometryUtility;
import org.codice.alliance.video.stream.mpegts.Context;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineStringMetacardUpdater implements MetacardUpdater {
  private static final Logger LOGGER = LoggerFactory.getLogger(LineStringMetacardUpdater.class);

  private final String attributeName;

  private final GeometryOperator geometryOperator;

  public LineStringMetacardUpdater(String attributeName) {
    this(attributeName, GeometryOperator.IDENTITY);
  }

  public LineStringMetacardUpdater(String attributeName, GeometryOperator geometryOperator) {
    this.attributeName = attributeName;
    this.geometryOperator = geometryOperator;
  }

  public GeometryOperator getGeometryOperator() {
    return geometryOperator;
  }

  @Override
  public void update(Metacard parent, Metacard child, Context context) {
    if (!hasFrameCenter(parent) && hasFrameCenter(child)) {
      setAttribute(parent, child);
    } else if (hasFrameCenter(parent) && hasFrameCenter(child)) {
      WKTReader wktReader = new WKTReader();

      Optional<Geometry> parentGeo = GeometryUtility.wktToGeometry(getValue(parent), wktReader);
      Optional<Geometry> childGeo = GeometryUtility.wktToGeometry(getValue(child), wktReader);

      if (parentGeo.isPresent() && childGeo.isPresent()) {
        Coordinate[] coordinates = getMergedCoordinates(parentGeo, childGeo);
        if (coordinates.length == 0) {
          LOGGER.trace(
              "Coordinates list was empty, unable to create linestring. parent='{}', child='{}'",
              parentGeo,
              childGeo);
          return;
        }
        LineString lineString = convertCoordinatesToLineString(coordinates);
        setAttribute(
            parent, geometryOperator.apply(lineString, context.getGeometryOperatorContext()));
      }
    }
  }

  @Override
  public String toString() {
    return "LineStringMetacardUpdater{"
        + "attributeName='"
        + attributeName
        + '\''
        + ", geometryOperator="
        + geometryOperator
        + '}';
  }

  private Attribute createAttribute(Serializable value) {
    return new AttributeImpl(attributeName, value);
  }

  private void setAttribute(Metacard parent, Metacard child) {
    parent.setAttribute(createAttribute(child.getAttribute(attributeName).getValue()));
  }

  private void setAttribute(Metacard parent, Geometry lineString) {
    WKTWriter wktWriter = new WKTWriter();
    parent.setAttribute(createAttribute(wktWriter.write(lineString)));
  }

  private Coordinate[] getMergedCoordinates(
      Optional<Geometry> parentGeo, Optional<Geometry> childGeo) {
    return ArrayUtils.addAll(parentGeo.get().getCoordinates(), childGeo.get().getCoordinates());
  }

  private String getValue(Metacard metacard) {
    return (String) metacard.getAttribute(attributeName).getValue();
  }

  private boolean hasFrameCenter(Metacard metacard) {
    return metacard.getAttribute(attributeName) != null
        && metacard.getAttribute(attributeName).getValue() instanceof String;
  }

  private LineString convertCoordinatesToLineString(Coordinate[] coordinates) {
    return new GeometryFactory().createLineString(coordinates);
  }
}
