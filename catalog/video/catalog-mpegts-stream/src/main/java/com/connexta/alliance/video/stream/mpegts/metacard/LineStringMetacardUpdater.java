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
package com.connexta.alliance.video.stream.mpegts.metacard;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import com.connexta.alliance.libs.klv.GeometryUtility;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class LineStringMetacardUpdater implements MetacardUpdater {

    private final String attributeName;

    public LineStringMetacardUpdater(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public void update(Metacard parent, Metacard child) {
        if (!hasFrameCenter(parent) && hasFrameCenter(child)) {
            setAttribute(parent, child);
        } else if (hasFrameCenter(parent) && hasFrameCenter(child)) {
            WKTReader wktReader = new WKTReader();

            Optional<Geometry> parentGeo = GeometryUtility.wktToGeometry(getValue(parent),
                    wktReader);
            Optional<Geometry> childGeo = GeometryUtility.wktToGeometry(getValue(child), wktReader);

            if (parentGeo.isPresent() && childGeo.isPresent()) {
                Coordinate[] coordinates = getMergedCoordinates(parentGeo, childGeo);
                LineString lineString = convertCoordinatesToLineString(coordinates);
                setAttribute(parent, lineString);
            }

        }
    }

    private Attribute createAttribute(Serializable value) {
        return new AttributeImpl(attributeName, value);
    }

    private void setAttribute(Metacard parent, Metacard child) {
        parent.setAttribute(createAttribute(child.getAttribute(attributeName)
                .getValue()));
    }

    private void setAttribute(Metacard parent, LineString lineString) {
        WKTWriter wktWriter = new WKTWriter();
        parent.setAttribute(createAttribute(wktWriter.write(lineString.norm())));
    }

    private Coordinate[] getMergedCoordinates(Optional<Geometry> parentGeo,
            Optional<Geometry> childGeo) {
        return ArrayUtils.addAll(parentGeo.get()
                        .getCoordinates(),
                childGeo.get()
                        .getCoordinates());
    }

    private String getValue(Metacard metacard) {
        return (String) metacard.getAttribute(attributeName)
                .getValue();
    }

    private boolean hasFrameCenter(Metacard metacard) {
        return metacard.getAttribute(attributeName) != null && metacard.getAttribute(attributeName)
                .getValue() instanceof String;
    }

    private LineString convertCoordinatesToLineString(Coordinate[] coordinates) {
        return new GeometryFactory().createLineString(coordinates);
    }
}
