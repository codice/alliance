package org.codice.alliance.libs.klv;

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

public class LocationCountryCodeKlvProcessor extends DistinctKlvProcessor {
    protected LocationCountryCodeKlvProcessor() {
        super(AttributeNameConstants.LOCATION_COUNTY_CODE,
                Stanag4609TransportStreamParser.CLASSIFYING_COUNTRY);
    }
}
