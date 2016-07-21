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
package org.codice.alliance.transformer.nitf.gmti;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.transformer.nitf.common.SegmentHandler;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.TaggedRecordExtensionHandler;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreGroup;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.transform.CatalogTransformerException;

public class NitfGmtiTransformer extends SegmentHandler {

    private static final String ACFTB = "ACFTB";

    private static final String MTIRPB = "MTIRPB";

    private static final String TARGETS = "TARGETS";

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfGmtiTransformer.class);

    private static final String LOCATION_REGEX =
            "([+\\-]\\d{2}+\\.\\d{7}+)([+\\-]\\d{3}+\\.\\d{7})";

    private static final Pattern LOCATION_PATTERN = Pattern.compile(LOCATION_REGEX);

    private WKTReader wktReader;

    public Metacard transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard)
            throws IOException, CatalogTransformerException {

        if (nitfSegmentsFlow == null) {
            throw new IllegalArgumentException("argument 'nitfSegmentsFlow' may not be null.");
        }

        if (metacard == null) {
            throw new IllegalArgumentException("argument 'metacard' may not be null.");
        }

        nitfSegmentsFlow.fileHeader(header -> handleHeader(header, metacard))
                .end();

        String locationString = setLocation(metacard);

        try {
            LOGGER.debug("Location = " + locationString);

            if (locationString != null) {
                //validate the wkt
                Geometry geometry = wktReader.read(locationString);
                metacard.setAttribute(new AttributeImpl(Metacard.GEOGRAPHY, geometry.toText()));
            }
        } catch (ParseException e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return metacard;
    }

    private void handleHeader(TaggedRecordExtensionHandler header, Metacard metacard) {
        List<Tre> tres = header.getTREsRawStructure()
                .getTREs();

        tres.stream()
                .filter(tre -> ACFTB.equals(tre.getName()
                        .trim()))
                .forEach(tre -> handleSegmentHeader(metacard, tre, AcftbAttribute.values()));

        tres.stream()
                .filter(tre -> MTIRPB.equals(tre.getName()
                        .trim()))
                .forEach(tre -> {
                    handleSegmentHeader(metacard, tre, MtirpbAttribute.values());

                    try {
                        List<TreGroup> targets = tre.getEntry(TARGETS)
                                .getGroups();

                        targets.stream()
                                .forEach(group -> handleSegmentHeader(metacard,
                                        group,
                                        IndexedMtirpbAttribute.values()));
                    } catch (NitfFormatException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
    }

    private String setLocation(Metacard metacard) {
        Attribute locationAttribute =
                metacard.getAttribute(IndexedMtirpbAttribute.INDEXED_TARGET_LOCATION.getAttributeDescriptor()
                        .getName());

        if (locationAttribute != null) {
            StringBuilder stringBuilder = new StringBuilder("MULTIPOINT (");

            locationAttribute.getValues()
                    .stream()
                    .forEach(value -> {
                        parseLocation(stringBuilder, value.toString());
                    });

            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
            stringBuilder.append(")");
            return stringBuilder.toString();
        }

        return null;
    }

    private void parseLocation(StringBuilder stringBuilder, String locationString) {

        if (StringUtils.isEmpty(locationString)) {
            return;
        }

        Matcher matcher = LOCATION_PATTERN.matcher(locationString);

        if (matcher.matches()) {
            String lon = matcher.group(1);
            String lat = matcher.group(2);

            stringBuilder.append(String.format("%s %s,", lon, lat));
        }
    }

    public void setWktReader(WKTReader wktReader) {
        this.wktReader = wktReader;
    }
}
