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
package org.codice.alliance.transformer.nitf.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;

public class AcftbAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    private static final String ATTRIBUTE_NAME_PREFIX = "acftb.";

    /*
     * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.acftb.*` when
     * appropriate
     */

    public static final AcftbAttribute AIRCRAFT_MISSION_ID = new AcftbAttribute(Isr.MISSION_ID,
            "AC_MSN_ID",
            tre -> TreUtility.getTreValue(tre, "AC_MSN_ID"),
            new IsrAttributes().getAttributeDescriptor(Isr.MISSION_ID),
            "aircraft-mission-id",
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute AIRCRAFT_TAIL_NUMBER = new AcftbAttribute(Isr.PLATFORM_ID,
            "AC_TAIL_NO",
            tre -> TreUtility.getTreValue(tre, "AC_TAIL_NO"),
            new IsrAttributes().getAttributeDescriptor(Isr.PLATFORM_ID),
            "aircraft-tail-number",
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute SENSOR_ID_TYPE = new AcftbAttribute(Isr.SENSOR_TYPE,
            "SENSOR_ID_TYPE",
            tre -> TreUtility.getTreValue(tre, "SENSOR_ID_TYPE"),
            new IsrAttributes().getAttributeDescriptor(Isr.SENSOR_TYPE),
            "sensor-id-type",
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute SENSOR_ID = new AcftbAttribute(Isr.SENSOR_ID,
            "SENSOR_ID",
            tre -> TreUtility.getTreValue(tre, "SENSOR_ID"),
            new IsrAttributes().getAttributeDescriptor(Isr.SENSOR_ID),
            "sensor-id",
            ATTRIBUTE_NAME_PREFIX);

    /*
     * Non-normalized attributes
     */

    public static final AcftbAttribute AIRCRAFT_TAKEOFF = new AcftbAttribute("aircraft-take-off",
            "AC_TO",
            tre -> TreUtility.getTreValue(tre, "AC_TO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute SCENE_SOURCE = new AcftbAttribute("scene-source",
            "SCENE_SOURCE",
            tre -> TreUtility.getTreValue(tre, "SCENE_SOURCE"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute SCENE_NUMBER = new AcftbAttribute("scene-number",
            "SCNUM",
            tre -> TreUtility.getTreValue(tre, "SCNUM"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute PROCESSING_DATE = new AcftbAttribute("processing-date",
            "PDATE",
            tre -> TreUtility.getTreValue(tre, "PDATE"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute IMMEDIATE_SCENE_HOST = new AcftbAttribute(
            "immediate-scene-host",
            "IMHOSTNO",
            tre -> TreUtility.getTreValue(tre, "IMHOSTNO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute IMMEDIATE_SCENE_REQUEST_ID = new AcftbAttribute(
            "immediate-scene-request-id",
            "IMREQID",
            tre -> TreUtility.getTreValue(tre, "IMREQID"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AcftbAttribute MISSION_PLAN_MODE = new AcftbAttribute("mission-plan-mode",
            "MPLAN",
            tre -> TreUtility.getTreValue(tre, "MPLAN"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private AcftbAttribute(final String longName, final String shortName,
            final Function<Tre, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private AcftbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
