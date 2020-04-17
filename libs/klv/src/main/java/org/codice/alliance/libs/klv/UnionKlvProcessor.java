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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Union the values of multiple stanag fields into a single metacard attribute. Filter out empty
 * strings.
 */
public class UnionKlvProcessor extends AbstractMultiKlvProcessor {

  private final String attributeName;

  public UnionKlvProcessor(Set<String> stanagFieldNames, String attributeName) {
    super(stanagFieldNames);
    this.attributeName = attributeName;
  }

  @Override
  protected final void doProcess(List<Attribute> attributes, Metacard metacard) {
    List<Serializable> serializables =
        attributes.stream()
            .filter(a -> a.getValues() != null)
            .flatMap(a -> a.getValues().stream())
            .filter(Utilities::isNotBlankString)
            .distinct()
            .collect(Collectors.toList());
    if (!serializables.isEmpty()) {
      safelySetAttribute(metacard, attributeName, serializables);
    }
  }

  @Override
  public final void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
