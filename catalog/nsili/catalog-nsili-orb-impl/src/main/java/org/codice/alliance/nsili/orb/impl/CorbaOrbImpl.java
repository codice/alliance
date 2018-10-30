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
package org.codice.alliance.nsili.orb.impl;

import ddf.catalog.util.impl.MaskableImpl;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.collections.MapUtils;
import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.codice.alliance.nsili.orb.api.CorbaServiceListener;
import org.codice.ddf.configuration.PropertyResolver;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorbaOrbImpl extends MaskableImpl implements CorbaOrb {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorbaOrbImpl.class);

  public static final String CORBA_TIMEOUT = "corbaTimeout";

  public static final String CORBA_PORT = "corbaPort";

  public static final String CORBA_HOST = "corbaHost";

  public static final String ORB_PERSISTENT_SERVER_PORT_PROPERTY =
      "com.sun.CORBA.POA.ORBPersistentServerPort";

  public static final String ORB_SERVER_PORT_PROPERTY = "com.sun.CORBA.ORBServerPort";

  public static final String ORB_TCP_READ_TIMEOUTS_PROPERTY =
      "com.sun.CORBA.transport.ORBTCPReadTimeouts";

  public static final String SUN_ORB_SERVER_HOST_PROPERTY = "com.sun.CORBA.ORBServerHost";

  public static final String SUN_ORB_SERVER_INITIAL_HOST_PROPERTY = "com.sun.CORBA.ORBInitialHost";

  private String corbaHost = "localhost";

  private int corbaPort;

  private int corbaTimeout;

  private ORB orb = null;

  private Thread orbRunThread = null;

  private Set<CorbaServiceListener> corbaServiceListeners = new HashSet<>(5);

  public String getCorbaHost() {
    return corbaHost;
  }

  public void setCorbaHost(String corbaHost) {
    PropertyResolver propertyResolver = new PropertyResolver(corbaHost);
    this.corbaHost = propertyResolver.getResolvedString();
  }

  public int getCorbaPort() {
    return corbaPort;
  }

  public void setCorbaPort(int corbaPort) {
    if (this.corbaPort != corbaPort) {
      this.corbaPort = corbaPort;
    }
  }

  public void setCorbaPort(String port) {
    PropertyResolver propertyResolver = new PropertyResolver(port);

    try {
      Integer corbaPort = Integer.parseInt(propertyResolver.getResolvedString());

      if (corbaPort != this.corbaPort) {
        setCorbaPort(corbaPort);
      }
    } catch (NumberFormatException e) {
      LOGGER.info(
          "Unable to parse specified port: {}. Not updating Corba port.",
          propertyResolver.getResolvedString());
    }
  }

  @Override
  public ORB getOrb() {
    return orb;
  }

  public void setCorbaTimeout(int corbaTimeout) {
    if (this.corbaTimeout != corbaTimeout) {
      this.corbaTimeout = corbaTimeout;
      System.setProperty(ORB_TCP_READ_TIMEOUTS_PROPERTY, getCorbaWaitTime());
    }
  }

  @Override
  public void shutdown() {
    if (orb != null) {
      for (CorbaServiceListener listener : corbaServiceListeners) {
        listener.corbaShutdown();
      }

      LOGGER.debug("Stopping ORB on port: {}", corbaPort);
      orb.shutdown(true);
      orb.destroy();
    }

    orbRunThread = null;
    orb = null;
  }

  public void refresh(Map<String, Object> configuration) {
    if (MapUtils.isEmpty(configuration)) {
      LOGGER.info("Received null or empty configuration during refresh.");
      return;
    }

    if (configuration.get(CORBA_TIMEOUT) instanceof Integer) {
      setCorbaTimeout((Integer) configuration.get(CORBA_TIMEOUT));
    }

    if (configuration.get(CORBA_PORT) instanceof String) {
      setCorbaPort((String) configuration.get(CORBA_PORT));
    }

    if (configuration.get(CORBA_HOST) instanceof String) {
      setCorbaHost((String) configuration.get(CORBA_HOST));
    }

    init();
  }

  /** Initializes the Corba ORB with no additional arguments */
  public void init() {
    shutdown();

    System.setProperty(ORB_PERSISTENT_SERVER_PORT_PROPERTY, String.valueOf(corbaPort));
    System.setProperty(ORB_SERVER_PORT_PROPERTY, String.valueOf(corbaPort));
    System.setProperty(ORB_TCP_READ_TIMEOUTS_PROPERTY, getCorbaWaitTime());

    Properties props = new Properties();
    props.put(SUN_ORB_SERVER_HOST_PROPERTY, corbaHost);
    props.put(SUN_ORB_SERVER_INITIAL_HOST_PROPERTY, corbaHost);
    orb = ORB.init(new String[0], props);
    if (orb != null) {
      LOGGER.debug(
          "Successfully initialized CORBA orb with hostname: {}, port: {}", corbaHost, corbaPort);
    } else {
      LOGGER.warn(
          "Unable to initialize CORBA orb with hostname: {}, port: {}", corbaHost, corbaPort);
    }

    orbRunThread = new Thread(() -> orb.run());
    orbRunThread.start();

    System.clearProperty(ORB_PERSISTENT_SERVER_PORT_PROPERTY);
    System.clearProperty(ORB_SERVER_PORT_PROPERTY);
    System.clearProperty(ORB_TCP_READ_TIMEOUTS_PROPERTY);

    for (CorbaServiceListener listener : corbaServiceListeners) {
      listener.corbaInitialized();
    }
  }

  @Override
  public void addCorbaServiceListener(CorbaServiceListener corbaServiceListener) {
    corbaServiceListeners.add(corbaServiceListener);
  }

  @Override
  public void removeCorbaServiceListener(CorbaServiceListener corbaServiceListener) {
    corbaServiceListeners.remove(corbaServiceListener);
  }

  private String getCorbaWaitTime() {
    long waitTimeMillis = corbaTimeout * 1000L;
    return "1:" + waitTimeMillis + ":" + waitTimeMillis + ":" + 1;
  }
}
