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
package org.codice.alliance.video.stream.mpegts.rollover;

import java.io.File;

import ddf.catalog.data.impl.MetacardImpl;

public interface RolloverAction {

    /**
     * Populate a metacard (if non-null) and return a new metacard (may be the same metacard).
     *
     * @param metacard the metacard being populated
     * @param tempFile the temp file that contain content
     * @return a new metacard or the same metacard
     * @throws RolloverActionException
     */
    MetacardImpl doAction(MetacardImpl metacard, File tempFile) throws RolloverActionException;

    /**
     * Return a new metacard.
     *
     * @param tempFile the temp file that contain content
     * @return a new metacard or the same metacard
     * @throws RolloverActionException
     */
    MetacardImpl doAction(File tempFile) throws RolloverActionException;

}
