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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.opengis.filter.sort.SortOrder;

public class FindChildrenStreamEndPluginTest {

  private CatalogFramework catalogFramework;

  private Metacard parentMetacard;

  private Metacard metacard1;

  private Metacard metacard2;

  private Context context;

  private FindChildrenStreamEndPlugin findChildrenStreamEndPlugin;

  private FindChildrenStreamEndPlugin.Handler handler;

  private QueryResponse queryResponse1;

  private QueryResponse queryResponse2;

  @Before
  public void setup()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {
    catalogFramework = mock(CatalogFramework.class);

    parentMetacard = mock(Metacard.class);

    metacard1 = mock(Metacard.class);
    metacard2 = mock(Metacard.class);

    Result result1 = mock(Result.class);
    when(result1.getMetacard()).thenReturn(metacard1);
    Result result2 = mock(Result.class);
    when(result2.getMetacard()).thenReturn(metacard2);

    queryResponse1 = mock(QueryResponse.class);
    when(queryResponse1.getHits()).thenReturn(2L);
    when(queryResponse1.getResults()).thenReturn(Collections.singletonList(result1));

    queryResponse2 = mock(QueryResponse.class);
    when(queryResponse2.getHits()).thenReturn(2L);
    when(queryResponse2.getResults()).thenReturn(Collections.singletonList(result2));

    when(catalogFramework.query(any())).thenReturn(queryResponse1, queryResponse2);

    UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);
    when(udpStreamProcessor.getCatalogFramework()).thenReturn(catalogFramework);

    context = new Context(udpStreamProcessor);
    context.setParentMetacard(parentMetacard);

    handler = mock(FindChildrenStreamEndPlugin.Handler.class);

    FindChildrenStreamEndPlugin.Factory factory = mock(FindChildrenStreamEndPlugin.Factory.class);

    when(factory.build()).thenReturn(handler);

