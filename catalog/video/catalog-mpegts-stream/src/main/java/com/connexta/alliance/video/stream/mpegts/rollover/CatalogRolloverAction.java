/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.video.stream.mpegts.rollover;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ThreadContext;
import org.codice.ddf.security.common.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connexta.alliance.video.stream.mpegts.Constants;
import com.connexta.alliance.video.stream.mpegts.filename.FilenameGenerator;
import com.connexta.alliance.video.stream.mpegts.metacard.FrameCenterMetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.metacard.ListMetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.metacard.LocationMetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.metacard.ModifiedDateMetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.metacard.TemporalEndMetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.metacard.TemporalStartMetacardUpdater;
import com.connexta.alliance.video.stream.mpegts.netty.StreamProcessor;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import ddf.catalog.CatalogFramework;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.impl.CreateStorageRequestImpl;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardCreationException;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.SubjectUtils;

/**
 * Creates the parent metacard that represents
 * the stream, stores the child content, links the child to the parent, and updates the parent's
 * location with the union of the child's location.
 */
public class CatalogRolloverAction extends BaseRolloverAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRolloverAction.class);

    private static final long MAX_RETRY_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);

    private static final long INITIAL_RETRY_WAIT_MILLISECONDS = TimeUnit.MILLISECONDS.toMillis(500);

    private final FilenameGenerator filenameGenerator;

    private final StreamProcessor streamProcessor;

    private final CatalogFramework catalogFramework;

    private final Security security;

    private final List<MetacardType> metacardTypeList;

    private String filenameTemplate;

    private Metacard parentMetacard;

    private MetacardUpdater parentMetacardUpdater =
            new ListMetacardUpdater(Arrays.asList(new LocationMetacardUpdater(),
                    new TemporalStartMetacardUpdater(),
                    new TemporalEndMetacardUpdater(),
                    new ModifiedDateMetacardUpdater(),
                    new FrameCenterMetacardUpdater()));

    /**
     * @param filenameGenerator must be non-null
     * @param filenameTemplate  must be non-null
     * @param streamProcessor   must be non-null
     * @param catalogFramework  must be non-null
     * @param security          must be non-null
     * @param metacardTypeList  must be non-null
     */
    public CatalogRolloverAction(FilenameGenerator filenameGenerator, String filenameTemplate,
            StreamProcessor streamProcessor, CatalogFramework catalogFramework, Security security,
            List<MetacardType> metacardTypeList) {
        notNull(filenameGenerator, "filenameGenerator must be non-null");
        notNull(filenameTemplate, "filenameTemplate must be non-null");
        notNull(streamProcessor, "streamProcessor must be non-null");
        notNull(catalogFramework, "catalogFramework must be non-null");
        notNull(security, "security must be non-null");
        notNull(metacardTypeList, "metacardTypeList must be non-null");

        this.filenameGenerator = filenameGenerator;
        this.filenameTemplate = filenameTemplate;
        this.streamProcessor = streamProcessor;
        this.catalogFramework = catalogFramework;
        this.security = security;
        this.metacardTypeList = metacardTypeList;
    }

    @Override
    public String toString() {
        return "CatalogRolloverAction{" +
                "catalogFramework=" + catalogFramework +
                ", filenameTemplate='" + filenameTemplate + '\'' +
                ", filenameGenerator=" + filenameGenerator +
                ", metacardTypeList=" + metacardTypeList +
                '}';
    }

    @Override
    public MetacardImpl doAction(MetacardImpl metacard, File tempFile)
            throws RolloverActionException {

        bindSecuritySubject();

        String fileName = generateFilename();

        enforceRequiredMetacardFields(metacard, fileName);

        createParentMetacard();

        ContentItem contentItem = createContentItem(metacard,
                fileName,
                Files.asByteSource(tempFile));

        CreateStorageRequest createStorageRequest = createStorageRequest(contentItem);

        CreateResponse createResponse = submitStorageCreateRequest(createStorageRequest);

        for (Metacard childMetacard : createResponse.getCreatedMetacards()) {
            LOGGER.info("created catalog content with id={}", childMetacard.getId());

            linkChildToParent(childMetacard);

            updateParentWithChildMetadata(childMetacard);

        }

        return metacard;
    }

    private String generateFilename() {
        return filenameGenerator.generateFilename(filenameTemplate);
    }

    private void updateParentWithChildMetadata(Metacard childMetacard)
            throws RolloverActionException {
        parentMetacardUpdater.update(parentMetacard, childMetacard);
        UpdateRequest updateRequest = createUpdateRequest(parentMetacard.getId(), parentMetacard);
        submitParentUpdateRequest(updateRequest);
    }

    private void submitParentUpdateRequest(UpdateRequest updateRequest)
            throws RolloverActionException {
        submitUpdateRequestWithRetry(updateRequest, update -> {
            LOGGER.info("updated parent metacard: newMetacard={}",
                    update.getNewMetacard()
                            .getId());
            parentMetacard = update.getNewMetacard();
        });
    }

    private void submitChildUpdateRequest(UpdateRequest updateRequest)
            throws RolloverActionException {
        submitUpdateRequestWithRetry(updateRequest,
                update -> LOGGER.info("updated child metacard with link to parent: child={}",
                        update.getNewMetacard()
                                .getId()));
    }

    private void submitUpdateRequestWithRetry(UpdateRequest updateRequest,
            Consumer<Update> updateConsumer) throws RolloverActionException {

        long wait = INITIAL_RETRY_WAIT_MILLISECONDS;
        long start = System.currentTimeMillis();

        while (true) {
            try {
                submitUpdateRequest(updateRequest, updateConsumer);
                return;
            } catch (RolloverActionException e) {
                if ((System.currentTimeMillis() - start) > MAX_RETRY_MILLISECONDS) {
                    throw e;
                } else {
                    LOGGER.warn("failed to update catalog, will retry in {} milliseconds", wait);
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e1) {
                        LOGGER.warn("interrupted while waiting to retry update request", e1);
                        Thread.interrupted();
                        return;
                    }
                    long timeRemaining =
                            MAX_RETRY_MILLISECONDS - (System.currentTimeMillis() - start);
                    wait = Math.min(wait * 2, timeRemaining);
                }
            }
        }
    }

    private void submitUpdateRequest(UpdateRequest updateRequest, Consumer<Update> updateConsumer)
            throws RolloverActionException {
        try {
            catalogFramework.update(updateRequest)
                    .getUpdatedMetacards()
                    .forEach(updateConsumer);
        } catch (IngestException | SourceUnavailableException e) {
            throw new RolloverActionException(String.format(
                    "unable to submit update request to catalog framework: updateRequest=%s",
                    updateRequest), e);
        }
    }

    private UpdateRequest createUpdateRequest(String id, Metacard metacard) {
        return new UpdateRequestImpl(id, metacard);
    }

    private void linkChildToParent(Metacard childMetacard) throws RolloverActionException {
        setDerivedAttribute(childMetacard);

        UpdateRequest updateChild = createUpdateRequest(childMetacard.getId(), childMetacard);

        submitChildUpdateRequest(updateChild);
    }

    private void setDerivedAttribute(Metacard childMetacard) {
        childMetacard.setAttribute(new AttributeImpl(Metacard.DERIVED, parentMetacard.getId()));
    }

    private CreateResponse submitStorageCreateRequest(CreateStorageRequest createRequest)
            throws RolloverActionException {
        try {
            return catalogFramework.create(createRequest);
        } catch (IngestException | SourceUnavailableException e) {
            throw new RolloverActionException(String.format(
                    "unable to submit storage create request to catalog framework: %s",
                    createRequest), e);
        }
    }

    private CreateStorageRequest createStorageRequest(ContentItem contentItem) {
        return new CreateStorageRequestImpl(Collections.singletonList(contentItem),
                new HashMap<>());
    }

    private void bindSecuritySubject() {
        ThreadContext.bind(security.getSystemSubject());
    }

    private ContentItem createContentItem(MetacardImpl metacard, String fileName,
            ByteSource byteSource) {
        return new ContentItemImpl(byteSource, Constants.MPEGTS_MIME_TYPE, fileName, metacard);
    }

    private void createParentMetacard() throws RolloverActionException {
        try {
            createParentIfUnset();
        } catch (MetacardCreationException | SourceUnavailableException | IngestException e) {
            throw new RolloverActionException(String.format(
                    "unable to create parent metacard: sourceUri=%s",
                    streamProcessor.getStreamUri()), e);
        }
    }

    private void createParentIfUnset()
            throws MetacardCreationException, SourceUnavailableException, IngestException {
        if (parentMetacard == null) {
            MetacardImpl metacard = createInitialMetacard();

            setParentResourceUri(metacard);
            setParentTitle(metacard);
            setParentContentType(metacard);

            CreateRequest createRequest = new CreateRequestImpl(metacard);

            submitParentCreateRequest(createRequest);

        }
    }

    private void submitParentCreateRequest(CreateRequest createRequest)
            throws IngestException, SourceUnavailableException {
        catalogFramework.create(createRequest)
                .getCreatedMetacards()
                .forEach(createdMetacard -> {
                    LOGGER.info("created parent metacard: metacard={}", createdMetacard.getId());
                    parentMetacard = createdMetacard;
                });
    }

    private void setParentResourceUri(MetacardImpl metacard) {
        streamProcessor.getStreamUri()
                .ifPresent(metacard::setResourceURI);
    }

    private void setParentContentType(MetacardImpl metacard) {
        metacard.setContentTypeName(Constants.MPEGTS_MIME_TYPE);
    }

    private void setParentTitle(MetacardImpl metacard) {
        streamProcessor.getTitle()
                .ifPresent(metacard::setTitle);
    }

    private MetacardImpl createInitialMetacard() throws MetacardCreationException {
        return new MetacardImpl(metacardTypeList.stream()
                .findFirst()
                .orElseThrow(() -> new MetacardCreationException("unable to find a metacard type")));
    }

    private void enforceRequiredMetacardFields(MetacardImpl metacard, String fileName) {
        if (metacard != null) {
            setIdAttribute(metacard);

            setTitleAttribute(metacard, fileName);

            setPointOfContactAttribute(metacard);
        }
    }

    private void setPointOfContactAttribute(MetacardImpl metacard) {
        String subjectName = SubjectUtils.getName(security.getSystemSubject());

        metacard.setAttribute(new AttributeImpl(Metacard.POINT_OF_CONTACT,
                subjectName == null ? "" : subjectName));
    }

    private void setTitleAttribute(MetacardImpl metacard, String fileName) {
        if (StringUtils.isBlank(metacard.getTitle())) {
            metacard.setTitle(fileName);
        }
    }

    private void setIdAttribute(MetacardImpl metacard) {
        if (metacard.getId() == null) {
            metacard.setId(UUID.randomUUID()
                    .toString()
                    .replaceAll("-", ""));
        }
    }

}
