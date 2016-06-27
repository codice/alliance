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
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;

public class GeometryUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryUtility.class);

    /**
     * Create the union of multi-valued attribute that contains WKT. If the union cannot
     * be computed, then this method returns {@link Optional#empty()}
     *
     * @param wktReader non-null
     * @param wktWriter non-null
     * @param attribute non-null
     * @return optional wkt string
     */
    public static Optional<String> createUnionOfGeometryAttribute(WKTReader wktReader,
            WKTWriter wktWriter, Attribute attribute) {
        return createUnionOfGeometryAttribute(wktReader,
                wktWriter,
                attribute,
                UnaryOperator.identity(),
                UnaryOperator.identity());
    }

    /**
     * Create the union of multi-valued attribute that contains WKT. If the union cannot
     * be computed, then this method returns {@link Optional#empty()}
     *
     * @param wktReader                 non-null
     * @param wktWriter                 non-null
     * @param attribute                 non-null
     * @param postUnionGeometryOperator non-null, transform the geometry (e.g. simplify or normalize)
     * @param preUnionGeometryOperator  non-null, transform the geometry just before the union operation (e.g. reduce precision)
     * @return optional wkt string
     */
    public static Optional<String> createUnionOfGeometryAttribute(WKTReader wktReader,
            WKTWriter wktWriter, Attribute attribute,
            UnaryOperator<Geometry> postUnionGeometryOperator,
            UnaryOperator<Geometry> preUnionGeometryOperator) {
        return attribute.getValues()
                .stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(wkt -> wktToGeometry(wkt, wktReader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(preUnionGeometryOperator)
                .reduce(Geometry::union)
                .map(postUnionGeometryOperator)
                .map(wktWriter::write);
    }

    public static Optional<Geometry> wktToGeometry(String wkt, WKTReader wktReader) {
        try {
            return Optional.of(wktReader.read(wkt));
        } catch (ParseException e) {
            LOGGER.warn("unable to convert WKT to a Geometry object: wkt={}", wkt, e);
        }
        return Optional.empty();
    }

}
