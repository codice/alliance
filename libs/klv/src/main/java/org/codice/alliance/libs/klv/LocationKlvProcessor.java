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

import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the location metadata based on the klv corner data. Callers must supply a {@link
 * Configuration} that contains a postive (&gt;0) Integer for {@link Configuration#SUBSAMPLE_COUNT}.
 */
public class LocationKlvProcessor implements KlvProcessor {

  public static final Integer MIN_SUBSAMPLE_COUNT = 1;

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationKlvProcessor.class);

  private final GeometryOperator postUnionGeometryOperator;

  private final GeometryOperator preUnionGeometryOperator;

  public LocationKlvProcessor() {
    this(GeometryOperator.IDENTITY, GeometryOperator.IDENTITY);
  }

  /**
   * @param preUnionGeometryOperator transform the final Geometry object (must be non-null)
   * @param postUnionGeometryOperator transform the final Geometry object (must be non-null)
   */
  public LocationKlvProcessor(
      GeometryOperator preUnionGeometryOperator, GeometryOperator postUnionGeometryOperator) {
    notNull(preUnionGeometryOperator, "preUnionGeometryOperator must be non-null");
    notNull(postUnionGeometryOperator, "postUnionGeometryOperator must be non-null");
    this.preUnionGeometryOperator = preUnionGeometryOperator;
    this.postUnionGeometryOperator = postUnionGeometryOperator;
  }

  public GeometryOperator getGeometryFunction() {
    return postUnionGeometryOperator;
  }

  @Override
  public void process(
      Map<String, KlvHandler> handlers, Metacard metacard, Configuration configuration) {

    if (configuration.get(Configuration.SUBSAMPLE_COUNT) == null
        || (Integer) configuration.get(Configuration.SUBSAMPLE_COUNT) < MIN_SUBSAMPLE_COUNT) {
      LOGGER.debug(
          "the subsample count configuration is missing or incorrectly configured (the minimum subsample count is {}), skipping location klv processing",
          MIN_SUBSAMPLE_COUNT);
      return;
    }

    Integer subsampleCount = (Integer) configuration.get(Configuration.SUBSAMPLE_COUNT);

    tryCornerData(handlers, metacard, subsampleCount, configuration.getGeometryOperatorContext());

    if (isLocationNotSet(metacard)) {
      tryFrameCenterData(
          handlers, metacard, subsampleCount, configuration.getGeometryOperatorContext());
    }
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  private <T extends KlvHandler> Optional<T> find(
      Map<String, KlvHandler> handlers, String name, Class<T> clazz) {
    return handlers.values().stream()
        .filter(Objects::nonNull)
        .filter(handler -> clazz.isAssignableFrom(handler.getClass()))
        .filter(handler -> handler.getAttributeName().equals(name))
        .map(clazz::cast)
        .findFirst();
  }

  private void tryFrameCenterData(
      Map<String, KlvHandler> handlers,
      Metacard metacard,
      Integer subsampleCount,
      GeometryOperator.Context geometryOperatorContext) {
    find(handlers, AttributeNameConstants.FRAME_CENTER, LatitudeLongitudeHandler.class)
        .ifPresent(
            frameCenterHandler ->
                frameCenterHandler
                    .asSubsampledHandler(subsampleCount)
                    .asAttribute()
                    .ifPresent(
                        attribute ->
                            setLocationFromFrameCenter(
                                metacard, attribute, geometryOperatorContext)));
  }

  private boolean isLocationNotSet(Metacard metacard) {
    return metacard.getAttribute(AttributeNameConstants.GEOGRAPHY) == null;
  }

  private void tryCornerData(
      Map<String, KlvHandler> handlers,
      Metacard metacard,
      Integer subsampleCount,
      GeometryOperator.Context geometryOperatorContext) {
    find(handlers, AttributeNameConstants.CORNER, GeoBoxHandler.class)
        .ifPresent(
            cornerHandler ->
                cornerHandler
                    .asSubsampledHandler(subsampleCount)
                    .asAttribute()
                    .ifPresent(
                        attribute ->
                            setLocationFromAttribute(
                                metacard, attribute, geometryOperatorContext)));
  }

  private void setLocationFromAttribute(
      Metacard metacard, Attribute attribute, GeometryOperator.Context geometryOperatorContext) {
    WKTReader wktReader = new WKTReader();
    WKTWriter wktWriter = new WKTWriter();

    GeometryUtility.createUnionOfGeometryAttribute(
            wktReader,
            wktWriter,
            attribute,
            postUnionGeometryOperator,
            preUnionGeometryOperator,
            geometryOperatorContext)
        .ifPresent(location -> setAttribute(metacard, location));
  }

  /**
   * Compose the pre and post geometry operators into a single operator when working with a line
   * string.
   */
  private void setLocationFromFrameCenter(
      Metacard metacard, Attribute attribute, GeometryOperator.Context geometryOperatorContext) {

    String wkt =
        GeometryUtility.attributeToLineString(
            attribute,
            new GeometryOperatorList(
                Arrays.asList(preUnionGeometryOperator, postUnionGeometryOperator)),
            geometryOperatorContext);

    if (wkt != null) {
      setAttribute(metacard, wkt);
    }
  }

  private void setAttribute(Metacard metacard, String wkt) {
    metacard.setAttribute(new AttributeImpl(AttributeNameConstants.GEOGRAPHY, wkt));
  }
}
