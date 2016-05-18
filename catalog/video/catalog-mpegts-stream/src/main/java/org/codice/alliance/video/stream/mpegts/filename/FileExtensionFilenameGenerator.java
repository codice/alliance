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
package org.codice.alliance.video.stream.mpegts.filename;

/**
 * Ensures that a filename always ends with a specific extension. If the filename does not
 * end with the specified extension, then it adds it to the filename. Does not attempt to
 * rewrite an existing extension. The test is case-insensitive.
 */
public class FileExtensionFilenameGenerator extends BaseFilenameGenerator {

    private final String extension;

    public FileExtensionFilenameGenerator(String extension) {
        this.extension = extension;
    }

    @Override
    protected String doGenerateFilename(String baseFilename) {
        if (baseFilename.toLowerCase()
                .endsWith("." + extension.toLowerCase())) {
            return baseFilename;
        }
        return baseFilename + "." + extension;
    }

    @Override
    public String toString() {
        return "FileExtensionFilenameGenerator{" +
                "extension='" + extension + '\'' +
                '}';
    }
}
