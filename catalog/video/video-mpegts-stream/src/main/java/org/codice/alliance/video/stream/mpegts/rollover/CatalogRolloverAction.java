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
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.video.stream.mpegts.Constants;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import ddf.catalog.CatalogFramework;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.impl.CreateStorageRequestImpl;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.Subject;
import ddf.security.SubjectUtils;

/**
 * Stores the child content, links the child to the parent, and updates the parent's
 * location with the union of the child's location.
 */
public class CatalogRolloverAction extends BaseRolloverAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRolloverAction.class);

    private static final long MAX_RETRY_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);

    private static final long INITIAL_RETRY_WAIT_MILLISECONDS = TimeUnit.MILLISECONDS.toMillis(500);

    private final FilenameGenerator filenameGenerator;

    private final CatalogFramework catalogFramework;

    private final Context context;

    private final MetacardUpdater parentMetacardUpdater;

    private String filenameTemplate;

    /**
     * @param filenameGenerator     must be non-null
     * @param filenameTemplate      must be non-null
     * @param catalogFramework      must be non-null
     * @param context               must be non-null
     * @param parentMetacardUpdater must be non-null
     */
    public CatalogRolloverAction(FilenameGenerator filenameGenerator, String filenameTemplate,
            CatalogFramework catalogFramework, Context context,
            MetacardUpdater parentMetacardUpdater) {
        notNull(filenameGenerator, "filenameGenerator must be non-null");
        notNull(filenameTemplate, "filenameTemplate must be non-null");
        notNull(catalogFramework, "catalogFramework must be non-null");
        notNull(context, "context must be non-null");
        notNull(parentMetacardUpdater, "parentMetacardUpdater must be non-null");

        this.filenameGenerator = filenameGenerator;
        this.filenameTemplate = filenameTemplate;
        this.catalogFramework = catalogFramework;
        this.context = context;
        this.parentMetacardUpdater = parentMetacardUpdater;
    }

    @Override
    public String toString() {
        return "CatalogRolloverAction{" +
                "catalogFramework=" + catalogFramework +
                ", filenameTemplate='" + filenameTemplate + '\'' +
                ", filenameGenerator=" + filenameGenerator +
                ", parentMetacardUpdater=" + parentMetacardUpdater +
                '}';
    }

    @Override
    public MetacardImpl doAction(MetacardImpl metacard, File tempFile)
            throws RolloverActionException {

        Subject subject = context.getUdpStreamProcessor()
                .getSubject();

        if (subject == null) {
            LOGGER.warn("no security subject available, cannot upload video chunk");
            return metacard;
        }

        return subject.execute(() -> {
            String fileName = generateFilename();

            enforceRequiredMetacardFields(metacard, fileName);

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
        });

    }

    private String generateFilename() {
        return filenameGenerator.generateFilename(filenameTemplate);
    }

    private void updateParentWithChildMetadata(Metacard childMetacard)
            throws RolloverActionException {
        if (context.getParentMetacard()
                .isPresent()) {
            Metacard parentMetacard = context.getParentMetacard()
                    .get();
            parentMetacardUpdater.update(parentMetacard, childMetacard);
            UpdateRequest updateRequest = createUpdateRequest(parentMetacard.getId(),
                    parentMetacard);
            submitParentUpdateRequest(updateRequest);
        }
    }

    private void submitParentUpdateRequest(UpdateRequest updateRequest)
            throws RolloverActionException {
        if (context.getParentMetacard()
                .isPresent()) {
            submitUpdateRequestWithRetry(updateRequest, update -> {
                LOGGER.info("updated parent metacard: newMetacard={}",
                        update.getNewMetacard()
                                .getId());
                context.setParentMetacard(update.getNewMetacard());
            });
        }
    }

    private void submitChildUpdateRequest(UpdateRequest updateRequest)
            throws RolloverActionException {
        submitUpdateRequestWithRetry(updateRequest,
                update -> LOGGER.info("updated child metacard with link to parent: child={}",
                        update.getNewMetacard()
                                .getId()));
    }

    /**
     * @param sleep milliseconds to sleep
     * @return true if interrupted
     */
    private boolean sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            LOGGER.warn("interrupted while waiting to attempt update request", e);
            Thread.interrupted();
            return true;
        }
        return false;
    }

    private void submitUpdateRequestWithRetry(UpdateRequest updateRequest,
            Consumer<Update> updateConsumer) throws RolloverActionException {

        if (sleep(TimeUnit.SECONDS.toMillis(context.getUdpStreamProcessor()
                .getMetacardUpdateInitialDelay()))) {
            return;
        }

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
                    if (sleep(wait)) {
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
        if (context.getParentMetacard()
                .isPresent()) {
            childMetacard.setAttribute(new AttributeImpl(Metacard.DERIVED,
                    context.getParentMetacard()
                            .get()
                            .getId()));
        }
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

    private ContentItem createContentItem(MetacardImpl metacard, String fileName,
            ByteSource byteSource) {
        return new ContentItemImpl(byteSource, Constants.MPEGTS_MIME_TYPE, fileName, metacard);
    }

    private void enforceRequiredMetacardFields(MetacardImpl metacard, String fileName) {
        if (metacard != null) {
            setIdAttribute(metacard);

            setTitleAttribute(metacard, fileName);

            setPointOfContactAttribute(metacard);
        }
    }

    private void setPointOfContactAttribute(MetacardImpl metacard) {

        String subjectName = SubjectUtils.getName(context.getUdpStreamProcessor()
                .getSubject());

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
