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

import com.google.common.collect.ImmutableList;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses {@link Stanag4609TransportStreamParser#FRAME_CENTER_LATITUDE} and {@link
 * Stanag4609TransportStreamParser#FRAME_CENTER_LONGITUDE} to generate a WKT LINESTRING and store it
 * in the metacard attribute {@link AttributeNameConstants#FRAME_CENTER}. Callers must supply a
 * {@link Configuration} that contains a postive (&gt;0) Integer for {@link
 * Configuration#SUBSAMPLE_COUNT}.
 */
public class FrameCenterKlvProcessor implements KlvProcessor {

  public static final Integer MIN_SUBSAMPLE_COUNT = 1;

  private static final Logger LOGGER = LoggerFactory.getLogger(FrameCenterKlvProcessor.class);

  /** The name of the metacard attribute being set. */
  private static final String ATTRIBUTE_NAME = AttributeNameConstants.FRAME_CENTER;

  private static final ImmutableList<String> STANAG_FIELD_NAMES =
      ImmutableList.of(
          Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE,
          Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);

  private static final int STANAG_FIELD_NAME_SIZE = STANAG_FIELD_NAMES.size();

  private final GeometryOperator geometryOperator;

  public FrameCenterKlvProcessor() {
    this(GeometryOperator.IDENTITY);
  }

  /** @param geometryOperator transform the Geometry object (e.g. simplify) (must be non-null) */
  public FrameCenterKlvProcessor(GeometryOperator geometryOperator) {
    notNull(geometryOperator, "geometryOperator must be non-null");
    this.geometryOperator = geometryOperator;
  }

  public GeometryOperator getGeometryOperator() {
    return geometryOperator;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public final void process(
      Map<String, KlvHandler> handlers, Metacard metacard, Configuration configuration) {

    if (configuration.get(Configuration.SUBSAMPLE_COUNT) == null
        || (Integer) configuration.get(Configuration.SUBSAMPLE_COUNT) < MIN_SUBSAMPLE_COUNT) {
      LOGGER.debug(
          "the subsample count configuration is missing or incorrectly configured (the minimum subsample count is {}), skipping location klv processing",
          MIN_SUBSAMPLE_COUNT);
      return;
    }

    List<LatitudeLongitudeHandler> stanagHandlers = findKlvHandlers(handlers);

    if (areAllHandlersFound(stanagHandlers)) {
      callFirstHandler(metacard, stanagHandlers, configuration);
    }
  }

  private void doProcess(
      Attribute attribute, Metacard metacard, GeometryOperator.Context geometryOperatorContext) {

    String wkt =
        GeometryUtility.attributeToLineString(attribute, geometryOperator, geometryOperatorContext);

    if (wkt != null) {
      setAttribute(metacard, wkt);
    }
  }

  private void setAttribute(Metacard metacard, String wkt) {
    metacard.setAttribute(new AttributeImpl(ATTRIBUTE_NAME, wkt));
  }

  private void callFirstHandler(
      Metacard metacard,
      List<LatitudeLongitudeHandler> stanagHandlers,
      Configuration configuration) {

    Integer subsampleCount = (Integer) configuration.get(Configuration.SUBSAMPLE_COUNT);

    stanagHandlers.stream()
        .findFirst()
        .ifPresent(
            handler ->
                handler
                    .asSubsampledHandler(subsampleCount)
                    .asAttribute()
                    .ifPresent(
                        attribute ->
                            doProcess(
                                attribute, metacard, configuration.getGeometryOperatorContext())));
  }

  /** All handlers are found if the number of handlers is the same as the number of field names. */
  private boolean areAllHandlersFound(List<LatitudeLongitudeHandler> stanagHandlers) {
    return stanagHandlers.size() == STANAG_FIELD_NAME_SIZE;
  }

  private List<LatitudeLongitudeHandler> findKlvHandlers(Map<String, KlvHandler> handlers) {
    return handlers.entrySet().stream()
        .filter(
            stringKlvHandlerEntry ->
                stringKlvHandlerEntry.getValue() instanceof LatitudeLongitudeHandler)
        .filter(entry -> STANAG_FIELD_NAMES.contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .map(LatitudeLongitudeHandler.class::cast)
        .collect(Collectors.toList());
  }
}
