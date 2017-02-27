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
package org.codice.alliance.video.stream.mpegts;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.junit.Before;
import org.junit.Test;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;

public class UdpStreamMonitorTest {

    private UdpStreamProcessor udpStreamProcessor;

    private UdpStreamMonitor udpStreamMonitor;

    @Before
    public void setup() {
        udpStreamProcessor = mock(UdpStreamProcessor.class);
        udpStreamMonitor = new UdpStreamMonitor(udpStreamProcessor);
    }

    @Test
    public void testSetElapsedTimeRolloverCondition() {
        udpStreamMonitor.setElapsedTimeRolloverCondition(UdpStreamMonitor.ELAPSED_TIME_MIN);
        verify(udpStreamProcessor).setElapsedTimeRolloverCondition(UdpStreamMonitor.ELAPSED_TIME_MIN);
        assertThat(udpStreamMonitor.getElapsedTimeRolloverCondition(),
                is(UdpStreamMonitor.ELAPSED_TIME_MIN));
    }

    @Test(expected = NullPointerException.class)
    public void testSetElapsedTimeRolloverConditionNullArg() {
        udpStreamMonitor.setElapsedTimeRolloverCondition(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetElapsedTimeRolloverConditionBelowRangeArg() {
        udpStreamMonitor.setElapsedTimeRolloverCondition(UdpStreamMonitor.ELAPSED_TIME_MIN - 10);
    }

    @Test
    public void testSetStartImmediately() {
        assertThat(udpStreamMonitor.getStartImmediately(), is(false));
        udpStreamMonitor.setStartImmediately(true);
        assertThat(udpStreamMonitor.getStartImmediately(), is(true));
    }

    @Test
    public void testSetMegabyteCountRolloverCondition() {
        udpStreamMonitor.setMegabyteCountRolloverCondition(Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN));
        verify(udpStreamProcessor).setMegabyteCountRolloverCondition(Math.toIntExact(
                UdpStreamMonitor.MEGABYTE_COUNT_MIN));
        assertThat(udpStreamMonitor.getByteCountRolloverCondition(),
                is(Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN)));
    }

    @Test(expected = NullPointerException.class)
    public void testSetByteCountRolloverConditionNullArg() {
        udpStreamMonitor.setMegabyteCountRolloverCondition(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetByteCountRolloverConditionBelowRangeArg() {
        udpStreamMonitor.setMegabyteCountRolloverCondition(
                Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN) - 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMonitoredAddressNoProtocol() {
        String addr = "127.0.0.1:50000";
        udpStreamMonitor.setMonitoredAddress(addr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMonitoredAddressUnsupportedProtocol() {
        String addr = "tcp://127.0.0.1";
        udpStreamMonitor.setMonitoredAddress(addr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMonitoredAddressNoPort() {
        String addr = "udp://127.0.0.1";
        udpStreamMonitor.setMonitoredAddress(addr);
    }

    @Test(expected = NullPointerException.class)
    public void testMonitoredAddressNullArg() {
        udpStreamMonitor.setMonitoredAddress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMonitoredAddressUnresolvableArg() {
        udpStreamMonitor.setMonitoredAddress("127.0.0.0.1");
    }

    @Test
    public void testSetCatalogFramework() {
        CatalogFramework catalogFramework = mock(CatalogFramework.class);
        udpStreamMonitor.setCatalogFramework(catalogFramework);
        verify(udpStreamProcessor).setCatalogFramework(catalogFramework);
    }

    @Test(expected = NullPointerException.class)
    public void testSetCatalogFrameworkNullArg() {
        udpStreamMonitor.setCatalogFramework(null);
    }

    @Test
    public void testSetMetacardTypeList() {
        List<MetacardType> metacardTypeList = Collections.emptyList();
        udpStreamMonitor.setMetacardTypeList(metacardTypeList);
        verify(udpStreamProcessor).setMetacardTypeList(metacardTypeList);
    }

    @Test(expected = NullPointerException.class)
    public void testSetMetacardTypeListNullArg() {
        udpStreamMonitor.setMetacardTypeList(null);
    }

    @Test
    public void testSetTitle() {
        String title = "title";
        udpStreamMonitor.setParentTitle(title);
        assertThat(udpStreamMonitor.getTitle()
                .get(), is(title));
    }

    @Test
    public void testGetNullStreamUri() {
        assertThat(udpStreamMonitor.getStreamUri()
                .isPresent(), is(false));
    }

    @Test
    public void testGetStreamUri() {
        String addr = "udp://127.0.0.1:50000";
        udpStreamMonitor.setMonitoredAddress(addr);
        assertThat(udpStreamMonitor.getStreamUri()
                .get()
                .toString(), is(addr));
        assertThat(udpStreamMonitor.getMonitoredAddress(), is("127.0.0.1"));
    }

    @Test(expected = NullPointerException.class)
    public void testSetRolloverConditionNullArg() {
        udpStreamMonitor.setRolloverCondition(null);
    }

    @Test
    public void testSetRolloverCondition() {
        RolloverCondition rolloverCondition = mock(RolloverCondition.class);
        udpStreamMonitor.setRolloverCondition(rolloverCondition);
        verify(udpStreamProcessor).setRolloverCondition(rolloverCondition);
    }

    @Test(expected = NullPointerException.class)
    public void testSetFilenameTemplateNullArg() {
        udpStreamMonitor.setFilenameTemplate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFilenameTemplateBlankArg() {
        udpStreamMonitor.setFilenameTemplate("");
    }

    @Test
    public void testSetFilenameTemplate() {
        String filenameTemplate = "template";
        udpStreamMonitor.setFilenameTemplate(filenameTemplate);
        verify(udpStreamProcessor).setFilenameTemplate(filenameTemplate);
        assertThat(udpStreamMonitor.getFileNameTemplate(), is(filenameTemplate));
    }

    @Test(expected = NullPointerException.class)
    public void testSetFilenameGeneratorNullArg() {
        udpStreamMonitor.setFilenameGenerator(null);
    }

    @Test
    public void testSetFilenameGenerator() {
        FilenameGenerator filenameGenerator = mock(FilenameGenerator.class);
        udpStreamMonitor.setFilenameGenerator(filenameGenerator);
        verify(udpStreamProcessor).setFilenameGenerator(filenameGenerator);
    }

}
