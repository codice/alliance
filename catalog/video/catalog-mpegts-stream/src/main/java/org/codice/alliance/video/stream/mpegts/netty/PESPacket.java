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
package org.codice.alliance.video.stream.mpegts.netty;

import static org.apache.commons.lang3.Validate.notNull;

import org.jcodec.containers.mps.MTSUtils;

/**
 * POJO that contains the payload of a PES, the stream type and the packet id.
 */
class PESPacket {

    private final byte[] payload;

    private final MTSUtils.StreamType streamType;

    private final int packetId;

    /**
     * @param payload    must be non-null
     * @param streamType must be non-null
     * @param packetId   the packet identifier
     */
    public PESPacket(byte[] payload, MTSUtils.StreamType streamType, int packetId) {
        notNull(payload, "payload must be non-null");
        notNull(streamType, "streamType must be non-null");
        this.payload = payload;
        this.streamType = streamType;
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public MTSUtils.StreamType getStreamType() {
        return streamType;
    }

}
