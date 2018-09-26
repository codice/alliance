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

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttributeImpl;
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.tre.Tre;

public class CmetaaAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "cmetaa.";

  public static final String CMETAA = "CMETAA";

  public static final String RELATED_TRES = PREFIX + "related-tres";

  public static final String ADDITIONAL_TRES = PREFIX + "additional-tres";

  public static final String RD_PRC_NO = PREFIX + "processor-version";

  public static final String IF_PROCESS = PREFIX + "vph-processing-method";

  public static final String RD_CEN_FREQ = PREFIX + "center-frequency-band";

  public static final String RD_MODE = PREFIX + "collection-mode";

  public static final String RD_PATCH_NO = PREFIX + "data-patch-number";

  public static final String CMPLX_DOMAIN = PREFIX + "complex-domain";

  public static final String CMPLX_MAG_REMAP_TYPE = PREFIX + "magnitude-remap-type";

  public static final String CMPLX_LIN_SCALE = PREFIX + "linear-scale-factor";

  public static final String CMPLX_AVG_POWER = PREFIX + "average-power";

  public static final String CMPLX_LINLOG_TP = PREFIX + "linlog-transition-point";

  public static final String CMPLX_PHASE_QUANT_FLAG = PREFIX + "phase-quantization";

  public static final String CMPLX_PHASE_QUANT_BIT_DEPTH = PREFIX + "phase-quantization-bit-depth";

  public static final String CMPLX_SIZE_1 = PREFIX + "first-pixel-size";

  public static final String CMPLX_IC_1 = PREFIX + "first-pixel-data-compression";

  public static final String CMPLX_SIZE_2 = PREFIX + "second-pixel-size";

  public static final String CMPLX_IC_2 = PREFIX + "second-pixel-data-compression";

  public static final String CMPLX_IC_BPP = PREFIX + "compressed-bits-per-pixel";

  public static final String CMPLX_WEIGHT = PREFIX + "weighting-type";

  public static final String CMPLX_AZ_SLL = PREFIX + "azimuth-sidelobe-level";

  public static final String CMPLX_RNG_SLL = PREFIX + "range-sidelob-level";

  public static final String CMPLX_AZ_TAY_NBAR = PREFIX + "azimuth-taylor-nbar";

  public static final String CMPLX_RNG_TAY_NBAR = PREFIX + "range-taylor-nbar";

  public static final String CMPLX_WEIGHT_NORM = PREFIX + "weight-normalization";

  public static final String CMPLX_SIGNAL_PLANE = PREFIX + "complex-plane";

  public static final String IF_DC_SF_ROW =
      PREFIX + "zero-frequency-spatial-frequency-location-row";

  public static final String IF_DC_SF_COL =
      PREFIX + "zero-frequency-spatial-frequency-location-column";

  public static final String IF_PATCH_1_ROW = PREFIX + "location-upper-left-row";

  public static final String IF_PATCH_1_COL = PREFIX + "location-upper-left-column";

  public static final String IF_PATCH_2_ROW = PREFIX + "location-upper-right-row";

  public static final String IF_PATCH_2_COL = PREFIX + "location-upper-right-column";

  public static final String IF_PATCH_3_ROW = PREFIX + "location-lower-right-row";

  public static final String IF_PATCH_3_COL = PREFIX + "location-lower-right-column";

  public static final String IF_PATCH_4_ROW = PREFIX + "location-lower-left-row";

  public static final String IF_PATCH_4_COL = PREFIX + "location-lower-left-column";

  public static final String IF_DC_IS_ROW = PREFIX + "zero-frequency-image-space-location-row";

  public static final String IF_DC_IS_COL = PREFIX + "zero-frequency-image-space-location-column";

  public static final String IF_IMG_ROW_DC = PREFIX + "patch-location-row";

  public static final String IF_IMG_COL_DC = PREFIX + "patch-location-column";

  public static final String IF_TILE_1_ROW = PREFIX + "tile-location-upper-left-row";

  public static final String IF_TILE_1_COL = PREFIX + "tile-location.upper-left-column";

  public static final String IF_TILE_2_ROW = PREFIX + "tile-location-upper-right-row";

  public static final String IF_TILE_2_COL = PREFIX + "tile-location.upper-right-column";

  public static final String IF_TILE_3_ROW = PREFIX + "tile-location-lower-right-row";

  public static final String IF_TILE_3_COL = PREFIX + "tile-location.lower-right-column";

  public static final String IF_TILE_4_ROW = PREFIX + "tile-location-lower-left-row";

  public static final String IF_TILE_4_COL = PREFIX + "tile-location.lower-left-column";

  public static final String IF_RD = PREFIX + "range-deskew";

  public static final String IF_RGWLK = PREFIX + "range-walk-correction";

  public static final String IF_KEYSTN =
      PREFIX + "range-curvature-and-keystone-distortion-correction";

  public static final String IF_LINSFT = PREFIX + "residual-linear-shift-correction";

  public static final String IF_SUBPATCH = PREFIX + "subpatch-phase-correction";

  public static final String IF_GEODIST = PREFIX + "deterministic-geometric-distortion-corrections";

  public static final String IF_RGFO = PREFIX + "range-fall-off-correction";

  public static final String IF_BEAM_COMP = PREFIX + "antenna-beam-pattern-compensation";

  public static final String IF_RGRES = PREFIX + "range-resolution";

  public static final String IF_AZRES = PREFIX + "azimuth-resolution";

  public static final String IF_RSS = PREFIX + "range-sample-spacing";

  public static final String IF_AZSS = PREFIX + "azimuth-sample-spacing";

  public static final String IF_RSR = PREFIX + "range-sample-rate";

  public static final String IF_AZSR = PREFIX + "azimuth-sample-rate";

  public static final String IF_RFFT_SAMP = PREFIX + "original-range-fft-samples";

  public static final String IF_AZFFT_SAMP = PREFIX + "original-azimuth-fft-samples";

  public static final String IF_RFFT_TOT = PREFIX + "total-range-fft-samples";

  public static final String IF_AZFFT_TOT = PREFIX + "total-azimuth-fft-samples";

  public static final String IF_SUBP_ROW = PREFIX + "subpatch-range-pixels";

  public static final String IF_SUBP_COL = PREFIX + "subpatch-azimuth-pixels";

  public static final String IF_SUB_RG = PREFIX + "range-subpatch-count";

  public static final String IF_SUB_AZ = PREFIX + "azimuth-subpatch-count";

  public static final String IF_RFFTS = PREFIX + "range-fft-sign";

  public static final String IF_AFFTS = PREFIX + "azimuth-fft-sign";

  public static final String IF_RANGE_DATA = PREFIX + "range-data";

  public static final String IF_INCPH = PREFIX + "increasing-phase";

  public static final String IF_SR_NAME1 = PREFIX + "first-iteration-super-resolution-algorithm";

  public static final String IF_SR_AMOUNT1 = PREFIX + "first-iteration-super-resolution-amount";

  public static final String IF_SR_NAME2 = PREFIX + "second-iteration-super-resolution-algorithm";

  public static final String IF_SR_AMOUNT2 = PREFIX + "second-iteration-super-resolution-amount";

  public static final String IF_SR_NAME3 = PREFIX + "third-iteration-super-resolution-algorithm";

  public static final String IF_SR_AMOUNT = PREFIX + "third-iteration-super-resolution-amount";

  public static final String AF_TYPE1 = PREFIX + "first-autofocus-iteration";

  public static final String AF_TYPE2 = PREFIX + "second-autofocus-iteration";

  public static final String AF_TYPE3 = PREFIX + "third-autofocus-iteration";

  public static final String POL_TR = PREFIX + "transmit-polarization";

  public static final String POL_RE = PREFIX + "receive-polarization";

  public static final String POL_REFERENCE = PREFIX + "polarization-reference";

  public static final String POL = PREFIX + "polarimetric-data-set";

  public static final String POL_REG = PREFIX + "pixel-registered";

  public static final String POL_ISO_1 = PREFIX + "minimum-polarization-isolation";

  public static final String POL_BAL = PREFIX + "rcs-gray-level-balancing";

  public static final String POL_BAL_MAG = PREFIX + "pixel-amplitude-balance-coefficient";

  public static final String POL_BAL_PHS = PREFIX + "pixel-phase-balance-coefficient";

  public static final String POL_HCOMP = PREFIX + "hardware-phase-balancing";

  public static final String POL_HCOMP_BASIS = PREFIX + "phase-balancing-basis-set";

  public static final String POL_HCOMP_COEF_1 = PREFIX + "first-phase-balancing-coefficient";

  public static final String POL_HCOMP_COEF_2 = PREFIX + "second-phase-balancing-coefficient";

  public static final String POL_HCOMP_COEF_3 = PREFIX + "third-phase-balancing-coefficient";

  public static final String POL_AFCOMP = PREFIX + "autofocus-phase-balancing";

  public static final String POL_SPARE_A = PREFIX + "spare-alpha-field";

  public static final String POL_SPARE_N = PREFIX + "spare-numeric-field";

  public static final String T_UTC_YYYYMMMDD = PREFIX + "collection-date";

  public static final String T_HHMMSSUTC = PREFIX + "collection-time";

  public static final String T_HHMMSSLOCAL = PREFIX + "local-collection-time";

  public static final String CG_SRAC = PREFIX + "center-slant-range";

  public static final String CG_SLANT_CONFIDENCE = PREFIX + "slant-range-confidence";

  public static final String CG_CROSS = PREFIX + "center-cross-track-range";

  public static final String CG_CROSS_CONFIDENCE = PREFIX + "cross-track-confidence";

  public static final String CG_CAAC = PREFIX + "cone-angle";

  public static final String CG_CONE_CONFIDENCE = PREFIX + "cone-angle-confidence";

  public static final String CG_GPSAC = PREFIX + "groung-plane-squint-angle";

  public static final String CG_GPSAC_CONFIDENCE = "ground-plane-squint-angle-confidence";

  public static final String CG_SQUINT = PREFIX + "slant-plan-squint-angle";

  public static final String CG_GAAC = PREFIX + "center-grazing-angle";

  public static final String CG_GAAC_CONFIDENCE = PREFIX + "center-grazing-angle-confidence";

  public static final String CG_INCIDENT = PREFIX + "incident-angle";

  public static final String CG_SLOPE = PREFIX + "slope-angle";

  public static final String CG_TILT = PREFIX + "tilt-angle";

  public static final String CG_LD = PREFIX + "look-direction";

  public static final String CG_NORTH = PREFIX + "image-angle-to-north";

  public static final String CG_NORTH_CONFIDENCE = PREFIX + "north-angle-confidence";

  public static final String CG_EAST = PREFIX + "image-angle-to-east";

  public static final String CG_RLOS = PREFIX + "image-range-angle-to-line-of-sight";

  public static final String CG_LOS_CONFIDENCE = PREFIX + "angle-to-line-of-sight-confidence";

  public static final String CG_LAYOVER = PREFIX + "layover-angle";

  public static final String CG_SHADOW = PREFIX + "shadow-angle";

  public static final String CG_OPM = PREFIX + "out-of-plane-motion";

  public static final String CG_MODEL = PREFIX + "nominal-geometry-reference";

  public static final String CG_AMPT_X = PREFIX + "aimpoint-x-coordinate";

  public static final String CG_AMPT_Y = PREFIX + "aimpoint-y-coordinate";

  public static final String CG_AMPT_Z = PREFIX + "aimpoint-z-coordinate";

  public static final String CG_AP_CONF_XY = PREFIX + "aimpoint-xy-confidence";

  public static final String CG_AP_CONF_Z = PREFIX + "aimpoint-z-confidence";

  public static final String CG_APCEN_X = PREFIX + "sensor-reference-point-x";

  public static final String CG_APCEN_Y = PREFIX + "sensor-reference-point-y";

  public static final String CG_APCEN_Z = PREFIX + "sensor-reference-point-z";

  public static final String CG_APER_CONF_XY = PREFIX + "sensor-reference-point-xy-confidence";

  public static final String CG_APER_CONF_Z = PREFIX + "sensor-reference-point-z-confidence";

  public static final String CG_FPNUV_X = PREFIX + "focus-plane-normal-unit-vector-x";

  public static final String CG_FPNUV_Y = PREFIX + "focus-plane-normal-unit-vector-y";

  public static final String CG_FPNUV_Z = PREFIX + "focus-plane-normal-unit-vector-z";

  public static final String CG_IDPNUVX = PREFIX + "image-display-plane-normal-unit-vector-x";

  public static final String CG_IDPNUVY = PREFIX + "image-display-plane-normal-unit-vector-y";

  public static final String CG_IDPNUVZ = PREFIX + "image-display-plane-normal-unit-vector-z";

  public static final String CG_SCECN_X = PREFIX + "scene-center-x";

  public static final String CG_SCECN_Y = PREFIX + "scene-center-y";

  public static final String CG_SCECN_Z = PREFIX + "scene-center-z";

  public static final String CG_SC_CONF_XY = PREFIX + "scene-center-confidence-xy";

  public static final String CG_SC_CONF_Z = PREFIX + "scene-center-confidence-z";

  public static final String CG_SWWD = PREFIX + "swath-width";

  public static final String CG_SNVEL_X = PREFIX + "sensor-nominal-velocity-x";

  public static final String CG_SNVEL_Y = PREFIX + "sensor-nominal-velocity-y";

  public static final String CG_SNVEL_Z = PREFIX + "sensor-nominal-velocity-z";

  public static final String CG_SNACC_X = PREFIX + "sensor-nominal-acceleration-x";

  public static final String CG_SNACC_Y = PREFIX + "sensor-nominal-acceleration-y";

  public static final String CG_SNACC_Z = PREFIX + "sensor-nominal-acceleration-z";

  public static final String CG_SNATT_ROLL = PREFIX + "sensor-nominal-attitude-roll";

  public static final String CG_SNATT_PITCH = PREFIX + "sensor-nominal-attitude-pitch";

  public static final String CG_SNATT_YAW = PREFIX + "sensor-nominal-attitude-yaw";

  public static final String CG_GTP_X = PREFIX + "geoid-tangent-plane-normal-x";

  public static final String CG_GTP_Y = PREFIX + "geoid-tangent-plane-normal-y";

  public static final String CG_GTP_Z = PREFIX + "geoid-tangent-plane-normal-z";

  public static final String CG_MAP_TYPE = PREFIX + "mapping-coordinate";

  public static final String CG_PATCH_LATCEN = PREFIX + "patch-center-latitude";

  public static final String CG_PATCH_LNGCEN = PREFIX + "patch-center-longitude";

  public static final String CG_PATCH_LTCORUL = PREFIX + "patch-upper-left-latitude";

  public static final String CG_PATCH_LGCORUL = PREFIX + "patch-upper-left-longitude";

  public static final String CG_PATCH_LTCORUR = PREFIX + "patch-upper-right-latitude";

  public static final String CG_PATCH_LGCORUR = PREFIX + "patch-upper-right-longitude";

  public static final String CG_PATCH_LTCORLR = PREFIX + "patch-lower-right-latitude";

  public static final String CG_PATCH_LGCORLR = PREFIX + "patch-lower-right-longitude";

  public static final String CG_PATCH_LTCORLL = PREFIX + "patch-lower-left-latitude";

  public static final String CG_PATCH_LNGCOLL = PREFIX + "patch-lower-left-longitude";

  public static final String CG_PATCH_LAT_CONFIDENCE = PREFIX + "patch-latitude-confidence";

  public static final String CG_PATCH_LONG_CONFIDENCE = PREFIX + "patch-longitude-confidence";

  public static final String CG_MGRS_CENT = PREFIX + "image-center-mgrs";

  public static final String CG_MGRSCORUL = PREFIX + "image-upper-left-corner";

  public static final String CG_MGRSCORUR = PREFIX + "image-upper-right-corner";

  public static final String CG_MGRSCORLR = PREFIX + "image-lower-right-corner";

  public static final String CG_MGRCORLL = PREFIX + "image-lower-left-corner";

  public static final String CG_MGRS_CONFIDENCE = PREFIX + "mgrs-confidence";

  public static final String CG_SPARE_A = PREFIX + "spare-collection-geometry-alpha-field";

  public static final String CA_CALPA = PREFIX + "radiometric-calibration-parameter";

  public static final String WF_SRTFR = PREFIX + "chirp-start-frequency";

  public static final String WF_ENDFR = PREFIX + "chirp-end-frequency";

  public static final String WF_CHRPRT = PREFIX + "chirp-rate";

  public static final String WF_WIDTH = PREFIX + "pulsewidth";

  public static final String WF_CENFRQ = PREFIX + "center-frequency";

  public static final String WF_BW = PREFIX + "chirp-bandwidth";

  public static final String WF_PRF = PREFIX + "pulse-repetition-frequency";

  public static final String WF_PRI = PREFIX + "pulse-repetition-interval";

  public static final String WF_CDP = PREFIX + "coherent-data-period";

  public static final String WF_NUMBER_OF_PULSES = PREFIX + "number-of-pulses";

  public static final String VPH_COND = PREFIX + "vph-conditional-fields";

  public static final CmetaaAttribute RELATED_TRES_ATTRIBUTE =
      new CmetaaAttribute(
          RELATED_TRES,
          "RELATED_TRES",
          tre -> TreUtility.convertToInteger(tre, "RELATED_TRES"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute ADDITIONAL_TRES_ATTRIBUTE =
      new CmetaaAttribute(
          ADDITIONAL_TRES,
          "ADDITIONAL_TRES",
          tre -> TreUtility.getTreValue(tre, "ADDITIONAL_TRES"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute RD_PRC_NO_ATTRIBUTE =
      new CmetaaAttribute(
          RD_PRC_NO,
          "RD_PRC_NO",
          tre -> TreUtility.getTreValue(tre, "RD_PRC_NO"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_PROCESS_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PROCESS,
          "IF_PROCESS",
          tre -> TreUtility.getTreValue(tre, "IF_PROCESS"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute RD_CEN_FREQ_ATTRIBUTE =
      new CmetaaAttribute(
          RD_CEN_FREQ,
          "RD_CEN_FREQ",
          tre -> TreUtility.getTreValue(tre, "RD_CEN_FREQ"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute RD_MODE_ATTRIBUTE =
      new CmetaaAttribute(
          RD_MODE,
          "RD_MODE",
          tre -> TreUtility.getTreValue(tre, "RD_MODE"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute RD_PATCH_NO_ATTRIBUTE =
      new CmetaaAttribute(
          RD_PATCH_NO,
          "RD_PATCH_NO",
          tre -> TreUtility.convertToInteger(tre, "RD_PATCH_NO"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_DOMAIN_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_DOMAIN,
          "CMPLX_DOMAIN",
          tre -> TreUtility.getTreValue(tre, "CMPLX_DOMAIN"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_MAG_REMAP_TYPE_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_MAG_REMAP_TYPE,
          "CMPLX_MAG_REMAP_TYPE",
          tre -> TreUtility.getTreValue(tre, "CMPLX_MAG_REMAP_TYPE"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_LIN_SCALE_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_LIN_SCALE,
          "CMPLX_LIN_SCALE",
          tre -> TreUtility.convertToFloat(tre, "CMPLX_LIN_SCALE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CMPLX_AVG_POWER_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_AVG_POWER,
          "CMPLX_AVG_POWER",
          tre -> TreUtility.convertToFloat(tre, "CMPLX_AVG_POWER"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CMPLX_LINLOG_TP_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_LINLOG_TP,
          "CMPLX_LINLOG_TP",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_LINLOG_TP"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_PHASE_QUANT_FLAG_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_PHASE_QUANT_FLAG,
          "CMPLX_PHASE_QUANT_FLAG",
          tre -> TreUtility.getTreValue(tre, "CMPLX_PHASE_QUANT_FLAG"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_PHASE_QUANT_BIT_DEPTH_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_PHASE_QUANT_BIT_DEPTH,
          "CMPLX_PHASE_QUANT_BIT_DEPTH",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_PHASE_QUANT_BIT_DEPTH"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_SIZE_1_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_SIZE_1,
          "CMPLX_SIZE_1",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_SIZE_1"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_IC_1_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_IC_1,
          "CMPLX_IC_1",
          tre -> TreUtility.getTreValue(tre, "CMPLX_IC_1"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_SIZE_2_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_SIZE_2,
          "CMPLX_SIZE_2",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_SIZE_2"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_IC_2_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_IC_2,
          "CMPLX_IC_2",
          tre -> TreUtility.getTreValue(tre, "CMPLX_IC_2"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_IC_BPP_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_IC_BPP,
          "CMPLX_IC_BPP",
          tre -> TreUtility.convertToFloat(tre, "CMPLX_IC_BPP"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CMPLX_WEIGHT_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_WEIGHT,
          "CMPLX_WEIGHT",
          tre -> TreUtility.getTreValue(tre, "CMPLX_WEIGHT"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_AZ_SLL_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_AZ_SLL,
          "CMPLX_AZ_SLL",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_AZ_SLL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_RNG_SLL_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_RNG_SLL,
          "CMPLX_RNG_SLL",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_RNG_SLL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_AZ_TAY_NBAR_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_AZ_TAY_NBAR,
          "CMPLX_AZ_TAY_NBAR",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_AZ_TAY_NBAR"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_RNG_TAY_NBAR_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_RNG_TAY_NBAR,
          "CMPLX_RNG_TAY_NBAR",
          tre -> TreUtility.convertToInteger(tre, "CMPLX_RNG_TAY_NBAR"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute CMPLX_WEIGHT_NORM_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_WEIGHT_NORM,
          "CMPLX_WEIGHT_NORM",
          tre -> TreUtility.getTreValue(tre, "CMPLX_WEIGHT_NORM"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CMPLX_SIGNAL_PLANE_ATTRIBUTE =
      new CmetaaAttribute(
          CMPLX_SIGNAL_PLANE,
          "CMPLX_SIGNAL_PLANE",
          tre -> TreUtility.getTreValue(tre, "CMPLX_SIGNAL_PLANE"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_DC_SF_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_DC_SF_ROW,
          "IF_DC_SF_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_DC_SF_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_DC_SF_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_DC_SF_COL,
          "IF_DC_SF_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_DC_SF_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_1_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_1_ROW,
          "IF_PATCH_1_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_1_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_1_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_1_COL,
          "IF_PATCH_1_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_1_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_2_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_2_ROW,
          "IF_PATCH_2_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_2_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_2_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_2_COL,
          "IF_PATCH_2_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_2_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_3_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_3_ROW,
          "IF_PATCH_3_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_3_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_3_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_3_COL,
          "IF_PATCH_3_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_3_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_4_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_4_ROW,
          "IF_PATCH_4_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_4_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_PATCH_4_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_PATCH_4_COL,
          "IF_PATCH_4_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_PATCH_4_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_DC_IS_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_DC_IS_ROW,
          "IF_DC_IS_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_DC_IS_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_DC_IS_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_DC_IS_COL,
          "IF_DC_IS_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_DC_IS_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_IMG_ROW_DC_ATTRIBUTE =
      new CmetaaAttribute(
          IF_IMG_ROW_DC,
          "IF_IMG_ROW_DC",
          tre -> TreUtility.convertToInteger(tre, "IF_IMG_ROW_DC"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_IMG_COL_DC_ATTRIBUTE =
      new CmetaaAttribute(
          IF_IMG_COL_DC,
          "IF_IMG_COL_DC",
          tre -> TreUtility.convertToInteger(tre, "IF_IMG_COL_DC"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_1_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_1_ROW,
          "IF_TILE_1_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_1_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_1_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_1_COL,
          "IF_TILE_1_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_1_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_2_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_2_ROW,
          "IF_TILE_2_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_2_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_2_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_2_COL,
          "IF_TILE_2_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_2_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_3_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_3_ROW,
          "IF_TILE_3_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_3_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_3_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_3_COL,
          "IF_TILE_3_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_3_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_4_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_4_ROW,
          "IF_TILE_4_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_4_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_TILE_4_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_TILE_4_COL,
          "IF_TILE_4_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_TILE_4_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_RD_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RD, "IF_RD", tre -> TreUtility.getTreValue(tre, "IF_RD"), BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_RGWLK_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RGWLK,
          "IF_RGWLK",
          tre -> TreUtility.getTreValue(tre, "IF_RGWLK"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_KEYSTN_ATTRIBUTE =
      new CmetaaAttribute(
          IF_KEYSTN,
          "IF_KEYSTN",
          tre -> TreUtility.getTreValue(tre, "IF_KEYSTN"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_LINSFT_ATTRIBUTE =
      new CmetaaAttribute(
          IF_LINSFT,
          "IF_LINSFT",
          tre -> TreUtility.getTreValue(tre, "IF_LINSFT"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_SUBPATCH_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SUBPATCH,
          "IF_SUBPATCH",
          tre -> TreUtility.getTreValue(tre, "IF_SUBPATCH"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_GEODIST_ATTRIBUTE =
      new CmetaaAttribute(
          IF_GEODIST,
          "IF_GEODIST",
          tre -> TreUtility.getTreValue(tre, "IF_GEODIST"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_RGFO_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RGFO,
          "IF_RGFO",
          tre -> TreUtility.getTreValue(tre, "IF_RGFO"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_BEAM_COMP_ATTRIBUTE =
      new CmetaaAttribute(
          IF_BEAM_COMP,
          "IF_BEAM_COMP",
          tre -> TreUtility.getTreValue(tre, "IF_BEAM_COMP"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_RGRES_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RGRES,
          "IF_RGRES",
          tre -> TreUtility.convertToFloat(tre, "IF_RGRES"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_AZRES_ATTRIBUTE =
      new CmetaaAttribute(
          IF_AZRES,
          "IF_AZRES",
          tre -> TreUtility.convertToFloat(tre, "IF_AZRES"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_RSS_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RSS, "IF_RSS", tre -> TreUtility.convertToFloat(tre, "IF_RSS"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_AZSS_ATTRIBUTE =
      new CmetaaAttribute(
          IF_AZSS,
          "IF_AZSS",
          tre -> TreUtility.convertToFloat(tre, "IF_AZSS"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_RSR_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RSR, "IF_RSR", tre -> TreUtility.convertToFloat(tre, "IF_RSR"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_AZSR_ATTRIBUTE =
      new CmetaaAttribute(
          IF_AZSR,
          "IF_AZSR",
          tre -> TreUtility.convertToFloat(tre, "IF_AZSR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_RFFT_SAMP_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RFFT_SAMP,
          "IF_RFFT_SAMP",
          tre -> TreUtility.convertToInteger(tre, "IF_RFFT_SAMP"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_AZFFT_SAMP_ATTRIBUTE =
      new CmetaaAttribute(
          IF_AZFFT_SAMP,
          "IF_AZFFT_SAMP",
          tre -> TreUtility.convertToInteger(tre, "IF_AZFFT_SAMP"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_RFFT_TOT_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RFFT_TOT,
          "IF_RFFT_TOT",
          tre -> TreUtility.convertToInteger(tre, "IF_RFFT_TOT"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_AZFFT_TOT_ATTRIBUTE =
      new CmetaaAttribute(
          IF_AZFFT_TOT,
          "IF_AZFFT_TOT",
          tre -> TreUtility.convertToInteger(tre, "IF_AZFFT_TOT"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_SUBP_ROW_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SUBP_ROW,
          "IF_SUBP_ROW",
          tre -> TreUtility.convertToInteger(tre, "IF_SUBP_ROW"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_SUBP_COL_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SUBP_COL,
          "IF_SUBP_COL",
          tre -> TreUtility.convertToInteger(tre, "IF_SUBP_COL"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_SUB_RG_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SUB_RG,
          "IF_SUB_RG",
          tre -> TreUtility.convertToInteger(tre, "IF_SUB_RG"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_SUB_AZ_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SUB_AZ,
          "IF_SUB_AZ",
          tre -> TreUtility.convertToInteger(tre, "IF_SUB_AZ"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute IF_RFFTS_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RFFTS,
          "IF_RFFTS",
          tre -> TreUtility.getTreValue(tre, "IF_RFFTS"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_AFFTS_ATTRIBUTE =
      new CmetaaAttribute(
          IF_AFFTS,
          "IF_AFFTS",
          tre -> TreUtility.getTreValue(tre, "IF_AFFTS"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_RANGE_DATA_ATTRIBUTE =
      new CmetaaAttribute(
          IF_RANGE_DATA,
          "IF_RANGE_DATA",
          tre -> TreUtility.getTreValue(tre, "IF_RANGE_DATA"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_INCPH_ATTRIBUTE =
      new CmetaaAttribute(
          IF_INCPH,
          "IF_INCPH",
          tre -> TreUtility.getTreValue(tre, "IF_INCPH"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_SR_NAME1_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SR_NAME1,
          "IF_SR_NAME1",
          tre -> TreUtility.getTreValue(tre, "IF_SR_NAME1"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_SR_AMOUNT1_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SR_AMOUNT1,
          "IF_SR_AMOUNT1",
          tre -> TreUtility.convertToFloat(tre, "IF_SR_AMOUNT1"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_SR_NAME2_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SR_NAME2,
          "IF_SR_NAME2",
          tre -> TreUtility.getTreValue(tre, "IF_SR_NAME2"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_SR_AMOUNT2_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SR_AMOUNT2,
          "IF_SR_AMOUNT2",
          tre -> TreUtility.convertToFloat(tre, "IF_SR_AMOUNT2"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute IF_SR_NAME3_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SR_NAME3,
          "IF_SR_NAME3",
          tre -> TreUtility.getTreValue(tre, "IF_SR_NAME3"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute IF_SR_AMOUNT_ATTRIBUTE =
      new CmetaaAttribute(
          IF_SR_AMOUNT,
          "IF_SR_AMOUNT",
          tre -> TreUtility.convertToFloat(tre, "IF_SR_AMOUNT"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute AF_TYPE1_ATTRIBUTE =
      new CmetaaAttribute(
          AF_TYPE1,
          "AF_TYPE1",
          tre -> TreUtility.getTreValue(tre, "AF_TYPE1"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute AF_TYPE2_ATTRIBUTE =
      new CmetaaAttribute(
          AF_TYPE2,
          "AF_TYPE2",
          tre -> TreUtility.getTreValue(tre, "AF_TYPE2"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute AF_TYPE3_ATTRIBUTE =
      new CmetaaAttribute(
          AF_TYPE3,
          "AF_TYPE3",
          tre -> TreUtility.getTreValue(tre, "AF_TYPE3"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_TR_ATTRIBUTE =
      new CmetaaAttribute(
          POL_TR, "POL_TR", tre -> TreUtility.getTreValue(tre, "POL_TR"), BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_RE_ATTRIBUTE =
      new CmetaaAttribute(
          POL_RE, "POL_RE", tre -> TreUtility.getTreValue(tre, "POL_RE"), BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_REFERENCE_ATTRIBUTE =
      new CmetaaAttribute(
          POL_REFERENCE,
          "POL_REFERENCE",
          tre -> TreUtility.getTreValue(tre, "POL_REFERENCE"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_ATTRIBUTE =
      new CmetaaAttribute(
          POL, "POL", tre -> TreUtility.getTreValue(tre, "POL"), BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_REG_ATTRIBUTE =
      new CmetaaAttribute(
          POL_REG,
          "POL_REG",
          tre -> TreUtility.getTreValue(tre, "POL_REG"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_ISO_1_ATTRIBUTE =
      new CmetaaAttribute(
          POL_ISO_1,
          "POL_ISO_1",
          tre -> TreUtility.convertToFloat(tre, "POL_ISO_1"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute POL_BAL_ATTRIBUTE =
      new CmetaaAttribute(
          POL_BAL,
          "POL_BAL",
          tre -> TreUtility.getTreValue(tre, "POL_BAL"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_BAL_MAG_ATTRIBUTE =
      new CmetaaAttribute(
          POL_BAL_MAG,
          "POL_BAL_MAG",
          tre -> TreUtility.convertToFloat(tre, "POL_BAL_MAG"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute POL_BAL_PHS_ATTRIBUTE =
      new CmetaaAttribute(
          POL_BAL_PHS,
          "POL_BAL_PHS",
          tre -> TreUtility.convertToFloat(tre, "POL_BAL_PHS"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute POL_HCOMP_ATTRIBUTE =
      new CmetaaAttribute(
          POL_HCOMP,
          "POL_HCOMP",
          tre -> TreUtility.getTreValue(tre, "POL_HCOMP"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_HCOMP_BASIS_ATTRIBUTE =
      new CmetaaAttribute(
          POL_HCOMP_BASIS,
          "POL_HCOMP_BASIS",
          tre -> TreUtility.getTreValue(tre, "POL_HCOMP_BASIS"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_HCOMP_COEF_1_ATTRIBUTE =
      new CmetaaAttribute(
          POL_HCOMP_COEF_1,
          "POL_HCOMP_COEF_1",
          tre -> TreUtility.convertToFloat(tre, "POL_HCOMP_COEF_1"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute POL_HCOMP_COEF_2_ATTRIBUTE =
      new CmetaaAttribute(
          POL_HCOMP_COEF_2,
          "POL_HCOMP_COEF_2",
          tre -> TreUtility.convertToFloat(tre, "POL_HCOMP_COEF_2"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute POL_HCOMP_COEF_3_ATTRIBUTE =
      new CmetaaAttribute(
          POL_HCOMP_COEF_3,
          "POL_HCOMP_COEF_3",
          tre -> TreUtility.convertToFloat(tre, "POL_HCOMP_COEF_3"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute POL_AFCOMP_ATTRIBUTE =
      new CmetaaAttribute(
          POL_AFCOMP,
          "POL_AFCOMP",
          tre -> TreUtility.getTreValue(tre, "POL_AFCOMP"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_SPARE_A_ATTRIBUTE =
      new CmetaaAttribute(
          POL_SPARE_A,
          "POL_SPARE_A",
          tre -> TreUtility.getTreValue(tre, "POL_SPARE_A"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute POL_SPARE_N_ATTRIBUTE =
      new CmetaaAttribute(
          POL_SPARE_N,
          "POL_SPARE_N",
          tre -> TreUtility.convertToInteger(tre, "POL_SPARE_N"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute T_UTC_YYYYMMMDD_ATTRIBUTE =
      new CmetaaAttribute(
          T_UTC_YYYYMMMDD,
          "T_UTC_YYYYMMMDD",
          tre -> TreUtility.getTreValue(tre, "T_UTC_YYYYMMMDD"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute T_HHMMSSUTC_ATTRIBUTE =
      new CmetaaAttribute(
          T_HHMMSSUTC,
          "T_HHMMSSUTC",
          tre -> TreUtility.getTreValue(tre, "T_HHMMSSUTC"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute T_HHMMSSLOCAL_ATTRIBUTE =
      new CmetaaAttribute(
          T_HHMMSSLOCAL,
          "T_HHMMSSLOCAL",
          tre -> TreUtility.getTreValue(tre, "T_HHMMSSLOCAL"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_SRAC_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SRAC,
          "CG_SRAC",
          tre -> TreUtility.convertToFloat(tre, "CG_SRAC"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SLANT_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SLANT_CONFIDENCE,
          "CG_SLANT_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_SLANT_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_CROSS_ATTRIBUTE =
      new CmetaaAttribute(
          CG_CROSS,
          "CG_CROSS",
          tre -> TreUtility.convertToFloat(tre, "CG_CROSS"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_CROSS_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_CROSS_CONFIDENCE,
          "CG_CROSS_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_CROSS_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_CAAC_ATTRIBUTE =
      new CmetaaAttribute(
          CG_CAAC,
          "CG_CAAC",
          tre -> TreUtility.convertToFloat(tre, "CG_CAAC"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_CONE_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_CONE_CONFIDENCE,
          "CG_CONE_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_CONE_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GPSAC_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GPSAC,
          "CG_GPSAC",
          tre -> TreUtility.convertToFloat(tre, "CG_GPSAC"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GPSAC_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GPSAC_CONFIDENCE,
          "CG_GPSAC_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_GPSAC_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SQUINT_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SQUINT,
          "CG_SQUINT",
          tre -> TreUtility.convertToFloat(tre, "CG_SQUINT"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GAAC_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GAAC,
          "CG_GAAC",
          tre -> TreUtility.convertToFloat(tre, "CG_GAAC"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GAAC_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GAAC_CONFIDENCE,
          "CG_GAAC_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_GAAC_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_INCIDENT_ATTRIBUTE =
      new CmetaaAttribute(
          CG_INCIDENT,
          "CG_INCIDENT",
          tre -> TreUtility.convertToFloat(tre, "CG_INCIDENT"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SLOPE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SLOPE,
          "CG_SLOPE",
          tre -> TreUtility.convertToFloat(tre, "CG_SLOPE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_TILT_ATTRIBUTE =
      new CmetaaAttribute(
          CG_TILT,
          "CG_TILT",
          tre -> TreUtility.convertToFloat(tre, "CG_TILT"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_LD_ATTRIBUTE =
      new CmetaaAttribute(
          CG_LD, "CG_LD", tre -> TreUtility.getTreValue(tre, "CG_LD"), BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_NORTH_ATTRIBUTE =
      new CmetaaAttribute(
          CG_NORTH,
          "CG_NORTH",
          tre -> TreUtility.convertToFloat(tre, "CG_NORTH"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_NORTH_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_NORTH_CONFIDENCE,
          "CG_NORTH_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_NORTH_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_EAST_ATTRIBUTE =
      new CmetaaAttribute(
          CG_EAST,
          "CG_EAST",
          tre -> TreUtility.convertToFloat(tre, "CG_EAST"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_RLOS_ATTRIBUTE =
      new CmetaaAttribute(
          CG_RLOS,
          "CG_RLOS",
          tre -> TreUtility.convertToFloat(tre, "CG_RLOS"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_LOS_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_LOS_CONFIDENCE,
          "CG_LOS_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_LOS_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_LAYOVER_ATTRIBUTE =
      new CmetaaAttribute(
          CG_LAYOVER,
          "CG_LAYOVER",
          tre -> TreUtility.convertToFloat(tre, "CG_LAYOVER"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SHADOW_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SHADOW,
          "CG_SHADOW",
          tre -> TreUtility.convertToFloat(tre, "CG_SHADOW"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_OPM_ATTRIBUTE =
      new CmetaaAttribute(
          CG_OPM, "CG_OPM", tre -> TreUtility.convertToFloat(tre, "CG_OPM"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_MODEL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MODEL,
          "CG_MODEL",
          tre -> TreUtility.getTreValue(tre, "CG_MODEL"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_AMPT_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_AMPT_X,
          "CG_AMPT_X",
          tre -> TreUtility.convertToFloat(tre, "CG_AMPT_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_AMPT_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_AMPT_Y,
          "CG_AMPT_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_AMPT_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_AMPT_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_AMPT_Z,
          "CG_AMPT_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_AMPT_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_AP_CONF_XY_ATTRIBUTE =
      new CmetaaAttribute(
          CG_AP_CONF_XY,
          "CG_AP_CONF_XY",
          tre -> TreUtility.convertToFloat(tre, "CG_AP_CONF_XY"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_AP_CONF_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_AP_CONF_Z,
          "CG_AP_CONF_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_AP_CONF_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_APCEN_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_APCEN_X,
          "CG_APCEN_X",
          tre -> TreUtility.convertToFloat(tre, "CG_APCEN_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_APCEN_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_APCEN_Y,
          "CG_APCEN_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_APCEN_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_APCEN_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_APCEN_Z,
          "CG_APCEN_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_APCEN_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_APER_CONF_XY_ATTRIBUTE =
      new CmetaaAttribute(
          CG_APER_CONF_XY,
          "CG_APER_CONF_XY",
          tre -> TreUtility.convertToFloat(tre, "CG_APER_CONF_XY"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_APER_CONF_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_APER_CONF_Z,
          "CG_APER_CONF_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_APER_CONF_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_FPNUV_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_FPNUV_X,
          "CG_FPNUV_X",
          tre -> TreUtility.convertToFloat(tre, "CG_FPNUV_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_FPNUV_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_FPNUV_Y,
          "CG_FPNUV_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_FPNUV_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_FPNUV_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_FPNUV_Z,
          "CG_FPNUV_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_FPNUV_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_IDPNUVX_ATTRIBUTE =
      new CmetaaAttribute(
          CG_IDPNUVX,
          "CG_IDPNUVX",
          tre -> TreUtility.convertToFloat(tre, "CG_IDPNUVX"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_IDPNUVY_ATTRIBUTE =
      new CmetaaAttribute(
          CG_IDPNUVY,
          "CG_IDPNUVY",
          tre -> TreUtility.convertToFloat(tre, "CG_IDPNUVY"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_IDPNUVZ_ATTRIBUTE =
      new CmetaaAttribute(
          CG_IDPNUVZ,
          "CG_IDPNUVZ",
          tre -> TreUtility.convertToFloat(tre, "CG_IDPNUVZ"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SCECN_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SCECN_X,
          "CG_SCECN_X",
          tre -> TreUtility.convertToFloat(tre, "CG_SCECN_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SCECN_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SCECN_Y,
          "CG_SCECN_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_SCECN_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SCECN_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SCECN_Z,
          "CG_SCECN_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_SCECN_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SC_CONF_XY_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SC_CONF_XY,
          "CG_SC_CONF_XY",
          tre -> TreUtility.convertToFloat(tre, "CG_SC_CONF_XY"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SC_CONF_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SC_CONF_Z,
          "CG_SC_CONF_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_SC_CONF_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SWWD_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SWWD,
          "CG_SWWD",
          tre -> TreUtility.convertToFloat(tre, "CG_SWWD"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNVEL_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNVEL_X,
          "CG_SNVEL_X",
          tre -> TreUtility.convertToFloat(tre, "CG_SNVEL_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNVEL_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNVEL_Y,
          "CG_SNVEL_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_SNVEL_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNVEL_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNVEL_Z,
          "CG_SNVEL_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_SNVEL_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNACC_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNACC_X,
          "CG_SNACC_X",
          tre -> TreUtility.convertToFloat(tre, "CG_SNACC_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNACC_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNACC_Y,
          "CG_SNACC_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_SNACC_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNACC_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNACC_Z,
          "CG_SNACC_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_SNACC_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNATT_ROLL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNATT_ROLL,
          "CG_SNATT_ROLL",
          tre -> TreUtility.convertToFloat(tre, "CG_SNATT_ROLL"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNATT_PITCH_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNATT_PITCH,
          "CG_SNATT_PITCH",
          tre -> TreUtility.convertToFloat(tre, "CG_SNATT_PITCH"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SNATT_YAW_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SNATT_YAW,
          "CG_SNATT_YAW",
          tre -> TreUtility.convertToFloat(tre, "CG_SNATT_YAW"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GTP_X_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GTP_X,
          "CG_GTP_X",
          tre -> TreUtility.convertToFloat(tre, "CG_GTP_X"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GTP_Y_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GTP_Y,
          "CG_GTP_Y",
          tre -> TreUtility.convertToFloat(tre, "CG_GTP_Y"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_GTP_Z_ATTRIBUTE =
      new CmetaaAttribute(
          CG_GTP_Z,
          "CG_GTP_Z",
          tre -> TreUtility.convertToFloat(tre, "CG_GTP_Z"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_MAP_TYPE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MAP_TYPE,
          "CG_MAP_TYPE",
          tre -> TreUtility.getTreValue(tre, "CG_MAP_TYPE"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_PATCH_LATCEN_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LATCEN,
          "CG_PATCH_LATCEN",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LATCEN"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LNGCEN_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LNGCEN,
          "CG_PATCH_LNGCEN",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LNGCEN"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LTCORUL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LTCORUL,
          "CG_PATCH_LTCORUL",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LTCORUL"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LGCORUL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LGCORUL,
          "CG_PATCH_LGCORUL",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LGCORUL"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LTCORUR_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LTCORUR,
          "CG_PATCH_LTCORUR",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LTCORUR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LGCORUR_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LGCORUR,
          "CG_PATCH_LGCORUR",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LGCORUR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LTCORLR_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LTCORLR,
          "CG_PATCH_LTCORLR",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LTCORLR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LGCORLR_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LGCORLR,
          "CG_PATCH_LGCORLR",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LGCORLR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LTCORLL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LTCORLL,
          "CG_PATCH_LTCORLL",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LTCORLL"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LNGCOLL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LNGCOLL,
          "CG_PATCH_LNGCOLL",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LNGCOLL"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LAT_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LAT_CONFIDENCE,
          "CG_PATCH_LAT_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LAT_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_PATCH_LONG_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_PATCH_LONG_CONFIDENCE,
          "CG_PATCH_LONG_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_PATCH_LONG_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_MGRS_CENT_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MGRS_CENT,
          "CG_MGRS_CENT",
          tre -> TreUtility.getTreValue(tre, "CG_MGRS_CENT"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_MGRSCORUL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MGRSCORUL,
          "CG_MGRSCORUL",
          tre -> TreUtility.getTreValue(tre, "CG_MGRSCORUL"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_MGRSCORUR_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MGRSCORUR,
          "CG_MGRSCORUR",
          tre -> TreUtility.getTreValue(tre, "CG_MGRSCORUR"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_MGRSCORLR_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MGRSCORLR,
          "CG_MGRSCORLR",
          tre -> TreUtility.getTreValue(tre, "CG_MGRSCORLR"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_MGRCORLL_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MGRCORLL,
          "CG_MGRCORLL",
          tre -> TreUtility.getTreValue(tre, "CG_MGRCORLL"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CG_MGRS_CONFIDENCE_ATTRIBUTE =
      new CmetaaAttribute(
          CG_MGRS_CONFIDENCE,
          "CG_MGRS_CONFIDENCE",
          tre -> TreUtility.convertToFloat(tre, "CG_MGRS_CONFIDENCE"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute CG_SPARE_A_ATTRIBUTE =
      new CmetaaAttribute(
          CG_SPARE_A,
          "CG_SPARE_A",
          tre -> TreUtility.getTreValue(tre, "CG_SPARE_A"),
          BasicTypes.STRING_TYPE);

  public static final CmetaaAttribute CA_CALPA_ATTRIBUTE =
      new CmetaaAttribute(
          CA_CALPA,
          "CA_CALPA",
          tre -> TreUtility.convertToFloat(tre, "CA_CALPA"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_SRTFR_ATTRIBUTE =
      new CmetaaAttribute(
          WF_SRTFR,
          "WF_SRTFR",
          tre -> TreUtility.convertToFloat(tre, "WF_SRTFR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_ENDFR_ATTRIBUTE =
      new CmetaaAttribute(
          WF_ENDFR,
          "WF_ENDFR",
          tre -> TreUtility.convertToFloat(tre, "WF_ENDFR"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_CHRPRT_ATTRIBUTE =
      new CmetaaAttribute(
          WF_CHRPRT,
          "WF_CHRPRT",
          tre -> TreUtility.convertToFloat(tre, "WF_CHRPRT"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_WIDTH_ATTRIBUTE =
      new CmetaaAttribute(
          WF_WIDTH,
          "WF_WIDTH",
          tre -> TreUtility.convertToFloat(tre, "WF_WIDTH"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_CENFRQ_ATTRIBUTE =
      new CmetaaAttribute(
          WF_CENFRQ,
          "WF_CENFRQ",
          tre -> TreUtility.convertToFloat(tre, "WF_CENFRQ"),
          BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_BW_ATTRIBUTE =
      new CmetaaAttribute(
          WF_BW, "WF_BW", tre -> TreUtility.convertToFloat(tre, "WF_BW"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_PRF_ATTRIBUTE =
      new CmetaaAttribute(
          WF_PRF, "WF_PRF", tre -> TreUtility.convertToFloat(tre, "WF_PRF"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_PRI_ATTRIBUTE =
      new CmetaaAttribute(
          WF_PRI, "WF_PRI", tre -> TreUtility.convertToFloat(tre, "WF_PRI"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_CDP_ATTRIBUTE =
      new CmetaaAttribute(
          WF_CDP, "WF_CDP", tre -> TreUtility.convertToFloat(tre, "WF_CDP"), BasicTypes.FLOAT_TYPE);

  public static final CmetaaAttribute WF_NUMBER_OF_PULSES_ATTRIBUTE =
      new CmetaaAttribute(
          WF_NUMBER_OF_PULSES,
          "WF_NUMBER_OF_PULSES",
          tre -> TreUtility.convertToInteger(tre, "WF_NUMBER_OF_PULSES"),
          BasicTypes.INTEGER_TYPE);

  public static final CmetaaAttribute VPH_COND_ATTRIBUTE =
      new CmetaaAttribute(
          VPH_COND,
          "VPH_COND",
          tre -> TreUtility.getTreValue(tre, "VPH_COND"),
          BasicTypes.STRING_TYPE);

  private CmetaaAttribute(
      final String longName,
      final String shortName,
      final Function<Tre, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private CmetaaAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
