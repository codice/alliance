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

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class CsdidaAttributeTest {

    private static final String PLATFORM_CODE = "XY";

    private static final String VEHICLE_ID = "02";

    private Tre tre;

    @Before
    public void setup() {
        tre = mock(Tre.class);
    }

    @Test
    public void testPlatformIdBothSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE)).thenReturn(PLATFORM_CODE);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID)).thenReturn(VEHICLE_ID);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is("XY02"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlatformIdPlatformOnly() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE)).thenReturn(PLATFORM_CODE);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID)).thenThrow(NitfFormatException.class);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlatformIdVehicleOnly() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE)).thenThrow(NitfFormatException.class);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID)).thenReturn(VEHICLE_ID);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlatformIdNeitherSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE)).thenThrow(NitfFormatException.class);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID)).thenThrow(NitfFormatException.class);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

}
