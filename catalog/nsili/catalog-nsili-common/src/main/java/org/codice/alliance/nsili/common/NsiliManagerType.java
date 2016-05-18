/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.common;

import java.util.HashMap;
import java.util.Map;

public enum NsiliManagerType {
    /* Required */
    ORDER_MGR("OrderMgr"),
    CATALOG_MGR("CatalogMgr"),
    PRODUCT_MGR("ProductMgr"),
    DATA_MODEL_MGR("DataModelMgr"),
    /* Optional */
    QUERY_ORDER_MGR("QueryOrderMgr"),
    STANDING_QUERY_MGR("StandingQueryMgr"),
    CREATION_MGR("CreationMgr"),
    UPDATE_MGR("UpdateMgr");

    private String specName;
    private static final Map<String, NsiliManagerType> typeBySpecName = new HashMap<>();
    static {
        for (NsiliManagerType managerType: NsiliManagerType.values()) {
            typeBySpecName.put(managerType.getSpecName(), managerType);
        }
    }

    NsiliManagerType(String specName) {
        this.specName = specName;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public static NsiliManagerType fromSpecName(String specName) {
        return typeBySpecName.get(specName);
    }
}
