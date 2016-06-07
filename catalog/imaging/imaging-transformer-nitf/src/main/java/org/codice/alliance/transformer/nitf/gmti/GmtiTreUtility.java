/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.gmti;

import java.io.Serializable;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.TreGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GmtiTreUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(GmtiTreUtility.class);

    private GmtiTreUtility() {
    }

    public static Serializable getTreValue(TreGroup tre, String key) {
        try {
            String value = tre.getFieldValue(key);

            if (value != null) {
                value = value.trim();
            }

            return value;
        } catch (NitfFormatException e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return null;
    }
}
