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

import static org.hamcrest.Matchers.closeTo;
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

public class CsexraAttributeTest {

    private static final Double DELTA = 0.01;

    private Tre tre;

    @Before
    public void setup() {
        tre = mock(Tre.class);
    }

    @Test
    public void testGroundCoverTrue() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.GRD_COVER)).thenReturn(1);
        Serializable actual = CsexraAttribute.SNOW_COVER.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(instanceOf(Boolean.class)));
        assertThat(actual, is(Boolean.TRUE));
    }

    @Test
    public void testGroundCoverFalse() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.GRD_COVER)).thenReturn(0);
        Serializable actual = CsexraAttribute.SNOW_COVER.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(instanceOf(Boolean.class)));
        assertThat(actual, is(Boolean.FALSE));
    }

    @Test
    public void testGroundCoverOther() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.GRD_COVER)).thenReturn(5);
        Serializable actual = CsexraAttribute.SNOW_COVER.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGroundCoverNotSet() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.GRD_COVER)).thenThrow(NitfFormatException.class);
        Serializable actual = CsexraAttribute.SNOW_COVER.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testNiirs() throws NitfFormatException {
        when(tre.getFieldValue(NitfConstants.PREDICTED_NIIRS)).thenReturn("3.1");

        Serializable actual = CsexraAttribute.PREDICTED_NIIRS.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Integer.class)));
        assertThat(actual, is(3));

    }

    @Test
    public void testNiirsOther() throws NitfFormatException {
        when(tre.getFieldValue(NitfConstants.PREDICTED_NIIRS)).thenReturn("N/A");

        Serializable actual = CsexraAttribute.PREDICTED_NIIRS.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNiirsNotSet() throws NitfFormatException {
        when(tre.getFieldValue(NitfConstants.PREDICTED_NIIRS)).thenThrow(NitfFormatException.class);

        Serializable actual = CsexraAttribute.PREDICTED_NIIRS.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());

    }

    @Test
    public void testSnowDepthMinCategory0() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(0);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(0, DELTA)));
    }

    @Test
    public void testSnowDepthMinCategory1() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(1);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(2.54, DELTA)));
    }

    @Test
    public void testSnowDepthMinCategory2() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(2);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(22.86, DELTA)));
    }

    @Test
    public void testSnowDepthMinCategory3() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(3);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(43.18, DELTA)));
    }

    @Test
    public void testSnowDepthMinCategoryOther() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(10);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSnowDepthMinCategoryNotSet() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenThrow(NitfFormatException.class);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @Test
    public void testSnowDepthMaxCategory0() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(0);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(2.54, DELTA)));
    }

    @Test
    public void testSnowDepthMaxCategory1() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(1);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(22.86, DELTA)));
    }

    @Test
    public void testSnowDepthMaxCategory2() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(2);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(43.18, DELTA)));
    }

    @Test
    public void testSnowDepthMaxCategory3() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(3);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(instanceOf(Double.class)));
        assertThat((Double) actual, is(closeTo(Double.MAX_VALUE, DELTA)));
    }

    @Test
    public void testSnowDepthMaxCategoryOther() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenReturn(10);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSnowDepthMaxCategoryNotSet() throws NitfFormatException {
        when(tre.getIntValue(NitfConstants.SNOW_DEPTH_CAT)).thenThrow(NitfFormatException.class);

        Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

}
