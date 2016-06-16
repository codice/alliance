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
package org.codice.alliance.video.ui.service;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private ObjectName objectName;

    private MBeanServer mBeanServer;

    private BundleContext context;

    @Override
    public void callStartMonitoringStreamByServicePid(String servicePid) {
        Map<String, StreamMonitor> udpStreamMonitors = getUdpStreamMonitorServices();

        if (MapUtils.isEmpty(udpStreamMonitors)) {
            return;
        }

        udpStreamMonitors.entrySet()
                .stream()
                .filter(entry -> entry.getKey()
                        .equals(servicePid))
                .forEach(stringStreamMonitorEntry -> {
                    stringStreamMonitorEntry.getValue()
                            .startMonitoring();
                    LOGGER.debug("Started monitor for {}", stringStreamMonitorEntry.getKey());
                });
    }

    @Override
    public void callStopMonitoringStreamByServicePid(String servicePid) {
        Map<String, StreamMonitor> udpStreamMonitors = getUdpStreamMonitorServices();

        if (MapUtils.isEmpty(udpStreamMonitors)) {
            return;
        }

        udpStreamMonitors.entrySet()
                .stream()
                .filter(entry -> entry.getKey()
                        .equals(servicePid))
                .forEach(stringStreamMonitorEntry -> {
                    stringStreamMonitorEntry.getValue()
                            .stopMonitoring();
                    LOGGER.debug("Stopped monitor for {}", stringStreamMonitorEntry.getKey());
                });
    }

    @Override
    public List<Map<String, Object>> udpStreamMonitors() {
        Map<String, StreamMonitor> udpStreamMonitors = getUdpStreamMonitorServices();

        if (MapUtils.isEmpty(udpStreamMonitors)) {
            return null;
        }

        return udpStreamMonitors.entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof UdpStreamMonitor)
                .map(stringStreamMonitorEntry -> {
                    UdpStreamMonitor udpStreamMonitor =
                            (UdpStreamMonitor) stringStreamMonitorEntry.getValue();
                    Map<String, Object> map = new HashMap<>();
                    map.put(ID, stringStreamMonitorEntry.getKey());
                    map.put(UdpStreamMonitor.METATYPE_TITLE,
                            udpStreamMonitor.getTitle()
                                    .get());
                    map.put(UdpStreamMonitor.METATYPE_BYTE_COUNT_ROLLOVER_CONDITION,
                            udpStreamMonitor.getByteCountRolloverCondition());
                    map.put(UdpStreamMonitor.METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION,
                            udpStreamMonitor.getElapsedTimeRolloverCondition());
                    map.put(UdpStreamMonitor.METATYPE_MONITORED_ADDRESS,
                            udpStreamMonitor.getStreamUri()
                                    .get()
                                    .toString());
                    map.put(UdpStreamMonitor.METATYPE_METACARD_UPDATE_INITIAL_DELAY,
                            udpStreamMonitor.getMetacardUpdateInitialDelay());
                    map.put(MONITORING, udpStreamMonitor.isMonitoring());
                    map.put(START_TIME, udpStreamMonitor.getStartDateAsString());
                    map.put(UdpStreamMonitor.METATYPE_FILENAME_TEMPLATE,
                            udpStreamMonitor.getFileNameTemplate());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public void init() {
        registerMbean();
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public BundleContext getContext() {
        return this.context;
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
            LOGGER.warn("Unable to get service references for {}",
                    StreamMonitor.class.getName(),
                    e);
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
            LOGGER.error("Unable to create FMV Stream Monitor Helper MBean.", e);
        }
        if (mBeanServer == null) {
            return;
        }
        try {
            try {
                mBeanServer.registerMBean(this, objectName);
                LOGGER.info("Registered FMV Stream Monitor Helper MBean under object name: {}",
                        objectName.toString());
            } catch (InstanceAlreadyExistsException e) {
                mBeanServer.unregisterMBean(objectName);
                mBeanServer.registerMBean(this, objectName);
                LOGGER.info("Re-registered FMV Stream Monitor Helper MBean");
            }
        } catch (MBeanRegistrationException | InstanceNotFoundException |
                InstanceAlreadyExistsException | NotCompliantMBeanException e) {
            LOGGER.error("Could not register MBean [{}].", objectName.toString(), e);
        }

    }

    public void destroy() {
        try {
            if (objectName != null && mBeanServer != null) {
                mBeanServer.unregisterMBean(objectName);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception unregistering MBean: ", e);
        }
    }

}
