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
import ddf.catalog.data.impl.types.AssociationsAttributes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.impl.types.SecurityAttributes;
import ddf.catalog.data.impl.types.ValidationAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;

public class Ddms20MetacardType implements MetacardType {
  private static final String NAME = "ddms20.metacard";

  private static final Set<AttributeDescriptor> DESCRIPTORS;

  public static final String IC_HIGH_WATER_MARK = "icism.security.high-water-mark";

  public static final String INFO_CUT_OFF = "ext.ddms.info-cut-off";

  public static final String POSTED = "ext.ddms.posted";

  public static final String IDENTIFIER = "ext.ddms.identifier";

  public static final String SECURITY = "ext.ddms.security";

  public static final String LOCATION_NAME = "ext.ddms.geographic-name";

  public static final String LOCATION_REGION = "ext.ddms.geographic-region";

  public static final String BE_NUMBER = "ext.ddms.facility-be-number";

  public static final String RELATED_RESOURCE = "ext.ddms.related-resource";

  public static final String SUBTITLE = "ext.ddms.subtitle";

  public static final String TOPIC_TYPE = "ext.ddms.topic-type";

  public static final String METADATA_NEUTRALITY_FIELDS = "ddms.metadata-neutrality-fields";

  static {
    Set<AttributeDescriptor> descriptors = new HashSet<>();

    descriptors.addAll(new CoreAttributes().getAttributeDescriptors());
    descriptors.addAll(new AssociationsAttributes().getAttributeDescriptors());
    descriptors.addAll(new ContactAttributes().getAttributeDescriptors());
    descriptors.addAll(new MediaAttributes().getAttributeDescriptors());
    descriptors.addAll(new DateTimeAttributes().getAttributeDescriptors());
    descriptors.addAll(new LocationAttributes().getAttributeDescriptors());
    descriptors.addAll(new ValidationAttributes().getAttributeDescriptors());
    descriptors.addAll(new IsrAttributes().getAttributeDescriptors());
    descriptors.addAll(new SecurityAttributes().getAttributeDescriptors());
    descriptors.addAll(new RestrictionAttributes().getAttributeDescriptors());

    descriptors.add(
        new AttributeDescriptorImpl(
            IC_HIGH_WATER_MARK, false, false, false, true, BasicTypes.OBJECT_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            INFO_CUT_OFF,
            true /* indexed */,
            false /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.DATE_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            POSTED,
            true /* indexed */,
            false /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.DATE_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            IDENTIFIER,
            true /* indexed */,
            false /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            LOCATION_NAME,
            true /* indexed */,
            false /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            LOCATION_REGION,
            true /* indexed */,
            false /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            BE_NUMBER,
            true /* indexed */,
            false /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            METADATA_NEUTRALITY_FIELDS,
            false /* indexed */,
            false /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.OBJECT_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            RELATED_RESOURCE,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            SUBTITLE,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

    descriptors.add(
        new AttributeDescriptorImpl(
            TOPIC_TYPE,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));

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
