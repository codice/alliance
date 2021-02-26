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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.UdpStreamMonitor;
import org.codice.alliance.video.ui.service.StreamMonitorHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StreamMonitorHelperTest {

  private static final String TEST_URL = "udp://127.0.0.1:50000";

  URI uri;

  private StreamMonitorHelper stream;

  private BundleContext bundleContext;

  private UdpStreamMonitor udpStreamMonitor;

  private boolean isMonitoring;

  @Before
  public void setUp() throws Exception {
    bundleContext = mock(BundleContext.class);
    uri = new URI(TEST_URL);
    List<ServiceReference<StreamMonitor>> serviceReferences = new ArrayList<>();
    udpStreamMonitor = mock(UdpStreamMonitor.class);
    ServiceReference<StreamMonitor> streamMonitorServiceReference = mock(ServiceReference.class);
    serviceReferences.add(streamMonitorServiceReference);

    when(bundleContext.getServiceReferences(eq(StreamMonitor.class), eq(null)))
        .thenReturn(serviceReferences);
    when(udpStreamMonitor.getTitle()).thenReturn(Optional.of("test"));
    when(udpStreamMonitor.getStreamUri()).thenReturn(Optional.of(uri));
    // when(udpStreamMonitor.startMonitoring()).thenReturn(Optional.of(uri));
    doAnswer(
            invocation -> {
              isMonitoring = true;
              return null;
            })
        .when(udpStreamMonitor)
        .startMonitoring();

    doAnswer(
            invocation -> {
              isMonitoring = false;
              return null;
            })
        .when(udpStreamMonitor)
        .stopMonitoring();

    when(bundleContext.getService(any(ServiceReference.class))).thenReturn(udpStreamMonitor);
    when(streamMonitorServiceReference.getProperty(anyString()))
        .thenReturn(StreamMonitorHelper.SERVICE_PID);

    stream = new StreamMonitorHelper();
    stream.setContext(bundleContext);
  }

  @Test
  public void testStreamMonitorsNoServiceReferences() throws Exception {
    List<ServiceReference<StreamMonitor>> serviceReferences = new ArrayList<>();
    when(bundleContext.getServiceReferences(eq(StreamMonitor.class), eq(null)))
        .thenReturn(serviceReferences);

    List<Map<String, Object>> list = stream.udpStreamMonitors();
    assertThat(list, nullValue());
  }

  @Test
  public void testStreamMonitorsWrongService() {
    when(bundleContext.getService(any(ServiceReference.class)))
        .thenReturn(new OtherStreamMonitor());
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

  @Test
  public void testNetworkInterfaces() throws SocketException {

    Inet4Address inetAddress = mock(Inet4Address.class);

    NetworkInterface networkInterface = mock(NetworkInterface.class);

    when(inetAddress.getHostAddress()).thenReturn("0.0.0.0");
    when(networkInterface.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress));
              }
            });

    when(networkInterface.supportsMulticast()).thenReturn(true);
    when(networkInterface.getName()).thenReturn("eth0");
    when(networkInterface.getDisplayName()).thenReturn("DisplayName");

    Map<String, String> networkInterfaces =
        new TestableStreamMonitorHelper(networkInterface).networkInterfaces();

    assertThat(networkInterfaces, is(Collections.singletonMap("eth0", "DisplayName (0.0.0.0)")));
  }

  @Test
  public void testNetworkInterfacesMultipleInterfaces() throws SocketException {

    Inet4Address inetAddress1 = mock(Inet4Address.class);
    Inet4Address inetAddress2 = mock(Inet4Address.class);

    NetworkInterface networkInterface1 = mock(NetworkInterface.class);
    NetworkInterface networkInterface2 = mock(NetworkInterface.class);

    when(inetAddress1.getHostAddress()).thenReturn("0.0.0.0");

    when(networkInterface1.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress1));
              }
            });

    when(networkInterface1.supportsMulticast()).thenReturn(true);
    when(networkInterface1.getName()).thenReturn("eth0");
    when(networkInterface1.getDisplayName()).thenReturn("DisplayName1");

    when(inetAddress2.getHostAddress()).thenReturn("0.0.0.0");

    when(networkInterface2.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress2));
              }
            });

    when(networkInterface2.supportsMulticast()).thenReturn(true);
    when(networkInterface2.getName()).thenReturn("eth1");
    when(networkInterface2.getDisplayName()).thenReturn("DisplayName2");

    Map<String, String> networkInterfaces =
        new TestableStreamMonitorHelper(Arrays.asList(networkInterface1, networkInterface2))
            .networkInterfaces();

    Map<String, String> expected = new HashMap<>();
    expected.put("eth0", "DisplayName1 (0.0.0.0)");
    expected.put("eth1", "DisplayName2 (0.0.0.0)");

    assertThat(networkInterfaces, is(expected));
  }

  @Test
  public void testNetworkInterfacesMultipleInterfacesOneDoesntSupportMulticast()
      throws SocketException {

    Inet4Address inetAddress1 = mock(Inet4Address.class);
    Inet4Address inetAddress2 = mock(Inet4Address.class);

    NetworkInterface networkInterface1 = mock(NetworkInterface.class);
    NetworkInterface networkInterface2 = mock(NetworkInterface.class);

    when(networkInterface1.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress1));
              }
            });

    when(networkInterface1.supportsMulticast()).thenReturn(false);
    when(networkInterface1.getName()).thenReturn("eth0");
    when(networkInterface1.getDisplayName()).thenReturn("DisplayName1");

    when(inetAddress2.getHostAddress()).thenReturn("0.0.0.0");

    when(networkInterface2.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress2));
              }
            });

    when(networkInterface2.supportsMulticast()).thenReturn(true);
    when(networkInterface2.getName()).thenReturn("eth1");
    when(networkInterface2.getDisplayName()).thenReturn("DisplayName2");

    Map<String, String> networkInterfaces =
        new TestableStreamMonitorHelper(Arrays.asList(networkInterface1, networkInterface2))
            .networkInterfaces();

    assertThat(networkInterfaces, is(Collections.singletonMap("eth1", "DisplayName2 (0.0.0.0)")));
  }

  @Test
  public void testNetworkInterfacesMultipleInterfacesOneIsIPv6() throws SocketException {

    Inet6Address inetAddress1 = mock(Inet6Address.class);
    Inet4Address inetAddress2 = mock(Inet4Address.class);

    NetworkInterface networkInterface1 = mock(NetworkInterface.class);
    NetworkInterface networkInterface2 = mock(NetworkInterface.class);

    when(networkInterface1.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress1));
              }
            });

    when(networkInterface1.supportsMulticast()).thenReturn(true);
    when(networkInterface1.getName()).thenReturn("eth0");
    when(networkInterface1.getDisplayName()).thenReturn("DisplayName1");

    when(inetAddress2.getHostAddress()).thenReturn("0.0.0.0");

    when(networkInterface2.getInetAddresses())
        .then(
            new Answer<Enumeration<InetAddress>>() {
              @Override
              public Enumeration<InetAddress> answer(InvocationOnMock invocationOnMock)
                  throws Throwable {
                return Collections.enumeration(Collections.singletonList(inetAddress2));
              }
            });

    when(networkInterface2.supportsMulticast()).thenReturn(true);
    when(networkInterface2.getName()).thenReturn("eth1");
    when(networkInterface2.getDisplayName()).thenReturn("DisplayName2");

    Map<String, String> networkInterfaces =
        new TestableStreamMonitorHelper(Arrays.asList(networkInterface1, networkInterface2))
            .networkInterfaces();

    assertThat(networkInterfaces, is(Collections.singletonMap("eth1", "DisplayName2 (0.0.0.0)")));
  }

  @Test
  public void testNetworkInterfacesGetNetworkInterfacesThrows() throws SocketException {

    Map<String, String> networkInterfaces = new ThrowingStreamMonitorHelper().networkInterfaces();

    assertThat(networkInterfaces, is(Collections.emptyMap()));
  }

  @Test
  public void testNetworkInterfacesNothingReturned() throws SocketException {

    Map<String, String> networkInterfaces =
        new TestableStreamMonitorHelper(Collections.emptyList()).networkInterfaces();

    assertThat(networkInterfaces, is(Collections.emptyMap()));
  }

  private static class TestableStreamMonitorHelper extends StreamMonitorHelper {

    private List<NetworkInterface> networkInterfaces;

    public TestableStreamMonitorHelper(NetworkInterface networkInterface) {
      this.networkInterfaces = Collections.singletonList(networkInterface);
    }

    public TestableStreamMonitorHelper(List<NetworkInterface> networkInterfaces) {
      this.networkInterfaces = networkInterfaces;
    }

    @Override
    public Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
      return Collections.enumeration(networkInterfaces);
    }
  }

  private static class ThrowingStreamMonitorHelper extends StreamMonitorHelper {

    @Override
    public Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
      throw new SocketException();
    }
  }

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
    public void stopMonitoring() {}

    @Override
    public void startMonitoring() {}

    @Override
    public boolean isMonitoring() {
      return true;
    }
  }
}
