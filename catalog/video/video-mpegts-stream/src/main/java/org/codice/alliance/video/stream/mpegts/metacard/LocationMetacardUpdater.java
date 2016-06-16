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
package org.codice.alliance.video.stream.mpegts.metacard;

import java.util.Optional;

import org.codice.alliance.libs.klv.GeometryUtility;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class LocationMetacardUpdater implements MetacardUpdater {

    @Override
    public void update(Metacard parent, Metacard child) {
        if (parent.getLocation() == null) {
            setParentLocation(parent, child.getLocation());
        } else if (child.getLocation() != null) {
            locationUnion(parent, child).ifPresent(wkt -> setParentLocation(parent, wkt));
        }
    }

    private void setParentLocation(Metacard parent, String location) {
        parent.setAttribute(new AttributeImpl(Metacard.GEOGRAPHY, location));
    }

    private Optional<String> locationUnion(Metacard metacard1, Metacard metacard2) {

        WKTReader wktReader = new WKTReader();

        Optional<Geometry> parentGeometry = GeometryUtility.wktToGeometry(metacard1.getLocation(),
                wktReader);
        Optional<Geometry> childGeometry = GeometryUtility.wktToGeometry(metacard2.getLocation(),
                wktReader);

        if (parentGeometry.isPresent() && childGeometry.isPresent()) {
            return Optional.of(new WKTWriter().write(parentGeometry.get()
                    .union(childGeometry.get())));
        }

        return Optional.empty();
    }

}
