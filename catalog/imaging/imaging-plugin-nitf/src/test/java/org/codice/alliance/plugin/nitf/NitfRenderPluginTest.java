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
package org.codice.alliance.plugin.nitf;

import static org.codice.alliance.plugin.nitf.NitfRenderPlugin.ORIGINAL;
import static org.codice.alliance.plugin.nitf.NitfRenderPlugin.OVERVIEW;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.codice.ddf.catalog.async.data.api.internal.ProcessCreateItem;
import org.codice.ddf.catalog.async.data.api.internal.ProcessDeleteItem;
import org.codice.ddf.catalog.async.data.api.internal.ProcessItem;
import org.codice.ddf.catalog.async.data.api.internal.ProcessRequest;
import org.codice.ddf.catalog.async.data.api.internal.ProcessResource;
import org.codice.ddf.catalog.async.data.api.internal.ProcessUpdateItem;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import ddf.catalog.plugin.PluginExecutionException;

public class NitfRenderPluginTest {

    public static final String TEST_ID = "101ABC";

    private ArgumentCaptor<Attribute> attributeArgumentCaptor =
            ArgumentCaptor.forClass(Attribute.class);

    NitfRenderPlugin nitfRenderPlugin;

    @Before
    public void setUp() {
        nitfRenderPlugin = new NitfRenderPlugin();
    }

    @Test
    public void testRunTimeException()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        NitfRenderer nitfRenderer = mock(NitfRenderer.class);
        when(nitfRenderer.render(any(ImageSegment.class))).thenThrow(RuntimeException.class);
        NitfRenderPlugin nitfRenderPlugin = new NitfRenderPlugin() {
            @Override
            NitfRenderer getNitfRenderer() {
                return nitfRenderer;
            }
        };

