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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Attribute;

/**
 * This handler expects pairs of latitude and longitude values. It generates WKT Points.
 */
class LatitudeLongitudeHandler extends BaseKlvHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatitudeLongitudeHandler.class);

    private Map<String, List<Double>> map = new HashMap<>();

    private String latitudeFieldName;

    private String longitudeFieldName;

    public LatitudeLongitudeHandler(String attributeName, String latitudeFieldName,
            String longitudeFieldName) {
        super(attributeName);
        this.latitudeFieldName = latitudeFieldName;
        this.longitudeFieldName = longitudeFieldName;
    }

    public String getLongitudeFieldName() {
        return longitudeFieldName;
    }

    public String getLatitudeFieldName() {
        return latitudeFieldName;
    }

    public Map<String, List<Double>> getRawGeoData() {
        return map;
    }

    @Override
    public Optional<Attribute> asAttribute() {

        int minimumListSize = map.values()
                .stream()
                .min((a, b) -> Integer.compare(a.size(), b.size()))
                .orElse(Collections.emptyList())
                .size();

        List<String> pairs = new ArrayList<>();

        for (int i = 0; i < minimumListSize; i++) {
            pairs.add(String.format("POINT (%f %f)",
                    map.get(longitudeFieldName)
                            .get(i),
                    map.get(latitudeFieldName)
                            .get(i)));
        }

        return asAttribute(pairs);
    }

    @Override
    public void accept(KlvDataElement klvDataElement) {
        if (!(klvDataElement instanceof KlvIntegerEncodedFloatingPoint)) {
            LOGGER.warn(
                    "non-KlvIntegerEncodedFloatingPoint data was passed to the LatitudeLongitudeHandler: name = {} klvDataElement = {}",
                    klvDataElement.getName(),
                    klvDataElement);
            return;
        }
        map.putIfAbsent(klvDataElement.getName(), new ArrayList<>());
        map.get(klvDataElement.getName())
                .add(((KlvIntegerEncodedFloatingPoint) klvDataElement).getValue());
    }

    @Override
    public void reset() {
        map.clear();
    }

}
