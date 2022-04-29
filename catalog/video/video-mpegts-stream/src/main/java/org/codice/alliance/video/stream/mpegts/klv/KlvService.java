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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KlvService implements KlvConsumer, KlvStreamService {

  private Cache<String, KlvData> parentToKlv =
      CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();

  @Override
  public void postKlvForStream(KlvData klvData) {
    if (klvData.getProperty(KlvData.PARENT_ID_KEY) != null) {
      parentToKlv.put(klvData.getProperty(KlvData.PARENT_ID_KEY), klvData);
    }
  }

  @Override
  public Collection<KlvData> getStreamsKlvByParam(String key, String value) {
    return parentToKlv.asMap().values().stream()
        .filter(klv -> value.equals(klv.getProperty(key)))
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, KlvData> getAllStreamsKlvByParent() {
    return new HashMap<>(parentToKlv.asMap());
  }

  @Override
  public Collection<KlvData> getAllStreamsKlvWithParam(String key) {
    return parentToKlv.asMap().values().stream()
        .filter(klv -> klv.getProperty(key) != null)
        .collect(Collectors.toList());
  }
}
