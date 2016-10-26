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
            when(tre.getFieldValue(PiaimcAttribute.CLOUDCVR_NAME)).thenReturn(Integer.toString(
                    cloudCover));
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

    @Test
    public void testSrpTrue() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SRP_NAME)).thenReturn("Y");
        Serializable actual = PiaimcAttribute.SRP.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(true));
    }

    @Test
    public void testSrpFalse() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SRP_NAME)).thenReturn("N");
        Serializable actual = PiaimcAttribute.SRP.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSrpNotSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SRP_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SRP.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSenseModeSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SENSMODE_NAME)).thenReturn("PUSHBROOM");
        Serializable actual = PiaimcAttribute.SENSMODE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("PUSHBROOM"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSenseModeNotSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SENSMODE_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SENSMODE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSensorNameSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SENSNAME_NAME)).thenReturn("OrbView");
        Serializable actual = PiaimcAttribute.SENSNAME.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("OrbView"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSensorNameNotSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SENSNAME_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SENSNAME.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSourceSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SOURCE_NAME)).thenReturn("Test Source");
        Serializable actual = PiaimcAttribute.SOURCE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("Test Source"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSourceNotSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SOURCE_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SOURCE.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testComgenMin() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.COMGEN_NAME)).thenReturn("0");
        Serializable actual = PiaimcAttribute.COMGEN.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(0));
    }

    @Test
    public void testComgenMax() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.COMGEN_NAME)).thenReturn("99");
        Serializable actual = PiaimcAttribute.COMGEN.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(99));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testComgenNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.COMGEN_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.COMGEN.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSubqualSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SUBQUAL_NAME)).thenReturn("G");
        Serializable actual = PiaimcAttribute.SUBQUAL.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("G"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubqualNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.SUBQUAL_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SUBQUAL.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testPIAMsnNumSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.PIAMSNNUM_NAME)).thenReturn("TESTMSN");
        Serializable actual = PiaimcAttribute.PIAMSNNUM.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("TESTMSN"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPIAMsnNumNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.PIAMSNNUM_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.PIAMSNNUM.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testCameraSpecsSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.CAMSPECS_NAME)).thenReturn("Test Camera Specs");
        Serializable actual = PiaimcAttribute.CAMSPECS.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("Test Camera Specs"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCameraSpecsNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.CAMSPECS_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.CAMSPECS.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testProjectIdSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.PROJID_NAME)).thenReturn("AB");
        Serializable actual = PiaimcAttribute.PROJID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("AB"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProjectIdNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.PROJID_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.PROJID.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testGenerationSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.GENERATION_NAME)).thenReturn("1");
        Serializable actual = PiaimcAttribute.GENERATION.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenerationNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.GENERATION_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.GENERATION.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testEsdTrue() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.ESD_NAME)).thenReturn("Y");
        Serializable actual = PiaimcAttribute.ESD.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(true));
    }

    @Test
    public void testEsdFalse() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.ESD_NAME)).thenReturn("N");
        Serializable actual = PiaimcAttribute.ESD.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEsdNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.ESD_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.ESD.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testOtherCondSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.OTHERCOND_NAME)).thenReturn("AZ");
        Serializable actual = PiaimcAttribute.OTHERCOND.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("AZ"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOtherCondNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.OTHERCOND_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.OTHERCOND.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testMeanGSDMin() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.MEANGSD_NAME)).thenReturn("00000.0");
        Serializable actual = PiaimcAttribute.MEANGSD.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(0.0f));
    }

    @Test
    public void testMeanGSDMax() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.MEANGSD_NAME)).thenReturn("99999.9");
        Serializable actual = PiaimcAttribute.MEANGSD.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(99999.9f));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMeanGSDNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.MEANGSD_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.MEANGSD.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testIdatumSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.IDATUM_NAME)).thenReturn("WGS");
        Serializable actual = PiaimcAttribute.IDATUM.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("WGS"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIdatumNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.IDATUM_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.IDATUM.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testIellipSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.IELLIP_NAME)).thenReturn("WGE");
        Serializable actual = PiaimcAttribute.IELLIP.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("WGE"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIellipNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.IELLIP_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.IELLIP.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testImageProcessingLevelSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.PREPROC_NAME)).thenReturn("AA");
        Serializable actual = PiaimcAttribute.PREPROC.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("AA"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testImageProcessingLevelNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.PREPROC_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.PREPROC.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testImageProjectionSystemSet() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.IPROJ_NAME)).thenReturn("AA");
        Serializable actual = PiaimcAttribute.IPROJ.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is("AA"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testImageProjectionSystemNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.IPROJ_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.IPROJ.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSatTrackPathSetMin() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SATTRACK_PATH_NAME)).thenReturn("0001");
        Serializable actual = PiaimcAttribute.SATTRACK_PATH.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(1));
    }

    @Test
    public void testSatTrackPathSetMax() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SATTRACK_PATH_NAME)).thenReturn("9999");
        Serializable actual = PiaimcAttribute.SATTRACK_PATH.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(9999));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSatTrackPathNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.SATTRACK_PATH_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SATTRACK_PATH.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }

    @Test
    public void testSatTrackRowSetMin() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SATTRACK_ROW_NAME)).thenReturn("0001");
        Serializable actual = PiaimcAttribute.SATTRACK_ROW.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(1));
    }

    @Test
    public void testSatTrackRowSetMax() throws NitfFormatException {
        when(tre.getFieldValue(PiaimcAttribute.SATTRACK_ROW_NAME)).thenReturn("9999");
        Serializable actual = PiaimcAttribute.SATTRACK_ROW.getAccessorFunction()
                .apply(tre);
        assertThat(actual, is(9999));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSatTrackRowNotSet() throws NitfFormatException {
        when(tre.getIntValue(PiaimcAttribute.SATTRACK_ROW_NAME)).thenThrow(NitfFormatException.class);
        Serializable actual = PiaimcAttribute.SATTRACK_ROW.getAccessorFunction()
                .apply(tre);
        assertThat(actual, nullValue());
    }
}