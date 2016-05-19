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

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvHandlerFactory;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.junit.Test;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;

public class TestUdpStreamProcessor {

    @Test
    public void testCreateChannelHandlers() {
        StreamMonitor streamMonitor = mock(StreamMonitor.class);
        Stanag4609Processor stanag4609Processor = mock(Stanag4609Processor.class);
        KlvHandler defaultKlvHandler = mock(KlvHandler.class);
        RolloverCondition rolloverCondition = mock(RolloverCondition.class);
        String filenameTemplate = "template";
        FilenameGenerator filenameGenerator = mock(FilenameGenerator.class);
        KlvProcessor klvProcessor = mock(KlvProcessor.class);
        List<MetacardType> metacardTypeList = mock(List.class);
        CatalogFramework catalogFramework = mock(CatalogFramework.class);
        KlvHandlerFactory klvHandlerFactory = mock(KlvHandlerFactory.class);
        when(klvHandlerFactory.createStanag4609Handlers()).thenReturn(Collections.emptyMap());
        UdpStreamProcessor udpStreamProcessor = new UdpStreamProcessor(streamMonitor);
        udpStreamProcessor.setStanag4609Processor(stanag4609Processor);
        udpStreamProcessor.setKlvHandlerFactory(klvHandlerFactory);
        udpStreamProcessor.setDefaultKlvHandler(defaultKlvHandler);
        udpStreamProcessor.setRolloverCondition(rolloverCondition);
        udpStreamProcessor.setFilenameTemplate(filenameTemplate);
        udpStreamProcessor.setFilenameGenerator(filenameGenerator);
        udpStreamProcessor.setKlvProcessor(klvProcessor);
        udpStreamProcessor.setMetacardTypeList(metacardTypeList);
        udpStreamProcessor.setCatalogFramework(catalogFramework);

        udpStreamProcessor.init();
        try {
            assertThat(udpStreamProcessor.createChannelHandlers(), notNullValue());
        } finally {
            udpStreamProcessor.shutdown();
        }
    }

}
