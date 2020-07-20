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

import static org.codice.alliance.libs.klv.Utilities.safelySetAttribute;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reduces a list of attribute values returned by the KLV handlers to a list of distinct values and
 * adds those values to the metacard.
 */
public class DistinctKlvProcessor extends SingleFieldKlvProcessor {

  private final String attributeName;

  protected DistinctKlvProcessor(String attributeName, String stanagFieldName) {
    super(stanagFieldName);
    this.attributeName = attributeName;
  }

  @Override
  protected void doProcess(Attribute attribute, Metacard metacard) {

    List<Serializable> serializables =
        attribute.getValues().stream()
            .filter(this::isValidAttributeValue)
            .distinct()
            .collect(Collectors.toList());

    if (!serializables.isEmpty()) {
      safelySetAttribute(metacard, attributeName, serializables);
    }
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  protected boolean isValidAttributeValue(Serializable value) {
    return Utilities.isNotBlankString(value);
  }
}
