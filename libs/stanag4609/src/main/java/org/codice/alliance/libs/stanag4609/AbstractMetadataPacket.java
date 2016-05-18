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

import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedShort;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.jcodec.containers.mps.MPSDemuxer.PESPacket;

abstract class AbstractMetadataPacket {
    protected final byte[] pesPacketBytes;

    protected final PESPacket pesHeader;

    protected final KlvDecoder decoder;

    protected AbstractMetadataPacket(final byte[] pesPacketBytes, final PESPacket pesHeader,
            final KlvDecoder decoder) {
        this.pesPacketBytes = pesPacketBytes;
        this.pesHeader = pesHeader;
        this.decoder = decoder;
    }

    private boolean validateChecksum(final KlvContext klvContext, final byte[] klvBytes)
            throws KlvDecodingException {
        if (!klvContext.hasDataElement(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET)) {
            throw new KlvDecodingException("KLV did not contain the UAS Datalink Local Set");
        }

        final KlvContext localSetContext = ((KlvLocalSet) klvContext.getDataElementByName(
                Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET)).getValue();

        if (localSetContext.hasDataElement(Stanag4609TransportStreamParser.CHECKSUM)) {
            final int packetChecksum = ((KlvUnsignedShort) localSetContext.getDataElementByName(
                    Stanag4609TransportStreamParser.CHECKSUM)).getValue();

            short calculatedChecksum = 0;
            // Checksum is calculated by a 16-bit sum from the beginning of the KLV set to the 1-byte
            // checksum length (the checksum value is 2 bytes, which is why we subtract 2).
            for (int i = 0; i < klvBytes.length - 2; ++i) {
                calculatedChecksum += (klvBytes[i] & 0xFF) << (8 * ((i + 1) % 2));
            }

            return (calculatedChecksum & 0xFFFF) == packetChecksum;
        }

        throw new KlvDecodingException(
                "Decoded KLV packet didn't contain checksum (which is required).");
    }

    protected final byte[] getPESPacketPayload(final int packetLength,
            final int expectedHeaderLength) {
        final int payloadEnd = Math.min(pesPacketBytes.length, expectedHeaderLength + packetLength);
        return Arrays.copyOfRange(pesPacketBytes, expectedHeaderLength, payloadEnd);
    }

    protected abstract byte[] getKLVBytes();

    final DecodedKLVMetadataPacket decodeKLV() throws KlvDecodingException {
        final byte[] klvBytes = getKLVBytes();

        if (klvBytes != null && klvBytes.length > 0) {
            final KlvContext decodedKLV = decoder.decode(klvBytes);

            if (validateChecksum(decodedKLV, klvBytes)) {
                return new DecodedKLVMetadataPacket(pesHeader.pts, decodedKLV);
            } else {
                throw new KlvDecodingException("KLV packet checksum does not match.");
            }
        }

        return null;
    }
}
