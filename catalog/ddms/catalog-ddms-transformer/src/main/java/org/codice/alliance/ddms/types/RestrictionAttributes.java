/*
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
package org.codice.alliance.ddms.types;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RestrictionAttributes implements MetacardType {
  private static final String NAME = "restrictions";
  private static final Set<AttributeDescriptor> DESCRIPTORS;

  private static final String RESTRICTION_NS = "ext.restrictions.";
  public static final String COPYRIGHT_APPLIES = RESTRICTION_NS + "copyright-applies";
  public static final String INTELLECTUAL_PROPERTY =
      RESTRICTION_NS + "intellectual-property-applies";
  public static final String PRIVACY_ACT = RESTRICTION_NS + "privacy-act-applies";

  static {
    Set<AttributeDescriptor> descriptors = new HashSet<>();

    descriptors.add(
        new AttributeDescriptorImpl(
            INTELLECTUAL_PROPERTY, true, true, false, false, BasicTypes.BOOLEAN_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            PRIVACY_ACT, true, true, false, false, BasicTypes.BOOLEAN_TYPE));
    descriptors.add(
        new AttributeDescriptorImpl(
            COPYRIGHT_APPLIES, true, true, false, false, BasicTypes.BOOLEAN_TYPE));

    DESCRIPTORS = Collections.unmodifiableSet(descriptors);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public AttributeDescriptor getAttributeDescriptor(String attributeName) {
    return DESCRIPTORS
        .stream()
        .filter(attr -> attr.getName().equals(attributeName))
        .findFirst()
        .orElse(null);
  }
}
