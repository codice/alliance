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

import org.codice.alliance.video.stream.mpegts.Context;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Copies child attribute to parent if a specific condition is met.
 */
public abstract class AbstractBasicMetacardUpdater implements MetacardUpdater {

    private final String attributeName;

    protected AbstractBasicMetacardUpdater(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public final void update(Metacard parent, Metacard child, Context context) {

        if (parent.getMetacardType()
                .getAttributeDescriptor(attributeName) == null) {
            return;
        }

        if (isChildAvailable(child) && isCondition(parent, child)) {
            parent.setAttribute(new AttributeImpl(attributeName,
                    child.getAttribute(attributeName)
                            .getValue()));
        }
    }

    private boolean isChildAvailable(Metacard child) {
        return child.getAttribute(attributeName) != null;
    }

    /**
     * The child is guaranteed to already have the attribute.
     *
     * @param parent parent metacard
     * @param child  child metacard
     * @return true if the parent should be set with the child's value
     */
    protected abstract boolean isCondition(Metacard parent, Metacard child);

}
