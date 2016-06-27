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
package org.codice.alliance.transformer.video;

import org.codice.alliance.libs.klv.CopyPresentKlvProcessor;
import org.codice.alliance.libs.klv.DistinctKlvProcessor;
import org.codice.alliance.libs.klv.FrameCenterKlvProcessor;
import org.codice.alliance.libs.klv.GeometryOperator;
import org.codice.alliance.libs.klv.GeometryReducer;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.LocationKlvProcessor;
import org.codice.alliance.libs.klv.NormalizeGeometry;
import org.codice.alliance.libs.klv.SetDatesKlvProcessor;
import org.codice.alliance.libs.klv.SimplifyGeometryFunction;

/**
 * Call {@link SimplifyGeometryFunction#setDistanceTolerance(Double)} that is embedded within a
 * KlvProcessor.
 */
class SetDistanceToleranceVisitor implements KlvProcessor.Visitor {

    private final Double distanceTolerance;

    private final GeometryOperator.Visitor geometryFunctionVisitor =
            new GeometryOperator.Visitor() {

                @Override
                public void visit(GeometryReducer geometryReducer) {

                }

                @Override
                public void visit(SimplifyGeometryFunction function) {
                    function.setDistanceTolerance(distanceTolerance);
                }

                @Override
                public void visit(NormalizeGeometry function) {

                }
            };

    public SetDistanceToleranceVisitor(Double distanceTolerance) {
        this.distanceTolerance = distanceTolerance;
    }

    @Override
    public void visit(DistinctKlvProcessor distinctKlvProcessor) {

    }

    @Override
    public void visit(CopyPresentKlvProcessor copyPresentKlvProcessor) {

    }

    @Override
    public void visit(FrameCenterKlvProcessor frameCenterKlvProcessor) {
        frameCenterKlvProcessor.getGeometryOperator()
                .accept(geometryFunctionVisitor);
    }

    @Override
    public void visit(LocationKlvProcessor locationKlvProcessor) {
        locationKlvProcessor.getGeometryFunction()
                .accept(geometryFunctionVisitor);
    }

    @Override
    public void visit(SetDatesKlvProcessor setDatesKlvProcessor) {

    }
}
