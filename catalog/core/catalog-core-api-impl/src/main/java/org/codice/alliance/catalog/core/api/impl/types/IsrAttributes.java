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
import org.codice.alliance.catalog.core.api.types.Isr;

/** This class provides attributes that relate to intelligence, search, and reconnaissance */
public class IsrAttributes implements Isr, MetacardType {

  private static final Set<AttributeDescriptor> DESCRIPTORS = new HashSet<>();

  private static final String NAME = "isr";

  static {
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CLOUD_COVER,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.FLOAT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            VIDEO_MOVING_TARGET_INDICATOR_PROCESSED,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.BOOLEAN_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            COMMENTS,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            DWELL_LOCATION,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            true /* multivalued */,
            BasicTypes.GEO_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            EXPLOTATION_AUTO_GENERATED,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.BOOLEAN_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            EXPLOITATION_LEVEL,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.INTEGER_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            EXPLOITATION_SUBJECTIVE_QUALITY_CODE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            MOVING_TARGET_INDICATOR_JOB_ID,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.LONG_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            FREQUENCY_HERTZ,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.FLOAT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            IMAGE_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            JC3IEDM_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            MISSION_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            NATO_REPORTING_CODE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.FLOAT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            ORGANIZATIONAL_UNIT,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            ORIGINAL_SOURCE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            PLATFORM_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            PLATFORM_NAME,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_ENTITY_ALIAS,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_ENTITY_NAME,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_ENTITY_TYPE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_INFO_RATING,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_SITUATION_TYPE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_SERIAL_NUMBER,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_TYPE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REPORT_PRIORITY,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REQUEST_FOR_INFORMATION_FOR_INFORMATION,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REQUEST_FOR_INFORMATION_FOR_ACTION,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REQUEST_FOR_INFORMATION_SERIAL_NUMBER,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REQUEST_FOR_INFORMATION_STATUS,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            REQUEST_FOR_INFORMATION_WORKFLOW_STATUS,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            SENSOR_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            SENSOR_TYPE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            SNOW_COVER,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.BOOLEAN_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            SNOW_DEPTH_MIN_CENTIMETERS,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.FLOAT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            SNOW_DEPTH_MAX_CENTIMETERS,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.FLOAT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TARGET_CATEGORY_CODE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TARGET_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TARGET_REPORT_COUNT,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.INTEGER_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TASK_COMMENTS,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            true /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TASK_ID,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TASK_STATUS,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_ALARM_CLASSIFICATION,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_CATEGORY,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_INCIDENT_NUMBER,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_OPERATION_NAME,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_TYPE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_SUBSTANCE,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TACTICAL_DATA_LINK_ACTIVITY,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.SHORT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TACTICAL_DATA_LINK_TRACK_NUMBER,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TACTICAL_DATA_LINK_MESSAGE_NUMBER,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            TACTICAL_DATA_LINK_PLATFORM,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.SHORT_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            VIDEO_MOTION_IMAGERY_SYSTEMS_MATRIX_LEVEL,
            true /* indexed */,
            true /* stored */,
            false /* tokenized */,
            false /* multivalued */,
            BasicTypes.INTEGER_TYPE));
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            CATEGORY,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
            false /* multivalued */,
            BasicTypes.STRING_TYPE));

    /* Experimental Attributes, subject to change*/
    DESCRIPTORS.add(
        new AttributeDescriptorImpl(
            DATA_QUALITY,
            true /* indexed */,
            true /* stored */,
            true /* tokenized */,
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
