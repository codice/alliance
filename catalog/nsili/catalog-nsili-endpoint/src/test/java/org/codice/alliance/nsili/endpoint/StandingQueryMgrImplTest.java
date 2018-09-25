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
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.security.service.SecurityServiceException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.GIAS.Event;
import org.codice.alliance.nsili.common.GIAS.LifeEvent;
import org.codice.alliance.nsili.common.GIAS.LifeEventType;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.QueryLifeSpan;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.StandingQueryMgrHelper;
import org.codice.alliance.nsili.common.GIAS.SubmitStandingQueryRequest;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.Date;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.endpoint.managers.AccessManagerImpl;
import org.codice.alliance.nsili.endpoint.managers.StandingQueryMgrImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandingQueryMgrImplTest extends NsiliTestCommon {

  private CatalogFramework mockCatalogFramework = mock(CatalogFramework.class);

  private StandingQueryMgrImpl standingQueryMgr;

  private String bqsQuery = "NSIL_CARD.identifier like '%'";

  private Product testProduct = null;

  private String testMetacardId = UUID.randomUUID().toString();

  private static final Logger LOGGER = LoggerFactory.getLogger(StandingQueryMgrImplTest.class);

  @Before
  public void setUp() throws Exception {
    setupCommonMocks();
    setupStandingQueryMgrMocks();
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
    standingQueryMgr = new StandingQueryMgrImpl(null, null, null);
    standingQueryMgr.setFilterBuilder(new GeotoolsFilterBuilder());
    standingQueryMgr.setCatalogFramework(mockCatalogFramework);
    standingQueryMgr.setDefaultUpdateFrequencyMsec(60000);
    standingQueryMgr.setMaxWaitToStartTimeMsecs(TimeUnit.MINUTES.toMillis(5));
    standingQueryMgr.setMaxPendingResults(10000);

    if (!CorbaUtils.isIdActive(
        rootPOA, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        rootPOA.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), standingQueryMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.error("Error activating StandingQueryMgr: {}", e);
      }
    }

    rootPOA.create_reference_with_id(
        managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), StandingQueryMgrHelper.id());
  }

  @Test
  public void testGetEventDescriptions() throws SystemFault, ProcessingFault {
    Event[] events = standingQueryMgr.get_event_descriptions();
    assertThat(events, notNullValue());
    assertThat(events.length, is(3));
  }

  @Test
  public void testSubmitStandingQuery() throws InvalidInputParameter, SystemFault, ProcessingFault {
    String[] resultAttributes = new String[0];
    SortAttribute[] sortAttributes = new SortAttribute[0];
    LifeEvent start = new LifeEvent();
    start.at(
        LifeEventType.ABSOLUTE_TIME,
        new AbsTime(
            new Date((short) 2016, (short) 05, (short) 01),
            new Time((short) 00, (short) 00, (short) 00)));
    LifeEvent stop = new LifeEvent();
    stop.at(
        LifeEventType.ABSOLUTE_TIME,
        new AbsTime(
            new Date((short) 2050, (short) 05, (short) 01),
            new Time((short) 00, (short) 00, (short) 00)));
    LifeEvent frequency1 = new LifeEvent();
    frequency1.rt(LifeEventType.RELATIVE_TIME, new Time((short) 00, (short) 01, (short) 00));
    LifeEvent[] frequency = new LifeEvent[] {frequency1};
    QueryLifeSpan lifeSpan = new QueryLifeSpan(start, stop, frequency);
    Query query = new Query(NsiliConstants.NSIL_ALL_VIEW, bqsQuery);
    SubmitStandingQueryRequest request =
        standingQueryMgr.submit_standing_query(
            query, resultAttributes, sortAttributes, lifeSpan, new NameValue[0]);
    assertThat(request, notNullValue());
  }

  @Test
  public void testGetTimeout() throws InvalidInputParameter, SystemFault, ProcessingFault {
    int timeout = standingQueryMgr.get_timeout(null);
    assertThat(timeout, is(AccessManagerImpl.DEFAULT_TIMEOUT));
  }

  @Test
  public void testGetDefaultTimeout() throws SystemFault, ProcessingFault {
    int timeout = standingQueryMgr.get_default_timeout();
    assertThat(timeout, is(AccessManagerImpl.DEFAULT_TIMEOUT));
  }

  @Test
  public void testGetActiveRequests() throws SystemFault, ProcessingFault {
    Request[] activeReqs = standingQueryMgr.get_active_requests();
    assertThat(activeReqs.length, is(0));
  }

  @Test(expected = NO_IMPLEMENT.class)
  public void testGetPropertyNames() throws SystemFault, ProcessingFault {
    standingQueryMgr.get_property_names();
  }

  @Test(expected = NO_IMPLEMENT.class)
  public void testGetPropertyValues() throws InvalidInputParameter, SystemFault, ProcessingFault {
    standingQueryMgr.get_property_values(null);
  }

  @Test(expected = NO_IMPLEMENT.class)
  public void testGetLibraries() throws SystemFault, ProcessingFault {
    standingQueryMgr.get_libraries();
  }

  private void setupStandingQueryMgrMocks()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {
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
