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

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

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
     * @param geometryOperator transform the Geometry object (e.g. simplify) (must be non-null)
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

        String wkt = GeometryUtility.attributeToLineString(attribute, geometryOperator);

        setAttribute(metacard, wkt);
    }

    private void setAttribute(Metacard metacard, String wkt) {
        metacard.setAttribute(new AttributeImpl(ATTRIBUTE_NAME, wkt));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
