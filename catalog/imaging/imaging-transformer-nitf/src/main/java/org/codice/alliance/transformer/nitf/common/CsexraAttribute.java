/**
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
package org.codice.alliance.transformer.nitf.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.measure.converter.MultiplyConverter;
import javax.measure.converter.UnitConverter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;

/**
 * TRE for "Exploitation Reference Data"
 */
public class CsexraAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    static final String SNOW_DEPTH_CAT_NAME = "SNOW_DEPTH_CAT";

    static final String GRD_COVER_NAME = "GRD_COVER";

    static final String PREDICTED_NIIRS_NAME = "PREDICTED_NIIRS";

    static final String SENSOR_NAME = "SENSOR";

    static final String TIME_FIRST_LINE_IMAGE_NAME = "TIME_FIRST_LINE_IMAGE";

    static final String TIME_IMAGE_DURATION_NAME = "TIME_IMAGE_DURATION";

    static final String MAX_GSD_NAME = "MAX_GSD";

    static final String ALONG_SCAN_GSD_NAME = "ALONG_SCAN_GSD";

    static final String CROSS_SCAN_GSD_NAME = "CROSS_SCAN_GSD";

    static final String GEO_MEAN_GSD_NAME = "GEO_MEAN_GSD";

    static final String A_S_VERT_GSD_NAME = "A_S_VERT_GSD";

    static final String C_S_VERT_GSD_NAME = "C_S_VERT_GSD";

    static final String GEO_MEAN_VERT_GSD_NAME = "GEO_MEAN_VERT_GSD";

    static final String GSD_BETA_ANGLE_NAME = "GSD_BETA_ANGLE";

    static final String DYNAMIC_RANGE_NAME = "DYNAMIC_RANGE";

    static final String NUM_LINES_NAME = "NUM_LINES";

    static final String NUM_SAMPLES_NAME = "NUM_SAMPLES";

    static final String ANGLE_TO_NORTH_NAME = "ANGLE_TO_NORTH";

    static final String OBLIQUITY_ANGLE_NAME = "OBLIQUITY_ANGLE";

    static final String AZ_OF_OBLIQUITY_NAME = "AZ_OF_OBLIQUITY";

    static final String SUN_AZIMUTH_NAME = "SUN_AZIMUTH";

    static final String SUN_ELEVATION_NAME = "SUN_ELEVATION";

    static final String CIRCL_ERR_NAME = "CIRCL_ERR";

    static final String LINEAR_ERR_NAME = "LINEAR_ERR";

    private static final String ATTRIBUTE_NAME_PREFIX = "csexra.";

    public static final CsexraAttribute SNOW_COVER = new CsexraAttribute(Isr.SNOW_COVER,
            GRD_COVER_NAME,
            CsexraAttribute::getSnowCoverFunction,
            new IsrAttributes().getAttributeDescriptor(Isr.SNOW_COVER),
            "snow-cover",
            ATTRIBUTE_NAME_PREFIX);

    private static final Pattern NIIRS_FORMAT = Pattern.compile("^[0-9]\\.[0-9]$");

    public static final CsexraAttribute PREDICTED_NIIRS =
            new CsexraAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
                    PREDICTED_NIIRS_NAME,
                    CsexraAttribute::getNiirsFunction,
                    new IsrAttributes().getAttributeDescriptor(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE),
                    "predicted-niirs",
                    ATTRIBUTE_NAME_PREFIX);

    private static final UnitConverter INCHES_TO_CENTIMETERS_CONVERTER =
            new MultiplyConverter(2.54);

    private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_0 = new ImmutablePair<>(
            inchesToCentimeters(0),
            inchesToCentimeters(1));

    private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_1 = new ImmutablePair<>(
            inchesToCentimeters(1),
            inchesToCentimeters(9));

    private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_2 = new ImmutablePair<>(
            inchesToCentimeters(9),
            inchesToCentimeters(17));

    private static final Pair<Float, Float> SNOW_DEPTH_CATEGORY_3 = new ImmutablePair<>(
            inchesToCentimeters(17),
            Float.MAX_VALUE);

    static final CsexraAttribute SNOW_DEPTH_MIN =
            new CsexraAttribute(Isr.SNOW_DEPTH_MIN_CENTIMETERS,
                    SNOW_DEPTH_CAT_NAME,
                    getSnowDepthAccessorFunction(Pair::getLeft),
                    new IsrAttributes().getAttributeDescriptor(Isr.SNOW_DEPTH_MIN_CENTIMETERS),
                    "snow-depth-min",
                    ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute SNOW_DEPTH_MAX =
            new CsexraAttribute(Isr.SNOW_DEPTH_MAX_CENTIMETERS,
                    SNOW_DEPTH_CAT_NAME,
                    getSnowDepthAccessorFunction(Pair::getRight),
                    new IsrAttributes().getAttributeDescriptor(Isr.SNOW_DEPTH_MAX_CENTIMETERS),
                    "snow-depth-max",
                    ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute SNOW_DEPTH_CAT = new CsexraAttribute("snow-depth-category",
            SNOW_DEPTH_CAT_NAME,
            tre -> TreUtility.convertToInteger(tre, SNOW_DEPTH_CAT_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute SENSOR = new CsexraAttribute("sensor",
            SENSOR_NAME,
            tre -> TreUtility.convertToString(tre, SENSOR_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute TIME_FIRST_LINE_IMAGE = new CsexraAttribute("time-first-line-image",
            TIME_FIRST_LINE_IMAGE_NAME,
            tre -> TreUtility.convertToFloat(tre, TIME_FIRST_LINE_IMAGE_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute TIME_IMAGE_DURATION = new CsexraAttribute("image-duration-time",
            TIME_IMAGE_DURATION_NAME,
            tre -> TreUtility.convertToFloat(tre, TIME_IMAGE_DURATION_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute MAX_GSD = new CsexraAttribute("max-gsd",
            MAX_GSD_NAME,
            tre -> TreUtility.convertToFloat(tre, MAX_GSD_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute ALONG_SCAN_GSD = new CsexraAttribute("along-scan-gsd",
            ALONG_SCAN_GSD_NAME,
            tre -> TreUtility.convertToString(tre, ALONG_SCAN_GSD_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute CROSS_SCAN_GSD = new CsexraAttribute("cross-scan-gsd",
            CROSS_SCAN_GSD_NAME,
            tre -> TreUtility.convertToString(tre, CROSS_SCAN_GSD_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute GEO_MEAN_GSD = new CsexraAttribute("geometric-mean-gsd",
            GEO_MEAN_GSD_NAME,
            tre -> TreUtility.convertToString(tre, GEO_MEAN_GSD_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute A_S_VERT_GSD = new CsexraAttribute("along-scan-vertical-gsd",
            A_S_VERT_GSD_NAME,
            tre -> TreUtility.convertToString(tre, A_S_VERT_GSD_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute C_S_VERT_GSD = new CsexraAttribute("cross-scan-vertical-gsd",
            C_S_VERT_GSD_NAME,
            tre -> TreUtility.convertToString(tre, C_S_VERT_GSD_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute GEO_MEAN_VERT_GSD = new CsexraAttribute("geometric-mean-vertical-gsd",
            GEO_MEAN_VERT_GSD_NAME,
            tre -> TreUtility.convertToString(tre, GEO_MEAN_VERT_GSD_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute GSD_BETA_ANGLE = new CsexraAttribute("gsd-beta-angle",
            GSD_BETA_ANGLE_NAME,
            tre -> TreUtility.convertToString(tre, GSD_BETA_ANGLE_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute DYNAMIC_RANGE = new CsexraAttribute("pixel-dynamic-range",
            DYNAMIC_RANGE_NAME,
            tre -> TreUtility.convertToInteger(tre, DYNAMIC_RANGE_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute NUM_LINES = new CsexraAttribute("num-lines",
            NUM_LINES_NAME,
            tre -> TreUtility.convertToInteger(tre, NUM_LINES_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute NUM_SAMPLES = new CsexraAttribute("num-samples",
            NUM_SAMPLES_NAME,
            tre -> TreUtility.convertToInteger(tre, NUM_SAMPLES_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute ANGLE_TO_NORTH = new CsexraAttribute("angle-to-north",
            ANGLE_TO_NORTH_NAME,
            tre -> TreUtility.convertToFloat(tre, ANGLE_TO_NORTH_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute OBLIQUITY_ANGLE = new CsexraAttribute("obliquity-angle",
            OBLIQUITY_ANGLE_NAME,
            tre -> TreUtility.convertToFloat(tre, OBLIQUITY_ANGLE_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute AZ_OF_OBLIQUITY = new CsexraAttribute("azimuth-obliquity",
            AZ_OF_OBLIQUITY_NAME,
            tre -> TreUtility.convertToFloat(tre, AZ_OF_OBLIQUITY_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute SUN_AZIMUTH = new CsexraAttribute("sun-azimuth",
            SUN_AZIMUTH_NAME,
            tre -> TreUtility.convertToFloat(tre, SUN_AZIMUTH_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute SUN_ELEVATION = new CsexraAttribute("sun-elevation",
            SUN_ELEVATION_NAME,
            tre -> TreUtility.convertToFloat(tre, SUN_ELEVATION_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute CIRCL_ERR = new CsexraAttribute("circular-error",
            CIRCL_ERR_NAME,
            tre -> TreUtility.convertToInteger(tre, CIRCL_ERR_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute LINEAR_ERR = new CsexraAttribute("linear-error",
            LINEAR_ERR_NAME,
            tre -> TreUtility.convertToInteger(tre, LINEAR_ERR_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private CsexraAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private CsexraAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeDescriptor attributeDescriptor,
            String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private static float inchesToCentimeters(float inches) {
        return (float) INCHES_TO_CENTIMETERS_CONVERTER.convert(inches);
    }

    private static Serializable getSnowCoverFunction(Tre tre) {
        return Optional.ofNullable(TreUtility.getTreValue(tre, GRD_COVER_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(value -> {
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

        Serializable value = TreUtility.getTreValue(tre, PREDICTED_NIIRS_NAME);

        if (value instanceof String) {
            return parseNiirs((String) value);
        }

        return null;
    }

    private static Integer parseNiirs(String niirs) {
        return NIIRS_FORMAT.matcher(niirs)
                .matches() ? (int) Math.round(Double.valueOf(niirs)) : null;
    }

    private static Function<Tre, Serializable> getSnowDepthAccessorFunction(
            Function<Pair<Float, Float>, Float> pairFunction) {
        return tre -> Optional.ofNullable(TreUtility.getTreValue(tre, SNOW_DEPTH_CAT_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(Integer::valueOf)
                .map(CsexraAttribute::convertSnowDepthCat)
                .map(pair -> pair.map(pairFunction)
                        .orElse(null))
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
