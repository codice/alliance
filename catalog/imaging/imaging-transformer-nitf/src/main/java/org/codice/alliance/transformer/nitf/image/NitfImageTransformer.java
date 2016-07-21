/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codice.alliance.transformer.nitf.common.SegmentHandler;
import org.codice.imaging.nitf.core.image.ImageCoordinates;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.transform.CatalogTransformerException;

/**
 * Converts NITF images into a Metacard.
 */
public class NitfImageTransformer extends SegmentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfImageTransformer.class);

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(
            PrecisionModel.FLOATING), 4326);

    public Metacard transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard)
            throws IOException, CatalogTransformerException {

        validateArgument(nitfSegmentsFlow, "nitfSegmentsFlow");
        validateArgument(metacard, "metacard");

        handleSegments(nitfSegmentsFlow, metacard);
        return metacard;
    }

    private void handleSegments(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard)
            throws CatalogTransformerException {
        validateArgument(nitfSegmentsFlow, "nitfSegmentsFlow");
        validateArgument(metacard, "metacard");

        List<Polygon> polygonList = new ArrayList<>();

        nitfSegmentsFlow.forEachImageSegment(segment -> handleImageSegmentHeader(metacard,
                segment,
                polygonList))
                .forEachGraphicSegment(segment -> handleSegmentHeader(metacard,
                        segment,
                        GraphicAttribute.values()))
                .forEachTextSegment(segment -> handleSegmentHeader(metacard,
                        segment,
                        TextAttribute.values()))
                .forEachSymbolSegment(segment -> handleSegmentHeader(metacard,
                        segment,
                        SymbolAttribute.values()))
                .forEachLabelSegment(segment -> handleSegmentHeader(metacard,
                        segment,
                        LabelAttribute.values()))
                .end();

        // Set GEOGRAPHY from discovered polygons
        if (polygonList.size() == 1) {
            metacard.setAttribute(new AttributeImpl(Metacard.GEOGRAPHY,
                    polygonList.get(0)
                            .toText()));
        } else if (polygonList.size() > 1) {
            Polygon[] polyAry = polygonList.toArray(new Polygon[polygonList.size()]);
            MultiPolygon multiPolygon = GEOMETRY_FACTORY.createMultiPolygon(polyAry);
            metacard.setAttribute(new AttributeImpl(Metacard.GEOGRAPHY, multiPolygon.toText()));
        }
    }

    private void handleImageSegmentHeader(Metacard metacard, ImageSegment imagesegmentHeader,
            List<Polygon> polygons) {

        handleSegmentHeader(metacard, imagesegmentHeader, ImageAttribute.values());

        // handle geometry
        if ((imagesegmentHeader.getImageCoordinatesRepresentation()
                == ImageCoordinatesRepresentation.GEOGRAPHIC) || (
                imagesegmentHeader.getImageCoordinatesRepresentation()
                        == ImageCoordinatesRepresentation.DECIMALDEGREES)) {
            polygons.add(getPolygonForSegment(imagesegmentHeader, GEOMETRY_FACTORY));
        } else if (imagesegmentHeader.getImageCoordinatesRepresentation()
                != ImageCoordinatesRepresentation.NONE) {
            LOGGER.warn("Unsupported representation: {}. The NITF will be ingested, but image"
                            + " coordinates will not be available.",
                    imagesegmentHeader.getImageCoordinatesRepresentation());
        }
    }

    private Polygon getPolygonForSegment(ImageSegment segment, GeometryFactory geomFactory) {
        Coordinate[] coords = new Coordinate[5];
        ImageCoordinates imageCoordinates = segment.getImageCoordinates();
        coords[0] = new Coordinate(imageCoordinates.getCoordinate00()
                .getLongitude(),
                imageCoordinates.getCoordinate00()
                        .getLatitude());
        coords[4] = new Coordinate(coords[0]);
        coords[1] = new Coordinate(imageCoordinates.getCoordinate0MaxCol()
                .getLongitude(),
                imageCoordinates.getCoordinate0MaxCol()
                        .getLatitude());
        coords[2] = new Coordinate(imageCoordinates.getCoordinateMaxRowMaxCol()
                .getLongitude(),
                imageCoordinates.getCoordinateMaxRowMaxCol()
                        .getLatitude());
        coords[3] = new Coordinate(imageCoordinates.getCoordinateMaxRow0()
                .getLongitude(),
                imageCoordinates.getCoordinateMaxRow0()
                        .getLatitude());
        LinearRing externalRing = geomFactory.createLinearRing(coords);
        return geomFactory.createPolygon(externalRing, null);
    }

    private void validateArgument(Object object, String argumentName) {
        if (object == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' may not be null.",
                    argumentName));
        }
    }
}
