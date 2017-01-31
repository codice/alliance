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

import java.util.concurrent.Executors;

import org.codice.alliance.video.stream.mpegts.Context;

public class RolloverThreadPoolStreamCreationPlugin extends BaseStreamCreationPlugin {

    private static final int ROLLOVER_THREAD_POOL_THREAD_COUNT = 10;

    @Override
    protected void doOnCreate(Context context) throws StreamCreationException {
        context.getUdpStreamProcessor()
                .setRolloverThreadPool(Executors.newFixedThreadPool(
                        ROLLOVER_THREAD_POOL_THREAD_COUNT));
    }
}
