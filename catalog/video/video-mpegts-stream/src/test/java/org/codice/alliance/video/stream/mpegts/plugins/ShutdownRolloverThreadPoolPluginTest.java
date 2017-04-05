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

import java.util.concurrent.ExecutorService;

import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.junit.Test;

public class ShutdownRolloverThreadPoolPluginTest {

    @Test
    public void testShutdown() throws StreamShutdownException {

        Context context = mock(Context.class);
        UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);
        when(context.getUdpStreamProcessor()).thenReturn(udpStreamProcessor);
        ExecutorService executorService = mock(ExecutorService.class);
        when(udpStreamProcessor.getRolloverThreadPool()).thenReturn(executorService);

        new ShutdownRolloverThreadPoolPlugin().doOnShutdown(context);

        verify(executorService).shutdown();

    }

}
