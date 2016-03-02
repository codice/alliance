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
package org.codice.alliance.nato.stanag4559.common;

import java.util.HashMap;
import java.util.Map;

public enum Stanag4559VideoEncodingScheme {
    V264ON2("264ON2"),
    MPEG2("MPEG-2");

    private static final Map<String, Stanag4559VideoEncodingScheme> encodingBySpec = new HashMap<>();
    static {
        for (Stanag4559VideoEncodingScheme encodingScheme:Stanag4559VideoEncodingScheme.values()) {
            encodingBySpec.put(encodingScheme.getSpecName(), encodingScheme);
        }
    }

    private String specName;

    private Stanag4559VideoEncodingScheme(String specName) {
        this.specName = specName;
    }

    public String getSpecName() {
        return specName;
    }

    public static Stanag4559VideoEncodingScheme fromSpecName(String specName) {
        return encodingBySpec.get(specName);
    }
}
