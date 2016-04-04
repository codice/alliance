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

import java.util.Map;

import org.codice.ddf.libs.klv.KlvDataElement;

/**
 * The PostProcessor is called after each klv metadata packet is processed. The primary purpose is
 * to inject new metadata that is calculated from other metadata fields in the same klv metadata
 * packet.
 */
public interface PostProcessor {

    /**
     * A typical implementation would search dataElements for specific instances, calculate a new
     * value, and then call the KlvHandler in handlers that corresponds to the new data.
     *
     * @param dataElements map of klv data element names to klv data elements that were retrieved from the current klv metadata packet
     * @param handlers     map of klv data element names to the handlers that process the klv data elements
     */
    void postProcess(Map<String, KlvDataElement> dataElements, Map<String, KlvHandler> handlers);

}
