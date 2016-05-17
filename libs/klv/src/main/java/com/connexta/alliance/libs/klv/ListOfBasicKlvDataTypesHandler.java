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
package com.connexta.alliance.libs.klv;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.codice.ddf.libs.klv.KlvDataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Attribute;

/**
 * This handler expects a KlvDataElement that matches the Class passed into
 * {@link #ListOfBasicKlvDataTypesHandler(String, Class)} generates a list of values returned by
 * {@link KlvDataElement#getValue()}.
 */
class ListOfBasicKlvDataTypesHandler<T extends Serializable> extends BaseKlvHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ListOfBasicKlvDataTypesHandler.class);

    private Class<? extends KlvDataElement<T>> clazz;

    private List<T> list = new LinkedList<>();

    public ListOfBasicKlvDataTypesHandler(String attributeName,
            Class<? extends KlvDataElement<T>> clazz) {
        super(attributeName);
        this.clazz = clazz;
    }

    @Override
    public Optional<Attribute> asAttribute() {
        return asAttribute(list);
    }

    @Override
    public void accept(KlvDataElement klvDataElement) {
        if (!clazz.isInstance(klvDataElement)) {
            LOGGER.warn(
                    "non-{} data was passed to the ListOfBasicKlvDataTypesHandler: name = {} klvDataElement = {}",
                    clazz.getName(),
                    klvDataElement.getName(),
                    klvDataElement);
            return;
        }
        list.add(clazz.cast(klvDataElement)
                .getValue());
    }

    @Override
    public void reset() {
        list.clear();
    }

}
