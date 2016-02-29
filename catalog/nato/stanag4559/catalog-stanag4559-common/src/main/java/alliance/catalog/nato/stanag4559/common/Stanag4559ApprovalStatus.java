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
package alliance.catalog.nato.stanag4559.common;

import java.util.HashMap;
import java.util.Map;

public enum Stanag4559ApprovalStatus {
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    NOT_APPLICABLE("NOT APPLICABLE");

    private static final Map<String, Stanag4559ApprovalStatus> specNameMap = new HashMap<>();
    static {
        for (Stanag4559ApprovalStatus status:Stanag4559ApprovalStatus.values()) {
            specNameMap.put(status.getSpecName(), status);
        }
    }

    private String specName;

    Stanag4559ApprovalStatus(String specName) {
        this.specName = specName;
    }

    public String getSpecName() {
        return specName;
    }

    public static Stanag4559ApprovalStatus fromSpecName(String specName) {
        return specNameMap.get(specName);
    }
}
