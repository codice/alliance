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
package org.codice.alliance.nsili.endpoint;

import ddf.catalog.CatalogFramework;
import java.util.Set;

/**
 * Accesses and modifies {@link org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources}. Used
 * by {@link org.codice.alliance.nsili.sourcestoquery.ui.service.SourcesToQuery}.
 */
public interface QuerySources {
  /**
   * Returns a list of source ids from which to perform a federated query. Empty list defaults to
   * the local source only. The {@link CatalogFramework#getSourceIds()} should contain all source
   * ids in this set.
   *
   * @return {@link org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources}. Returns an empty
   *     Set<String> when no querySources have been added.
   */
  Set<String> getQuerySources();

  /**
   * Adds the sourceId to {@link org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources}.
   *
   * @param sourceId to add to the querySources list. sourceId may be null. {@link
   *     CatalogFramework#getSourceIds()} must contain the sourceId to be added.
   */
  void addQuerySource(String sourceId);

  /**
   * Removed sourceId from {@link org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources}
   *
   * @param sourceId to remove from the querySources list. sourceId may be null.
   */
  void removeQuerySource(String sourceId);
}
