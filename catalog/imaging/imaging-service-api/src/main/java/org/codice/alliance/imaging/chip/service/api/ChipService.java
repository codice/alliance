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
package org.codice.alliance.imaging.chip.service.api;

import java.awt.image.BufferedImage;
import org.locationtech.jts.geom.Polygon;

/**
 * A ChipService provides a "chipped image" from a supplied image and polygons representing the
 * coordinates of the original and the chip.
 */
public interface ChipService {

  /**
   * @param inputImage The original source image that the chip will be taken from.
   * @param inputImagePolygon A polygon representing the coordinates of the soure image.
   * @param chipPolygon A polygon representing the coordinates of the region to be chipped.
   * @return A BufferedImage containing the chipped region's pixels.
   * @throws ChipOutOfBoundsException when the chip's envelope crosses the boundary of the
   *     inputImagePolygon.
   */
  BufferedImage chip(BufferedImage inputImage, Polygon inputImagePolygon, Polygon chipPolygon)
      throws ChipOutOfBoundsException;

  /**
   * @param inputImage The image to be cropped.
   * @param x The x coordinate of the top left corner of the crop area. If 'x' is less than 0 then 0
   *     will be used.
   * @param y The y coordinate of the top left corner of the crop area. If 'y' is less than 0 then 0
   *     will be used.
   * @param w The width of the crop region. If x + w is greater than the width of the image then w
   *     will be adjusted down such that x + w will equal the image width.
   * @param h The height of hte crop region. If y + h is greater than the height of the image then h
   *     will be adjusted down such that y + w will equal the image height.
   * @return The portion of the image inside the crop area.
   * @throws ChipOutOfBoundsException when x &gt; image width, y &gt; image height, w &lt; 0 or h
   *     &lt; 0.
   */
  BufferedImage crop(BufferedImage inputImage, int x, int y, int w, int h)
      throws ChipOutOfBoundsException;
}
