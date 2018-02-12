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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.operation.impl.ResourceResponseImpl;
import ddf.catalog.operation.impl.UpdateImpl;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.resource.Resource;
import ddf.catalog.resource.impl.ResourceImpl;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.codice.alliance.imaging.nitf.api.NitfParserService;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class NitfPostIngestPluginTest {

  private static final String GEO_NITF = "/i_3001a.ntf";

  private NitfPostIngestPlugin nitfPostIngestPlugin = null;

  private CatalogFramework catalogFramework = null;

  private NitfParserService nitfParserService = null;

  private CreateResponse createResponse = null;

  private CreateRequest createRequest = null;

  private UpdateResponse updateResponse = null;

  private UpdateRequest updateRequest = null;

  private MetacardImpl metacard = null;

  private Map<String, Serializable> requestProperties;

  private ArgumentCaptor<UpdateStorageRequest> updateStorageCaptor;

  private ArgumentCaptor<UpdateRequest> updateMetacardCaptor;

  @Before
  public void setUp() throws Exception {

    this.catalogFramework = mock(CatalogFramework.class);
    this.nitfParserService = mock(NitfParserService.class);

    this.nitfPostIngestPlugin = new NitfPostIngestPlugin();
    this.nitfPostIngestPlugin.setCatalogFramework(catalogFramework);
    this.nitfPostIngestPlugin.setNitfParserService(nitfParserService);

    this.updateStorageCaptor = ArgumentCaptor.forClass(UpdateStorageRequest.class);
    this.updateMetacardCaptor = ArgumentCaptor.forClass(UpdateRequest.class);

    this.createResponse = mock(CreateResponse.class);
    this.createRequest = mock(CreateRequest.class);
    this.updateResponse = mock(UpdateResponse.class);
    this.updateRequest = mock(UpdateRequest.class);
    this.requestProperties = new HashMap<>();
    this.metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Core.ID, "123456"));
    metacard.setAttribute(new AttributeImpl(Core.RESOURCE_SIZE, "1048576"));
    metacard.setAttribute(new AttributeImpl(Media.TYPE, NitfPostIngestPlugin.IMAGE_NITF));
    when(createResponse.getCreatedMetacards()).thenReturn(Collections.singletonList(metacard));
    when(createResponse.getRequest()).thenReturn(createRequest);
    when(createRequest.getProperties()).thenReturn(requestProperties);
    Update update = new UpdateImpl(metacard, metacard);
    when(updateResponse.getUpdatedMetacards()).thenReturn(Collections.singletonList(update));
    when(updateResponse.getRequest()).thenReturn(updateRequest);
    when(updateRequest.getProperties()).thenReturn(requestProperties);
    when(catalogFramework.getLocalResource(any(ResourceRequest.class)))
        .thenAnswer(invocationOnMock -> getInputStream(GEO_NITF));

    when(nitfParserService.parseNitf(any(InputStream.class), any()))
        .thenAnswer(
            invocationOnMock ->
                new NitfParserInputFlowImpl()
                    .inputStream(invocationOnMock.getArgument(0))
                    .allData());
  }

  @Test
  public void testRunTimeException() throws Exception {
    NitfRenderer nitfRenderer = mock(NitfRenderer.class);

    NitfPostIngestPlugin npip =
        new NitfPostIngestPlugin() {
          @Override
          NitfRenderer getNitfRenderer() {
            return nitfRenderer;
          }
        };
    npip.setCatalogFramework(catalogFramework);
    npip.setNitfParserService(nitfParserService);
    when(nitfRenderer.render(any(ImageSegment.class))).thenThrow(RuntimeException.class);
    npip.process(createResponse);
    verify(catalogFramework, never()).update(any(UpdateStorageRequest.class));
    verify(catalogFramework, never()).update(any(UpdateRequest.class));
  }

  @Test(expected = PluginExecutionException.class)
  public void testNullInputOnCreate() throws PluginExecutionException {
    nitfPostIngestPlugin.process((CreateResponse) null);
  }

  @Test(expected = PluginExecutionException.class)
  public void testNullInputOnUpdate() throws PluginExecutionException {
    nitfPostIngestPlugin.process((UpdateResponse) null);
  }

  @Test
  public void testCreateResponse() throws Exception {
    nitfPostIngestPlugin.process(createResponse);
    validate();
  }

  @Test
  public void testTooLargeCreateResponse() throws Exception {
    nitfPostIngestPlugin.setMaxNitfSizeMB(0);
    nitfPostIngestPlugin.process(createResponse);
    assertThat(metacard.getThumbnail(), is(nullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(nullValue()));
  }

  @Test
  public void testCreateResponseNoOverview() throws Exception {
    nitfPostIngestPlugin.setCreateOverview(false);
    nitfPostIngestPlugin.process(createResponse);
    assertThat(metacard.getThumbnail(), is(notNullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(notNullValue()));
    assertThat(
        metacard.getAttribute(Core.DERIVED_RESOURCE_URI).getValue().toString(),
        containsString("original"));
    verify(catalogFramework, times(1)).update(updateStorageCaptor.capture());
    assertThat(
        updateStorageCaptor.getValue().getContentItems().get(0).getQualifier(), is("original"));
  }

  @Test
  public void testCreateResponseNoOriginal() throws Exception {
    nitfPostIngestPlugin.setStoreOriginalImage(false);
    nitfPostIngestPlugin.process(createResponse);
    assertThat(metacard.getThumbnail(), is(notNullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(notNullValue()));
    assertThat(
        metacard.getAttribute(Core.DERIVED_RESOURCE_URI).getValue().toString(),
        containsString("overview"));
    verify(catalogFramework, times(1)).update(updateStorageCaptor.capture());
    assertThat(
        updateStorageCaptor.getValue().getContentItems().get(0).getQualifier(), is("overview"));
  }

  @Test
  public void testCreateThumbnailOnly() throws Exception {
    nitfPostIngestPlugin.setCreateOverview(false);
    nitfPostIngestPlugin.setStoreOriginalImage(false);
    nitfPostIngestPlugin.process(createResponse);
    assertThat(metacard.getThumbnail(), is(notNullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(nullValue()));
    verify(catalogFramework, never()).update(updateStorageCaptor.capture());
    verify(catalogFramework, times(1)).update(updateMetacardCaptor.capture());
    assertThat(
        updateMetacardCaptor.getValue().getUpdates().get(0).getValue().getThumbnail(),
        is(notNullValue()));
  }

  @Test
  public void testUpdateResponse() throws Exception {
    nitfPostIngestPlugin.process(updateResponse);
    validate();
  }

  @Test
  public void testNonNitfContent() throws Exception {
    metacard.setAttribute(new AttributeImpl(Media.TYPE, "text/xml"));
    nitfPostIngestPlugin.process(updateResponse);
    assertThat(metacard.getThumbnail(), is(nullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(nullValue()));
  }

  @Test
  public void testNoMediaType() throws Exception {
    metacard.setAttribute(Media.TYPE, null);
    nitfPostIngestPlugin.process(updateResponse);
    assertThat(metacard.getThumbnail(), is(nullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(nullValue()));
  }

  @Test
  public void testAlreadyProcessedRequest() throws Exception {
    requestProperties.put("NitfPostIngestPlugin.Processed", true);
    nitfPostIngestPlugin.process(createResponse);
    assertThat(metacard.getThumbnail(), is(nullValue()));
    assertThat(metacard.getAttribute(Core.DERIVED_RESOURCE_URI), is(nullValue()));
    verify(catalogFramework, never()).update(updateStorageCaptor.capture());
    verify(catalogFramework, never()).update(updateMetacardCaptor.capture());
  }

  @Test
  public void testMultipleWithBadNitf() throws Exception {

    NitfRenderer nitfRenderer = mock(NitfRenderer.class);

    NitfPostIngestPlugin npip =
        new NitfPostIngestPlugin() {
          @Override
          NitfRenderer getNitfRenderer() {
            return nitfRenderer;
          }
        };
    npip.setCatalogFramework(catalogFramework);
    npip.setNitfParserService(nitfParserService);
    when(nitfRenderer.render(any(ImageSegment.class)))
        .thenThrow(RuntimeException.class)
        .thenCallRealMethod();

    this.createResponse = mock(CreateResponse.class);
    this.createRequest = mock(CreateRequest.class);

    List<Metacard> metacards = new ArrayList<>();
    metacards.add(metacard);
    metacards.add(metacard);

    when(createResponse.getCreatedMetacards()).thenReturn(metacards);
    when(createResponse.getRequest()).thenReturn(createRequest);
    when(createRequest.getProperties()).thenReturn(requestProperties);

    nitfPostIngestPlugin.process(createResponse);

    validate();
  }

  private void validate() throws Exception {
    verify(catalogFramework, times(1)).update(updateStorageCaptor.capture());

    Attribute thumbnail = metacard.getAttribute(Core.THUMBNAIL);
    Attribute derivedResource = metacard.getAttribute(Core.DERIVED_RESOURCE_URI);
    assertThat(thumbnail.getName(), is("thumbnail"));
    assertThat(thumbnail.getValue(), is(notNullValue()));
    assertThat(derivedResource.getName(), is(Core.DERIVED_RESOURCE_URI));
    assertThat(derivedResource.getValue(), is(notNullValue()));
    assertThat(derivedResource.getValues().size(), is(2));

    Set<String> qualifiers =
        updateStorageCaptor
            .getValue()
            .getContentItems()
            .stream()
            .map(ci -> ci.getQualifier())
            .collect(Collectors.toSet());
    assertThat(qualifiers.contains("overview"), is(true));
    assertThat(qualifiers.contains("original"), is(true));
  }

  private ResourceResponse getInputStream(String filename) {
    assertNotNull("Test file missing", getClass().getResource(filename));
    Resource resource = new ResourceImpl(getClass().getResourceAsStream(filename), filename);
    ResourceResponse response = new ResourceResponseImpl(resource);
    return response;
  }
}
