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
package org.codice.alliance.transformer.nitf.complex;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class CmetaaAttributeTest {

  private static final String RELATED_TRES = "02";

  private static final String ADDITIONAL_TRES =
      "AIMIDB AIPBCA                                                                                                           ";

  private static final String RD_PRC_NO = "Proc-X-0.901";

  private static final String IF_PROCESS = "  PF";

  private static final String RD_CEN_FREQ = "   X";

  private static final String RD_MODE = "  1FR";

  private static final String RD_PATCH_NO = "1234";

  private static final String CMPLX_DOMAIN = "   IQ";

  private static final String CMPLX_MAG_REMAP_TYPE = "LINM";

  private static final String CMPLX_LIN_SCALE = "1.00000";

  private static final String CMPLX_AVG_POWER = "2.00000";

  private static final String CMPLX_LINLOG_TP = "12345";

  private static final String CMPLX_PHASE_QUANT_FLAG = "USQ";

  private static final String CMPLX_PHASE_QUANT_BIT_DEPTH = "16";

  private static final String CMPLX_SIZE_1 = "32";

  private static final String CMPLX_IC_1 = "NC";

  private static final String CMPLX_SIZE_2 = "16";

  private static final String CMPLX_IC_2 = "C3";

  private static final String CMPLX_IC_BPP = "32.00";

  private static final String CMPLX_WEIGHT = "UWT";

  private static final String CMPLX_AZ_SLL = "45";

  private static final String CMPLX_RNG_SLL = "54";

  private static final String CMPLX_AZ_TAY_NBAR = "12";

  private static final String CMPLX_RNG_TAY_NBAR = "23";

  private static final String CMPLX_WEIGHT_NORM = "AVG";

  private static final String CMPLX_SIGNAL_PLANE = "S";

  private static final String IF_DC_SF_ROW = "123456";

  private static final String IF_DC_SF_COL = "234567";

  private static final String IF_PATCH_1_ROW = "012345";

  private static final String IF_PATCH_1_COL = "543210";

  private static final String IF_PATCH_2_ROW = "112345";

  private static final String IF_PATCH_2_COL = "543211";

  private static final String IF_PATCH_3_ROW = "212345";

  private static final String IF_PATCH_3_COL = "543212";

  private static final String IF_PATCH_4_ROW = "312345";

  private static final String IF_PATCH_4_COL = "543213";

  private static final String IF_DC_IS_ROW = "12345678";

  private static final String IF_DC_IS_COL = "87654321";

  private static final String IF_IMG_ROW_DC = "45678901";

  private static final String IF_IMG_COL_DC = "10987654";

  private static final String IF_TILE_1_ROW = "101234";

  private static final String IF_TILE_1_COL = "143210";

  private static final String IF_TILE_2_ROW = "201234";

  private static final String IF_TILE_2_COL = "243210";

  private static final String IF_TILE_3_ROW = "301234";

  private static final String IF_TILE_3_COL = "343210";

  private static final String IF_TILE_4_ROW = "401234";

  private static final String IF_TILE_4_COL = "443210";

  private static final String IF_RD = "Y";

  private static final String IF_RGWLK = "N";

  private static final String IF_KEYSTN = "O";

  private static final String IF_LINSFT = "Y";

  private static final String IF_SUBPATCH = "N";

  private static final String IF_GEODIST = "N";

  private static final String IF_RGFO = "Y";

  private static final String IF_BEAM_COMP = "O";

  private static final String IF_RGRES = "1234.567";

  private static final String IF_AZRES = "7654.321";

  private static final String IF_RSS = "12.34567";

  private static final String IF_AZSS = "76.54321";

  private static final String IF_RSR = "23.45678";

  private static final String IF_AZSR = "87.65432";

  private static final String IF_RFFT_SAMP = "1234567";

  private static final String IF_AZFFT_SAMP = "7654321";

  private static final String IF_RFFT_TOT = "0000123";

  private static final String IF_AZFFT_TOT = "0000234";

  private static final String IF_SUBP_ROW = "000012";

  private static final String IF_SUBP_COL = "000034";

  private static final String IF_SUB_RG = "0123";

  private static final String IF_SUB_AZ = "0234";

  private static final String IF_RFFTS = "+";

  private static final String IF_AFFTS = "-";

  private static final String IF_RANGE_DATA = "ROW_INC";

  private static final String IF_INCPH = "+";

  private static final String IF_SR_NAME1 = "S-SVA   ";

  private static final String IF_SR_AMOUNT1 = "01.23456";

  private static final String IF_SR_NAME2 = "HDI     ";

  private static final String IF_SR_AMOUNT2 = "02.34567";

  private static final String IF_SR_NAME3 = "CLEAN   ";

  private static final String IF_SR_AMOUNT = "03.45678";

  private static final String AF_TYPE1 = "PGA  ";

  private static final String AF_TYPE2 = "HOAF ";

  private static final String AF_TYPE3 = "RMDA ";

  private static final String POL_TR = "H";

  private static final String POL_RE = "V";

  private static final String POL_REFERENCE = "ANT33.44556,-44.55667;34.45566,-45.56677";

  private static final String POL = "P";

  private static final String POL_REG = "Y";

  private static final String POL_ISO_1 = "01.23";

  private static final String POL_BAL = "C";

  private static final String POL_BAL_MAG = "0.123456";

  private static final String POL_BAL_PHS = "1.234567";

  private static final String POL_HCOMP = "A";

  private static final String POL_HCOMP_BASIS = "POLYNOMIAL";

  private static final String POL_HCOMP_COEF_1 = "123456789";

  private static final String POL_HCOMP_COEF_2 = "006543210";

  private static final String POL_HCOMP_COEF_3 = "987654321";

  private static final String POL_AFCOMP = "D";

  private static final String POL_SPARE_A = "               ";

  private static final String POL_SPARE_N = "000000000";

  private static final String T_UTC_YYYYMMMDD = "1987JUN12";

  private static final String T_HHMMSSUTC = "123456";

  private static final String T_HHMMSSLOCAL = "163456";

  private static final String CG_SRAC = "12345678.90";

  private static final String CG_SLANT_CONFIDENCE = "01234.56";

  private static final String CG_CROSS = "09876543.21";

  private static final String CG_CROSS_CONFIDENCE = "9876.54";

  private static final String CG_CAAC = "+120.0034";

  private static final String CG_CONE_CONFIDENCE = "0.5678";

  private static final String CG_GPSAC = "-45.6789";

  private static final String CG_GPSAC_CONFIDENCE = "0.8765";

  private static final String CG_SQUINT = "+54.9087";

  private static final String CG_GAAC = "32.7593";

  private static final String CG_GAAC_CONFIDENCE = "0.7623";

  private static final String CG_INCIDENT = "66.3542";

  private static final String CG_SLOPE = "-23.995";

  private static final String CG_TILT = "+12.8845";

  private static final String CG_LD = "R";

  private static final String CG_NORTH = "275.4432";

  private static final String CG_NORTH_CONFIDENCE = "1.0034";

  private static final String CG_EAST = "056.9500";

  private static final String CG_RLOS = "043.3405";

  private static final String CG_LOS_CONFIDENCE = "0.0035";

  private static final String CG_LAYOVER = "180.0000";

  private static final String CG_SHADOW = "270.0000";

  private static final String CG_OPM = "888.999";

  private static final String CG_MODEL = "WGS84";

  private static final String CG_AMPT_X = "+0012.5678905";

  private static final String CG_AMPT_Y = "-0114.8765659";

  private static final String CG_AMPT_Z = "+55544433.222";

  private static final String CG_AP_CONF_XY = "003.55";

  private static final String CG_AP_CONF_Z = "555.55";

  private static final String CG_APCEN_X = "+0080.5000000";

  private static final String CG_APCEN_Y = "-0120.8500000";

  private static final String CG_APCEN_Z = "+45000000.000";

  private static final String CG_APER_CONF_XY = "200.00";

  private static final String CG_APER_CONF_Z = "400.00";

  private static final String CG_FPNUV_X = "+0.230000";

  private static final String CG_FPNUV_Y = "-0.650000";

  private static final String CG_FPNUV_Z = "+0.380000";

  private static final String CG_IDPNUVX = "+0.750000";

  private static final String CG_IDPNUVY = "+0.840000";

  private static final String CG_IDPNUVZ = "-0.200000";

  private static final String CG_SCECN_X = "-0076.8500000";

  private static final String CG_SCECN_Y = "+0140.6500000";

  private static final String CG_SCECN_Z = "-11122345.900";

  private static final String CG_SC_CONF_XY = "250.55";

  private static final String CG_SC_CONF_Z = "650.33";

  private static final String CG_SWWD = "04500.00";

  private static final String CG_SNVEL_X = "+02500.500";

  private static final String CG_SNVEL_Y = "-00350.000";

  private static final String CG_SNVEL_Z = "+00023.950";

  private static final String CG_SNACC_X = "+05.975000";

  private static final String CG_SNACC_Y = "-00.500000";

  private static final String CG_SNACC_Z = "+15.000000";

  private static final String CG_SNATT_ROLL = "+100.000";

  private static final String CG_SNATT_PITCH = "-005.555";

  private static final String CG_SNATT_YAW = "+002.500";

  private static final String CG_GTP_X = "+0.500000";

  private static final String CG_GTP_Y = "+0.325000";

  private static final String CG_GTP_Z = "-0.875000";

  private static final String CG_MAP_TYPE = "GEOD ";

  private static final String CG_PATCH_LATCEN = "-44.50000000";

  private static final String CG_PATCH_LNGCEN = "+022.5000000";

  private static final String CG_PATCH_LTCORUL = "-44.3000000";

  private static final String CG_PATCH_LGCORUL = "+022.300000";

  private static final String CG_PATCH_LTCORUR = "-44.3000000";

  private static final String CG_PATCH_LGCORUR = "+022.700000";

  private static final String CG_PATCH_LTCORLR = "-44.7000000";

  private static final String CG_PATCH_LGCORLR = "+022.700000";

  private static final String CG_PATCH_LTCORLL = "-44.7000000";

  private static final String CG_PATCH_LNGCOLL = "+022.500000";

  private static final String CG_PATCH_LAT_CONFIDENCE = "5.0000000";

  private static final String CG_PATCH_LONG_CONFIDENCE = "4.5000000";

  private static final String CG_MGRS_CENT = "zzBJKeeeeeeeeennnnnn001";

  private static final String CG_MGRSCORUL = "zzBJKeeeeeeeeennnnnn002";

  private static final String CG_MGRSCORUR = "zzBJKeeeeeeeeennnnnn003";

  private static final String CG_MGRSCORLR = "zzBJKeeeeeeeeennnnnn004";

  private static final String CG_MGRCORLL = "zzBJKeeeeeeeeennnnnn005";

  private static final String CG_MGRS_CONFIDENCE = "5000.00";

  private static final String CA_CALPA = "125.500";

  private static final String WF_SRTFR = "475000000000.0";

  private static final String WF_ENDFR = "587500000000.0";

  private static final String WF_CHRPRT = "+45.500000";

  private static final String WF_WIDTH = "0.5750000";

  private static final String WF_CENFRQ = "50000000000.0";

  private static final String WF_BW = "00095000000.0";

  private static final String WF_PRF = "55595.0";

  private static final String WF_PRI = "0.1910000";

  private static final String WF_CDP = "019.200";

  private static final String WF_NUMBER_OF_PULSES = "000000009";

  private static final String VPH_COND = "N";

  private Tre tre;

  @Before
  public void setUp() {
    tre = mock(Tre.class);
  }

  @Test
  public void testCmetaaAttribute() throws NitfFormatException {
    CmetaaAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getShortName(), notNullValue()));
    CmetaaAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getLongName(), notNullValue()));
  }

  @Test
  public void testRelatedTres() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.RELATED_TRES_ATTRIBUTE.getShortName()))
        .thenReturn(RELATED_TRES);

    Integer relatedTres =
        (Integer) CmetaaAttribute.RELATED_TRES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(relatedTres, is(Integer.parseInt(RELATED_TRES, 10)));
  }

  @Test
  public void testAdditionalTres() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.ADDITIONAL_TRES_ATTRIBUTE.getShortName()))
        .thenReturn(ADDITIONAL_TRES);

    String additionalTres =
        (String) CmetaaAttribute.ADDITIONAL_TRES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(additionalTres, is(ADDITIONAL_TRES.trim()));
  }

  @Test
  public void testRdPrcNo() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.RD_PRC_NO_ATTRIBUTE.getShortName()))
        .thenReturn(RD_PRC_NO);

    String rdPrcNo = (String) CmetaaAttribute.RD_PRC_NO_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(rdPrcNo, is(RD_PRC_NO.trim()));
  }

  @Test
  public void testIfProcess() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PROCESS_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PROCESS);

    String rdPrcNo = (String) CmetaaAttribute.IF_PROCESS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(rdPrcNo, is(IF_PROCESS.trim()));
  }

  @Test
  public void testRdCenFreq() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.RD_CEN_FREQ_ATTRIBUTE.getShortName()))
        .thenReturn(RD_CEN_FREQ);

    String rdPrcNo =
        (String) CmetaaAttribute.RD_CEN_FREQ_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(rdPrcNo, is(RD_CEN_FREQ.trim()));
  }

  @Test
  public void testRdMode() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.RD_MODE_ATTRIBUTE.getShortName())).thenReturn(RD_MODE);

    String rdPrcNo = (String) CmetaaAttribute.RD_MODE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(rdPrcNo, is(RD_MODE.trim()));
  }

  @Test
  public void testRdPatchNo() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.RD_PATCH_NO_ATTRIBUTE.getShortName()))
        .thenReturn(RD_PATCH_NO);

    Integer rdPatchNo =
        (Integer) CmetaaAttribute.RD_PATCH_NO_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(rdPatchNo, is(Integer.parseInt(RD_PATCH_NO, 10)));
  }

  @Test
  public void testCmplxDomain() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_DOMAIN_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_DOMAIN);

    String cmplxDomain =
        (String) CmetaaAttribute.CMPLX_DOMAIN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxDomain, is(CMPLX_DOMAIN.trim()));
  }

  @Test
  public void testCmplxMagRemapType() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_MAG_REMAP_TYPE_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_MAG_REMAP_TYPE);

    String cmplxMagRemapType =
        (String) CmetaaAttribute.CMPLX_MAG_REMAP_TYPE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxMagRemapType, is(CMPLX_MAG_REMAP_TYPE.trim()));
  }

  @Test
  public void testCmplxLinScale() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_LIN_SCALE_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_LIN_SCALE);

    Float cmplxLinScale =
        (Float) CmetaaAttribute.CMPLX_LIN_SCALE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxLinScale, is(Float.parseFloat(CMPLX_LIN_SCALE)));
  }

  @Test
  public void testCmplxAvgPower() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_AVG_POWER_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_AVG_POWER);

    Float cmplxAvgPower =
        (Float) CmetaaAttribute.CMPLX_AVG_POWER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxAvgPower, is(Float.parseFloat(CMPLX_AVG_POWER)));
  }

  @Test
  public void testCmplxLinLogTp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_LINLOG_TP_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_LINLOG_TP);

    Integer cmplxLinLogTp =
        (Integer) CmetaaAttribute.CMPLX_LINLOG_TP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxLinLogTp, is(Integer.parseInt(CMPLX_LINLOG_TP, 10)));
  }

  @Test
  public void testCmplxPhaseQuantFlag() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_PHASE_QUANT_FLAG_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_PHASE_QUANT_FLAG);

    String cmplxPhaseQuantFlag =
        (String) CmetaaAttribute.CMPLX_PHASE_QUANT_FLAG_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxPhaseQuantFlag, is(CMPLX_PHASE_QUANT_FLAG.trim()));
  }

  @Test
  public void testCmplxPhaseQuantBitDepth() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_PHASE_QUANT_BIT_DEPTH_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_PHASE_QUANT_BIT_DEPTH);

    Integer cmplxPhaseQuantBitDepth =
        (Integer)
            CmetaaAttribute.CMPLX_PHASE_QUANT_BIT_DEPTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxPhaseQuantBitDepth, is(Integer.parseInt(CMPLX_PHASE_QUANT_BIT_DEPTH, 10)));
  }

  @Test
  public void testCmplxSize1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_SIZE_1_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_SIZE_1);

    Integer cmplxSize1 =
        (Integer) CmetaaAttribute.CMPLX_SIZE_1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxSize1, is(Integer.parseInt(CMPLX_SIZE_1, 10)));
  }

  @Test
  public void testCmplxIc1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_IC_1_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_IC_1);

    String cmplxIc1 =
        (String) CmetaaAttribute.CMPLX_IC_1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxIc1, is(CMPLX_IC_1.trim()));
  }

  @Test
  public void testCmplxSize2() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_SIZE_2_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_SIZE_2);

    Integer cmplxSize2 =
        (Integer) CmetaaAttribute.CMPLX_SIZE_2_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxSize2, is(Integer.parseInt(CMPLX_SIZE_2, 10)));
  }

  @Test
  public void testCmplxIc2() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_IC_2_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_IC_2);

    String cmplxIc2 =
        (String) CmetaaAttribute.CMPLX_IC_2_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxIc2, is(CMPLX_IC_2.trim()));
  }

  @Test
  public void testCmplxIcBpp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_IC_BPP_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_IC_BPP);

    Float cmplxIcBpp =
        (Float) CmetaaAttribute.CMPLX_IC_BPP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxIcBpp, is(Float.parseFloat(CMPLX_IC_BPP)));
  }

  @Test
  public void testCmplxWeight() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_WEIGHT_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_WEIGHT);

    String cmplxWeight =
        (String) CmetaaAttribute.CMPLX_WEIGHT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxWeight, is(CMPLX_WEIGHT.trim()));
  }

  @Test
  public void testCmplxAzSll() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_AZ_SLL_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_AZ_SLL);

    Integer cmplxAzSll =
        (Integer) CmetaaAttribute.CMPLX_AZ_SLL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxAzSll, is(Integer.parseInt(CMPLX_AZ_SLL, 10)));
  }

  @Test
  public void testCmplxRngSll() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_RNG_SLL_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_RNG_SLL);

    Integer cmplxRngSll =
        (Integer) CmetaaAttribute.CMPLX_RNG_SLL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxRngSll, is(Integer.parseInt(CMPLX_RNG_SLL, 10)));
  }

  @Test
  public void testCmplxAzTayNbar() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_AZ_TAY_NBAR_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_AZ_TAY_NBAR);

    Integer cmplxAzTayNbar =
        (Integer) CmetaaAttribute.CMPLX_AZ_TAY_NBAR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxAzTayNbar, is(Integer.parseInt(CMPLX_AZ_TAY_NBAR, 10)));
  }

  @Test
  public void testCmplxRngTayNbar() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_RNG_TAY_NBAR_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_RNG_TAY_NBAR);

    Integer cmplxRngTayNbar =
        (Integer) CmetaaAttribute.CMPLX_RNG_TAY_NBAR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxRngTayNbar, is(Integer.parseInt(CMPLX_RNG_TAY_NBAR, 10)));
  }

  @Test
  public void testCmplxWeightNorm() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_WEIGHT_NORM_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_WEIGHT_NORM);

    String cmplxWeightNorm =
        (String) CmetaaAttribute.CMPLX_WEIGHT_NORM_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxWeightNorm, is(CMPLX_WEIGHT_NORM.trim()));
  }

  @Test
  public void testCmplxSignalPlane() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CMPLX_SIGNAL_PLANE_ATTRIBUTE.getShortName()))
        .thenReturn(CMPLX_SIGNAL_PLANE);

    String cmplxSignalPlane =
        (String) CmetaaAttribute.CMPLX_SIGNAL_PLANE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cmplxSignalPlane, is(CMPLX_SIGNAL_PLANE.trim()));
  }

  @Test
  public void testIfDcSfRow() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_DC_SF_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_DC_SF_ROW);

    Integer ifDcSfRow =
        (Integer) CmetaaAttribute.IF_DC_SF_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifDcSfRow, is(Integer.parseInt(IF_DC_SF_ROW, 10)));
  }

  @Test
  public void testIfDcSfCol() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_DC_SF_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_DC_SF_COL);

    Integer ifDcSfCol =
        (Integer) CmetaaAttribute.IF_DC_SF_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifDcSfCol, is(Integer.parseInt(IF_DC_SF_COL, 10)));
  }

  @Test
  public void testIfPatch1Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_1_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_1_ROW);

    Integer ifPatch1Row =
        (Integer) CmetaaAttribute.IF_PATCH_1_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch1Row, is(Integer.parseInt(IF_PATCH_1_ROW, 10)));
  }

  @Test
  public void testIfPatch1Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_1_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_1_COL);

    Integer ifPatch1Col =
        (Integer) CmetaaAttribute.IF_PATCH_1_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch1Col, is(Integer.parseInt(IF_PATCH_1_COL, 10)));
  }

  @Test
  public void testIfPatch2Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_2_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_2_ROW);

    Integer ifPatch2Row =
        (Integer) CmetaaAttribute.IF_PATCH_2_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch2Row, is(Integer.parseInt(IF_PATCH_2_ROW, 10)));
  }

  @Test
  public void testIfPatch2Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_2_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_2_COL);

    Integer ifPatch2Col =
        (Integer) CmetaaAttribute.IF_PATCH_2_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch2Col, is(Integer.parseInt(IF_PATCH_2_COL, 10)));
  }

  @Test
  public void testIfPatch3Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_3_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_3_ROW);

    Integer ifPatch3Row =
        (Integer) CmetaaAttribute.IF_PATCH_3_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch3Row, is(Integer.parseInt(IF_PATCH_3_ROW, 10)));
  }

  @Test
  public void testIfPatch3Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_3_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_3_COL);

    Integer ifPatch3Col =
        (Integer) CmetaaAttribute.IF_PATCH_3_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch3Col, is(Integer.parseInt(IF_PATCH_3_COL, 10)));
  }

  @Test
  public void testIfPatch4Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_4_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_4_ROW);

    Integer ifPatch4Row =
        (Integer) CmetaaAttribute.IF_PATCH_4_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch4Row, is(Integer.parseInt(IF_PATCH_4_ROW, 10)));
  }

  @Test
  public void testIfPatch4Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_PATCH_4_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_PATCH_4_COL);

    Integer ifPatch4Col =
        (Integer) CmetaaAttribute.IF_PATCH_4_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifPatch4Col, is(Integer.parseInt(IF_PATCH_4_COL, 10)));
  }

  @Test
  public void testIfDcIsRow() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_DC_IS_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_DC_IS_ROW);

    Integer ifDcIsRow =
        (Integer) CmetaaAttribute.IF_DC_IS_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifDcIsRow, is(Integer.parseInt(IF_DC_IS_ROW, 10)));
  }

  @Test
  public void testIfDcIsCol() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_DC_IS_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_DC_IS_COL);

    Integer ifDcIsCol =
        (Integer) CmetaaAttribute.IF_DC_IS_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifDcIsCol, is(Integer.parseInt(IF_DC_IS_COL, 10)));
  }

  @Test
  public void testIfImgRowDc() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_IMG_ROW_DC_ATTRIBUTE.getShortName()))
        .thenReturn(IF_IMG_ROW_DC);

    Integer ifImgRowDc =
        (Integer) CmetaaAttribute.IF_IMG_ROW_DC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifImgRowDc, is(Integer.parseInt(IF_IMG_ROW_DC, 10)));
  }

  @Test
  public void testIfImgColDc() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_IMG_COL_DC_ATTRIBUTE.getShortName()))
        .thenReturn(IF_IMG_COL_DC);

    Integer ifImgColDc =
        (Integer) CmetaaAttribute.IF_IMG_COL_DC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifImgColDc, is(Integer.parseInt(IF_IMG_COL_DC, 10)));
  }

  @Test
  public void testIfTile1Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_1_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_1_ROW);

    Integer ifTile1Row =
        (Integer) CmetaaAttribute.IF_TILE_1_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile1Row, is(Integer.parseInt(IF_TILE_1_ROW, 10)));
  }

  @Test
  public void testIfTile1Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_1_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_1_COL);

    Integer ifTile1Col =
        (Integer) CmetaaAttribute.IF_TILE_1_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile1Col, is(Integer.parseInt(IF_TILE_1_COL, 10)));
  }

  @Test
  public void testIfTile2Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_2_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_2_ROW);

    Integer ifTile2Row =
        (Integer) CmetaaAttribute.IF_TILE_2_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile2Row, is(Integer.parseInt(IF_TILE_2_ROW, 10)));
  }

  @Test
  public void testIfTile2Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_2_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_2_COL);

    Integer ifTile2Col =
        (Integer) CmetaaAttribute.IF_TILE_2_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile2Col, is(Integer.parseInt(IF_TILE_2_COL, 10)));
  }

  @Test
  public void testIfTile3Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_3_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_3_ROW);

    Integer ifTile3Row =
        (Integer) CmetaaAttribute.IF_TILE_3_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile3Row, is(Integer.parseInt(IF_TILE_3_ROW, 10)));
  }

  @Test
  public void testIfTile3Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_3_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_3_COL);

    Integer ifTile3Col =
        (Integer) CmetaaAttribute.IF_TILE_3_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile3Col, is(Integer.parseInt(IF_TILE_3_COL, 10)));
  }

  @Test
  public void testIfTile4Row() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_4_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_4_ROW);

    Integer ifTile4Row =
        (Integer) CmetaaAttribute.IF_TILE_4_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile4Row, is(Integer.parseInt(IF_TILE_4_ROW, 10)));
  }

  @Test
  public void testIfTile4Col() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_TILE_4_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_TILE_4_COL);

    Integer ifTile4Col =
        (Integer) CmetaaAttribute.IF_TILE_4_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifTile4Col, is(Integer.parseInt(IF_TILE_4_COL, 10)));
  }

  @Test
  public void testIfRd() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RD_ATTRIBUTE.getShortName())).thenReturn(IF_RD);

    String ifRd = (String) CmetaaAttribute.IF_RD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRd, is(IF_RD.trim()));
  }

  @Test
  public void testIfRgwlk() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RGWLK_ATTRIBUTE.getShortName())).thenReturn(IF_RGWLK);

    String ifRgwlk = (String) CmetaaAttribute.IF_RGWLK_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRgwlk, is(IF_RGWLK.trim()));
  }

  @Test
  public void testIfKeystn() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_KEYSTN_ATTRIBUTE.getShortName()))
        .thenReturn(IF_KEYSTN);

    String ifKeystn = (String) CmetaaAttribute.IF_KEYSTN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifKeystn, is(IF_KEYSTN.trim()));
  }

  @Test
  public void testIfLinsft() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_LINSFT_ATTRIBUTE.getShortName()))
        .thenReturn(IF_LINSFT);

    String ifLinsft = (String) CmetaaAttribute.IF_LINSFT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifLinsft, is(IF_LINSFT.trim()));
  }

  @Test
  public void testIfSubpatch() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SUBPATCH_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SUBPATCH);

    String ifSubpatch =
        (String) CmetaaAttribute.IF_SUBPATCH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSubpatch, is(IF_SUBPATCH.trim()));
  }

  @Test
  public void testIfGeodist() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_GEODIST_ATTRIBUTE.getShortName()))
        .thenReturn(IF_GEODIST);

    String ifGeodist =
        (String) CmetaaAttribute.IF_GEODIST_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifGeodist, is(IF_GEODIST.trim()));
  }

  @Test
  public void testIfRgfo() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RGFO_ATTRIBUTE.getShortName())).thenReturn(IF_RGFO);

    String ifRgfo = (String) CmetaaAttribute.IF_RGFO_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRgfo, is(IF_RGFO.trim()));
  }

  @Test
  public void testIfBeamComp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_BEAM_COMP_ATTRIBUTE.getShortName()))
        .thenReturn(IF_BEAM_COMP);

    String ifBeamComp =
        (String) CmetaaAttribute.IF_BEAM_COMP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifBeamComp, is(IF_BEAM_COMP.trim()));
  }

  @Test
  public void testIfRgres() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RGRES_ATTRIBUTE.getShortName())).thenReturn(IF_RGRES);

    Float ifRgres = (Float) CmetaaAttribute.IF_RGRES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRgres, is(Float.parseFloat(IF_RGRES)));
  }

  @Test
  public void testIfAzres() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_AZRES_ATTRIBUTE.getShortName())).thenReturn(IF_AZRES);

    Float ifAzres = (Float) CmetaaAttribute.IF_AZRES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifAzres, is(Float.parseFloat(IF_AZRES)));
  }

  @Test
  public void testIfRss() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RSS_ATTRIBUTE.getShortName())).thenReturn(IF_RSS);

    Float ifRss = (Float) CmetaaAttribute.IF_RSS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRss, is(Float.parseFloat(IF_RSS)));
  }

  @Test
  public void testIfAzss() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_AZSS_ATTRIBUTE.getShortName())).thenReturn(IF_AZSS);

    Float ifAzss = (Float) CmetaaAttribute.IF_AZSS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifAzss, is(Float.parseFloat(IF_AZSS)));
  }

  @Test
  public void testIfRsr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RSR_ATTRIBUTE.getShortName())).thenReturn(IF_RSR);

    Float ifRsr = (Float) CmetaaAttribute.IF_RSR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRsr, is(Float.parseFloat(IF_RSR)));
  }

  @Test
  public void testIfAzsr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_AZSR_ATTRIBUTE.getShortName())).thenReturn(IF_AZSR);

    Float ifAzsr = (Float) CmetaaAttribute.IF_AZSR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifAzsr, is(Float.parseFloat(IF_AZSR)));
  }

  @Test
  public void testIfRfftSamp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RFFT_SAMP_ATTRIBUTE.getShortName()))
        .thenReturn(IF_RFFT_SAMP);

    Integer ifRfftSamp =
        (Integer) CmetaaAttribute.IF_RFFT_SAMP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRfftSamp, is(Integer.parseInt(IF_RFFT_SAMP, 10)));
  }

  @Test
  public void testIfAzfftSamp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_AZFFT_SAMP_ATTRIBUTE.getShortName()))
        .thenReturn(IF_AZFFT_SAMP);

    Integer ifAzfftSamp =
        (Integer) CmetaaAttribute.IF_AZFFT_SAMP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifAzfftSamp, is(Integer.parseInt(IF_AZFFT_SAMP, 10)));
  }

  @Test
  public void testIfRfftTot() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RFFT_TOT_ATTRIBUTE.getShortName()))
        .thenReturn(IF_RFFT_TOT);

    Integer ifRfftTot =
        (Integer) CmetaaAttribute.IF_RFFT_TOT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRfftTot, is(Integer.parseInt(IF_RFFT_TOT, 10)));
  }

  @Test
  public void testIfAzfftTot() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_AZFFT_TOT_ATTRIBUTE.getShortName()))
        .thenReturn(IF_AZFFT_TOT);

    Integer ifAzfftTot =
        (Integer) CmetaaAttribute.IF_AZFFT_TOT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifAzfftTot, is(Integer.parseInt(IF_AZFFT_TOT, 10)));
  }

  @Test
  public void testIfSubpRow() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SUBP_ROW_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SUBP_ROW);

    Integer ifSubpRow =
        (Integer) CmetaaAttribute.IF_SUBP_ROW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSubpRow, is(Integer.parseInt(IF_SUBP_ROW, 10)));
  }

  @Test
  public void testIfSubpCol() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SUBP_COL_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SUBP_COL);

    Integer ifSubpCol =
        (Integer) CmetaaAttribute.IF_SUBP_COL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSubpCol, is(Integer.parseInt(IF_SUBP_COL, 10)));
  }

  @Test
  public void testIfSubRg() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SUB_RG_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SUB_RG);

    Integer ifSubRg =
        (Integer) CmetaaAttribute.IF_SUB_RG_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSubRg, is(Integer.parseInt(IF_SUB_RG, 10)));
  }

  @Test
  public void testIfSubAz() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SUB_AZ_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SUB_AZ);

    Integer ifSubAz =
        (Integer) CmetaaAttribute.IF_SUB_AZ_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSubAz, is(Integer.parseInt(IF_SUB_AZ, 10)));
  }

  @Test
  public void testIfRffts() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RFFTS_ATTRIBUTE.getShortName())).thenReturn(IF_RFFTS);

    String ifRffts = (String) CmetaaAttribute.IF_RFFTS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRffts, is(IF_RFFTS.trim()));
  }

  @Test
  public void testIfAffts() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_AFFTS_ATTRIBUTE.getShortName())).thenReturn(IF_AFFTS);

    String ifAffts = (String) CmetaaAttribute.IF_AFFTS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifAffts, is(IF_AFFTS.trim()));
  }

  @Test
  public void testIfRangeData() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_RANGE_DATA_ATTRIBUTE.getShortName()))
        .thenReturn(IF_RANGE_DATA);

    String ifRangeData =
        (String) CmetaaAttribute.IF_RANGE_DATA_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifRangeData, is(IF_RANGE_DATA.trim()));
  }

  @Test
  public void testIfIncph() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_INCPH_ATTRIBUTE.getShortName())).thenReturn(IF_INCPH);

    String ifIncph = (String) CmetaaAttribute.IF_INCPH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifIncph, is(IF_INCPH.trim()));
  }

  @Test
  public void testIfSrName1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SR_NAME1_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SR_NAME1);

    String ifSrName1 =
        (String) CmetaaAttribute.IF_SR_NAME1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSrName1, is(IF_SR_NAME1.trim()));
  }

  @Test
  public void testIfSrAmount1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SR_AMOUNT1_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SR_AMOUNT1);

    Float ifSrAmount1 =
        (Float) CmetaaAttribute.IF_SR_AMOUNT1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSrAmount1, is(Float.parseFloat(IF_SR_AMOUNT1)));
  }

  @Test
  public void testIfSrName2() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SR_NAME2_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SR_NAME2);

    String ifSrName2 =
        (String) CmetaaAttribute.IF_SR_NAME2_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSrName2, is(IF_SR_NAME2.trim()));
  }

  @Test
  public void testIfSrAmount2() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SR_AMOUNT2_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SR_AMOUNT2);

    Float ifSrAmount2 =
        (Float) CmetaaAttribute.IF_SR_AMOUNT2_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSrAmount2, is(Float.parseFloat(IF_SR_AMOUNT2)));
  }

  @Test
  public void testIfSrName3() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SR_NAME3_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SR_NAME3);

    String ifSrName3 =
        (String) CmetaaAttribute.IF_SR_NAME3_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSrName3, is(IF_SR_NAME3.trim()));
  }

  @Test
  public void testIfSrAmount() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.IF_SR_AMOUNT_ATTRIBUTE.getShortName()))
        .thenReturn(IF_SR_AMOUNT);

    Float ifSrAmount =
        (Float) CmetaaAttribute.IF_SR_AMOUNT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(ifSrAmount, is(Float.parseFloat(IF_SR_AMOUNT)));
  }

  @Test
  public void testAfType1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.AF_TYPE1_ATTRIBUTE.getShortName())).thenReturn(AF_TYPE1);

    String afType1 = (String) CmetaaAttribute.AF_TYPE1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(afType1, is(AF_TYPE1.trim()));
  }

  @Test
  public void testAfType2() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.AF_TYPE2_ATTRIBUTE.getShortName())).thenReturn(AF_TYPE2);

    String afType2 = (String) CmetaaAttribute.AF_TYPE2_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(afType2, is(AF_TYPE2.trim()));
  }

  @Test
  public void testAfType3() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.AF_TYPE3_ATTRIBUTE.getShortName())).thenReturn(AF_TYPE3);

    String afType3 = (String) CmetaaAttribute.AF_TYPE3_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(afType3, is(AF_TYPE3.trim()));
  }

  @Test
  public void testPolTr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_TR_ATTRIBUTE.getShortName())).thenReturn(POL_TR);

    String polTr = (String) CmetaaAttribute.POL_TR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polTr, is(POL_TR.trim()));
  }

  @Test
  public void testPolRe() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_RE_ATTRIBUTE.getShortName())).thenReturn(POL_RE);

    String polRe = (String) CmetaaAttribute.POL_RE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polRe, is(POL_RE.trim()));
  }

  @Test
  public void testPolReference() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_REFERENCE_ATTRIBUTE.getShortName()))
        .thenReturn(POL_REFERENCE);

    String polReference =
        (String) CmetaaAttribute.POL_REFERENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polReference, is(POL_REFERENCE.trim()));
  }

  @Test
  public void testPol() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_ATTRIBUTE.getShortName())).thenReturn(POL);

    String pol = (String) CmetaaAttribute.POL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(pol, is(POL.trim()));
  }

  @Test
  public void testPolReg() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_REG_ATTRIBUTE.getShortName())).thenReturn(POL_REG);

    String polReg = (String) CmetaaAttribute.POL_REG_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polReg, is(POL_REG.trim()));
  }

  @Test
  public void testPolIso1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_ISO_1_ATTRIBUTE.getShortName()))
        .thenReturn(POL_ISO_1);

    Float polIso1 = (Float) CmetaaAttribute.POL_ISO_1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polIso1, is(Float.parseFloat(POL_ISO_1)));
  }

  @Test
  public void testPolBal() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_BAL_ATTRIBUTE.getShortName())).thenReturn(POL_BAL);

    String polBal = (String) CmetaaAttribute.POL_BAL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polBal, is(POL_BAL.trim()));
  }

  @Test
  public void testPolBalMag() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_BAL_MAG_ATTRIBUTE.getShortName()))
        .thenReturn(POL_BAL_MAG);

    Float polBalMag =
        (Float) CmetaaAttribute.POL_BAL_MAG_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polBalMag, is(Float.parseFloat(POL_BAL_MAG)));
  }

  @Test
  public void testPolBalPhs() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_BAL_PHS_ATTRIBUTE.getShortName()))
        .thenReturn(POL_BAL_PHS);

    Float polBalPhs =
        (Float) CmetaaAttribute.POL_BAL_PHS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polBalPhs, is(Float.parseFloat(POL_BAL_PHS)));
  }

  @Test
  public void testPolHcomp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_HCOMP_ATTRIBUTE.getShortName()))
        .thenReturn(POL_HCOMP);

    String polHcomp = (String) CmetaaAttribute.POL_HCOMP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polHcomp, is(POL_HCOMP.trim()));
  }

  @Test
  public void testPolHcompBasis() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_HCOMP_BASIS_ATTRIBUTE.getShortName()))
        .thenReturn(POL_HCOMP_BASIS);

    String polHcompBasis =
        (String) CmetaaAttribute.POL_HCOMP_BASIS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polHcompBasis, is(POL_HCOMP_BASIS.trim()));
  }

  @Test
  public void testPolHcompCoef1() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_HCOMP_COEF_1_ATTRIBUTE.getShortName()))
        .thenReturn(POL_HCOMP_COEF_1);

    Float polHcompCoef1 =
        (Float) CmetaaAttribute.POL_HCOMP_COEF_1_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polHcompCoef1, is(Float.parseFloat(POL_HCOMP_COEF_1)));
  }

  @Test
  public void testPolHcompCoef2() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_HCOMP_COEF_2_ATTRIBUTE.getShortName()))
        .thenReturn(POL_HCOMP_COEF_2);

    Float polHcompCoef2 =
        (Float) CmetaaAttribute.POL_HCOMP_COEF_2_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polHcompCoef2, is(Float.parseFloat(POL_HCOMP_COEF_2)));
  }

  @Test
  public void testPolHcompCoef3() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_HCOMP_COEF_3_ATTRIBUTE.getShortName()))
        .thenReturn(POL_HCOMP_COEF_3);

    Float polHcompCoef3 =
        (Float) CmetaaAttribute.POL_HCOMP_COEF_3_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polHcompCoef3, is(Float.parseFloat(POL_HCOMP_COEF_3)));
  }

  @Test
  public void testPolAfcomp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_AFCOMP_ATTRIBUTE.getShortName()))
        .thenReturn(POL_AFCOMP);

    String polAfcomp =
        (String) CmetaaAttribute.POL_AFCOMP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polAfcomp, is(POL_AFCOMP.trim()));
  }

  @Test
  public void testPolSpareA() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_SPARE_A_ATTRIBUTE.getShortName()))
        .thenReturn(POL_SPARE_A);

    String polSpareA =
        (String) CmetaaAttribute.POL_SPARE_A_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polSpareA, is(POL_SPARE_A.trim()));
  }

  @Test
  public void testPolSpareN() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.POL_SPARE_N_ATTRIBUTE.getShortName()))
        .thenReturn(POL_SPARE_N);

    Integer polSpareN =
        (Integer) CmetaaAttribute.POL_SPARE_N_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(polSpareN, is(Integer.parseInt(POL_SPARE_N, 10)));
  }

  @Test
  public void testTUtcYyyyMmmDd() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.T_UTC_YYYYMMMDD_ATTRIBUTE.getShortName()))
        .thenReturn(T_UTC_YYYYMMMDD);

    String tUtcYyyyMmmDd =
        (String) CmetaaAttribute.T_UTC_YYYYMMMDD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(tUtcYyyyMmmDd, is(T_UTC_YYYYMMMDD.trim()));
  }

  @Test
  public void testTHhMmSsUtc() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.T_HHMMSSUTC_ATTRIBUTE.getShortName()))
        .thenReturn(T_HHMMSSUTC);

    String tHhMmSsUtc =
        (String) CmetaaAttribute.T_HHMMSSUTC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(tHhMmSsUtc, is(T_HHMMSSUTC.trim()));
  }

  @Test
  public void testTHhMmSsLocal() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.T_HHMMSSLOCAL_ATTRIBUTE.getShortName()))
        .thenReturn(T_HHMMSSLOCAL);

    String tHhMmSsLocal =
        (String) CmetaaAttribute.T_HHMMSSLOCAL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(tHhMmSsLocal, is(T_HHMMSSLOCAL.trim()));
  }

  @Test
  public void testCgSrac() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SRAC_ATTRIBUTE.getShortName())).thenReturn(CG_SRAC);

    Float cgSrac = (Float) CmetaaAttribute.CG_SRAC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSrac, is(Float.parseFloat(CG_SRAC)));
  }

  @Test
  public void testCgSlantConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SLANT_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SLANT_CONFIDENCE);

    Float cgSlantConfidence =
        (Float) CmetaaAttribute.CG_SLANT_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSlantConfidence, is(Float.parseFloat(CG_SLANT_CONFIDENCE)));
  }

  @Test
  public void testCgCross() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_CROSS_ATTRIBUTE.getShortName())).thenReturn(CG_CROSS);

    Float cgCross = (Float) CmetaaAttribute.CG_CROSS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgCross, is(Float.parseFloat(CG_CROSS)));
  }

  @Test
  public void testCgCrossConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_CROSS_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_CROSS_CONFIDENCE);

    Float cgCrossConfidence =
        (Float) CmetaaAttribute.CG_CROSS_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgCrossConfidence, is(Float.parseFloat(CG_CROSS_CONFIDENCE)));
  }

  @Test
  public void testCgCaac() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_CAAC_ATTRIBUTE.getShortName())).thenReturn(CG_CAAC);

    Float cgCaac = (Float) CmetaaAttribute.CG_CAAC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgCaac, is(Float.parseFloat(CG_CAAC)));
  }

  @Test
  public void testCgConeConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_CONE_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_CONE_CONFIDENCE);

    Float cgConeConfidence =
        (Float) CmetaaAttribute.CG_CONE_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgConeConfidence, is(Float.parseFloat(CG_CONE_CONFIDENCE)));
  }

  @Test
  public void testCgGpsac() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GPSAC_ATTRIBUTE.getShortName())).thenReturn(CG_GPSAC);

    Float cgGpsac = (Float) CmetaaAttribute.CG_GPSAC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGpsac, is(Float.parseFloat(CG_GPSAC)));
  }

  @Test
  public void testCgGpsacConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GPSAC_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_GPSAC_CONFIDENCE);

    Float cgGpsacConfidence =
        (Float) CmetaaAttribute.CG_GPSAC_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGpsacConfidence, is(Float.parseFloat(CG_GPSAC_CONFIDENCE)));
  }

  @Test
  public void testCgSquint() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SQUINT_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SQUINT);

    Float cgSquint = (Float) CmetaaAttribute.CG_SQUINT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSquint, is(Float.parseFloat(CG_SQUINT)));
  }

  @Test
  public void testCgGaac() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GAAC_ATTRIBUTE.getShortName())).thenReturn(CG_GAAC);

    Float cgGaac = (Float) CmetaaAttribute.CG_GAAC_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGaac, is(Float.parseFloat(CG_GAAC)));
  }

  @Test
  public void testCgGaacConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GAAC_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_GAAC_CONFIDENCE);

    Float cgGaacConfidence =
        (Float) CmetaaAttribute.CG_GAAC_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGaacConfidence, is(Float.parseFloat(CG_GAAC_CONFIDENCE)));
  }

  @Test
  public void testCgIncident() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_INCIDENT_ATTRIBUTE.getShortName()))
        .thenReturn(CG_INCIDENT);

    Float cgIncident =
        (Float) CmetaaAttribute.CG_INCIDENT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgIncident, is(Float.parseFloat(CG_INCIDENT)));
  }

  @Test
  public void testCgSlope() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SLOPE_ATTRIBUTE.getShortName())).thenReturn(CG_SLOPE);

    Float cgSlope = (Float) CmetaaAttribute.CG_SLOPE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSlope, is(Float.parseFloat(CG_SLOPE)));
  }

  @Test
  public void testCgTilt() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_TILT_ATTRIBUTE.getShortName())).thenReturn(CG_TILT);

    Float cgTilt = (Float) CmetaaAttribute.CG_TILT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgTilt, is(Float.parseFloat(CG_TILT)));
  }

  @Test
  public void testCgLd() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_LD_ATTRIBUTE.getShortName())).thenReturn(CG_LD);

    String cgLd = (String) CmetaaAttribute.CG_LD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgLd, is(CG_LD.trim()));
  }

  @Test
  public void testCgNorth() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_NORTH_ATTRIBUTE.getShortName())).thenReturn(CG_NORTH);

    Float cgNorth = (Float) CmetaaAttribute.CG_NORTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgNorth, is(Float.parseFloat(CG_NORTH)));
  }

  @Test
  public void testCgNorthConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_NORTH_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_NORTH_CONFIDENCE);

    Float cgNorthConfidence =
        (Float) CmetaaAttribute.CG_NORTH_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgNorthConfidence, is(Float.parseFloat(CG_NORTH_CONFIDENCE)));
  }

  @Test
  public void testCgEast() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_EAST_ATTRIBUTE.getShortName())).thenReturn(CG_EAST);

    Float cgEast = (Float) CmetaaAttribute.CG_EAST_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgEast, is(Float.parseFloat(CG_EAST)));
  }

  @Test
  public void testCgRlos() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_RLOS_ATTRIBUTE.getShortName())).thenReturn(CG_RLOS);

    Float cgRlos = (Float) CmetaaAttribute.CG_RLOS_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgRlos, is(Float.parseFloat(CG_RLOS)));
  }

  @Test
  public void testCgLosConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_LOS_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_LOS_CONFIDENCE);

    Float cgLosConfidence =
        (Float) CmetaaAttribute.CG_LOS_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgLosConfidence, is(Float.parseFloat(CG_LOS_CONFIDENCE)));
  }

  @Test
  public void testCgLayover() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_LAYOVER_ATTRIBUTE.getShortName()))
        .thenReturn(CG_LAYOVER);

    Float cgLayover = (Float) CmetaaAttribute.CG_LAYOVER_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgLayover, is(Float.parseFloat(CG_LAYOVER)));
  }

  @Test
  public void testCgShadow() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SHADOW_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SHADOW);

    Float cgShadow = (Float) CmetaaAttribute.CG_SHADOW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgShadow, is(Float.parseFloat(CG_SHADOW)));
  }

  @Test
  public void testCgOpm() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_OPM_ATTRIBUTE.getShortName())).thenReturn(CG_OPM);

    Float cgOpm = (Float) CmetaaAttribute.CG_OPM_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgOpm, is(Float.parseFloat(CG_OPM)));
  }

  @Test
  public void testCgModel() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MODEL_ATTRIBUTE.getShortName())).thenReturn(CG_MODEL);

    String cgModel = (String) CmetaaAttribute.CG_MODEL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgModel, is(CG_MODEL.trim()));
  }

  @Test
  public void testCgAmptX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_AMPT_X_ATTRIBUTE.getShortName()))
        .thenReturn(CG_AMPT_X);

    Float cgAmptX = (Float) CmetaaAttribute.CG_AMPT_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgAmptX, is(Float.parseFloat(CG_AMPT_X)));
  }

  @Test
  public void testCgAmptY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_AMPT_Y_ATTRIBUTE.getShortName()))
        .thenReturn(CG_AMPT_Y);

    Float cgAmptY = (Float) CmetaaAttribute.CG_AMPT_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgAmptY, is(Float.parseFloat(CG_AMPT_Y)));
  }

  @Test
  public void testCgAmptZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_AMPT_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_AMPT_Z);

    Float cgAmptZ = (Float) CmetaaAttribute.CG_AMPT_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgAmptZ, is(Float.parseFloat(CG_AMPT_Z)));
  }

  @Test
  public void testCgApConfXY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_AP_CONF_XY_ATTRIBUTE.getShortName()))
        .thenReturn(CG_AP_CONF_XY);

    Float cgApConfXY =
        (Float) CmetaaAttribute.CG_AP_CONF_XY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgApConfXY, is(Float.parseFloat(CG_AP_CONF_XY)));
  }

  @Test
  public void testCgApConfZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_AP_CONF_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_AP_CONF_Z);

    Float cgApConfZ =
        (Float) CmetaaAttribute.CG_AP_CONF_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgApConfZ, is(Float.parseFloat(CG_AP_CONF_Z)));
  }

  @Test
  public void testCgApcenX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_APCEN_X_ATTRIBUTE.getShortName()))
        .thenReturn(CG_APCEN_X);

    Float cgApcenX = (Float) CmetaaAttribute.CG_APCEN_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgApcenX, is(Float.parseFloat(CG_APCEN_X)));
  }

  @Test
  public void testCgApcenY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_APCEN_Y_ATTRIBUTE.getShortName()))
        .thenReturn(CG_APCEN_Y);

    Float cgApcenY = (Float) CmetaaAttribute.CG_APCEN_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgApcenY, is(Float.parseFloat(CG_APCEN_Y)));
  }

  @Test
  public void testCgApcenZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_APCEN_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_APCEN_Z);

    Float cgApcenZ = (Float) CmetaaAttribute.CG_APCEN_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgApcenZ, is(Float.parseFloat(CG_APCEN_Z)));
  }

  @Test
  public void testCgAperConfXY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_APER_CONF_XY_ATTRIBUTE.getShortName()))
        .thenReturn(CG_APER_CONF_XY);

    Float cgAperConfXY =
        (Float) CmetaaAttribute.CG_APER_CONF_XY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgAperConfXY, is(Float.parseFloat(CG_APER_CONF_XY)));
  }

  @Test
  public void testCgAperConfZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_APER_CONF_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_APER_CONF_Z);

    Float cgAperConfZ =
        (Float) CmetaaAttribute.CG_APER_CONF_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgAperConfZ, is(Float.parseFloat(CG_APER_CONF_Z)));
  }

  @Test
  public void testCgFpnuvX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_FPNUV_X_ATTRIBUTE.getShortName()))
        .thenReturn(CG_FPNUV_X);

    Float cgFpnuvX = (Float) CmetaaAttribute.CG_FPNUV_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgFpnuvX, is(Float.parseFloat(CG_FPNUV_X)));
  }

  @Test
  public void testCgFpnuvY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_FPNUV_Y_ATTRIBUTE.getShortName()))
        .thenReturn(CG_FPNUV_Y);

    Float cgFpnuvY = (Float) CmetaaAttribute.CG_FPNUV_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgFpnuvY, is(Float.parseFloat(CG_FPNUV_Y)));
  }

  @Test
  public void testCgFpnuvZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_FPNUV_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_FPNUV_Z);

    Float cgFpnuvZ = (Float) CmetaaAttribute.CG_FPNUV_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgFpnuvZ, is(Float.parseFloat(CG_FPNUV_Z)));
  }

  @Test
  public void testCgIdpnuvx() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_IDPNUVX_ATTRIBUTE.getShortName()))
        .thenReturn(CG_IDPNUVX);

    Float cgIdpnuvx = (Float) CmetaaAttribute.CG_IDPNUVX_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgIdpnuvx, is(Float.parseFloat(CG_IDPNUVX)));
  }

  @Test
  public void testCgIdpnuvy() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_IDPNUVY_ATTRIBUTE.getShortName()))
        .thenReturn(CG_IDPNUVY);

    Float cgIdpnuvy = (Float) CmetaaAttribute.CG_IDPNUVY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgIdpnuvy, is(Float.parseFloat(CG_IDPNUVY)));
  }

  @Test
  public void testCgIdpnuvz() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_IDPNUVZ_ATTRIBUTE.getShortName()))
        .thenReturn(CG_IDPNUVZ);

    Float cgIdpnuvz = (Float) CmetaaAttribute.CG_IDPNUVZ_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgIdpnuvz, is(Float.parseFloat(CG_IDPNUVZ)));
  }

  @Test
  public void testCgScecnX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SCECN_X_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SCECN_X);

    Float cgScecnX = (Float) CmetaaAttribute.CG_SCECN_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgScecnX, is(Float.parseFloat(CG_SCECN_X)));
  }

  @Test
  public void testCgScecnY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SCECN_Y_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SCECN_Y);

    Float cgScecnY = (Float) CmetaaAttribute.CG_SCECN_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgScecnY, is(Float.parseFloat(CG_SCECN_Y)));
  }

  @Test
  public void testCgScecnZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SCECN_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SCECN_Z);

    Float cgScecnZ = (Float) CmetaaAttribute.CG_SCECN_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgScecnZ, is(Float.parseFloat(CG_SCECN_Z)));
  }

  @Test
  public void testCgScConfXY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SC_CONF_XY_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SC_CONF_XY);

    Float cgScConfXY =
        (Float) CmetaaAttribute.CG_SC_CONF_XY_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgScConfXY, is(Float.parseFloat(CG_SC_CONF_XY)));
  }

  @Test
  public void testCgScConfZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SC_CONF_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SC_CONF_Z);

    Float cgScConfZ =
        (Float) CmetaaAttribute.CG_SC_CONF_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgScConfZ, is(Float.parseFloat(CG_SC_CONF_Z)));
  }

  @Test
  public void testCgSwwd() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SWWD_ATTRIBUTE.getShortName())).thenReturn(CG_SWWD);

    Float cgSwwd = (Float) CmetaaAttribute.CG_SWWD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSwwd, is(Float.parseFloat(CG_SWWD)));
  }

  @Test
  public void testCgSnvelX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNVEL_X_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNVEL_X);

    Float cgSnvelX = (Float) CmetaaAttribute.CG_SNVEL_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnvelX, is(Float.parseFloat(CG_SNVEL_X)));
  }

  @Test
  public void testCgSnvelY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNVEL_Y_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNVEL_Y);

    Float cgSnvelY = (Float) CmetaaAttribute.CG_SNVEL_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnvelY, is(Float.parseFloat(CG_SNVEL_Y)));
  }

  @Test
  public void testCgSnvelZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNVEL_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNVEL_Z);

    Float cgSnvelZ = (Float) CmetaaAttribute.CG_SNVEL_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnvelZ, is(Float.parseFloat(CG_SNVEL_Z)));
  }

  @Test
  public void testCgSnaccX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNACC_X_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNACC_X);

    Float cgSnaccX = (Float) CmetaaAttribute.CG_SNACC_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnaccX, is(Float.parseFloat(CG_SNACC_X)));
  }

  @Test
  public void testCgSnaccY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNACC_Y_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNACC_Y);

    Float cgSnaccY = (Float) CmetaaAttribute.CG_SNACC_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnaccY, is(Float.parseFloat(CG_SNACC_Y)));
  }

  @Test
  public void testCgSnaccZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNACC_Z_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNACC_Z);

    Float cgSnaccZ = (Float) CmetaaAttribute.CG_SNACC_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnaccZ, is(Float.parseFloat(CG_SNACC_Z)));
  }

  @Test
  public void testCgSnattRoll() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNATT_ROLL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNATT_ROLL);

    Float cgSnattRoll =
        (Float) CmetaaAttribute.CG_SNATT_ROLL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnattRoll, is(Float.parseFloat(CG_SNATT_ROLL)));
  }

  @Test
  public void testCgSnattPitch() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNATT_PITCH_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNATT_PITCH);

    Float cgSnattPitch =
        (Float) CmetaaAttribute.CG_SNATT_PITCH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnattPitch, is(Float.parseFloat(CG_SNATT_PITCH)));
  }

  @Test
  public void testCgSnattYaw() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_SNATT_YAW_ATTRIBUTE.getShortName()))
        .thenReturn(CG_SNATT_YAW);

    Float cgSnattYaw =
        (Float) CmetaaAttribute.CG_SNATT_YAW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgSnattYaw, is(Float.parseFloat(CG_SNATT_YAW)));
  }

  @Test
  public void testCgGtpX() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GTP_X_ATTRIBUTE.getShortName())).thenReturn(CG_GTP_X);

    Float cgGtpX = (Float) CmetaaAttribute.CG_GTP_X_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGtpX, is(Float.parseFloat(CG_GTP_X)));
  }

  @Test
  public void testCgGtpY() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GTP_Y_ATTRIBUTE.getShortName())).thenReturn(CG_GTP_Y);

    Float cgGtpY = (Float) CmetaaAttribute.CG_GTP_Y_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGtpY, is(Float.parseFloat(CG_GTP_Y)));
  }

  @Test
  public void testCgGtpZ() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_GTP_Z_ATTRIBUTE.getShortName())).thenReturn(CG_GTP_Z);

    Float cgGtpZ = (Float) CmetaaAttribute.CG_GTP_Z_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgGtpZ, is(Float.parseFloat(CG_GTP_Z)));
  }

  @Test
  public void testCgMapType() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MAP_TYPE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MAP_TYPE);

    String cgMapType =
        (String) CmetaaAttribute.CG_MAP_TYPE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMapType, is(CG_MAP_TYPE.trim()));
  }

  @Test
  public void testCgPatchLatcen() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LATCEN_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LATCEN);

    Float cgPatchLatcen =
        (Float) CmetaaAttribute.CG_PATCH_LATCEN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLatcen, is(Float.parseFloat(CG_PATCH_LATCEN)));
  }

  @Test
  public void testCgPatchLngcen() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LNGCEN_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LNGCEN);

    Float cgPatchLngcen =
        (Float) CmetaaAttribute.CG_PATCH_LNGCEN_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLngcen, is(Float.parseFloat(CG_PATCH_LNGCEN)));
  }

  @Test
  public void testCgPatchLtcorul() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LTCORUL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LTCORUL);

    Float cgPatchLtcorul =
        (Float) CmetaaAttribute.CG_PATCH_LTCORUL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLtcorul, is(Float.parseFloat(CG_PATCH_LTCORUL)));
  }

  @Test
  public void testCgPatchLgcorul() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LGCORUL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LGCORUL);

    Float cgPatchLgcorul =
        (Float) CmetaaAttribute.CG_PATCH_LGCORUL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLgcorul, is(Float.parseFloat(CG_PATCH_LGCORUL)));
  }

  @Test
  public void testCgPatchLtcorur() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LTCORUR_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LTCORUR);

    Float cgPatchLtcorur =
        (Float) CmetaaAttribute.CG_PATCH_LTCORUR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLtcorur, is(Float.parseFloat(CG_PATCH_LTCORUR)));
  }

  @Test
  public void testCgPatchLgcorur() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LGCORUR_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LGCORUR);

    Float cgPatchLgcorur =
        (Float) CmetaaAttribute.CG_PATCH_LGCORUR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLgcorur, is(Float.parseFloat(CG_PATCH_LGCORUR)));
  }

  @Test
  public void testCgPatchLtcorlr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LTCORLR_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LTCORLR);

    Float cgPatchLtcorlr =
        (Float) CmetaaAttribute.CG_PATCH_LTCORLR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLtcorlr, is(Float.parseFloat(CG_PATCH_LTCORLR)));
  }

  @Test
  public void testCgPatchLgcorlr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LGCORLR_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LGCORLR);

    Float cgPatchLgcorlr =
        (Float) CmetaaAttribute.CG_PATCH_LGCORLR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLgcorlr, is(Float.parseFloat(CG_PATCH_LGCORLR)));
  }

  @Test
  public void testCgPatchLtcorll() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LTCORLL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LTCORLL);

    Float cgPatchLtcorll =
        (Float) CmetaaAttribute.CG_PATCH_LTCORLL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLtcorll, is(Float.parseFloat(CG_PATCH_LTCORLL)));
  }

  @Test
  public void testCgPatchLngcoll() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LNGCOLL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LNGCOLL);

    Float cgPatchLngcoll =
        (Float) CmetaaAttribute.CG_PATCH_LNGCOLL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLngcoll, is(Float.parseFloat(CG_PATCH_LNGCOLL)));
  }

  @Test
  public void testCgPatchLatConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LAT_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LAT_CONFIDENCE);

    Float cgPatchLatConfidence =
        (Float) CmetaaAttribute.CG_PATCH_LAT_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLatConfidence, is(Float.parseFloat(CG_PATCH_LAT_CONFIDENCE)));
  }

  @Test
  public void testCgPatchLongConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_PATCH_LONG_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_PATCH_LONG_CONFIDENCE);

    Float cgPatchLongConfidence =
        (Float) CmetaaAttribute.CG_PATCH_LONG_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgPatchLongConfidence, is(Float.parseFloat(CG_PATCH_LONG_CONFIDENCE)));
  }

  @Test
  public void testCgMgrsCent() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MGRS_CENT_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MGRS_CENT);

    String cgMgrsCent =
        (String) CmetaaAttribute.CG_MGRS_CENT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMgrsCent, is(CG_MGRS_CENT.trim()));
  }

  @Test
  public void testCgMgrsCorul() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MGRSCORUL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MGRSCORUL);

    String cgMgrsCorul =
        (String) CmetaaAttribute.CG_MGRSCORUL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMgrsCorul, is(CG_MGRSCORUL.trim()));
  }

  @Test
  public void testCgMgrsCorur() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MGRSCORUR_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MGRSCORUR);

    String cgMgrsCorur =
        (String) CmetaaAttribute.CG_MGRSCORUR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMgrsCorur, is(CG_MGRSCORUR.trim()));
  }

  @Test
  public void testCgMgrsCorlr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MGRSCORLR_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MGRSCORLR);

    String cgMgrsCorlr =
        (String) CmetaaAttribute.CG_MGRSCORLR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMgrsCorlr, is(CG_MGRSCORLR.trim()));
  }

  @Test
  public void testCgMgrCorll() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MGRCORLL_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MGRCORLL);

    String cgMgrCorll =
        (String) CmetaaAttribute.CG_MGRCORLL_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMgrCorll, is(CG_MGRCORLL.trim()));
  }

  @Test
  public void testCgMgrsConfidence() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CG_MGRS_CONFIDENCE_ATTRIBUTE.getShortName()))
        .thenReturn(CG_MGRS_CONFIDENCE);

    Float cgMgrsConfidence =
        (Float) CmetaaAttribute.CG_MGRS_CONFIDENCE_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(cgMgrsConfidence, is(Float.parseFloat(CG_MGRS_CONFIDENCE)));
  }

  @Test
  public void testCaCalpa() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.CA_CALPA_ATTRIBUTE.getShortName())).thenReturn(CA_CALPA);

    Float caCalpa = (Float) CmetaaAttribute.CA_CALPA_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(caCalpa, is(Float.parseFloat(CA_CALPA)));
  }

  @Test
  public void testWfSrtfr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_SRTFR_ATTRIBUTE.getShortName())).thenReturn(WF_SRTFR);

    Float wfSrtfr = (Float) CmetaaAttribute.WF_SRTFR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfSrtfr, is(Float.parseFloat(WF_SRTFR)));
  }

  @Test
  public void testWfEndfr() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_ENDFR_ATTRIBUTE.getShortName())).thenReturn(WF_ENDFR);

    Float wfEndfr = (Float) CmetaaAttribute.WF_ENDFR_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfEndfr, is(Float.parseFloat(WF_ENDFR)));
  }

  @Test
  public void testWfChrprt() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_CHRPRT_ATTRIBUTE.getShortName()))
        .thenReturn(WF_CHRPRT);

    Float wfChrprt = (Float) CmetaaAttribute.WF_CHRPRT_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfChrprt, is(Float.parseFloat(WF_CHRPRT)));
  }

  @Test
  public void testWfWidth() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_WIDTH_ATTRIBUTE.getShortName())).thenReturn(WF_WIDTH);

    Float wfWidth = (Float) CmetaaAttribute.WF_WIDTH_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfWidth, is(Float.parseFloat(WF_WIDTH)));
  }

  @Test
  public void testWfCenfrq() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_CENFRQ_ATTRIBUTE.getShortName()))
        .thenReturn(WF_CENFRQ);

    Float wfCenfrq = (Float) CmetaaAttribute.WF_CENFRQ_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfCenfrq, is(Float.parseFloat(WF_CENFRQ)));
  }

  @Test
  public void testWfBw() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_BW_ATTRIBUTE.getShortName())).thenReturn(WF_BW);

    Float wfBw = (Float) CmetaaAttribute.WF_BW_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfBw, is(Float.parseFloat(WF_BW)));
  }

  @Test
  public void testWfPrf() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_PRF_ATTRIBUTE.getShortName())).thenReturn(WF_PRF);

    Float wfPrf = (Float) CmetaaAttribute.WF_PRF_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfPrf, is(Float.parseFloat(WF_PRF)));
  }

  @Test
  public void testWfPri() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_PRI_ATTRIBUTE.getShortName())).thenReturn(WF_PRI);

    Float wfPri = (Float) CmetaaAttribute.WF_PRI_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfPri, is(Float.parseFloat(WF_PRI)));
  }

  @Test
  public void testWfCdp() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_CDP_ATTRIBUTE.getShortName())).thenReturn(WF_CDP);

    Float wfCdp = (Float) CmetaaAttribute.WF_CDP_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfCdp, is(Float.parseFloat(WF_CDP)));
  }

  @Test
  public void testWfNumberOfPulses() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.WF_NUMBER_OF_PULSES_ATTRIBUTE.getShortName()))
        .thenReturn(WF_NUMBER_OF_PULSES);

    Integer wfNumberOfPulses =
        (Integer) CmetaaAttribute.WF_NUMBER_OF_PULSES_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(wfNumberOfPulses, is(Integer.parseInt(WF_NUMBER_OF_PULSES, 10)));
  }

  @Test
  public void testVphCond() throws NitfFormatException {
    when(tre.getFieldValue(CmetaaAttribute.VPH_COND_ATTRIBUTE.getShortName())).thenReturn(VPH_COND);

    String vphCond = (String) CmetaaAttribute.VPH_COND_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(vphCond, is(VPH_COND.trim()));
  }
}
