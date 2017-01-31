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

import java.util.concurrent.ExecutorService;

import org.codice.alliance.video.stream.mpegts.Context;

/**
 * Shutdown the rollover thread pool. Waits for rollover tasks to complete.
 */
public class ShutdownRolloverThreadPoolPlugin extends BaseStreamShutdownPlugin {

    @Override
    protected void doOnShutdown(Context context) throws StreamShutdownException {
        ExecutorService executorService = context.getUdpStreamProcessor()
                .getRolloverThreadPool();

        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
