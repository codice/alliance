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
package org.codice.alliance.transformer.nitf.gmti;

import java.io.Serializable;
import java.util.function.Function;

import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.tre.Tre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public enum MtirpbAttribute implements NitfAttribute<Tre> {
    DESTINATION_POINT("destinationPoint",
            "MTI_DP",
            tre -> GmtiTreUtility.getTreValue(tre, "MTI_DP")),
    PATCH_NUMBER("patchNumber", "PATCH_NO", tre -> GmtiTreUtility.getTreValue(tre, "PATCH_NO")),
    WIDE_AREA_MTI_FRAME_NUMBER("wideAreaMtiFrameNumber",
            "WAMTI_FRAME_NO",
            tre -> GmtiTreUtility.getTreValue(tre, "WAMTI_FRAME_NO")),
    WIDE_AREA_MTI_BAR_NUMBER("wideAreaMtiBarNumber",
            "WAMTI_BAR_NO",
            tre -> GmtiTreUtility.getTreValue(tre, "WAMTI_BAR_NO")),
    SCAN_DATE_AND_TIME("scanDateAndTime",
            "DATIME",
            tre -> GmtiTreUtility.getTreValue(tre, "DATIME")),
    AIRCRAFT_LOCATION("aircraftLocation",
            "ACFT_LOC",
            tre -> GmtiTreUtility.getTreValue(tre, "ACFT_LOC")),
    AIRCRAFT_ALTITUDE("aircraftAltitude",
            "ACFT_ALT",
            tre -> GmtiTreUtility.getTreValue(tre, "ACFT_ALT")),
    AIRCRAFT_ALTITUDE_UNITS("aircraftAltitudeUnitOfMeasure",
            "ACFT_ALT_UNIT",
            tre -> GmtiTreUtility.getTreValue(tre, "ACFT_ALT_UNIT")),
    AIRCRAFT_HEADING("aircraftHeading",
            "ACFT_HEADING",
            tre -> GmtiTreUtility.getTreValue(tre, "ACFT_HEADING")),
    MTI_LR("mtiLeftOrRight", "MTI_LR", tre -> GmtiTreUtility.getTreValue(tre, "MTI_LR")),
    SQUINT_ANGLE("squintAngle",
            "SQUINT_ANGLE",
            tre -> GmtiTreUtility.getTreValue(tre, "SQUINT_ANGLE")),
    COSINE_OF_GRAZE_ANGLE("cosineOfGrazeAngle",
            "COSGRZ",
            tre -> GmtiTreUtility.getTreValue(tre, "COSGRZ")),
    NUMBER_OF_VALID_TARGETS("numberOfValidTargets",
            "NO_VALID_TARGETS",
            tre -> GmtiTreUtility.getTreValue(tre, "NO_VALID_TARGETS"));

    private static final Logger LOGGER = LoggerFactory.getLogger(MtirpbAttribute.class);

    private static final String ATTRIBUTE_NAME_PREFIX = "nitf.mtirpb.";

    private String shortName;

    private String longName;

    private Function<Tre, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    MtirpbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction) {
        this.longName = longName;
        this.shortName = shortName;
        this.accessorFunction = accessorFunction;
        this.attributeDescriptor = new AttributeDescriptorImpl(ATTRIBUTE_NAME_PREFIX + longName,
                true,
                true,
                false,
                true,
                BasicTypes.STRING_TYPE);
    }

    @Override
    public String getLongName() {
        return this.longName;
    }

    @Override
    public String getShortName() {
        return this.shortName;
    }

    @Override
    public Function<Tre, Serializable> getAccessorFunction() {
        return this.accessorFunction;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
