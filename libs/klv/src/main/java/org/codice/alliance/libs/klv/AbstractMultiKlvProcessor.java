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
import ddf.catalog.data.Metacard;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractMultiKlvProcessor implements KlvProcessor {

  private final Set<String> stanagFieldNames;

  public AbstractMultiKlvProcessor(Set<String> stanagFieldNames) {
    this.stanagFieldNames = stanagFieldNames;
  }

  @Override
  public void process(
      Map<String, KlvHandler> handlers, Metacard metacard, Configuration configuration) {
    List<Attribute> attributes =
        findKlvHandlers(handlers).stream()
            .map(KlvHandler::asAttribute)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    doProcess(attributes, metacard);
  }

  protected abstract void doProcess(List<Attribute> attributes, Metacard metacard);

  private List<KlvHandler> findKlvHandlers(Map<String, KlvHandler> handlers) {
    return handlers.entrySet().stream()
        .filter(entry -> stanagFieldNames.contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .distinct()
        .collect(Collectors.toList());
  }
}
