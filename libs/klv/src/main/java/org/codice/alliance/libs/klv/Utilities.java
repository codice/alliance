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
package org.codice.alliance.libs.klv;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class Utilities {
    public static boolean isEmptyString(Serializable serializable) {
        return serializable instanceof String && StringUtils.isEmpty((String) serializable);
    }

    public static boolean isNotEmptyString(Serializable serializable) {
        return !isEmptyString(serializable);
    }

    static void safelySetAttribute(Metacard metacard, Attribute attribute) {
        safelySetAttribute(metacard, attribute.getName(), attribute.getValues());
    }

    static void safelySetAttribute(Metacard metacard, String attributeName, Serializable value) {
        AttributeDescriptor descriptor = metacard.getMetacardType()
                .getAttributeDescriptor(attributeName);
        if (descriptor != null && descriptor.getType()
                .getBinding()
                .isAssignableFrom(value.getClass())) {
            metacard.setAttribute(new AttributeImpl(attributeName, value));
        }
    }

    static void safelySetAttribute(Metacard metacard, String attributeName,
            List<Serializable> valueList) {
        AttributeDescriptor descriptor = metacard.getMetacardType()
                .getAttributeDescriptor(attributeName);
        if (descriptor != null && valueList.stream()
                .allMatch(serializable -> descriptor.getType()
                        .getBinding()
                        .isAssignableFrom(serializable.getClass()))) {
            metacard.setAttribute(new AttributeImpl(attributeName, valueList));
        }
    }
}
