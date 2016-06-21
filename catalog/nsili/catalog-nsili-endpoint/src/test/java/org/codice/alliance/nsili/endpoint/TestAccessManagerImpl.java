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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.codice.alliance.nsili.endpoint.managers.AccessManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.common.UID.ProductHelper;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.security.Subject;
import ddf.security.service.SecurityServiceException;

public class TestAccessManagerImpl extends TestNsiliCommon {

    private AccessManagerImpl accessManager;

    private Query testQuery;

    private String bqsQuery = "NSIL_CARD.identifier like '%'";

    private Product testProduct = null;

    private String testMetacardId = UUID.randomUUID().toString().replaceAll("-", "");;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAccessManagerImpl.class);

    @Before
    public void setUp() throws Exception {
        setupCommonMocks();
        setupAccessMgrMocks();
        try {
            setupOrb();
            orbRunThread = new Thread(() -> orb.run());
            orbRunThread.start();
        } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
            LOGGER.error("Unable to start the CORBA server", e);
        } catch (IOException e) {
            LOGGER.error("Unable to generate the IOR file", e);
        } catch (SecurityServiceException e) {
            LOGGER.error("Unable to setup guest security credentials", e);
        }

        testQuery = new Query(NsiliConstants.NSIL_ALL_VIEW, bqsQuery);

        String managerId = UUID.randomUUID().toString();
        accessManager = new AccessManagerImpl();
        accessManager.setFilterBuilder(new GeotoolsFilterBuilder());
        accessManager.setSubject(mockSubject);
        accessManager.setCatalogFramework(mockCatalogFramework);

        if (!CorbaUtils.isIdActive(rootPOA,
                managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
            try {
                rootPOA.activate_object_with_id(managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                        accessManager);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating ProductMgr: {}", e);
            }
        }

        rootPOA.create_reference_with_id(managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                ProductMgrHelper.id());
    }

    @Test
    public void testIsAvailableNoURL() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        boolean avail = accessManager.is_available(product, null);
        assertThat(avail, is(false));

        avail = accessManager.is_available(null, null);
        assertThat(avail, is(false));
    }

    @Test
    public void testIsAvailableWithBadURL() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        testMetacard.setAttribute(new AttributeImpl(Metacard.RESOURCE_DOWNLOAD_URL, "http://localhost:20000/not/present"));
        Result testResult = new ResultImpl(testMetacard);

        List<Result> results = new ArrayList<>();
        results.add(testResult);
        QueryResponse testResponse = new QueryResponseImpl(null, results, results.size());
        when(mockCatalogFramework.query(any(QueryRequest.class))).thenReturn(testResponse);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        boolean avail = accessManager.is_available(product, null);
        assertThat(avail, is(false));

        avail = accessManager.is_available(null, null);
        assertThat(avail, is(false));
    }

    @Test
    public void testIsUrlValidBadUrls() throws IOException {
        boolean valid = accessManager.isUrlValid(null);
        assertThat(valid, is(false));

        valid = accessManager.isUrlValid("http://localhost:2000/not/present");
        assertThat(valid, is(false));

    }

    @Test
    public void testQueryAvailDelay() throws Exception {
        int delay = accessManager.query_availability_delay(null, null, null);
        assertThat(delay, greaterThan(-1));
    }

    @Test
    public void testGetNumberOfPriorities() throws Exception {
        int numPriorities = accessManager.get_number_of_priorities();
        assertThat(numPriorities, is(1));
    }

    @Test
    public void testGetActiveRequests() throws Exception {
        Request[] requests = accessManager.get_active_requests();
        assertThat(requests.length, is(0));
    }

    @Test
    public void testGetDefaultTimeout() throws Exception {
        int defaultTimeout = accessManager.get_default_timeout();
        assertThat(defaultTimeout, is(-1));
    }

    @Test
    public void testGetTimeout() throws Exception {
        int timeout = accessManager.get_timeout(null);
        assertThat(timeout, is(-1));
    }

    private void setupAccessMgrMocks() throws Exception {
        int testTotalHits = 5;
        List<Result> results = new ArrayList<>(testTotalHits);
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        Result testResult = new ResultImpl(testMetacard);
        results.add(testResult);
        QueryResponse testResponse = new QueryResponseImpl(null, results, testTotalHits);
        when(mockCatalogFramework.query(any(QueryRequest.class))).thenReturn(testResponse);
    }

    @After
    public void tearDown() {
        if (orbRunThread != null) {
            orbRunThread.interrupt();
            orbRunThread = null;
        }

        if (orb != null) {
            orb.destroy();
        }

        library = null;
    }
}
