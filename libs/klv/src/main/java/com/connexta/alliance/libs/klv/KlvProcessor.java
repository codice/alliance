/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.libs.klv;

import java.util.HashMap;
import java.util.Map;

import ddf.catalog.data.Metacard;

/**
 * A KlvProcessor is used to extract data from KlvHandlers and insert that data into a metacard.
 */
public interface KlvProcessor {

    void process(Map<String, KlvHandler> handlers, Metacard metacard, Configuration configuration);

    class Configuration {
        public static final String SUBSAMPLE_COUNT = "subsample-count";

        private Map<String, Object> configuration = new HashMap<>();

        public void set(String name, Object value) {
            configuration.put(name, value);
        }

        public Object get(String name) {
            return configuration.get(name);
        }
    }

}
