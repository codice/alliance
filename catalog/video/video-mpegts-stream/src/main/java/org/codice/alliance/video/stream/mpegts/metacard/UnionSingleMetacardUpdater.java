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

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.codice.alliance.video.stream.mpegts.Context;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Set the parent to a single value of the union of the child and parent values.
 */
public class UnionSingleMetacardUpdater implements MetacardUpdater {

    private final String attributeName;

    public UnionSingleMetacardUpdater(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public final void update(Metacard parent, Metacard child, Context context) {

        Stream.of(parent, child)
                .map(this::getAttribute)
                .filter(Objects::nonNull)
                .map(Attribute::getValues)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .findFirst()
                .ifPresent(serializable -> {
                    parent.setAttribute(new AttributeImpl(attributeName, serializable));
                });

    }

    private Attribute getAttribute(Metacard metacard) {
        return metacard.getAttribute(attributeName);
    }

}
