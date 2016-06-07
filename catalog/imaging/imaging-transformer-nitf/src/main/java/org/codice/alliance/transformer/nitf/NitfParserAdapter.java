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
package org.codice.alliance.transformer.nitf;

import java.io.InputStream;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

import ddf.catalog.transform.CatalogTransformerException;

public class NitfParserAdapter {

    public NitfSegmentsFlow parseNitf(InputStream inputStream) throws NitfFormatException {

        if (inputStream == null) {
            throw new IllegalArgumentException("method argument 'inputStream' may not be null.");
        }

        return new NitfParserInputFlow().inputStream(inputStream)
                .allData();
    }

    /**
     * @param exception the exception to be wrapped.
     * @throws CatalogTransformerException in every case.
     */
    public void wrapException(Exception exception) throws CatalogTransformerException {
        throw new CatalogTransformerException(exception);
    }
}
