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
package org.codice.alliance.transformer.nitf.gmti;

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
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreGroup;

public class MtirpbAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "mtirpb.";

  public static final String NUMBER_OF_VALID_TARGETS = PREFIX + "number-of-valid-targets";

  public static final String AIRCRAFT_LOCATION = PREFIX + "aircraft-location";

  public static final String AIRCRAFT_ALTITUDE = PREFIX + "aircraft-altitude";

  public static final String AIRCRAFT_ALTITUDE_UNITS = PREFIX + "aircraft-altitude-unit-of-measure";

  public static final String AIRCRAFT_HEADING = PREFIX + "aircraft-heading";

  public static final String COSINE_OF_GRAZE_ANGLE = PREFIX + "cosine-of-graze-angle";

  public static final String DESTINATION_POINT = PREFIX + "destination-point";

  public static final String MTI_LR = PREFIX + "mti-left-or-right";

  public static final String PATCH_NUMBER = PREFIX + "patch-number";

  public static final String SCAN_DATE_AND_TIME = PREFIX + "scan-date-and-time";

  public static final String SQUINT_ANGLE = PREFIX + "squint-angle";

  public static final String WIDE_AREA_MTI_FRAME_NUMBER = PREFIX + "wide-area-mti-frame-number";

  public static final String WIDE_AREA_MTI_BAR_NUMBER = PREFIX + "wide-area-mti-bar-number";

  public static final String TARGETS = PREFIX + "targets";

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.mtirpb.*` when
   * appropriate
   */

  public static final MtirpbAttribute NUMBER_OF_VALID_TARGETS_ATTRIBUTE =
      new MtirpbAttribute(
          Isr.TARGET_REPORT_COUNT,
          "NO_VALID_TARGETS",
          tre -> TreUtility.getTreValue(tre, "NO_VALID_TARGETS"),
          new IsrAttributes().getAttributeDescriptor(Isr.TARGET_REPORT_COUNT),
          NUMBER_OF_VALID_TARGETS);

  /*
   * Non-normalized attributes
   */

  public static final MtirpbAttribute AIRCRAFT_LOCATION_ATTRIBUTE =
      new MtirpbAttribute(
          AIRCRAFT_LOCATION,
          "ACFT_LOC",
          tre -> TreUtility.getTreValue(tre, "ACFT_LOC"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute AIRCRAFT_ALTITUDE_ATTRIBUTE =
      new MtirpbAttribute(
          AIRCRAFT_ALTITUDE,
          "ACFT_ALT",
          tre -> TreUtility.getTreValue(tre, "ACFT_ALT"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute AIRCRAFT_ALTITUDE_UNITS_ATTRIBUTE =
      new MtirpbAttribute(
          AIRCRAFT_ALTITUDE_UNITS,
          "ACFT_ALT_UNIT",
          tre -> TreUtility.getTreValue(tre, "ACFT_ALT_UNIT"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute AIRCRAFT_HEADING_ATTRIBUTE =
      new MtirpbAttribute(
          AIRCRAFT_HEADING,
          "ACFT_HEADING",
          tre -> TreUtility.getTreValue(tre, "ACFT_HEADING"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute COSINE_OF_GRAZE_ANGLE_ATTRIBUTE =
      new MtirpbAttribute(
          COSINE_OF_GRAZE_ANGLE,
          "COSGRZ",
          tre -> TreUtility.getTreValue(tre, "COSGRZ"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute DESTINATION_POINT_ATTRIBUTE =
      new MtirpbAttribute(
          DESTINATION_POINT,
          "MTI_DP",
          tre -> TreUtility.getTreValue(tre, "MTI_DP"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute MTI_LR_ATTRIBUTE =
      new MtirpbAttribute(
          MTI_LR, "MTI_LR", tre -> TreUtility.getTreValue(tre, "MTI_LR"), BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute PATCH_NUMBER_ATTRIBUTE =
      new MtirpbAttribute(
          PATCH_NUMBER,
          "PATCH_NO",
          tre -> TreUtility.getTreValue(tre, "PATCH_NO"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute SCAN_DATE_AND_TIME_ATTRIBUTE =
      new MtirpbAttribute(
          SCAN_DATE_AND_TIME,
          "DATIME",
          tre -> TreUtility.getTreValue(tre, "DATIME"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute SQUINT_ANGLE_ATTRIBUTE =
      new MtirpbAttribute(
          SQUINT_ANGLE,
          "SQUINT_ANGLE",
          tre -> TreUtility.getTreValue(tre, "SQUINT_ANGLE"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute WIDE_AREA_MTI_FRAME_NUMBER_ATTRIBUTE =
      new MtirpbAttribute(
          WIDE_AREA_MTI_FRAME_NUMBER,
          "WAMTI_FRAME_NO",
          tre -> TreUtility.getTreValue(tre, "WAMTI_FRAME_NO"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute WIDE_AREA_MTI_BAR_NUMBER_ATTRIBUTE =
      new MtirpbAttribute(
          WIDE_AREA_MTI_BAR_NUMBER,
          "WAMTI_BAR_NO",
          tre -> TreUtility.getTreValue(tre, "WAMTI_BAR_NO"),
          BasicTypes.STRING_TYPE);

  public static final MtirpbAttribute TARGETS_ATTRIBUTE =
      new MtirpbAttribute(
          TARGETS,
          "TARGETS",
          tre -> TreUtility.getTreValue(tre, "TARGETS"),
          BasicTypes.STRING_TYPE,
          IndexedMtirpbAttribute.getAttributes());

  private MtirpbAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private MtirpbAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType,
      List<NitfAttribute<TreGroup>> indexedAttributes) {
    super(longName, shortName, accessorFunction, attributeType, indexedAttributes);
    ATTRIBUTES.add(this);
  }

  private MtirpbAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
