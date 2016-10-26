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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE_NAME)).thenReturn(PLATFORM_CODE);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID_NAME)).thenReturn(VEHICLE_ID);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, is("XY02"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlatformIdPlatformOnly() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE_NAME)).thenReturn(PLATFORM_CODE);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID_NAME)).thenThrow(NitfFormatException.class);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlatformIdVehicleOnly() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE_NAME)).thenThrow(NitfFormatException.class);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID_NAME)).thenReturn(VEHICLE_ID);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlatformIdNeitherSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PLATFORM_CODE_NAME)).thenThrow(NitfFormatException.class);
        when(tre.getFieldValue(CsdidaAttribute.VEHICLE_ID_NAME)).thenThrow(NitfFormatException.class);

        Serializable actual = CsdidaAttribute.PLATFORM_ID.getAccessorFunction()
                .apply(tre);

        assertThat(actual, nullValue());
    }

    @Test
    public void testDayOfDatasetCollectionMin() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.DAY_NAME)).thenReturn("01");
        Serializable actual = CsdidaAttribute.DAY.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(1));
    }

    @Test
    public void testDayOfDatasetCollectionMax() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.DAY_NAME)).thenReturn("31");
        Serializable actual = CsdidaAttribute.DAY.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(31));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDayOfDatasetCollectionNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.DAY_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.DAY.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testMonOfDatasetCollectionSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.MONTH_NAME)).thenReturn("JAN");
        Serializable actual = CsdidaAttribute.MONTH.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("JAN"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMonthOfDatasetCollectionNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.MONTH_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.MONTH.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testYearOfDatasetCollectionMin() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.YEAR_NAME)).thenReturn("0000");
        Serializable actual = CsdidaAttribute.YEAR.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(0));
    }

    @Test
    public void testYearOfDatasetCollectionMax() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.YEAR_NAME)).thenReturn("9999");
        Serializable actual = CsdidaAttribute.YEAR.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(9999));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testYearOfDatasetCollectionNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.YEAR_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.YEAR.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testPassNumMin() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PASS_NAME)).thenReturn("01");
        Serializable actual = CsdidaAttribute.PASS.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(1));
    }

    @Test
    public void testPassNumMax() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PASS_NAME)).thenReturn("99");
        Serializable actual = CsdidaAttribute.PASS.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(99));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPassNumNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.PASS_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.PASS.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testOperationNumMin() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.OPERATION_NAME)).thenReturn("000");
        Serializable actual = CsdidaAttribute.OPERATION.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(0));
    }

    @Test
    public void testOperationNumMax() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.OPERATION_NAME)).thenReturn("999");
        Serializable actual = CsdidaAttribute.OPERATION.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(999));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOperationNumNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.OPERATION_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.OPERATION.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSensorIdSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.SENSOR_ID_NAME)).thenReturn("AA");
        Serializable actual = CsdidaAttribute.SENSOR_ID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("AA"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSensorIdNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.SENSOR_ID_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.SENSOR_ID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testProductIdSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PRODUCT_ID_NAME)).thenReturn("P2");
        Serializable actual = CsdidaAttribute.PRODUCT_ID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("P2"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProductIdNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.PRODUCT_ID_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.PRODUCT_ID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testTimeSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.TIME_NAME)).thenReturn("20160101231500");
        Serializable actual = CsdidaAttribute.TIME.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(instanceOf(Date.class)));
        Date convDate = (Date) actual;
        DateFormat dateFormat = new SimpleDateFormat(TreUtility.TRE_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertThat(dateFormat.format(convDate), is("20160101231500"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTimeNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.TIME_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.TIME.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testProcessTimeSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.PROCESS_TIME_NAME)).thenReturn("20160101231515");
        Serializable actual = CsdidaAttribute.PROCESS_TIME.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(instanceOf(Date.class)));
        Date convDate = (Date) actual;
        DateFormat dateFormat = new SimpleDateFormat(TreUtility.TRE_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertThat(dateFormat.format(convDate), is("20160101231515"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessTimeNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.PROCESS_TIME_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = CsdidaAttribute.PROCESS_TIME.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSoftwareVersionNumberSet() throws NitfFormatException {
        when(tre.getFieldValue(CsdidaAttribute.SOFTWARE_VERSION_NUMBER_NAME)).thenReturn("v1.1.0");
        Serializable actual = CsdidaAttribute.SOFTWARE_VERSION_NUMBER.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("v1.1.0"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSoftwareVersionNotSet() throws NitfFormatException {
        when(tre.getIntValue(CsdidaAttribute.SOFTWARE_VERSION_NUMBER_NAME)).thenThrow(
                NitfFormatException.class);
        Serializable actual = CsdidaAttribute.SOFTWARE_VERSION_NUMBER.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }
}
