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
 * If the child has an end time, then set the parent's end time to the child's value.
 */
public class TemporalEndMetacardUpdater implements MetacardUpdater {

    private static final String ATTRIBUTE_NAME = AttributeNameConstants.TEMPORAL_END;

    @Override
    public void update(Metacard parent, Metacard child) {
        if (child.getAttribute(ATTRIBUTE_NAME) != null) {
            parent.setAttribute(new AttributeImpl(ATTRIBUTE_NAME,
                    child.getAttribute(ATTRIBUTE_NAME)
                            .getValue()));
        }
    }
}
