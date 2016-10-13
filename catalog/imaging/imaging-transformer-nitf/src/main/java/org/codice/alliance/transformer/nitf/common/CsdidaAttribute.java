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
 * TRE for "Dataset Identification"
 */
class CsdidaAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    public static final String PLATFORM_CODE = "PLATFORM_CODE";

    public static final String VEHICLE_ID = "VEHICLE_ID";

    public static final CsdidaAttribute PLATFORM_ID = new CsdidaAttribute(Isr.PLATFORM_ID,
            "PLATFORM_CODE_VEHICLE_ID",
            CsdidaAttribute::getPlatformIdFunction,
            new IsrAttributes().getAttributeDescriptor(Isr.PLATFORM_ID),
            "platformCodeVehicleId",
            "csdida.");

    private CsdidaAttribute(final String longName, final String shortName,
            final Function<Tre, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);

        ATTRIBUTES.add(this);
    }

    private static Serializable getPlatformIdFunction(Tre tre) {
        Optional<String> platformCode = Optional.ofNullable(TreUtility.getTreValue(tre,
                PLATFORM_CODE))
                .filter(String.class::isInstance)
                .map(String.class::cast);
        Optional<Serializable> vehicleId = Optional.ofNullable(TreUtility.getTreValue(tre, VEHICLE_ID));

        if (platformCode.isPresent() && vehicleId.isPresent()) {
            return String.format("%s%s", platformCode.get(), vehicleId.get());
        }

        return null;
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
