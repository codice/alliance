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

import static org.apache.commons.lang3.Validate.notNull;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import ddf.catalog.CatalogFramework;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.impl.CreateStorageRequestImpl;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Media;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.security.Subject;
import ddf.security.SubjectOperations;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.catalog.core.api.types.VideoStream;
import org.codice.alliance.video.stream.mpegts.Constants;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.framework.CatalogUpdateRetry;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.codice.ddf.platform.util.uuidgenerator.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the child content, links the child to the parent, and updates the parent's location with
 * the union of the child's location.
 */
public class CatalogRolloverAction extends BaseRolloverAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRolloverAction.class);

  private static final long MAX_RETRY_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);

  private static final long INITIAL_RETRY_WAIT_MILLISECONDS = TimeUnit.MILLISECONDS.toMillis(500);

  private final FilenameGenerator filenameGenerator;

  private final CatalogFramework catalogFramework;

  private final FilterBuilder filterBuilder;

  private final Context context;

  private final MetacardUpdater parentMetacardUpdater;

  private final UuidGenerator uuidGenerator;

  private CatalogUpdateRetry catalogUpdateRetry = new CatalogUpdateRetry();

  private String filenameTemplate;

  private SubjectOperations subjectOperations;

  /**
   * @param filenameGenerator must be non-null
   * @param filenameTemplate must be non-null
   * @param catalogFramework must be non-null
   * @param context must be non-null
   * @param parentMetacardUpdater must be non-null
   */
  public CatalogRolloverAction(
      FilenameGenerator filenameGenerator,
      String filenameTemplate,
      CatalogFramework catalogFramework,
      FilterBuilder filterBuilder,
      Context context,
      MetacardUpdater parentMetacardUpdater,
      UuidGenerator uuidGenerator,
      SubjectOperations subjectOperations) {
    notNull(filenameGenerator, "filenameGenerator must be non-null");
    notNull(filenameTemplate, "filenameTemplate must be non-null");
    notNull(catalogFramework, "catalogFramework must be non-null");
    notNull(context, "context must be non-null");
    notNull(filterBuilder, "filterBuilder must be non-null");
    notNull(parentMetacardUpdater, "parentMetacardUpdater must be non-null");
    notNull(uuidGenerator, "uuidGenerator must be non-null");
    notNull(subjectOperations, "subjectOperations must be non-null");

    this.filenameGenerator = filenameGenerator;
    this.filenameTemplate = filenameTemplate;
    this.catalogFramework = catalogFramework;
    this.filterBuilder = filterBuilder;
    this.context = context;
    this.parentMetacardUpdater = parentMetacardUpdater;
    this.uuidGenerator = uuidGenerator;
    this.subjectOperations = subjectOperations;
  }

  public void setCatalogUpdateRetry(CatalogUpdateRetry catalogUpdateRetry) {
    this.catalogUpdateRetry = catalogUpdateRetry;
  }

  @Override
  public String toString() {
    return "CatalogRolloverAction{"
        + "catalogFramework="
        + catalogFramework
        + ", filenameTemplate='"
        + filenameTemplate
        + '\''
        + ", filenameGenerator="
        + filenameGenerator
        + ", parentMetacardUpdater="
        + parentMetacardUpdater
        + '}';
  }

  @Override
  public MetacardImpl doAction(MetacardImpl metacard, File tempFile) {

    return context.modifyParentOrChild(
        isParentDirty -> {
          Subject subject = context.getUdpStreamProcessor().getSubject();

          if (subject == null) {
            LOGGER.debug("no security subject available, cannot upload video chunk");
            return metacard;
          }

          return subject.execute(
              () -> {
                String fileName = generateFilename();

                setTitle(metacard);

                enforceRequiredMetacardFields(metacard, fileName);

                addTimestamps(metacard, tempFile);

                setDerivedAttribute(metacard);

                ContentItem contentItem =
                    createContentItem(metacard, fileName, Files.asByteSource(tempFile));

                CreateStorageRequest createStorageRequest = createStorageRequest(contentItem);

                CreateResponse createResponse = submitStorageCreateRequest(createStorageRequest);

                for (Metacard childMetacard : createResponse.getCreatedMetacards()) {
                  LOGGER.trace("created catalog content with id={}", childMetacard.getId());

                  updateParentWithChildMetadata(childMetacard);
                }

                isParentDirty.set(true);

                return metacard;
              });
        });
  }

  private void addTimestamps(MetacardImpl metacard, File segmentFile) {
    long startTime = context.getUdpStreamProcessor().getPacketBuffer().getLastSegmentStart();
    long endTime = context.getUdpStreamProcessor().getPacketBuffer().getLastSegmentEnd();
    if (startTime < 0 || endTime < 0) {
      LOGGER.warn("Segment start/end time not set. Start: {}  End: {}", startTime, endTime);
      return;
    }
    Date start = new Date(startTime);
    Date end = new Date(endTime);

    metacard.setAttribute(DateTime.START, start);
    metacard.setAttribute(DateTime.END, end);
    metacard.setAttribute(Media.DURATION, TimeUnit.MILLISECONDS.toSeconds(endTime - startTime));
    metacard.setAttribute(
        Media.BITS_PER_SECOND,
        (segmentFile.length() * 8.0) / TimeUnit.MILLISECONDS.toSeconds(endTime - startTime));
  }

  private void setTitle(MetacardImpl mcard) {
    if (context
        .getUdpStreamProcessor()
        .getAdditionalProperties()
        .getOrDefault("filename-as-title", "true")
        .equals("true")) {
      return;
    }
    if (mcard != null && context.getParentMetacard().isPresent()) {
      String parentTitle = context.getParentMetacard().get().getTitle();
      mcard.setTitle(parentTitle + " - Seg " + context.getNextSegmentCount());
    }
  }

  private String generateFilename() {
    return filenameGenerator.generateFilename(filenameTemplate);
  }

  private void updateParentWithChildMetadata(Metacard childMetacard) {
    if (context.getParentMetacard().isPresent()) {
      Metacard parentMetacard = getFreshParentMetacard();
      parentMetacardUpdater.update(parentMetacard, childMetacard, context);
      UpdateRequest updateRequest = createUpdateRequest(parentMetacard.getId(), parentMetacard);
      submitParentUpdateRequest(updateRequest);
    }
  }

  private Metacard getFreshParentMetacard() {
    Metacard parent = context.getParentMetacard().orElse(null);
    if (parent != null) {
      Query query = new QueryImpl(filterBuilder.attribute(Core.ID).equalTo().text(parent.getId()));
      try {
        parent =
            catalogFramework.query(new QueryRequestImpl(query)).getResults().stream()
                .map(Result::getMetacard)
                .findFirst()
                .orElse(parent);
      } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
        LOGGER.warn("Unable to get fresh parent metacard for {}", parent.getId(), e);
      }
    }
    return parent;
  }

  private void submitParentUpdateRequest(UpdateRequest updateRequest) {
    if (context.getParentMetacard().isPresent()) {
      catalogUpdateRetry.submitUpdateRequestWithRetry(
          catalogFramework,
          updateRequest,
          context.getUdpStreamProcessor().getMetacardUpdateInitialDelay(),
          INITIAL_RETRY_WAIT_MILLISECONDS,
          MAX_RETRY_MILLISECONDS,
          update -> {
            LOGGER.debug(
                "updated parent metacard: newMetacard={}", update.getNewMetacard().getId());
            context.setParentMetacard(update.getNewMetacard());
          });
    }
  }

  private UpdateRequest createUpdateRequest(String id, Metacard metacard) {
    return new UpdateRequestImpl(id, metacard);
  }

  private void setDerivedAttribute(Metacard childMetacard) {
    if (childMetacard != null && context.getParentMetacard().isPresent()) {
      childMetacard.setAttribute(
          new AttributeImpl(Metacard.DERIVED, context.getParentMetacard().get().getId()));
      childMetacard.setAttribute(
          new AttributeImpl(VideoStream.RECORDING_ID, context.getParentMetacard().get().getId()));
    }
  }

  private CreateResponse submitStorageCreateRequest(CreateStorageRequest createRequest)
      throws RolloverActionException {
    try {
      return catalogFramework.create(createRequest);
    } catch (IngestException | SourceUnavailableException e) {
      throw new RolloverActionException(
          String.format(
              "unable to submit storage create request to catalog framework: %s", createRequest),
          e);
    }
  }

  private CreateStorageRequest createStorageRequest(ContentItem contentItem) {
    return new CreateStorageRequestImpl(Collections.singletonList(contentItem), new HashMap<>());
  }

  private ContentItem createContentItem(
      MetacardImpl metacard, String fileName, ByteSource byteSource) {
    String metacardId;
    if (metacard != null && !StringUtils.isEmpty(metacard.getId())) {
      metacardId = metacard.getId();
    } else {
      metacardId = uuidGenerator.generateUuid();
    }

    return new ContentItemImpl(
        metacardId, byteSource, Constants.MPEGTS_MIME_TYPE, fileName, 0l, metacard);
  }

  private void enforceRequiredMetacardFields(MetacardImpl metacard, String fileName) {
    if (metacard != null) {
      setIdAttribute(metacard);

      setTitleAttribute(metacard, fileName);

      setPointOfContactAttribute(metacard);

      setStreamId(metacard);
    }
  }

  private void setPointOfContactAttribute(MetacardImpl metacard) {

    String subjectName = subjectOperations.getName(context.getUdpStreamProcessor().getSubject());

    metacard.setAttribute(
        new AttributeImpl(Metacard.POINT_OF_CONTACT, subjectName == null ? "" : subjectName));
  }

  private void setTitleAttribute(MetacardImpl metacard, String fileName) {
    if (StringUtils.isBlank(metacard.getTitle())) {
      metacard.setTitle(fileName);
    }
  }

  private void setIdAttribute(MetacardImpl metacard) {
    if (metacard.getId() == null) {
      metacard.setId(UUID.randomUUID().toString().replaceAll("-", ""));
    }
  }

  private void setStreamId(MetacardImpl metacard) {
    metacard.setAttribute(
        new AttributeImpl(VideoStream.STREAM_ID, context.getUdpStreamProcessor().getStreamId()));
  }
}
