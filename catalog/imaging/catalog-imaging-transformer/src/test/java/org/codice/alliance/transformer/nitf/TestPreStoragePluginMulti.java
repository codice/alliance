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
import ddf.catalog.plugin.PluginExecutionException;

/**
 * Test the PreStoragePlugin with multiple content items.
 */
public class TestPreStoragePluginMulti {

    private static final String GEO_NITF = "/i_3001a.ntf";

    private NitfPreStoragePlugin nitfPreStoragePlugin;

    private CreateStorageRequest createStorageRequest;

    private UpdateStorageRequest updateStorageRequest;

    private ArgumentCaptor<Attribute> attributeArgumentCaptor;

    private ContentItem contentItem1;

    private ContentItem contentItem2;

    private Metacard metacard1;

    private Metacard metacard2;

    @Before
    public void setup() {
        nitfPreStoragePlugin = new NitfPreStoragePlugin();
        this.createStorageRequest = mock(CreateStorageRequest.class);
        this.updateStorageRequest = mock(UpdateStorageRequest.class);
        metacard1 = mock(Metacard.class);
        metacard2 = mock(Metacard.class);
        contentItem1 = mock(ContentItem.class);
        contentItem2 = mock(ContentItem.class);
        this.attributeArgumentCaptor = ArgumentCaptor.forClass(Attribute.class);
        List<ContentItem> contentItems = new ArrayList<>();
        contentItems.add(contentItem1);
        contentItems.add(contentItem2);
        when(createStorageRequest.getContentItems()).thenReturn(contentItems);
        when(updateStorageRequest.getContentItems()).thenReturn(contentItems);
    }

    @Test
    public void testCreateMultipleNitfContentItems() throws IOException, PluginExecutionException {

        makeNitf(contentItem1, metacard1);
        makeNitf(contentItem2, metacard2);

        nitfPreStoragePlugin.process(createStorageRequest);

        validateNitf(contentItem1, metacard1);
        validateNitf(contentItem2, metacard2);

    }

    @Test
    public void testCreateMultipleMixedContentItems() throws IOException, PluginExecutionException {

        makeNitf(contentItem1, metacard1);
        makeXml(contentItem2, metacard2);

        nitfPreStoragePlugin.process(createStorageRequest);

        validateNitf(contentItem1, metacard1);
        validateXml(contentItem2, metacard2);

    }

    @Test
    public void testUpdateMultipleNitfContentItems() throws IOException, PluginExecutionException {

        makeNitf(contentItem1, metacard1);
        makeNitf(contentItem2, metacard2);

        nitfPreStoragePlugin.process(updateStorageRequest);

        validateNitf(contentItem1, metacard1);
        validateNitf(contentItem2, metacard2);

    }

    @Test
    public void testUpdateMultipleMixedContentItems() throws IOException, PluginExecutionException {

        makeNitf(contentItem1, metacard1);
        makeXml(contentItem2, metacard2);

        nitfPreStoragePlugin.process(updateStorageRequest);

        validateNitf(contentItem1, metacard1);
        validateXml(contentItem2, metacard2);

    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }

    private void validateXml(ContentItem contentItem, Metacard metacard) {
        verify(contentItem, times(0)).getId();
        verify(metacard, times(0)).setAttribute(attributeArgumentCaptor.capture());
    }

    private void validateNitf(ContentItem contentItem, Metacard metacard) {
        verify(contentItem, times(1)).getId();
        verify(metacard, times(2)).setAttribute(attributeArgumentCaptor.capture());
        Attribute thumbnail1 = attributeArgumentCaptor.getAllValues()
                .get(0);
        Attribute overview1 = attributeArgumentCaptor.getAllValues()
                .get(1);
        assertThat(thumbnail1.getName(), is("thumbnail"));
        assertThat(thumbnail1.getValue(), is(notNullValue()));
        assertThat(overview1.getName(), is(Metacard.DERIVED_RESOURCE_URI));
        assertThat(overview1.getValue(), is(notNullValue()));
    }

    private void makeNitf(ContentItem contentItem, Metacard metacard) throws IOException {
        when(contentItem.getMetacard()).thenReturn(metacard);
        when(contentItem.getId()).thenReturn("101ABC");
        when(contentItem.getInputStream()).thenReturn(getInputStream(GEO_NITF));
        when(contentItem.getMimeTypeRawData()).thenReturn(NitfInputTransformer.MIME_TYPE.toString());
    }

    private void makeXml(ContentItem contentItem, Metacard metacard) throws IOException {
        when(contentItem.getMetacard()).thenReturn(metacard);
        when(contentItem.getId()).thenReturn("101ABC");
        when(contentItem.getInputStream()).thenReturn(new ByteArrayInputStream("<xml>...</xml>".getBytes()));
        when(contentItem.getMimeTypeRawData()).thenReturn("text/xml");
    }

}
