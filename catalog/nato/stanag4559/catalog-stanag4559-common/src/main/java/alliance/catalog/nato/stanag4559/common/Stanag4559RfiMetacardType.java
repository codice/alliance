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

public class Stanag4559RfiMetacardType extends Stanag4559MetacardType {
    public static final String METACARD_TYPE_NAME = STANAG4559_METACARD_TYPE_PREFIX+".rfi."+STANAG4559_METACARD_TYPE_POSTFIX;

    /**
     * A nation, command, agency, organization or unit requested to provide a response.
     */
    public static final String FOR_ACTION = "forAction";

    /**
     * A comma-separated list of nations, commands, agencies, organizations and units which may have an interest in the response.
     */
    public static final String FOR_INFORMATION = "forInformation";

    /**
     * Unique human readable string identifying the RFI instance.
     */
    public static final String SERIAL_NUMBER = "serialNumber";

    /**
     * Indicates the status of the RFI.s
     */
    public static final String STATUS = "status";

    /**
     * Indicates the workflow status of the RFI.
     */
    public static final String WORKFLOW_STATUS = "workflowStatus";

    /**
     * Metacard AttributeType for the STANAG-4559 RFI Status.
     */
    public static final AttributeType<Stanag4559RfiStatus> STANAG_4559_RFI_STATUS_TYPE;

    /**
     * Metacard AttributeType for the STANAG-4559 RFI Workflow Status.
     */
    public static final AttributeType<Stanag4559RfiWorkflowStatus> STANAG_4559_RFI_WORKFLOW_STATUS_TYPE;


    private static final Set<AttributeDescriptor> STANAG4559_RFI_DESCRIPTORS = new HashSet<>();

    static {
        STANAG_4559_RFI_STATUS_TYPE = new AttributeType<Stanag4559RfiStatus>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559RfiStatus> getBinding() {
                return Stanag4559RfiStatus.class;
            }
        };

        STANAG_4559_RFI_WORKFLOW_STATUS_TYPE = new AttributeType<Stanag4559RfiWorkflowStatus>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559RfiWorkflowStatus> getBinding() {
                return Stanag4559RfiWorkflowStatus.class;
            }
        };

        STANAG4559_RFI_DESCRIPTORS.add(new AttributeDescriptorImpl(FOR_ACTION,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        STANAG4559_RFI_DESCRIPTORS.add(new AttributeDescriptorImpl(FOR_INFORMATION,
                true /* indexed */,
                true /* stored */,
                true /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        STANAG4559_RFI_DESCRIPTORS.add(new AttributeDescriptorImpl(SERIAL_NUMBER,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        STANAG4559_RFI_DESCRIPTORS.add(new AttributeDescriptorImpl(STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_RFI_STATUS_TYPE));

        STANAG4559_RFI_DESCRIPTORS.add(new AttributeDescriptorImpl(WORKFLOW_STATUS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_RFI_WORKFLOW_STATUS_TYPE));

    }

    public Stanag4559RfiMetacardType() {
        super();
        attributeDescriptors.addAll(Stanag4559RfiMetacardType.STANAG4559_RFI_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
