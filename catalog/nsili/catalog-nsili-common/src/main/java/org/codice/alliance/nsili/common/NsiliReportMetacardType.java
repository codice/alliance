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
package org.codice.alliance.nsili.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class NsiliReportMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".report."+ NSILI_METACARD_TYPE_POSTFIX;

    /**
     * Based on the originators request serial number STANAG 3277.
     */
    public static final String ORIGINATOR_REQ_SERIAL_NUM = "originatorsRequestSerialNumber";

    /**
     * A priority marking of the report.
     */
    public static final String PRIORITY = "reportPriority";

    /**
     * The specific type of report.
     */
    public static final String TYPE = "reportType";

    /**
     * Metacard AttributeType for the NSILI Report Priority.
     */
    public static final AttributeType<NsiliReportPriority> NSILI_REPORT_PRIORITY_TYPE;

    /**
     * Metacard AttributeType for the NSILI Priority.
     */
    public static final AttributeType<NsiliReportType> NSILI_REPORT_TYPE;

    private static final Set<AttributeDescriptor> NSILI_REPORT_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_REPORT_PRIORITY_TYPE = new AttributeType<NsiliReportPriority>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliReportPriority> getBinding() {
                return NsiliReportPriority.class;
            }
        };

        NSILI_REPORT_TYPE = new AttributeType<NsiliReportType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliReportType> getBinding() {
                return NsiliReportType.class;
            }
        };

        NSILI_REPORT_DESCRIPTORS.add(new AttributeDescriptorImpl(ORIGINATOR_REQ_SERIAL_NUM,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_REPORT_DESCRIPTORS.add(new AttributeDescriptorImpl(PRIORITY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */, NSILI_REPORT_PRIORITY_TYPE));

        NSILI_REPORT_DESCRIPTORS.add(new AttributeDescriptorImpl(TYPE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */, NSILI_REPORT_TYPE));

    }

    public NsiliReportMetacardType() {
        super();
        attributeDescriptors.addAll(NsiliReportMetacardType.NSILI_REPORT_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
