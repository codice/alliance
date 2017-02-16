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

import java.util.stream.IntStream;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Convert the subpolygons in a geometry to envelopes. If the geometry only contains one
 * geometry, then return the original geometry.
 */
public class ConvertSubpolygonsToEnvelopes implements GeometryOperator {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Geometry apply(Geometry geometry) {

        if (geometry.getNumGeometries() == 1) {
            return geometry;
        }

        return IntStream.range(0, geometry.getNumGeometries())
                .mapToObj(geometry::getGeometryN)
                .map(Geometry::getEnvelope)
                .reduce(Geometry::union)
                .orElse(geometry);
    }
}
