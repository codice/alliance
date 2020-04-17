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

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.libs.klv.AttributeNameConstants;
import org.codice.alliance.libs.klv.GeometryOperator;
import org.codice.alliance.libs.klv.GeometryOperatorList;
import org.codice.alliance.libs.klv.GeometryUtility;
import org.codice.alliance.libs.klv.LinestringGeometrySubsampler;
import org.codice.alliance.video.stream.mpegts.Context;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the frame-center field based on the frame-center data in the children. The coordinates of
 * the child frame-center linestrings are concatenated into a single linestring and saved to the
 * parent.
 *
 * <p>This is not thread-safe.
 */
@NotThreadSafe
public class FrameCenterUpdateField extends UpdateParent.BaseUpdateField {
  private static final Logger LOGGER = LoggerFactory.getLogger(FrameCenterUpdateField.class);

  public static final int MAX_SIZE = 1000;

  private final GeometryOperator geometryOperator;

  private final GeometryFactory geometryFactory;

  private Geometry intermediateGeometry;

  /**
   * @param geometryOperator applied to the final linestring before it is saved to the parent
   * @param geometryFactory factory for creating geometry objects
   */
  public FrameCenterUpdateField(
      GeometryOperator geometryOperator, GeometryFactory geometryFactory) {
    this.geometryOperator =
        new GeometryOperatorList(
            Arrays.asList(geometryOperator, new LinestringGeometrySubsampler()));
    this.geometryFactory = geometryFactory;
  }

  @Override
  protected void doEnd(Metacard parent, Context context) {
    if (intermediateGeometry != null) {
      Integer originSubsampleCount = context.getGeometryOperatorContext().getSubsampleCount();
      try {
        context.getGeometryOperatorContext().setSubsampleCount(MAX_SIZE);
        setFrameCenter(
            parent,
            geometryOperator.apply(intermediateGeometry, context.getGeometryOperatorContext()));
      } finally {
        context.getGeometryOperatorContext().setSubsampleCount(originSubsampleCount);
      }
    }
  }

  @Override
  protected void doUpdateField(Metacard parent, List<Metacard> children, Context context) {
    WKTReader wktReader = new WKTReader();

    List<String> childLocations = extractChildFrameCenters(children);

    List<Geometry> geometries = new LinkedList<>();

    if (intermediateGeometry != null) {
      geometries.add(intermediateGeometry);
    }

    geometries.addAll(
        childLocations.stream()
            .map(s -> GeometryUtility.wktToGeometry(s, wktReader))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));

    List<Coordinate> coordinates =
        geometries.stream()
            .flatMap(geometry -> Stream.of(geometry.getCoordinates()))
            .collect(Collectors.toList());

    if (coordinates.isEmpty()) {
      LOGGER.trace("Coordinates are empty, unable to create linestring");
      return;
    }
    intermediateGeometry =
        geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
  }

  private void setFrameCenter(Metacard parentMetacard, Geometry geometry) {
    WKTWriter wktWriter = new WKTWriter();
    parentMetacard.setAttribute(
        new AttributeImpl(AttributeNameConstants.FRAME_CENTER, wktWriter.write(geometry)));
  }

  private List<String> extractChildFrameCenters(List<Metacard> children) {
    return children.stream()
        .map(metacard -> metacard.getAttribute(AttributeNameConstants.FRAME_CENTER))
        .filter(Objects::nonNull)
        .map(Attribute::getValue)
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toList());
  }
}
