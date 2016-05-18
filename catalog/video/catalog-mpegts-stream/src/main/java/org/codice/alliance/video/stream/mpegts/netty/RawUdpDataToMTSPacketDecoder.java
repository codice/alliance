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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.MTSSources;
import org.taktik.mpegts.sources.ResettableMTSSource;

import com.google.common.io.ByteSource;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Converts datagrams to a series of MTSPackets. Will discard data while looking for the MPEG-TS
 * sync byte.
 */
class RawUdpDataToMTSPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {

    public static final byte TS_SYNC = (byte) 0x47;

    public static final int BUFFER_SIZE = 4096;

    public static final int TS_PACKET_SIZE = 188;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RawUdpDataToMTSPacketDecoder.class);

    private ByteBuf byteBuf;

    private PacketBuffer packetBuffer;

    private MTSParser mtsParser = MTSSources::from;

    public RawUdpDataToMTSPacketDecoder(PacketBuffer packetBuffer) {
        this.packetBuffer = packetBuffer;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (byteBuf != null) {
            byteBuf.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byteBuf = ctx.alloc()
                .buffer(BUFFER_SIZE);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> outputList)
            throws Exception {

        notNull(ctx, "ctx must be non-null");
        notNull(msg, "msg must be non-null");
        notNull(outputList, "outputList must be non-null");

        byteBuf.writeBytes(msg.content());

        skipToSyncByte();

        while (byteBuf.readableBytes() >= TS_PACKET_SIZE) {

            byte[] payload = new byte[TS_PACKET_SIZE];

            byteBuf.readBytes(payload);

            ResettableMTSSource src = mtsParser.parse(ByteSource.wrap(payload));

            MTSPacket packet = null;
            try {
                packet = src.nextPacket();
            } catch (IOException e) {
                LOGGER.warn("unable to parse mpegst packet", e);
            }

            if (packet != null) {
                packetBuffer.write(payload);
                outputList.add(packet);
            }

            skipToSyncByte();
        }

        byteBuf.discardReadBytes();

    }

    private void skipToSyncByte() {

        int bytesBefore;

        if ((bytesBefore = byteBuf.bytesBefore(TS_SYNC)) > 0) {
            LOGGER.info("skipping bytes in raw data stream, looking for MPEG-TS sync {}",
                    bytesBefore);
            byteBuf.skipBytes(bytesBefore);
        }

    }

    public interface MTSParser {
        ResettableMTSSource parse(ByteSource byteSource) throws IOException;
    }

}
