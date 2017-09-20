/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.libs.klv;

import static org.apache.commons.lang.Validate.notNull;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utilities {
  private static final Logger LOGGER = LoggerFactory.getLogger(Utilities.class);

  public static boolean isEmptyString(Serializable serializable) {
    return serializable instanceof String && StringUtils.isEmpty((String) serializable);
  }

  public static boolean isNotEmptyString(Serializable serializable) {
    return !isEmptyString(serializable);
  }

  static void safelySetAttribute(Metacard metacard, Attribute attribute) {
    notNull(attribute, "Attribute cannot be null");
    safelySetAttribute(metacard, attribute.getName(), attribute.getValues());
  }

  static void safelySetAttribute(Metacard metacard, String attributeName, Serializable value) {
    notNull(metacard, "Metacard cannot be null");
    notNull(attributeName, "Attribute name cannot be null");
    notNull(value, "Serializable attribute value cannot be null");
    AttributeDescriptor descriptor =
        metacard.getMetacardType().getAttributeDescriptor(attributeName);
    if (descriptor != null) {
      if (descriptor.getType().getBinding().isAssignableFrom(value.getClass())) {
        metacard.setAttribute(new AttributeImpl(attributeName, value));
      } else {
        LOGGER.debug(
            "Attempt to safely set attribute failed; value not assignable to {}",
            descriptor.getType().getBinding().getName());
      }
    } else {
      LOGGER.debug("Attribute descriptor was null for attribute name: {}", attributeName);
    }
  }

  static void safelySetAttribute(
      Metacard metacard, String attributeName, List<Serializable> valueList) {
    notNull(metacard, "Metacard cannot be null");
    notNull(attributeName, "Attribute name cannot be null");
    notNull(valueList, "List of serializable attribute values cannot be null");
    AttributeDescriptor descriptor =
        metacard.getMetacardType().getAttributeDescriptor(attributeName);
    if (descriptor != null) {
      if (valueList
          .stream()
          .filter(serializable -> serializable != null)
          .allMatch(
              serializable ->
                  descriptor.getType().getBinding().isAssignableFrom(serializable.getClass()))) {
        metacard.setAttribute(new AttributeImpl(attributeName, valueList));
      } else {
        LOGGER.debug(
            "Attempt to safely set attribute failed; value not assignable to {}",
            descriptor.getType().getBinding().getName());
      }
    } else {
      LOGGER.debug("Attribute descriptor was null for attribute name: {}", attributeName);
    }
  }
}
