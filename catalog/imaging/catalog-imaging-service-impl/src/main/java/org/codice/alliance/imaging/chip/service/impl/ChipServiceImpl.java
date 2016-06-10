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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codice.alliance.imaging.chip.service.api.ChipOutOfBoundsException;
import org.codice.alliance.imaging.chip.service.api.ChipService;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

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

        List<Vector> imageVectors = createVectorListFromPolygon(inputImagePolygon);
        List<Vector> chipVectors = createVectorListFromPolygon(chipPolygon);

        LinearTransformation t = findTransform(imageVectors, inputImage);
        List<Vector> transformedChip = t.apply(chipVectors);

        return getSubimage(transformedChip, inputImage);
    }

    private List<Vector> createVectorListFromPolygon(Polygon polygon) {
        List<Vector> vectors = Stream.of(polygon.getCoordinates())
                .map(v -> new BasicVector(new double[]{v.x, v.y}))
                .collect(Collectors.toList());

        return vectors;
    }

    private LinearTransformation findTransform(List<Vector> vectors, BufferedImage image) {
        LinearTransformation t = new LinearTransformation();

        /*
         * We are given a rectangular boundary in longitude / latitude and a corresponding image,
         * and we would like to find the transformation that goes from lat/lon to pixel space.
         *
         * Assumptions:
         *   1) Lon / Lat space is Cartesian.
         *      This is definitely WRONG, but it makes the math easier and should be close enough.
         *   2) The boundary is a rectangle and in the following order:
         *
         *              TOP
         *           0-------1
         *           |       |
         *           |       |
         *           |       |
         *           3-------2
         *
         * Procedure:
         *   1) Translate the boundary so that the top left corner is at the origin
         *
         *   2) Rotate the boundary such that the top is along the X axis and the left
         *      side is going down the Y axis in the negative direction
         *
         *          0               origin
         *        *   *                0---------1-----> X axis
         *      *       *              |         |
         *    3           1  -->       |         |
         *      *       *              |         |
         *        *   *                3---------2
         *          2                  |
         *                             V
         *                          Y Axis
         *
         *   3) Stretch in the X direction and Y direction so that it lines up with pixel space
         *
         *                       Width (pixels)                    Height (pixels)
         *      X scale factor = ---------------  Y scale factor = ---------------
         *                       Width (degrees)                   Height (degrees)
         */

        t.setTranslateVector(vectors.get(0));
        List<Vector> translated = t.translate(vectors);

        t.setRotationMatrix(findAngle(translated.get(1)));
        List<Vector> rotated = t.rotate(translated);

        t.setScaleFactor(findScale(rotated, image));

        return t;
    }

    private double findAngle(Vector v) {

        /* To find the angle, we exploit the following things:
         *  1) A * B = |A||B|cos(theta)
         *  2) A = our vector, B = unit vector in X direction, so
         *     A = [x y] and B = [1 0]
         *  3) The dot product then becomes x*1 + y*0 = x
         */
        double angle = Math.acos(v.get(0) / v.norm());

        /* We have the angle but we still need to know whether to rotate clockwise or
         * counter-clockwise. We check the y value of our vector to see if it is above
         * or below the X axis. If it is above, we need to rotate clockwise, and if it
         * is below, we need to rotate counter-clockwise.
         */
        return v.get(1) > 0 ? angle : -angle;
    }

    private Vector findScale(List<Vector> vectors, BufferedImage image) {

        /* By the time we are ready to scale, we have translated and rotate so we have
         * the following:
         *
         *    origin
         *      0---------1-----> X axis
         *      |         |
         *      |         |
         *      |         |
         *      3---------2
         *      |
         *      V
         *   Y Axis
         *
         *   Thus, to get the width we just get the X value of the top and to get the height
         *   we get the Y value of the left, but we flip it since it's going in the negative
         *   direction.
         */

        double[] scaleFactor = new double[2];
        scaleFactor[0] =   image.getWidth()  / vectors.get(1).get(0);
        scaleFactor[1] = -image.getHeight() / vectors.get(3).get(1);
        return new BasicVector(scaleFactor);
    }

    private BufferedImage getSubimage(List<Vector> vectors, BufferedImage image)
            throws ChipOutOfBoundsException {

        /* We have the following picture, and we are trying to get the subimage inside
         * the larger one.
         *
         *    origin
         *      0--------------1-----> X axis
         *      |              |
         *      |  0--1        |
         *      |  |  |        |
         *      |  3--2        |
         *      |              |
         *      3--------------2
         *      |
         *      V
         *   Y Axis
         *
         *   We need the top left corner and the width and height.
         *   - The top left corner is the first element, keeping in mind we need to flip the Y axis
         *   - The width is the X value for corner 1 minus corner 0
         *   - For the height, we again keep in mind that we are going in the negative Y direction
         *     so -(corner 3 - corner 0) becomes corner 0 - corner 3
         */

        int x = (int)   vectors.get(0).get(0);
        int y = (int) -vectors.get(0).get(1);

        int w = (int) (vectors.get(1).get(0) - vectors.get(0).get(0));
        int h = (int) (vectors.get(0).get(1) - vectors.get(3).get(1));

        Rectangle imageBounds = new Rectangle(image.getWidth(), image.getHeight());
        Rectangle chipBounds = new Rectangle(x, y);

        if (!imageBounds.contains(chipBounds)) {
            throw new ChipOutOfBoundsException("the requested chip is not within the bounds of the parent image.");
        }

        return image.getSubimage(x, y, w, h);
    }

    private void validateNotNull(Object value, String argumentName) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("argument '%s' may not be null.",
                    argumentName));
        }
    }
}