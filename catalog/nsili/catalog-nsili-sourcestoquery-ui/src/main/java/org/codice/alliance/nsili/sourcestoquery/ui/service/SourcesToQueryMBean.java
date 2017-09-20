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
package org.codice.alliance.nsili.sourcestoquery.ui.service;

import java.util.Map;
import java.util.Set;

public interface SourcesToQueryMBean {
  /**
   * Returns mapped list of queried sources from the NSILI endpoint.
   *
   * @return mapped list of {@link org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources} in
   *     the format [{{@link
   *     org.codice.alliance.nsili.sourcestoquery.ui.service.SourcesToQuery#SOURCE_ID_TITLE}=
   *     sourceId1, ...]. Returns an empty map is there are no queried sources.
   */
  Set<Map<String, String>> queriedSources();

  /**
   * Returns mapped list of queryable sources that have not yet been added to querySources on the
   * NSILI Endpoint.
   *
   * @return mapped list of queried sources from {@link
   *     org.codice.alliance.nsili.endpoint.NsiliEndpoint} in the format [{{@link
   *     org.codice.alliance.nsili.sourcestoquery.ui.service.SourcesToQuery#SOURCE_ID_TITLE}=
   *     sourceId1, ...] that aren't already included in queriedSources(). Returns an empty map if
   *     there are no queried sources.
   */
  Set<Map<String, String>> queryableSources();

  /**
   * Adds a sourceId to the NSILI Endpoint querySources set.
   *
   * @param sourceId to add to {@link
   *     org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources}. sourceId may be null.
   *     {@link CatalogFramework#getSourceIds()} should contain sourceId.
   */
  void addQueriedSource(String sourceId);

  /**
   * Removes a sourceId from the NSILI Endpoint querySources set.
   *
   * @param sourceId to remove from {@link
   *     org.codice.alliance.nsili.endpoint.NsiliEndpoint#querySources}. sourceId may be null.
   */
  void removeQueriedSource(String sourceId);
}
