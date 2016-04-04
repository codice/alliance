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

import java.util.List;
import java.util.Map;

import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;

import com.connexta.alliance.libs.stanag4609.DecodedKLVMetadataPacket;

/**
 * Handle the various KLV data elements/structures that returned by the STANAG 4609 parser. The
 * main entry point is {@link #handle(Map, KlvHandler, Map)}.
 */
public interface Stanag4609Processor {
    void handle(Map<String, KlvHandler> handlers, KlvHandler defaultHander,
            Map<Integer, List<DecodedKLVMetadataPacket>> stanagMetadata);

    void handle(Map<String, KlvHandler> handlers, KlvHandler defaultHandler, KlvContext klvContext,
            Map<String, KlvDataElement> dataElements);

    void handle(Map<String, KlvHandler> handlers, KlvHandler defaultHandler,
            KlvDataElement klvDataElement, Map<String, KlvDataElement> dataElements);

    void callDataElementHandlers(Map<String, KlvHandler> handlers, KlvHandler defaultHandler,
            KlvDataElement klvDataElement, Map<String, KlvDataElement> dataElements);

    void handle(Map<String, KlvHandler> handlers, KlvHandler defaultHandler,
            KlvLocalSet klvLocalSet, Map<String, KlvDataElement> dataElements);
}
