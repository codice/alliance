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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.codice.alliance.libs.mpegts.MpegTsDecoder;
import org.junit.Test;
import org.taktik.mpegts.MTSPacket;

import io.netty.channel.embedded.EmbeddedChannel;

public class MTSPacketToPESPacketDecoderTest {

    /**
     * This test sends MTSPackets to the decoder so that the decoder outputs one video pes packet.
     *
     * @throws Exception
     */
    @Test
    public void testDecode() throws Exception {

        MpegTsDecoder mpegTsDecoder = mock(MpegTsDecoder.class);

        MTSPacketToPESPacketDecoder decoder = new MTSPacketToPESPacketDecoder(mpegTsDecoder);

        MTSPacket mtsPacket = mock(MTSPacket.class);

        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        channel.writeInbound(mtsPacket);

        NettyUtility.read(channel);

        verify(mpegTsDecoder).read(eq(mtsPacket), anyObject());

    }

}