    findChildrenStreamEndPlugin =
        new FindChildrenStreamEndPlugin(new GeotoolsFilterBuilder(), factory);
  }

  /**
   * It is possible (in theory) that the catalog could return irregular sized batches. We want to
   * make sure this case is handled properly when mixed with intermittent exceptions. In this test,
   * we are going to return a total of 10 metacards with exceptions intermixed.
   */
  @Test
  public void testIrregularBatching()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {

    long expectedCount = 10;

    Metacard metacard1 = mock(Metacard.class);
    Metacard metacard2 = mock(Metacard.class);
    Metacard metacard3 = mock(Metacard.class);
    Metacard metacard4 = mock(Metacard.class);
    Metacard metacard5 = mock(Metacard.class);
    Metacard metacard6 = mock(Metacard.class);
    Metacard metacard7 = mock(Metacard.class);
    Metacard metacard8 = mock(Metacard.class);
    Metacard metacard9 = mock(Metacard.class);
    Metacard metacard10 = mock(Metacard.class);

    Result result1 = mock(Result.class);
    when(result1.getMetacard()).thenReturn(metacard1);
    Result result2 = mock(Result.class);
    when(result2.getMetacard()).thenReturn(metacard2);
    Result result3 = mock(Result.class);
    when(result3.getMetacard()).thenReturn(metacard3);
    Result result4 = mock(Result.class);
    when(result4.getMetacard()).thenReturn(metacard4);
    Result result5 = mock(Result.class);
    when(result5.getMetacard()).thenReturn(metacard5);
    Result result6 = mock(Result.class);
    when(result6.getMetacard()).thenReturn(metacard6);
    Result result7 = mock(Result.class);
    when(result7.getMetacard()).thenReturn(metacard7);
    Result result8 = mock(Result.class);
    when(result8.getMetacard()).thenReturn(metacard8);
    Result result9 = mock(Result.class);
    when(result9.getMetacard()).thenReturn(metacard9);
    Result result10 = mock(Result.class);
    when(result10.getMetacard()).thenReturn(metacard10);

    QueryResponse queryResponse1 = mock(QueryResponse.class);
    when(queryResponse1.getHits()).thenReturn(expectedCount);
    when(queryResponse1.getResults()).thenReturn(Arrays.asList(result1, result2));

    QueryResponse queryResponse2 = mock(QueryResponse.class);
    when(queryResponse2.getHits()).thenReturn(expectedCount);
    when(queryResponse2.getResults()).thenReturn(Arrays.asList(result3, result4, result5));

    QueryResponse queryResponse3 = mock(QueryResponse.class);
    when(queryResponse3.getHits()).thenReturn(expectedCount);
    when(queryResponse3.getResults())
        .thenReturn(Arrays.asList(result6, result7, result8, result9, result10));

    when(catalogFramework.query(any()))
        .thenThrow(SourceUnavailableException.class)
        .thenReturn(queryResponse1, queryResponse2)
        .thenThrow(SourceUnavailableException.class)
        .thenReturn(queryResponse3);

    setIsParentDirty(true);

    findChildrenStreamEndPlugin.streamEnded(context);

    ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

    verify(handler, times(3))
        .handle(Matchers.eq(context), Matchers.eq(parentMetacard), argumentCaptor.capture());
    verify(handler).end(Matchers.eq(context), Matchers.eq(parentMetacard));

    long capturedMetacardCount = argumentCaptor.getAllValues().stream().mapToLong(List::size).sum();

    assertThat(capturedMetacardCount, is(expectedCount));
  }

  /** Make sure that if we get too many subsequent errors that the code will bail out. */
  @Test(timeout = 10000)
  public void testSubsequentErrors()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {

    Throwable[] exceptions =
        new Throwable[FindChildrenStreamEndPlugin.MAX_SUBSEQUENT_ERROR_COUNT + 1];

    IntStream.range(0, FindChildrenStreamEndPlugin.MAX_SUBSEQUENT_ERROR_COUNT + 1)
        .forEach(i -> exceptions[i] = new SourceUnavailableException());

    when(catalogFramework.query(any())).thenThrow(exceptions);

    setIsParentDirty(true);

    findChildrenStreamEndPlugin.streamEnded(context);

    verify(handler, never())
        .handle(Matchers.eq(context), Matchers.eq(parentMetacard), any(List.class));
    verify(handler, times(1)).end(Matchers.eq(context), Matchers.eq(parentMetacard));
  }

  @Test
  public void testBatching()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {

    setIsParentDirty(true);

    findChildrenStreamEndPlugin.streamEnded(context);

    assertHandlerCalls(2, 0, 1, 2, 0, 1);
  }

  @Test
  public void testHandlerThrowsRuntimeException()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {

    when(catalogFramework.query(any()))
        .thenAnswer(
            invocationOnMock -> {
              QueryRequest queryRequest = invocationOnMock.getArgument(0);
              switch (queryRequest.getQuery().getStartIndex()) {
                case 1:
                  return queryResponse1;
                case 2:
                  return queryResponse2;
              }

              return null;
            });

    doThrow(new RuntimeException())
        .doNothing()
        .when(handler)
        .handle(Matchers.eq(context), Matchers.eq(parentMetacard), any(List.class));

    setIsParentDirty(true);

    findChildrenStreamEndPlugin.streamEnded(context);

    assertHandlerCalls(3, 1, 2, 3, 1, 2);
  }

  @Test
  public void testCatalogFrameworkThrowsUnsupportedQueryException()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {
    testCatalogFrameworkThrows(UnsupportedQueryException.class);
  }

  @Test
  public void testCatalogFrameworkThrowsSourceUnavailableException()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {
    testCatalogFrameworkThrows(SourceUnavailableException.class);
  }

  @Test
  public void testCatalogFrameworkThrowsFederationException()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {
    testCatalogFrameworkThrows(FederationException.class);
  }

  private void testCatalogFrameworkThrows(Class exceptionClass)
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {

    when(catalogFramework.query(any()))
        .thenThrow(exceptionClass)
        .thenReturn(queryResponse1, queryResponse2);

    setIsParentDirty(true);

    findChildrenStreamEndPlugin.streamEnded(context);

    assertHandlerCalls(3, 1, 2, 2, 0, 1);
  }

  /**
   * Assert the handler calls. Assumes the catalog framework should have two successful calls.
   * Assumes the handler should have two successful calls.
   *
   * @param queryCalls the number of expected query calls to the catalog framework (successful and
   *     exceptional calls)
   * @param firstGoodQuery of the captured query calls, this is the index value of the first good
   *     query (ie. an exception wasn't thrown)
   * @param secondGoodQuery of the captured query calls, this is the index value of the second good
   *     query (ie. an exception wasn't thrown)
   * @param handlerCalls the number of expected handler calls (successful and exceptional calls)
   * @param firstGoodHandler of the captured handlers calls, this is the index value of the first
   *     good handler call (ie. an exception wasn't thrown)
   * @param secondGoodHandler of the captured handlers calls, this is the index value of the second
   *     good handler call (ie. an exception wasn't thrown)
   * @throws UnsupportedQueryException
   * @throws SourceUnavailableException
   * @throws FederationException
   */
  private void assertHandlerCalls(
      int queryCalls,
      int firstGoodQuery,
      int secondGoodQuery,
      int handlerCalls,
      int firstGoodHandler,
      int secondGoodHandler)
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {
    ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

    verify(handler, times(handlerCalls))
        .handle(Matchers.eq(context), Matchers.eq(parentMetacard), argumentCaptor.capture());
    verify(handler).end(Matchers.eq(context), Matchers.eq(parentMetacard));

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);

    verify(catalogFramework, times(queryCalls)).query(queryRequestCaptor.capture());

    assertThat(
        queryRequestCaptor.getAllValues().get(firstGoodQuery).getQuery().getStartIndex(), is(1));
    assertThat(
        queryRequestCaptor.getAllValues().get(firstGoodQuery).getQuery().getPageSize(),
        is(FindChildrenStreamEndPlugin.BATCH_SIZE));
    assertThat(
        queryRequestCaptor
            .getAllValues()
            .get(firstGoodQuery)
            .getQuery()
            .requestsTotalResultsCount(),
        is(true));
    assertThat(
        queryRequestCaptor
            .getAllValues()
            .get(firstGoodQuery)
            .getQuery()
            .getSortBy()
            .getPropertyName()
            .getPropertyName(),
        is(Core.METACARD_CREATED));
    assertThat(
        queryRequestCaptor.getAllValues().get(firstGoodQuery).getQuery().getSortBy().getSortOrder(),
        is(SortOrder.ASCENDING));

    assertThat(
        queryRequestCaptor.getAllValues().get(secondGoodQuery).getQuery().getStartIndex(), is(2));
    assertThat(
        queryRequestCaptor.getAllValues().get(secondGoodQuery).getQuery().getPageSize(),
        is(FindChildrenStreamEndPlugin.BATCH_SIZE));
    assertThat(
        queryRequestCaptor
            .getAllValues()
            .get(secondGoodQuery)
            .getQuery()
            .requestsTotalResultsCount(),
        is(true));
    assertThat(
        queryRequestCaptor
            .getAllValues()
            .get(secondGoodQuery)
            .getQuery()
            .getSortBy()
            .getPropertyName()
            .getPropertyName(),
        is(Core.METACARD_CREATED));
    assertThat(
        queryRequestCaptor
            .getAllValues()
            .get(secondGoodQuery)
            .getQuery()
            .getSortBy()
            .getSortOrder(),
        is(SortOrder.ASCENDING));

    assertThat(argumentCaptor.getAllValues().get(firstGoodHandler).get(0), is(metacard1));
    assertThat(argumentCaptor.getAllValues().get(secondGoodHandler).get(0), is(metacard2));
    assertThat(context.modifyParentOrChild(AtomicBoolean::get), is(false));
  }

  /**
   * Test that when the parent metcard is not dirty, that the catalog framework does not get called.
   */
  @Test
  public void testIsParentDirty()
      throws UnsupportedQueryException, SourceUnavailableException, FederationException {

    setIsParentDirty(false);

    findChildrenStreamEndPlugin.streamEnded(context);

    verify(catalogFramework, never()).query(Matchers.any());
  }

  private void setIsParentDirty(boolean value) {
    context.modifyParentOrChild(
        isParentDirty -> {
          isParentDirty.set(value);
          return (Void) null;
        });
  }
}
