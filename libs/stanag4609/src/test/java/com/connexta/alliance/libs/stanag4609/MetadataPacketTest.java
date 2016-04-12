/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.libs.stanag4609;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedShort;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.jcodec.containers.mps.MPSUtils;
import org.junit.Test;

public class MetadataPacketTest {
    @Test
    public void testSynchronousMetadataPacket() throws Exception {
        final byte[] pesPacketBytes =
                new byte[] {0x00, 0x00, 0x01, (byte) 0xFC, 0x00, 0x22, (byte) 0x85, (byte) 0x80,
                        0x00, 0x27, 0x19, 0x2B, 0x33, (byte) 0x91, 0x01, 0x01, 0x01, 0x00, 0x15,
                        0x06, 0x0E, 0x2B, 0x34, 0x02, 0x0B, 0x01, 0x01, 0x0E, 0x01, 0x03, 0x01,
                        0x01, 0x00, 0x00, 0x00, 0x04, 0x01, 0x02, 0x4C, 0x51};

        final SynchronousMetadataPacket packet = new SynchronousMetadataPacket(pesPacketBytes,
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
                new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
        final DecodedKLVMetadataPacket decodedPacket = packet.decodeKLV();

        verifyDecodedKLV(decodedPacket);

        final long presentationTimestamp = 3326777800L;
        assertThat(decodedPacket.getPresentationTimestamp(), is(presentationTimestamp));
    }

    @Test
    public void testSynchronousMetadataPacketMetadataAccessUnitTooShort() throws Exception {
        final byte[] pesPacketBytes =
                new byte[] {0x00, 0x00, 0x01, (byte) 0xFC, 0x00, 0x0C, (byte) 0x85, (byte) 0x80,
                        0x00, 0x27, 0x19, 0x2B, 0x33, (byte) 0x91, 0x01, 0x01, 0x01, 0x00};

        final SynchronousMetadataPacket packet = new SynchronousMetadataPacket(pesPacketBytes,
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
                new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
        assertThat(packet.decodeKLV(), nullValue());
    }

    @Test
    public void testAsynchronousMetadataPacket() throws Exception {
        final byte[] pesPacketBytes =
                new byte[] {0x00, 0x00, 0x01, (byte) 0xBD, 0x00, 0x18, (byte) 0x85, (byte) 0x00,
                        0x00, 0x06, 0x0E, 0x2B, 0x34, 0x02, 0x0B, 0x01, 0x01, 0x0E, 0x01, 0x03,
                        0x01, 0x01, 0x00, 0x00, 0x00, 0x04, 0x01, 0x02, 0x4C, 0x51};

        final AsynchronousMetadataPacket packet = new AsynchronousMetadataPacket(pesPacketBytes,
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
                new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
        final DecodedKLVMetadataPacket decodedPacket = packet.decodeKLV();
        verifyDecodedKLV(decodedPacket);

        assertThat(decodedPacket.getPresentationTimestamp(), is(lessThan(0L)));
    }

    private void verifyDecodedKLV(final DecodedKLVMetadataPacket decodedPacket) {
        assertThat(decodedPacket, notNullValue());

        assertThat(decodedPacket.getDecodedKLV()
                .hasDataElement(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET), is(true));

        final KlvContext localSetContext = ((KlvLocalSet) decodedPacket.getDecodedKLV()
                .getDataElementByName(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET)).getValue();
        assertThat(((KlvUnsignedShort) localSetContext.getDataElementByName(
                Stanag4609TransportStreamParser.CHECKSUM)).getValue(), is(19537));
    }

    @Test(expected = KlvDecodingException.class)
    public void testWrongChecksum() throws Exception {
        final byte[] pesPacketBytes =
                new byte[] {0x00, 0x00, 0x01, (byte) 0xBD, 0x00, 0x18, (byte) 0x85, (byte) 0x00,
                        0x00, 0x06, 0x0E, 0x2B, 0x34, 0x02, 0x0B, 0x01, 0x01, 0x0E, 0x01, 0x03,
                        0x01, 0x01, 0x00, 0x00, 0x00, 0x04, 0x01, 0x02, 0x4C, 0x52};

        final AsynchronousMetadataPacket packet = new AsynchronousMetadataPacket(pesPacketBytes,
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
                new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
        packet.decodeKLV();
    }

    @Test(expected = KlvDecodingException.class)
    public void testChecksumMissingUASDatalinkLocalSet() throws Exception {
        final byte[] pesPacketBytes =
                new byte[] {0x00, 0x00, 0x01, (byte) 0xBD, 0x00, 0x18, (byte) 0x85, (byte) 0x00,
                        0x00, 0x06, 0x0E, 0x2B, 0x34, 0x02, 0x0B, 0x01, 0x01, 0x0E, 0x01, 0x03,
                        0x01, 0x01, 0x00, 0x00, 0x01, 0x04, 0x01, 0x02, 0x4C, 0x52};

        final AsynchronousMetadataPacket packet = new AsynchronousMetadataPacket(pesPacketBytes,
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
                new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
        packet.decodeKLV();
    }

    @Test(expected = KlvDecodingException.class)
    public void testMissingChecksum() throws Exception {
        final byte[] pesPacketBytes =
                new byte[] {0x00, 0x00, 0x01, (byte) 0xBD, 0x00, 0x18, (byte) 0x85, (byte) 0x00,
                        0x00, 0x06, 0x0E, 0x2B, 0x34, 0x02, 0x0B, 0x01, 0x01, 0x0E, 0x01, 0x03,
                        0x01, 0x01, 0x00, 0x00, 0x00, 0x04, 0x06, 0x02, 0x01, 0x01};

        final AsynchronousMetadataPacket packet = new AsynchronousMetadataPacket(pesPacketBytes,
                MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
                new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
        packet.decodeKLV();
    }
}
