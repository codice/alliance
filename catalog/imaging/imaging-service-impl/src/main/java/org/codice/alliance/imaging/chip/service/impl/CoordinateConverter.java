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
package org.codice.alliance.imaging.chip.service.impl;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import org.la4j.LinearAlgebra;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.linear.LinearSystemSolver;
import org.la4j.matrix.dense.Basic2DMatrix;

/** Converts lat/lon points to pixels and vice-versa. */
public class CoordinateConverter {

  private List<Vector> boundary;

  private Matrix basis;

  private LinearSystemSolver solver;

  /**
   * @param image the input image that the basis will be calcluated for.
   * @param boundary a List of vectors that represent the Lat/Lon of the image.
   */
  public CoordinateConverter(BufferedImage image, List<Vector> boundary) {
    calculateBasis(boundary, image);
    this.boundary = boundary;
  }

  public CoordinateConverter(int width, int height, List<Vector> boundary) {
    calculateBasis(boundary, width, height);
    this.boundary = boundary;
  }

  private void calculateBasis(List<Vector> boundary, int width, int height) {
    // Move to origin
    Vector xAxis = boundary.get(1).subtract(boundary.get(0));
    Vector yAxis = boundary.get(3).subtract(boundary.get(0));

    // The xAxis is the width of the image and the yAxis is the height
    // We want our vectors to be 1 pixel in length
    Vector xBasis = xAxis.multiply(1.0 / width);
    Vector yBasis = yAxis.multiply(1.0 / height);

    basis = new Basic2DMatrix(2, 2);
    basis.setColumn(0, xBasis);
    basis.setColumn(1, yBasis);

    solver = basis.withSolver(LinearAlgebra.FORWARD_BACK_SUBSTITUTION);
  }

  private void calculateBasis(List<Vector> boundary, BufferedImage image) {
    calculateBasis(boundary, image.getWidth(), image.getHeight());
  }

  /**
   * @param lonLats the area within in the image to be converted to pixel.
   * @return a Vector containing the pixel-equivalent of the area described by lonLats.
   */
  public List<Vector> toPixels(List<Vector> lonLats) {
    return lonLats.stream()
        .map(vector -> solver.solve(vector.subtract(boundary.get(0))))
        .collect(Collectors.toList());
  }

  /**
   * @param pixels the area withing the image to be converted to lat/lon.
   * @return a Vector containing the latlon-equivalent of the area described by pixels.
   */
  public List<Vector> toLonLat(List<Vector> pixels) {
    return pixels.stream()
        .map(vector -> basis.multiply(vector).add(boundary.get(0)))
        .collect(Collectors.toList());
  }
}
