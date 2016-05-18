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
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class NsiliGmtiMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".gmti."+ NSILI_METACARD_TYPE_POSTFIX;

    /**
     * A platform-assigned number identifying the specific requestor task to which the packet pertains.
     * The Job ID shall be unique within a mission.
     */
    public static final String JOB_ID = "jobId";

    /**
     * The total number of target reports within all the dwells in the file.
     */
    public static final String NUM_TARGET_REPORTS = "numTargetReports";

    private static final Set<AttributeDescriptor> NSILI_GMTI_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_GMTI_DESCRIPTORS.add(new AttributeDescriptorImpl(JOB_ID,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.FLOAT_TYPE));

        NSILI_GMTI_DESCRIPTORS.add(new AttributeDescriptorImpl(NUM_TARGET_REPORTS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));
    }

    public NsiliGmtiMetacardType() {
        super();
        attributeDescriptors.addAll(NsiliGmtiMetacardType.NSILI_GMTI_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }

}
