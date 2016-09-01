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
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.tre.TreGroup;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Core;

enum IndexedMtirpbAttribute implements NitfAttribute<TreGroup> {
    INDEXED_TARGET_LOCATION(Core.LOCATION,
            "TGT_LOC",
            tre -> TreUtility.getTreValue(tre, "TGT_LOC"),
            new CoreAttributes());

    private String shortName;

    private String longName;

    private Function<TreGroup, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    IndexedMtirpbAttribute(String longName, String shortName,
            Function<TreGroup, Serializable> accessorFunction, MetacardType metacardType) {
        this.longName = longName;
        this.shortName = shortName;
        this.accessorFunction = accessorFunction;
        // retrieving metacard attribute descriptor for this attribute to prevent later lookups
        this.attributeDescriptor = metacardType.getAttributeDescriptor(longName);
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
    public Function<TreGroup, Serializable> getAccessorFunction() {
        return this.accessorFunction;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
