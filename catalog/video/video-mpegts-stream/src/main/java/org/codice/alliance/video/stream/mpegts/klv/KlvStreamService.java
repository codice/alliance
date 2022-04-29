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
package org.codice.alliance.video.stream.mpegts.klv;

import java.util.Collection;
import java.util.Map;

public interface KlvStreamService {
  /**
   * Get the current klv data mappings associated with the given parent id.
   *
   * @param key the klv properties key
   * @param value the klv property value to filter on
   * @return The current klv data the streams with matching key/value. Will return an empty
   *     collection if no klv has been reported recently for the parameters.
   */
  Collection<KlvData> getStreamsKlvByParam(String key, String value);

  /**
   * Get the current klv data mappings organized by parent stream id.
   *
   * @return The current klv data map for the stream. Will return an empty map if no klv has been
   *     reported recently for any streams.
   */
  Map<String, KlvData> getAllStreamsKlvByParent();

  /**
   * Get the collection of all active streams with klv data that have the given key defined in their
   * properties.
   *
   * @return A map of klv data maps by the passed in key
   */
  Collection<KlvData> getAllStreamsKlvWithParam(String key);
}
