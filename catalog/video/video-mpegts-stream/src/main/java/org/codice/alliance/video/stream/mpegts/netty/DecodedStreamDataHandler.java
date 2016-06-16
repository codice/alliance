/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.netty;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.jcodec.codecs.h264.io.model.NALUnit;
import org.jcodec.codecs.h264.io.model.NALUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty handler for {@link DecodedStreamData}. If called with video data, then tells the
 * PacketBuffer if the data contains an IDR or NON-IDR frame. If called with metadata, then
 * calls the KlvHandlers.
 */
class DecodedStreamDataHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecodedStreamDataHandler.class);

    private final PacketBuffer packetBuffer;

    private final Stanag4609Processor stanag4609Processor;

    private final Map<String, KlvHandler> klvHandlerMap;

    private final KlvHandler defaultKlvHandler;

    private final Lock klvHandlerMapLock;

    public DecodedStreamDataHandler(PacketBuffer packetBuffer,
            Stanag4609Processor stanag4609Processor, Map<String, KlvHandler> klvHandlerMap,
            KlvHandler defaultKlvHandler, Lock klvHandlerMapLock) {

        notNull(packetBuffer, "packetBuffer must be non-null");
        notNull(stanag4609Processor, "stanag4609Processor must be non-null");
        notNull(klvHandlerMap, "klvHandlerMap must be non-null");
        notNull(defaultKlvHandler, "defaultKlvHandler must be non-null");
        notNull(klvHandlerMapLock, "klvHandlerMapLocl must be non-null");

        this.packetBuffer = packetBuffer;
        this.stanag4609Processor = stanag4609Processor;
        this.klvHandlerMap = klvHandlerMap;
        this.defaultKlvHandler = defaultKlvHandler;
        this.klvHandlerMapLock = klvHandlerMapLock;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof DecodedStreamData)) {
            LOGGER.error("handler passed incorrect data type, must be DecodedStreamData, but was {}",
                    msg.getClass());
            return;
        }

        DecodedStreamData decodedStreamData = (DecodedStreamData) msg;

        decodedStreamData.accept(new DecodedStreamData.Visitor() {
            @Override
            public void visit(KLVDecodedStreamData decodedStreamData) {
                handleDecodedKLVMetadataPacket(decodedStreamData.getDecodedKLVMetadataPacket(),
                        decodedStreamData.getPacketId());
            }

            @Override
            public void visit(Mpeg2DecodedStreamData decodedStreamData) {
                handleMpeg2(decodedStreamData.getListOfTypes());
            }

            @Override
            public void visit(Mpeg4DecodedStreamData decodedStreamData) {
                handleNALUnits(decodedStreamData.getNalUnits());
            }
        });

    }

    private void frameComplete(boolean isIDR) {
        packetBuffer.frameComplete(isIDR ?
                PacketBuffer.FrameType.IDR :
                PacketBuffer.FrameType.NON_IDR);
    }

    private void handleMpeg2(List<Mpeg2PictureType> mpeg2PictureTypeList) {

        boolean allIntraCoded = mpeg2PictureTypeList.stream()
                .allMatch(mpeg2PictureType -> mpeg2PictureType == Mpeg2PictureType.INTRA_CODED);

        frameComplete(allIntraCoded);
    }

    private void handleNALUnits(List<NALUnit> nalUnitList) {

        boolean containsIDR = nalUnitList.stream()
                .anyMatch(nalUnit -> nalUnit.type == NALUnitType.IDR_SLICE);

        frameComplete(containsIDR);
    }

    private void handleDecodedKLVMetadataPacket(DecodedKLVMetadataPacket decodedKLVMetadataPacket,
            int packetId) {

        klvHandlerMapLock.lock();
        try {
            stanag4609Processor.handle(klvHandlerMap,
                    defaultKlvHandler,
                    Collections.singletonMap(packetId,
                            Collections.singletonList(decodedKLVMetadataPacket)));
        } finally {
            klvHandlerMapLock.unlock();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("error: ", cause);
        ctx.close();
    }

}
