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
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;

public class AcftbAttribute extends NitfAttributeImpl<Tre> {

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "acftb.";

  public static final String AIRCRAFT_MISSION_ID = PREFIX + "aircraft-mission-id";

  public static final String AIRCRAFT_TAIL_NUMBER = PREFIX + "aircraft-tail-number";

  public static final String SENSOR_ID_TYPE = PREFIX + "sensor-id-type";

  public static final String SENSOR_ID = PREFIX + "sensor-id";

  public static final String AIRCRAFT_TAKE_OFF = PREFIX + "aircraft-take-off";

  public static final String SCENE_SOURCE = PREFIX + "scene-source";

  public static final String SCENE_NUMBER = PREFIX + "scene-number";

  public static final String PROCESSING_DATE = PREFIX + "processing-date";

  public static final String IMMEDIATE_SCENE_HOST = PREFIX + "immediate-scene-host";

  public static final String IMMEDIATE_SCENE_REQUEST_ID = PREFIX + "immediate-scene-request-id";

  public static final String MISSION_PLAN_MODE = PREFIX + "mission-plan-mode";

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.acftb.*` when
   * appropriate
   */

  public static final AcftbAttribute AIRCRAFT_MISSION_ID_ATTRIBUTE =
      new AcftbAttribute(
          Isr.MISSION_ID,
          "AC_MSN_ID",
          tre -> TreUtility.getTreValue(tre, "AC_MSN_ID"),
          new IsrAttributes().getAttributeDescriptor(Isr.MISSION_ID),
          AIRCRAFT_MISSION_ID);

  public static final AcftbAttribute AIRCRAFT_TAIL_NUMBER_ATTRIBUTE =
      new AcftbAttribute(
          Isr.PLATFORM_ID,
          "AC_TAIL_NO",
          tre -> TreUtility.getTreValue(tre, "AC_TAIL_NO"),
          new IsrAttributes().getAttributeDescriptor(Isr.PLATFORM_ID),
          AIRCRAFT_TAIL_NUMBER);

  public static final AcftbAttribute SENSOR_ID_TYPE_ATTRIBUTE =
      new AcftbAttribute(
          Isr.SENSOR_TYPE,
          "SENSOR_ID_TYPE",
          tre -> TreUtility.getTreValue(tre, "SENSOR_ID_TYPE"),
          new IsrAttributes().getAttributeDescriptor(Isr.SENSOR_TYPE),
          SENSOR_ID_TYPE);

  public static final AcftbAttribute SENSOR_ID_ATTRIBUTE =
      new AcftbAttribute(
          Isr.SENSOR_ID,
          "SENSOR_ID",
          tre -> TreUtility.getTreValue(tre, "SENSOR_ID"),
          new IsrAttributes().getAttributeDescriptor(Isr.SENSOR_ID),
          SENSOR_ID);

  /*
   * Non-normalized attributes
   */

  public static final AcftbAttribute AIRCRAFT_TAKE_OFF_ATTRIBUTE =
      new AcftbAttribute(
          AIRCRAFT_TAKE_OFF,
          "AC_TO",
          tre -> TreUtility.getTreValue(tre, "AC_TO"),
          BasicTypes.STRING_TYPE);

  public static final AcftbAttribute SCENE_SOURCE_ATTRIBUTE =
      new AcftbAttribute(
          SCENE_SOURCE,
          "SCENE_SOURCE",
          tre -> TreUtility.getTreValue(tre, "SCENE_SOURCE"),
          BasicTypes.STRING_TYPE);

  public static final AcftbAttribute SCENE_NUMBER_ATTRIBUTE =
      new AcftbAttribute(
          SCENE_NUMBER,
          "SCNUM",
          tre -> TreUtility.getTreValue(tre, "SCNUM"),
          BasicTypes.STRING_TYPE);

  public static final AcftbAttribute PROCESSING_DATE_ATTRIBUTE =
      new AcftbAttribute(
          PROCESSING_DATE,
          "PDATE",
          tre -> TreUtility.getTreValue(tre, "PDATE"),
          BasicTypes.STRING_TYPE);

  public static final AcftbAttribute IMMEDIATE_SCENE_HOST_ATTRIBUTE =
      new AcftbAttribute(
          IMMEDIATE_SCENE_HOST,
          "IMHOSTNO",
          tre -> TreUtility.getTreValue(tre, "IMHOSTNO"),
          BasicTypes.STRING_TYPE);

  public static final AcftbAttribute IMMEDIATE_SCENE_REQUEST_ID_ATTRIBUTE =
      new AcftbAttribute(
          IMMEDIATE_SCENE_REQUEST_ID,
          "IMREQID",
          tre -> TreUtility.getTreValue(tre, "IMREQID"),
          BasicTypes.STRING_TYPE);

  public static final AcftbAttribute MISSION_PLAN_MODE_ATTRIBUTE =
      new AcftbAttribute(
          MISSION_PLAN_MODE,
          "MPLAN",
          tre -> TreUtility.getTreValue(tre, "MPLAN"),
          BasicTypes.STRING_TYPE);

  private AcftbAttribute(
      final String longName,
      final String shortName,
      final Function<Tre, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private AcftbAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
