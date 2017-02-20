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
package org.codice.alliance.video.stream.mpegts.metacard;

import org.codice.alliance.libs.klv.AttributeNameConstants;

import ddf.catalog.data.Metacard;

/**
 * If the parent does not have a start time and the child does have a start time, then set the
 * parent start to time to the child's start time.
 */
public class TemporalStartMetacardUpdater extends AbstractBasicMetacardUpdater {

    public TemporalStartMetacardUpdater() {
        super(AttributeNameConstants.TEMPORAL_START);
    }

    @Override
    protected boolean isCondition(Metacard parent, Metacard child) {
        return parent.getAttribute(AttributeNameConstants.TEMPORAL_START) == null;
    }

}
