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

class GeoChipDescriptor {
    private GeoImageDescriptor parentImage;

    private Rectangle<Coordinate> latLonRectangle;

    private Rectangle<Point> pixelRectangle;

    private int widthInPixels;

    private int heightInPixels;

    public GeoChipDescriptor(GeoImageDescriptor parentImage,
            Rectangle<Coordinate> latLonRectangle) {
        this.parentImage = parentImage;
        this.latLonRectangle = latLonRectangle;
        this.pixelRectangle = buildPixelRectangle();
        this.widthInPixels = (int) (this.pixelRectangle.getLowerRight()
                .getX() - this.pixelRectangle.getUpperLeft()
                .getX());
        this.heightInPixels = (int) (this.pixelRectangle.getLowerRight()
                .getY() - this.pixelRectangle.getUpperLeft()
                .getY());
    }

    private Rectangle<Point> buildPixelRectangle() {
        Point upperLeft = new Point(convertLonToPixelWidth(latLonRectangle.getUpperLeft().x),
                convertLatToPixelHeight(latLonRectangle.getUpperLeft().y));
        Point lowerRight = new Point(convertLonToPixelWidth(latLonRectangle.getLowerRight().x),
                convertLatToPixelHeight(latLonRectangle.getLowerRight().y));
        return new Rectangle<>(upperLeft, lowerRight);
    }

    public Rectangle<Point> getPixelRectangle() {
        return this.pixelRectangle;
    }

    private int convertLatToPixelHeight(double lat) {
        double heightMultiplier = parentImage.getHeightInPixels() / parentImage.getHeightInLat();
        double translation = lat - parentImage.getLatLonRectangle()
                .getUpperLeft().y;
        return (int) (translation * heightMultiplier);
    }

    private int convertLonToPixelWidth(double lon) {
        double widthMultiplier = parentImage.getWidthInPixels() / parentImage.getWidthInLon();
        double translation = lon - parentImage.getLatLonRectangle()
                .getUpperLeft().x;
        return (int) (translation * widthMultiplier);
    }

    public int getWidthInPixels() {
        return this.widthInPixels;
    }

    public int getHeightInPixels() {
        return this.heightInPixels;
    }
}
