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

import java.util.Optional;

import org.codice.ddf.libs.klv.KlvDataElement;

import ddf.catalog.data.Attribute;

/**
 * The KlvHandler receives STANAG 4609 data with the {@link #accept(KlvDataElement)} method
 * and generates an optional {@link Attribute} with the {@link #asAttribute()} method.
 */
public interface KlvHandler {

    /**
     * Get the name of the metacard attribute that is associated with this handler.
     *
     * @return name of metacard attribute
     */
    String getAttributeName();

    /**
     * After {@link #accept(KlvDataElement)} has been called with all of the incoming data, this
     * method will convert it to an {@link Attribute}. If there is insufficient data to create
     * an Attribute, then the Optional will be empty.
     *
     * @return optional attribute
     */
    Optional<Attribute> asAttribute();

    /**
     * The handler accepts the incoming KLV metadata. If the incoming KLV metadata is not the
     * datatype expected by this handler, then the handler should log a warning and return.
     *
     * @param klvDataElement incoming klv metadata
     */
    void accept(KlvDataElement klvDataElement);

    /**
     * Reset the handler to its initial state.
     */
    void reset();

}
