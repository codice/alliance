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
package org.codice.alliance.video.stream.mpegts.netty;

import static org.apache.commons.lang3.Validate.notNull;

import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;

public class KLVDecodedStreamData implements DecodedStreamData {

    private final DecodedKLVMetadataPacket decodedKLVMetadataPacket;

    private final int packetId;

    /**
     * @param decodedKLVMetadataPacket decoded klv metadata (must be non-null)
     * @param packetId                 MPEG-TS packet identifier
     */
    public KLVDecodedStreamData(DecodedKLVMetadataPacket decodedKLVMetadataPacket, int packetId) {
        notNull(decodedKLVMetadataPacket, "decodedKLVMetadataPacket must be non-null");
        this.decodedKLVMetadataPacket = decodedKLVMetadataPacket;
        this.packetId = packetId;
    }

    /**
     * @return decoded klv metadata
     */
    public DecodedKLVMetadataPacket getDecodedKLVMetadataPacket() {
        return decodedKLVMetadataPacket;
    }

    @Override
    public int getPacketId() {
        return packetId;
    }

    @Override
    public void accept(Visitor visitor) {
        notNull(visitor, "visitor must be non-null");
        visitor.visit(this);
    }

}
