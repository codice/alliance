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

import java.util.List;
import java.util.Optional;

import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.jcodec.codecs.h264.io.model.NALUnit;

/**
 * POJO that contains decoded stream data (H264 video data or metadata). It is intended for the
 * object to contain only NALUnit data or metadata, but never both at the same time.
 */
class DecodedStreamData {

    private final int packetId;

    private List<NALUnit> nalUnits = null;

    private DecodedKLVMetadataPacket decodedKLVMetadataPacket = null;

    /**
     * @param nalUnits must be non-null
     * @param packetId the MPEG-TS packet id associated with data
     */
    public DecodedStreamData(List<NALUnit> nalUnits, int packetId) {
        notNull(nalUnits, "nalUnits must be non-null");
        this.nalUnits = nalUnits;
        this.packetId = packetId;
    }

    /**
     * @param decodedKLVMetadataPacket must be non-null
     * @param packetId                 the MPEG-TS packet id associated with data
     */
    public DecodedStreamData(DecodedKLVMetadataPacket decodedKLVMetadataPacket, int packetId) {
        notNull(decodedKLVMetadataPacket, "decodedKLVMetadataPacket must be non-null");
        this.decodedKLVMetadataPacket = decodedKLVMetadataPacket;
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    /**
     * @return non-null value
     */
    public Optional<List<NALUnit>> getNalUnits() {
        return Optional.ofNullable(nalUnits);
    }

    /**
     * @return non-null value
     */
    public Optional<DecodedKLVMetadataPacket> getDecodedKLVMetadataPacket() {
        return Optional.ofNullable(decodedKLVMetadataPacket);
    }

}
