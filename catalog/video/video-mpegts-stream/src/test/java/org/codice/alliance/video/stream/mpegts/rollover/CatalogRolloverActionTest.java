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
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.Subject;
import ddf.security.service.impl.SubjectUtils;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.codice.alliance.catalog.core.api.types.VideoStream;
import org.codice.alliance.libs.klv.AttributeNameConstants;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.SimpleSubject;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.ListMetacardUpdater;
import org.codice.alliance.video.stream.mpegts.metacard.ModifiedDateMetacardUpdater;
import org.codice.alliance.video.stream.mpegts.metacard.TemporalEndMetacardUpdater;
import org.codice.alliance.video.stream.mpegts.metacard.TemporalStartMetacardUpdater;
import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;
import org.codice.alliance.video.stream.mpegts.netty.StreamProcessor;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.ddf.platform.util.uuidgenerator.UuidGenerator;
import org.codice.ddf.security.Security;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class CatalogRolloverActionTest {

  private static final Date TEMPORAL_START_DATE = new Date();

  private static final Date TEMPORAL_END_DATE = new Date();

  private CatalogFramework catalogFramework;

  private File tempFile;

  private CatalogRolloverAction catalogRolloverAction;

  private Metacard createdParentMetacard;

  private Metacard createdChildMetacard;

  private UpdateResponse childUpdateResponse;

  private UpdateResponse parentUpdateResponse;

  private String childWkt;

  private PacketBuffer packetBuffer;

  private final String streamId = UUID.randomUUID().toString();

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setup() throws Exception {
    FilenameGenerator filenameGenerator = mock(FilenameGenerator.class);
    String filenameTemplate = "filenameTemplate";
    StreamProcessor streamProcessor = mock(StreamProcessor.class);
    when(streamProcessor.getMetacardUpdateInitialDelay()).thenReturn(1L);
    catalogFramework = mock(CatalogFramework.class);
    MetacardType metacardType = mock(MetacardType.class);
    tempFile = folder.newFile("someTempFile");

    URI uri = URI.create("udp://127.0.0.1:10000");
    String title = "theTitleString";
    childWkt = "POLYGON (( 0.5 0.5, 1.5 0.5, 1.5 1.5, 0.5 1.5, 0.5 0.5 ))";

    when(metacardType.getAttributeDescriptor(AttributeNameConstants.TEMPORAL_START))
        .thenReturn(mock(AttributeDescriptor.class));
    when(metacardType.getAttributeDescriptor(AttributeNameConstants.TEMPORAL_END))
        .thenReturn(mock(AttributeDescriptor.class));

    UdpStreamProcessor udpStreamProcessor = mock(UdpStreamProcessor.class);
    when(udpStreamProcessor.getStreamId()).thenReturn(streamId);
    when(udpStreamProcessor.getSubject()).thenReturn(new SimpleSubject());

    packetBuffer = mock(PacketBuffer.class);
    when(packetBuffer.getLastSegmentStart()).thenReturn(-1l);
    when(packetBuffer.getLastSegmentEnd()).thenReturn(-1l);
    when(udpStreamProcessor.getPacketBuffer()).thenReturn(packetBuffer);

    Context context = new Context(udpStreamProcessor);

    when(udpStreamProcessor.getMetacardTypeList())
        .thenReturn(Collections.singletonList(metacardType));
    when(udpStreamProcessor.getStreamUri()).thenReturn(Optional.of(uri));
    when(udpStreamProcessor.getTitle()).thenReturn(Optional.of(title));

    Security security = mock(Security.class);
    Subject subject = mock(Subject.class);
    when(security.getSystemSubject()).thenReturn(subject);

    UuidGenerator uuidGenerator = mock(UuidGenerator.class);
    when(uuidGenerator.generateUuid()).thenReturn("anId");

    catalogRolloverAction =
        new CatalogRolloverAction(
            filenameGenerator,
            filenameTemplate,
            catalogFramework,
            context,
            new ListMetacardUpdater(
                Arrays.asList(
                    new TemporalStartMetacardUpdater(),
                    new TemporalEndMetacardUpdater(),
                    new ModifiedDateMetacardUpdater())),
            uuidGenerator,
            new SubjectUtils());

    createdParentMetacard = mock(Metacard.class);
    when(createdParentMetacard.getMetacardType()).thenReturn(metacardType);

    context.setParentMetacard(createdParentMetacard);
    context.getGeometryOperatorContext().setDistanceTolerance(0.0025);

    createdChildMetacard = mock(Metacard.class);
    when(createdChildMetacard.getMetacardType()).thenReturn(metacardType);
    Metacard updatedParentMetacard = mock(Metacard.class);
    when(updatedParentMetacard.getMetacardType()).thenReturn(metacardType);
    Metacard updatedChildMetacard = mock(Metacard.class);
    when(updatedChildMetacard.getMetacardType()).thenReturn(metacardType);
    CreateResponse createResponse = mock(CreateResponse.class);
    CreateResponse storageCreateResponse = mock(CreateResponse.class);
    Update childUpdate = mock(Update.class);
    childUpdateResponse = mock(UpdateResponse.class);
    Update parentUpdate = mock(Update.class);
    parentUpdateResponse = mock(UpdateResponse.class);

    when(createResponse.getCreatedMetacards())
        .thenReturn(Collections.singletonList(createdParentMetacard));
    when(storageCreateResponse.getCreatedMetacards())
        .thenReturn(Collections.singletonList(createdChildMetacard));
    when(childUpdate.getNewMetacard()).thenReturn(updatedChildMetacard);
    when(childUpdateResponse.getUpdatedMetacards())
        .thenReturn(Collections.singletonList(childUpdate));
    when(parentUpdate.getNewMetacard()).thenReturn(updatedParentMetacard);
    when(parentUpdateResponse.getUpdatedMetacards())
        .thenReturn(Collections.singletonList(parentUpdate));
    when(filenameGenerator.generateFilename(any())).thenReturn("someFileName");
    when(streamProcessor.getStreamUri()).thenReturn(Optional.of(uri));
    when(streamProcessor.getTitle()).thenReturn(Optional.of(title));
    when(catalogFramework.create(any(CreateRequest.class))).thenReturn(createResponse);
    when(catalogFramework.create(any(CreateStorageRequest.class)))
        .thenReturn(storageCreateResponse);
    when(catalogFramework.update(any(UpdateRequest.class)))
        .thenReturn(childUpdateResponse)
        .thenReturn(parentUpdateResponse);
    when(createdChildMetacard.getLocation()).thenReturn(childWkt);
    when(createdChildMetacard.getAttribute(AttributeNameConstants.TEMPORAL_START))
        .thenReturn(new AttributeImpl(AttributeNameConstants.TEMPORAL_START, TEMPORAL_START_DATE));
    when(createdChildMetacard.getAttribute(AttributeNameConstants.TEMPORAL_END))
        .thenReturn(new AttributeImpl(AttributeNameConstants.TEMPORAL_END, TEMPORAL_END_DATE));
  }

  /** Test that the parent update succeeded after an initial failure. */
  @Test
  public void testRetry()
      throws RolloverActionException, SourceUnavailableException, IngestException {

    when(catalogFramework.update(any(UpdateRequest.class)))
        .thenThrow(IngestException.class)
        .thenReturn(childUpdateResponse)
        .thenReturn(parentUpdateResponse);

    catalogRolloverAction.doAction(tempFile);

    ArgumentCaptor<UpdateRequest> argumentCaptor = ArgumentCaptor.forClass(UpdateRequest.class);

    verify(catalogFramework, times(3)).update(argumentCaptor.capture());

    ArgumentCaptor<Attribute> attributeCaptor = ArgumentCaptor.forClass(Attribute.class);
    verify(createdParentMetacard, atLeastOnce()).setAttribute(attributeCaptor.capture());

    List<Attribute> geoAttributeList =
        attributeCaptor.getAllValues().stream()
            .filter(attr -> attr.getName().equals(Core.MODIFIED))
            .collect(Collectors.toList());

    assertThat(geoAttributeList, hasSize(1));
    assertThat(geoAttributeList.get(0).getValue(), is(TEMPORAL_END_DATE));
  }

  @Test
  public void testTemporalStart()
      throws RolloverActionException, SourceUnavailableException, IngestException {

    catalogRolloverAction.doAction(tempFile);

    ArgumentCaptor<UpdateRequest> argumentCaptor = ArgumentCaptor.forClass(UpdateRequest.class);

    verify(catalogFramework, times(2)).update(argumentCaptor.capture());

    ArgumentCaptor<Attribute> attributeCaptor = ArgumentCaptor.forClass(Attribute.class);
    verify(createdParentMetacard, atLeastOnce()).setAttribute(attributeCaptor.capture());

    List<Attribute> geoAttributeList =
        attributeCaptor.getAllValues().stream()
            .filter(attr -> attr.getName().equals(AttributeNameConstants.TEMPORAL_START))
            .collect(Collectors.toList());

    assertThat(geoAttributeList, hasSize(1));
    assertThat(geoAttributeList.get(0).getValue(), is(TEMPORAL_START_DATE));
  }

  @Test
  public void testTemporalEnd()
      throws RolloverActionException, SourceUnavailableException, IngestException {

    catalogRolloverAction.doAction(tempFile);

    ArgumentCaptor<UpdateRequest> argumentCaptor = ArgumentCaptor.forClass(UpdateRequest.class);

    verify(catalogFramework, times(2)).update(argumentCaptor.capture());

    ArgumentCaptor<Attribute> attributeCaptor = ArgumentCaptor.forClass(Attribute.class);
    verify(createdParentMetacard, atLeastOnce()).setAttribute(attributeCaptor.capture());

    List<Attribute> geoAttributeList =
        attributeCaptor.getAllValues().stream()
            .filter(attr -> attr.getName().equals(AttributeNameConstants.TEMPORAL_END))
            .collect(Collectors.toList());

    assertThat(geoAttributeList, hasSize(1));
    assertThat(geoAttributeList.get(0).getValue(), is(TEMPORAL_END_DATE));
  }

  @Test
  public void testSegmentTemporalStartEnd()
      throws RolloverActionException, SourceUnavailableException, IngestException {
    long start = System.currentTimeMillis() - 10000;
    long end = System.currentTimeMillis();
    when(packetBuffer.getLastSegmentStart()).thenReturn(start);
    when(packetBuffer.getLastSegmentEnd()).thenReturn(end);
    MetacardImpl metacard = new MetacardImpl();
    catalogRolloverAction.doAction(metacard, tempFile);

    assertThat(
        ((Date) metacard.getAttribute(AttributeNameConstants.TEMPORAL_START).getValue()).getTime(),
        is(start));
    assertThat(
        ((Date) metacard.getAttribute(AttributeNameConstants.TEMPORAL_END).getValue()).getTime(),
        is(end));
    assertThat(metacard.getAttribute(Media.DURATION).getValue(), is((end - start) / 1000));
  }

  @Test
  public void testStreamId() throws Exception {
    final MetacardImpl metacard = mock(MetacardImpl.class);

    catalogRolloverAction.doAction(metacard, tempFile);

    verify(metacard).setAttribute(new AttributeImpl(VideoStream.STREAM_ID, streamId));
  }
}
