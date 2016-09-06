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
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.imaging.nitf.core.tre.Tre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;

enum CsexraAttribute implements NitfAttribute<Tre> {

    @SuppressWarnings("RedundantTypeArguments")
    SNOW_DEPTH_MIN(Isr.SNOW_DEPTH_MIN,
            Constants.SNOW_DEPTH_CAT,
            getSnowDepthAccessorFunction(Pair<Double, Double>::getLeft),
            new IsrAttributes()),
    @SuppressWarnings("RedundantTypeArguments")
    SNOW_DEPTH_MAX(Isr.SNOW_DEPTH_MAX,
            Constants.SNOW_DEPTH_CAT,
            getSnowDepthAccessorFunction(Pair<Double, Double>::getRight),
            new IsrAttributes()),
    PREDICTED_NIIRS(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
            Constants.PREDICTED_NIIRS, tre -> {
        return Optional.ofNullable(TreUtility.getTreValue(tre, Constants.PREDICTED_NIIRS))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(CsexraAttribute::parseNiirs)
            .orElse(null);
    }, new IsrAttributes()),
    SNOW_COVER(Isr.SNOW_COVER, Constants.GRD_COVER, tre -> {
        return TreUtility.findIntValue(tre, Constants.GRD_COVER)
                .map(value -> {
                    switch (value) {
                    case 0:
                        return Boolean.FALSE;
                    case 1:
                        return Boolean.TRUE;
                    default:
                        return null;
                    }
                })
                .orElse(null);
    }, new IsrAttributes());

    private static final Logger LOGGER = LoggerFactory.getLogger(CsexraAttribute.class);

    private String shortName;

    private String longName;

    private Function<Tre, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    CsexraAttribute(String longName, String shortName, Function<Tre, Serializable> accessorFunction,
            MetacardType metacardType) {
        this.longName = longName;
        this.shortName = shortName;
        this.accessorFunction = accessorFunction;
        // retrieving metacard attribute descriptor for this attribute to prevent later lookups
        this.attributeDescriptor = metacardType.getAttributeDescriptor(longName);
    }

    private static Integer parseNiirs(String niirs) {
        return Constants.NIIRS_FORMAT.matcher(niirs)
                .matches() ?
                Double.valueOf(niirs)
                        .intValue() :
                null;
    }

    private static double inchesToCentimeters(double inches) {
        return inches * 2.54;
    }

    private static Function<Tre, Serializable> getSnowDepthAccessorFunction(
            Function<Pair, Double> pairFunction) {
        return tre -> TreUtility.findIntValue(tre, Constants.SNOW_DEPTH_CAT)
                .map(CsexraAttribute::convertSnowDepthCat)
                .map(doubleDoublePair -> doubleDoublePair.map(pairFunction)
                        .orElse(null))
                .orElse(null);
    }

    private static Optional<Pair<Double, Double>> convertSnowDepthCat(int category) {
        switch (category) {
        case 0:
            return Optional.of(new ImmutablePair<>(inchesToCentimeters(0), inchesToCentimeters(1)));
        case 1:
            return Optional.of(new ImmutablePair<>(inchesToCentimeters(1), inchesToCentimeters(9)));
        case 2:
            return Optional.of(new ImmutablePair<>(inchesToCentimeters(9),
                    inchesToCentimeters(17)));
        case 3:
            return Optional.of(new ImmutablePair<>(inchesToCentimeters(17), Double.MAX_VALUE));
        default:
            return Optional.empty();
        }
    }

    @Override
    public String getLongName() {
        return longName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public Function<Tre, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return attributeDescriptor;
    }
}
