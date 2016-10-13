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

import static org.hamcrest.Matchers.instanceOf;
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

public class PiaimcAttributeTest {

    private Tre tre;

    @Before
    public void setup() {
        tre = mock(Tre.class);
    }

    @Test
    public void testCloudCover() throws NitfFormatException {
        for (int cloudCover = 0; cloudCover <= 100; cloudCover++) {
            when(tre.getFieldValue(PiaimcAttribute.CLOUDCVR_NAME)).thenReturn(Integer.toString(cloudCover));
            Serializable actual = PiaimcAttribute.CLOUDCVR.getAccessorFunction()
                    .apply(tre);
            assertThat(actual, is(instanceOf(Integer.class)));
            assertThat(actual, is(cloudCover));
        }
    }

    @Test
    public void testCloudCoverTooLow() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.CLOUDCVR_NAME)).thenReturn(-10);
        Serializable actual = PiaimcAttribute.CLOUDCVR.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testCloudCoverTooHigh() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.CLOUDCVR_NAME)).thenReturn(110);
        Serializable actual = PiaimcAttribute.CLOUDCVR.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCloudCoverNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.CLOUDCVR_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.CLOUDCVR.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

}