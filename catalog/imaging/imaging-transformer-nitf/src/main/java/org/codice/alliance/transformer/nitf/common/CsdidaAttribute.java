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
import java.util.Optional;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;

enum CsdidaAttribute implements NitfAttribute<Tre> {

    PLATFORM_ID(Isr.PLATFORM_ID, "PLATFORM_CODE_VEHICLE_ID", tre -> {
        Optional<String> platformCode = Optional.ofNullable(TreUtility.getTreValue(tre,
                Constants.PLATFORM_CODE))
                .filter(String.class::isInstance)
                .map(String.class::cast);
        Optional<Integer> vehicleId = TreUtility.findIntValue(tre, Constants.VEHICLE_ID);

        if (platformCode.isPresent() && vehicleId.isPresent()) {
            return platformCode.get() + String.format("%02d", vehicleId.get());
        }

        return null;
    }, new IsrAttributes());

    private String shortName;

    private String longName;

    private Function<Tre, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    CsdidaAttribute(String longName, String shortName, Function<Tre, Serializable> accessorFunction,
            MetacardType metacardType) {
        this.longName = longName;
        this.shortName = shortName;
        this.accessorFunction = accessorFunction;
        // retrieving metacard attribute descriptor for this attribute to prevent later lookups
        this.attributeDescriptor = metacardType.getAttributeDescriptor(longName);
    }

    @Override
    public String getLongName() {
        return longName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public Function<Tre, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return attributeDescriptor;
    }
}
