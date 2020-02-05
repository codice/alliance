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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.Subject;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.ddf.security.Security;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ParentMetacardStreamCreationPluginTest {

  private Context context;

  private ParentMetacardStreamCreationPlugin parentMetacardStreamCreationPlugin;

  private CatalogFramework catalogFramework;

  private URI uri;

  private String title;

  @Before
  public void setup() throws SourceUnavailableException, IngestException {
    context = mock(Context.class);
    uri = URI.create("udp://127.0.0.1:10000");
    title = "theTitleString";

    UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);
    when(udpStreamProcessor.getStreamUri()).thenReturn(Optional.of(uri));
    when(udpStreamProcessor.getTitle()).thenReturn(Optional.of(title));

    when(context.getUdpStreamProcessor()).thenReturn(udpStreamProcessor);

    Security security = mock(Security.class);
    Subject subject = mock(Subject.class);
    when(security.getSystemSubject()).thenReturn(subject);

    catalogFramework = mock(CatalogFramework.class);
    parentMetacardStreamCreationPlugin =
        new ParentMetacardStreamCreationPlugin(
            catalogFramework, Collections.singletonList(mock(MetacardType.class)));
    CreateResponse createResponse = mock(CreateResponse.class);
    Metacard createdParentMetacard = mock(Metacard.class);

    context.setParentMetacard(createdParentMetacard);

    when(createResponse.getCreatedMetacards())
        .thenReturn(Collections.singletonList(createdParentMetacard));
    when(catalogFramework.create(any(CreateRequest.class))).thenReturn(createResponse);
  }

  @Test
  public void testThatParentMetacardHasResourceURI()
      throws StreamCreationException, SourceUnavailableException, IngestException {

    parentMetacardStreamCreationPlugin.onCreate(context);

    ArgumentCaptor<CreateRequest> argumentCaptor = ArgumentCaptor.forClass(CreateRequest.class);

    verify(catalogFramework).create(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getMetacards().get(0).getResourceURI(), is(uri));
  }

  @Test
  public void testThatParentMetacardHasTitle()
      throws StreamCreationException, SourceUnavailableException, IngestException {

    parentMetacardStreamCreationPlugin.onCreate(context);

    ArgumentCaptor<CreateRequest> argumentCaptor = ArgumentCaptor.forClass(CreateRequest.class);

    verify(catalogFramework).create(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getMetacards().get(0).getTitle(), is(title));
  }
}
