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

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;

/**
 * TRE for "Dataset Identification"
 */
public class CsdidaAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    static final String PLATFORM_CODE_NAME = "PLATFORM_CODE";

    static final String VEHICLE_ID_NAME = "VEHICLE_ID";

    static final String DAY_NAME = "DAY";

    static final String MONTH_NAME = "MONTH";

    static final String YEAR_NAME = "YEAR";

    static final String PASS_NAME = "PASS";

    static final String OPERATION_NAME = "OPERATION";

    static final String SENSOR_ID_NAME = "SENSOR_ID";

    static final String PRODUCT_ID_NAME = "PRODUCT_ID";

    static final String TIME_NAME = "TIME";

    static final String PROCESS_TIME_NAME = "PROCESS_TIME";

    static final String SOFTWARE_VERSION_NUMBER_NAME = "SOFTWARE_VERSION_NUMBER";

    private static final String ATTRIBUTE_NAME_PREFIX = "csdida.";

    static final CsdidaAttribute PLATFORM_ID = new CsdidaAttribute(Isr.PLATFORM_ID,
            "PLATFORM_CODE_VEHICLE_ID",
            CsdidaAttribute::getPlatformIdFunction,
            new IsrAttributes().getAttributeDescriptor(Isr.PLATFORM_ID),
            "platform-code-vehicle-id",
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute DAY = new CsdidaAttribute("day-dataset-collection",
            DAY_NAME,
            tre -> TreUtility.convertToInteger(tre, DAY_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute MONTH = new CsdidaAttribute("month-dataset-collection",
            MONTH_NAME,
            tre -> TreUtility.convertToString(tre, MONTH_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute YEAR = new CsdidaAttribute("year-dataset-collection",
            YEAR_NAME,
            tre -> TreUtility.convertToInteger(tre, YEAR_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute PASS = new CsdidaAttribute("pass-num",
            PASS_NAME,
            tre -> TreUtility.convertToInteger(tre, PASS_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute OPERATION = new CsdidaAttribute("operation-num",
            OPERATION_NAME,
            tre -> TreUtility.convertToInteger(tre, OPERATION_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute SENSOR_ID = new CsdidaAttribute("sensor-id",
            SENSOR_ID_NAME,
            tre -> TreUtility.convertToString(tre, SENSOR_ID_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute PRODUCT_ID = new CsdidaAttribute("product-id",
            PRODUCT_ID_NAME,
            tre -> TreUtility.convertToString(tre, PRODUCT_ID_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute TIME = new CsdidaAttribute("image-start-time",
            TIME_NAME,
            tre -> TreUtility.convertToDate(tre, TIME_NAME),
            BasicTypes.DATE_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute PROCESS_TIME = new CsdidaAttribute("process-completion-time",
            PROCESS_TIME_NAME,
            tre -> TreUtility.convertToDate(tre, PROCESS_TIME_NAME),
            BasicTypes.DATE_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final CsdidaAttribute SOFTWARE_VERSION_NUMBER = new CsdidaAttribute("software-version-num",
            SOFTWARE_VERSION_NUMBER_NAME,
            tre -> TreUtility.convertToString(tre, SOFTWARE_VERSION_NUMBER_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private CsdidaAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private CsdidaAttribute(final String longName, final String shortName,
            final Function<Tre, Serializable> accessorFunction,
            AttributeDescriptor attributeDescriptor, String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private static Serializable getPlatformIdFunction(Tre tre) {
        Optional<String> platformCode = Optional.ofNullable(TreUtility.getTreValue(tre,
                PLATFORM_CODE_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast);
        Optional<Serializable> vehicleId = Optional.ofNullable(TreUtility.getTreValue(tre,
                VEHICLE_ID_NAME));

        if (platformCode.isPresent() && vehicleId.isPresent()) {
            return String.format("%s%s", platformCode.get(), vehicleId.get());
        }

        return null;
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