        ProcessCreateItem processCreateItem = createProcessCreateItem(createXmlProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, false, false, false);
    }

    // processCreate tests

    @Test(expected = PluginExecutionException.class)
    public void testNullInputOnCreate() throws PluginExecutionException {
        nitfRenderPlugin.processCreate(null);
    }

    @Test
    public void testCreateNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, true, true, true);
    }

    @Test
    public void testCreateNonNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessCreateItem processCreateItem = createProcessCreateItem(createXmlProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, false, false, false);
    }

    @Test
    public void testCreateMultipleNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessCreateItem processCreateItem1 = createProcessCreateItem(createNitfProcessResource());
        ProcessCreateItem processCreateItem2 = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem1,
                processCreateItem2);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem1, true, true, true);
        validateImagesStored(processCreateItem2, true, true, true);
    }

    @Test
    public void testCreateMultipleMixedResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessCreateItem processCreateItem1 = createProcessCreateItem(createNitfProcessResource());
        ProcessCreateItem processCreateItem2 = createProcessCreateItem(createXmlProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem1,
                processCreateItem2);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem1, true, true, true);
        validateImagesStored(processCreateItem2, false, false, false);
    }

    private static ProcessCreateItem createProcessCreateItem(ProcessResource processResource) {
        ProcessCreateItem processCreateItem = mock(ProcessCreateItem.class);
        Metacard mockMetacard = mock(Metacard.class);
        when(mockMetacard.getId()).thenReturn(TEST_ID);
        when(processCreateItem.getMetacard()).thenReturn(mockMetacard);
        when(processCreateItem.getProcessResource()).thenReturn(processResource);
        return processCreateItem;
    }

    // processUpdate tests

    @Test(expected = PluginExecutionException.class)
    public void testNullInputOnUpdate() throws PluginExecutionException {
        nitfRenderPlugin.processUpdate(null);
    }

    @Test
    public void testUpdateNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessUpdateItem processUpdateItem = createProcessUpdateItem(createNitfProcessResource());
        ProcessRequest<ProcessUpdateItem> processUpdateRequest = createProcessRequest(
                processUpdateItem);

        // when:
        nitfRenderPlugin.processUpdate(processUpdateRequest);

        // then:
        validateImagesStored(processUpdateItem, true, true, true);
    }

    @Test
    public void testUpdateNonNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessUpdateItem processUpdateItem = createProcessUpdateItem(createXmlProcessResource());
        ProcessRequest<ProcessUpdateItem> processUpdateRequest = createProcessRequest(
                processUpdateItem);

        // when:
        nitfRenderPlugin.processUpdate(processUpdateRequest);

        // then:
        validateImagesStored(processUpdateItem, false, false, false);
    }

    @Test
    public void testUpdateMultipleNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessUpdateItem processUpdateItem1 = createProcessUpdateItem(createNitfProcessResource());
        ProcessUpdateItem processUpdateItem2 = createProcessUpdateItem(createNitfProcessResource());
        ProcessRequest<ProcessUpdateItem> processUpdateRequest = createProcessRequest(
                processUpdateItem1,
                processUpdateItem2);

        // when:
        nitfRenderPlugin.processUpdate(processUpdateRequest);

        // then:
        validateImagesStored(processUpdateItem1, true, true, true);
        validateImagesStored(processUpdateItem2, true, true, true);
    }

    @Test
    public void testUpdateMultipleMixedResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessUpdateItem processUpdateItem1 = createProcessUpdateItem(createNitfProcessResource());
        ProcessUpdateItem processUpdateItem2 = createProcessUpdateItem(createXmlProcessResource());
        ProcessRequest<ProcessUpdateItem> processUpdateRequest = createProcessRequest(
                processUpdateItem1,
                processUpdateItem2);

        // when:
        nitfRenderPlugin.processUpdate(processUpdateRequest);

        // then:
        validateImagesStored(processUpdateItem1, true, true, true);
        validateImagesStored(processUpdateItem2, false, false, false);
    }

    private static ProcessUpdateItem createProcessUpdateItem(ProcessResource processResource) {
        ProcessUpdateItem processUpdateItem = mock(ProcessUpdateItem.class);
        Metacard mockNewMetacard = mock(Metacard.class);
        when(mockNewMetacard.getId()).thenReturn(TEST_ID);
        when(processUpdateItem.getMetacard()).thenReturn(mockNewMetacard);
        when(processUpdateItem.getOldMetacard()).thenReturn(mock(Metacard.class));
        when(processUpdateItem.getProcessResource()).thenReturn(processResource);
        return processUpdateItem;
    }

    // processDelete test
    
    @Test(expected = PluginExecutionException.class)
    public void testNullInputOnDelete() throws PluginExecutionException {
        nitfRenderPlugin.processDelete(null);
    }

    @Test
    public void testDeleteNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessDeleteItem processDeleteItem = createProcessDeleteItem();
        ProcessRequest<ProcessDeleteItem> processDeleteRequest = createProcessRequest(processDeleteItem);

        // when:
        nitfRenderPlugin.processDelete(processDeleteRequest);

        // then:
        validateImagesStored(processDeleteItem, false, false, false);
    }

    @Test
    public void testDeleteNonNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessDeleteItem processDeleteItem = createProcessDeleteItem();
        ProcessRequest<ProcessDeleteItem> processDeleteRequest = createProcessRequest(
                processDeleteItem);

        // when:
        nitfRenderPlugin.processDelete(processDeleteRequest);

        // then:
        validateImagesStored(processDeleteItem, false, false, false);
    }

    @Test
    public void testDeleteMultipleNitfProcessResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessDeleteItem processDeleteItem1 = createProcessDeleteItem();
        ProcessDeleteItem processDeleteItem2 = createProcessDeleteItem();
        ProcessRequest<ProcessDeleteItem> processDeleteRequest = createProcessRequest(
                processDeleteItem1,
                processDeleteItem2);

        // when:
        nitfRenderPlugin.processDelete(processDeleteRequest);

        // then:
        validateImagesStored(processDeleteItem1, false, false, false);
        validateImagesStored(processDeleteItem2, false, false, false);
    }

    @Test
    public void testDeleteMultipleMixedResourceItems()
            throws IOException, PluginExecutionException, ParseException, NitfFormatException {
        // given:
        ProcessDeleteItem processDeleteItem1 = createProcessDeleteItem();
        ProcessDeleteItem processDeleteItem2 = createProcessDeleteItem();
        ProcessRequest<ProcessDeleteItem> processDeleteRequest = createProcessRequest(
                processDeleteItem1,
                processDeleteItem2);

        // when:
        nitfRenderPlugin.processDelete(processDeleteRequest);

        // then:
        validateImagesStored(processDeleteItem1, false, false, false);
        validateImagesStored(processDeleteItem2, false, false, false);
    }

    private static ProcessDeleteItem createProcessDeleteItem() {
        ProcessDeleteItem processDeleteItem = mock(ProcessDeleteItem.class);
        Metacard mockNewMetacard = mock(Metacard.class);
        when(mockNewMetacard.getId()).thenReturn(TEST_ID);
        when(processDeleteItem.getMetacard()).thenReturn(mockNewMetacard);
        return processDeleteItem;
    }

    // metatype settings tests

    @Test
    public void testSetMaxNitfSize()
            throws PluginExecutionException, IOException, ParseException, NitfFormatException {
        // given:
        nitfRenderPlugin.setMaxNitfSizeMB(4);
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, false, false, false);
    }

    @Test
    public void testSetNoOverview()
            throws PluginExecutionException, IOException, ParseException, NitfFormatException {
        // given:
        nitfRenderPlugin.setCreateOverview(false);
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, true, false, true);
    }

    @Test
    public void testSetNoOriginal()
            throws PluginExecutionException, IOException, ParseException, NitfFormatException {
        // given:
        nitfRenderPlugin.setStoreOriginalImage(false);
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, true, true, false);
    }

    @Test
    public void testSetNoOverviewAndNoOriginal()
            throws PluginExecutionException, IOException, ParseException, NitfFormatException {
        // given:
        nitfRenderPlugin.setCreateOverview(false);
        nitfRenderPlugin.setStoreOriginalImage(false);
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, true, false, false);
    }

    @Test
    public void testSetOverviewMaxSideLength()
            throws PluginExecutionException, IOException, ParseException, NitfFormatException {
        // given:
        nitfRenderPlugin.setOverviewMaxSideLength(4);
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, true, true, true);
    }

    @Test
    public void testSetInvalidOverviewMaxSideLength()
            throws PluginExecutionException, IOException, ParseException, NitfFormatException {
        // given:
        nitfRenderPlugin.setOverviewMaxSideLength(-325);
        ProcessCreateItem processCreateItem = createProcessCreateItem(createNitfProcessResource());
        ProcessRequest<ProcessCreateItem> processCreateRequest = createProcessRequest(
                processCreateItem);

        // when:
        nitfRenderPlugin.processCreate(processCreateRequest);

        // then:
        validateImagesStored(processCreateItem, true, true, true);
    }

    private void validateImagesStored(ProcessItem processResourceItem,
            boolean thumbnailRendered, boolean overviewRendered, boolean originalRendered)
            throws ParseException, NitfFormatException, IOException {
        int expectedImagesCount = getTrueCount(thumbnailRendered,
                overviewRendered,
                originalRendered);

        verify(processResourceItem.getMetacard(), times(expectedImagesCount)).setAttribute(
                attributeArgumentCaptor.capture());

        if (expectedImagesCount == 0) {
            // TODO check that the image is never rendered
            return;
        }

        final List<Attribute> setAttributes = attributeArgumentCaptor.getAllValues();

        // thumbnail
        if (thumbnailRendered) {
            final Attribute thumbnailAttribute = setAttributes.get(0);
            assertThat(thumbnailAttribute.getName(), is(Core.THUMBNAIL));
            assertThat(thumbnailAttribute.getValue(), notNullValue());
        }

        // overview
        if (overviewRendered) {
            final Attribute overviewAttribute = setAttributes.get(1);
            assertThat(overviewAttribute.getName(), is(Core.DERIVED_RESOURCE_URI));
            assertThat(overviewAttribute.getValue(), is("content:" + TEST_ID + "#" + OVERVIEW));
        }

        //original
        if (originalRendered) {
            final Attribute originalAttribute = setAttributes.get(setAttributes.size() - 1);
            assertThat(originalAttribute.getName(), is(Core.DERIVED_RESOURCE_URI));
            assertThat(originalAttribute.getValue(), is("content:" + TEST_ID + "#" + ORIGINAL));
        }
    }

    private static int getTrueCount(boolean... bools) {
        int count = 0;
        for (boolean bool : bools) {
            count += (bool ? 1 : 0);
        }
        return count;
    }

    private ProcessResource createNitfProcessResource() throws IOException {
        ProcessResource mockProcessResource = mock(ProcessResource.class);
        when(mockProcessResource.getMimeType()).thenReturn(NitfRenderPlugin.NITF_MIME_TYPE.toString());
        when(mockProcessResource.getSize()).thenReturn(5L * 1024L * 1024L);

        final String filename = "/i_3001a.ntf";
        assertNotNull("Test file missing", getClass().getResource(filename));
        when(mockProcessResource.getInputStream()).thenAnswer(invocationOnMock -> getClass().getResourceAsStream(
                filename));

        return mockProcessResource;
    }

    private static ProcessResource createXmlProcessResource() throws IOException {
        ProcessResource mockProcessResource = mock(ProcessResource.class);
        when(mockProcessResource.getMimeType()).thenReturn("text/xml");
        when(mockProcessResource.getInputStream()).thenAnswer(invocationOnMock -> new ByteArrayInputStream(
                "<xml>...</xml>".getBytes()));

        return mockProcessResource;
    }

    private static <T extends ProcessItem> ProcessRequest<T> createProcessRequest(
            T... processResourceItems) {
        ProcessRequest processRequest = mock(ProcessRequest.class);
        when(processRequest.getProcessItems()).thenReturn(Arrays.stream(processResourceItems)
                .collect(Collectors.toList()));
        return processRequest;
    }
}