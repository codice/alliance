/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.libs.klv;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.junit.Before;
import org.junit.Test;

public class TestListOfBasicKlvDataTypesHandler {

    private KlvHandler handler;

    @Before
    public void setup() {
        handler = new ListOfBasicKlvDataTypesHandler<>("field",
                KlvIntegerEncodedFloatingPoint.class);
    }

    @Test
    public void testEmpty() {
        assertThat(handler.asAttribute()
                .isPresent(), is(false));
    }

    @Test
    public void testAcceptWrongType() {

        KlvInt klvInt = mock(KlvInt.class);

        handler.accept(klvInt);

        assertThat(handler.asAttribute()
                .isPresent(), is(false));

    }

    @Test
    public void testAcceptingData() throws KlvDecodingException {

        double expected = 100;

        handler.accept(KlvUtilities.createTestFloat("a", expected));

        assertThat(handler.asAttribute()
                .isPresent(), is(true));

        assertThat(((Double) handler.asAttribute()
                .get()
                .getValue()), is(closeTo(expected, 0.001)));

    }

}
