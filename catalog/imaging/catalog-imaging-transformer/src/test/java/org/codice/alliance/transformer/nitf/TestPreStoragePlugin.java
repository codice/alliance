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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.federation.FederationException;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

public class TestPreStoragePlugin {

    private static final String GEO_NITF = "/i_3001a.ntf";

    private NitfPreStoragePlugin nitfPreStoragePlugin = null;

    private CreateStorageRequest createStorageRequest = null;

    private UpdateStorageRequest updateStorageRequest = null;

    private Metacard metacard = null;

    private ContentItem contentItem = null;

    private ArgumentCaptor<Attribute> attributeArgumentCaptor = null;

    @Before
    public void setUp()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException,
            IOException {
        nitfPreStoragePlugin = new NitfPreStoragePlugin();

        this.createStorageRequest = mock(CreateStorageRequest.class);
        this.updateStorageRequest = mock(UpdateStorageRequest.class);
        this.metacard = mock(Metacard.class);
        this.contentItem = mock(ContentItem.class);
        this.attributeArgumentCaptor = ArgumentCaptor.forClass(Attribute.class);
        List<ContentItem> contentItems = new ArrayList<>();
        contentItems.add(contentItem);

        when(createStorageRequest.getContentItems()).thenReturn(contentItems);
        when(updateStorageRequest.getContentItems()).thenReturn(contentItems);
        when(contentItem.getMetacard()).thenReturn(metacard);
        when(contentItem.getId()).thenReturn("101ABC");
        when(contentItem.getInputStream()).thenReturn(getInputStream(GEO_NITF));
        when(contentItem.getMimeTypeRawData()).thenReturn(NitfInputTransformer.MIME_TYPE.toString());
    }

    @Test(expected = PluginExecutionException.class)
    public void testNullInputOnCreate() throws PluginExecutionException {
        nitfPreStoragePlugin.process((CreateStorageRequest) null);
    }

    @Test(expected = PluginExecutionException.class)
    public void testNullInputOnUpdate() throws PluginExecutionException {
        nitfPreStoragePlugin.process((UpdateStorageRequest) null);
        validate();
    }

    @Test
    public void testCreateStorageRequest() throws PluginExecutionException {
        nitfPreStoragePlugin.process(createStorageRequest);
        validate();
    }

    @Test
    public void testUpdateStorageRequest() throws PluginExecutionException {
        nitfPreStoragePlugin.process(updateStorageRequest);
        validate();
    }

    /**
     * Test that the plugin handles non-nitf content items
     *
     * @throws PluginExecutionException
     * @throws IOException
     */
    @Test
    public void testNonNitfContent() throws PluginExecutionException, IOException {
        try (InputStream inputStream = new ByteArrayInputStream("<xml>...</xml>".getBytes())) {
            when(contentItem.getMimeTypeRawData()).thenReturn("text/xml");
            when(contentItem.getInputStream()).thenReturn(inputStream);
            nitfPreStoragePlugin.process(updateStorageRequest);
            verify(metacard, times(0)).setAttribute(attributeArgumentCaptor.capture());
        }
    }


    private void validate() {
        verify(contentItem, times(1)).getId();
        verify(metacard, times(2)).setAttribute(attributeArgumentCaptor.capture());
        Attribute thumbnail = attributeArgumentCaptor.getAllValues()
                .get(0);
        Attribute overview = attributeArgumentCaptor.getAllValues()
                .get(1);
        assertThat(thumbnail.getName(), is("thumbnail"));
        assertThat(thumbnail.getValue(), is(notNullValue()));
        assertThat(overview.getName(), is(Metacard.DERIVED_RESOURCE_URI));
        assertThat(overview.getValue(), is(notNullValue()));
    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }
}