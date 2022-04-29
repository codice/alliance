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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;
import ddf.security.Subject;
import ddf.security.audit.SecurityLogger;
import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.codice.alliance.video.stream.mpegts.plugins.StreamCreationPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamEndPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamShutdownPlugin;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class UdpStreamProcessorTest {
  private final BundleContext mockBundleContext = mock(BundleContext.class);

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
    UdpStreamProcessor udpStreamProcessor =
        new UdpStreamProcessor(streamMonitor, mockBundleContext);
    udpStreamProcessor.setRolloverCondition(rolloverCondition);
    udpStreamProcessor.setFilenameTemplate(filenameTemplate);
    udpStreamProcessor.setFilenameGenerator(filenameGenerator);
    udpStreamProcessor.setMetacardTypeList(metacardTypeList);
    udpStreamProcessor.setCatalogFramework(catalogFramework);
    udpStreamProcessor.setStreamCreationPlugin(context -> {});
    udpStreamProcessor.setStreamShutdownPlugin(mock(StreamShutdownPlugin.class));
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
    UdpStreamProcessor udpStreamProcessor =
        new UdpStreamProcessor(streamMonitor, mockBundleContext);
    RolloverCondition rolloverCondition = mock(RolloverCondition.class);
    when(rolloverCondition.isRolloverReady(any())).thenReturn(true);

    StreamEndPlugin streamEndPlugin = mock(StreamEndPlugin.class);

    udpStreamProcessor.setStreamEndPlugin(streamEndPlugin);
    udpStreamProcessor.setRolloverCondition(rolloverCondition);
    udpStreamProcessor.setRolloverAction(mock(RolloverAction.class));

    udpStreamProcessor.getPacketBuffer().write(new byte[] {0x00});

    Thread.sleep(5000);

    udpStreamProcessor.checkForRollover();

    verify(streamEndPlugin).streamEnded(any());
  }

  private void prepareSubject() throws SecurityServiceException {
    final ServiceReference<SecurityManager> mockSecurityManagerReference =
        mock(ServiceReference.class);
    final SecurityManager mockSecurityManager = mock(SecurityManager.class);
    when(mockBundleContext.getServiceReference(SecurityManager.class))
        .thenReturn(mockSecurityManagerReference);
    when(mockBundleContext.getService(mockSecurityManagerReference))
        .thenReturn(mockSecurityManager);
    final ServiceReference<SecurityLogger> mockSecurityLoggerReference =
        mock(ServiceReference.class);
    final SecurityLogger mockSecurityLogger = mock(SecurityLogger.class);
    when(mockBundleContext.getServiceReference(SecurityLogger.class))
        .thenReturn(mockSecurityLoggerReference);
    when(mockBundleContext.getService(mockSecurityLoggerReference)).thenReturn(mockSecurityLogger);
    final Subject mockSubject = mock(Subject.class);
    when(mockSecurityManager.getSubject(any())).thenReturn(mockSubject);
    doAnswer(
            invocationOnMock -> {
              ((Runnable) invocationOnMock.getArgument(0)).run();
              return null;
            })
        .when(mockSubject)
        .execute(any(Runnable.class));
  }

  @Test
  public void initCallsStreamCreationPlugin() throws Exception {
    prepareSubject();
    final StreamMonitor streamMonitor = mock(StreamMonitor.class);
    final UdpStreamProcessor udpStreamProcessor =
        new UdpStreamProcessor(streamMonitor, mockBundleContext);
    final StreamCreationPlugin streamCreationPlugin = mock(StreamCreationPlugin.class);
    udpStreamProcessor.setStreamCreationPlugin(streamCreationPlugin);
    udpStreamProcessor.init();
    verify(streamCreationPlugin).onCreate(any());
  }

  @Test
  public void shutdownCallsStreamShutdownPlugin() throws Exception {
    prepareSubject();
    final StreamMonitor streamMonitor = mock(StreamMonitor.class);
    final UdpStreamProcessor udpStreamProcessor =
        new UdpStreamProcessor(streamMonitor, mockBundleContext);
    final StreamShutdownPlugin streamShutdownPlugin = mock(StreamShutdownPlugin.class);
    udpStreamProcessor.setStreamShutdownPlugin(streamShutdownPlugin);
    udpStreamProcessor.shutdown();
    verify(streamShutdownPlugin).onShutdown(any());
  }
}
