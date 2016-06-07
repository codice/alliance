/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.gmti;

import java.io.Serializable;
import java.util.function.Function;

import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.tre.Tre;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

enum AcftbAttribute implements NitfAttribute<Tre> {
    AIRCRAFT_MISSION_ID("aircraftMissionId",
            "AC_MSN_ID",
            tre -> GmtiTreUtility.getTreValue(tre, "AC_MSN_ID")),

    AIRCRAFT_TAIL_NUMBER("aircraftTailNumber",
            "AC_TAIL_NO",
            tre -> GmtiTreUtility.getTreValue(tre, "AC_TAIL_NO")),

    AIRCRAFT_TAKEOFF("aircraftTakeOff", "AC_TO", tre -> GmtiTreUtility.getTreValue(tre, "AC_TO")),

    SENSOR_ID_TYPE("sensorIdType",
            "SENSOR_ID_TYPE",
            tre -> GmtiTreUtility.getTreValue(tre, "SENSOR_ID_TYPE")),

    SENSOR_ID("sensorId", "SENSOR_ID", tre -> GmtiTreUtility.getTreValue(tre, "SENSOR_ID")),

    SCENE_SOURCE("sceneSource",
            "SCENE_SOURCE",
            tre -> GmtiTreUtility.getTreValue(tre, "SCENE_SOURCE")),

    SCENE_NUMBER("sceneNumber", "SCNUM", tre -> GmtiTreUtility.getTreValue(tre, "SCNUM")),

    PROCESSING_DATE("processingDate", "PDATE", tre -> GmtiTreUtility.getTreValue(tre, "PDATE")),

    IMMEDIATE_SCENE_HOST("immediateSceneHost",
            "IMHOSTNO",
            tre -> GmtiTreUtility.getTreValue(tre, "IMHOSTNO")),

    IMMEDIATE_SCENE_REQUEST_ID("immediateSceneRequestId",
            "IMREQID",
            tre -> GmtiTreUtility.getTreValue(tre, "IMREQID")),

    MISSION_PLAN_MODE("missionPlanMode", "MPLAN", tre -> GmtiTreUtility.getTreValue(tre, "MPLAN"));

    private static final String ATTRIBUTE_NAME_PREFIX = "nitf.acftb.";

    private String shortName;

    private String longName;

    private Function<Tre, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;

    AcftbAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction) {
        this.longName = longName;
        this.shortName = shortName;
        this.accessorFunction = accessorFunction;
        this.attributeDescriptor = new AttributeDescriptorImpl(ATTRIBUTE_NAME_PREFIX + longName,
                true,
                true,
                false,
                true,
                BasicTypes.STRING_TYPE);
    }

    @Override
    public String getLongName() {
        return this.longName;
    }

    @Override
    public String getShortName() {
        return this.shortName;
    }

    @Override
    public Function<Tre, Serializable> getAccessorFunction() {
        return this.accessorFunction;
    }

    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }
}
