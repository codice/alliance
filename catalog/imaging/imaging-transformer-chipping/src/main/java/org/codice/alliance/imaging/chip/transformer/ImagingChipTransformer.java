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
package org.codice.alliance.imaging.chip.transformer;

import java.io.Serializable;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Header;

import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;

/**
 * This interface extends MetacardTransformer and duplicates the only method found there.  Its
 * purpose is to add the @Body and @Header annotations and serve as the defining interface for
 * a MetacardTransformer Camel proxy.
 */
public interface ImagingChipTransformer extends MetacardTransformer {
    /**
     * {@inheritDoc}
     */
    BinaryContent transform(@Body Metacard metacard,
            @Header("args") Map<String, Serializable> arguments) throws CatalogTransformerException;
}

