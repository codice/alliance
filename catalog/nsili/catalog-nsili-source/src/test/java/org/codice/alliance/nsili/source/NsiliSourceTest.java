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
package org.codice.alliance.nsili.source;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Metacard;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.filter.proxy.adapter.GeotoolsFilterAdapterImpl;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.UnsupportedQueryException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.AttributeType;
import org.codice.alliance.nsili.common.GIAS.CatalogMgr;
import org.codice.alliance.nsili.common.GIAS.DataModelMgr;
import org.codice.alliance.nsili.common.GIAS.Domain;
import org.codice.alliance.nsili.common.GIAS.HitCountRequest;
import org.codice.alliance.nsili.common.GIAS.Polarity;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.RequirementMode;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequest;
import org.codice.alliance.nsili.common.GIAS.View;
import org.codice.alliance.nsili.common.Nsili;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.ddf.cxf.SecureCxfClientFactory;
import org.codice.ddf.spatial.ogc.catalog.common.AvailabilityTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

public class NsiliSourceTest {

  private static final String ID = "mySTANAG";

  private static final String IOR_URL = "http://localhost:20002/data/ior.txt";

  private static final Integer POLL_INTERVAL = 1;

  private static final Integer MAX_HIT_COUNT = 250;

  private static final String GMTI = "GMTI";

  private static final String GMTI_EQ_FILTER =
      "((NSIL_FILE.format = 'GMTI') or (NSIL_STREAM.standard = 'GMTI')) and (not NSIL_PRODUCT:NSIL_CARD.status = 'OBSOLETE')";

  private static final String GMTI_LIKE_FILTER =
      "(GMTI like '%') and (not NSIL_PRODUCT:NSIL_CARD.status = 'OBSOLETE')";

  private static final String RELEVANCE = "RELEVANCE";

  private static final long LONG = 12L;

  private AvailabilityTask mockAvailabilityTask = mock(AvailabilityTask.class);

  private CatalogMgr catalogMgr = mock(CatalogMgr.class);

  private final GeotoolsFilterBuilder builder = new GeotoolsFilterBuilder();

  private NsiliSource source;

  private AttributeInformation[] attributeInformations = new AttributeInformation[0];

  private ORB orb;

  private Thread orbRunThread;

  private HashMap<String, List<AttributeInformation>> attributeInformationMap =
      getAttributeInformationMap();

  @Before
  public void setUp() throws Exception {
    orb = ORB.init(new String[0], null);
    orbRunThread = new Thread(() -> orb.run());
    orbRunThread.start();
    source = buildSource();
  }

  @After
  public void tearDown() {
    if (orb != null) {
      orb.destroy();
    }

    orb = null;
  }

  @Test
  public void testInitialContentList() {
    source.getContentTypes();
    assertThat(source.getContentTypes(), is(NsiliConstants.getContentTypes()));
  }

  @Test
  public void testIsAvailable() {
    source.setupAvailabilityPoll();
    assertThat(source.isAvailable(), is(true));
  }

