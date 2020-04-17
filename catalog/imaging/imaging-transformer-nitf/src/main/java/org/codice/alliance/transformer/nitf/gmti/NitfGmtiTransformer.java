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
package org.codice.alliance.transformer.nitf.gmti;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.transformer.nitf.common.SegmentHandler;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NitfGmtiTransformer extends SegmentHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(NitfGmtiTransformer.class);

  // Handles locations that have either 6 or 7 decimal places
  private static final String LOCATION_REGEX =
      "([+\\-]\\d{2}+\\.\\d{6,7}+)([+\\-]\\d{3}+\\.\\d{6,7})";

  private static final Pattern LOCATION_PATTERN = Pattern.compile(LOCATION_REGEX);

  private GeometryFactory geometryFactory;

  public Metacard transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard) {

    if (nitfSegmentsFlow == null) {
      throw new IllegalArgumentException("argument 'nitfSegmentsFlow' may not be null.");
    }

    if (metacard == null) {
      throw new IllegalArgumentException("argument 'metacard' may not be null.");
    }

    transformTargetLocation(metacard);
    transformAircraftLocation(metacard);

    return metacard;
  }

  private void transformTargetLocation(Metacard metacard) {
    String locationString = formatTargetLocation(metacard);

    try {
      LOGGER.debug("Formatted Target Location(s) = {}", locationString);

      if (locationString != null) {
        // validate the wkt
        WKTReader wktReader = new WKTReader(geometryFactory);
        Geometry geometry = wktReader.read(locationString);

        LOGGER.debug("Setting the metacard attribute [{}, {}]", Core.LOCATION, geometry.toText());
        IndexedMtirpbAttribute.INDEXED_TARGET_LOCATION_ATTRIBUTE
            .getAttributeDescriptors()
            .forEach(
                descriptor ->
                    setMetacardAttribute(metacard, descriptor.getName(), geometry.toText()));
      }

    } catch (ParseException e) {
      LOGGER.debug(e.getMessage(), e);
    }
  }

  private String formatTargetLocation(Metacard metacard) {
    Attribute locationAttribute =
        IndexedMtirpbAttribute.INDEXED_TARGET_LOCATION_ATTRIBUTE.getAttributeDescriptors().stream()
            .map(descriptor -> metacard.getAttribute(descriptor.getName()))
            .findFirst()
            .orElse(null);

    if (locationAttribute != null) {
      StringBuilder stringBuilder = new StringBuilder("MULTIPOINT (");

      locationAttribute
          .getValues()
          .forEach(
              value -> {
                parseLocation(stringBuilder, value.toString());
                stringBuilder.append(",");
              });

      stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
      stringBuilder.append(")");
      return stringBuilder.toString();
    }

    return null;
  }

  private void transformAircraftLocation(Metacard metacard) {
    String aircraftLocation = formatAircraftLocation(metacard);

    try {
      LOGGER.debug("Formatted Aircraft Location = {}", aircraftLocation);

      if (aircraftLocation != null) {
        // validate the wkt
        WKTReader wktReader = new WKTReader(geometryFactory);
        wktReader.read(aircraftLocation);

        MtirpbAttribute.AIRCRAFT_LOCATION_ATTRIBUTE
            .getAttributeDescriptors()
            .forEach(
                descriptor ->
                    setMetacardAttribute(metacard, descriptor.getName(), aircraftLocation));
      }

    } catch (ParseException e) {
      LOGGER.debug(e.getMessage(), e);
    }
  }

  private String formatAircraftLocation(Metacard metacard) {
    Attribute aircraftLocation =
        MtirpbAttribute.AIRCRAFT_LOCATION_ATTRIBUTE.getAttributeDescriptors().stream()
            .map(descriptor -> metacard.getAttribute(descriptor.getName()))
            .findFirst()
            .orElse(null);

    if (aircraftLocation != null
        && StringUtils.isNotEmpty(aircraftLocation.getValue().toString())) {

      String unformattedAircraftLocation = aircraftLocation.getValue().toString();

      StringBuilder sb = new StringBuilder("POINT (");
      parseLocation(sb, unformattedAircraftLocation);
      sb.append(")");

      return sb.toString();
    }

    return null;
  }

  private void parseLocation(StringBuilder stringBuilder, String locationString) {

    if (StringUtils.isEmpty(locationString)) {
      return;
    }

    Matcher matcher = LOCATION_PATTERN.matcher(locationString);

    if (matcher.matches()) {
      String lon = matcher.group(1);
      String lat = matcher.group(2);

      stringBuilder.append(String.format("%s %s", lon, lat));
    }
  }

  public void setGeometryFactory(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private void setMetacardAttribute(Metacard metacard, String attrName, String value) {
    LOGGER.trace("Setting the metacard attribute [{}, {}]", attrName, value);
    metacard.setAttribute(new AttributeImpl(attrName, value));
  }
}
