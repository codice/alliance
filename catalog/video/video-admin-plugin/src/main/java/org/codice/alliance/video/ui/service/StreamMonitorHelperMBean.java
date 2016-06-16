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
package org.codice.alliance.video.ui.service;

import java.util.List;
import java.util.Map;

public interface StreamMonitorHelperMBean {

    /**
     * Calls {@link StreamMonitor#startMonitoring()} on the monitor with the given servicePid
     *
     * @param servicePid the servicePid of the monitor
     */
    void callStartMonitoringStreamByServicePid(String servicePid);

    /**
     * Calls {@link StreamMonitor#stopMonitoring()} on the monitor with the given servicePid
     *
     * @param servicePid the servicePid of the monitor
     */
    void callStopMonitoringStreamByServicePid(String servicePid);

    /**
     * Gets all the existing {@link org.codice.alliance.video.stream.mpegts.UdpStreamMonitor}s and their properties
     *
     * @return a list of {@link org.codice.alliance.video.stream.mpegts.UdpStreamMonitor}s
     */
    List<Map<String, Object>> udpStreamMonitors();

}
