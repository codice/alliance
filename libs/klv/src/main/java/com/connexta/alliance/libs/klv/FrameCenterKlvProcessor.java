/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.libs.klv;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.connexta.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Uses {@link Stanag4609TransportStreamParser#FRAME_CENTER_LATITUDE} and
 * {@link Stanag4609TransportStreamParser#FRAME_CENTER_LONGITUDE} to generate a WKT LINESTRING
 * and store it in the metacard attribute {@link AttributeNameConstants#FRAME_CENTER}.
 */
public class FrameCenterKlvProcessor extends MultipleFieldKlvProcessor {

    /**
     * The name of the metacard attribute being set.
     */
    private static final String ATTRIBUTE_NAME = AttributeNameConstants.FRAME_CENTER;

    public FrameCenterKlvProcessor() {
        super(Arrays.asList(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE,
                Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE));
    }

    @Override
    protected void doProcess(Attribute attribute, Metacard metacard) {
        List<String> points = getAttributeStrings(attribute);

        Coordinate[] coordinates = listToArray(convertWktToCoordinates(points));

        LineString lineString = convertCoordinatesToLineString(coordinates);

        String wkt = convertLineStringToWkt(lineString);

        setAttribute(metacard, wkt);

    }

    private void setAttribute(Metacard metacard, String wkt) {
        metacard.setAttribute(new AttributeImpl(ATTRIBUTE_NAME, wkt));
    }

    private Coordinate[] listToArray(List<Coordinate> coordinateList) {
        return coordinateList.toArray(new Coordinate[coordinateList.size()]);
    }

    private String convertLineStringToWkt(LineString lineString) {
        WKTWriter wktWriter = new WKTWriter();
        return wktWriter.write(lineString.norm());
    }

    private LineString convertCoordinatesToLineString(Coordinate[] coordinates) {
        return new GeometryFactory().createLineString(coordinates);
    }

    private List<Coordinate> convertWktToCoordinates(List<String> points) {
        WKTReader wktReader = new WKTReader();
        return points.stream()
                .map(wkt -> GeometryUtility.wktToGeometry(wkt, wktReader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Geometry::getCoordinate)
                .collect(Collectors.toList());
    }

    private List<String> getAttributeStrings(Attribute attribute) {
        return attribute.getValues()
                .stream()
                .filter(serializable -> serializable instanceof String)
                .map(serializable -> (String) serializable)
                .collect(Collectors.toList());
    }
}
