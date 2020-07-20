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
package org.codice.alliance.catalog.core.api.impl.types;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import java.util.HashSet;
import java.util.Set;
import org.codice.alliance.catalog.core.api.types.Dod520001;

/**
 * <b> This code is experimental. While this is functional and tested, it may change or be removed
 * in a future version of the library. </b>
 */
public class Dod520001Attributes implements Dod520001, MetacardType {
  private static final Set<AttributeDescriptor> DESCRIPTORS = new HashSet<>();

  private static final String NAME = "dod520001";

  static {
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            Dod520001.SECURITY_DOD5200_AEA,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            Dod520001.SECURITY_DOD5200_DODUCNI,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            Dod520001.SECURITY_DOD5200_DOEUCNI,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            Dod520001.SECURITY_DOD5200_FGI,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            Dod520001.SECURITY_DOD5200_OTHER_DISSEM,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            Dod520001.SECURITY_DOD5200_SAP,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
  }

  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public AttributeDescriptor getAttributeDescriptor(String name) {
    return DESCRIPTORS.stream()
        .filter(attr -> attr.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
