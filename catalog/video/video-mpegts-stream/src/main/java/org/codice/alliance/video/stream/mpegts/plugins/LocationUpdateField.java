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

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.libs.klv.GeometryOperator;
import org.codice.alliance.libs.klv.GeometryUtility;
import org.codice.alliance.video.stream.mpegts.Context;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

/** Update the parent metacard location field with the union of each child location field. */
@NotThreadSafe
public class LocationUpdateField extends UpdateParent.BaseUpdateField {

  private final GeometryOperator preUnionGeometryOperator;

  private final GeometryOperator postUnionGeometryOperator;

  private Geometry intermediateGeometry;

  /**
   * @param preUnionGeometryOperator applied to each child location before the union
   * @param postUnionGeometryOperator applied to the location just before being saved to the parent
   */
  public LocationUpdateField(
      GeometryOperator preUnionGeometryOperator, GeometryOperator postUnionGeometryOperator) {
    this.preUnionGeometryOperator = preUnionGeometryOperator;
    this.postUnionGeometryOperator = postUnionGeometryOperator;
  }

  @Override
  protected void doEnd(Metacard parent, Context context) {
    if (intermediateGeometry != null) {
      setLocation(
          parent,
          postUnionGeometryOperator.apply(
              intermediateGeometry, context.getGeometryOperatorContext()));
    }
  }

  @Override
  protected void doUpdateField(Metacard parent, List<Metacard> children, Context context) {

    WKTReader wktReader = new WKTReader();

    List<String> childLocations = extractChildLocations(children);

    List<Geometry> geometries =
        childLocations
            .stream()
            .map(s -> GeometryUtility.wktToGeometry(s, wktReader))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(
                geometry ->
                    preUnionGeometryOperator.apply(geometry, context.getGeometryOperatorContext()))
            .collect(Collectors.toList());

    if (intermediateGeometry != null) {
      geometries.add(intermediateGeometry);
    }

    geometries
        .stream()
        .reduce(Geometry::union)
        .ifPresent(geometry -> intermediateGeometry = geometry);
  }

  private List<String> extractChildLocations(List<Metacard> children) {
    return children
        .stream()
        .map(Metacard::getLocation)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toList());
  }

  private void setLocation(Metacard parentMetacard, Geometry geometry) {
    WKTWriter wktWriter = new WKTWriter();
    parentMetacard.setAttribute(new AttributeImpl(Core.LOCATION, wktWriter.write(geometry)));
  }

  /** Only used for testing. */
  GeometryOperator getPreUnionGeometryOperator() {
    return preUnionGeometryOperator;
  }

  /** Only used for testing. */
  GeometryOperator getPostUnionGeometryOperator() {
    return postUnionGeometryOperator;
  }
}
