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
package org.codice.alliance.nsili.common;

import java.util.HashMap;
import java.util.Map;

public enum NsiliClassification {
  COSMIC_TOP_SECRET("COSMIC TOP SECRET", 4),
  SECRET("SECRET", 3),
  CONFIDENTIAL("CONFIDENTIAL", 2),
  RESTRICTED("RESTRICTED", 1),
  UNCLASSIFIED("UNCLASSIFIED", 0),
  NO_CLASSIFICATION("NO CLASSIFICATION", -1);

  private static final Map<String, NsiliClassification> specNameMap = new HashMap<>();

  static {
    for (NsiliClassification classification : NsiliClassification.values()) {
      specNameMap.put(classification.getSpecName(), classification);
    }
  }

  private String specName;
  private int classificationRank;

  NsiliClassification(String specName, int classificationRank) {
    this.specName = specName;
    this.classificationRank = classificationRank;
  }

  public String getSpecName() {
    return specName;
  }

  public int getClassificationRank() {
    return classificationRank;
  }

  public static NsiliClassification fromSpecName(String specName) {
    return specNameMap.get(specName);
  }
}
