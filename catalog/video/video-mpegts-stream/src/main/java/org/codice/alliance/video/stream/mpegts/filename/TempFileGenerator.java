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
package org.codice.alliance.video.stream.mpegts.filename;

import java.io.File;
import java.io.IOException;

/**
 * Generate temporary files. Implementations should should call
 * {@link File#deleteOnExit()} on the file object before returning it if
 * the creation of that object results in the automatic creation of the
 * actual file.
 */
public interface TempFileGenerator {

    /**
     * Return a temporary file.
     *
     * @return non-null File object
     * @throws IOException
     */
    File generate() throws IOException;

}
