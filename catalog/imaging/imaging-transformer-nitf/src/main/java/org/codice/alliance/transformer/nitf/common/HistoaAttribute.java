/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
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

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;

/**
 * TRE for "Softcopy History Tagged Record Extension"
 */
public class HistoaAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    static final String SYSTYPE_NAME = "SYSTYPE";

    static final String PC_NAME = "PC";

    static final String PE_NAME = "PE";

    static final String REMAP_FLAG_NAME = "REMAP_FLAG";

    static final String LUTID_NAME = "LUTID";

    private static final String ATTRIBUTE_NAME_PREFIX = "histoa.";

    static final HistoaAttribute SYSTYPE = new HistoaAttribute("system-type",
            SYSTYPE_NAME,
            tre -> TreUtility.convertToString(tre, SYSTYPE_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final HistoaAttribute PC = new HistoaAttribute("prior-compression",
            PC_NAME,
            tre -> TreUtility.convertToString(tre, PC_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final HistoaAttribute PE = new HistoaAttribute("prior-enhancements",
            PE_NAME,
            tre -> TreUtility.convertToString(tre, PE_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final HistoaAttribute REMAP_FLAG = new HistoaAttribute("system-specific-remap",
            REMAP_FLAG_NAME,
            tre -> TreUtility.convertToString(tre, REMAP_FLAG_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final HistoaAttribute LUTID = new HistoaAttribute("data-mapping-id",
            LUTID_NAME,
            tre -> TreUtility.convertToInteger(tre, LUTID_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private HistoaAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private HistoaAttribute(final String longName, final String shortName,
            final Function<Tre, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
