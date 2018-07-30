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
package org.codice.alliance.transformer.nitf;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;

/**
 * Utility class for holding common elements and creating NITF {@link AttributeDescriptor}s that do
 * not map to the taxonomy.
 */
public class ExtNitfUtility {

  public static final String EXT_NITF_PREFIX = "ext.nitf.";

  private ExtNitfUtility() {}

  public static AttributeDescriptor createDuplicateDescriptorAndRename(
      String newName, AttributeDescriptor duplicatedDescriptor) {
    return new AttributeDescriptorImpl(
        newName,
        duplicatedDescriptor.isIndexed(),
        duplicatedDescriptor.isStored(),
        duplicatedDescriptor.isTokenized(),
        duplicatedDescriptor.isMultiValued(),
        duplicatedDescriptor.getType());
  }

  public static boolean isExtAttribute(NitfAttribute nitfAttribute) {
    return nitfAttribute.getLongName().startsWith(EXT_NITF_PREFIX);
  }
}
