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
package org.codice.alliance.imaging.chip.transformer;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does the translation from chip coordinates of the overview image to the chip coordinates
 * on the full-size image.
 */
public class CropAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CropAdapter.class);

    /**
     * @param original the full-sized image that the chip will be taken from.
     * @param overview a scaled-down version of the same image.
     * @param args     A map containing values for the top left corner of the chip (x, y) and the
     *                 height and width of the chip (w, h) as taken from the overview image.
     * @return a vector of [x, y, w, h] translated to the full size image.
     */
    public int[] scaleChip(BufferedImage original, BufferedImage overview,
            Map<String, Serializable> args) {

        if (args == null) {
            throw new IllegalArgumentException("method argument 'args' may not be null.");
        }

        int x = getArg(args, "x");
        int y = getArg(args, "y");
        int w = getArg(args, "w");
        int h = getArg(args, "h");

        double scaleFactor = 1.0;

        if (original != null && overview != null) {
            scaleFactor = original.getWidth() / ((double) overview.getWidth());
        }

        int scaledX = multiply(scaleFactor, x);
        int scaledY = multiply(scaleFactor, y);
        int scaledW = multiply(scaleFactor, w);
        int scaledH = multiply(scaleFactor, h);

        return new int[] {scaledX, scaledY, scaledW, scaledH};
    }

    private int getArg(Map<String, Serializable> args, String key) {
        Serializable value = args.get(key);

        if (value == null) {
            throw new IllegalArgumentException(String.format("argument '%s' may not be null.",
                    key));
        }

        return Integer.valueOf(value.toString());
    }

    private int multiply(double scaleFactor, int coord) {
        return (int) Math.round(scaleFactor * coord);
    }
}
