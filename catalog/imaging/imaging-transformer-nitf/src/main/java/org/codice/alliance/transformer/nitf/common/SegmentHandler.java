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
import java.util.function.Function;
import java.util.stream.Stream;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.BasicTypes;

public class SegmentHandler {

    protected <T> void handleSegmentHeader(Metacard metacard, T segment,
            NitfAttribute[] attributes) {

        Stream.of(attributes)
                .forEach(attribute -> handleValue(metacard, attribute, segment));
    }

    private <T> void handleValue(Metacard metacard, NitfAttribute attribute, T segment) {
        Function<T, Serializable> accessor = attribute.getAccessorFunction();
        Serializable value = accessor.apply(segment);
        AttributeDescriptor descriptor = attribute.getAttributeDescriptor();

        if (descriptor.getType()
                .equals(BasicTypes.STRING_TYPE) &&
                value != null && ((String) value).length() == 0) {
            value = null;
        }

        if (value != null) {
            Attribute catalogAttribute = populateAttribute(metacard, descriptor.getName(), value);
            metacard.setAttribute(catalogAttribute);
        }
    }

    private Attribute populateAttribute(Metacard metacard, String attributeName,
            Serializable value) {
        Attribute currentAttribute = metacard.getAttribute(attributeName);

        if (currentAttribute == null) {
            currentAttribute = new AttributeImpl(attributeName, value);
        } else {
            AttributeImpl newAttribute = new AttributeImpl(currentAttribute);
            newAttribute.addValue(value);
            currentAttribute = newAttribute;
        }

        return currentAttribute;
    }
}
