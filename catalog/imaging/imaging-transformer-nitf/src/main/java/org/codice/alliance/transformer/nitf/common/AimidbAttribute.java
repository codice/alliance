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

import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;

/**
 * TRE for "Image Search and Discovery"
 */
public class AimidbAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    private static final String ATTRIBUTE_NAME_PREFIX = "aimidb.";

    /*
     * Non-normalized attributes
     */
    public static final AimidbAttribute ACQUISITION_DATE = new AimidbAttribute("acquisition-date",
            "ACQUISITION_DATE",
            tre -> TreUtility.convertToString(tre, "ACQUISITION_DATE"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute MISSION_NUMBER = new AimidbAttribute("mission-number",
            "MISSION_NO",
            tre -> TreUtility.convertToString(tre, "MISSION_NO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute MISSION_IDENTIFICATION = new AimidbAttribute(
            "mission-identification",
            "MISSION_IDENTIFICATION",
            tre -> TreUtility.convertToString(tre, "MISSION_IDENTIFICATION"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute FLIGHT_NUMBER = new AimidbAttribute("flight-number",
            "FLIGHT_NO",
            tre -> TreUtility.convertToString(tre, "FLIGHT_NO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute OPERATION_NUMBER = new AimidbAttribute(
            "imageOperation-number",
            "OP_NUM",
            tre -> TreUtility.convertToInteger(tre, "OP_NUM"),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute CURRENT_SEGMENT = new AimidbAttribute("current-segment",
            "CURRENT_SEGMENT",
            tre -> TreUtility.convertToString(tre, "CURRENT_SEGMENT"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute REPROCESS_NUMBER = new AimidbAttribute("reprocess-number",
            "REPRO_NUM",
            tre -> TreUtility.convertToInteger(tre, "REPRO_NUM"),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute REPLAY = new AimidbAttribute("replay",
            "REPLAY",
            tre -> TreUtility.convertToString(tre, "REPLAY"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute START_TILE_COLUMN = new AimidbAttribute("start-tile-column",
            "START_TILE_COLUMN",
            tre -> TreUtility.convertToInteger(tre, "START_TILE_COLUMN"),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute START_TILE_ROW = new AimidbAttribute("start-tile-row",
            "START_TILE_ROW",
            tre -> TreUtility.convertToInteger(tre, "START_TILE_ROW"),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute END_SEGMENT = new AimidbAttribute("end-segment",
            "END_SEGMENT",
            tre -> TreUtility.convertToString(tre, "END_SEGMENT"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute END_TILE_COLUMN = new AimidbAttribute("end-tile-column",
            "END_TILE_COLUMN",
            tre -> TreUtility.convertToInteger(tre, "END_TILE_COLUMN"),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute END_TILE_ROW = new AimidbAttribute("end-tile-row",
            "END_TILE_ROW",
            tre -> TreUtility.convertToInteger(tre, "END_TILE_ROW"),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute COUNTRY = new AimidbAttribute("country-code",
            "COUNTRY",
            tre -> TreUtility.convertToString(tre, "COUNTRY"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final AimidbAttribute LOCATION = new AimidbAttribute("location",
            "LOCATION",
            tre -> TreUtility.convertToString(tre, "LOCATION"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private AimidbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }
}