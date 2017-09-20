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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedShort;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.jcodec.containers.mps.MPSDemuxer;
import org.jcodec.containers.mps.MPSUtils;
import org.junit.Test;

public class MetadataPacketTest {

  /**
   * The binary data in this test was captured from a data stream where the PES header contains an
   * optional timestamp.
   */
  @Test
  public void testThatPesHeadersWithOptionalFieldsAreHandled() throws KlvDecodingException {

    byte[] packet =
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xbd,
          0x01,
          0x08,
          (byte) 0x84,
          (byte) 0x80,
          0x05,
          0x21,
          0x00,
          0x09,
          0x41,
          (byte) 0xd9,
          0x06,
          0x0e,
          0x2b,
          0x34,
          0x02,
          0x0b,
          0x01,
          0x01,
          0x0e,
          0x01,
          0x03,
          0x01,
          0x01,
          0x00,
          0x00,
          0x00,
          (byte) 0x82,
          0x00,
          (byte) 0xed,
          0x02,
          0x08,
          0x00,
          0x04,
          (byte) 0xe4,
          (byte) 0xea,
          (byte) 0xad,
          0x53,
          (byte) 0xfb,
          0x48,
          0x03,
          0x05,
          0x30,
          0x30,
          0x30,
          0x30,
          0x31,
          0x04,
          0x03,
          0x30,
          0x30,
          0x31,
          0x05,
          0x02,
          0x5a,
          (byte) 0xb6,
          0x06,
          0x02,
          0x00,
          0x00,
          0x07,
          0x02,
          0x00,
          0x00,
          0x0a,
          0x08,
          0x56,
          0x52,
          0x53,
          0x47,
          0x20,
          0x35,
          0x2e,
          0x36,
          0x0b,
          0x18,
          0x56,
          0x52,
          0x53,
          0x47,
          0x20,
          0x56,
          0x35,
          0x2e,
          0x36,
          0x2e,
          0x37,
          0x32,
          0x20,
          0x41,
          0x70,
          0x72,
          0x20,
          0x20,
          0x37,
          0x20,
          0x32,
          0x30,
          0x31,
          0x31,
          0x0c,
          0x0e,
          0x47,
          0x65,
          0x6f,
          0x64,
          0x65,
          0x74,
          0x69,
          0x63,
          0x20,
          0x57,
          0x47,
          0x53,
          0x38,
          0x34,
          0x0d,
          0x04,
          0x30,
          (byte) 0xf7,
          0x04,
          (byte) 0xea,
          0x0e,
          0x04,
          0x31,
          0x2d,
          (byte) 0xb4,
          0x76,
          0x0f,
          0x02,
          0x3f,
          0x08,
          0x10,
          0x02,
          0x03,
          (byte) 0x89,
          0x11,
          0x02,
          0x02,
          (byte) 0xa7,
          0x12,
          0x04,
          0x06,
          (byte) 0xfd,
          0x6b,
          (byte) 0xff,
          0x13,
          0x04,
          (byte) 0xd7,
          (byte) 0xc3,
          0x3c,
          (byte) 0xce,
          0x14,
          0x04,
          0x00,
          0x00,
          0x00,
          0x00,
          0x15,
          0x04,
          0x00,
          0x21,
          (byte) 0xac,
          0x7e,
          0x16,
          0x02,
          0x02,
          (byte) 0xdb,
          0x17,
          0x04,
          0x30,
          (byte) 0xf3,
          (byte) 0x98,
          0x3a,
          0x18,
          0x04,
          0x31,
          0x2f,
          (byte) 0x99,
          0x59,
          0x19,
          0x02,
          0x23,
          0x72,
          0x1a,
          0x02,
          (byte) 0xff,
          (byte) 0xfe,
          0x1b,
          0x02,
          0x01,
          0x6e,
          0x1c,
          0x02,
          (byte) 0xfe,
          (byte) 0xc7,
          0x1d,
          0x02,
          (byte) 0xff,
          (byte) 0xee,
          0x1e,
          0x02,
          (byte) 0xff,
          (byte) 0xf8,
          0x1f,
          0x02,
          (byte) 0xfe,
          (byte) 0xa6,
          0x20,
          0x02,
          0x01,
          0x22,
          0x21,
          0x02,
          0x00,
          0x22,
          0x30,
          0x20,
          0x01,
          0x01,
          0x01,
          0x02,
          0x01,
          0x01,
          0x03,
          0x04,
          0x2f,
          0x2f,
          0x55,
          0x53,
          0x06,
          0x05,
          0x43,
          0x41,
          0x20,
          0x55,
          0x53,
          0x0c,
          0x01,
          0x01,
          0x0d,
          0x04,
          0x2f,
          0x2f,
          0x55,
          0x53,
          0x16,
          0x02,
          0x00,
          0x07,
          0x41,
          0x01,
          0x01,
          0x48,
          0x08,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x01,
          0x02,
          (byte) 0xb7,
          (byte) 0x8d
        };

    final MPSDemuxer.PESPacket pesHeader = MPSUtils.readPESHeader(ByteBuffer.wrap(packet), 0);

