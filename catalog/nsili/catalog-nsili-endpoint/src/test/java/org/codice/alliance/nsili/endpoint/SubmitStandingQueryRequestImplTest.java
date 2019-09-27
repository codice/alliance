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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.codice.alliance.nsili.common.BqsConverter;
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.GIAS.CreationMgrHelper;
import org.codice.alliance.nsili.common.GIAS.DayEvent;
import org.codice.alliance.nsili.common.GIAS.DayEventTime;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.LifeEvent;
import org.codice.alliance.nsili.common.GIAS.LifeEventType;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.QueryLifeSpan;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.StringDAGListHolder;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.endpoint.requests.SubmitStandingQueryRequestImpl;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.StringHolder;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.opengis.filter.Filter;

public class SubmitStandingQueryRequestImplTest extends NsiliTestCommon {

  private SubmitStandingQueryRequestImpl standingQueryRequest;

  private Callback mockCallback = mock(Callback.class);

  private Callback mockCallback2 = mock(Callback.class);

  private CatalogFramework mockFramework = mock(CatalogFramework.class);

  private FilterBuilder filterBuilder = new GeotoolsFilterBuilder();

  @Before
  public void setUp() throws Exception {
    setupCommonMocks();
    setupMocks();
    setupOrb();
    setupStandingQueryRequest();
  }

  @Test
  public void testRegisterCallback() throws Exception {
    String callbackId = standingQueryRequest.register_callback(mockCallback);
    assertThat(callbackId, notNullValue());
  }

  @Test
  public void testFreeCallback() throws Exception {
    String cbId = standingQueryRequest.register_callback(mockCallback2);
    standingQueryRequest.free_callback(cbId);

    cbId = standingQueryRequest.register_callback(mockCallback2);
    standingQueryRequest.freeCallback(mockCallback2);
  }

  @Test
  public void testGetRequestManager() throws SystemFault, ProcessingFault {
    RequestManager requestManager = standingQueryRequest.get_request_manager();
    assertThat(requestManager, notNullValue());
  }

