/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.libs.stanag4609;

import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedShort;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.jcodec.containers.mps.MPSDemuxer.PESPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractMetadataPacket {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetadataPacket.class);

  /**
   * Location of the "PES header length" field which contains the number of extra bytes contained in
   * the header.
   */
  private static final int PES_HEADER_LENGTH_INDEX = 8;

  private static final int BASE_PES_PACKET_HEADER_LENGTH = 9;

  private final byte[] pesPacketBytes;

  private final PESPacket pesHeader;

  protected final KlvDecoder decoder;

  AbstractMetadataPacket(
      final byte[] pesPacketBytes, final PESPacket pesHeader, final KlvDecoder decoder) {
    this.pesPacketBytes = pesPacketBytes;
    this.pesHeader = pesHeader;
    this.decoder = decoder;
  }

  private boolean validateChecksum(final KlvContext klvContext, final byte[] klvBytes)
      throws KlvDecodingException {
    if (!klvContext.hasDataElement(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET)) {
      throw new KlvDecodingException("KLV did not contain the UAS Datalink Local Set");
    }

    final KlvContext localSetContext =
        ((KlvLocalSet)
                klvContext.getDataElementByName(
                    Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET))
            .getValue();

    if (localSetContext.hasDataElement(Stanag4609TransportStreamParser.CHECKSUM)) {
      final int packetChecksum =
          ((KlvUnsignedShort)
                  localSetContext.getDataElementByName(Stanag4609TransportStreamParser.CHECKSUM))
              .getValue();

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

  protected final byte[] getPESPacketPayload() {

    if (this.pesPacketBytes.length < BASE_PES_PACKET_HEADER_LENGTH) {
      return null;
    }

    int additionalHeaderBytes = Byte.toUnsignedInt(pesPacketBytes[PES_HEADER_LENGTH_INDEX]);

    int payloadLength = pesHeader.length - 3 - additionalHeaderBytes;
    int headerLength = BASE_PES_PACKET_HEADER_LENGTH + additionalHeaderBytes;

    final int payloadEnd = Math.min(pesPacketBytes.length, headerLength + payloadLength);
    return Arrays.copyOfRange(pesPacketBytes, headerLength, payloadEnd);
  }

  /** @return klv payload bytes, otherwise null */
  protected abstract byte[] getKLVBytes();

  final DecodedKLVMetadataPacket decodeKLV() throws KlvDecodingException {
    final byte[] klvBytes = getKLVBytes();

    if (klvBytes != null && klvBytes.length > 0) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("KLV bytes: {}", DatatypeConverter.printHexBinary(klvBytes));
      }

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
