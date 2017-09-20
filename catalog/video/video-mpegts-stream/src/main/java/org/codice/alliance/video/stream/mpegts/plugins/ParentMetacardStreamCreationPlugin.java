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

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardCreationException;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import java.util.List;
import java.util.stream.Collectors;
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
    setParentContentType(metacard);

    CreateRequest createRequest = new CreateRequestImpl(metacard);

    try {
      submitParentCreateRequest(context, createRequest);
    } catch (IngestException | SourceUnavailableException e) {
      throw new StreamCreationException("unable to submit parent metacard to catalog framework", e);
    }
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

  private void setParentContentType(MetacardImpl metacard) {
    metacard.setContentTypeName(Constants.MPEGTS_MIME_TYPE);
  }

  private void setParentTitle(Context context, MetacardImpl metacard) {
    context.getUdpStreamProcessor().getTitle().ifPresent(metacard::setTitle);
  }

  private void submitParentCreateRequest(Context context, CreateRequest createRequest)
      throws IngestException, SourceUnavailableException {
    List<Metacard> createdMetacards = catalogFramework.create(createRequest).getCreatedMetacards();
    List<String> createdIds =
        createdMetacards.stream().map(Metacard::getId).collect(Collectors.toList());
    LOGGER.debug("created parent metacards with ids: {}", createdIds);
    context.setParentMetacard(createdMetacards.get(createdMetacards.size() - 1));
  }
}
