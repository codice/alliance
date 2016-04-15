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

import com.vividsolutions.jts.geom.Coordinate;

class GeoImageDescriptor {
    private Rectangle<Point> pixelRectangle;
    private Rectangle<Coordinate> latLonRectangle;

    public GeoImageDescriptor(Rectangle<Point> pixelRectangle, Rectangle<Coordinate> latLonRectangle) {
        this.pixelRectangle = pixelRectangle;
        this.latLonRectangle = latLonRectangle;
    }

    public int getHeightInPixels() {
        return pixelRectangle.getLowerRight().y - pixelRectangle.getUpperLeft().y;
    }

    public int getWidthInPixels() {
        return pixelRectangle.getLowerRight().x - pixelRectangle.getUpperLeft().x;
    }

    public double getHeightInLat() {
        return latLonRectangle.getLowerRight().y - latLonRectangle.getUpperLeft().y;
    }

    public double getWidthInLon() {
        return latLonRectangle.getLowerRight().x - latLonRectangle.getUpperLeft().x;
    }

    public Rectangle<Coordinate> getLatLonRectangle() {
        return latLonRectangle;
    }
}
