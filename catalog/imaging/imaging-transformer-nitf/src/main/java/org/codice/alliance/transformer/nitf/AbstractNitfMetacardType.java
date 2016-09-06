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
package org.codice.alliance.transformer.nitf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codice.alliance.transformer.nitf.common.NitfAttribute;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.MetacardTypeImpl;

public abstract class AbstractNitfMetacardType extends MetacardTypeImpl {

    public AbstractNitfMetacardType(String name, Set<AttributeDescriptor> descriptors) {
        super(name, descriptors);
    }

    public abstract void initDescriptors();

    protected Set<AttributeDescriptor> getDescriptors(NitfAttribute[] attributes) {
        Set<AttributeDescriptor> descriptors = new HashSet<>();
        for (NitfAttribute attribute : attributes) {
            descriptors.addAll(attribute.getAttributeDescriptors());
        }
        return descriptors;
    }

    protected <T> Set<AttributeDescriptor> getDescriptors(List<NitfAttribute<T>> attributes) {
        return getDescriptors(attributes.toArray(new NitfAttribute[attributes.size()]));
    }

}
