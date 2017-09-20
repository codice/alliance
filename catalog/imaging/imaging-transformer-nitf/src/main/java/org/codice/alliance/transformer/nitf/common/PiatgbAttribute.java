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

/** TRE for "Imagery Access Target" */
public class PiatgbAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "piatgb.";

  public static final String TARGET_UTM = PREFIX + "target-utm";

  public static final String PIA_TARGET_IDENTIFICATION = PREFIX + "target-identification";

  public static final String PIA_COUNTRY = PREFIX + "country-code";

  public static final String PIA_CATEGORY = PREFIX + "category-code";

  public static final String TARGET_GEOGRAPHIC_COORDINATES =
      PREFIX + "target-geographic-coordinates";

  public static final String TARGET_COORDINATE_DATUM = PREFIX + "target-coordinate-datum";

  public static final String TARGET_NAME = PREFIX + "target-name";

  public static final String PERCENTAGE_OF_COVERAGE = PREFIX + "percentage-of-coverage";

  public static final String TARGET_LATITUDE = PREFIX + "target-latitude";

  public static final String TARGET_LONGITUDE = PREFIX + "target-longitude";

  /*
   * Non-normalized attributes
   */
  public static final PiatgbAttribute TARGET_UTM_ATTRIBUTE =
      new PiatgbAttribute(
          TARGET_UTM,
          "TGTUTM",
          tre -> TreUtility.getTreValue(tre, "TGTUTM"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute PIA_TARGET_IDENTIFICATION_ATTRIBUTE =
      new PiatgbAttribute(
          PIA_TARGET_IDENTIFICATION,
          "PIATGAID",
          tre -> TreUtility.getTreValue(tre, "PIATGAID"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute PIA_COUNTRY_ATTRIBUTE =
      new PiatgbAttribute(
          PIA_COUNTRY,
          "PIACTRY",
          tre -> TreUtility.getTreValue(tre, "PIACTRY"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute PIA_CATEGORY_ATTRIBUTE =
      new PiatgbAttribute(
          PIA_CATEGORY,
          "PIACAT",
          tre -> TreUtility.getTreValue(tre, "PIACAT"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute TARGET_GEOGRAPHIC_COORDINATES_ATTRIBUTE =
      new PiatgbAttribute(
          TARGET_GEOGRAPHIC_COORDINATES,
          "TGTGEO",
          tre -> TreUtility.getTreValue(tre, "TGTGEO"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute TARGET_COORDINATE_DATUM_ATTRIBUTE =
      new PiatgbAttribute(
          TARGET_COORDINATE_DATUM,
          "DATUM",
          tre -> TreUtility.convertToString(tre, "DATUM"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute TARGET_NAME_ATTRIBUTE =
      new PiatgbAttribute(
          TARGET_NAME,
          "TGTNAME",
          tre -> TreUtility.getTreValue(tre, "TGTNAME"),
          BasicTypes.STRING_TYPE);

  public static final PiatgbAttribute PERCENTAGE_OF_COVERAGE_ATTRIBUTE =
      new PiatgbAttribute(
          PERCENTAGE_OF_COVERAGE,
          "PERCOVER",
          tre -> TreUtility.convertToInteger(tre, "PERCOVER"),
          BasicTypes.INTEGER_TYPE);

  public static final PiatgbAttribute TARGET_LATITUDE_ATTRIBUTE =
      new PiatgbAttribute(
          TARGET_LATITUDE,
          "TGTLAT",
          tre -> TreUtility.convertToFloat(tre, "TGTLAT"),
          BasicTypes.FLOAT_TYPE);

  public static final PiatgbAttribute TARGET_LONGITUDE_ATTRIBUTE =
      new PiatgbAttribute(
          TARGET_LONGITUDE,
          "TGTLON",
          tre -> TreUtility.convertToFloat(tre, "TGTLON"),
          BasicTypes.FLOAT_TYPE);

  private PiatgbAttribute(
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
