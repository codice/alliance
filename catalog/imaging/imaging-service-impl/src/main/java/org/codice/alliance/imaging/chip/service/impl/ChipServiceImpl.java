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
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codice.alliance.imaging.chip.service.api.ChipOutOfBoundsException;
import org.codice.alliance.imaging.chip.service.api.ChipService;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An implementation of ChipService. */
public class ChipServiceImpl implements ChipService {

  private static final int MAX_THREAD_COUNT =
      Integer.parseInt(System.getProperty("default.nitf.thread.count", "3"));

  private static final Logger LOGGER = LoggerFactory.getLogger(ChipServiceImpl.class);

  private Semaphore lock;

  public ChipServiceImpl() {
    lock = new Semaphore(MAX_THREAD_COUNT, true);
  }

  public ChipServiceImpl(Semaphore lock) {
    this.lock = lock;
  }

  /** {@inheritDoc} */
  @Override
  public BufferedImage chip(
      BufferedImage inputImage, Polygon inputImagePolygon, Polygon chipPolygon)
      throws ChipOutOfBoundsException {

    validateNotNull(inputImage, "inputImage");
    validateNotNull(inputImagePolygon, "inputImagePolygon");
    validateNotNull(chipPolygon, "chipPolygon");

    List<Vector> imageVectors = createVectorListFromPolygon(inputImagePolygon);
    List<Vector> chipVectors = createVectorListFromPolygon(chipPolygon);

    CoordinateConverter converter = new CoordinateConverter(inputImage, imageVectors);
    List<Vector> pixels = converter.toPixels(chipVectors);

    int maxX = findMax(pixels, v -> v.get(0));
    int maxY = findMax(pixels, v -> v.get(1));
    int minX = findMin(pixels, v -> v.get(0));
    int minY = findMin(pixels, v -> v.get(1));

    return crop(inputImage, minX, minY, maxX - minX, maxY - minY);
  }

  private int findMin(List<Vector> vectors, Function<Vector, Double> selector) {
    double minimum = vectors.stream().map(selector).min(Double::compareTo).orElse(Double.MIN_VALUE);

    return (int) Math.round(minimum);
  }

  private int findMax(List<Vector> vectors, Function<Vector, Double> selector) {
    double maximum = vectors.stream().map(selector).max(Double::compareTo).orElse(Double.MAX_VALUE);

    return (int) Math.round(maximum);
  }

  public BufferedImage crop(BufferedImage inputImage, int x, int y, int w, int h)
      throws ChipOutOfBoundsException {
    validateNotNull(inputImage, "inputImage");

    if (w < 0 || h < 0) {
      throw new ChipOutOfBoundsException(
          String.format(
              "method arguments 'w', 'h' may not be less than 0. Values were %s and %s.", w, h));
    }

    if (x > inputImage.getWidth() || y > inputImage.getHeight()) {
      throw new ChipOutOfBoundsException(
          String.format(
              "method arguments 'x' and 'y' may not be greater than the width and height of the supplied image."
                  + "\n   image width = %s, x = %s\n   image height = %s, y = %s",
              inputImage.getWidth(), x, inputImage.getHeight(), y));
    }

    if (x < 0) {
      x = 0;
    }

    if (y < 0) {
      y = 0;
    }

    if (x + w > inputImage.getWidth()) {
      w = inputImage.getWidth() - x;
    }

    if (y + h > inputImage.getHeight()) {
      h = inputImage.getHeight() - y;
    }

    try {
      lock.acquire();
      BufferedImage subImage;
      try {
        subImage = inputImage.getSubimage(x, y, w, h);
      } finally {
        lock.release();
      }
      return subImage;
    } catch (InterruptedException e) {
      LOGGER.debug("Interrupt received while doing image processing.", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  private List<Vector> createVectorListFromPolygon(Polygon polygon) {
    return Stream.of(polygon.getCoordinates())
        .map(v -> new BasicVector(new double[] {v.x, v.y}))
        .collect(Collectors.toList());
  }

  private void validateNotNull(Object value, String argumentName) {
    if (value == null) {
      throw new IllegalArgumentException(
          String.format("argument '%s' may not be null.", argumentName));
    }
  }
}