  @Test
  public void testQuerySupportedAscendingSorting() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.CONTENT_TYPE).is().equalTo().text(GMTI));

    SortBy sortBy = new SortByImpl(Metacard.MODIFIED, SortOrder.ASCENDING);
    propertyIsLikeQuery.setSortBy(sortBy);

    source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<SortAttribute[]> argumentCaptor = ArgumentCaptor.forClass(SortAttribute[].class);
    verify(catalogMgr)
        .submit_query(
            any(Query.class),
            any(String[].class),
            argumentCaptor.capture(),
            any(NameValue[].class));

    String sortAttr = NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED;
    assertThat(argumentCaptor.getValue()[0].attribute_name, is(sortAttr));
    assertThat(argumentCaptor.getValue()[0].sort_polarity, is(Polarity.ASCENDING));
  }

  @Test
  public void testQuerySupportedDescendingSorting() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.CONTENT_TYPE).is().equalTo().text(GMTI));

    SortBy sortBy = new SortByImpl(Metacard.MODIFIED, SortOrder.DESCENDING);
    propertyIsLikeQuery.setSortBy(sortBy);

    source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<SortAttribute[]> argumentCaptor = ArgumentCaptor.forClass(SortAttribute[].class);
    verify(catalogMgr)
        .submit_query(
            any(Query.class),
            any(String[].class),
            argumentCaptor.capture(),
            any(NameValue[].class));

    String sortAttr = NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED;
    assertThat(argumentCaptor.getValue()[0].attribute_name, is(sortAttr));
    assertThat(argumentCaptor.getValue()[0].sort_polarity, is(Polarity.DESCENDING));
  }

  @Test
  public void testQueryUnsupportedSorting() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.CONTENT_TYPE).is().equalTo().text(GMTI));

    SortBy sortBy = new SortByImpl(RELEVANCE, SortOrder.DESCENDING);
    propertyIsLikeQuery.setSortBy(sortBy);

    source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<SortAttribute[]> argumentCaptor = ArgumentCaptor.forClass(SortAttribute[].class);
    verify(catalogMgr)
        .submit_query(
            any(Query.class),
            any(String[].class),
            argumentCaptor.capture(),
            any(NameValue[].class));

    assertThat(argumentCaptor.getValue().length, is(0));
  }

  @Test
  public void testQuerySortingNullSortableAttributes() throws Exception {

    source.setSortableAttributes(null);
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.CONTENT_TYPE).is().equalTo().text(GMTI));

    SortBy sortBy = new SortByImpl(RELEVANCE, SortOrder.DESCENDING);
    propertyIsLikeQuery.setSortBy(sortBy);

    source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<SortAttribute[]> argumentCaptor = ArgumentCaptor.forClass(SortAttribute[].class);
    verify(catalogMgr)
        .submit_query(
            any(Query.class),
            any(String[].class),
            argumentCaptor.capture(),
            any(NameValue[].class));

    // Length is 1, as we force a sort attribute if a valid one is not provided.
    assertThat(argumentCaptor.getValue().length, is(1));
  }

  @Test
  public void testQuerySortingNullSortBy() throws Exception {

    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.CONTENT_TYPE).is().equalTo().text(GMTI));

    SortBy sortBy = null;
    propertyIsLikeQuery.setSortBy(sortBy);

    source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<SortAttribute[]> argumentCaptor = ArgumentCaptor.forClass(SortAttribute[].class);
    verify(catalogMgr)
        .submit_query(
            any(Query.class),
            any(String[].class),
            argumentCaptor.capture(),
            any(NameValue[].class));

    // Sort attributes are always forced to be at least 1
    assertThat(argumentCaptor.getValue().length, is(1));
  }

  @Test
  public void testQueryResponseHitCount() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.ANY_TEXT).is().like().text("*"));
    SourceResponse sourceResponse = source.query(new QueryRequestImpl(propertyIsLikeQuery));
    assertThat(sourceResponse.getHits(), is(LONG));
  }

  @Test
  public void testQueryByContentType() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.CONTENT_TYPE).is().equalTo().text(GMTI));
    SourceResponse sourceResponse = source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);
    verify(catalogMgr)
        .submit_query(
            argumentCaptor.capture(),
            any(String[].class),
            any(SortAttribute[].class),
            any(NameValue[].class));
    assertThat(sourceResponse.getHits(), is(LONG));
    assertThat(argumentCaptor.getValue().bqs_query, is(GMTI_EQ_FILTER));
  }

  @Test
  public void testQueryAnyTextWildcardRepl() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.ANY_TEXT).is().like().text("*"));
    SourceResponse sourceResponse = source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);
    verify(catalogMgr)
        .submit_query(
            argumentCaptor.capture(),
            any(String[].class),
            any(SortAttribute[].class),
            any(NameValue[].class));
    assertThat(sourceResponse.getHits(), is(LONG));
    assertThat(argumentCaptor.getValue().bqs_query, is(GMTI_LIKE_FILTER));
  }

  @Test
  public void testQueryAnyText() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.ANY_TEXT).is().like().text("%"));
    SourceResponse sourceResponse = source.query(new QueryRequestImpl(propertyIsLikeQuery));
    ArgumentCaptor<Query> argumentCaptor = ArgumentCaptor.forClass(Query.class);
    verify(catalogMgr)
        .submit_query(
            argumentCaptor.capture(),
            any(String[].class),
            any(SortAttribute[].class),
            any(NameValue[].class));
    assertThat(sourceResponse.getHits(), is(LONG));
    assertThat(argumentCaptor.getValue().bqs_query, is(GMTI_LIKE_FILTER));
  }

  @Test(expected = UnsupportedQueryException.class)
  public void testBadQuery() throws Exception {
    QueryImpl propertyIsLikeQuery =
        new QueryImpl(builder.attribute(Metacard.ANY_TEXT).is().overlapping().last(LONG));
    source.query(new QueryRequestImpl(propertyIsLikeQuery));
  }

  @Test
  public void testRefreshWithNullConfiguration() throws Exception {
    NsiliSource source = buildSource();
    HashMap<String, Object> configuration = null;
    assertConfiguration(source);
    source.refresh(configuration);
    assertConfiguration(source);
  }

  @Test
  public void testRefreshWithEmptyConfiguration() throws Exception {
    NsiliSource source = buildSource();
    HashMap<String, Object> configuration = new HashMap<>();
    assertConfiguration(source);
    source.refresh(configuration);
    assertConfiguration(source);
  }

  @Test
  public void testRefresh() throws Exception {
    NsiliSource source = buildSource();
    HashMap<String, Object> configuration = new HashMap<>();

    configuration.put(NsiliSource.SERVER_USERNAME, GMTI);
    configuration.put(NsiliSource.SERVER_PASSWORD, GMTI);
    configuration.put(NsiliSource.KEY, GMTI);
    configuration.put(NsiliSource.IOR_URL, GMTI);
    configuration.put(NsiliSource.POLL_INTERVAL, 0);
    configuration.put(NsiliSource.MAX_HIT_COUNT, 0);
    configuration.put(NsiliSource.ID, GMTI);

    source.refresh(configuration);
    assertChangedConfiguration(source, GMTI, 0);
  }

  private NsiliSource buildSource() throws Exception {
    NsiliSource source;
    Nsili nsili = mock(Nsili.class);
    Response clientResponse = mock(Response.class);
    when(clientResponse.getEntity()).thenReturn("");
    InputStream mockInputStream = mock(InputStream.class);
    when(nsili.getIorFile()).thenReturn(mockInputStream);
    SecureCxfClientFactory factory = getMockFactory(nsili);

    HashMap<String, String[]> resultAttributes = new HashMap<>();
    HashMap<String, List<String>> sortableAttributes = generateMockSortableAttributes();

    source =
        Mockito.spy(
            new NsiliSource(
                factory,
                resultAttributes,
                sortableAttributes,
                new NsiliFilterDelegate(attributeInformationMap, NsiliConstants.NSIL_ALL_VIEW),
                orb));
    source.setIorUrl(IOR_URL);
    source.setServerUsername(NsiliSource.SERVER_USERNAME);
    source.setServerPassword(NsiliSource.SERVER_PASSWORD);
    source.setMaxHitCount(MAX_HIT_COUNT);
    source.setId(ID);
    source.setPollInterval(POLL_INTERVAL);
    source.setDataModelMgr(getMockDataModelMgr());
    source.setCatalogMgr(getMockCatalogMgr());
    source.setFilterAdapter(new GeotoolsFilterAdapterImpl());
    source.setNumberWorkerThreads(6);
    source.setAdditionalQueryParams("and (not NSIL_PRODUCT:NSIL_CARD.status = 'OBSOLETE')");

    // Suppress CORBA communications to test refresh
    doNothing().when(source).init();

    when(mockAvailabilityTask.isAvailable()).thenReturn(true);
    source.setAvailabilityTask(mockAvailabilityTask);
    return source;
  }

  private SecureCxfClientFactory getMockFactory(Nsili client) {
    SecureCxfClientFactory factory = mock(SecureCxfClientFactory.class);
    doReturn(client).when(factory).getClient();
    return factory;
  }

  private CatalogMgr getMockCatalogMgr() throws Exception {
    SubmitQueryRequest submitQueryRequest = mock(SubmitQueryRequest.class);
    HitCountRequest hitCountRequest = mock(HitCountRequest.class);

    doReturn(State.COMPLETED).when(hitCountRequest).complete(any(IntHolder.class));

    Status status = new Status();
    status.completion_state = State.COMPLETED;
    doReturn(status).when(hitCountRequest).get_status();

    when(hitCountRequest.complete(any(IntHolder.class)))
        .thenAnswer(
            (InvocationOnMock invocationOnMock) -> {
              IntHolder intHolder = (IntHolder) invocationOnMock.getArguments()[0];
              intHolder.value = 12;
              return State.COMPLETED;
            });

    when(submitQueryRequest.complete_DAG_results(any(DAGListHolder.class)))
        .thenAnswer(
            (InvocationOnMock invocationOnMock) -> {
              DAGListHolder dagListHolder = (DAGListHolder) invocationOnMock.getArguments()[0];
              dagListHolder.value = getMockDAGArray();
              return State.COMPLETED;
            });

    doReturn(submitQueryRequest)
        .when(catalogMgr)
        .submit_query(
            any(Query.class),
            any(String[].class),
            any(SortAttribute[].class),
            any(NameValue[].class));
    doReturn(hitCountRequest).when(catalogMgr).hit_count(any(Query.class), any(NameValue[].class));
    return catalogMgr;
  }

  private DataModelMgr getMockDataModelMgr() throws Exception {
    DataModelMgr dataModelMgr = mock(DataModelMgr.class);
    View[] views = new View[0];
    doReturn(attributeInformations)
        .when(dataModelMgr)
        .get_attributes(anyString(), any(NameValue[].class));
    doReturn(attributeInformations)
        .when(dataModelMgr)
        .get_queryable_attributes(anyString(), any(NameValue[].class));
    doReturn(views).when(dataModelMgr).get_view_names(any(NameValue[].class));

    return dataModelMgr;
  }

  private DAG[] getMockDAGArray() {
    return new DAG[0];
  }

  private HashMap<String, List<AttributeInformation>> getAttributeInformationMap() {
    HashMap<String, List<AttributeInformation>> map = new HashMap<>();
    List<AttributeInformation> list = new ArrayList<>();
    Domain domain = new Domain();
    domain.t(200);
    AttributeInformation attributeInformation =
        new AttributeInformation(
            GMTI,
            AttributeType.TEXT,
            domain,
            NsiliFilterDelegate.EMPTY_STRING,
            NsiliFilterDelegate.EMPTY_STRING,
            RequirementMode.OPTIONAL,
            NsiliFilterDelegate.EMPTY_STRING,
            false,
            true);
    list.add(attributeInformation);
    map.put(NsiliConstants.NSIL_ALL_VIEW, list);
    return map;
  }

  private HashMap<String, List<String>> generateMockSortableAttributes() {
    HashMap<String, List<String>> sortableAttributes = new HashMap<>();
    String declaredSortAttr = NsiliConstants.NSIL_FILE + "." + NsiliConstants.DATE_TIME_DECLARED;
    String modifiedSortAttr = NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED;
    sortableAttributes.put(
        NsiliConstants.NSIL_ALL_VIEW, Arrays.asList(declaredSortAttr, modifiedSortAttr));
    return sortableAttributes;
  }

  private void assertConfiguration(NsiliSource source) {
    assertThat(source.getId(), is(ID));
    assertThat(source.getServerUsername(), is(NsiliSource.SERVER_USERNAME));
    assertThat(source.getServerPassword(), is(NsiliSource.SERVER_PASSWORD));
    assertThat(source.getPollInterval(), is(POLL_INTERVAL));
    assertThat(source.getMaxHitCount(), is(MAX_HIT_COUNT));
    assertThat(source.getIorUrl(), is(IOR_URL));
  }

  private void assertChangedConfiguration(
      NsiliSource source, String newConstantString, int newConstantInteger) {
    assertThat(source.getId(), is(newConstantString));
    assertThat(source.getServerUsername(), is(newConstantString));
    assertThat(source.getServerPassword(), is(newConstantString));
    assertThat(source.getPollInterval(), is(newConstantInteger));
    assertThat(source.getMaxHitCount(), is(newConstantInteger));
    assertThat(source.getIorUrl(), is(newConstantString));
  }
}
