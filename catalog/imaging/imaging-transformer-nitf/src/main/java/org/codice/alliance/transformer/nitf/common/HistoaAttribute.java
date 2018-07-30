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

import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.BasicTypes;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;

/** TRE for "Softcopy History Tagged Record Extension" */
public class HistoaAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  public static final String SYSTYPE_NAME = "SYSTYPE";

  public static final String PRIOR_COMPRESSION_NAME = "PC";

  public static final String PRIOR_ENHANCEMENTS_NAME = "PE";

  public static final String REMAP_FLAG_NAME = "REMAP_FLAG";

  public static final String LUTID_NAME = "LUTID";

  public static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "histoa.";

  public static final String SYSTYPE = PREFIX + "system-type";

  public static final String PC = PREFIX + "prior-compression";

  public static final String PE = PREFIX + "prior-enhancements";

  public static final String REMAP_FLAG = PREFIX + "system-specific-remap";

  public static final String LUTID = PREFIX + "data-mapping-id";

  /*
   * Non-normalized attributes
   */

  static final HistoaAttribute SYSTYPE_ATTRIBUTE =
      new HistoaAttribute(
          SYSTYPE,
          SYSTYPE_NAME,
          tre -> TreUtility.convertToString(tre, SYSTYPE_NAME),
          BasicTypes.STRING_TYPE);

  static final HistoaAttribute PC_ATTRIBUTE =
      new HistoaAttribute(
          PC,
          PRIOR_COMPRESSION_NAME,
          tre -> TreUtility.convertToString(tre, PRIOR_COMPRESSION_NAME),
          BasicTypes.STRING_TYPE);

  static final HistoaAttribute PE_ATTRIBUTE =
      new HistoaAttribute(
          PE,
          PRIOR_ENHANCEMENTS_NAME,
          tre -> TreUtility.convertToString(tre, PRIOR_ENHANCEMENTS_NAME),
          BasicTypes.STRING_TYPE);

  static final HistoaAttribute REMAP_FLAG_ATTRIBUTE =
      new HistoaAttribute(
          REMAP_FLAG,
          REMAP_FLAG_NAME,
          tre -> TreUtility.convertToString(tre, REMAP_FLAG_NAME),
          BasicTypes.STRING_TYPE);

  static final HistoaAttribute LUTID_ATTRIBUTE =
      new HistoaAttribute(
          LUTID,
          LUTID_NAME,
          tre -> TreUtility.convertToInteger(tre, LUTID_NAME),
          BasicTypes.INTEGER_TYPE);

  private HistoaAttribute(
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
