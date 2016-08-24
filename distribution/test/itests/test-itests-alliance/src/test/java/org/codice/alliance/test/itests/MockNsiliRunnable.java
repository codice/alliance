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
package org.codice.alliance.test.itests;

import org.codice.alliance.nsili.mockserver.server.MockNsili;

public class MockNsiliRunnable implements Runnable {

    private int httpWebPort;

    private int ftpWebPort;

    private int corbaPort;

    public MockNsiliRunnable(int httpWebPort, int ftpWebPort, int corbaPort) {
        this.httpWebPort = httpWebPort;
        this.ftpWebPort = ftpWebPort;
        this.corbaPort = corbaPort;
    }

    @Override
    public void run() {
        MockNsili mockNsili = MockNsili.getInstance();
        mockNsili.startWebServer(this.httpWebPort);
        mockNsili.startFtpWebServer(this.ftpWebPort);
        mockNsili.startMockServer(this.corbaPort);
    }
}