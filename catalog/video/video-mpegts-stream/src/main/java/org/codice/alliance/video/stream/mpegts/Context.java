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
package org.codice.alliance.video.stream.mpegts;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Optional;

import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;

import ddf.catalog.data.Metacard;

/**
 * This class supplies data used by different parts of the stream processor.
 */
public class Context {

    private final UdpStreamProcessor udpStreamProcessor;

    private Optional<Metacard> parentMetacard = Optional.empty();

    /**
     * @param udpStreamProcessor must be non-null
     */
    public Context(UdpStreamProcessor udpStreamProcessor) {
        notNull(udpStreamProcessor, "udpStreamProcessor must be non-null");
        this.udpStreamProcessor = udpStreamProcessor;
    }

    public UdpStreamProcessor getUdpStreamProcessor() {
        return udpStreamProcessor;
    }

    public Optional<Metacard> getParentMetacard() {
        return parentMetacard;
    }

    /**
     * @param parentMetacard must be non-null
     */
    public void setParentMetacard(Metacard parentMetacard) {
        notNull(parentMetacard, "parentMetacard must be non-null");
        this.parentMetacard = Optional.of(parentMetacard);
    }

}
