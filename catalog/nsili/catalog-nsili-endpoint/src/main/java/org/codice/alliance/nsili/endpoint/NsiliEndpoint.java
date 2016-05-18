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
package org.codice.alliance.nsili.endpoint;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.codice.ddf.security.handler.api.AuthenticationHandler;
import org.codice.ddf.security.handler.api.BaseAuthenticationToken;
import org.codice.ddf.security.handler.api.GuestAuthenticationToken;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;

import ddf.catalog.CatalogFramework;
import ddf.catalog.filter.FilterBuilder;
import ddf.security.Subject;
import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;

public class NsiliEndpoint {

    public static final String ENCODING = StandardCharsets.ISO_8859_1.name();

     public static final int DEFAULT_MAX_NUM_RESULTS = 500;

    public static final int DEFAULT_CORBA_PORT = 20003;

    private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";

    private int corbaPort = DEFAULT_CORBA_PORT;

    private int maxNumResults = DEFAULT_MAX_NUM_RESULTS;

    private ORB orb = null;

    private LibraryImpl library = null;

    private BundleContext context;

    private CatalogFramework framework;

    private String iorString;

    private Thread orbRunThread = null;

    private AuthenticationHandler securityHandler;

    private SecurityManager securityManager;

    private FilterBuilder filterBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliEndpoint.class);

    public NsiliEndpoint() {
        LOGGER.debug("NSILI Endpoint constructed");
    }

    public void shutdown() {
        if (orbRunThread != null) {
            orbRunThread.interrupt();
            orbRunThread = null;
        }

        if (orb != null) {
            LOGGER.debug("Attempting to stop ORB on port: {}", corbaPort);
            orb.destroy();
        }

        library = null;
    }

    public String getIorString() {
        return iorString;
    }

    public int getCorbaPort() {
        return corbaPort;
    }

    public void setCorbaPort(int corbaPort) {
        shutdown();
        this.corbaPort = corbaPort;
        init();
    }

    public int getMaxNumResults() {
        return maxNumResults;
    }

    public void setMaxNumResults(int maxNumResults) {
        this.maxNumResults = maxNumResults;
        if (library != null) {
            library.setMaxNumResults(maxNumResults);
        }
    }

    public void setSecurityHandler(AuthenticationHandler securityHandler) {
        this.securityHandler = securityHandler;
    }

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public CatalogFramework getFramework() {
        return framework;
    }

    public void setFramework(CatalogFramework framework) {
        this.framework = framework;
    }

    public void setFilterBuilder(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
    }

    public void init() {
        try {
            orb = getOrbForServer(corbaPort);
            orbRunThread = new Thread(() -> orb.run());
            orbRunThread.start();
            LOGGER.info("Started ORB on port: {}", corbaPort);
        } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
            LOGGER.warn("Unable to start the CORBA server. {}", NsilCorbaExceptionUtil.getExceptionDetails(e));
            LOGGER.debug("CORBA server startup exception details", e);
        } catch (IOException e) {
            LOGGER.warn("Unable to generate the IOR file.", e);
        } catch (SecurityServiceException e) {
            LOGGER.error("Unable to setup guest security credentials", e);
        }
    }

    public LibraryImpl getLibrary() {
        return library;
    }

    private ORB getOrbForServer(int port)
            throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException,
            SecurityServiceException {

        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", port);
        props.put("com.sun.CORBA.POA.ORBPersistentServerPort", port);
        final ORB orb = ORB.init(new String[0], props);

        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        rootPOA.the_POAManager()
                .activate();

        library = new LibraryImpl(rootPOA);
        library.setCatalogFramework(framework);
        Subject guestSubject = getGuestSubject();
        library.setGuestSubject(guestSubject);
        library.setFilterBuilder(filterBuilder);

        org.omg.CORBA.Object objref = rootPOA.servant_to_reference(library);

        iorString = orb.object_to_string(objref);

        return orb;

    }

    private Subject getGuestSubject() throws SecurityServiceException {
        String ip = DEFAULT_IP_ADDRESS;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.warn("Could not get IP address for localhost", e);
        }

        LOGGER.debug("Guest token ip: {}", ip);

        String guestTokenId = ip;
        GuestAuthenticationToken guestToken = new GuestAuthenticationToken(BaseAuthenticationToken.ALL_REALM, guestTokenId);
        return securityManager.getSubject(guestToken);
    }
}
