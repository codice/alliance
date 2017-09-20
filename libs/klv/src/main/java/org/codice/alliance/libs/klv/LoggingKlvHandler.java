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
import java.util.Optional;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingKlvHandler implements KlvHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingKlvHandler.class);

  @Override
  public String getAttributeName() {
    return null;
  }

  @Override
  public Optional<Attribute> asAttribute() {
    return Optional.empty();
  }

  @Override
  public void accept(KlvDataElement klvDataElement) {
    LOGGER.debug(
        "unhandled klv data element: name = {} value ={}",
        klvDataElement.getName(),
        klvDataElement);
  }

  @Override
  public void reset() {}
}
