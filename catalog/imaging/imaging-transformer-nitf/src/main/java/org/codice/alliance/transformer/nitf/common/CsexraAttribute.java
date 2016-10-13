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

/**
 * TRE for "Exploitation Reference Data"
 */
class CsexraAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    static final String SNOW_DEPTH_CAT = "SNOW_DEPTH_CAT";

    static final String GRD_COVER = "GRD_COVER";

    static final String PREDICTED_NIIRS_NAME = "PREDICTED_NIIRS";

    private static final String ATTRIBUTE_NAME_PREFIX = "csexra.";

    public static final CsexraAttribute SNOW_COVER = new CsexraAttribute(Isr.SNOW_COVER,
            GRD_COVER,
            CsexraAttribute::getSnowCoverFunction,
            new IsrAttributes().getAttributeDescriptor(Isr.SNOW_COVER),
            "snowCover",
            ATTRIBUTE_NAME_PREFIX);

    private static final Pattern NIIRS_FORMAT = Pattern.compile("^[0-9]\\.[0-9]$");

    public static final CsexraAttribute PREDICTED_NIIRS =
            new CsexraAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
                    PREDICTED_NIIRS_NAME,
                    CsexraAttribute::getNiirsFunction,
                    new IsrAttributes().getAttributeDescriptor(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE),
                    "predictedNiirs",
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
                    SNOW_DEPTH_CAT,
                    getSnowDepthAccessorFunction(Pair::getLeft),
                    new IsrAttributes().getAttributeDescriptor(Isr.SNOW_DEPTH_MIN_CENTIMETERS),
                    "snowDepthMin",
                    ATTRIBUTE_NAME_PREFIX);

    static final CsexraAttribute SNOW_DEPTH_MAX =
            new CsexraAttribute(Isr.SNOW_DEPTH_MAX_CENTIMETERS,
                    SNOW_DEPTH_CAT,
                    getSnowDepthAccessorFunction(Pair::getRight),
                    new IsrAttributes().getAttributeDescriptor(Isr.SNOW_DEPTH_MAX_CENTIMETERS),
                    "snowDepthMax",
                    ATTRIBUTE_NAME_PREFIX);

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
        return Optional.ofNullable(TreUtility.getTreValue(tre, GRD_COVER))
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
        return tre -> Optional.ofNullable(TreUtility.getTreValue(tre, SNOW_DEPTH_CAT))
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
