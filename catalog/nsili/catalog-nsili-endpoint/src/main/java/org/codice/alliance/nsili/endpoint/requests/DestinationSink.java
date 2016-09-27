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

package org.codice.alliance.nsili.endpoint.requests;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ddf.catalog.data.Metacard;

public interface DestinationSink {

    /**
     * Write a data stream to a destination.
     *
     * @param fileData    the data to write
     * @param size        the number of bytes to be written
     * @param name        the name of the file to be written
     * @param contentType the content type of the file to be written
     * @param metacards   the metacards associated with the fileData
     * @throws IOException exception indicating that data could not be written
     */
    void writeFile(InputStream fileData, long size, String name, String contentType,
            List<Metacard> metacards) throws IOException;
}