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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;

public class MtirpbAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    private static final String ATTRIBUTE_NAME_PREFIX = "mtirpb.";

    /*
     * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.mtirpb.*` when
     * appropriate
     */

    public static final MtirpbAttribute NUMBER_OF_VALID_TARGETS =
            new MtirpbAttribute(Isr.TARGET_REPORT_COUNT,
                    "NO_VALID_TARGETS",
                    tre -> TreUtility.getTreValue(tre, "NO_VALID_TARGETS"),
                    new IsrAttributes().getAttributeDescriptor(Isr.TARGET_REPORT_COUNT),
                    "number-of-valid-targets",
                    ATTRIBUTE_NAME_PREFIX);

    /*
     * Non-normalized attributes
     */

    public static final MtirpbAttribute AIRCRAFT_LOCATION = new MtirpbAttribute("aircraft-location",
            "ACFT_LOC",
            tre -> TreUtility.getTreValue(tre, "ACFT_LOC"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute AIRCRAFT_ALTITUDE = new MtirpbAttribute("aircraft-altitude",
            "ACFT_ALT",
            tre -> TreUtility.getTreValue(tre, "ACFT_ALT"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute AIRCRAFT_ALTITUDE_UNITS = new MtirpbAttribute(
            "aircraft-altitude-unit-of-measure",
            "ACFT_ALT_UNIT",
            tre -> TreUtility.getTreValue(tre, "ACFT_ALT_UNIT"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute AIRCRAFT_HEADING = new MtirpbAttribute("aircraft-heading",
            "ACFT_HEADING",
            tre -> TreUtility.getTreValue(tre, "ACFT_HEADING"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute COSINE_OF_GRAZE_ANGLE = new MtirpbAttribute(
            "cosine-of-graze-angle",
            "COSGRZ",
            tre -> TreUtility.getTreValue(tre, "COSGRZ"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute DESTINATION_POINT = new MtirpbAttribute("destination-point",
            "MTI_DP",
            tre -> TreUtility.getTreValue(tre, "MTI_DP"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute MTI_LR = new MtirpbAttribute("mti-left-or-right",
            "MTI_LR",
            tre -> TreUtility.getTreValue(tre, "MTI_LR"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute PATCH_NUMBER = new MtirpbAttribute("patch-number",
            "PATCH_NO",
            tre -> TreUtility.getTreValue(tre, "PATCH_NO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute SCAN_DATE_AND_TIME = new MtirpbAttribute("scan-date-and-time",
            "DATIME",
            tre -> TreUtility.getTreValue(tre, "DATIME"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute SQUINT_ANGLE = new MtirpbAttribute("squint-angle",
            "SQUINT_ANGLE",
            tre -> TreUtility.getTreValue(tre, "SQUINT_ANGLE"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute WIDE_AREA_MTI_FRAME_NUMBER = new MtirpbAttribute(
            "wide-area-mti-frame-number",
            "WAMTI_FRAME_NO",
            tre -> TreUtility.getTreValue(tre, "WAMTI_FRAME_NO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final MtirpbAttribute WIDE_AREA_MTI_BAR_NUMBER = new MtirpbAttribute(
            "wide-area-mti-bar-number",
            "WAMTI_BAR_NO",
            tre -> TreUtility.getTreValue(tre, "WAMTI_BAR_NO"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private MtirpbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private MtirpbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeDescriptor attributeDescriptor,
            String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
