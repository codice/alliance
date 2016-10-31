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
package org.codice.alliance.libs.klv;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.AttributeImpl;

public abstract class BaseKlvHandler implements KlvHandler {

    private String attributeName;

    public BaseKlvHandler(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

    protected final <T extends Serializable> Optional<Attribute> asAttribute(Collection<T> col) {
        if (col.isEmpty()) {
            return Optional.empty();
        }
        List<Serializable> serials = new LinkedList<>(col);
        return Optional.of(new AttributeImpl(getAttributeName(), serials));
    }

    protected <T> int getMinimumListSize(Collection<List<T>> lists) {
        return lists.stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);
    }

    protected void subsample(Map<String, List<Double>> data, int subsampleCount, int size,
            BiConsumer<String, Double> biConsumer) {
        data.forEach((key, value) -> {
            for (int i = 0; i < subsampleCount; i++) {
                biConsumer.accept(key, value.get(i * size / subsampleCount));
            }
        });
    }
}
