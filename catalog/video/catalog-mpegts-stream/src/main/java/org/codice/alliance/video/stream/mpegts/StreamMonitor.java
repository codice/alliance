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
package org.codice.alliance.video.stream.mpegts;

import java.net.URI;
import java.util.Optional;

public interface StreamMonitor {

    /**
     * Get the URI of the stream associated with this stream processor.
     *
     * @return optional uri of the stream
     */
    Optional<URI> getStreamUri();

    /**
     * Get the title string to be used for the parent metacard.
     *
     * @return optional title of the stream
     */
    Optional<String> getTitle();
}
