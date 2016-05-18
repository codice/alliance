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

import org.codice.ddf.libs.klv.KlvDecoder;
import org.jcodec.containers.mps.MPSDemuxer.PESPacket;

class AsynchronousMetadataPacket extends AbstractMetadataPacket {
    private static final int ASYNCHRONOUS_PES_PACKET_HEADER_LENGTH = 9;

    AsynchronousMetadataPacket(final byte[] pesPacketBytes, final PESPacket pesHeader,
            final KlvDecoder decoder) {
        super(pesPacketBytes, pesHeader, decoder);
    }

    @Override
    protected byte[] getKLVBytes() {
        // For asynchronous metadata streams, the header is supposed to be 9 bytes long. The header's
        // length field gives the number of bytes in the packet following it, so we need to skip
        // the 3 header bytes after the length field to get the true length of the payload.
        return getPESPacketPayload(pesHeader.length - 3, ASYNCHRONOUS_PES_PACKET_HEADER_LENGTH);
    }
}
