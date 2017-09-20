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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreEntry;
import org.codice.imaging.nitf.core.tre.TreGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TreUtility {

  static final String TRE_DATE_FORMAT = "yyyyMMddkkmmss";

  private static final Logger LOGGER = LoggerFactory.getLogger(TreUtility.class);

  private static final FastDateFormat DATE_FORMATTER =
      FastDateFormat.getInstance(TRE_DATE_FORMAT, TimeZone.getTimeZone("GMT"));

  private static final String DIGITS_REGEX = "(\\p{Digit}+)";

  private static final String HEX_DIGITS_REGEX = "(\\p{XDigit}+)";

  private static final String EXP_REGEX = "[eE][+-]?" + DIGITS_REGEX;

  private static final String FLOATING_POINT_REGEX =
      ("[\\x00-\\x20]*[+-]?(NaN|Infinity|((("
          + DIGITS_REGEX
          + "(\\.)?("
          + DIGITS_REGEX
          + "?)("
          + EXP_REGEX
          + ")?)|(\\.("
          + DIGITS_REGEX
          + ")("
          + EXP_REGEX
          + ")?)|(((0[xX]"
          + HEX_DIGITS_REGEX
          + "(\\.)?)|(0[xX]"
          + HEX_DIGITS_REGEX
          + "?(\\.)"
          + HEX_DIGITS_REGEX
          + "))[pP][+-]?"
          + DIGITS_REGEX
          + "))[fFdD]?))[\\x00-\\x20]*");

  private static final Pattern FLOAT_PATTERN = Pattern.compile(FLOATING_POINT_REGEX);

  private static final Pattern INTEGER_PATTERN = Pattern.compile("[+-]?\\d+");

  private TreUtility() {}

  @Nullable
  public static String getTreValue(TreGroup tre, String key) {
    try {
      String value = tre.getFieldValue(key);

      if (value != null) {
        value = value.trim();
      }

      return value;
    } catch (NitfFormatException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public static List<TreGroup> getTreGroups(TreGroup tre, String entry) {
    try {
      TreEntry treEntry = tre.getEntry(entry);
      if (treEntry != null) {
        return treEntry.getGroups();
      }
    } catch (NitfFormatException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public static String convertToString(Tre tre, String fieldName) {
    return TreUtility.getTreValue(tre, fieldName);
  }

  @Nullable
  public static Integer convertToInteger(Tre tre, String fieldName) {
    String value = TreUtility.getTreValue(tre, fieldName);
    if (StringUtils.isNotEmpty(value) && INTEGER_PATTERN.matcher(value).matches()) {
      return Integer.valueOf(value);
    }
    return null;
  }

  public static Optional<Integer> findIntValue(Tre tre, String tagName) {
    try {
      return Optional.of(tre.getIntValue(tagName));
    } catch (NitfFormatException e) {
      LOGGER.debug("failed to find {}", tagName, e);
    }
    return Optional.empty();
  }

  @Nullable
  public static Float convertToFloat(Tre tre, String fieldName) {
    String value = TreUtility.getTreValue(tre, fieldName);
    if (StringUtils.isNotEmpty(value) && FLOAT_PATTERN.matcher(value).matches()) {
      return Float.valueOf(value);
    }
    return null;
  }

  @Nullable
  public static Boolean convertYnToBoolean(Tre tre, String fieldName) {
    String value = TreUtility.getTreValue(tre, fieldName);
    if (value != null) {
      return value.equalsIgnoreCase("Y");
    } else {
      return null;
    }
  }

  @Nullable
  public static Date convertToDate(Tre tre, String fieldName) {
    String value = TreUtility.getTreValue(tre, fieldName);
    if (StringUtils.isNotEmpty(value)) {
      try {
        return DATE_FORMATTER.parse(value);
      } catch (ParseException e) {
        LOGGER.debug("Unable to parse date {} according to format {}", value, TRE_DATE_FORMAT, e);
      }
    }
    return null;
  }
}
