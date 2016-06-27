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

import java.util.List;
import java.util.function.UnaryOperator;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryOperatorList implements GeometryOperator {

    private final List<GeometryOperator> operatorList;

    /**
     * @param operatorList list of functions (must be non-null)
     */
    public GeometryOperatorList(List<GeometryOperator> operatorList) {
        this.operatorList = operatorList;
    }

    @Override
    public Geometry apply(Geometry t) {
        if (t == null) {
            return null;
        }
        Geometry tmp = t;
        for (UnaryOperator<Geometry> function : operatorList) {
            tmp = function.apply(tmp);
        }
        return tmp;
    }

    @Override
    public String toString() {
        return "GeometryOperator{" +
                "operatorList=" + operatorList +
                '}';
    }

    @Override
    public void accept(Visitor visitor) {
        for (GeometryOperator geometryFunction : operatorList) {
            geometryFunction.accept(visitor);
        }
    }
}



