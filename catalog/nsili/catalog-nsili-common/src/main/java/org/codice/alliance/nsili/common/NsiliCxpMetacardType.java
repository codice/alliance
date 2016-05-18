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

public class NsiliCxpMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".cxp."+ NSILI_METACARD_TYPE_POSTFIX;

    /**
     * A status field describing whether the Collection and Exploitation Plan is active, planned or expired.
     */
    public static final String STATUS = "status";

    /**
     * Metacard AttributeType for the NSILI CXP Status.
     */
    public static final AttributeType<NsiliCxpStatusType> NSILI_CXP_STATUS_TYPE;

    private static final Set<AttributeDescriptor> NSILI_CXP_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_CXP_STATUS_TYPE = new AttributeType<NsiliCxpStatusType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliCxpStatusType> getBinding() {
                return NsiliCxpStatusType.class;
            }
        };

        NSILI_CXP_DESCRIPTORS.add(new AttributeDescriptorImpl(STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                NSILI_CXP_STATUS_TYPE));

    }

    public NsiliCxpMetacardType() {
        super();
        attributeDescriptors.addAll(NsiliCxpMetacardType.NSILI_CXP_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
