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

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Map;
import org.codice.ddf.libs.klv.KlvDataElement;

/**
 * Trim all of the {@link Trimmable} handlers. This is in case one of the values was not supplied by
 * the KLV data. For example the lat value was supplied, but the lon value was {@link
 * KlvDataElement#isErrorIndicated()}.
 */
public class TrimmingPostProcessor implements PostProcessor {
  @Override
  public void postProcess(
      Map<String, KlvDataElement> dataElements, Map<String, KlvHandler> handlers) {
    notNull(handlers, "handlers must be non-null");
    handlers.values().stream()
        .distinct()
        .filter(Trimmable.class::isInstance)
        .map(Trimmable.class::cast)
        .forEach(Trimmable::trim);
  }
}
