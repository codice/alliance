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
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.measure.UnitConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;
import tec.uom.se.function.MultiplyConverter;

/** TRE for "Exploitation Reference Data" */
public class CsexraAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  public static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "csexra.";

  public static final String SNOW_COVER = PREFIX + "snow-cover";

  public static final String PREDICTED_NIIRS = PREFIX + "predicted-niirs";

  public static final String SNOW_DEPTH_MIN = PREFIX + "snow-depth-min";

  public static final String SNOW_DEPTH_MAX = PREFIX + "snow-depth-max";

  public static final String SNOW_DEPTH_CAT = PREFIX + "snow-depth-category";

  public static final String SENSOR = PREFIX + "sensor";

  public static final String TIME_FIRST_LINE_IMAGE = PREFIX + "time-first-line-image";

  public static final String TIME_IMAGE_DURATION = PREFIX + "image-duration-time";

  public static final String MAX_GSD = PREFIX + "max-gsd";

  public static final String ALONG_SCAN_GSD = PREFIX + "along-scan-gsd";

  public static final String CROSS_SCAN_GSD = PREFIX + "cross-scan-gsd";

  public static final String GEO_MEAN_GSD = PREFIX + "geometric-mean-gsd";

  public static final String A_S_VERT_GSD = PREFIX + "along-scan-vertical-gsd";

  public static final String C_S_VERT_GSD = PREFIX + "cross-scan-vertical-gsd";

  public static final String GEO_MEAN_VERT_GSD = PREFIX + "geometric-mean-vertical-gsd";

  public static final String GSD_BETA_ANGLE = PREFIX + "gsd-beta-angle";

  public static final String DYNAMIC_RANGE = PREFIX + "pixel-dynamic-range";

  public static final String NUM_LINES = PREFIX + "num-lines";

  public static final String NUM_SAMPLES = PREFIX + "num-samples";

  public static final String ANGLE_TO_NORTH = PREFIX + "angle-to-north";

  public static final String OBLIQUITY_ANGLE = PREFIX + "obliquity-angle";

  public static final String AZ_OF_OBLIQUITY = PREFIX + "azimuth-obliquity";

  public static final String SUN_AZIMUTH = PREFIX + "sun-azimuth";

  public static final String SUN_ELEVATION = PREFIX + "sun-elevation";

  public static final String CIRCL_ERR = PREFIX + "circular-error";

  public static final String LINEAR_ERR = PREFIX + "linear-error";

  static final String SNOW_DEPTH_CAT_SHORT_NAME = "SNOW_DEPTH_CAT";

  static final String GRD_COVER_SHORT_NAME = "GRD_COVER";

  static final String PREDICTED_NIIRS_SHORT_NAME = "PREDICTED_NIIRS";

  public static final String SENSOR_SHORT_NAME = "SENSOR";

  public static final String TIME_FIRST_LINE_IMAGE_SHORT_NAME = "TIME_FIRST_LINE_IMAGE";

  public static final String TIME_IMAGE_DURATION_SHORT_NAME = "TIME_IMAGE_DURATION";

  public static final String MAX_GSD_SHORT_NAME = "MAX_GSD";

  public static final String ALONG_SCAN_GSD_SHORT_NAME = "ALONG_SCAN_GSD";

  public static final String CROSS_SCAN_GSD_SHORT_NAME = "CROSS_SCAN_GSD";

  public static final String GEO_MEAN_GSD_SHORT_NAME = "GEO_MEAN_GSD";

  public static final String A_S_VERT_GSD_SHORT_NAME = "A_S_VERT_GSD";

  public static final String C_S_VERT_GSD_SHORT_NAME = "C_S_VERT_GSD";

  public static final String GEO_MEAN_VERT_GSD_SHORT_NAME = "GEO_MEAN_VERT_GSD";

  public static final String GSD_BETA_ANGLE_SHORT_NAME = "GSD_BETA_ANGLE";

  public static final String DYNAMIC_RANGE_SHORT_NAME = "DYNAMIC_RANGE";

  public static final String NUM_LINES_SHORT_NAME = "NUM_LINES";

  public static final String NUM_SAMPLES_SHORT_NAME = "NUM_SAMPLES";

  public static final String ANGLE_TO_NORTH_SHORT_NAME = "ANGLE_TO_NORTH";

  public static final String OBLIQUITY_ANGLE_SHORT_NAME = "OBLIQUITY_ANGLE";

  public static final String AZ_OF_OBLIQUITY_SHORT_NAME = "AZ_OF_OBLIQUITY";

  public static final String SUN_AZIMUTH_SHORT_NAME = "SUN_AZIMUTH";

  public static final String SUN_ELEVATION_SHORT_NAME = "SUN_ELEVATION";

  public static final String CIRCL_ERR_SHORT_NAME = "CIRCL_ERR";

  public static final String LINEAR_ERR_SHORT_NAME = "LINEAR_ERR";

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.csexra.*` when appropriate.
   */

  public static final CsexraAttribute SNOW_COVER_ATTRIBUTE =
      new CsexraAttribute(
          Isr.SNOW_COVER,
          GRD_COVER_SHORT_NAME,
          CsexraAttribute::getSnowCoverFunction,
          new IsrAttributes().getAttributeDescriptor(Isr.SNOW_COVER),
          SNOW_COVER);

  private static final Pattern NIIRS_FORMAT = Pattern.compile("^[0-9]\\.[0-9]$");

  public static final CsexraAttribute PREDICTED_NIIRS_ATTRIBUTE =
      new CsexraAttribute(
          Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
          PREDICTED_NIIRS_SHORT_NAME,
          CsexraAttribute::getNiirsFunction,
          new IsrAttributes()
              .getAttributeDescriptor(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE),
          PREDICTED_NIIRS);

  private static final UnitConverter INCHES_TO_CENTIMETERS_CONVERTER = new MultiplyConverter(2.54);

  private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_0 =
      new ImmutablePair<>(inchesToCentimeters(0), inchesToCentimeters(1));

  private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_1 =
      new ImmutablePair<>(inchesToCentimeters(1), inchesToCentimeters(9));

  private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_2 =
      new ImmutablePair<>(inchesToCentimeters(9), inchesToCentimeters(17));

  private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_3 =
      new ImmutablePair<>(inchesToCentimeters(17), Float.MAX_VALUE);

  static final CsexraAttribute SNOW_DEPTH_MIN_ATTRIBUTE =
      new CsexraAttribute(
          Isr.SNOW_DEPTH_MIN_CENTIMETERS,
          SNOW_DEPTH_CAT_SHORT_NAME,
          getSnowDepthAccessorFunction(Pair::getLeft),
          new IsrAttributes().getAttributeDescriptor(Isr.SNOW_DEPTH_MIN_CENTIMETERS),
          SNOW_DEPTH_MIN);

  static final CsexraAttribute SNOW_DEPTH_MAX_ATTRIBUTE =
      new CsexraAttribute(
          Isr.SNOW_DEPTH_MAX_CENTIMETERS,
          SNOW_DEPTH_CAT_SHORT_NAME,
          getSnowDepthAccessorFunction(Pair::getRight),
          new IsrAttributes().getAttributeDescriptor(Isr.SNOW_DEPTH_MAX_CENTIMETERS),
          SNOW_DEPTH_MAX);

  /*
   * Non-normalized attributes
   */

  static final CsexraAttribute SNOW_DEPTH_CAT_ATTRIBUTE =
      new CsexraAttribute(
          SNOW_DEPTH_CAT,
          SNOW_DEPTH_CAT_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, SNOW_DEPTH_CAT_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsexraAttribute SENSOR_ATTRIBUTE =
      new CsexraAttribute(
          SENSOR,
          SENSOR_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SENSOR_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute TIME_FIRST_LINE_IMAGE_ATTRIBUTE =
      new CsexraAttribute(
          TIME_FIRST_LINE_IMAGE,
          TIME_FIRST_LINE_IMAGE_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, TIME_FIRST_LINE_IMAGE_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute TIME_IMAGE_DURATION_ATTRIBUTE =
      new CsexraAttribute(
          TIME_IMAGE_DURATION,
          TIME_IMAGE_DURATION_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, TIME_IMAGE_DURATION_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute MAX_GSD_ATTRIBUTE =
      new CsexraAttribute(
          MAX_GSD,
          MAX_GSD_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, MAX_GSD_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute ALONG_SCAN_GSD_ATTRIBUTE =
      new CsexraAttribute(
          ALONG_SCAN_GSD,
          ALONG_SCAN_GSD_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, ALONG_SCAN_GSD_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute CROSS_SCAN_GSD_ATTRIBUTE =
      new CsexraAttribute(
          CROSS_SCAN_GSD,
          CROSS_SCAN_GSD_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, CROSS_SCAN_GSD_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute GEO_MEAN_GSD_ATTRIBUTE =
      new CsexraAttribute(
          GEO_MEAN_GSD,
          GEO_MEAN_GSD_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, GEO_MEAN_GSD_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute A_S_VERT_GSD_ATTRIBUTE =
      new CsexraAttribute(
          A_S_VERT_GSD,
          A_S_VERT_GSD_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, A_S_VERT_GSD_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute C_S_VERT_GSD_ATTRIBUTE =
      new CsexraAttribute(
          C_S_VERT_GSD,
          C_S_VERT_GSD_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, C_S_VERT_GSD_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute GEO_MEAN_VERT_GSD_ATTRIBUTE =
      new CsexraAttribute(
          GEO_MEAN_VERT_GSD,
          GEO_MEAN_VERT_GSD_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, GEO_MEAN_VERT_GSD_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute GSD_BETA_ANGLE_ATTRIBUTE =
      new CsexraAttribute(
          GSD_BETA_ANGLE,
          GSD_BETA_ANGLE_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, GSD_BETA_ANGLE_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsexraAttribute DYNAMIC_RANGE_ATTRIBUTE =
      new CsexraAttribute(
          DYNAMIC_RANGE,
          DYNAMIC_RANGE_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, DYNAMIC_RANGE_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsexraAttribute NUM_LINES_ATTRIBUTE =
      new CsexraAttribute(
          NUM_LINES,
          NUM_LINES_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, NUM_LINES_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsexraAttribute NUM_SAMPLES_ATTRIBUTE =
      new CsexraAttribute(
          NUM_SAMPLES,
          NUM_SAMPLES_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, NUM_SAMPLES_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsexraAttribute ANGLE_TO_NORTH_ATTRIBUTE =
      new CsexraAttribute(
          ANGLE_TO_NORTH,
          ANGLE_TO_NORTH_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, ANGLE_TO_NORTH_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute OBLIQUITY_ANGLE_ATTRIBUTE =
      new CsexraAttribute(
          OBLIQUITY_ANGLE,
          OBLIQUITY_ANGLE_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, OBLIQUITY_ANGLE_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute AZ_OF_OBLIQUITY_ATTRIBUTE =
      new CsexraAttribute(
          AZ_OF_OBLIQUITY,
          AZ_OF_OBLIQUITY_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, AZ_OF_OBLIQUITY_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute SUN_AZIMUTH_ATTRIBUTE =
      new CsexraAttribute(
          SUN_AZIMUTH,
          SUN_AZIMUTH_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, SUN_AZIMUTH_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute SUN_ELEVATION_ATTRIBUTE =
      new CsexraAttribute(
          SUN_ELEVATION,
          SUN_ELEVATION_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, SUN_ELEVATION_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final CsexraAttribute CIRCL_ERR_ATTRIBUTE =
      new CsexraAttribute(
          CIRCL_ERR,
          CIRCL_ERR_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, CIRCL_ERR_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsexraAttribute LINEAR_ERR_ATTRIBUTE =
      new CsexraAttribute(
          LINEAR_ERR,
          LINEAR_ERR_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, LINEAR_ERR_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  private CsexraAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private CsexraAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private static float inchesToCentimeters(float inches) {
    return (float) INCHES_TO_CENTIMETERS_CONVERTER.convert(inches);
  }

  private static Serializable getSnowCoverFunction(Tre tre) {
    return Optional.ofNullable(TreUtility.getTreValue(tre, GRD_COVER_SHORT_NAME))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(
            value -> {
              switch (value) {
                case "0":
                  return Boolean.FALSE;
                case "1":
                  return Boolean.TRUE;
                default:
                  return null;
              }
            })
        .orElse(null);
  }

  private static Serializable getNiirsFunction(Tre tre) {

    String value = TreUtility.getTreValue(tre, PREDICTED_NIIRS_SHORT_NAME);

    if (StringUtils.isNotEmpty(value)) {
      return parseNiirs(value);
    }

    return null;
  }

  private static Float parseNiirs(String niirs) {
    return NIIRS_FORMAT.matcher(niirs).matches() ? Float.valueOf(niirs) : null;
  }

  private static Function<Tre, Serializable> getSnowDepthAccessorFunction(
      Function<Pair<Float, Float>, Float> pairFunction) {
    return tre ->
        Optional.ofNullable(TreUtility.getTreValue(tre, SNOW_DEPTH_CAT_SHORT_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(StringUtils::isNotEmpty)
            .map(Integer::valueOf)
            .map(CsexraAttribute::convertSnowDepthCat)
            .map(pair -> pair.map(pairFunction).orElse(null))
            .orElse(null);
  }

  private static Optional<Pair<Float, Float>> convertSnowDepthCat(int category) {
    switch (category) {
      case 0:
        return Optional.of(SNOW_DEPTH_CATEGORY_0);
      case 1:
        return Optional.of(SNOW_DEPTH_CATEGORY_1);
      case 2:
        return Optional.of(SNOW_DEPTH_CATEGORY_2);
      case 3:
        return Optional.of(SNOW_DEPTH_CATEGORY_3);
      default:
        return Optional.empty();
    }
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
