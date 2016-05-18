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

public class NsiliTaskMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".task."+ NSILI_METACARD_TYPE_POSTFIX;

    /**
     * Comments on the task.
     */
    public static final String COMMENTS = "comments";

    /**
     * A status of the task
     */
    public static final String STATUS = "status";

    /**
     * Metacard AttributeType for the NSILI Task Status.
     */
    public static final AttributeType<NsiliTaskStatus> NSILI_TASK_STATUS_TYPE;

    private static final Set<AttributeDescriptor> NSILI_TASK_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_TASK_STATUS_TYPE = new AttributeType<NsiliTaskStatus>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliTaskStatus> getBinding() {
                return NsiliTaskStatus.class;
            }
        };

        NSILI_TASK_DESCRIPTORS.add(new AttributeDescriptorImpl(COMMENTS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_TASK_DESCRIPTORS.add(new AttributeDescriptorImpl(STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */, NSILI_TASK_STATUS_TYPE));

    }

    public NsiliTaskMetacardType() {
        super();
        attributeDescriptors.addAll(NsiliTaskMetacardType.NSILI_TASK_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
