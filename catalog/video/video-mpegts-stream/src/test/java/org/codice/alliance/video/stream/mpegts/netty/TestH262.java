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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.MTSSources;
import org.taktik.mpegts.sources.ResettableMTSSource;

import com.google.common.io.ByteSource;

import io.netty.channel.embedded.EmbeddedChannel;

public class TestH262 {

    private static final int TS_SIZE = 188;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestH262.class);

    private PacketBuffer packetBuffer;

    private Stanag4609Processor stanag4609Processor;

    private Map<String, KlvHandler> klvHandlerMap;

    private KlvHandler defaultKlvHandler;

    private Lock klvHandlerMapLock;

    @Before
    public void setup() {
        packetBuffer = mock(PacketBuffer.class);
        stanag4609Processor = mock(Stanag4609Processor.class);
        klvHandlerMap = Collections.emptyMap();
        defaultKlvHandler = mock(KlvHandler.class);
        klvHandlerMapLock = mock(Lock.class);
    }

    @Test
    public void testIDRFrameCount() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new MTSPacketToPESPacketDecoder(),
                new PESPacketToApplicationDataDecoder(true),
                new DecodedStreamDataHandler(packetBuffer,
                        stanag4609Processor,
                        klvHandlerMap,
                        defaultKlvHandler,
                        klvHandlerMapLock));

        InputStream inputStream = getInputStream("/Closed_Caption_EIA_MPEG2.ts");
        byte[] buffer = new byte[TS_SIZE];
        int c;
        while ((c = inputStream.read(buffer)) != -1) {
            if (c == TS_SIZE) {
                ResettableMTSSource src = MTSSources.from(ByteSource.wrap(buffer));
                MTSPacket packet = null;
                try {
                    packet = src.nextPacket();
                } catch (IOException e) {
                    LOGGER.warn("unable to parse mpegst packet", e);
                }

                if (packet != null) {
                    channel.writeInbound(packet);
                }
            }
        }
        verify(packetBuffer, times(37)).frameComplete(PacketBuffer.FrameType.IDR);
    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }

}
