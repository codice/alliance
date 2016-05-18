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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;

public class TestRawUdpDataToMTSPacketDecoder {

    @Test
    public void test() throws Exception {

        int packetCount = 100;

        List<DatagramPacket> datagramPackets = toDatagrams(flatten(createTsPackets(packetCount)));

        PacketBuffer packetBuffer = mock(PacketBuffer.class);

        EmbeddedChannel channel =
                new EmbeddedChannel(new RawUdpDataToMTSPacketDecoder(packetBuffer));

        datagramPackets.forEach(channel::writeInbound);

        List<Object> outputList = NettyUtility.read(channel);

        assertThat(outputList, hasSize(packetCount));

    }

    /**
     * Create a list of fake MPEG-TS packets.
     *
     * @param packetCount number of packet
     * @return list of raw packets
     */
    private List<byte[]> createTsPackets(int packetCount) {

        List<byte[]> packets = new LinkedList<>();

        for (int i = 0; i < packetCount; i++) {
            byte[] bytes = new byte[RawUdpDataToMTSPacketDecoder.TS_PACKET_SIZE];
            bytes[0] = RawUdpDataToMTSPacketDecoder.TS_SYNC;
            packets.add(bytes);
        }

        return packets;
    }

    /**
     * Flatten a list of byte arrays into a single byte array.
     *
     * @param packets list of raw packets
     * @return raw packet data
     */
    private byte[] flatten(List<byte[]> packets) {

        byte[] bytes = new byte[0];

        for (byte[] in : packets) {
            bytes = ArrayUtils.addAll(bytes, in);
        }

        return bytes;
    }

    /**
     * Split an array of bytes into datagram packets.
     *
     * @param bytes payload data
     * @return list of datagrams
     */
    private List<DatagramPacket> toDatagrams(byte[] bytes) {

        int datagramSize = 1500;

        List<DatagramPacket> datagrams = new LinkedList<>();

        byte[] tmp = bytes;

        while (tmp.length > datagramSize) {
            byte[] subarray = ArrayUtils.subarray(tmp, 0, datagramSize);
            datagrams.add(new DatagramPacket(Unpooled.wrappedBuffer(subarray), null));
            tmp = ArrayUtils.subarray(tmp, datagramSize, tmp.length);
        }

        if (tmp.length > 0) {
            datagrams.add(new DatagramPacket(Unpooled.wrappedBuffer(tmp), null));
        }

        return datagrams;
    }

}
