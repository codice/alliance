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
package org.codice.alliance.imaging.chip.service.api;

import java.awt.image.BufferedImage;

import com.vividsolutions.jts.geom.Polygon;

/**
 * A ChipService provides a "chipped image" from a supplied image and polygons representing
 * the coordinates of the original and the chip.
 */
public interface ChipService {

    /**
     *
     * @param inputImage The original source image that the chip will be taken from.
     * @param inputImagePolygon A polygon representing the coordinates of the soure image.
     * @param chipPolygon A polygon representing the coordinates of the region to be chipped.
     * @return A BufferedImage containing the chipped region's pixels.
     * @throws ChipOutOfBoundsException when the chip's envelope crosses the boundary of the
     *                              inputImagePolygon.
     */
    BufferedImage chip(BufferedImage inputImage, Polygon inputImagePolygon, Polygon chipPolygon)
            throws ChipOutOfBoundsException;
}
