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
package org.codice.alliance.video.ui.service;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.collections.MapUtils;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.UdpStreamMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamMonitorHelper implements StreamMonitorHelperMBean {

  public static final String SERVICE_PID = "service.Pid";

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamMonitorHelper.class);

  private static final String MONITORING = "monitoring";

  private static final String START_TIME = "startTime";

  private static final String ID = "id";

  private static final Predicate<InetAddress> IPV4_FILTER =
      inetAddress -> inetAddress instanceof Inet4Address;

  private static final Predicate<NetworkInterface> INTERFACE_WITH_IPV4 =
      networkInterface ->
          Collections.list(networkInterface.getInetAddresses()).stream().anyMatch(IPV4_FILTER);

  private static final Predicate<NetworkInterface> SUPPORTS_MULTICAST =
      networkInterface -> {
        try {
          return networkInterface.supportsMulticast();
        } catch (SocketException e) {
          LOGGER.debug(
              "unable to determine if the network interface {} supports multicast",
              networkInterface.getName(),
              e);
        }
        return false;
      };

  private ObjectName objectName;

  private MBeanServer mBeanServer;

  private BundleContext context;

  @Override
  public void callStartMonitoringStreamByServicePid(String servicePid) {
    Map<String, StreamMonitor> udpStreamMonitors = getUdpStreamMonitorServices();

    if (MapUtils.isEmpty(udpStreamMonitors)) {
      return;
    }

    udpStreamMonitors.entrySet().stream()
        .filter(entry -> entry.getKey().equals(servicePid))
        .forEach(
            stringStreamMonitorEntry -> {
              stringStreamMonitorEntry.getValue().startMonitoring();
              LOGGER.debug("Started monitor for {}", stringStreamMonitorEntry.getKey());
            });
  }

  @Override
  public void callStopMonitoringStreamByServicePid(String servicePid) {
    Map<String, StreamMonitor> udpStreamMonitors = getUdpStreamMonitorServices();

    if (MapUtils.isEmpty(udpStreamMonitors)) {
      return;
    }

    udpStreamMonitors.entrySet().stream()
        .filter(entry -> entry.getKey().equals(servicePid))
        .forEach(
            stringStreamMonitorEntry -> {
              stringStreamMonitorEntry.getValue().stopMonitoring();
              LOGGER.debug("Stopped monitor for {}", stringStreamMonitorEntry.getKey());
            });
  }

  @Override
  public List<Map<String, Object>> udpStreamMonitors() {
    Map<String, StreamMonitor> udpStreamMonitors = getUdpStreamMonitorServices();

    if (MapUtils.isEmpty(udpStreamMonitors)) {
      return null;
    }

    return udpStreamMonitors.entrySet().stream()
        .filter(entry -> entry.getValue() instanceof UdpStreamMonitor)
        .map(
            stringStreamMonitorEntry -> {
              UdpStreamMonitor udpStreamMonitor =
                  (UdpStreamMonitor) stringStreamMonitorEntry.getValue();
              Map<String, Object> map = new HashMap<>();
              map.put(ID, stringStreamMonitorEntry.getKey());
              map.put(UdpStreamMonitor.METATYPE_TITLE, udpStreamMonitor.getTitle().get());
              map.put(
                  UdpStreamMonitor.METATYPE_BYTE_COUNT_ROLLOVER_CONDITION,
                  udpStreamMonitor.getByteCountRolloverCondition());
              map.put(
                  UdpStreamMonitor.METATYPE_DISTANCE_TOLERANCE,
                  udpStreamMonitor.getDistanceTolerance());
              map.put(
                  UdpStreamMonitor.METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION,
                  udpStreamMonitor.getElapsedTimeRolloverCondition());
              map.put(
                  UdpStreamMonitor.METATYPE_MONITORED_ADDRESS,
                  udpStreamMonitor.getStreamUri().get().toString());
              map.put(
                  UdpStreamMonitor.METATYPE_NETWORK_INTERFACE,
                  udpStreamMonitor.getNetworkInterface());
              map.put(
                  UdpStreamMonitor.METATYPE_METACARD_UPDATE_INITIAL_DELAY,
                  udpStreamMonitor.getMetacardUpdateInitialDelay());
              map.put(MONITORING, udpStreamMonitor.isMonitoring());
              map.put(START_TIME, udpStreamMonitor.getStartDateAsString());
              map.put(
                  UdpStreamMonitor.METATYPE_FILENAME_TEMPLATE,
                  udpStreamMonitor.getFileNameTemplate());
              return map;
            })
        .collect(Collectors.toList());
  }

  private String commaSeparatedListOfIPv4(NetworkInterface networkInterface) {
    return Collections.list(networkInterface.getInetAddresses()).stream()
        .filter(IPV4_FILTER)
        .map(InetAddress::getHostAddress)
        .collect(Collectors.joining(","));
  }

  private String formatNeworkInterface(NetworkInterface networkInterface) {
    return networkInterface.getDisplayName()
        + " ("
        + commaSeparatedListOfIPv4(networkInterface)
        + ")";
  }

  /**
   * This method is needed for testing. PowerMock does not return the correct value for getName when
   * getName is used directly as a method reference in a streaming command.
   */
  private String getNetworkInterfaceName(NetworkInterface networkInterface) {
    return networkInterface.getName();
  }

  @Override
  public Map<String, String> networkInterfaces() {
    try {
      return Collections.list(getNetworkInterfaces()).stream()
          .filter(INTERFACE_WITH_IPV4)
          .filter(SUPPORTS_MULTICAST)
          .collect(Collectors.toMap(this::getNetworkInterfaceName, this::formatNeworkInterface));
    } catch (SocketException e) {
      LOGGER.debug("unable to get a list of network interfaces", e);
    }
    return Collections.emptyMap();
  }

  /** This method exists so unit tests can mock the network interfaces. */
  protected Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
    return NetworkInterface.getNetworkInterfaces();
  }

  public void init() {
    registerMbean();
  }

  public BundleContext getContext() {
    return this.context;
  }

  public void setContext(BundleContext context) {
    this.context = context;
  }

  private Map<String, StreamMonitor> getUdpStreamMonitorServices() {
    if (getContext() == null) {
      return null;
    }

    Map<String, StreamMonitor> sources = new HashMap<>();
    List<ServiceReference<? extends StreamMonitor>> refs = new ArrayList<>();
    try {
      refs.addAll(getContext().getServiceReferences(StreamMonitor.class, null));
    } catch (InvalidSyntaxException e) {
      LOGGER.debug("Unable to get service references for {}", StreamMonitor.class.getName(), e);
    }

    for (ServiceReference<? extends StreamMonitor> ref : refs) {
      sources.put((String) ref.getProperty(SERVICE_PID), getContext().getService(ref));
    }

    return sources;
  }

  private void registerMbean() {
    try {
      objectName = new ObjectName(StreamMonitorHelper.class.getName() + ":service=stream");
      mBeanServer = ManagementFactory.getPlatformMBeanServer();
    } catch (MalformedObjectNameException e) {
      LOGGER.info("Unable to create FMV Stream Monitor Helper MBean.", e);
    }
    if (mBeanServer == null) {
      return;
    }
    try {
      try {
        mBeanServer.registerMBean(this, objectName);
        LOGGER.debug(
            "Registered FMV Stream Monitor Helper MBean under object name: {}", objectName);
      } catch (InstanceAlreadyExistsException e) {
        mBeanServer.unregisterMBean(objectName);
        mBeanServer.registerMBean(this, objectName);
        LOGGER.debug("Re-registered FMV Stream Monitor Helper MBean", e);
      }
    } catch (MBeanRegistrationException
        | InstanceNotFoundException
        | InstanceAlreadyExistsException
        | NotCompliantMBeanException e) {
      LOGGER.info("Could not register MBean [{}].", objectName.toString(), e);
    }
  }

  public void destroy() {
    try {
      if (objectName != null && mBeanServer != null) {
        mBeanServer.unregisterMBean(objectName);
      }
    } catch (Exception e) {
      LOGGER.info("Exception unregistering MBean: ", e);
    }
  }
}
