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

import java.util.Map;
import org.codice.ddf.libs.klv.KlvDataElement;

public class KlvData {

  public static final String PARENT_ID_KEY = "parentId";

  private Map<String, KlvDataElement> klvMap;

  private Map<String, String> properties;

  public KlvData(Map<String, KlvDataElement> klvMap, Map<String, String> properties) {
    this.klvMap = klvMap;
    this.properties = properties;
  }

  public Map<String, KlvDataElement> getKlvMap() {
    return klvMap;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getProperty(String key) {
    return properties.get(key);
  }
}
