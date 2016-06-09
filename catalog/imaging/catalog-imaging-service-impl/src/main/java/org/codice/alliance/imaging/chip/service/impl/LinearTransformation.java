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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;

class LinearTransformation {
    private double angle;

    private Vector translateVector;

    private Vector scaleFactor;

    private Matrix rotationMatrix;

    public List<Vector> translate(List<Vector> vectors) {
        return apply(vectors, this::translate);
    }

    public List<Vector> rotate(List<Vector> vectors) {
        return apply(vectors, this::rotate);
    }

    public List<Vector> scale(List<Vector> vectors) {
        return apply(vectors, this::scale);
    }

    private List<Vector> apply(List<Vector> vectors, Function<Vector, Vector> function) {
        List<Vector> output = vectors.stream()
                .map(v -> function.apply(v))
                .collect(Collectors.toList());

        return output;
    }

    private Vector translate(Vector v) {
        return v.subtract(translateVector);
    }

    private Vector rotate(Vector v) {
        return rotationMatrix.multiply(v);
    }

    private Vector scale(Vector v) {
        return v.hadamardProduct(scaleFactor);
    }

    public void setRotationMatrix(double angle) {
        this.angle = angle;

        double[][] rotArray =
                {{Math.cos(angle), Math.sin(angle)}, {-Math.sin(angle), Math.cos(angle)}};

        rotationMatrix = new Basic2DMatrix(rotArray);
    }

    public List<Vector> apply(List<Vector> vectors) {
        return scale(rotate(translate(vectors)));
    }

    public void setTranslateVector(Vector translateVector) {
        this.translateVector = translateVector;
    }

    public void setScaleFactor(Vector scaleFactor) {
        this.scaleFactor = scaleFactor;
    }
}
