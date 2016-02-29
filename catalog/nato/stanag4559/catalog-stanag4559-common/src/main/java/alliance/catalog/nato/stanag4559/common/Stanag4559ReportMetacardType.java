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
package alliance.catalog.nato.stanag4559.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class Stanag4559ReportMetacardType extends Stanag4559MetacardType {
    public static final String METACARD_TYPE_NAME = STANAG4559_METACARD_TYPE_PREFIX+".report."+STANAG4559_METACARD_TYPE_POSTFIX;

    /**
     * Based on the originators request serial number STANAG 3277.
     */
    public static final String ORIGINATOR_REQ_SERIAL_NUM = "originatorsRequestSerialNumber";

    /**
     * A priority marking of the report.
     */
    public static final String PRIORITY = "priority";

    /**
     * The specific type of report.
     */
    public static final String TYPE = "type";

    /**
     * Metacard AttributeType for the STANAG-4559 Report Priority.
     */
    public static final AttributeType<Stanag4559ReportPriority> STANAG_4559_REPORT_PRIORITY_TYPE;

    /**
     * Metacard AttributeType for the STANAG-4559 Report Priority.
     */
    public static final AttributeType<Stanag4559ReportType> STANAG_4559_REPORT_TYPE;

    private static final Set<AttributeDescriptor> STANAG4559_REPORT_DESCRIPTORS = new HashSet<>();

    static {
        STANAG_4559_REPORT_PRIORITY_TYPE = new AttributeType<Stanag4559ReportPriority>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559ReportPriority> getBinding() {
                return Stanag4559ReportPriority.class;
            }
        };

        STANAG_4559_REPORT_TYPE = new AttributeType<Stanag4559ReportType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559ReportType> getBinding() {
                return Stanag4559ReportType.class;
            }
        };

        STANAG4559_REPORT_DESCRIPTORS.add(new AttributeDescriptorImpl(ORIGINATOR_REQ_SERIAL_NUM,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        STANAG4559_REPORT_DESCRIPTORS.add(new AttributeDescriptorImpl(PRIORITY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_REPORT_PRIORITY_TYPE));

        STANAG4559_REPORT_DESCRIPTORS.add(new AttributeDescriptorImpl(TYPE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_REPORT_TYPE));

    }

    public Stanag4559ReportMetacardType() {
        super();
        attributeDescriptors.addAll(Stanag4559ReportMetacardType.STANAG4559_REPORT_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
