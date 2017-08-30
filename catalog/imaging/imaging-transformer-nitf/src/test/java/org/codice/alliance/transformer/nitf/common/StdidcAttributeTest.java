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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class StdidcAttributeTest {

    private Tre tre;

    @Before
    public void setup() {
        tre = mock(Tre.class);
    }

    @Test
    public void testValidCountryCode() throws Exception {
        when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn("US");
        Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("USA"));
        actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("US"));
    }

    @Test
    public void testInvalidCountryCode() throws Exception {
        when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn("0");
        Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("0"));
        actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("0"));
    }

    @Test
    public void testEmptyCountryCode() throws Exception {
        when(tre.getFieldValue(StdidcAttribute.COUNTRY_SHORT_NAME)).thenReturn(null);
        Serializable actual = StdidcAttribute.COUNTRY_ALPHA3_ATTRIBUTE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(nullValue()));
        actual = StdidcAttribute.COUNTRY_ATTRIBUTE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }
}
