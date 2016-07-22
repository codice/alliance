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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;

import org.jcodec.codecs.h264.io.model.NALUnit;
import org.jcodec.containers.mps.MTSUtils;
import org.junit.Before;
import org.junit.Test;

import io.netty.channel.embedded.EmbeddedChannel;

public class TestPESPacketToApplicationDataDecoder {

    private static final byte[] EMPTY_ARRAY = new byte[] {};

    private static final ByteBuffer EMPTY_BUF = ByteBuffer.wrap(EMPTY_ARRAY);

    private PESPacketToApplicationDataDecoder decoder;

    private PESPacket pesPacket;

    @Before
    public void setup() {
        decoder = new PESPacketToApplicationDataDecoder();
        pesPacket = mock(PESPacket.class);
    }

    @Test
    public void testDecodeNALUnits() throws Exception {

        when(pesPacket.getStreamType()).thenReturn(MTSUtils.StreamType.VIDEO_H264);
        when(pesPacket.getPayload()).thenReturn(EMPTY_ARRAY);

        PESPacketToApplicationDataDecoder.NALReader nalReader = mock(
                PESPacketToApplicationDataDecoder.NALReader.class);
        when(nalReader.next(any())).thenReturn(EMPTY_BUF)
                .thenReturn(EMPTY_BUF)
                .thenReturn(null);
        decoder.setNalReader(nalReader);

        PESPacketToApplicationDataDecoder.NALParser nalParser = mock(
                PESPacketToApplicationDataDecoder.NALParser.class);

        NALUnit nalUnit1 = mock(NALUnit.class);
        NALUnit nalUnit2 = mock(NALUnit.class);

        when(nalParser.parse(any())).thenReturn(nalUnit1)
                .thenReturn(nalUnit2);

        decoder.setNalParser(nalParser);

        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        channel.writeInbound(pesPacket);

        List<Object> outputList = NettyUtility.read(channel);

        assertThat(outputList, hasSize(1));
        assertThat(outputList.get(0), is(instanceOf(Mpeg4DecodedStreamData.class)));
        Mpeg4DecodedStreamData decodedStreamData = (Mpeg4DecodedStreamData) outputList.get(0);

        assertThat(decodedStreamData.getNalUnits(), notNullValue());
        assertThat(decodedStreamData.getNalUnits(), hasSize(2));
        assertThat(decodedStreamData.getNalUnits()
                .get(0), is(nalUnit1));
        assertThat(decodedStreamData.getNalUnits()
                .get(1), is(nalUnit2));
    }

}
