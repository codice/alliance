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

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.codice.ddf.libs.klv.KlvDecodingException;
import org.junit.Before;
import org.junit.Test;

public class LoggingKlvHandlerTest {

    private LoggingKlvHandler loggingKlvHandler;

    @Before
    public void setup() {
        loggingKlvHandler = new LoggingKlvHandler();
    }

    @Test
    public void testAccept() throws KlvDecodingException {

        loggingKlvHandler.accept(KlvUtilities.createTestFloat("name", 1));

        assertThat(loggingKlvHandler.asAttribute()
                .isPresent(), is(false));

    }

    /**
     * The LoggingKlvHandler always return null for the attribute name.
     */
    @Test
    public void testAttributeName() {
        assertThat(loggingKlvHandler.getAttributeName(), is(nullValue()));
    }

}
