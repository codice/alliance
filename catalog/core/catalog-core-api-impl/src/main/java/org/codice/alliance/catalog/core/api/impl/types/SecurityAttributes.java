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
import org.codice.alliance.catalog.core.api.types.Security;

/** This class provides attributes that relate to security of the resource and metadata */
public class SecurityAttributes implements Security, MetacardType {
  private static final Set<AttributeDescriptor> DESCRIPTORS = new HashSet<>();

  private static final String NAME = "security";

  static {
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CLASSIFICATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            RELEASABILITY,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CLASSIFICATION_SYSTEM,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            OWNER_PRODUCER,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            DISSEMINATION_CONTROLS,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            OTHER_DISSEMINATION_CONTROLS,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CODEWORDS,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

    /* Experimental Attributes, subject to change*/
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            METADATA_ORIGINATOR_CLASSIFICATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            METADATA_CLASSIFICATION_SYSTEM,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            METADATA_CLASSIFICATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            METADATA_DISSEMINATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            METADATA_RELEASABILITY,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            RESOURCE_ORIGINATOR_CLASSIFICATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            RESOURCE_CLASSIFICATION_SYSTEM,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            RESOURCE_CLASSIFICATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            RESOURCE_RELEASABILITY,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            RESOURCE_DISSEMINATION,
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
