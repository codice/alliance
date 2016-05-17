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
package com.connexta.alliance.video.stream.mpegts.filename;

import java.io.File;
import java.io.IOException;

/**
 * Creates a File object with {@link File#createTempFile(String, String)} where the file
 * always begins with "mpegts-stream-" and ends with ".ts".
 */
public class TempFileGeneratorImpl implements TempFileGenerator {

    private static final String TEMP_FILE_PREFIX = "mpegts-stream-";

    private static final String TEMP_FILE_SUFFIX = ".ts";

    @Override
    public File generate() throws IOException {
        File file = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        file.deleteOnExit();
        return file;
    }

}
