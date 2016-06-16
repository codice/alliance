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
package org.codice.alliance.imaging.chip.service.impl;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import org.codice.alliance.imaging.chip.service.api.ChipOutOfBoundsException;
import org.codice.alliance.imaging.chip.service.api.ChipService;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * An implementation of ChipService.
 */
public class ChipServiceImpl implements ChipService {

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage chip(BufferedImage inputImage, Polygon inputImagePolygon,
            Polygon chipPolygon) throws ChipOutOfBoundsException {

        validateNotNull(inputImage, "inputImage");
        validateNotNull(inputImagePolygon, "inputImagePolygon");
        validateNotNull(chipPolygon, "chipPolygon");

        if (!inputImagePolygon.contains(chipPolygon.getEnvelope())) {
            throw new ChipOutOfBoundsException(
                    "The envelope of the chip polygon must reside entirely within the image.");
        }

        Rectangle<Point> imagePixelBounds = getImagePixelBounds(inputImage);
        Rectangle<Coordinate> imageGeoBounds = getGeoBounds(inputImagePolygon);
        Rectangle<Coordinate> chipBounds = getGeoBounds(chipPolygon);
        GeoImageDescriptor geoImageDescriptor = new GeoImageDescriptor(imagePixelBounds,
                imageGeoBounds);
        GeoChipDescriptor geoChipDescriptor = new GeoChipDescriptor(geoImageDescriptor, chipBounds);
        Rectangle<Point> chipPixelRectangle = geoChipDescriptor.getPixelRectangle();

        BufferedImage chipImage = inputImage.getSubimage(chipPixelRectangle.getUpperLeft().x,
                chipPixelRectangle.getUpperLeft().y,
                geoChipDescriptor.getWidthInPixels(),
                geoChipDescriptor.getHeightInPixels());

        return chipImage;
    }

    private void validateNotNull(Object value, String argumentName) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("argument '%s' may not be null.",
                    argumentName));
        }
    }

    private Rectangle<Point> getImagePixelBounds(BufferedImage image) {
        Point upperLeft = new Point(0, 0);
        Point bottomRight = new Point(image.getWidth(null), image.getHeight(null));
        Rectangle<Point> rectangle = new Rectangle<>(upperLeft, bottomRight);
        return rectangle;
    }

    private Rectangle<Coordinate> getGeoBounds(Polygon polygon) {
        Coordinate[] coordinates = polygon.getEnvelope()
                .getCoordinates();

        double minLat = findMin(coordinates, coord -> coord.y);
        double maxLat = findMax(coordinates, coord -> coord.y);
        double minLon = findMin(coordinates, coord -> coord.x);
        double maxLon = findMax(coordinates, coord -> coord.x);

        Coordinate upperLeft = new Coordinate(minLon, maxLat);
        Coordinate lowerRight = new Coordinate(maxLon, minLat);
        Rectangle<Coordinate> geoBounds = new Rectangle<>(upperLeft, lowerRight);
        return geoBounds;
    }

    private double findMin(Coordinate[] coordinates, ToDoubleFunction<Coordinate> mappingFunction) {
        return Stream.of(coordinates)
                .mapToDouble(mappingFunction)
                .min()
                .getAsDouble();
    }

    private double findMax(Coordinate[] coordinates, ToDoubleFunction<Coordinate> mappingFunction) {
        return Stream.of(coordinates)
                .mapToDouble(mappingFunction)
                .max()
                .getAsDouble();
    }
}