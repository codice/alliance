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

import static org.apache.commons.lang3.Validate.notNull;
import static org.codice.alliance.catalog.core.api.types.Isr.ORIGINAL_SOURCE;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardCreationException;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codice.alliance.catalog.core.api.types.VideoStream;
import org.codice.alliance.video.stream.mpegts.Constants;
import org.codice.alliance.video.stream.mpegts.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParentMetacardStreamCreationPlugin extends BaseStreamCreationPlugin {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ParentMetacardStreamCreationPlugin.class);

  private final CatalogFramework catalogFramework;

  private final List<MetacardType> metacardTypeList;

  /**
   * @param catalogFramework must be non-null
   * @param metacardTypeList must be non-null
   */
  public ParentMetacardStreamCreationPlugin(
      CatalogFramework catalogFramework, List<MetacardType> metacardTypeList) {
    notNull(catalogFramework, "catalogFramework must be non-null");
    notNull(metacardTypeList, "metacardTypeList must be non-null");
    this.catalogFramework = catalogFramework;
    this.metacardTypeList = metacardTypeList;
  }

  @Override
  protected void doOnCreate(Context context) throws StreamCreationException {

    MetacardImpl metacard;
    try {
      metacard = createInitialMetacard();
    } catch (MetacardCreationException e) {
      throw new StreamCreationException("unable to create initial parent metacard", e);
    }

    setParentResourceUri(context, metacard);
    setParentTitle(context, metacard);
    setParentStreamId(context, metacard);
    setParentContentType(metacard);
    setParentVideoSource(context, metacard);
    setParentOriginalUrl(context, metacard);
    setParentType(metacard);

    CreateRequest createRequest = new CreateRequestImpl(metacard);

    try {
      submitParentCreateRequest(context, createRequest);
    } catch (IngestException | SourceUnavailableException e) {
      throw new StreamCreationException("unable to submit parent metacard to catalog framework", e);
    }

    linkSourceWithParent(context);
  }

  private MetacardCreationException createException() {
    return new MetacardCreationException("unable to find a metacard type");
  }

  private MetacardImpl createInitialMetacard() throws MetacardCreationException {
    return new MetacardImpl(
        metacardTypeList.stream().findFirst().orElseThrow(this::createException));
  }

  private void setParentResourceUri(Context context, MetacardImpl metacard) {
    context.getUdpStreamProcessor().getStreamUri().ifPresent(metacard::setResourceURI);
  }

  private void setParentVideoSource(Context context, MetacardImpl metacard) {
    addRelation(
        metacard,
        context.getUdpStreamProcessor().getAdditionalProperties().get("video-source-id"),
        Associations.DERIVED);
  }

  private void setParentOriginalUrl(Context context, MetacardImpl metacard) {
    LOGGER.debug(
        "Adding origin url of {} to {}",
        context.getUdpStreamProcessor().getAdditionalProperties().get("original-url"),
        metacard.getId());
    metacard.setAttribute(
        ORIGINAL_SOURCE,
        context.getUdpStreamProcessor().getAdditionalProperties().get("original-url"));
  }

  private void setParentContentType(MetacardImpl metacard) {
    metacard.setContentTypeName(Constants.MPEGTS_MIME_TYPE);
  }

  private void setParentTitle(Context context, MetacardImpl metacard) {
    // a new metacard is created each time recording is started
    context
        .getUdpStreamProcessor()
        .getTitle()
        .ifPresent(title -> metacard.setTitle(title + " - Rec " + context.getNextRecordingCount()));
  }

  private void setParentStreamId(Context context, MetacardImpl metacard) {
    metacard.setAttribute(
        new AttributeImpl(VideoStream.STREAM_ID, context.getUdpStreamProcessor().getStreamId()));
  }

  private void setParentType(MetacardImpl metacard) {
    metacard.setAttribute(VideoStream.CONTAINER, true);
  }

  private void submitParentCreateRequest(Context context, CreateRequest createRequest)
      throws IngestException, SourceUnavailableException {
    List<Metacard> createdMetacards = catalogFramework.create(createRequest).getCreatedMetacards();
    List<String> createdIds =
        createdMetacards.stream().map(Metacard::getId).collect(Collectors.toList());
    LOGGER.debug("created parent metacards with ids: {}", createdIds);
    context.setParentMetacard(createdMetacards.get(createdMetacards.size() - 1));
  }

  private void linkSourceWithParent(Context context) {
    String videoSourceId =
        context.getUdpStreamProcessor().getAdditionalProperties().get("video-source-id");
    LOGGER.debug("Linking source with parent id: {}", videoSourceId);
    if (videoSourceId != null) {
      try {
        Metacard source =
            catalogFramework
                .query(
                    new QueryRequestImpl(
                        new QueryImpl(
                            getFilterBuilder()
                                .attribute(Core.ID)
                                .is()
                                .equalTo()
                                .text(videoSourceId))))
                .getResults().stream()
                .map(Result::getMetacard)
                .findFirst()
                .orElse(null);
        if (source == null) {
          LOGGER.warn("Couldn't find video source metacard with id {}", videoSourceId);
          return;
        }
        context
            .getParentMetacard()
            .ifPresent(
                parent -> {
                  addRelation(source, parent.getId(), Associations.RELATED);
                  try {
                    catalogFramework.update(new UpdateRequestImpl(source.getId(), source));
                  } catch (IngestException | SourceUnavailableException e) {
                    LOGGER.warn(
                        "Failed to update video source association with id {}", videoSourceId, e);
                  }
                });
      } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
        LOGGER.warn("Failed to locate video source metacard with id {}", videoSourceId, e);
      }
    }
  }

  private void addRelation(Metacard metacard, String id, String type) {
    if (metacard != null && id != null) {
      LOGGER.debug("Adding relation from {} to {}", metacard.getTitle(), id);
      List<Serializable> related =
          Optional.ofNullable(metacard.getAttribute(type))
              .map(Attribute::getValues)
              .orElse(new ArrayList<>());
      if (!related.contains(id)) {
        related.add(id);
        metacard.setAttribute(new AttributeImpl(type, related));
      }
    }
  }
}
