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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class SimplifyGeometryFunction implements GeometryFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyGeometryFunction.class);

    private Optional<Double> distanceTolerance;

    public SimplifyGeometryFunction(double distanceTolerance) {
        this.distanceTolerance = Optional.of(distanceTolerance);
    }

    public SimplifyGeometryFunction() {
        this.distanceTolerance = Optional.empty();
    }

    public Optional<Double> getDistanceTolerance() {
        return distanceTolerance;
    }

    public void setDistanceTolerance(Double distanceTolerance) {
        this.distanceTolerance = Optional.ofNullable(distanceTolerance);
    }

    @Override
    public Geometry apply(Geometry geometry) {
        if (geometry == null) {
            return null;
        }

        LOGGER.debug("simplifying geometry: {}", geometry);

        Geometry simplifiedGeometry;
        if (distanceTolerance.isPresent()) {
            simplifiedGeometry = TopologyPreservingSimplifier.simplify(geometry,
                    distanceTolerance.get());
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
        return "SimplifyGeometryFunction{" +
                "distanceTolerance=" + distanceTolerance +
                '}';
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
