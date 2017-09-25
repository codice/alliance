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

/** TRE for "Image Search and Discovery" */
public class AimidbAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "aimidb.";

  public static final String ACQUISITION_DATE = PREFIX + "acquisition-date";

  public static final String MISSION_NUMBER = PREFIX + "mission-number";

  public static final String MISSION_IDENTIFICATION = PREFIX + "mission-identification";

  public static final String FLIGHT_NUMBER = PREFIX + "flight-number";

  public static final String OPERATION_NUMBER = PREFIX + "image-operation-number";

  public static final String CURRENT_SEGMENT = PREFIX + "current-segment";

  public static final String REPROCESS_NUMBER = PREFIX + "reprocess-number";

  public static final String REPLAY = PREFIX + "replay";

  public static final String START_TILE_COLUMN = PREFIX + "start-tile-column";

  public static final String START_TILE_ROW = PREFIX + "start-tile-row";

  public static final String END_SEGMENT = PREFIX + "end-segment";

  public static final String END_TILE_COLUMN = PREFIX + "end-tile-column";

  public static final String END_TILE_ROW = PREFIX + "end-tile-row";

  public static final String COUNTRY_CODE = PREFIX + "country-code";

  public static final String LOCATION = PREFIX + "location";

  /*
   * Non-normalized attributes
   */

  public static final AimidbAttribute ACQUISITION_DATE_ATTRIBUTE =
      new AimidbAttribute(
          ACQUISITION_DATE,
          "ACQUISITION_DATE",
          tre -> TreUtility.convertToString(tre, "ACQUISITION_DATE"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute MISSION_NUMBER_ATTRIBUTE =
      new AimidbAttribute(
          MISSION_NUMBER,
          "MISSION_NO",
          tre -> TreUtility.convertToString(tre, "MISSION_NO"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute MISSION_IDENTIFICATION_ATTRIBUTE =
      new AimidbAttribute(
          MISSION_IDENTIFICATION,
          "MISSION_IDENTIFICATION",
          tre -> TreUtility.convertToString(tre, "MISSION_IDENTIFICATION"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute FLIGHT_NUMBER_ATTRIBUTE =
      new AimidbAttribute(
          FLIGHT_NUMBER,
          "FLIGHT_NO",
          tre -> TreUtility.convertToString(tre, "FLIGHT_NO"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute OPERATION_NUMBER_ATTRIBUTE =
      new AimidbAttribute(
          OPERATION_NUMBER,
          "OP_NUM",
          tre -> TreUtility.convertToInteger(tre, "OP_NUM"),
          BasicTypes.INTEGER_TYPE);

  public static final AimidbAttribute CURRENT_SEGMENT_ATTRIBUTE =
      new AimidbAttribute(
          CURRENT_SEGMENT,
          "CURRENT_SEGMENT",
          tre -> TreUtility.convertToString(tre, "CURRENT_SEGMENT"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute REPROCESS_NUMBER_ATTRIBUTE =
      new AimidbAttribute(
          REPROCESS_NUMBER,
          "REPRO_NUM",
          tre -> TreUtility.convertToInteger(tre, "REPRO_NUM"),
          BasicTypes.INTEGER_TYPE);

  public static final AimidbAttribute REPLAY_ATTRIBUTE =
      new AimidbAttribute(
          REPLAY,
          "REPLAY",
          tre -> TreUtility.convertToString(tre, "REPLAY"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute START_TILE_COLUMN_ATTRIBUTE =
      new AimidbAttribute(
          START_TILE_COLUMN,
          "START_TILE_COLUMN",
          tre -> TreUtility.convertToInteger(tre, "START_TILE_COLUMN"),
          BasicTypes.INTEGER_TYPE);

  public static final AimidbAttribute START_TILE_ROW_ATTRIBUTE =
      new AimidbAttribute(
          START_TILE_ROW,
          "START_TILE_ROW",
          tre -> TreUtility.convertToInteger(tre, "START_TILE_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final AimidbAttribute END_SEGMENT_ATTRIBUTE =
      new AimidbAttribute(
          END_SEGMENT,
          "END_SEGMENT",
          tre -> TreUtility.convertToString(tre, "END_SEGMENT"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute END_TILE_COLUMN_ATTRIBUTE =
      new AimidbAttribute(
          END_TILE_COLUMN,
          "END_TILE_COLUMN",
          tre -> TreUtility.convertToInteger(tre, "END_TILE_COLUMN"),
          BasicTypes.INTEGER_TYPE);

  public static final AimidbAttribute END_TILE_ROW_ATTRIBUTE =
      new AimidbAttribute(
          END_TILE_ROW,
          "END_TILE_ROW",
          tre -> TreUtility.convertToInteger(tre, "END_TILE_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final AimidbAttribute COUNTRY_CODE_ATTRIBUTE =
      new AimidbAttribute(
          COUNTRY_CODE,
          "COUNTRY",
          tre -> TreUtility.convertToString(tre, "COUNTRY"),
          BasicTypes.STRING_TYPE);

  public static final AimidbAttribute LOCATION_ATTRIBUTE =
      new AimidbAttribute(
          LOCATION,
          "LOCATION",
          tre -> TreUtility.convertToString(tre, "LOCATION"),
          BasicTypes.STRING_TYPE);

  private AimidbAttribute(
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
