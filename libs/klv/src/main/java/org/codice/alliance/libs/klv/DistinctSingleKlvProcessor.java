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

/**
 * Reduces a list of attribute values returned by the KLV handlers to a list of distinct values,
 * select one of those values (for metacard attributes that only support single values) and add
 * those values to the metacard.
 */
public class DistinctSingleKlvProcessor extends SingleFieldKlvProcessor {

  private final String attributeName;

  protected DistinctSingleKlvProcessor(String attributeName, String stanagFieldName) {
    super(stanagFieldName);
    this.attributeName = attributeName;
  }

  @Override
  protected void doProcess(Attribute attribute, Metacard metacard) {

    attribute.getValues().stream()
        .filter(Utilities::isNotBlankString)
        .findFirst()
        .ifPresent(
            serializable -> {
              safelySetAttribute(metacard, attributeName, serializable);
            });
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
