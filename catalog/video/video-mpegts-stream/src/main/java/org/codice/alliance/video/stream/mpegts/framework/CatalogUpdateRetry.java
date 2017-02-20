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
package org.codice.alliance.video.stream.mpegts.framework;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class CatalogUpdateRetry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogUpdateRetry.class);

    public void submitUpdateRequestWithRetry(CatalogFramework catalogFramework,
            UpdateRequest updateRequest, long initialSleepSeconds,
            long initialRetryWaitMilliseconds, long maxRetryMilliseconds,
            Consumer<Update> updateConsumer) {

        if (sleep(TimeUnit.SECONDS.toMillis(initialSleepSeconds))) {
            return;
        }

        RetryPolicy retryPolicy = new RetryPolicy().retryOn(IngestException.class,
                SourceUnavailableException.class)
                .withBackoff(initialRetryWaitMilliseconds,
                        maxRetryMilliseconds,
                        TimeUnit.MILLISECONDS);

        Failsafe.with(retryPolicy)
                .onFailedAttempt(throwable -> LOGGER.debug(
                        "failed to update catalog, will retry: updateRequest={}",
                        updateRequest))
                .onFailure(throwable -> LOGGER.debug("failed to update catalog: updateRequest={}",
                        updateRequest))
                .run(() -> catalogFramework.update(updateRequest)
                        .getUpdatedMetacards()
                        .forEach(updateConsumer));
    }

    private boolean sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            LOGGER.trace("interrupted while waiting to attempt update request", e);
            Thread.interrupted();
            return true;
        }
        return false;
    }

}
