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
package com.connexta.alliance.video.stream.mpegts.metacard;

import com.connexta.alliance.libs.klv.AttributeNameConstants;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * If the parent does not have a start time and the child does have a start time, then set the
 * parent start to time to the child's start time.
 */
public class TemporalStartMetacardUpdater implements MetacardUpdater {

    /**
     * Metacard attribute name
     */
    static final String ATTRIBUTE_NAME = AttributeNameConstants.TEMPORAL_START;

    @Override
    public void update(Metacard parent, Metacard child) {
        if (parent.getAttribute(ATTRIBUTE_NAME) == null
                && child.getAttribute(ATTRIBUTE_NAME) != null) {
            parent.setAttribute(new AttributeImpl(ATTRIBUTE_NAME,
                    child.getAttribute(ATTRIBUTE_NAME)
                            .getValue()));
        }
    }
}
