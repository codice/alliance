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

import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.tre.TreGroup;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Core;

public class IndexedMtirpbAttribute extends NitfAttributeImpl<TreGroup> {

    private static final List<NitfAttribute<TreGroup>> ATTRIBUTES = new LinkedList<>();

    private static final String ATTRIBUTE_NAME_PREFIX = "mtirpb.";

    /*
     * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.mtirpb.*` when
     * appropriate
     */

    public static final IndexedMtirpbAttribute INDEXED_TARGET_LOCATION = new IndexedMtirpbAttribute(
            Core.LOCATION,
            "TGT_LOC",
            tre -> TreUtility.getTreValue(tre, "TGT_LOC"),
            new CoreAttributes().getAttributeDescriptor(Core.LOCATION),
            "target-location",
            ATTRIBUTE_NAME_PREFIX);

    /*
     * Non-normalized attributes
     */

    public static final IndexedMtirpbAttribute INDEXED_TARGET_CLASSIFICATION_CATEGORY =
            new IndexedMtirpbAttribute("target-classification-category",
                    "TGT_CAT",
                    IndexedMtirpbAttribute::getClassificationCategory,
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    public static final IndexedMtirpbAttribute INDEXED_TARGET_AMPLITUDE =
            new IndexedMtirpbAttribute("target-amplitude",
                    "TGT_AMPLITUDE",
                    tre -> TreUtility.getTreValue(tre, "TGT_AMPLITUDE"),
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    public static final IndexedMtirpbAttribute INDEXED_TARGET_HEADING = new IndexedMtirpbAttribute(
            "target-heading",
            "TGT_HEADING",
            tre -> TreUtility.getTreValue(tre, "TGT_HEADING"),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    public static final IndexedMtirpbAttribute INDEXED_TARGET_GROUND_SPEED =
            new IndexedMtirpbAttribute("target-ground-speed",
                    "TGT_SPEED",
                    tre -> TreUtility.getTreValue(tre, "TGT_SPEED"),
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    public static final IndexedMtirpbAttribute INDEXED_TARGET_RADIAL_VELOCITY =
            new IndexedMtirpbAttribute("target-radial-velocity",
                    "TGT_VEL_R",
                    tre -> TreUtility.getTreValue(tre, "TGT_VEL_R"),
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    public static final IndexedMtirpbAttribute INDEXED_TARGET_LOCATION_ACCURACY =
            new IndexedMtirpbAttribute("target-location-accuracy",
                    "TGT_LOC_ACCY",
                    tre -> TreUtility.getTreValue(tre, "TGT_LOC_ACCY"),
                    BasicTypes.STRING_TYPE,
                    ATTRIBUTE_NAME_PREFIX);

    private IndexedMtirpbAttribute(String longName, String shortName,
            Function<TreGroup, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private IndexedMtirpbAttribute(String longName, String shortName,
            Function<TreGroup, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private static String getClassificationCategory(TreGroup treGroup) {
        Serializable value = TreUtility.getTreValue(treGroup,
                INDEXED_TARGET_CLASSIFICATION_CATEGORY.getShortName());
        if (value == null) {
            return MtiTargetClassificationCategory.U.getLongName();
        }
        return MtiTargetClassificationCategory.valueOf((String) value)
                .getLongName();
    }

    public static List<NitfAttribute<TreGroup>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }
}