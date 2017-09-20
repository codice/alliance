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
package org.codice.alliance.transformer.nitf.image;

import static org.codice.alliance.transformer.nitf.image.ImageAttribute.convertNitfDate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.common.SegmentHandler;
import org.codice.imaging.nitf.core.image.ImageCoordinates;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Converts NITF images into a Metacard. */
public class NitfImageTransformer extends SegmentHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(NitfImageTransformer.class);

  private static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

  private static final String IMAGE_DATATYPE = "Image";

  public Metacard transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard)
      throws IOException {

    validateArgument(nitfSegmentsFlow, "nitfSegmentsFlow");
    validateArgument(metacard, "metacard");

    LOGGER.debug("Setting the metacard attribute [{}, {}]", Core.DATATYPE, IMAGE_DATATYPE);
    metacard.setAttribute(new AttributeImpl(Core.DATATYPE, IMAGE_DATATYPE));
    handleSegments(nitfSegmentsFlow, metacard);
    return metacard;
  }

  private void handleSegments(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard) {
    validateArgument(nitfSegmentsFlow, "nitfSegmentsFlow");
    validateArgument(metacard, "metacard");

    List<Polygon> polygonList = new ArrayList<>();
    List<Date> imageDateAndTimeList = new ArrayList<>();

    nitfSegmentsFlow
        .forEachImageSegment(
            segment ->
                handleImageSegmentHeader(metacard, segment, polygonList, imageDateAndTimeList))
        .forEachGraphicSegment(
            segment -> handleSegmentHeader(metacard, segment, GraphicAttribute.values()))
        .forEachTextSegment(
            segment -> handleSegmentHeader(metacard, segment, TextAttribute.values()))
        .forEachSymbolSegment(
            segment -> handleSegmentHeader(metacard, segment, SymbolAttribute.values()))
        .forEachLabelSegment(
            segment -> handleSegmentHeader(metacard, segment, LabelAttribute.values()))
        .end();

    // Set GEOGRAPHY from discovered polygons
    if (polygonList.size() == 1) {
      metacard.setAttribute(new AttributeImpl(Core.LOCATION, polygonList.get(0).toText()));
    } else if (polygonList.size() > 1) {
      Polygon[] polyAry = polygonList.toArray(new Polygon[polygonList.size()]);
      MultiPolygon multiPolygon = GEOMETRY_FACTORY.createMultiPolygon(polyAry);
      metacard.setAttribute(new AttributeImpl(Core.LOCATION, multiPolygon.toText()));
    }

    // Set start, effective, and end from discovered imageDateAndTimes
    if (!imageDateAndTimeList.isEmpty()) {
      LOGGER.trace(
          "Discovered imageDateTimes of the image segments: {}", imageDateAndTimeList.toString());
      final Date firstDateAndTime = imageDateAndTimeList.get(0);
      final Date lastDateAndTime = imageDateAndTimeList.get(imageDateAndTimeList.size() - 1);
      LOGGER.trace(
          "Setting the {} metacard attribute to {}.", Metacard.EFFECTIVE, firstDateAndTime);
      metacard.setAttribute(new AttributeImpl(Metacard.EFFECTIVE, firstDateAndTime));
      LOGGER.trace(
          "Setting the {} metacard attribute to {}.",
          ddf.catalog.data.types.DateTime.START,
          firstDateAndTime);
      metacard.setAttribute(
          new AttributeImpl(ddf.catalog.data.types.DateTime.START, firstDateAndTime));
      LOGGER.trace(
          "Setting the {} metacard attribute to {}.",
          ddf.catalog.data.types.DateTime.END,
          lastDateAndTime);
      metacard.setAttribute(
          new AttributeImpl(ddf.catalog.data.types.DateTime.END, lastDateAndTime));
    }
  }

  private void handleImageSegmentHeader(
      Metacard metacard,
      ImageSegment imagesegmentHeader,
      List<Polygon> polygons,
      List<Date> imageDateAndTimeList) {

    handleSegmentHeader(metacard, imagesegmentHeader, ImageAttribute.getAttributes());

    // custom handling of image header fields
    handleGeometry(imagesegmentHeader, polygons);
    handleMissionIdentifier(metacard, imagesegmentHeader.getImageIdentifier2());
    handleComments(metacard, imagesegmentHeader.getImageComments());
    handleTres(metacard, imagesegmentHeader);
    imageDateAndTimeList.add(convertNitfDate(imagesegmentHeader.getImageDateTime()));
  }

  protected void handleGeometry(ImageSegment imageSegmentHeader, List<Polygon> polygons) {
    ImageCoordinatesRepresentation imageCoordinatesRepresentation =
        imageSegmentHeader.getImageCoordinatesRepresentation();

    switch (imageCoordinatesRepresentation) {
      case MGRS:
      case UTMNORTH:
      case UTMSOUTH:
      case GEOGRAPHIC:
      case DECIMALDEGREES:
        polygons.add(getPolygonForSegment(imageSegmentHeader, GEOMETRY_FACTORY));
        break;
      default:
        LOGGER.debug(
            "Unsupported representation: {}. The NITF will be ingested, but image"
                + " coordinates will not be available.",
            imageCoordinatesRepresentation);
        break;
    }
  }

  /*
   * Extracts the mission identifier from the image segment IID2 field
   */
  protected void handleMissionIdentifier(Metacard metacard, String imageIdentifier2) {
    final int startIndex = 7; // inclusive
    final int endIndex = 11; // exclusive

    if (StringUtils.isNotBlank(imageIdentifier2) && imageIdentifier2.length() > endIndex) {
      String missionId = imageIdentifier2.substring(startIndex, endIndex);

      LOGGER.debug("Setting the metacard attribute [{}, {}]", Isr.MISSION_ID, missionId);
      metacard.setAttribute(new AttributeImpl(Isr.MISSION_ID, missionId));
    }
  }

  /*
   * Appends the ICOMn fields together to form a single block comment
   */
  protected void handleComments(Metacard metacard, List<String> comments) {
    if (comments.size() > 0) {
      StringBuilder sb = new StringBuilder();
      comments.forEach(
          comment -> {
            if (StringUtils.isNotBlank(comment)) {
              sb.append(comment + " ");
            }
          });

      LOGGER.trace("Setting the metacard attribute [{}, {}]", Isr.COMMENTS, sb.toString());
      metacard.setAttribute(new AttributeImpl(Isr.COMMENTS, sb.toString()));
    }
  }

  private Polygon getPolygonForSegment(ImageSegment segment, GeometryFactory geomFactory) {
    Coordinate[] coords = new Coordinate[5];
    ImageCoordinates imageCoordinates = segment.getImageCoordinates();
    coords[0] =
        new Coordinate(
            imageCoordinates.getCoordinate00().getLongitude(),
            imageCoordinates.getCoordinate00().getLatitude());
    coords[4] = new Coordinate(coords[0]);
    coords[1] =
        new Coordinate(
            imageCoordinates.getCoordinate0MaxCol().getLongitude(),
            imageCoordinates.getCoordinate0MaxCol().getLatitude());
    coords[2] =
        new Coordinate(
            imageCoordinates.getCoordinateMaxRowMaxCol().getLongitude(),
            imageCoordinates.getCoordinateMaxRowMaxCol().getLatitude());
    coords[3] =
        new Coordinate(
            imageCoordinates.getCoordinateMaxRow0().getLongitude(),
            imageCoordinates.getCoordinateMaxRow0().getLatitude());
    LinearRing externalRing = geomFactory.createLinearRing(coords);
    return geomFactory.createPolygon(externalRing, null);
  }

  private void validateArgument(Object object, String argumentName) {
    if (object == null) {
      throw new IllegalArgumentException(
          String.format("Argument '%s' may not be null.", argumentName));
    }
  }
}
