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

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import java.util.Objects;
import java.util.function.Predicate;

public class OverviewPredicate implements Predicate<Metacard> {

  private final Predicate<String> isDerivedResourceOverviewUri =
      uri -> uri.startsWith(ContentItem.CONTENT_SCHEME) && uri.endsWith("#overview");

  @Override
  public boolean test(Metacard metacard) {
    final Attribute derivedResourceUris = metacard.getAttribute(Core.DERIVED_RESOURCE_URI);

    if (derivedResourceUris != null) {
      return derivedResourceUris.getValues().stream()
          .map(Objects::toString)
          .anyMatch(isDerivedResourceOverviewUri);
    }

    return false;
  }
}
