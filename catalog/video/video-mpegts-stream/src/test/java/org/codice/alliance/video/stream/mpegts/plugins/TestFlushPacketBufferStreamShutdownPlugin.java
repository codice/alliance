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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.junit.Test;

public class TestFlushPacketBufferStreamShutdownPlugin {

    @Test
    public void testOnShutdown() throws StreamShutdownException, IOException {

        Context context = mock(Context.class);
        UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);
        PacketBuffer packetBuffer = mock(PacketBuffer.class);
        File file = mock(File.class);

        when(context.getUdpStreamProcessor()).thenReturn(udpStreamProcessor);
        when(udpStreamProcessor.getPacketBuffer()).thenReturn(packetBuffer);
        when(packetBuffer.flushAndRotate()).thenReturn(Optional.of(file));

        FlushPacketBufferStreamShutdownPlugin flushPacketBufferStreamShutdownPlugin =
                new FlushPacketBufferStreamShutdownPlugin();

        flushPacketBufferStreamShutdownPlugin.onShutdown(context);

        verify(udpStreamProcessor).doRollover(file);

    }

}
