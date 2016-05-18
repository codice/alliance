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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.codice.alliance.libs.stanag4609.PESUtilities;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.codecs.h264.io.model.NALUnit;
import org.jcodec.containers.mps.MTSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Decodes PESPacket into NALUnits or decoded klv metadata. If the PES is some other type, then
 * it is ignored.
 */
class PESPacketToApplicationDataDecoder extends MessageToMessageDecoder<PESPacket> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PESPacketToApplicationDataDecoder.class);

    private final KlvDecoder klvDecoder =
            new KlvDecoder(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT);

    private NALReader nalReader = H264Utils::nextNALUnit;

    private NALParser nalParser = NALUnit::read;

    private KlvParser klvParser = PESUtilities::handlePESPacketBytes;

    private boolean isKlvEnabled;

    public PESPacketToApplicationDataDecoder(boolean isKlvEnabled) {
        this.isKlvEnabled = isKlvEnabled;
    }

    /**
     * @param nalParser must be non-null
     */
    public void setNalParser(NALParser nalParser) {
        notNull(nalParser, "nalParser must be non-null");
        this.nalParser = nalParser;
    }

    /**
     * @param nalReader must be non-null
     */
    public void setNalReader(NALReader nalReader) {
        notNull(nalReader, "nalReader must be non-null");
        this.nalReader = nalReader;
    }

    /**
     * @param klvParser must be non-null
     */
    public void setKlvParser(KlvParser klvParser) {
        notNull(klvParser, "klvParser must be non-null");
        this.klvParser = klvParser;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PESPacket pesPacket, List<Object> outputList)
            throws Exception {

        notNull(ctx, "ctx must be non-null");
        notNull(pesPacket, "pesPacket must be non-null");
        notNull(outputList, "outputList must be non-null");

        if (isKlvEnabled && isMetadata(pesPacket)) {
            decodeKlvMetadata(pesPacket, outputList);
        } else if (isVideo(pesPacket)) {
            decodeVideoH264(pesPacket, outputList);
        }
    }

    private boolean isVideo(PESPacket pesPacket) {
        return pesPacket.getStreamType() == MTSUtils.StreamType.VIDEO_H264;
    }

    private boolean isMetadata(PESPacket pesPacket) {
        return pesPacket.getStreamType() == MTSUtils.StreamType.PRIVATE_DATA
                || pesPacket.getStreamType() == MTSUtils.StreamType.META_PES;
    }

    private void decodeVideoH264(PESPacket pesPacket, List<Object> outputList) {
        ByteBuffer p = ByteBuffer.wrap(pesPacket.getPayload());

        List<NALUnit> nalUnits = new LinkedList<>();

        ByteBuffer segment;
        while ((segment = nalReader.next(p)) != null) {

            NALUnit nalUnit = nalParser.parse(segment);

            if (nalUnit != null) {
                nalUnits.add(nalUnit);
            }
        }

        outputList.add(new DecodedStreamData(nalUnits, pesPacket.getPacketId()));

    }

    private void decodeKlvMetadata(PESPacket pesPacket, List<Object> outputList) {
        try {
            outputList.add(new DecodedStreamData(klvParser.parse(pesPacket.getPayload(),
                    klvDecoder), pesPacket.getPacketId()));
        } catch (KlvDecodingException e) {
            LOGGER.warn("unable to decode KLV metadata", e);
        }
    }

    public interface KlvParser {
        DecodedKLVMetadataPacket parse(byte[] pesPacketBytes, KlvDecoder decoder)
                throws KlvDecodingException;
    }

    public interface NALReader {
        ByteBuffer next(ByteBuffer byteBuffer);
    }

    public interface NALParser {
        NALUnit parse(ByteBuffer byteBuffer);
    }

}
