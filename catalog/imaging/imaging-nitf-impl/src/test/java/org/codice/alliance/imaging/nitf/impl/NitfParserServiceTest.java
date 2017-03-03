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

import org.codice.alliance.imaging.nitf.api.NitfParserService;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NitfParserServiceTest {
    private static final String GEO_NITF = "/i_3001a.ntf";

    private NitfParserService nitfParserAdapter = null;

    @Before
    public void setUp() {
        this.nitfParserAdapter = new NitfParserServiceImpl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNitfNullInputStream() throws NitfFormatException {
        this.nitfParserAdapter.parseNitf((InputStream) null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNitfNullFile() throws FileNotFoundException, NitfFormatException {
        this.nitfParserAdapter.parseNitf((File) null, null);
    }

    @Test
    public void testParseNitfHeadersOnly() throws NitfFormatException {
        InputStream is = getInputStream(GEO_NITF);
        NitfSegmentsFlow nitfSegmentsFlow = this.nitfParserAdapter.parseNitf(is, false);
        MatcherAssert.assertThat(nitfSegmentsFlow, Is.is(CoreMatchers.notNullValue()));
    }

    @Test
    public void testParseNitfAllData() throws NitfFormatException {
        InputStream is = getInputStream(GEO_NITF);
        NitfSegmentsFlow nitfSegmentsFlow = this.nitfParserAdapter.parseNitf(is, true);
        MatcherAssert.assertThat(nitfSegmentsFlow, Is.is(CoreMatchers.notNullValue()));
    }

    private InputStream getInputStream(String filename) {
        Assert.assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }
}
