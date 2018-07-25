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
package org.codice.alliance.transformer.nitf;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.codice.ddf.internal.country.converter.api.CountryCodeConverter;
import org.codice.imaging.nitf.core.common.DateTime;

/** General NITF utility functions */
public class NitfAttributeConverters {

  private static CountryCodeConverter countryCodeConverter;

  public NitfAttributeConverters(CountryCodeConverter converter) {
    countryCodeConverter = converter;
  }

  @Nullable
  public static Date nitfDate(@Nullable DateTime nitfDateTime) {
    if (nitfDateTime == null || nitfDateTime.getZonedDateTime() == null) {
      return null;
    }

    ZonedDateTime zonedDateTime = nitfDateTime.getZonedDateTime();
    Instant instant = zonedDateTime.toInstant();

    return Date.from(instant);
  }

  /**
   * Gets the alpha3 country code for a fips country code by delegating to the {@link
   * CountryCodeConverter} service.
   *
   * @param fipsCode FIPS 10-4 country code to convert
   * @return a ISO 3166 Alpha3 country code
   * @throws NitfAttributeTransformException when the fipsCode maps to multiple ISO 3166-1 Alpha3
   *     values
   */
  @Nullable
  public static String fipsToStandardCountryCode(@Nullable String fipsCode)
      throws NitfAttributeTransformException {
    List<String> countryCodes = countryCodeConverter.convertFipsToIso3(fipsCode);

    if (countryCodes.size() > 1) {
      throw new NitfAttributeTransformException(
          String.format(
              "Found %s while converting %s, but expected only 1 conversion value.",
              countryCodes, fipsCode),
          fipsCode);
    }

    if (CollectionUtils.isEmpty(countryCodes)) {
      return null;
    }
    return countryCodes.get(0);
  }
}
