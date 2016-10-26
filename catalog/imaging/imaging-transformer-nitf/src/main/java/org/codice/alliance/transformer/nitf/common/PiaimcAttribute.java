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
 * TRE for "Profile for Imagery Access Image"
 */
public class PiaimcAttribute extends NitfAttributeImpl<Tre> {

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

    static final String CLOUDCVR_NAME = "CLOUDCVR";

    static final String SRP_NAME = "SRP";

    static final String SENSMODE_NAME = "SENSMODE";

    static final String SENSNAME_NAME = "SENSNAME";

    static final String SOURCE_NAME = "SOURCE";

    static final String COMGEN_NAME = "COMGEN";

    static final String SUBQUAL_NAME = "SUBQUAL";

    static final String PIAMSNNUM_NAME = "PIAMSNNUM";

    static final String CAMSPECS_NAME = "CAMSPECS";

    static final String PROJID_NAME = "PROJID";

    static final String GENERATION_NAME = "GENERATION";

    static final String ESD_NAME = "ESD";

    static final String OTHERCOND_NAME = "OTHERCOND";

    static final String MEANGSD_NAME = "MEANGSD";

    static final String IDATUM_NAME = "IDATUM";

    static final String IELLIP_NAME = "IELLIP";

    static final String PREPROC_NAME = "PREPROC";

    static final String IPROJ_NAME = "IPROJ";

    static final String SATTRACK_PATH_NAME = "SATTRACK_PATH";

    static final String SATTRACK_ROW_NAME = "SATTRACK_ROW";

    private static final String ATTRIBUTE_NAME_PREFIX = "piaimc.";

    static final PiaimcAttribute CLOUDCVR = new PiaimcAttribute(Isr.CLOUD_COVER,
            CLOUDCVR_NAME,
            PiaimcAttribute::getCloudCoverFunction,
            new IsrAttributes().getAttributeDescriptor(Isr.CLOUD_COVER),
            "cloud-cvr",
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SRP = new PiaimcAttribute("standard-radiometric-product",
            SRP_NAME,
            tre -> TreUtility.convertYnToBoolean(tre, SRP_NAME),
            BasicTypes.BOOLEAN_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SENSMODE = new PiaimcAttribute("sensor-mode",
            SENSMODE_NAME,
            tre -> TreUtility.convertToString(tre, SENSMODE_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SENSNAME = new PiaimcAttribute("sensor-name",
            SENSNAME_NAME,
            tre -> TreUtility.convertToString(tre, SENSNAME_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SOURCE = new PiaimcAttribute("source",
            SOURCE_NAME,
            tre -> TreUtility.convertToString(tre, SOURCE_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute COMGEN = new PiaimcAttribute("compression-generation",
            COMGEN_NAME,
            tre -> TreUtility.convertToInteger(tre, COMGEN_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SUBQUAL = new PiaimcAttribute("subjective-quality",
            SUBQUAL_NAME,
            tre -> TreUtility.convertToString(tre, SUBQUAL_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute PIAMSNNUM = new PiaimcAttribute("pia-mission-num",
            PIAMSNNUM_NAME,
            tre -> TreUtility.convertToString(tre, PIAMSNNUM_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute CAMSPECS = new PiaimcAttribute("camera-specs",
            CAMSPECS_NAME,
            tre -> TreUtility.convertToString(tre, CAMSPECS_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute PROJID = new PiaimcAttribute("project-id-code",
            PROJID_NAME,
            tre -> TreUtility.convertToString(tre, PROJID_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute GENERATION = new PiaimcAttribute("generation",
            GENERATION_NAME,
            tre -> TreUtility.convertToInteger(tre, GENERATION_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute ESD = new PiaimcAttribute("exploitation-support-data",
            ESD_NAME,
            tre -> TreUtility.convertYnToBoolean(tre, ESD_NAME),
            BasicTypes.BOOLEAN_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute OTHERCOND = new PiaimcAttribute("other-conditions",
            OTHERCOND_NAME,
            tre -> TreUtility.convertToString(tre, OTHERCOND_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute MEANGSD = new PiaimcAttribute("mean-gsd",
            MEANGSD_NAME,
            tre -> TreUtility.convertToFloat(tre, MEANGSD_NAME),
            BasicTypes.FLOAT_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute IDATUM = new PiaimcAttribute("image-datum",
            IDATUM_NAME,
            tre -> TreUtility.convertToString(tre, IDATUM_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute IELLIP = new PiaimcAttribute("image-ellipsoid",
            IELLIP_NAME,
            tre -> TreUtility.convertToString(tre, IELLIP_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute PREPROC = new PiaimcAttribute("image-processing-level",
            PREPROC_NAME,
            tre -> TreUtility.convertToString(tre, PREPROC_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute IPROJ = new PiaimcAttribute("image-projection-system",
            IPROJ_NAME,
            tre -> TreUtility.convertToString(tre, IPROJ_NAME),
            BasicTypes.STRING_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SATTRACK_PATH = new PiaimcAttribute("satellite-track-path",
            SATTRACK_PATH_NAME,
            tre -> TreUtility.convertToInteger(tre, SATTRACK_PATH_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    static final PiaimcAttribute SATTRACK_ROW = new PiaimcAttribute("satellite-track-row",
            SATTRACK_ROW_NAME,
            tre -> TreUtility.convertToInteger(tre, SATTRACK_ROW_NAME),
            BasicTypes.INTEGER_TYPE,
            ATTRIBUTE_NAME_PREFIX);

    private PiaimcAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType,
            String prefix) {
        super(longName, shortName, accessorFunction, attributeType, prefix);
        ATTRIBUTES.add(this);
    }

    private PiaimcAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeDescriptor attributeDescriptor,
            String extNitfName, String prefix) {
        super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName, prefix);
        ATTRIBUTES.add(this);
    }

    private static Serializable getCloudCoverFunction(Tre tre) {
        return Optional.ofNullable(TreUtility.getTreValue(tre, CLOUDCVR_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(Integer::valueOf)
                .filter(value -> value >= 0)
                .filter(value -> value <= 100)
                .orElse(null);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

}
