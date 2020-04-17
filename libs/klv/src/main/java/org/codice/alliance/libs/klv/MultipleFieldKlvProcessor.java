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
import java.util.stream.Collectors;

public abstract class MultipleFieldKlvProcessor implements KlvProcessor {

  private final List<String> stanagFieldNames;

  public MultipleFieldKlvProcessor(List<String> stanagFieldNames) {
    this.stanagFieldNames = stanagFieldNames;
  }

  @Override
  public final void process(
      Map<String, KlvHandler> handlers, Metacard metacard, Configuration configuration) {

    List<KlvHandler> stanagHandlers = findKlvHandlers(handlers);

    if (areAllHandlersFound(stanagHandlers)) {
      callFirstHandler(metacard, stanagHandlers);
    }
  }

  private void callFirstHandler(Metacard metacard, List<KlvHandler> stanagHandlers) {
    stanagHandlers.stream()
        .findFirst()
        .ifPresent(
            handler ->
                handler.asAttribute().ifPresent(attribute -> doProcess(attribute, metacard)));
  }

  private boolean areAllHandlersFound(List<KlvHandler> stanagHandlers) {
    return stanagHandlers.size() == stanagFieldNames.size();
  }

  private List<KlvHandler> findKlvHandlers(Map<String, KlvHandler> handlers) {
    return handlers.entrySet().stream()
        .filter(entry -> stanagFieldNames.contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
  }

  protected abstract void doProcess(Attribute attribute, Metacard metacard);
}
