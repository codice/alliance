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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.security.SubjectOperations;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.ddf.platform.util.uuidgenerator.UuidGenerator;
import org.junit.Test;

public class RolloverStreamCreationPluginTest {

  @Test
  public void testOnCreate() throws StreamCreationException {

    Context context = mock(Context.class);
    UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);

    when(context.getUdpStreamProcessor()).thenReturn(udpStreamProcessor);
    when(udpStreamProcessor.getFilenameGenerator()).thenReturn(mock(FilenameGenerator.class));
    when(udpStreamProcessor.getFilenameTemplate()).thenReturn("template");
    when(udpStreamProcessor.getCatalogFramework()).thenReturn(mock(CatalogFramework.class));
    when(udpStreamProcessor.getParentMetacardUpdater()).thenReturn(mock(MetacardUpdater.class));
    when(udpStreamProcessor.getSubjectOperations()).thenReturn(mock(SubjectOperations.class));

    UuidGenerator uuidGenerator = mock(UuidGenerator.class);
    when(uuidGenerator.generateUuid()).thenReturn("anId");
    when(udpStreamProcessor.getUuidGenerator()).thenReturn(uuidGenerator);

    RolloverStreamCreationPlugin rolloverStreamCreationPlugin = new RolloverStreamCreationPlugin();

    rolloverStreamCreationPlugin.onCreate(context);

    verify(udpStreamProcessor).setRolloverAction(any());
  }
}
