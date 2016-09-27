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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.codice.alliance.core.email.EmailSender;
import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import ddf.security.service.SecurityServiceException;

public class NsiliWebEndpointTest extends NsiliCommonTest {

    private static final int TEST_CORBA_PORT = 20011;

    private NsiliEndpoint nsiliEndpoint;

    private NsiliWebEndpoint nsiliWebEndpoint;

    private CorbaOrb mockCorbaOrb = mock(CorbaOrb.class);

    @Before
    public void setUp()
            throws SecurityServiceException, AdapterInactive, InvalidName, ServantNotActive,
            WrongPolicy, IOException {
        setupCommonMocks();
        setupOrb();
        doReturn(orb).when(mockCorbaOrb).getOrb();
        createNsiliEndpoint();
        createWebEndpoint();
    }

    @Test
    public void testIORFile() {
        Response response = nsiliWebEndpoint.getIorFile();
        assertThat(response.getEntity(), notNullValue());

        String responseStr = response.getEntity().toString();
        assertThat(responseStr, containsString(nsiliEndpoint.getIorString()));
    }

    @After
    public void tearDown() {
        if (orb != null) {
            orb.destroy();
        }

        orb = null;
    }

    private void createNsiliEndpoint() {
        nsiliEndpoint = new NsiliEndpoint();
        nsiliEndpoint.setSecurityManager(securityManager);
        nsiliEndpoint.setCorbaOrb(mockCorbaOrb);
        nsiliEndpoint.setEmailSender(mock(EmailSender.class));
        nsiliEndpoint.init();
    }

    private void createWebEndpoint() {
        nsiliWebEndpoint = new NsiliWebEndpoint(nsiliEndpoint);
    }
}