  @Test
  public void testDayEvent() {
    String dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.END_OF_MONTH);
    assertThat(dayEvent, containsString("END"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.FIRST_OF_MONTH);
    assertThat(dayEvent, containsString("FIRST"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.SUN);
    assertThat(dayEvent, containsString("SUN"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.MON);
    assertThat(dayEvent, containsString("MON"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.TUE);
    assertThat(dayEvent, containsString("TUE"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.WED);
    assertThat(dayEvent, containsString("WED"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.THU);
    assertThat(dayEvent, containsString("THU"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.FRI);
    assertThat(dayEvent, containsString("FRI"));

    dayEvent = SubmitStandingQueryRequestImpl.getDayEvent(DayEvent.SAT);
    assertThat(dayEvent, containsString("SAT"));
  }

  @Test
  public void testGetLifeEventDiscriminator() {
    String lifeEvent =
        SubmitStandingQueryRequestImpl.getLifeEventDiscriminator(LifeEventType.DAY_EVENT_TIME);
    assertThat(lifeEvent, containsString("DAY"));

    lifeEvent = SubmitStandingQueryRequestImpl.getLifeEventDiscriminator(LifeEventType.NAMED_EVENT);
    assertThat(lifeEvent, containsString("NAMED"));

    lifeEvent =
        SubmitStandingQueryRequestImpl.getLifeEventDiscriminator(LifeEventType.ABSOLUTE_TIME);
    assertThat(lifeEvent, containsString("ABS"));

    lifeEvent =
        SubmitStandingQueryRequestImpl.getLifeEventDiscriminator(LifeEventType.RELATIVE_TIME);
    assertThat(lifeEvent, containsString("REL"));
  }

  @Test
  public void testComplete()
      throws SystemFault, ProcessingFault, UnsupportedQueryException, SourceUnavailableException,
          FederationException {
    DAGListHolder results = new DAGListHolder();
    standingQueryRequest.complete_DAG_results(results);
    assertThat(results, notNullValue());

    DAG[] dagResults = results.value;
    assertThat(dagResults.length, is(2));
  }

  @Test
  public void convertTimeToMillis() {
    Time ucoTime = new Time((short) 01, (short) 01, (short) 01);
    long millis = SubmitStandingQueryRequestImpl.convertTimeToMillis(ucoTime);
    long checkMillis = (60 * 60 * 1000) + (60 * 1000) + 1000;
    assertThat(millis, is(checkMillis));
  }

  @Test
  public void convertDayEventTime() {
    Time time = new Time((short) 15, (short) 30, 25);
    DayEventTime dayEventTime = new DayEventTime();
    dayEventTime.time = time;
    dayEventTime.day_event = DayEvent.END_OF_MONTH;
    Date testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.FIRST_OF_MONTH;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.MON;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.TUE;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.WED;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.THU;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.FRI;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.SAT;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());

    dayEventTime.day_event = DayEvent.SUN;
    testDate = SubmitStandingQueryRequestImpl.convertDayEventTime(dayEventTime);
    assertThat(testDate, notNullValue());
  }

  @Test
  public void testGetDate() {
    Time time = new Time((short) 15, (short) 30, 25);
    DayEventTime dayEventTime = new DayEventTime();
    dayEventTime.time = time;
    dayEventTime.day_event = DayEvent.END_OF_MONTH;

    LifeEvent lifeEvent = new LifeEvent();
    lifeEvent.day_event(dayEventTime);
    Date date = SubmitStandingQueryRequestImpl.getDate(lifeEvent);
    assertThat(date, notNullValue());

    lifeEvent = new LifeEvent();
    lifeEvent.rt(time);
    date = SubmitStandingQueryRequestImpl.getDate(lifeEvent);
    assertThat(date, notNullValue());

    org.codice.alliance.nsili.common.UCO.Date ucoDate =
        new org.codice.alliance.nsili.common.UCO.Date((short) 2016, (short) 5, (short) 15);
    AbsTime absTime = new AbsTime();
    absTime.aDate = ucoDate;
    absTime.aTime = time;
    lifeEvent = new LifeEvent();
    lifeEvent.at(absTime);
    date = SubmitStandingQueryRequestImpl.getDate(lifeEvent);
    assertThat(date, notNullValue());

    lifeEvent = new LifeEvent();
    lifeEvent.ev("test");
    date = SubmitStandingQueryRequestImpl.getDate(lifeEvent);
    assertThat(date, nullValue());
  }

  @Test(expected = NO_IMPLEMENT.class)
  public void testCompleteStringDAGResults() throws SystemFault, ProcessingFault {
    StringDAGListHolder resultHolder = new StringDAGListHolder();
    standingQueryRequest.complete_stringDAG_results(resultHolder);
  }

  @Test(expected = NO_IMPLEMENT.class)
  public void testCompleteXMLResults() throws SystemFault, ProcessingFault {
    StringHolder results = new StringHolder();
    standingQueryRequest.complete_XML_results(results);
  }

  @Test
  public void testSetUserInfo() throws InvalidInputParameter, SystemFault, ProcessingFault {
    String testUser = "test";
    standingQueryRequest.set_user_info(testUser);
    RequestDescription requestDescription = standingQueryRequest.get_request_description();
    assertThat(testUser, is(requestDescription.user_info));
  }

  @Test
  public void testPauseResume() throws SystemFault, ProcessingFault {
    standingQueryRequest.pause();
    Status status = standingQueryRequest.get_status();
    assertThat(status.completion_state, is(State.SUSPENDED));
    standingQueryRequest.resume();
    status = standingQueryRequest.get_status();
    assertThat(status.completion_state, is(State.PENDING));
  }

  @Test
  public void testExecutionTimes() throws SystemFault, ProcessingFault {
    AbsTime lastExec = standingQueryRequest.get_time_last_executed();
    assertThat(lastExec, notNullValue());

    AbsTime nextExec = standingQueryRequest.get_time_next_execution();
    assertThat(nextExec, notNullValue());

    DelayEstimate delayEstimate = standingQueryRequest.get_remaining_delay();
    assertThat(delayEstimate, notNullValue());
  }

  @Test
  public void testGetId() {
    String id = standingQueryRequest.getId();
    assertThat(id, notNullValue());
  }

  @Test
  public void testSetNumberOfHits() throws InvalidInputParameter, SystemFault, ProcessingFault {
    standingQueryRequest.set_number_of_hits(250);
    int pageSize = standingQueryRequest.getPageSize();
    assertThat(pageSize, is(250));
  }

  @Test
  public void testCancel()
      throws SystemFault, ProcessingFault, InvalidInputParameter, WrongPolicy, ServantAlreadyActive,
          ObjectAlreadyActive {
    standingQueryRequest.cancel();
    Status status = standingQueryRequest.get_status();
    assertThat(status.completion_state, is(State.CANCELED));
    setupStandingQueryRequest();
  }

  @Test
  public void testGetNumHits() throws SystemFault, ProcessingFault {
    int hits = standingQueryRequest.get_number_of_hits();
    assertThat(hits, is(0));
  }

  @Test
  public void testGetNumHitsInInterval()
      throws InvalidInputParameter, SystemFault, ProcessingFault {
    int hits = standingQueryRequest.get_number_of_hits_in_interval(1);
    assertThat(hits, is(0));
  }

  @Test
  public void testGetNumIntervals() throws SystemFault, ProcessingFault {
    int intervals = standingQueryRequest.get_number_of_intervals();
    assertThat(intervals, is(0));
  }

  @Test
  public void testClearMethods() throws InvalidInputParameter, SystemFault, ProcessingFault {
    standingQueryRequest.clear_all();
    standingQueryRequest.clear_intervals(1);
    Time time = new Time((short) 05, (short) 05, 05);
    standingQueryRequest.clear_before(time);
  }

  @Test
  public void testIsNewer() {
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    when(attr1.getValue()).thenReturn(new Date(2000));
    when(attr2.getValue()).thenReturn(new Date(1000));
    Metacard mc1 = mock(Metacard.class);
    Metacard mc2 = mock(Metacard.class);
    when(mc1.getAttribute(MetacardVersion.VERSIONED_ON)).thenReturn(attr1);
    when(mc2.getAttribute(MetacardVersion.VERSIONED_ON)).thenReturn(attr2);
    Result result1 = mock(Result.class);
    Result result2 = mock(Result.class);
    when(result1.getMetacard()).thenReturn(mc1);
    when(result2.getMetacard()).thenReturn(mc2);
    assertThat(SubmitStandingQueryRequestImpl.isNewer(null, null), is(true));
    assertThat(SubmitStandingQueryRequestImpl.isNewer(null, result1), is(false));
    assertThat(SubmitStandingQueryRequestImpl.isNewer(result1, null), is(true));
    assertThat(SubmitStandingQueryRequestImpl.isNewer(result1, result2), is(true));
    assertThat(SubmitStandingQueryRequestImpl.isNewer(result2, result1), is(false));
    assertThat(SubmitStandingQueryRequestImpl.isNewer(result2, result2), is(false));
  }

  private void setupMocks() throws Exception {
    List<Result> results = getTestResults();
    BqsConverter bqsConverter = new BqsConverter(filterBuilder, true);
    Filter filter = bqsConverter.convertBQSToDDF(getQuery());
    ddf.catalog.operation.Query query = new QueryImpl(filter);
    QueryResponse testResult =
        new QueryResponseImpl(new QueryRequestImpl(query), results, results.size());
    when(mockFramework.query(any(QueryRequest.class))).thenReturn(testResult);
  }

  private Query getQuery() {
    Query query = new Query();
    query.bqs_query = "NSIL_CARD.identifier like '%'";
    query.view = NsiliConstants.NSIL_ALL_VIEW;
    return query;
  }

  private QueryLifeSpan getEmptyLifespan() {
    QueryLifeSpan lifeSpan = new QueryLifeSpan();
    return lifeSpan;
  }

  private void setupStandingQueryRequest()
      throws InvalidInputParameter, SystemFault, ProcessingFault, WrongPolicy, ServantAlreadyActive,
          ObjectAlreadyActive {
    Query query = getQuery();
    String[] resultAttributes = new String[0];
    SortAttribute[] sortAttributes = new SortAttribute[0];
    QueryLifeSpan lifespan = getEmptyLifespan();
    NameValue[] properties = new NameValue[0];
    // Set artificially low for for test cases.
    long defaultUpdateFrequencyMsec = 2000;
    int maxPendingResults = 10000;
    long maxWaitToStartTimeMsecs = TimeUnit.MINUTES.toMillis(5);
    standingQueryRequest =
        new SubmitStandingQueryRequestImpl(
            query,
            resultAttributes,
            sortAttributes,
            lifespan,
            properties,
            mockFramework,
            filterBuilder,
            defaultUpdateFrequencyMsec,
            null,
            maxPendingResults,
            true,
            false,
            maxWaitToStartTimeMsecs);
    standingQueryRequest.register_callback(mockCallback2);

    String managerId = UUID.randomUUID().toString();
    rootPOA.activate_object_with_id(
        managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), standingQueryRequest);
    rootPOA.create_reference_with_id(
        managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), CreationMgrHelper.id());
  }

  private List<Result> getTestResults() {
    return getHistoryTestResults();
  }
}
