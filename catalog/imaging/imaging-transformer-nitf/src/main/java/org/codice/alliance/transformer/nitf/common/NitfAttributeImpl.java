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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;

public class NitfAttributeImpl<T> implements NitfAttribute<T> {

    private final String shortName;

    private final String longName;

    private final Function<T, Serializable> accessorFunction;

    private Set<AttributeDescriptor> attributeDescriptors;

    protected NitfAttributeImpl(String longName, String shortName,
            Function<T, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        this.shortName = shortName;
        this.longName = longName;
        this.accessorFunction = accessorFunction;
        // retrieving metacard attribute descriptor for this attribute to prevent later lookups
        this.attributeDescriptors = Collections.singleton(new AttributeDescriptorImpl(
                ExtNitfUtility.EXT_NITF_PREFIX + prefix + longName, true, /* indexed */
                true, /* stored */
                false, /* tokenized */
                true, /* multivalued */
                attributeType));
    }

    protected NitfAttributeImpl(final String longName, final String shortName,
            final Function<T, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        this.shortName = shortName;
        this.longName = longName;
        this.accessorFunction = accessorFunction;
        this.attributeDescriptors = new HashSet<>();
        this.attributeDescriptors.add(attributeDescriptor);
        if (StringUtils.isNotEmpty(extNitfName)) {
            this.attributeDescriptors.add(ExtNitfUtility.createDuplicateDescriptorAndRename(
                    prefix + extNitfName, attributeDescriptor));
        }
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
    public Function<T, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    @Override
    public Set<AttributeDescriptor> getAttributeDescriptors() {
        return attributeDescriptors;
    }

    @Override
    public String toString() {
        return getLongName();
    }
}
