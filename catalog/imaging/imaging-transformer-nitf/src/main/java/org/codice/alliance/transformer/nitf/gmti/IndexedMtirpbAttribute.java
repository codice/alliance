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
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Core;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.tre.TreGroup;

public class IndexedMtirpbAttribute extends NitfAttributeImpl<TreGroup> {

  private static final List<NitfAttribute<TreGroup>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "mtirpb.";

  public static final String INDEXED_TARGET_LOCATION = PREFIX + "target-location";

  public static final String INDEXED_TARGET_CLASSIFICATION_CATEGORY =
      PREFIX + "target-classification-category";

  public static final String INDEXED_TARGET_AMPLITUDE = PREFIX + "target-amplitude";

  public static final String INDEXED_TARGET_HEADING = PREFIX + "target-heading";

  public static final String INDEXED_TARGET_GROUND_SPEED = PREFIX + "target-ground-speed";

  public static final String INDEXED_TARGET_RADIAL_VELOCITY = PREFIX + "target-radial-velocity";

  public static final String INDEXED_TARGET_LOCATION_ACCURACY = PREFIX + "target-location-accuracy";

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.mtirpb.*` when appropriate.
   */

  public static final IndexedMtirpbAttribute INDEXED_TARGET_LOCATION_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          Core.LOCATION,
          "TGT_LOC",
          tre -> TreUtility.getTreValue(tre, "TGT_LOC"),
          new CoreAttributes().getAttributeDescriptor(Core.LOCATION),
          INDEXED_TARGET_LOCATION);

  /*
   * Non-normalized attributes
   */

  public static final IndexedMtirpbAttribute INDEXED_TARGET_CLASSIFICATION_CATEGORY_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          INDEXED_TARGET_CLASSIFICATION_CATEGORY,
          "TGT_CAT",
          IndexedMtirpbAttribute::getClassificationCategory,
          BasicTypes.STRING_TYPE);

  public static final IndexedMtirpbAttribute INDEXED_TARGET_AMPLITUDE_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          INDEXED_TARGET_AMPLITUDE,
          "TGT_AMPLITUDE",
          tre -> TreUtility.getTreValue(tre, "TGT_AMPLITUDE"),
          BasicTypes.STRING_TYPE);

  public static final IndexedMtirpbAttribute INDEXED_TARGET_HEADING_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          INDEXED_TARGET_HEADING,
          "TGT_HEADING",
          tre -> TreUtility.getTreValue(tre, "TGT_HEADING"),
          BasicTypes.STRING_TYPE);

  public static final IndexedMtirpbAttribute INDEXED_TARGET_GROUND_SPEED_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          INDEXED_TARGET_GROUND_SPEED,
          "TGT_SPEED",
          tre -> TreUtility.getTreValue(tre, "TGT_SPEED"),
          BasicTypes.STRING_TYPE);

  public static final IndexedMtirpbAttribute INDEXED_TARGET_RADIAL_VELOCITY_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          INDEXED_TARGET_RADIAL_VELOCITY,
          "TGT_VEL_R",
          tre -> TreUtility.getTreValue(tre, "TGT_VEL_R"),
          BasicTypes.STRING_TYPE);

  public static final IndexedMtirpbAttribute INDEXED_TARGET_LOCATION_ACCURACY_ATTRIBUTE =
      new IndexedMtirpbAttribute(
          INDEXED_TARGET_LOCATION_ACCURACY,
          "TGT_LOC_ACCY",
          tre -> TreUtility.getTreValue(tre, "TGT_LOC_ACCY"),
          BasicTypes.STRING_TYPE);

  private IndexedMtirpbAttribute(
      String longName,
      String shortName,
      Function<TreGroup, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private IndexedMtirpbAttribute(
      String longName,
      String shortName,
      Function<TreGroup, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private static String getClassificationCategory(TreGroup treGroup) {
    String value =
        TreUtility.getTreValue(
            treGroup, INDEXED_TARGET_CLASSIFICATION_CATEGORY_ATTRIBUTE.getShortName());
    if (value == null) {
      return MtiTargetClassificationCategory.U.getLongName();
    }
    return MtiTargetClassificationCategory.valueOf(value).getLongName();
  }

  public static List<NitfAttribute<TreGroup>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
