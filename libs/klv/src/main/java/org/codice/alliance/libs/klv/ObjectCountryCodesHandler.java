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
package org.codice.alliance.libs.klv;

import ddf.catalog.data.Attribute;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.raw.KlvBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler expects a KlvBytes object which should hold the country code value. It detects the
 * country code charset encoding and generates the string using the proper encoding.
 */
public class ObjectCountryCodesHandler extends BaseKlvHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectCountryCodesHandler.class);

  private String countryCode = "";

  public ObjectCountryCodesHandler(String attributeName) {
    super(attributeName);
  }

  @Override
  public Optional<Attribute> asAttribute() {
    return asAttribute(Collections.singletonList(countryCode));
  }

  @Override
  public void accept(KlvDataElement klvDataElement) {
    if (!(klvDataElement instanceof KlvBytes)) {
      LOGGER.debug(
          "non-KlvString data was passed to the ObjectCountryCodesHandler: name = {} klvDataElement = {}",
          klvDataElement.getName(),
          klvDataElement);
      return;
    }

    String elementValue;
    byte[] bytes = ((KlvBytes) klvDataElement).getValue();

    CharsetDetector charsetDetector = new CharsetDetector();
    charsetDetector.setText(bytes);
    try {
      CharsetMatch charsetMatch = charsetDetector.detect();
      elementValue = new String(bytes, charsetMatch.getName());
    } catch (IOException e) {
      LOGGER.trace(
          "Unable to detect encoding for Object Country Codes, Country Code value may be malformed");
      elementValue = new String(bytes);
    }

    countryCode = elementValue;
  }

  @Override
  public void reset() {
    countryCode = "";
  }
}
