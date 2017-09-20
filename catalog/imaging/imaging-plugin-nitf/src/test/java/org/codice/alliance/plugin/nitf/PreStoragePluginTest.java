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
package org.codice.alliance.plugin.nitf;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PreStoragePluginTest {

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
    this.nitfPreStoragePlugin = new NitfPreStoragePlugin();

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
    when(contentItem.getInputStream()).thenAnswer(invocationOnMock -> getInputStream(GEO_NITF));
    when(contentItem.getMimeTypeRawData())
        .thenReturn(NitfPreStoragePlugin.NITF_MIME_TYPE.toString());
    when(contentItem.getSize()).thenReturn(5L * 1024L * 1024L);
  }

  @Test
  public void testRunTimeException() throws IOException, PluginExecutionException {
    NitfRenderer nitfRenderer = mock(NitfRenderer.class);

    NitfPreStoragePlugin nitfPreStoragePlugin =
        new NitfPreStoragePlugin() {
          @Override
          NitfRenderer getNitfRenderer() {
            return nitfRenderer;
          }
        };

    when(nitfRenderer.render(any(ImageSegment.class))).thenThrow(RuntimeException.class);
    CreateStorageRequest result = nitfPreStoragePlugin.process(createStorageRequest);
    assertThat(result.getContentItems(), is(createStorageRequest.getContentItems()));
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
  public void testTooLargeCreateStorageRequest() throws PluginExecutionException {
    nitfPreStoragePlugin.setMaxNitfSizeMB(4);
    nitfPreStoragePlugin.process(createStorageRequest);
    verify(metacard, never()).setAttribute(attributeArgumentCaptor.capture());
  }

  @Test
  public void testCreateStorageRequestNoOverview() throws PluginExecutionException {
    nitfPreStoragePlugin.setCreateOverview(false);
    nitfPreStoragePlugin.process(createStorageRequest);
    verify(metacard, times(2)).setAttribute(attributeArgumentCaptor.capture());
    assertThat(
        attributeArgumentCaptor.getAllValues().get(1).getValue().toString(),
        containsString("original"));
  }

  @Test
  public void testCreateStorageRequestNoOriginal() throws PluginExecutionException {
    nitfPreStoragePlugin.setStoreOriginalImage(false);
    nitfPreStoragePlugin.process(createStorageRequest);
    verify(metacard, times(2)).setAttribute(attributeArgumentCaptor.capture());
    assertThat(
        attributeArgumentCaptor.getAllValues().get(1).getValue().toString(),
        containsString("overview"));
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
    verify(contentItem, times(2)).getId();
    verify(metacard, times(3)).setAttribute(attributeArgumentCaptor.capture());
    Attribute thumbnail = attributeArgumentCaptor.getAllValues().get(0);
    Attribute overview = attributeArgumentCaptor.getAllValues().get(1);
    assertThat(thumbnail.getName(), is("thumbnail"));
    assertThat(thumbnail.getValue(), is(notNullValue()));
    assertThat(overview.getName(), is(Core.DERIVED_RESOURCE_URI));
    assertThat(overview.getValue(), is(notNullValue()));
  }

  private InputStream getInputStream(String filename) {
    assertNotNull("Test file missing", getClass().getResource(filename));
    return getClass().getResourceAsStream(filename);
  }
}
