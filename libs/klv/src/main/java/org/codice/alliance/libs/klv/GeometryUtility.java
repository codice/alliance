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

import ddf.catalog.data.Attribute;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeometryUtility.class);

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  /**
   * Create the union of multi-valued attribute that contains WKT. If the union cannot be computed,
   * then this method returns {@link Optional#empty()}
   *
   * @param wktReader non-null
   * @param wktWriter non-null
   * @param attribute non-null
   * @return optional wkt string
   */
  public static Optional<String> createUnionOfGeometryAttribute(
      WKTReader wktReader,
      WKTWriter wktWriter,
      Attribute attribute,
      GeometryOperator.Context geometryOperatorContext) {
    return createUnionOfGeometryAttribute(
        wktReader,
        wktWriter,
        attribute,
        GeometryOperator.IDENTITY,
        GeometryOperator.IDENTITY,
        geometryOperatorContext);
  }

  /**
   * Create the union of multi-valued attribute that contains WKT. If the union cannot be computed,
   * then this method returns {@link Optional#empty()}
   *
   * @param wktReader non-null
   * @param wktWriter non-null
   * @param attribute non-null
   * @param postUnionGeometryOperator non-null, transform the geometry (e.g. simplify or normalize)
   * @param preUnionGeometryOperator non-null, transform the geometry just before the union
   *     operation (e.g. reduce precision)
   * @return optional wkt string
   */
  public static Optional<String> createUnionOfGeometryAttribute(
      WKTReader wktReader,
      WKTWriter wktWriter,
      Attribute attribute,
      BiFunction<Geometry, GeometryOperator.Context, Geometry> postUnionGeometryOperator,
      BiFunction<Geometry, GeometryOperator.Context, Geometry> preUnionGeometryOperator,
      GeometryOperator.Context geometryOperatorContext) {
    return attribute.getValues().stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(wkt -> wktToGeometry(wkt, wktReader))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(geometry -> preUnionGeometryOperator.apply(geometry, geometryOperatorContext))
        .reduce(Geometry::union)
        .map(geometry -> postUnionGeometryOperator.apply(geometry, geometryOperatorContext))
        .map(geo -> !geo.isValid() ? geo.convexHull() : geo)
        .filter(Geometry::isValid)
        .map(wktWriter::write);
  }

  public static Optional<Geometry> wktToGeometry(String wkt, WKTReader wktReader) {
    try {
      return Optional.of(wktReader.read(wkt));
    } catch (ParseException e) {
      LOGGER.debug("unable to convert WKT to a Geometry object: wkt={}", wkt, e);
    }
    return Optional.empty();
  }

  /**
   * Convert an attribute that contains a list of WKT Points into a WKT. If the attribute does not
   * contain a list of WKT Points, then the results are undefined. If the attribute contains more
   * than one valid WKT Point, then this method will return a WKT LineString. If the attribute
   * contains one valid WKT Point, then this method will return a WKT Point. Otherwise, it will
   * return "LINESTRING EMPTY".
   *
   * @param attribute expected to contain a list of strings, which follow the WKT Point format
   * @param geometryOperator applied to final geometry before being converted to WKT string
   * @return a WKT LineString or Point
   */
  public static String attributeToLineString(
      Attribute attribute,
      GeometryOperator geometryOperator,
      GeometryOperator.Context geometryOperatorContext) {
    List<String> points = getAttributeStrings(attribute);

    Coordinate[] coordinates = listToArray(convertWktToCoordinates(points));

    Geometry geometry = convertCoordinatesToGeometry(coordinates);

    if (geometry == null) {
      return null;
    }

    geometry = geometryOperator.apply(geometry, geometryOperatorContext);

    if (geometry == null) {
      return null;
    }

    return convertGeometryToWkt(geometry);
  }

  private static List<String> getAttributeStrings(Attribute attribute) {
    return attribute.getValues().stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.toList());
  }

  private static List<Coordinate> convertWktToCoordinates(List<String> points) {
    WKTReader wktReader = new WKTReader();
    return points.stream()
        .map(wkt -> GeometryUtility.wktToGeometry(wkt, wktReader))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Geometry::getCoordinate)
        .collect(Collectors.toList());
  }

  private static Coordinate[] listToArray(List<Coordinate> coordinateList) {
    return coordinateList.toArray(new Coordinate[coordinateList.size()]);
  }

  private static Geometry convertCoordinatesToGeometry(Coordinate[] coordinates) {
    if (coordinates.length == 0) {
      return null;
    } else if (coordinates.length == 1) {
      return GEOMETRY_FACTORY.createPoint(coordinates[0]);
    } else {
      return GEOMETRY_FACTORY.createLineString(coordinates);
    }
  }

  private static String convertGeometryToWkt(Geometry geometry) {
    WKTWriter wktWriter = new WKTWriter();
    return wktWriter.write(geometry);
  }
}
