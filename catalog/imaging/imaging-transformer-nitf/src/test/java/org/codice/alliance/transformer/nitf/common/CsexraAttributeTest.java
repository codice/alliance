/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.common;

import static org.hamcrest.Matchers.closeTo;
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

  private static final Float DELTA = 0.01f;

  private Tre tre;

  @Before
  public void setup() {
    tre = mock(Tre.class);
  }

  @Test
  public void testGroundCoverTrue() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GRD_COVER_SHORT_NAME)).thenReturn("1");
    Serializable actual = CsexraAttribute.SNOW_COVER_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is(Boolean.TRUE));
  }

  @Test
  public void testGroundCoverFalse() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GRD_COVER_SHORT_NAME)).thenReturn("0");
    Serializable actual = CsexraAttribute.SNOW_COVER_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is(Boolean.FALSE));
  }

  @Test
  public void testGroundCoverOther() throws NitfFormatException {
    when(tre.getIntValue(CsexraAttribute.GRD_COVER_SHORT_NAME)).thenReturn(5);
    Serializable actual = CsexraAttribute.SNOW_COVER_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is(nullValue()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGroundCoverNotSet() throws NitfFormatException {
    when(tre.getIntValue(CsexraAttribute.GRD_COVER_SHORT_NAME))
        .thenThrow(NitfFormatException.class);
    Serializable actual = CsexraAttribute.SNOW_COVER_ATTRIBUTE.getAccessorFunction().apply(tre);
    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testNiirs() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.PREDICTED_NIIRS_SHORT_NAME)).thenReturn("3.1");

    Serializable actual =
        CsexraAttribute.PREDICTED_NIIRS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(3.1f));
  }

  @Test
  public void testNiirsOther() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.PREDICTED_NIIRS_SHORT_NAME)).thenReturn("N/A");

    Serializable actual =
        CsexraAttribute.PREDICTED_NIIRS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testNiirsEmptyString() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.PREDICTED_NIIRS_SHORT_NAME)).thenReturn("");

    Serializable actual =
        CsexraAttribute.PREDICTED_NIIRS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, nullValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNiirsNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.PREDICTED_NIIRS_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual =
        CsexraAttribute.PREDICTED_NIIRS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSnowDepthMinCategory0() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("0");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(0, DELTA)));
  }

  @Test
  public void testSnowDepthMinCategory1() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("1");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(2.54, DELTA)));
  }

  @Test
  public void testSnowDepthMinCategory2() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("2");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(22.86, DELTA)));
  }

  @Test
  public void testSnowDepthMinCategory3() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("3");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(43.18, DELTA)));
  }

  @Test
  public void testSnowDepthMinCategoryOther() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("10");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSnowDepthMinCategoryEmptyString() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, nullValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSnowDepthMinCategoryNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSnowDepthMaxCategory0() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("0");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(2.54, DELTA)));
  }

  @Test
  public void testSnowDepthMaxCategory1() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("1");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(22.86, DELTA)));
  }

  @Test
  public void testSnowDepthMaxCategory2() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("2");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(43.18, DELTA)));
  }

  @Test
  public void testSnowDepthMaxCategory3() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("3");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(((Float) actual).doubleValue(), is(closeTo(Float.MAX_VALUE, DELTA)));
  }

  @Test
  public void testSnowDepthMaxCategoryOther() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("10");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSnowDepthMaxCategoryNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.SNOW_DEPTH_MAX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSensorNameIsMs() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SENSOR_SHORT_NAME)).thenReturn("MS");

    Serializable actual = CsexraAttribute.SENSOR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("MS"));
  }

  @Test
  public void testSensorNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SENSOR_SHORT_NAME)).thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.SENSOR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testTimeFirstLineImageMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.TIME_FIRST_LINE_IMAGE_SHORT_NAME))
        .thenReturn("00000.000000");

    Serializable actual =
        CsexraAttribute.TIME_FIRST_LINE_IMAGE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(00000.000000f));
  }

  @Test
  public void testTimeFirstLineImageMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.TIME_FIRST_LINE_IMAGE_SHORT_NAME))
        .thenReturn("86400.000000");

    Serializable actual =
        CsexraAttribute.TIME_FIRST_LINE_IMAGE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(86400.000000f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTimeFirstLineNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.TIME_FIRST_LINE_IMAGE_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual =
        CsexraAttribute.TIME_FIRST_LINE_IMAGE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testTimeImageDurationMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.TIME_IMAGE_DURATION_SHORT_NAME))
        .thenReturn("-9999.999999");

    Serializable actual =
        CsexraAttribute.TIME_IMAGE_DURATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(-9999.999999f));
  }

  @Test
  public void testTimeImageDurationMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.TIME_IMAGE_DURATION_SHORT_NAME))
        .thenReturn("86400.000000");

    Serializable actual =
        CsexraAttribute.TIME_IMAGE_DURATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(86400.000000f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTimeImageDurationNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.TIME_IMAGE_DURATION_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual =
        CsexraAttribute.TIME_IMAGE_DURATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testMaxGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.MAX_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.MAX_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(000.0f));
  }

  @Test
  public void testMaxGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.MAX_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual = CsexraAttribute.MAX_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(999.9f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMaxGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.MAX_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.MAX_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testAlongScanGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ALONG_SCAN_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.ALONG_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testAlongScanGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ALONG_SCAN_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual = CsexraAttribute.ALONG_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("999.9"));
  }

  @Test
  public void testAlongScanGsdNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ALONG_SCAN_GSD_SHORT_NAME)).thenReturn("N/A");

    Serializable actual = CsexraAttribute.ALONG_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAlongScanGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ALONG_SCAN_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.ALONG_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testCrossScanGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CROSS_SCAN_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.CROSS_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testCrossScanGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CROSS_SCAN_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual = CsexraAttribute.CROSS_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("999.9"));
  }

  @Test
  public void testCrossScanGsdNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CROSS_SCAN_GSD_SHORT_NAME)).thenReturn("N/A");

    Serializable actual = CsexraAttribute.CROSS_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCrossScanGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CROSS_SCAN_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.CROSS_SCAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testGeoMeanGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.GEO_MEAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testGeoMeanGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual = CsexraAttribute.GEO_MEAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("999.9"));
  }

  @Test
  public void testGeoMeanGsdNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_GSD_SHORT_NAME)).thenReturn("N/A");

    Serializable actual = CsexraAttribute.GEO_MEAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGeoMeanGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.GEO_MEAN_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testASVertGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.A_S_VERT_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.A_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testASVertGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.A_S_VERT_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual = CsexraAttribute.A_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("999.9"));
  }

  @Test
  public void testASVertGsdNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.A_S_VERT_GSD_SHORT_NAME)).thenReturn("N/A");

    Serializable actual = CsexraAttribute.A_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testASVertGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.A_S_VERT_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.A_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testCSVertGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.C_S_VERT_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.C_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testCSVertGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.C_S_VERT_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual = CsexraAttribute.C_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("999.9"));
  }

  @Test
  public void testCSVertGsdNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.C_S_VERT_GSD_SHORT_NAME)).thenReturn("N/A");

    Serializable actual = CsexraAttribute.C_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCSVertGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.C_S_VERT_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.C_S_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testGeoMeanVertGsdMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_VERT_GSD_SHORT_NAME)).thenReturn("000.0");

    Serializable actual =
        CsexraAttribute.GEO_MEAN_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testGeoMeanVertGsdMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_VERT_GSD_SHORT_NAME)).thenReturn("999.9");

    Serializable actual =
        CsexraAttribute.GEO_MEAN_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("999.9"));
  }

  @Test
  public void testGeoMeanVertGsdNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_VERT_GSD_SHORT_NAME)).thenReturn("N/A");

    Serializable actual =
        CsexraAttribute.GEO_MEAN_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGeoMeanVertGsdNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GEO_MEAN_VERT_GSD_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual =
        CsexraAttribute.GEO_MEAN_VERT_GSD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testGsdBetaAngleMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GSD_BETA_ANGLE_SHORT_NAME)).thenReturn("000.0");

    Serializable actual = CsexraAttribute.GSD_BETA_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("000.0"));
  }

  @Test
  public void testGsdBetaAngleMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GSD_BETA_ANGLE_SHORT_NAME)).thenReturn("180.0");

    Serializable actual = CsexraAttribute.GSD_BETA_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("180.0"));
  }

  @Test
  public void testGsdBetaAngleNa() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GSD_BETA_ANGLE_SHORT_NAME)).thenReturn("N/A");

    Serializable actual = CsexraAttribute.GSD_BETA_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is("N/A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGsdBetaAngleNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.GSD_BETA_ANGLE_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.GSD_BETA_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testDynamicRangeMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.DYNAMIC_RANGE_SHORT_NAME)).thenReturn("00000");

    Serializable actual = CsexraAttribute.DYNAMIC_RANGE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0));
  }

  @Test
  public void testDynamicRangeMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.DYNAMIC_RANGE_SHORT_NAME)).thenReturn("02047");

    Serializable actual = CsexraAttribute.DYNAMIC_RANGE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(2047));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDynamicRangeNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.DYNAMIC_RANGE_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.DYNAMIC_RANGE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testNumLinesMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.NUM_LINES_SHORT_NAME)).thenReturn("0000101");

    Serializable actual = CsexraAttribute.NUM_LINES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(101));
  }

  @Test
  public void testNumLinesMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.NUM_LINES_SHORT_NAME)).thenReturn("9999999");

    Serializable actual = CsexraAttribute.NUM_LINES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(9999999));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNumLinesNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.NUM_LINES_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.NUM_LINES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testNumSamplesMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.NUM_SAMPLES_SHORT_NAME)).thenReturn("0000101");

    Serializable actual = CsexraAttribute.NUM_SAMPLES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(101));
  }

  @Test
  public void testNumSamplesMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.NUM_SAMPLES_SHORT_NAME)).thenReturn("9999999");

    Serializable actual = CsexraAttribute.NUM_SAMPLES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(9999999));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNumSamplesNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.NUM_SAMPLES_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.NUM_SAMPLES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testAngleToNorthMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ANGLE_TO_NORTH_SHORT_NAME)).thenReturn("000.000");

    Serializable actual = CsexraAttribute.ANGLE_TO_NORTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0.0f));
  }

  @Test
  public void testAngleToNorthMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ANGLE_TO_NORTH_SHORT_NAME)).thenReturn("360.000");

    Serializable actual = CsexraAttribute.ANGLE_TO_NORTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(360.0f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAngleToNorthNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.ANGLE_TO_NORTH_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.ANGLE_TO_NORTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testObliquityAngleMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.OBLIQUITY_ANGLE_SHORT_NAME)).thenReturn("00.000");

    Serializable actual =
        CsexraAttribute.OBLIQUITY_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0.0f));
  }

  @Test
  public void testObliquityAngleMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.OBLIQUITY_ANGLE_SHORT_NAME)).thenReturn("90.000");

    Serializable actual =
        CsexraAttribute.OBLIQUITY_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(90.0f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testObliquityAngleNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.OBLIQUITY_ANGLE_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual =
        CsexraAttribute.OBLIQUITY_ANGLE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testAzOfObliquityMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.AZ_OF_OBLIQUITY_SHORT_NAME)).thenReturn("000.000");

    Serializable actual =
        CsexraAttribute.AZ_OF_OBLIQUITY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0.0f));
  }

  @Test
  public void testAzOfObliquityMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.AZ_OF_OBLIQUITY_SHORT_NAME)).thenReturn("360.000");

    Serializable actual =
        CsexraAttribute.AZ_OF_OBLIQUITY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(360.0f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAzOfObliquityNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.AZ_OF_OBLIQUITY_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual =
        CsexraAttribute.AZ_OF_OBLIQUITY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSunAzMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SUN_AZIMUTH_SHORT_NAME)).thenReturn("000.000");

    Serializable actual = CsexraAttribute.SUN_AZIMUTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0.0f));
  }

  @Test
  public void testSunAzMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SUN_AZIMUTH_SHORT_NAME)).thenReturn("360.000");

    Serializable actual = CsexraAttribute.SUN_AZIMUTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(360.0f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSunAzNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SUN_AZIMUTH_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.SUN_AZIMUTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSunElevationMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SUN_ELEVATION_SHORT_NAME)).thenReturn("-90.000");

    Serializable actual = CsexraAttribute.SUN_ELEVATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(-90.0f));
  }

  @Test
  public void testSunElevationMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SUN_ELEVATION_SHORT_NAME)).thenReturn("90.000");

    Serializable actual = CsexraAttribute.SUN_ELEVATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(90.0f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSunElevationNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SUN_ELEVATION_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.SUN_ELEVATION_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testCircularErrorMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CIRCL_ERR_SHORT_NAME)).thenReturn("000");

    Serializable actual = CsexraAttribute.CIRCL_ERR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0));
  }

  @Test
  public void testCircularErrorMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CIRCL_ERR_SHORT_NAME)).thenReturn("999");

    Serializable actual = CsexraAttribute.CIRCL_ERR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(999));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCircularErrorNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.CIRCL_ERR_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.CIRCL_ERR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testLinearErrorMin() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.LINEAR_ERR_SHORT_NAME)).thenReturn("000");

    Serializable actual = CsexraAttribute.LINEAR_ERR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0));
  }

  @Test
  public void testLinearErrorMax() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.LINEAR_ERR_SHORT_NAME)).thenReturn("999");

    Serializable actual = CsexraAttribute.LINEAR_ERR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(999));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLinearErrorNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.LINEAR_ERR_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.LINEAR_ERR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testSnowDepthCategory0() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("0");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_CAT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(0));
  }

  @Test
  public void testSnowDepthCategory1() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("1");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_CAT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(1));
  }

  @Test
  public void testSnowDepthCategory2() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("2");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_CAT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(2));
  }

  @Test
  public void testSnowDepthCategory3() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("3");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_CAT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(3));
  }

  @Test
  public void testSnowDepthCategoryOther() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME)).thenReturn("9");

    Serializable actual = CsexraAttribute.SNOW_DEPTH_CAT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(9));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSnowDepthCategoryNotSet() throws NitfFormatException {
    when(tre.getFieldValue(CsexraAttribute.SNOW_DEPTH_CAT_SHORT_NAME))
        .thenThrow(NitfFormatException.class);

    Serializable actual = CsexraAttribute.SNOW_DEPTH_CAT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(actual, is(nullValue()));
  }
}
