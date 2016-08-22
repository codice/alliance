/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.imaging.chip.actionprovider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.action.Action;
import ddf.action.MultiActionProvider;
import ddf.action.impl.ActionImpl;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;

public class ImagingChipActionProvider implements MultiActionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImagingChipActionProvider.class);

    public static final String TITLE = "Chip Image";

    public static final String DESCRIPTION = "Opens a new window to enter the boundaries of an image chip for a Metacard.";

    public static final String PATH = "/chipping/chipping.html";

    public static final String ID = "catalog.data.metacard.image.chipping";

    private static final String NITF_IMAGE_METACARD_TYPE = "isr.image";

    public static final String ORIGINAL_QUALIFIER = "original";

    private GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public <T> List<Action> getActions(T input) {
        if (input == null) {
            LOGGER.debug("Metacard can not be null.");
            return new ArrayList<>();
        }

        if (canHandle(input)) {
            final Metacard metacard = (Metacard) input;

            URL url;
            String chipPath = null;
            try {
                chipPath = String.format("%1s%2s?id=%3s&source=%4s",
                        SystemBaseUrl.getBaseUrl(),
                        PATH,
                        metacard.getId(),
                        metacard.getSourceId());
                url = new URL(chipPath);
            } catch (MalformedURLException e) {
                LOGGER.debug("Invalid URL for chipping path : {}", chipPath, e);
                return new ArrayList<>();
            }

            return Collections.singletonList(new ActionImpl(getId(), TITLE, DESCRIPTION, url));
        }

        return new ArrayList<>();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public <T> boolean canHandle(T subject) {
        boolean canHandle = false;

        if (subject instanceof Metacard) {
            Metacard metacard = (Metacard) subject;

            boolean isImageNitf = NITF_IMAGE_METACARD_TYPE
                    .equals(metacard.getMetacardType().getName());
            boolean hasLocation = hasValidLocation(metacard.getLocation());
            boolean hasDerivedImage = hasOriginalDerivedResource(metacard);

            canHandle = isImageNitf && hasLocation && hasDerivedImage;
        }

        return canHandle;
    }

    private boolean hasOriginalDerivedResource(Metacard metacard) { 
        Attribute attribute = metacard.getAttribute(Core.DERIVED_RESOURCE_URI);

        return Stream.of(attribute)
                .filter(Objects::nonNull)
                .flatMap(a -> a.getValues().stream())
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .anyMatch(this::hasOriginalQualifier);
    }

    private boolean hasOriginalQualifier(String uriString) {
        try {
            URI derivedResourceUri = new URI(uriString);

            // find the #original URI fragment
            if (ORIGINAL_QUALIFIER.equals(derivedResourceUri.getFragment())) {
                return true;
            }
        } catch (URISyntaxException use) {
            LOGGER.debug("Could not parse URI string [{}]", uriString);
        }
        return false;
    }

    private boolean hasValidLocation(String location) {
        boolean hasValidLocation = false;

        if (StringUtils.isNotBlank(location)) {
            try {
                // parse the WKT location to determine if it has valid format
                WKTReader wktReader = new WKTReader(geometryFactory);
                wktReader.read(location);
                hasValidLocation = true;
            } catch (ParseException e) {
                LOGGER.debug("Location [{}] is invalid, cannot chip this image", location);
            }
        }

        return hasValidLocation;
    }
}
