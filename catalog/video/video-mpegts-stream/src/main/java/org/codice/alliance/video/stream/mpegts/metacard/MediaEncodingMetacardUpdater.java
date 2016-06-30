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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codice.alliance.libs.klv.AttributeNameConstants;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Set the media encodings attribute of the parent to be the union of the parent and child.
 */
public class MediaEncodingMetacardUpdater implements MetacardUpdater {

    private static final String ATTRIBUTE_NAME = AttributeNameConstants.MEDIA_ENCODING;

    @Override
    public void update(Metacard parent, Metacard child) {
        Set<Serializable> mediaEncodings = new HashSet<>();
        addAll(mediaEncodings, parent);
        addAll(mediaEncodings, child);
        List<Serializable> list = new ArrayList<>(mediaEncodings);
        if(!list.isEmpty()) {
            parent.setAttribute(new AttributeImpl(ATTRIBUTE_NAME, list));
        }
    }

    private void addAll(Set<Serializable> serializables, Metacard metacard) {
        Attribute parentAttribute = metacard.getAttribute(ATTRIBUTE_NAME);
        if (parentAttribute != null && parentAttribute.getValues() != null) {
            serializables.addAll(parentAttribute.getValues());
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MediaEncodingMetacardUpdater{}";
    }
}
