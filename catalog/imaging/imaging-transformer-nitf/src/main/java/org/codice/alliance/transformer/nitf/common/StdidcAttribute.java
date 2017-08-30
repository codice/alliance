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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.types.Location;

/**
 * TRE for "STDIDC "Standard ID"
 */
public class StdidcAttribute extends NitfAttributeImpl<Tre> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfAttributeImpl.class);

    private static final List<NitfAttribute<Tre>> ATTRIBUTES = new ArrayList<>();

    private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "stdidc.";

    public static final String ACQUISITION_DATE_SHORT_NAME = "ACQUISITION_DATE";

    public static final String COUNTRY_SHORT_NAME = "COUNTRY";

    public static final String LOCATION_SHORT_NAME = "LOCATION";

    public static final String WAC_SHORT_NAME = "WAC";

    public static final String MISSION_SHORT_NAME = "MISSION";

    public static final String OP_NUM_SHORT_NAME = "OP_NUM";

    public static final String PASS_SHORT_NAME = "PASS";

    public static final String REPLAY_REGEN_SHORT_NAME = "REPLAY_REGEN";

    public static final String REPRO_NUM_SHORT_NAME = "REPRO_NUM";

    public static final String START_COLUMN_SHORT_NAME = "START_COLUMN";

    public static final String START_ROW_SHORT_NAME = "START_ROW";

    public static final String START_SEGMENT_SHORT_NAME = "START_SEGMENT";

    public static final String END_COLUMN_SHORT_NAME = "END_COLUMN";

    public static final String END_ROW_SHORT_NAME = "END_ROW";

    public static final String END_SEGMENT_SHORT_NAME = "END_SEGMENT";

    public static final String ACQUISITION_DATE = PREFIX + "acquisition-date";

    public static final String COUNTRY = PREFIX + "country";

    public static final String LOCATION = PREFIX + "location";

    public static final String WAC = PREFIX + "wac";

    public static final String MISSION = PREFIX + "mission";

    public static final String OP_NUM = PREFIX + "op-num";

    public static final String PASS = PREFIX + "pass";

    public static final String REPLAY_REGEN = PREFIX + "replay-regen";

    public static final String REPRO_NUM = PREFIX + "repo-num";

    public static final String START_COLUMN = PREFIX + "start-column";

    public static final String START_ROW = PREFIX + "start-row";

    public static final String START_SEGMENT = PREFIX + "start-segment";

    public static final String END_COLUMN = PREFIX + "end-column";

    public static final String END_ROW = PREFIX + "end-row";

    public static final String END_SEGMENT = PREFIX + "end-segment";

    static final StdidcAttribute ACQUISITION_DATE_ATTRIBUTE = new StdidcAttribute(ACQUISITION_DATE,
            ACQUISITION_DATE_SHORT_NAME,
            tre -> TreUtility.convertToDate(tre, ACQUISITION_DATE_SHORT_NAME),
            BasicTypes.DATE_TYPE);

    static final StdidcAttribute COUNTRY_ATTRIBUTE = new StdidcAttribute(COUNTRY,
            COUNTRY_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, COUNTRY_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute COUNTRY_ALPHA3_ATTRIBUTE =
            new StdidcAttribute(Location.COUNTRY_CODE,
                    COUNTRY_SHORT_NAME,
                    tre -> getAlpha3CountryCode(TreUtility.convertToString(tre,
                            COUNTRY_SHORT_NAME)),
                    BasicTypes.STRING_TYPE);

    static final StdidcAttribute LOCATION_ATTRIBUTE = new StdidcAttribute(LOCATION,
            LOCATION_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, LOCATION_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute WAC_ATTRIBUTE = new StdidcAttribute(WAC,
            WAC_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, WAC_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute MISSION_ATTRIBUTE = new StdidcAttribute(MISSION,
            MISSION_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, MISSION_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute OP_NUM_ATTRIBUTE = new StdidcAttribute(OP_NUM,
            OP_NUM_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, OP_NUM_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute PASS_ATTRIBUTE = new StdidcAttribute(PASS,
            PASS_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, PASS_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute REPLAY_REGEN_ATTRIBUTE = new StdidcAttribute(REPLAY_REGEN,
            REPLAY_REGEN_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, REPLAY_REGEN_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute REPRO_NUM_ATTRIBUTE = new StdidcAttribute(REPRO_NUM,
            REPRO_NUM_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, REPRO_NUM_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute START_COLUMN_ATTRIBUTE = new StdidcAttribute(START_COLUMN,
            START_COLUMN_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, START_COLUMN_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute START_ROW_ATTRIBUTE = new StdidcAttribute(START_ROW,
            START_ROW_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, START_ROW_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute START_SEGMENT_ATTRIBUTE = new StdidcAttribute(START_SEGMENT,
            START_SEGMENT_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, START_SEGMENT_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute END_COLUMN_ATTRIBUTE = new StdidcAttribute(END_COLUMN,
            END_COLUMN_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, END_COLUMN_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute END_ROW_ATTRIBUTE = new StdidcAttribute(END_ROW,
            END_ROW_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, END_ROW_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    static final StdidcAttribute END_SEGMENT_ATTRIBUTE = new StdidcAttribute(END_SEGMENT,
            END_SEGMENT_SHORT_NAME,
            tre -> TreUtility.convertToString(tre, END_SEGMENT_SHORT_NAME),
            BasicTypes.STRING_TYPE);

    private StdidcAttribute(String longName, String shortName,
            Function<Tre, Serializable> accessorFunction, AttributeType attributeType) {
        super(longName, shortName, accessorFunction, attributeType);
        ATTRIBUTES.add(this);
    }

    public static List<NitfAttribute<Tre>> getAttributes() {
        return Collections.unmodifiableList(ATTRIBUTES);
    }

    /**
     * Get the alpha3 country code for and alpha2 country code.
     * in alpha2 code.
     * @param alpha2CountryCode the alpha2 country code.
     * @return Returns null if null is passed in. If the alpha3 code lookup fails it will return the passed in alpha2 code. Otherwise returns the alpha3 country code.
     */
    private static String getAlpha3CountryCode(String alpha2CountryCode) {
        if (alpha2CountryCode == null) {
            return alpha2CountryCode;
        }

        try {
            return new Locale(Locale.ENGLISH.getLanguage(), alpha2CountryCode).getISO3Country();
        } catch (MissingResourceException e) {
            LOGGER.debug(
                    "Failed to convert country code {} to alpha-3 format. Returning the original alpha2 country code",
                    alpha2CountryCode,
                    e);
            return alpha2CountryCode;
        }
    }

}
