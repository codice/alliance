/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.netty;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;
import ddf.security.audit.SecurityLogger;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.codice.alliance.video.stream.mpegts.SimpleSubject;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.codice.alliance.video.stream.mpegts.plugins.StreamEndPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamShutdownPlugin;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.junit.Test;

public class UdpStreamProcessorTest {

  @Test
  public void testCreateChannelHandlers() {
    StreamMonitor streamMonitor = mock(StreamMonitor.class);
    when(streamMonitor.getTitle()).thenReturn(Optional.of("title"));
    when(streamMonitor.getStreamUri()).thenReturn(Optional.of(URI.create("udp://127.0.0.1:80")));
    RolloverCondition rolloverCondition = mock(RolloverCondition.class);
    String filenameTemplate = "template";
    FilenameGenerator filenameGenerator = mock(FilenameGenerator.class);
    List<MetacardType> metacardTypeList = Collections.singletonList(mock(MetacardType.class));
    CatalogFramework catalogFramework = mock(CatalogFramework.class);
    UdpStreamProcessor udpStreamProcessor = new UdpStreamProcessor(streamMonitor);
    udpStreamProcessor.setRolloverCondition(rolloverCondition);
    udpStreamProcessor.setFilenameTemplate(filenameTemplate);
    udpStreamProcessor.setFilenameGenerator(filenameGenerator);
    udpStreamProcessor.setMetacardTypeList(metacardTypeList);
    udpStreamProcessor.setCatalogFramework(catalogFramework);
    udpStreamProcessor.setStreamCreationPlugin(context -> {});
    udpStreamProcessor.setStreamShutdownPlugin(mock(StreamShutdownPlugin.class));
    udpStreamProcessor.setStreamCreationSubject(new SimpleSubject());
    udpStreamProcessor.setParentMetacardUpdater(mock(MetacardUpdater.class));
    udpStreamProcessor.setSecurityLogger(mock(SecurityLogger.class));

    udpStreamProcessor.init();
    try {
      assertThat(udpStreamProcessor.createChannelHandlers(), notNullValue());
    } finally {
      udpStreamProcessor.shutdown();
    }
  }

  @Test
  public void testSetStreamEndPlugin() throws InterruptedException {

    StreamMonitor streamMonitor = mock(StreamMonitor.class);
    UdpStreamProcessor udpStreamProcessor = new UdpStreamProcessor(streamMonitor);
    RolloverCondition rolloverCondition = mock(RolloverCondition.class);
    when(rolloverCondition.isRolloverReady(any())).thenReturn(true);

    StreamEndPlugin streamEndPlugin = mock(StreamEndPlugin.class);

    udpStreamProcessor.setStreamEndPlugin(streamEndPlugin);
    udpStreamProcessor.setRolloverCondition(rolloverCondition);
    udpStreamProcessor.setRolloverAction(mock(RolloverAction.class));

    udpStreamProcessor.getPacketBuffer().write(new byte[] {0x00});

    Thread.sleep(1000);

    udpStreamProcessor.checkForRollover();

    verify(streamEndPlugin).streamEnded(any());
  }
}
