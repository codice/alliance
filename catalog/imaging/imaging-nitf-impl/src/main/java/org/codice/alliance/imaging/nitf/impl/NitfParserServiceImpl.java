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
package org.codice.alliance.imaging.nitf.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import org.codice.alliance.imaging.nitf.api.NitfParserService;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

public class NitfParserServiceImpl implements NitfParserService {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public NitfSegmentsFlow parseNitf(InputStream inputStream, Boolean allData)
            throws NitfFormatException {
        if (inputStream == null) {
            throw new IllegalArgumentException("method argument 'inputStream' may not be null.");
        }

        if (allData != null && allData) {
            return new NitfParserInputFlow().inputStream(inputStream)
                    .allData();
        }

        return new NitfParserInputFlow().inputStream(inputStream)
                .headerOnly();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public NitfSegmentsFlow parseNitf(File nitfFile, Boolean allData)
            throws FileNotFoundException, NitfFormatException {
        if (nitfFile == null) {
            throw new IllegalArgumentException("method argument 'nitfFile' may not be null.");
        }

        if (allData != null && allData) {
            return new NitfParserInputFlow().file(nitfFile)
                    .allData();
        }

        return new NitfParserInputFlow().file(nitfFile)
                .headerOnly();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void endNitfSegmentsFlow(NitfSegmentsFlow nitfSegmentsFlow) {
        Optional.of(nitfSegmentsFlow)
                .ifPresent(flow -> flow.end());
    }
}
