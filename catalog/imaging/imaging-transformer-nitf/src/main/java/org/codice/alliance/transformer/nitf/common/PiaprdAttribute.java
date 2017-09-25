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

import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreGroup;

/** TRE for Imagery Access Product */
public class PiaprdAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "piaprd.";

  public static final String ACCESS_ID = PREFIX + "access-id";

  public static final String FM_CONTROL_NUMBER = PREFIX + "fm-control-number";

  public static final String SUBJECTIVE_DETAIL = PREFIX + "subjective-detail";

  public static final String PRODUCT_CODE = PREFIX + "product-code";

  public static final String PRODUCER_SUPPLEMENT = PREFIX + "producer-supplement";

  public static final String PRODUCT_ID_NUMBER = PREFIX + "product-id-number";

  public static final String PRODUCT_SHORT_NAME = PREFIX + "product-short-name";

  public static final String PRODUCER_CODE = PREFIX + "producer-code";

  public static final String PRODUCER_CREATE_TIME = PREFIX + "producer-create-time";

  public static final String MAP_ID = PREFIX + "map-id";

  public static final String KEYWORD_REPETITIONS = PREFIX + "keyword-repetitions";

  public static final String KEYWORD = PREFIX + "keyword";

  /*
   * Non-normalized attributes
   */

  public static final PiaprdAttribute ACCESS_ID_ATTRIBUTE =
      new PiaprdAttribute(
          ACCESS_ID,
          "ACCESSID",
          tre -> TreUtility.getTreValue(tre, "ACCESSID"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute FM_CONTROL_NUMBER_ATTRIBUTE =
      new PiaprdAttribute(
          FM_CONTROL_NUMBER,
          "FMCONTROL",
          tre -> TreUtility.getTreValue(tre, "FMCONTROL"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute SUBJECTIVE_DETAIL_ATTRIBUTE =
      new PiaprdAttribute(
          SUBJECTIVE_DETAIL,
          "SUBDET",
          tre -> TreUtility.getTreValue(tre, "SUBDET"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute PRODUCT_CODE_ATTRIBUTE =
      new PiaprdAttribute(
          PRODUCT_CODE,
          "PRODCODE",
          tre -> TreUtility.getTreValue(tre, "PRODCODE"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute PRODUCER_SUPPLEMENT_ATTRIBUTE =
      new PiaprdAttribute(
          PRODUCER_SUPPLEMENT,
          "PRODUCERSE",
          tre -> TreUtility.getTreValue(tre, "PRODUCERSE"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute PRODUCT_ID_NUMBER_ATTRIBUTE =
      new PiaprdAttribute(
          PRODUCT_ID_NUMBER,
          "PRODIDNO",
          tre -> TreUtility.getTreValue(tre, "PRODIDNO"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute PRODUCT_SHORT_NAME_ATTRIBUTE =
      new PiaprdAttribute(
          PRODUCT_SHORT_NAME,
          "PRODSNME",
          tre -> TreUtility.getTreValue(tre, "PRODSNME"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute PRODUCER_CODE_ATTRIBUTE =
      new PiaprdAttribute(
          PRODUCER_CODE,
          "PRODUCERCD",
          tre -> TreUtility.getTreValue(tre, "PRODUCERCD"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute PRODUCER_CREATE_TIME_ATTRIBUTE =
      new PiaprdAttribute(
          PRODUCER_CREATE_TIME,
          "PRODCRTIME",
          tre -> TreUtility.getTreValue(tre, "PRODCRTIME"),
          BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute MAP_ID_ATTRIBUTE =
      new PiaprdAttribute(
          MAP_ID, "MAPID", tre -> TreUtility.getTreValue(tre, "MAPID"), BasicTypes.STRING_TYPE);

  public static final PiaprdAttribute KEYWORD_REPETITIONS_ATTRIBUTE =
      new PiaprdAttribute(
          KEYWORD_REPETITIONS,
          "KEYWORDREP",
          tre -> TreUtility.convertToInteger(tre, "KEYWORDREP"),
          BasicTypes.INTEGER_TYPE);

  public static final PiaprdAttribute KEYWORD_ATTRIBUTE =
      new PiaprdAttribute(
          KEYWORD,
          "KEYWORD",
          tre -> TreUtility.getTreValue(tre, "KEYWORD"),
          BasicTypes.STRING_TYPE,
          IndexedPiaprdAttribute.getAttributes());

  private PiaprdAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private PiaprdAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType,
      List<NitfAttribute<TreGroup>> indexedAttributes) {
    super(longName, shortName, accessorFunction, attributeType, indexedAttributes);
    ATTRIBUTES.add(this);
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
