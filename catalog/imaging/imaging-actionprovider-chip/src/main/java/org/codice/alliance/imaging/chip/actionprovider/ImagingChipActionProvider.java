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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codice.ddf.configuration.SystemBaseUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.action.Action;
import ddf.action.ActionProvider;
import ddf.action.impl.ActionImpl;
import ddf.catalog.data.Metacard;

public class ImagingChipActionProvider implements ActionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImagingChipActionProvider.class);

    public static final String TITLE = "Chip Image";

    public static final String DESCRIPTION =
            "Opens a new window to enter the boundaries of an image chip for a Metacard.";

    public static final String PATH = "/chipping/chipping.html";

    public static final String ID = "catalog.data.metacard.image.chipping";

    public static final String NITF_CONTENT_TYPE = "image/nitf";

    @Override
    public <T> List<Action> getActions(T input) {
        if (input == null) {
            LOGGER.warn("Metacard can not be null.");
            return new ArrayList<>();
        }

        if (canHandle(input)) {
            final Metacard metacard = (Metacard) input;

            URL url;
            String chipPath = null;
            try {
                chipPath =
                        SystemBaseUrl.getBaseUrl() + PATH + "?id=" + metacard.getId() + "&source="
                                + metacard.getSourceId();
                url = new URL(chipPath);
            } catch (MalformedURLException e) {
                LOGGER.debug("Invalid URL for chipping path : {}", chipPath, e);
                return new ArrayList<>();
            }
            return Arrays.asList(new ActionImpl(getId(), TITLE, DESCRIPTION, url));
        }
        return new ArrayList<>();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public <T> boolean canHandle(T subject) {
        if (subject instanceof Metacard) {
            Metacard metacard = (Metacard) subject;
            String contentTypeName = metacard.getContentTypeName();
            if (NITF_CONTENT_TYPE.equals(contentTypeName) && metacard.getLocation() != null) {
                return true;
            }
        }
        return false;
    }
}

