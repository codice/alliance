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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;

class GeometryUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryUtility.class);

    /**
     * Create the union of multi-valued attribute that contains WKT. If the union cannot
     * be computed, then this method returns {@link Optional#empty()}
     *
     * @param wktReader non-null
     * @param wktWriter non-null
     * @param attribute non-null
     * @return optional wkt string
     */
    public static Optional<String> createUnionOfGeometryAttribute(WKTReader wktReader,
            WKTWriter wktWriter, Attribute attribute) {
        return attribute.getValues()
                .stream()
                .filter(serializable -> serializable instanceof String)
                .map(s -> (String) s)
                .map(wkt -> wktToGeometry(wkt, wktReader))
                .reduce(GeometryUtility::geometryUnion)
                .orElse(Optional.<Geometry>empty())
                .map(wktWriter::write);
    }

    private static Optional<Geometry> geometryUnion(Optional<Geometry> geometry1,
            Optional<Geometry> geometry2) {
        if (geometry1.isPresent() && geometry2.isPresent()) {
            return Optional.of(geometry1.get()
                    .union(geometry2.get()));
        } else if (geometry1.isPresent()) {
            return geometry1;
        } else if (geometry2.isPresent()) {
            return geometry2;
        }
        return Optional.empty();
    }

    public static Optional<Geometry> wktToGeometry(String wkt, WKTReader wktReader) {
        try {
            return Optional.of(wktReader.read(wkt));
        } catch (ParseException e) {
            LOGGER.warn("unable to convert WKT to a Geometry object: wkt={}", wkt, e);
        }
        return Optional.empty();
    }

}
