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
package org.codice.alliance.libs.stanag4609;

import java.util.Arrays;

import org.codice.ddf.libs.klv.KlvDecoder;
import org.jcodec.containers.mps.MPSDemuxer.PESPacket;

class SynchronousMetadataPacket extends AbstractMetadataPacket {
    private static final int SYNCHRONOUS_PES_PACKET_HEADER_LENGTH = 14;

    private static final int METADATA_ACCESS_UNIT_HEADER_LENGTH = 5;

    SynchronousMetadataPacket(final byte[] pesPacketBytes, final PESPacket pesHeader,
            final KlvDecoder decoder) {
        super(pesPacketBytes, pesHeader, decoder);
    }

    @Override
    protected byte[] getKLVBytes() {
        // For synchronous metadata streams, the header is supposed to be 14 bytes long. The header's
        // length field gives the number of bytes in the packet following it, so we need to skip
        // the 8 header bytes after the length field to get the true length of the payload.
        final byte[] metadataAccessUnit = getPESPacketPayload(pesHeader.length - 8,
                SYNCHRONOUS_PES_PACKET_HEADER_LENGTH);

        if (metadataAccessUnit.length > METADATA_ACCESS_UNIT_HEADER_LENGTH) {
            return getKLVPayloadFromMetadataAccessUnit(metadataAccessUnit);
        }

        return null;
    }

    private byte[] getKLVPayloadFromMetadataAccessUnit(final byte[] metadataAccessUnit) {
        final int payloadLength =
                ((metadataAccessUnit[3] & 0xFF) << 8) | (metadataAccessUnit[4] & 0xFF);
        final int payloadEnd = Math.min(metadataAccessUnit.length,
                METADATA_ACCESS_UNIT_HEADER_LENGTH + payloadLength);
        return Arrays.copyOfRange(metadataAccessUnit,
                METADATA_ACCESS_UNIT_HEADER_LENGTH,
                payloadEnd);
    }
}
