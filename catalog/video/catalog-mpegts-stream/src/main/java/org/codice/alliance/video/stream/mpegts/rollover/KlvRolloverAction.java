/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.rollover;

import java.io.File;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvProcessor;

import ddf.catalog.data.impl.MetacardImpl;

public class KlvRolloverAction extends BaseRolloverAction {

    private Integer klvLocationSubsampleCount;

    private Lock klvHandlerMapLock;

    private KlvProcessor klvProcessor;

    private Map<String, KlvHandler> klvHandlerMap;

    public KlvRolloverAction(Map<String, KlvHandler> klvHandlerMap, Lock klvHandlerMapLock,
            Integer klvLocationSubsampleCount, KlvProcessor klvProcessor) {
        this.klvHandlerMap = klvHandlerMap;
        this.klvHandlerMapLock = klvHandlerMapLock;
        this.klvLocationSubsampleCount = klvLocationSubsampleCount;
        this.klvProcessor = klvProcessor;
    }

    @Override
    public MetacardImpl doAction(MetacardImpl metacard, File tempFile)
            throws RolloverActionException {
        KlvProcessor.Configuration klvProcessConfiguration = new KlvProcessor.Configuration();
        klvProcessConfiguration.set(KlvProcessor.Configuration.SUBSAMPLE_COUNT,
                klvLocationSubsampleCount);

        klvHandlerMapLock.lock();
        try {
            klvProcessor.process(klvHandlerMap, metacard, klvProcessConfiguration);

            klvHandlerMap.values()
                    .forEach(KlvHandler::reset);
        } finally {
            klvHandlerMapLock.unlock();
        }

        return metacard;
    }
}
