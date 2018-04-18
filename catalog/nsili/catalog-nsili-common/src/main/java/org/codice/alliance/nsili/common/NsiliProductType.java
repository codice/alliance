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

public enum NsiliProductType {
  CBRN("CBRN"),
  COLLECTION_EXPLOITATION_PLAN("COLLECTION/EXPLOITATION PLAN"),
  DOCUMENT("DOCUMENT"),
  EOB("ELECTRONIC ORDER OF BATTLE"),
  GEOGRAPHIC_AOI("GEOGRAPHIC AREA OF INTEREST"),
  GMTI("GMTI"),
  IMAGERY("IMAGERY"),
  INTELLIGENCE_REQUIREMENT("INTELLIGENCE REQUIREMENT"),
  MESSAGE("MESSAGE"),
  OPERATIONAL_ROLES("OPERATIONAL ROLES"),
  ORBAT("ORBAT"),
  REPORT("REPORT"),
  RFI("RFI"),
  SYSTEM_ASSIGNMENTS("SYSTEM ASSIGNMENTS"),
  SYSTEM_DEPLOYMENT_STATUS("SYSTEM DEPLOYMENT STATUS"),
  SYSTEM_SPEC("SYSTEM SPECIFICATIONS"),
  TACTICAL_SYMBOL("TACTICAL SYMBOL"),
  TASK("TASK"),
  TDL_DATA("TDL DATA"),
  VIDEO("VIDEO");

  private static final Map<String, NsiliProductType> specNameMap = new HashMap<>();

  static {
    for (NsiliProductType productType : NsiliProductType.values()) {
      specNameMap.put(productType.getSpecName(), productType);
    }
  }

  private String specName;

  private NsiliProductType(String specName) {
    this.specName = specName;
  }

  public static NsiliProductType fromSpecName(String specName) {
    return specNameMap.get(specName);
  }

  public String getSpecName() {
    return specName;
  }
}
