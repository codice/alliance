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
import java.util.Optional;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;

/**
 * TRE for "Profile for Imagery Access Image"
 */
class PiaimcAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();
    
    public static final String CLOUDCVR_NAME = "CLOUDCVR";

    public static final PiaimcAttribute CLOUDCVR = new PiaimcAttribute(Isr.CLOUD_COVER,
            CLOUDCVR_NAME,
            PiaimcAttribute::getCloudCoverFunction,
            new IsrAttributes().getAttributeDescriptor(Isr.CLOUD_COVER),
            "cloudCvr",
            "piaimc.");

    private PiaimcAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeDescriptor attributeDescriptor,
            String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private static Serializable getCloudCoverFunction(Tre tre) {
        return Optional.ofNullable(TreUtility.getTreValue(tre, CLOUDCVR_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(Integer::valueOf)
                .filter(value -> value >= 0)
                .filter(value -> value <= 100)
                .orElse(null);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
