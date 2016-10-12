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

import java.nio.ByteBuffer;

import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.jcodec.containers.mps.MPSDemuxer;
import org.jcodec.containers.mps.MPSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PESUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(PESUtilities.class);

    private static final int METADATA_STREAM_ID = 0xFC;

    private static final int PRIVATE_STREAM_ID = 0xBD;

    public static DecodedKLVMetadataPacket handlePESPacketBytes(final byte[] pesPacketBytes,
            KlvDecoder decoder) throws KlvDecodingException {
        final MPSDemuxer.PESPacket pesHeader =
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0);

        if (pesHeader.streamId == METADATA_STREAM_ID) {
            return new SynchronousMetadataPacket(pesPacketBytes, pesHeader, decoder).decodeKLV();
        } else if (pesHeader.streamId == PRIVATE_STREAM_ID) {
            return new AsynchronousMetadataPacket(pesPacketBytes, pesHeader, decoder).decodeKLV();
        } else {
            LOGGER.debug("Unknown stream type {}. Skipping this packet.", pesHeader.streamId);
        }
        return null;
    }

}