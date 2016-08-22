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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.numerical.KlvLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Attribute;

/**
 * This handler expects dates as microseconds since epoch and it generates a list of Dates.
 */
class ListOfDatesHandler extends BaseKlvHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListOfDatesHandler.class);

    private List<Date> dateList = new LinkedList<>();

    public ListOfDatesHandler(String attributeName) {
        super(attributeName);
    }

    @Override
    public Optional<Attribute> asAttribute() {
        return asAttribute(dateList);
    }

    @Override
    public void accept(KlvDataElement klvDataElement) {
        if (!(klvDataElement instanceof KlvLong)) {
            LOGGER.debug(
                    "non-KlvLong data was passed to the ListOfDatesHandler: name = {} klvDataElement = {}",
                    klvDataElement.getName(),
                    klvDataElement);
            return;
        }
        dateList.add(new Date(TimeUnit.MICROSECONDS.toMillis(((KlvLong) klvDataElement).getValue())));
    }

    @Override
    public void reset() {
        dateList.clear();
    }

}
