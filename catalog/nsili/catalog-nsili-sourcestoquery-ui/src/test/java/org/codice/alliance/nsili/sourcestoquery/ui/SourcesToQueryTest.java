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
package org.codice.alliance.nsili.sourcestoquery.ui;

import static org.codice.alliance.nsili.sourcestoquery.ui.service.SourcesToQuery.SOURCE_ID_TITLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.sourcestoquery.ui.service.SourcesToQuery;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import ddf.catalog.CatalogFramework;

public class SourcesToQueryTest {
    private CatalogFramework mockCatalogFramework = mock(CatalogFramework.class);

    private NsiliEndpoint mockNsiliEndpoint = mock(NsiliEndpoint.class);

    private SourcesToQuery sourcesToQuery;

    private static final String TEST_SOURCE_ID_1 = "testSourceId1";

    private static final String TEST_SOURCE_ID_2 = "testSourceId2";

    private static final String TEST_SOURCE_ID_3 = "testSourceId3";

    @Before
    public void setUp() {
        Set<String> testQueriedSourcesList = new HashSet<>();
        testQueriedSourcesList.add(TEST_SOURCE_ID_1);
        doReturn(testQueriedSourcesList).when(mockNsiliEndpoint)
                .getQuerySources();

        Set<String> testQueryableSourcesList = new HashSet<>();
        testQueryableSourcesList.add(TEST_SOURCE_ID_1);
        testQueryableSourcesList.add(TEST_SOURCE_ID_2);
        testQueryableSourcesList.add(TEST_SOURCE_ID_3);
        doReturn(testQueryableSourcesList).when(mockCatalogFramework)
                .getSourceIds();

        sourcesToQuery = new SourcesToQuery(mock(BundleContext.class),
                mockCatalogFramework,
                mockNsiliEndpoint);
    }

    @Test
    public void testQueriedSources() {
        Set<Map<String, String>> sourceIdMappedList = new HashSet<>();
        Map<String, String> map = new HashMap<>();
        map.put(SOURCE_ID_TITLE, TEST_SOURCE_ID_1);
        sourceIdMappedList.add(map);

        assertThat(sourcesToQuery.queriedSources(), is(sourceIdMappedList));
    }

    @Test
    public void testQueryableSources() {
        Set<Map<String, String>> expectedSet = new HashSet<>();
        Map<String, String> map2 = new HashMap<>();
        map2.put(SOURCE_ID_TITLE, TEST_SOURCE_ID_2);
        expectedSet.add(map2);
        Map<String, String> map3 = new HashMap<>();
        map3.put(SOURCE_ID_TITLE, TEST_SOURCE_ID_3);
        expectedSet.add(map3);

        assertThat(sourcesToQuery.queryableSources(), is(expectedSet));
    }

    @Test
    public void testAddQueriedSource() {
        sourcesToQuery.addQueriedSource(TEST_SOURCE_ID_1);
        verify(mockNsiliEndpoint, times(1)).addQuerySource(TEST_SOURCE_ID_1);
    }

    @Test
    public void testRemoveQueriedSource() {
        sourcesToQuery.addQueriedSource(TEST_SOURCE_ID_1);
        verify(mockNsiliEndpoint, times(1)).addQuerySource(TEST_SOURCE_ID_1);
    }
}
