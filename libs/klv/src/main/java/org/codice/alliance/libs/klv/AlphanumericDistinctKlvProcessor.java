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

import java.io.Serializable;

public class AlphanumericDistinctKlvProcessor extends DistinctKlvProcessor {

  public AlphanumericDistinctKlvProcessor(String attributeName, String stanagFieldName) {
    super(attributeName, stanagFieldName);
  }

  @Override
  protected boolean isValidAttributeValue(Serializable value) {
    return Utilities.isNotBlankString(value)
        && Utilities.isAlphanumericString(value)
        && Utilities.isNotStringNone(value);
  }
}
