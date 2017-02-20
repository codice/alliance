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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Collections;
import java.util.List;

import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.federation.FederationException;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

public class FindChildrenStreamEndPluginTest {

    private CatalogFramework catalogFramework;

    private Metacard parentMetacard;

    private Metacard metacard1;
    private Metacard metacard2;

    private Context context;

    private FindChildrenStreamEndPlugin findChildrenStreamEndPlugin;

    private FindChildrenStreamEndPlugin.Handler handler;

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

        QueryResponse queryResponse1 = mock(QueryResponse.class);
        when(queryResponse1.getHits()).thenReturn(2L);
        when(queryResponse1.getResults()).thenReturn(Collections.singletonList(result1));

        QueryResponse queryResponse2 = mock(QueryResponse.class);
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

        findChildrenStreamEndPlugin = new FindChildrenStreamEndPlugin(factory);

    }

    @Test
    public void testBatching()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException {

        context.isParentDirty().set(true);

        findChildrenStreamEndPlugin.streamEnded(context);

        ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(handler, times(2)).handle(Matchers.eq(context), Matchers.eq(parentMetacard), argumentCaptor.capture());
        verify(handler).end(Matchers.eq(context), Matchers.eq(parentMetacard));

        assertThat(argumentCaptor.getAllValues().get(0).get(0), is(metacard1));
        assertThat(argumentCaptor.getAllValues().get(1).get(0), is(metacard2));
        assertThat(context.isParentDirty().get(), is(false));

    }

    /**
     * Test that when the parent metcard is not dirty, that the catalog framework does not get called.
     */
    @Test
    public void testIsParentDirty()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException {

        context.isParentDirty().set(false);

        findChildrenStreamEndPlugin.streamEnded(context);

        verify(catalogFramework, times(0)).query(Matchers.any());

    }

}
