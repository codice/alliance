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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.junit.Before;
import org.junit.Test;

import ddf.catalog.transform.CatalogTransformerException;

public class TestNitfParserAdapter {
    private static final String GEO_NITF = "/i_3001a.ntf";

    private NitfParserAdapter nitfParserAdapter = null;

    @Before
    public void setUp() {
        this.nitfParserAdapter = new NitfParserAdapter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNitfNullInput() throws NitfFormatException {
        this.nitfParserAdapter.parseNitf(null);
    }

    @Test
    public void testParseNitf() throws NitfFormatException {
        InputStream is = getInputStream(GEO_NITF);
        NitfSegmentsFlow nitfSegmentsFlow = this.nitfParserAdapter.parseNitf(is);
        assertThat(nitfSegmentsFlow, is(notNullValue()));

    }

    @Test(expected = CatalogTransformerException.class)
    public void testWrapException() throws CatalogTransformerException {
        nitfParserAdapter.wrapException(new NullPointerException("Test Null Pointer Exception"));
    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }
}
