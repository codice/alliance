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

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Generate the location metadata based on the klv corner data. Callers must supply a
 * {@link Configuration} that contains a postive (>0)
 * Integer for {@link Configuration#SUBSAMPLE_COUNT}.
 */
public class LocationKlvProcessor implements KlvProcessor {

    public static final Integer MIN_SUBSAMPLE_COUNT = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationKlvProcessor.class);

    private final GeometryFunction geometryFunction;

    public LocationKlvProcessor() {
        this(GeometryFunction.IDENTITY);
    }

    /**
     * @param geometryFunction transform the final Geometry object (must be non-null)
     */
    public LocationKlvProcessor(GeometryFunction geometryFunction) {
        notNull(geometryFunction, "geometryFunction must be non-null");
        this.geometryFunction = geometryFunction;
    }

    public GeometryFunction getGeometryFunction() {
        return geometryFunction;
    }

    private Optional<KlvHandler> find(Map<String, KlvHandler> handlers, String name) {
        return handlers.values()
                .stream()
                .filter(handler -> handler.getAttributeName()
                        .equals(name))
                .findFirst();
    }

    @Override
    public void process(Map<String, KlvHandler> handlers, Metacard metacard,
            Configuration configuration) {

        if (configuration.get(Configuration.SUBSAMPLE_COUNT) == null || (Integer) configuration.get(
                Configuration.SUBSAMPLE_COUNT) < MIN_SUBSAMPLE_COUNT) {
            LOGGER.warn(
                    "the subsample count configuration is missing or incorrectly configured (the minimum subsample count is {}), skipping location klv processing",
                    MIN_SUBSAMPLE_COUNT);
            return;
        }

        Integer subsampleCount = (Integer) configuration.get(Configuration.SUBSAMPLE_COUNT);

        find(handlers, AttributeNameConstants.CORNER).ifPresent(cornerHandler -> {
            if (cornerHandler instanceof GeoBoxHandler) {
                subsample((GeoBoxHandler) cornerHandler, subsampleCount).asAttribute()
                        .ifPresent(attribute -> setLocationFromCornerAttribute(metacard,
                                attribute));
            }
        });

    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private void setLocationFromCornerAttribute(Metacard metacard, Attribute attribute) {
        WKTReader wktReader = new WKTReader();
        WKTWriter wktWriter = new WKTWriter();

        GeometryUtility.createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute,
                geometryFunction)
                .ifPresent(location -> metacard.setAttribute(new AttributeImpl(Metacard.GEOGRAPHY,
                        location)));

    }

    GeoBoxHandler subsample(GeoBoxHandler geoBoxHandler, Integer subsampleCount) {

        if (geoBoxHandler.getRawGeoData()
                .isEmpty()) {
            return geoBoxHandler;
        }

        int size = geoBoxHandler.getRawGeoData()
                .get(geoBoxHandler.getLatitude1())
                .size();

        if (size <= subsampleCount) {
            return geoBoxHandler;
        }

        GeoBoxHandler out = new GeoBoxHandler(geoBoxHandler.getAttributeName(),
                geoBoxHandler.getLatitude1(),
                geoBoxHandler.getLongitude1(),
                geoBoxHandler.getLatitude2(),
                geoBoxHandler.getLongitude2(),
                geoBoxHandler.getLatitude3(),
                geoBoxHandler.getLongitude3(),
                geoBoxHandler.getLatitude4(),
                geoBoxHandler.getLongitude4());

        geoBoxHandler.getRawGeoData()
                .forEach((key, value) -> {
                    for (int i = 0; i < subsampleCount; i++) {
                        out.accept(key, value.get(i * size / subsampleCount));
                    }
                });

        return out;
    }

}
