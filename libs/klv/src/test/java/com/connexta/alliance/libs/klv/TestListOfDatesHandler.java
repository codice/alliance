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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.codice.ddf.libs.klv.data.numerical.KlvInt;
import org.codice.ddf.libs.klv.data.numerical.KlvLong;
import org.junit.Before;
import org.junit.Test;

public class TestListOfDatesHandler {

    private KlvHandler klvHandler;

    @Before
    public void setup() {
        klvHandler = new ListOfDatesHandler("field");
    }

    @Test
    public void testEmpty() {
        assertThat(klvHandler.asAttribute()
                .isPresent(), is(false));
    }

    @Test
    public void testAcceptingData() {
        Date testDate = new Date();

        KlvLong klvLong = mock(KlvLong.class);
        when(klvLong.getValue()).thenReturn(TimeUnit.MILLISECONDS.toMicros(testDate.getTime()));

        klvHandler.accept(klvLong);

        assertThat(klvHandler.asAttribute()
                .isPresent(), is(true));

        assertThat(klvHandler.asAttribute()
                .get()
                .getValue(), is(testDate));
    }

    @Test
    public void testAcceptWrongType() {

        KlvInt klvInt = mock(KlvInt.class);

        klvHandler.accept(klvInt);

        assertThat(klvHandler.asAttribute()
                .isPresent(), is(false));

    }
}
