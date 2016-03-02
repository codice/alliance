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
package org.codice.alliance.nato.stanag4559.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;

public class Stanag4559CxpMetacardType extends Stanag4559MetacardType {
    public static final String METACARD_TYPE_NAME = STANAG4559_METACARD_TYPE_PREFIX+".cxp."+STANAG4559_METACARD_TYPE_POSTFIX;

    /**
     * A status field describing whether the Collection and Exploitation Plan is active, planned or expired.
     */
    public static final String STATUS = "status";

    /**
     * Metacard AttributeType for the STANAG-4559 CXP Status.
     */
    public static final AttributeType<Stanag4559CxpStatusType> STANAG_4559_CXP_STATUS_TYPE;

    private static final Set<AttributeDescriptor> STANAG4559_CXP_DESCRIPTORS = new HashSet<>();

    static {
        STANAG_4559_CXP_STATUS_TYPE = new AttributeType<Stanag4559CxpStatusType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559CxpStatusType> getBinding() {
                return Stanag4559CxpStatusType.class;
            }
        };

        STANAG4559_CXP_DESCRIPTORS.add(new AttributeDescriptorImpl(STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_CXP_STATUS_TYPE));

    }

    public Stanag4559CxpMetacardType() {
        super();
        attributeDescriptors.addAll(Stanag4559CxpMetacardType.STANAG4559_CXP_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
