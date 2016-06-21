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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_IMPLEMENT;
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
import org.codice.alliance.nsili.common.GIAS.GetParametersRequest;
import org.codice.alliance.nsili.common.GIAS.GetRelatedFilesRequest;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SetAvailabilityRequest;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGHolder;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.common.UID.ProductHelper;
import org.codice.alliance.nsili.endpoint.managers.ProductMgrImpl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.security.Subject;
import ddf.security.service.SecurityServiceException;

public class TestProductMgrImpl extends TestNsiliCommon {

    private ProductMgrImpl productMgr;

    private Product testProduct = null;

    private String testMetacardId = UUID.randomUUID().toString().replaceAll("-", "");

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProductMgrImpl.class);

    @Before
    public void setUp() throws Exception {
        setupCommonMocks();
        setupProductMgrMocks();
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

        String managerId = UUID.randomUUID().toString();
        productMgr = new ProductMgrImpl(null);
        productMgr.setFilterBuilder(new GeotoolsFilterBuilder());
        productMgr.setSubject(mockSubject);
        productMgr.setCatalogFramework(mockCatalogFramework);

        if (!CorbaUtils.isIdActive(rootPOA,
                managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
            try {
                rootPOA.activate_object_with_id(managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                        productMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating ProductMgr: {}", e);
            }
        }

        rootPOA.create_reference_with_id(managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                        ProductMgrHelper.id());
    }

    @Test
    public void testIsAvailable() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        boolean avail = productMgr.is_available(product, null);
        assertThat(avail, is(false));

        avail = productMgr.is_available(null, null);
        assertThat(avail, is(false));
    }

    @Test
    public void testGetParameters() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        GetParametersRequest parametersRequest = productMgr.get_parameters(product, new String[]{"ALL"}, null);
        assertThat(parametersRequest, notNullValue());

        DAGHolder dagHolder = new DAGHolder();
        parametersRequest.complete(dagHolder);
        assertThat(dagHolder, notNullValue());
    }

    @Test
    public void testGetParametersNullDesiredParams() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        GetParametersRequest parametersRequest = productMgr.get_parameters(product, null, null);
        assertThat(parametersRequest, notNullValue());

        DAGHolder dagHolder = new DAGHolder();
        parametersRequest.complete(dagHolder);
        assertThat(dagHolder, notNullValue());
    }

    @Test
    public void testGetRelatedFileTypes() throws Exception {
        String[] relatedFileTypes = productMgr.get_related_file_types(testProduct);
        assertThat(relatedFileTypes, arrayContaining("THUMBNAIL"));
    }

    @Test
    public void testGetRelatedFiles() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        Product[] products = new Product[]{product};
        String userName = "";
        String password = "";
        String hostName = "localhost";
        String pathName = "/nsili/file";
        FileLocation location = new FileLocation(userName, password, hostName, pathName, null);
        NameValue[] props = new NameValue[0];

        GetRelatedFilesRequest request = productMgr.get_related_files(products, location, "THUMBNAIL", props);
        assertThat(request, notNullValue());
    }

    @Test
    public void testGetRelatedFilesWithPort() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        Product[] products = new Product[]{product};
        String userName = "";
        String password = "";
        String hostName = "localhost";
        String pathName = "/nsili/file";
        FileLocation location = new FileLocation(userName, password, hostName, pathName, null);
        NameValue[] props = new NameValue[1];
        Any portAny = orb.create_any();
        portAny.insert_string("2000");
        NameValue prop = new NameValue("PORT", portAny);
        props[0] = prop;

        GetRelatedFilesRequest request = productMgr.get_related_files(products, location, "THUMBNAIL", props);
        assertThat(request, notNullValue());
    }

    @Test
    public void testGetRelatedFilesWithBadPort() throws Exception {
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(testMetacardId);
        testMetacard.setTitle("JUnit Test Card");
        Result testResult = new ResultImpl(testMetacard);

        DAG dag = ResultDAGConverter.convertResult(testResult, orb, rootPOA, new ArrayList<>());
        Product product = ProductHelper.extract(dag.nodes[0].value);
        Product[] products = new Product[]{product};
        String userName = "";
        String password = "";
        String hostName = "localhost";
        String pathName = "/nsili/file";
        FileLocation location = new FileLocation(userName, password, hostName, pathName, null);
        NameValue[] props = new NameValue[1];
        Any portAny = orb.create_any();
        portAny.insert_string("NOPE");
        NameValue prop = new NameValue("PORT", portAny);
        props[0] = prop;

        GetRelatedFilesRequest request = productMgr.get_related_files(products, location, "THUMBNAIL", props);
        assertThat(request, notNullValue());
    }

    @Test
    public void testGetTimeout() throws Exception{
        int timeout = productMgr.get_timeout(null);
        assertThat(timeout, greaterThan(0));
    }

    @Test
    public void testGetDefaultTimeout() throws Exception {
        int timeout = productMgr.get_default_timeout();
        assertThat(timeout, greaterThan(0));
    }

    @Test
    public void testGetUseModes() throws Exception {
        String[] useModes = productMgr.get_use_modes();
        assertThat(useModes, notNullValue());
        assertThat(useModes, arrayContainingInAnyOrder("OrderAccess"));
    }

    @Test
    public void testQueryAvailDelay() throws Exception {
        int delay = productMgr.query_availability_delay(null, null, null);
        assertThat(delay, greaterThan(0));
    }

    @Test
    public void testNumberOfPriorities() throws Exception {
        int numPriorities = productMgr.get_number_of_priorities();
        assertThat(numPriorities, is(1));
    }

    @Test
    public void testGetActiveRequests() throws Exception {
        Request[] activeReqs = productMgr.get_active_requests();
        assertThat(activeReqs.length, is(0));
    }

    @Test
    public void testSetAvailability() throws Exception {
        SetAvailabilityRequest request = productMgr.set_availability(null, null, null, (short)1);
        assertThat(request, notNullValue());
    }

    @Test (expected = NO_IMPLEMENT.class)
    public void testGetPropertyNames() throws Exception {
        productMgr.get_property_names();
    }

    @Test (expected = NO_IMPLEMENT.class)
    public void testGetPropertyValues() throws Exception {
        productMgr.get_property_values(null);
    }

    @Test (expected = NO_IMPLEMENT.class)
    public void testGetLibraries() throws Exception {
        productMgr.get_libraries();
    }

    private void setupProductMgrMocks() throws Exception {
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
        if (orb != null) {
            orb.destroy();
        }

        if (orbRunThread != null) {
            orbRunThread.interrupt();
            orbRunThread = null;
        }

        library = null;
    }
}
