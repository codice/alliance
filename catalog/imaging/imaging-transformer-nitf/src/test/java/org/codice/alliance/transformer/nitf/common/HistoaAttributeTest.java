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

public class HistoaAttributeTest {

    private Tre tre;

    @Before
    public void setup() {
        tre = mock(Tre.class);
    }

    @Test
    public void testSystypeSet() throws NitfFormatException {
        when(tre.getFieldValue(HistoaAttribute.SYSTYPE_NAME)).thenReturn("TestType");

        Serializable actual = HistoaAttribute.SYSTYPE.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is("TestType"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSystypeNotSet() throws NitfFormatException {
        when(tre.getIntValue(HistoaAttribute.SYSTYPE_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = HistoaAttribute.SYSTYPE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testPcSet() throws NitfFormatException {
        when(tre.getFieldValue(HistoaAttribute.PC_NAME)).thenReturn("TestPC");

        Serializable actual = HistoaAttribute.PC.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is("TestPC"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPcNotSet() throws NitfFormatException {
        when(tre.getIntValue(HistoaAttribute.PC_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = HistoaAttribute.PC.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testPeSet() throws NitfFormatException {
        when(tre.getFieldValue(HistoaAttribute.PE_NAME)).thenReturn("TestPE");

        Serializable actual = HistoaAttribute.PE.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is("TestPE"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPeNotSet() throws NitfFormatException {
        when(tre.getIntValue(HistoaAttribute.PE_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = HistoaAttribute.PE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testRemapFlagSet() throws NitfFormatException {
        when(tre.getFieldValue(HistoaAttribute.REMAP_FLAG_NAME)).thenReturn("N");

        Serializable actual = HistoaAttribute.REMAP_FLAG.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is("N"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemapFlagNotSet() throws NitfFormatException {
        when(tre.getIntValue(HistoaAttribute.REMAP_FLAG_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = HistoaAttribute.REMAP_FLAG.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testDataMappingIdSet() throws NitfFormatException {
        when(tre.getFieldValue(HistoaAttribute.LUTID_NAME)).thenReturn("01");

        Serializable actual = HistoaAttribute.LUTID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDataMappingFlagNotSet() throws NitfFormatException {
        when(tre.getIntValue(HistoaAttribute.LUTID_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = HistoaAttribute.LUTID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

}
