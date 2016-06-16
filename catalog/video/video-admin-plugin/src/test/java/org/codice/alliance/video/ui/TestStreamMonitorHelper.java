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
package org.codice.alliance.video.ui;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.UdpStreamMonitor;
import org.codice.alliance.video.ui.service.StreamMonitorHelper;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TestStreamMonitorHelper {

    private StreamMonitorHelper stream;

    private static final String TEST_URL = "udp://127.0.0.1:50000";

    private BundleContext bundleContext;

    private UdpStreamMonitor udpStreamMonitor;

    private boolean isMonitoring;

    private class OtherStreamMonitor implements StreamMonitor {

        @Override
        public Optional<URI> getStreamUri() {
            return null;
        }

        @Override
        public Optional<String> getTitle() {
            return Optional.of("");
        }

        @Override
        public void stopMonitoring() {

        }

        @Override
        public void startMonitoring() {

        }

        @Override
        public boolean isMonitoring() {
            return true;
        }
    }

    URI uri;

    @Before
    public void setUp() throws Exception {
        bundleContext = mock(BundleContext.class);
        uri = new URI(TEST_URL);
        List<ServiceReference<StreamMonitor>> serviceReferences = new ArrayList<>();
        udpStreamMonitor = mock(UdpStreamMonitor.class);
        ServiceReference<StreamMonitor> streamMonitorServiceReference =
                mock(ServiceReference.class);
        serviceReferences.add(streamMonitorServiceReference);

        when(bundleContext.getServiceReferences(eq(StreamMonitor.class), anyString())).thenReturn(
                serviceReferences);
        when(udpStreamMonitor.getTitle()).thenReturn(Optional.of("test"));
        when(udpStreamMonitor.getStreamUri()).thenReturn(Optional.of(uri));
        //when(udpStreamMonitor.startMonitoring()).thenReturn(Optional.of(uri));
        doAnswer(invocation -> {
            isMonitoring = true;
            return null;
        }).when(udpStreamMonitor)
                .startMonitoring();

        doAnswer(invocation -> {
            isMonitoring = false;
            return null;
        }).when(udpStreamMonitor)
                .stopMonitoring();

        when(bundleContext.getService(any(ServiceReference.class))).thenReturn(udpStreamMonitor);
        when(streamMonitorServiceReference.getProperty(anyString())).thenReturn(StreamMonitorHelper.SERVICE_PID);

        stream = new StreamMonitorHelper();
        stream.setContext(bundleContext);
    }

    @Test
    public void testStreamMonitorsNoServiceReferences() throws Exception {
        List<ServiceReference<StreamMonitor>> serviceReferences = new ArrayList<>();
        when(bundleContext.getServiceReferences(eq(StreamMonitor.class), anyString())).thenReturn(
                serviceReferences);

        List<Map<String, Object>> list = stream.udpStreamMonitors();
        assertThat(list, nullValue());
    }

    @Test
    public void testStreamMonitorsWrongService() {
        when(bundleContext.getService(any(ServiceReference.class))).thenReturn(new OtherStreamMonitor());
        List<Map<String, Object>> list = stream.udpStreamMonitors();
        assertThat(list, notNullValue());
        assertThat(list, hasSize(0));
    }

    @Test
    public void testStreamMonitors() {
        List<Map<String, Object>> list = stream.udpStreamMonitors();
        assertThat(list, notNullValue());
        assertThat(list.size(), is(1));
        Map<String, Object> objectMap = list.get(0);
        assertThat(objectMap.get("id"), is(StreamMonitorHelper.SERVICE_PID));
        assertThat(objectMap.get(UdpStreamMonitor.METATYPE_TITLE), is("test"));
        assertThat(objectMap.get(UdpStreamMonitor.METATYPE_MONITORED_ADDRESS), is(TEST_URL));
    }

    @Test
    public void testStreamMonitorsNullBundleContext() {
        StreamMonitorHelper stream = new StreamMonitorHelper();
        List<Map<String, Object>> list = stream.udpStreamMonitors();
        assertThat(list, nullValue());
    }

    @Test
    public void testCallStartAndStopMonitoringStream() {
        assertThat(isMonitoring, is(false));
        stream.callStartMonitoringStreamByServicePid(StreamMonitorHelper.SERVICE_PID);
        assertThat(isMonitoring, is(true));
        stream.callStopMonitoringStreamByServicePid(StreamMonitorHelper.SERVICE_PID);
        assertThat(isMonitoring, is(false));
    }
}
