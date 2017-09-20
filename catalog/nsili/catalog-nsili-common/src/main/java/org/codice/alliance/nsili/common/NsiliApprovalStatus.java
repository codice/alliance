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

public enum NsiliApprovalStatus {
  APPROVED("APPROVED"),
  REJECTED("REJECTED"),
  NOT_APPLICABLE("NOT APPLICABLE");

  private static final Map<String, NsiliApprovalStatus> specNameMap = new HashMap<>();

  static {
    for (NsiliApprovalStatus status : NsiliApprovalStatus.values()) {
      specNameMap.put(status.getSpecName(), status);
    }
  }

  private String specName;

  NsiliApprovalStatus(String specName) {
    this.specName = specName;
  }

  public String getSpecName() {
    return specName;
  }

  public static NsiliApprovalStatus fromSpecName(String specName) {
    return specNameMap.get(specName);
  }
}
