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
package org.codice.alliance.libs.klv;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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

    private final GeometryOperator geometryOperator;

    public FrameCenterKlvProcessor() {
        this(GeometryOperator.IDENTITY);
    }

    /**
     * @param geometryOperator transform the {@link Geometry} object (e.g. simplify) (must be non-null)
     */
    public FrameCenterKlvProcessor(GeometryOperator geometryOperator) {
        super(Arrays.asList(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE,
                Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE));
        notNull(geometryOperator, "geometryOperator must be non-null");
        this.geometryOperator = geometryOperator;
    }

    public GeometryOperator getGeometryOperator() {
        return geometryOperator;
    }

    @Override
    protected void doProcess(Attribute attribute, Metacard metacard) {
        List<String> points = getAttributeStrings(attribute);

        Coordinate[] coordinates = listToArray(convertWktToCoordinates(points));

        Geometry geometry = convertCoordinatesToGeometry(coordinates);

        String wkt = convertGeometryToWkt(geometryOperator.apply(geometry));

        setAttribute(metacard, wkt);
    }

    private void setAttribute(Metacard metacard, String wkt) {
        metacard.setAttribute(new AttributeImpl(ATTRIBUTE_NAME, wkt));
    }

    private Coordinate[] listToArray(List<Coordinate> coordinateList) {
        return coordinateList.toArray(new Coordinate[coordinateList.size()]);
    }

    private String convertGeometryToWkt(Geometry geometry) {
        WKTWriter wktWriter = new WKTWriter();
        return wktWriter.write(geometry);
    }

    private Geometry convertCoordinatesToGeometry(Coordinate[] coordinates) {
        if (coordinates.length == 1) {
            return new GeometryFactory().createPoint(coordinates[0]);
        } else {
            return new GeometryFactory().createLineString(coordinates);
        }
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

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
