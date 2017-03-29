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
package org.codice.alliance.video.stream.mpegts.netty;

import java.io.File;
import java.util.Optional;

public class RotateResult {

    private final File file;

    private final Boolean isTimeout;

    public RotateResult(File file, Boolean isTimeout) {
        this.file = file;
        this.isTimeout = isTimeout;
    }

    public Optional<File> getFile() {
        return Optional.ofNullable(file);
    }

    public Boolean isTimeout() {
        return isTimeout;
    }
}