    AsynchronousMetadataPacket asynchronousMetadataPacket =
        new AsynchronousMetadataPacket(
            packet,
            pesHeader,
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));

    DecodedKLVMetadataPacket p = asynchronousMetadataPacket.decodeKLV();

    assertThat(p, notNullValue());

    KlvLocalSet e =
        (KlvLocalSet)
            p.getDecodedKLV()
                .getDataElementByName(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET);

    KlvDataElement timestamp =
        e.getValue().getDataElementByName(Stanag4609TransportStreamParser.TIMESTAMP);

    assertThat(timestamp.getValue(), is(1377596488285000L));
  }

  @Test
  public void testSynchronousMetadataPacket() throws Exception {
    final byte[] pesPacketBytes =
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xFC,
          0x00,
          0x22,
          (byte) 0x85,
          (byte) 0x80,
          0x05,
          0x27,
          0x19,
          0x2B,
          0x33,
          (byte) 0x91,
          0x01,
          0x01,
          0x01,
          0x00,
          0x15,
          0x06,
          0x0E,
          0x2B,
          0x34,
          0x02,
          0x0B,
          0x01,
          0x01,
          0x0E,
          0x01,
          0x03,
          0x01,
          0x01,
          0x00,
          0x00,
          0x00,
          0x04,
          0x01,
          0x02,
          0x4C,
          0x51
        };

    final SynchronousMetadataPacket packet =
        new SynchronousMetadataPacket(
            pesPacketBytes,
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
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xFC,
          0x00,
          0x0C,
          (byte) 0x85,
          (byte) 0x80,
          0x05,
          0x27,
          0x19,
          0x2B,
          0x33,
          (byte) 0x91,
          0x01,
          0x01,
          0x01,
          0x00
        };

    final SynchronousMetadataPacket packet =
        new SynchronousMetadataPacket(
            pesPacketBytes,
            MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
    assertThat(packet.decodeKLV(), nullValue());
  }

  @Test
  public void testAsynchronousMetadataPacket() throws Exception {
    final byte[] pesPacketBytes =
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xBD,
          0x00,
          0x18,
          (byte) 0x85,
          (byte) 0x00,
          0x00,
          0x06,
          0x0E,
          0x2B,
          0x34,
          0x02,
          0x0B,
          0x01,
          0x01,
          0x0E,
          0x01,
          0x03,
          0x01,
          0x01,
          0x00,
          0x00,
          0x00,
          0x04,
          0x01,
          0x02,
          0x4C,
          0x51
        };

    final AsynchronousMetadataPacket packet =
        new AsynchronousMetadataPacket(
            pesPacketBytes,
            MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
    final DecodedKLVMetadataPacket decodedPacket = packet.decodeKLV();
    verifyDecodedKLV(decodedPacket);

    assertThat(decodedPacket.getPresentationTimestamp(), is(lessThan(0L)));
  }

  private void verifyDecodedKLV(final DecodedKLVMetadataPacket decodedPacket) {
    assertThat(decodedPacket, notNullValue());

    assertThat(
        decodedPacket
            .getDecodedKLV()
            .hasDataElement(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET),
        is(true));

    final KlvContext localSetContext =
        ((KlvLocalSet)
                decodedPacket
                    .getDecodedKLV()
                    .getDataElementByName(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET))
            .getValue();
    assertThat(
        ((KlvUnsignedShort)
                localSetContext.getDataElementByName(Stanag4609TransportStreamParser.CHECKSUM))
            .getValue(),
        is(19537));
  }

  @Test(expected = KlvDecodingException.class)
  public void testWrongChecksum() throws Exception {
    final byte[] pesPacketBytes =
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xBD,
          0x00,
          0x18,
          (byte) 0x85,
          (byte) 0x00,
          0x00,
          0x06,
          0x0E,
          0x2B,
          0x34,
          0x02,
          0x0B,
          0x01,
          0x01,
          0x0E,
          0x01,
          0x03,
          0x01,
          0x01,
          0x00,
          0x00,
          0x00,
          0x04,
          0x01,
          0x02,
          0x4C,
          0x52
        };

    final AsynchronousMetadataPacket packet =
        new AsynchronousMetadataPacket(
            pesPacketBytes,
            MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
    packet.decodeKLV();
  }

  @Test(expected = KlvDecodingException.class)
  public void testChecksumMissingUASDatalinkLocalSet() throws Exception {
    final byte[] pesPacketBytes =
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xBD,
          0x00,
          0x18,
          (byte) 0x85,
          (byte) 0x00,
          0x00,
          0x06,
          0x0E,
          0x2B,
          0x34,
          0x02,
          0x0B,
          0x01,
          0x01,
          0x0E,
          0x01,
          0x03,
          0x01,
          0x01,
          0x00,
          0x00,
          0x01,
          0x04,
          0x01,
          0x02,
          0x4C,
          0x52
        };

    final AsynchronousMetadataPacket packet =
        new AsynchronousMetadataPacket(
            pesPacketBytes,
            MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
    packet.decodeKLV();
  }

  @Test(expected = KlvDecodingException.class)
  public void testMissingChecksum() throws Exception {
    final byte[] pesPacketBytes =
        new byte[] {
          0x00,
          0x00,
          0x01,
          (byte) 0xBD,
          0x00,
          0x18,
          (byte) 0x85,
          (byte) 0x00,
          0x00,
          0x06,
          0x0E,
          0x2B,
          0x34,
          0x02,
          0x0B,
          0x01,
          0x01,
          0x0E,
          0x01,
          0x03,
          0x01,
          0x01,
          0x00,
          0x00,
          0x00,
          0x04,
          0x06,
          0x02,
          0x01,
          0x01
        };

    final AsynchronousMetadataPacket packet =
        new AsynchronousMetadataPacket(
            pesPacketBytes,
            MPSUtils.readPESHeader(ByteBuffer.wrap(pesPacketBytes), 0),
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT));
    packet.decodeKLV();
  }
}
