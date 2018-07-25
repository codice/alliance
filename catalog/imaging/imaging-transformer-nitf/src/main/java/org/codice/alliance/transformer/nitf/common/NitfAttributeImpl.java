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
package org.codice.alliance.transformer.nitf.common;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.TreGroup;

public class NitfAttributeImpl<T> implements NitfAttribute<T> {

  private final String shortName;

  private final String longName;

  private final Function<T, Serializable> accessorFunction;

  private final Function<T, Serializable> extAccessorFunction;

  private final Set<AttributeDescriptor> attributeDescriptors;

  private List<NitfAttribute<TreGroup>> indexedAttributes;

  protected NitfAttributeImpl(
      String extNitfName,
      String shortName,
      Function<T, Serializable> accessorFunction,
      AttributeType attributeType) {
    this.shortName = shortName;
    this.longName = extNitfName;
    this.accessorFunction = accessorFunction;
    this.extAccessorFunction = accessorFunction;
    // retrieving metacard attribute descriptor for this attribute to prevent later lookups
    this.attributeDescriptors =
        Collections.singleton(
            new AttributeDescriptorImpl(
                longName,
                true, /* indexed */
                true, /* stored */
                false, /* tokenized */
                true, /* multivalued */
                attributeType));
  }

  protected NitfAttributeImpl(
      final String attributeName,
      final String shortName,
      final Function<T, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    this(
        attributeName,
        shortName,
        accessorFunction,
        accessorFunction,
        attributeDescriptor,
        extNitfName);
  }

  protected NitfAttributeImpl(
      final String attributeName,
      final String shortName,
      final Function<T, Serializable> accessorFunction,
      final Function<T, Serializable> extAccessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    this.shortName = shortName;
    this.longName = attributeName;
    this.accessorFunction = accessorFunction;
    this.extAccessorFunction = extAccessorFunction;
    this.attributeDescriptors = new HashSet<>();
    this.attributeDescriptors.add(attributeDescriptor);
    if (StringUtils.isNotEmpty(extNitfName)) {
      this.attributeDescriptors.add(
          ExtNitfUtility.createDuplicateDescriptorAndRename(extNitfName, attributeDescriptor));
    }
  }

  protected NitfAttributeImpl(
      String extNitfName,
      String shortName,
      Function<T, Serializable> accessorFunction,
      AttributeType attributeType,
      List<NitfAttribute<TreGroup>> indexedAttributes) {
    this(extNitfName, shortName, accessorFunction, attributeType);
    this.indexedAttributes = indexedAttributes;
  }

  /** {@inheritDoc} */
  @Override
  public String getLongName() {
    return longName;
  }

  /** {@inheritDoc} */
  @Override
  public String getShortName() {
    return shortName;
  }

  /** {@inheritDoc} */
  @Override
  public Function<T, Serializable> getAccessorFunction() {
    return accessorFunction;
  }

  /** {@inheritDoc} */
  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return attributeDescriptors;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getLongName();
  }

  public List<NitfAttribute<TreGroup>> getIndexedAttributes() {
    return indexedAttributes;
  }

  /** {@inheritDoc} */
  @Override
  public Function<T, Serializable> getExtAccessorFunction() {
    return extAccessorFunction;
  }
}
