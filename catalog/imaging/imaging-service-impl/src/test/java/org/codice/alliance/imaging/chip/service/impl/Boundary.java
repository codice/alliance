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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.dense.BasicVector;

public class Boundary {
  private static final int KILOMETERS_PER_DEGREE_AT_EQUATOR = 111;

  private List<Vector> boundary;

  public Boundary(double lon, double lat, double sizeInKm, double rotation) {
    createBoundary(lat, lon, sizeInKm, rotation);
  }

  private List<Vector> getUnitSquare() {
    List<Vector> unitSquare = new ArrayList<>();
    unitSquare.add(new BasicVector(new double[] {-1, 1}));
    unitSquare.add(new BasicVector(new double[] {1, 1}));
    unitSquare.add(new BasicVector(new double[] {1, -1}));
    unitSquare.add(new BasicVector(new double[] {-1, -1}));
    unitSquare.replaceAll(v -> v.multiply(0.5));
    return unitSquare;
  }

  private Vector rotate(Vector v, double angle) {
    Matrix rotation =
        new Basic2DMatrix(
            new double[][] {
              {Math.cos(Math.toRadians(angle)), Math.sin(Math.toRadians(angle))},
              {-Math.sin(Math.toRadians(angle)), Math.cos(Math.toRadians(angle))}
            });
    return rotation.multiply(v);
  }

  private Vector scaleByLatitude(Vector v, double latitude) {
    double lat = v.get(1);
    double lon = v.get(0) / Math.cos(Math.toRadians(latitude));
    return new BasicVector(new double[] {lon, lat});
  }

  private void createBoundary(double lat, double lon, double sizeInKm, double rotation) {
    boundary =
        getUnitSquare().stream()
            .map(v -> v.multiply(sizeInKm / KILOMETERS_PER_DEGREE_AT_EQUATOR))
            .collect(Collectors.toList());
    boundary.replaceAll(v -> rotate(v, rotation));
    boundary.replaceAll(v -> scaleByLatitude(v, lat));
    Vector offset = new BasicVector(new double[] {lon, lat});
    boundary.replaceAll(v -> v.add(offset));
  }

  public List<Vector> getBoundary() {
    return boundary;
  }
}
