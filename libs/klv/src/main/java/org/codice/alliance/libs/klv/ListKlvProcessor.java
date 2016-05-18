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
package org.codice.alliance.libs.klv;

import java.util.List;
import java.util.Map;

import ddf.catalog.data.Metacard;

/**
 * This {@link KlvProcessor} delegates to each KlvProcessor in a list of processors.
 */
public class ListKlvProcessor implements KlvProcessor {

    private final List<KlvProcessor> klvProcessorList;

    public ListKlvProcessor(List<KlvProcessor> klvProcessorList) {
        this.klvProcessorList = klvProcessorList;
    }

    @Override
    public void process(Map<String, KlvHandler> handlers, Metacard metacard,
            Configuration configuration) {
        klvProcessorList.forEach(klvProcessor -> klvProcessor.process(handlers,
                metacard,
                configuration));
    }

}
