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
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import java.util.Collections;
import java.util.List;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class UpdateParentTest {

  /** Test that #handle calls the UpdateField object */
  @Test
  public void testHandle() {

    UpdateParent.UpdateField updateField = mock(UpdateParent.UpdateField.class);

    UpdateParent updateParent = new UpdateParent(updateField);

    Context context = mock(Context.class);

    Metacard parent = mock(Metacard.class);
    List<Metacard> children = mock(List.class);

    updateParent.handle(context, parent, children);

    verify(updateField).updateField(parent, children, context);
  }

  /** Test that #end calls the catalog framework */
  @Test
  public void testEnd() throws SourceUnavailableException, IngestException {

    UpdateParent.UpdateField updateField = mock(UpdateParent.UpdateField.class);

    UpdateParent updateParent = new UpdateParent(updateField);

    UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);
    CatalogFramework catalogFramework = mock(CatalogFramework.class);
    when(udpStreamProcessor.getCatalogFramework()).thenReturn(catalogFramework);
    when(udpStreamProcessor.getMetacardUpdateInitialDelay()).thenReturn(1L);

    Context context = mock(Context.class);
    when(context.getUdpStreamProcessor()).thenReturn(udpStreamProcessor);

    Metacard parent = mock(Metacard.class);

    UpdateResponse updateResponse = mock(UpdateResponse.class);
    Update update = mock(Update.class);
    when(update.getNewMetacard()).thenReturn(parent);
    when(updateResponse.getUpdatedMetacards()).thenReturn(Collections.singletonList(update));

    when(catalogFramework.update(any(UpdateRequest.class))).thenReturn(updateResponse);

    updateParent.end(context, parent);

    verify(updateField).end(parent, context);

    ArgumentCaptor<UpdateRequest> captor = ArgumentCaptor.forClass(UpdateRequest.class);

    verify(catalogFramework).update(captor.capture());

    assertThat(captor.getValue().getUpdates().get(0).getValue(), is(parent));
  }

  @Test(expected = IllegalStateException.class)
  public void testCallUpdateAfterEnd() {

    UpdateParent.UpdateField updateField =
        new UpdateParent.BaseUpdateField() {

          @Override
          protected void doEnd(Metacard parent, Context context) {}

          @Override
          protected void doUpdateField(Metacard parent, List<Metacard> children, Context context) {}
        };

    Context context = mock(Context.class);

    Metacard parent = mock(Metacard.class);

    Metacard child = mock(Metacard.class);

    updateField.end(parent, context);

    updateField.updateField(parent, Collections.singletonList(child), context);
  }
}
