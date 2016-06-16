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

import java.io.Serializable;
import java.util.function.Function;

import ddf.catalog.data.AttributeDescriptor;

/**
 * An interface to provide access to common properties of NITF metacard segment attributes.
 * The accessor function is used to extract an attribute value from the CommonNitfSegment that
 * contains it.  This interface allows the transformer to access NitfAttributes in a
 * generic way.
 *
 * @param <T> The type of CommonNitfSegment that implementations of NitfAttribute represent.
 */
interface NitfAttribute<T> {
    /**
     *
     * @return the attribute's long name in CamelCase.
     */
    String getLongName();

    /**
     *
     * @return the attribute's short name as listed in MIL-STD-2500C.
     */
    String getShortName();

    /**
     *
     * @return a function that, given the CommonNitfSegment of type T,
     *         will return the corresponding value for the NitfAttribute.
     */
    Function<T, Serializable> getAccessorFunction();

    /**
     *
     * @return an AttributeDescriptor for this attribute.
     */
    AttributeDescriptor getAttributeDescriptor();
}
