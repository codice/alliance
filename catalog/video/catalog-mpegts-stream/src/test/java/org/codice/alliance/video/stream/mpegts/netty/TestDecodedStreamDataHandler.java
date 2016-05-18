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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.jcodec.codecs.h264.io.model.NALUnit;
import org.jcodec.codecs.h264.io.model.NALUnitType;
import org.junit.Before;
import org.junit.Test;

import io.netty.channel.embedded.EmbeddedChannel;

public class TestDecodedStreamDataHandler {

    private PacketBuffer packetBuffer;

    private Stanag4609Processor stanag4609Processor;

    private KlvHandler defaultKlvHandler;

    private Lock lock;

    @Before
    public void setup() {
        packetBuffer = mock(PacketBuffer.class);
        stanag4609Processor = mock(Stanag4609Processor.class);
        defaultKlvHandler = mock(KlvHandler.class);
        lock = mock(Lock.class);
    }

    @Test
    public void testWrongArgumentType() throws Exception {

        EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer,
                stanag4609Processor,
                Collections.emptyMap(),
                defaultKlvHandler,
                lock));

        channel.writeInbound("not a DecodedStreamData.class");

        verify(packetBuffer, never()).frameComplete(any());
        verify(stanag4609Processor, never()).handle(any(), any(), any());

    }

    @Test
    public void testDetectIDR() throws Exception {

        List<NALUnit> nalUnitList = new LinkedList<>();
        NALUnit nalUnit = new NALUnit(NALUnitType.IDR_SLICE, 0);
        nalUnitList.add(nalUnit);

        DecodedStreamData decodedStreamData = mock(DecodedStreamData.class);
        when(decodedStreamData.getNalUnits()).thenReturn(Optional.of(nalUnitList));
        when(decodedStreamData.getDecodedKLVMetadataPacket()).thenReturn(Optional.empty());

        EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer,
                stanag4609Processor,
                Collections.emptyMap(),
                defaultKlvHandler,
                lock));

        channel.writeInbound(decodedStreamData);

        verify(packetBuffer).frameComplete(PacketBuffer.FrameType.IDR);

    }

    @Test
    public void testDetectNonIDR() throws Exception {

        EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer,
                stanag4609Processor,
                Collections.emptyMap(),
                defaultKlvHandler,
                lock));

        List<NALUnit> nalUnitList = new LinkedList<>();
        NALUnit nalUnit = new NALUnit(NALUnitType.NON_IDR_SLICE, 0);
        nalUnitList.add(nalUnit);

        DecodedStreamData decodedStreamData = mock(DecodedStreamData.class);
        when(decodedStreamData.getNalUnits()).thenReturn(Optional.of(nalUnitList));
        when(decodedStreamData.getDecodedKLVMetadataPacket()).thenReturn(Optional.empty());

        channel.writeInbound(decodedStreamData);

        verify(packetBuffer).frameComplete(PacketBuffer.FrameType.NON_IDR);

    }

    @Test
    public void testKlvCalled() throws Exception {

        Map<String, KlvHandler> klvHandlerMap = new HashMap<>();

        EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer,
                stanag4609Processor,
                klvHandlerMap,
                defaultKlvHandler,
                lock));

        DecodedKLVMetadataPacket decodedKLVMetadataPacket = mock(DecodedKLVMetadataPacket.class);

        int pid = 2;

        DecodedStreamData decodedStreamData = mock(DecodedStreamData.class);
        when(decodedStreamData.getPacketId()).thenReturn(pid);
        when(decodedStreamData.getDecodedKLVMetadataPacket()).thenReturn(Optional.of(
                decodedKLVMetadataPacket));
        when(decodedStreamData.getNalUnits()).thenReturn(Optional.empty());

        channel.writeInbound(decodedStreamData);

        verify(stanag4609Processor).handle(klvHandlerMap,
                defaultKlvHandler,
                Collections.singletonMap(pid, Collections.singletonList(decodedKLVMetadataPacket)));

    }

}
