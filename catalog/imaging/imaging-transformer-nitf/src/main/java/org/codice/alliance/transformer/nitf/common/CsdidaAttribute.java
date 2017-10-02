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
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.imaging.nitf.core.tre.Tre;

/** TRE for "Dataset Identification" */
public class CsdidaAttribute extends NitfAttributeImpl<Tre> {

  private static final List<NitfAttribute<Tre>> ATTRIBUTES = new LinkedList<>();

  private static final String PREFIX = ExtNitfUtility.EXT_NITF_PREFIX + "csdida.";

  public static final String PLATFORM_ID = PREFIX + "platform-code-vehicle-id";

  public static final String DAY = PREFIX + "day-dataset-collection";

  public static final String MONTH = PREFIX + "month-dataset-collection";

  public static final String YEAR = PREFIX + "year-dataset-collection";

  public static final String PASS = PREFIX + "pass-num";

  public static final String OPERATION = PREFIX + "operation-num";

  public static final String SENSOR_ID = PREFIX + "sensor-id";

  public static final String PRODUCT_ID = PREFIX + "product-id";

  public static final String TIME = PREFIX + "image-start-time";

  public static final String PROCESS_TIME = PREFIX + "process-completion-time";

  public static final String SOFTWARE_VERSION_NUMBER = PREFIX + "software-version-num";

  public static final String PLATFORM_CODE_SHORT_NAME = "PLATFORM_CODE";

  public static final String VEHICLE_ID_SHORT_NAME = "VEHICLE_ID";

  public static final String DAY_SHORT_NAME = "DAY";

  public static final String MONTH_SHORT_NAME = "MONTH";

  public static final String YEAR_SHORT_NAME = "YEAR";

  public static final String PASS_SHORT_NAME = "PASS";

  public static final String OPERATION_SHORT_NAME = "OPERATION";

  public static final String SENSOR_ID_SHORT_NAME = "SENSOR_ID";

  public static final String PRODUCT_ID_SHORT_NAME = "PRODUCT_ID";

  public static final String TIME_SHORT_NAME = "TIME";

  public static final String PROCESS_TIME_SHORT_NAME = "PROCESS_TIME";

  public static final String SOFTWARE_VERSION_NUMBER_SHORT_NAME = "SOFTWARE_VERSION_NUMBER";

  /*
   * Normalized attributes. These taxonomy terms will be duplicated by `ext.nitf.csdida.*` when appropriate.
   */

  public static final CsdidaAttribute PLATFORM_ID_ATTRIBUTE =
      new CsdidaAttribute(
          Isr.PLATFORM_ID,
          "PLATFORM_CODE_VEHICLE_ID",
          CsdidaAttribute::getPlatformIdFunction,
          new IsrAttributes().getAttributeDescriptor(Isr.PLATFORM_ID),
          PLATFORM_ID);

  /*
   * Non-normalized attributes
   */

  static final CsdidaAttribute DAY_ATTRIBUTE =
      new CsdidaAttribute(
          DAY,
          DAY_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, DAY_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsdidaAttribute MONTH_ATTRIBUTE =
      new CsdidaAttribute(
          MONTH,
          MONTH_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, MONTH_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsdidaAttribute YEAR_ATTRIBUTE =
      new CsdidaAttribute(
          YEAR,
          YEAR_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, YEAR_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsdidaAttribute PASS_ATTRIBUTE =
      new CsdidaAttribute(
          PASS,
          PASS_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, PASS_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsdidaAttribute OPERATION_ATTRIBUTE =
      new CsdidaAttribute(
          OPERATION,
          OPERATION_SHORT_NAME,
          tre -> TreUtility.convertToInteger(tre, OPERATION_SHORT_NAME),
          BasicTypes.INTEGER_TYPE);

  static final CsdidaAttribute SENSOR_ID_ATTRIBUTE =
      new CsdidaAttribute(
          SENSOR_ID,
          SENSOR_ID_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SENSOR_ID_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsdidaAttribute PRODUCT_ID_ATTRIBUTE =
      new CsdidaAttribute(
          PRODUCT_ID,
          PRODUCT_ID_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, PRODUCT_ID_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  static final CsdidaAttribute TIME_ATTRIBUTE =
      new CsdidaAttribute(
          TIME,
          TIME_SHORT_NAME,
          tre -> TreUtility.convertToDate(tre, TIME_SHORT_NAME),
          BasicTypes.DATE_TYPE);

  static final CsdidaAttribute PROCESS_TIME_ATTRIBUTE =
      new CsdidaAttribute(
          PROCESS_TIME,
          PROCESS_TIME_SHORT_NAME,
          tre -> TreUtility.convertToDate(tre, PROCESS_TIME_SHORT_NAME),
          BasicTypes.DATE_TYPE);

  static final CsdidaAttribute SOFTWARE_VERSION_NUMBER_ATTRIBUTE =
      new CsdidaAttribute(
          SOFTWARE_VERSION_NUMBER,
          SOFTWARE_VERSION_NUMBER_SHORT_NAME,
          tre -> TreUtility.convertToString(tre, SOFTWARE_VERSION_NUMBER_SHORT_NAME),
          BasicTypes.STRING_TYPE);

  private CsdidaAttribute(
      String longName,
      String shortName,
      Function<Tre, Serializable> accessorFunction,
      AttributeType attributeType) {
    super(longName, shortName, accessorFunction, attributeType);
    ATTRIBUTES.add(this);
  }

  private CsdidaAttribute(
      final String longName,
      final String shortName,
      final Function<Tre, Serializable> accessorFunction,
      AttributeDescriptor attributeDescriptor,
      String extNitfName) {
    super(longName, shortName, accessorFunction, attributeDescriptor, extNitfName);
    ATTRIBUTES.add(this);
  }

  private static Serializable getPlatformIdFunction(Tre tre) {
    Optional<String> platformCode =
        Optional.ofNullable(TreUtility.getTreValue(tre, PLATFORM_CODE_SHORT_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);
    Optional<Serializable> vehicleId =
        Optional.ofNullable(TreUtility.getTreValue(tre, VEHICLE_ID_SHORT_NAME));

    if (platformCode.isPresent() && vehicleId.isPresent()) {
      return String.format("%s%s", platformCode.get(), vehicleId.get());
    }

    return null;
  }

  public static List<NitfAttribute<Tre>> getAttributes() {
    return Collections.unmodifiableList(ATTRIBUTES);
  }
}
