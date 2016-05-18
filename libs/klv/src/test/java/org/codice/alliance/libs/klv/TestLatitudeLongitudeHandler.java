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
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.junit.Before;
import org.junit.Test;

public class TestLatitudeLongitudeHandler {

    private static final String LAT = "lat";

    private static final String LON = "lon";

    private LatitudeLongitudeHandler klvHandler;

    @Before
    public void setup() {
        klvHandler = new LatitudeLongitudeHandler("field", LAT, LON);
    }

    @Test
    public void testGetLongitudeFieldName() {
        assertThat(klvHandler.getLongitudeFieldName(), is(LON));
    }

    @Test
    public void testGetLatitudeFieldName() {
        assertThat(klvHandler.getLatitudeFieldName(), is(LAT));
    }

    @Test
    public void testGetRawGeoData() throws KlvDecodingException {
        klvHandler.accept(KlvUtilities.createTestFloat(LAT, 10.0));
        assertThat(klvHandler.getRawGeoData()
                .get(LAT)
                .get(0), closeTo(10.0, 0.01));
    }

    @Test
    public void testEmpty() {
        assertThat(klvHandler.asAttribute()
                .isPresent(), is(false));
    }

    @Test
    public void testAcceptingData() throws KlvDecodingException {

        double expectedLatitude = 33;
        double expectedLongitude = -112;

        klvHandler.accept(KlvUtilities.createTestFloat(LAT, expectedLatitude));
        klvHandler.accept(KlvUtilities.createTestFloat(LON, expectedLongitude));

        String r = (String) klvHandler.asAttribute()
                .get()
                .getValue();

        Pattern p = Pattern.compile("POINT \\(([^ ]+) ([^)]+)\\)");

        Matcher m = p.matcher(r);

        assertThat(m.matches(), is(true));
        assertThat(m.groupCount(), is(2));

        assertThat(Double.parseDouble(m.group(1)), is(closeTo(expectedLongitude, 0.01)));
        assertThat(Double.parseDouble(m.group(2)), is(closeTo(expectedLatitude, 0.01)));

    }

    @Test
    public void testAcceptWrongType() {

        KlvInt klvInt = mock(KlvInt.class);

        klvHandler.accept(klvInt);

        assertThat(klvHandler.asAttribute()
                .isPresent(), is(false));

    }
}
