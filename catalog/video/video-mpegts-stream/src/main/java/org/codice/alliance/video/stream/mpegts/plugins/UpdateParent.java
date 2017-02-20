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
package org.codice.alliance.video.stream.mpegts.plugins;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.framework.FrameworkUtilities;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.UpdateRequestImpl;

/**
 * Update the parent metacard by calling an {@link UpdateField} and then by calling the
 * catalog framework.
 */
public class UpdateParent implements FindChildrenStreamEndPlugin.Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateParent.class);

    private static final long INITIAL_RETY_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    private static final long MAX_RETRY_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);

    private final UpdateField updateField;

    public UpdateParent(UpdateField updateField) {
        this.updateField = updateField;
    }

    @Override
    public void handle(Context context, Metacard parent, List<Metacard> children) {
        updateField.updateField(parent, children);
    }

    @Override
    public void end(Context context, Metacard parentMetacard) {

        updateField.end(parentMetacard);

        UpdateRequest updateRequest = createUpdateRequest(parentMetacard);

        update(context, parentMetacard, updateRequest);

    }

    private void update(Context context, Metacard parentMetacard, UpdateRequest updateRequest) {
        try {
            FrameworkUtilities.submitUpdateRequestWithRetry(context.getUdpStreamProcessor()
                            .getCatalogFramework(),
                    updateRequest,
                    context.getUdpStreamProcessor()
                            .getMetacardUpdateInitialDelay(),
                    INITIAL_RETY_MILLISECONDS,
                    MAX_RETRY_MILLISECONDS,
                    update -> {
                        LOGGER.debug("updated parent metacard: newMetacard={}",
                                update.getNewMetacard()
                                        .getId());
                        context.setParentMetacard(update.getNewMetacard());
                    });
        } catch (RolloverActionException e) {
            LOGGER.debug("unable to update parent metacard: metacardId={}",
                    parentMetacard.getId(),
                    e);
        }
    }

    private UpdateRequest createUpdateRequest(Metacard parentMetacard) {
        return new UpdateRequestImpl(parentMetacard.getId(), parentMetacard);
    }

    /**
     * Update a field in the parent metacard.
     */
    interface UpdateField {
        /**
         * Called for each batch of child metacards to be processed.
         *
         * @param parent   not-null
         * @param children not-null
         */
        void updateField(Metacard parent, List<Metacard> children);

        /**
         * Called after the last batch is submitted to {@link #updateField(Metacard, List)}.
         *
         * @param parent not-null
         */
        void end(Metacard parent);
    }
}
