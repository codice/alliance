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

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * The method {@link Context#getDistanceTolerance()} should return a non-null value if the
 * simplification should include a distance tolerance. See {@link TopologyPreservingSimplifier#simplify(Geometry, double)}.
 * If a distance tolerance is not set, then {@link TopologyPreservingSimplifier#TopologyPreservingSimplifier(Geometry)} will
 * be used.
 */
@ThreadSafe
public class SimplifyGeometryFunction implements GeometryOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyGeometryFunction.class);

    @Override
    public Geometry apply(Geometry geometry, Context context) {
        if (geometry == null) {
            return null;
        }

        if(geometry.isEmpty()) {
            return geometry;
        }

        LOGGER.debug("simplifying geometry: {}", geometry);

        Double distanceTolerance = context.getDistanceTolerance();

        Geometry simplifiedGeometry;
        if (distanceTolerance != null) {
            simplifiedGeometry = TopologyPreservingSimplifier.simplify(geometry, distanceTolerance);
        } else {
            simplifiedGeometry = new TopologyPreservingSimplifier(geometry).getResultGeometry();
        }

        LOGGER.debug("old coord count={} new coord count={}",
                geometry.getCoordinates().length,
                simplifiedGeometry.getCoordinates().length);

        return simplifiedGeometry;
    }

    @Override
    public String toString() {
        return "SimplifyGeometryFunction{}";
    }

}
