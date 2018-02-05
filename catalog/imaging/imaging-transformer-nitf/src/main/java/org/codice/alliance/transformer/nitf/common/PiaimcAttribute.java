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

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;

/** TRE for "Profile for Imagery Access Image" */
public class PiaimcAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "piaimc.";

  public static final String CLOUDCVR = PREFIX + "cloud-cvr";

  public static final String SRP = PREFIX + "standard-radiometric-product";

  public static final String SENSMODE = PREFIX + "sensor-mode";

  public static final String SENSNAME = PREFIX + "sensor-name";

  public static final String SOURCE = PREFIX + "source";

  public static final String COMGEN = PREFIX + "compression-generation";

  public static final String SUBQUAL = PREFIX + "subjective-quality";

  public static final String PIAMSNNUM = PREFIX + "pia-mission-num";

  public static final String CAMSPECS = PREFIX + "camera-specs";

  public static final String PROJID = PREFIX + "project-id-code";

  public static final String GENERATION = PREFIX + "generation";

  public static final String ESD = PREFIX + "exploitation-support-data";

  public static final String OTHERCOND = PREFIX + "other-conditions";

  public static final String MEANGSD = PREFIX + "mean-gsd";

  public static final String IDATUM = PREFIX + "image-datum";

  public static final String IELLIP = PREFIX + "image-ellipsoid";

  public static final String PREPROC = PREFIX + "image-processing-level";

  public static final String IPROJ = PREFIX + "image-projection-system";

  public static final String SATTRACK_PATH = PREFIX + "satellite-track-path";

  public static final String SATTRACK_ROW = PREFIX + "satellite-track-row";

  public static final String CLOUDCVR_SHORT_NAME = "CLOUDCVR";

  public static final String STANDARD_RADIOMETRIC_PRODUCT_SHORT_NAME = "SRP";

  public static final String SENSMODE_SHORT_NAME = "SENSMODE";

  public static final String SENSNAME_SHORT_NAME = "SENSNAME";

  public static final String SOURCE_SHORT_NAME = "SOURCE";

  public static final String COMGEN_SHORT_NAME = "COMGEN";

  public static final String SUBQUAL_SHORT_NAME = "SUBQUAL";

  public static final String PIAMSNNUM_SHORT_NAME = "PIAMSNNUM";

  public static final String CAMSPECS_SHORT_NAME = "CAMSPECS";

  public static final String PROJID_SHORT_NAME = "PROJID";

  public static final String GENERATION_SHORT_NAME = "GENERATION";

  public static final String EXPLOITATION_SUPPORT_DATA_SHORT_NAME = "ESD";

  public static final String OTHERCOND_SHORT_NAME = "OTHERCOND";

  public static final String MEANGSD_SHORT_NAME = "MEANGSD";

  public static final String IDATUM_SHORT_NAME = "IDATUM";

  public static final String IELLIP_SHORT_NAME = "IELLIP";

  public static final String PREPROC_SHORT_NAME = "PREPROC";

  public static final String IPROJ_SHORT_NAME = "IPROJ";

  public static final String SATTRACK_PATH_SHORT_NAME = "SATTRACK_PATH";

  public static final String SATTRACK_ROW_SHORT_NAME = "SATTRACK_ROW";

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.piaimc.*` when appropriate.
   */

  static final PiaimcAttribute CLOUDCVR_ATTRIBUTE =
      new PiaimcAttribute(
          Isr.CLOUD_COVER,
          CLOUDCVR_SHORT_NAME,
          PiaimcAttribute::getCloudCoverFunction,
          new IsrAttributes().getAttributeDescriptor(Isr.CLOUD_COVER),
          CLOUDCVR);

  /*
   * Non-normalized attributes
   */

  static final PiaimcAttribute SRP_ATTRIBUTE =
      new PiaimcAttribute(
          SRP,
          STANDARD_RADIOMETRIC_PRODUCT_SHORT_NAME,
          tre -> TreUtility.convertYnToBoolean(tre, STANDARD_RADIOMETRIC_PRODUCT_SHORT_NAME),
          BasicTypes.BOOLEAN_TYPE);

  static final PiaimcAttribute SENSMODE_ATTRIBUTE =
      new PiaimcAttribute(
          SENSMODE,
          SENSMODE_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SENSMODE_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute SENSNAME_ATTRIBUTE =
      new PiaimcAttribute(
          SENSNAME,
          SENSNAME_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SENSNAME_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute SOURCE_ATTRIBUTE =
      new PiaimcAttribute(
          SOURCE,
          SOURCE_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SOURCE_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute COMGEN_ATTRIBUTE =
      new PiaimcAttribute(
          COMGEN,
          COMGEN_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, COMGEN_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final PiaimcAttribute SUBQUAL_ATTRIBUTE =
      new PiaimcAttribute(
          SUBQUAL,
          SUBQUAL_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SUBQUAL_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute PIAMSNNUM_ATTRIBUTE =
      new PiaimcAttribute(
          PIAMSNNUM,
          PIAMSNNUM_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, PIAMSNNUM_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute CAMSPECS_ATTRIBUTE =
      new PiaimcAttribute(
          CAMSPECS,
          CAMSPECS_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, CAMSPECS_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute PROJID_ATTRIBUTE =
      new PiaimcAttribute(
          PROJID,
          PROJID_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, PROJID_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute GENERATION_ATTRIBUTE =
      new PiaimcAttribute(
          GENERATION,
          GENERATION_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, GENERATION_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final PiaimcAttribute ESD_ATTRIBUTE =
      new PiaimcAttribute(
          ESD,
          EXPLOITATION_SUPPORT_DATA_SHORT_NAME,
          tre -> TreUtility.convertYnToBoolean(tre, EXPLOITATION_SUPPORT_DATA_SHORT_NAME),
          BasicTypes.BOOLEAN_TYPE);

  static final PiaimcAttribute OTHERCOND_ATTRIBUTE =
      new PiaimcAttribute(
          OTHERCOND,
          OTHERCOND_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, OTHERCOND_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute MEANGSD_ATTRIBUTE =
      new PiaimcAttribute(
          MEANGSD,
          MEANGSD_SHORT_NAME,
          tre -> TreUtility.convertToFloat(tre, MEANGSD_SHORT_NAME),
          BasicTypes.FLOAT_TYPE);

  static final PiaimcAttribute IDATUM_ATTRIBUTE =
      new PiaimcAttribute(
          IDATUM,
          IDATUM_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, IDATUM_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute IELLIP_ATTRIBUTE =
      new PiaimcAttribute(
          IELLIP,
          IELLIP_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, IELLIP_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute PREPROC_ATTRIBUTE =
      new PiaimcAttribute(
          PREPROC,
          PREPROC_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, PREPROC_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute IPROJ_ATTRIBUTE =
      new PiaimcAttribute(
          IPROJ,
          IPROJ_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, IPROJ_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final PiaimcAttribute SATTRACK_PATH_ATTRIBUTE =
      new PiaimcAttribute(
          SATTRACK_PATH,
          SATTRACK_PATH_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, SATTRACK_PATH_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final PiaimcAttribute SATTRACK_ROW_ATTRIBUTE =
      new PiaimcAttribute(
          SATTRACK_ROW,
          SATTRACK_ROW_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, SATTRACK_ROW_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  private PiaimcAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private PiaimcAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private static Serializable getCloudCoverFunction(Tre tre) {
    return Optional.ofNullable(TreUtility.getTreValue(tre, CLOUDCVR_SHORT_NAME))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .filter(StringUtils::isNotEmpty)
        .map(Double::valueOf)
        .filter(value -> value >= 0 && value <= 100)
        .orElse(null);
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
