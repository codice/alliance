package org.codice.alliance.video.stream.mpegts.metacard;

import org.codice.alliance.libs.klv.AttributeNameConstants;

/**
 * Set the parent to the union of the child and parent values.
 */
public class LocationCountryCodeMetacardUpdater extends UnionMetacardUpdater {
    public LocationCountryCodeMetacardUpdater() {
        super(AttributeNameConstants.LOCATION_COUNTY_CODE);
    }
}
