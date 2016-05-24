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
package org.codice.alliance.transformer.nitf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.image.ImageCoordinates;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;

/**
 * Converts NITF images into a Metacard.
 */
public class NitfInputTransformer implements InputTransformer {

    static final MimeType MIME_TYPE;

    private static final String ID = "ddf/catalog/transformer/nitf";

    private static final String MIME_TYPE_STRING = "image/nitf";

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfInputTransformer.class);

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(
            PrecisionModel.FLOATING), 4326);

    static {
        try {
            MIME_TYPE = new MimeType(MIME_TYPE_STRING);
        } catch (MimeTypeParseException e) {
            throw new ExceptionInInitializerError(String.format(
                    "unable to create MimeType from '%s': %s",
                    MIME_TYPE_STRING,
                    e.getMessage()));
        }
    }

    private MetacardType metacardType;

    /**
     * Transforms NITF images into a {@link Metacard}
     */
    public Metacard transform(InputStream input) throws IOException, CatalogTransformerException {
        return transform(input, null);
    }

    public Metacard transform(InputStream input, String id)
            throws IOException, CatalogTransformerException {
        if (input == null) {
            throw new CatalogTransformerException("Cannot transform null input.");
        }

        final MetacardImpl metacard = new MetacardImpl(metacardType);

        parseNitf(input, metacard);
        metacard.setAttribute(Metacard.ID, id);
        metacard.setAttribute(Metacard.CONTENT_TYPE, MIME_TYPE.toString());

        return metacard;
    }

    private void parseNitf(InputStream input, MetacardImpl metacard)
            throws CatalogTransformerException {
        List<Polygon> polygonList = new ArrayList<>();

        try {
            new NitfParserInputFlow()
                    .inputStream(input)
                    .headerOnly()
                    .fileHeader(header -> handleNitfHeader(metacard, header))
                    .forEachImageSegment(segment -> handleImageSegmentHeader(metacard, segment, polygonList))
                    .forEachGraphicSegment(segment -> handleSegmentHeader(metacard, segment, GraphicAttribute.values()))
                    .forEachTextSegment(segment -> handleSegmentHeader(metacard, segment, TextAttribute.values()))
                    .forEachSymbolSegment(segment -> handleSegmentHeader(metacard, segment, SymbolAttribute.values()))
                    .forEachLabelSegment(segment -> handleSegmentHeader(metacard, segment, LabelAttribute.values()));

            // Set GEOGRAPHY from discovered polygons
            if (polygonList.size() == 1) {
                metacard.setAttribute(Metacard.GEOGRAPHY,
                        polygonList.get(0)
                                .toText());
            } else if (polygonList.size() > 1) {
                Polygon[] polyAry = polygonList.toArray(new Polygon[polygonList.size()]);
                MultiPolygon multiPolygon = GEOMETRY_FACTORY.createMultiPolygon(polyAry);
                metacard.setAttribute(Metacard.GEOGRAPHY, multiPolygon.toText());
            }
        } catch (NitfFormatException e) {
            throw new CatalogTransformerException(e);
        }
    }

    private void handleNitfHeader(Metacard metacard, NitfHeader header) {
        Date date = (Date) NitfHeaderAttribute.FILE_DATE_AND_TIME.getAccessorFunction()
                .apply(header);

        metacard.setAttribute(new AttributeImpl(Metacard.TITLE, header.getFileTitle()));
        metacard.setAttribute(new AttributeImpl(Metacard.MODIFIED, date));
        metacard.setAttribute(new AttributeImpl(Metacard.CREATED, date));
        metacard.setAttribute(new AttributeImpl(Metacard.EFFECTIVE, date));
        handleSegmentHeader(metacard, header, NitfHeaderAttribute.values());
    }

    private void handleImageSegmentHeader(Metacard metacard,
            ImageSegment imagesegmentHeader, List<Polygon> polygons) {

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

    private <T> void handleSegmentHeader(Metacard metacard, T segment,
            NitfAttribute[] attributes) {
        for (NitfAttribute attribute : attributes) {
            @SuppressWarnings("unchecked")
            Function<T, Serializable> accessor = attribute.getAccessorFunction();
            Serializable value = accessor.apply(segment);
            AttributeDescriptor descriptor = attribute.getAttributeDescriptor();

            if (descriptor.getType()
                    .equals(BasicTypes.STRING_TYPE) &&
                    value != null && ((String) value).length() == 0) {
                value = null;
            }

            if (value != null) {
                Attribute catalogAttribute = new AttributeImpl(descriptor.getName(), value);
                metacard.setAttribute(catalogAttribute);
            }
        }
    }

    private Polygon getPolygonForSegment(ImageSegment segment,
            GeometryFactory geomFactory) {
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

    @Override
    public String toString() {
        return "InputTransformer {Impl=" + this.getClass()
                .getName() + ", id=" + ID + ", mime-type=" + MIME_TYPE + "}";
    }

    public void setNitfMetacardType(NitfMetacardType nitfMetacardType) {
        LOGGER.info("NitfInputTransformer setNitfMetacardType()");
        this.metacardType = nitfMetacardType;
    }
}
