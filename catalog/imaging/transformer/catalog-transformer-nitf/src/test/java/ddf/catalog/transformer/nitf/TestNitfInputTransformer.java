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
package ddf.catalog.transformer.nitf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.dynamic.api.MetacardFactory;
import ddf.catalog.data.dynamic.impl.MetacardFactoryImpl;
import ddf.catalog.data.dynamic.registry.MetacardTypeReader;
import ddf.catalog.federation.FederationException;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.transform.CatalogTransformerException;

public class TestNitfInputTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestNitfInputTransformer.class);

    private static final String BE_NUM_NITF = "/WithBE.ntf";

    private static final String TRE_NITF = "/i_3128b.ntf";

    private static final String GEO_NITF = "/i_3001a.ntf";

    private static MetacardFactory metacardFactory = null;

    private NitfInputTransformer transformer = null;

    @BeforeClass
    public static void setupBeforeClass() throws Exception {
        metacardFactory = new MetacardFactoryImpl();
        MetacardTypeReader reader = new MetacardTypeReader();
        reader.setMetacardFactory(metacardFactory);
        InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("nitf.xml");
        reader.parseMetacardDefinition(is);
        System.setProperty("java.awt.headless", "true");
        Toolkit tk = Toolkit.getDefaultToolkit();
    }

    @Before
    public void createTransformer()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException {
        transformer = new NitfInputTransformer();
        CatalogFramework catalog = mock(CatalogFramework.class);
        when(catalog.query(Matchers.any(QueryRequest.class))).thenReturn(new QueryResponseImpl(null,
                "sourceId"));
        transformer.setMetacardFactory(metacardFactory);
    }

    @Test(expected = CatalogTransformerException.class)
    public void testNullInput()
            throws IOException, CatalogTransformerException, UnsupportedQueryException,
            SourceUnavailableException, FederationException {
        transformer.transform(null);
    }

    @Test(expected = CatalogTransformerException.class)
    public void testBadInput()
            throws IOException, CatalogTransformerException, UnsupportedQueryException,
            SourceUnavailableException, FederationException {
        transformer.transform(new ByteArrayInputStream("{key=".getBytes()));
    }

    @Test
    public void testSorcerWithBE()
            throws IOException, CatalogTransformerException, UnsupportedQueryException,
            SourceUnavailableException, FederationException, ParseException {
        Metacard metacard = transformer.transform(getInputStream(BE_NUM_NITF));

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "2014-08-17 07:22:41");
    }

    @Test
    public void testTreParsing()
            throws IOException, CatalogTransformerException, UnsupportedQueryException,
            SourceUnavailableException, FederationException, ParseException {
        Metacard metacard = transformer.transform(getInputStream(TRE_NITF));

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "1999-02-10 14:01:44");
    }

    @Test
    public void testNitfParsing()
            throws IOException, CatalogTransformerException, UnsupportedQueryException,
            SourceUnavailableException, FederationException, ParseException {
        Metacard metacard = transformer.transform(getInputStream(GEO_NITF));

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "1997-12-17 10:26:30");
        validateDate(metacard, metacard.getEffectiveDate(), "1997-12-17 10:26:30");
        validateDate(metacard, metacard.getModifiedDate(), "1997-12-17 10:26:30");
        //validateDate(metacard, metacard.getExpirationDate(), "1997-12-17 10:26:30");
        assertThat(metacard.getMetacardType().getName(), is(Nitf.NAME));
        //assertThat("", is(metacard.getResourceSize()));
        //assertThat("", is(metacard.getResourceURI()));
        //assertThat("", is(metacard.getSourceId()));
        assertThat(
                "Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield",
                is(metacard.getTitle()));
        String wkt = metacard.getLocation();
        assertTrue(wkt.matches(
                "^POLYGON \\(\\(85 32.98\\d*, 85.00\\d* 32.98\\d*, 85.00\\d* 32.98\\d*, 85 32.98\\d*, 85 32.98\\d*\\)\\)"));

        assertThat(metacard.getAttribute(Nitf.BITS_PER_PIXEL_PER_BAND).getValue(), is(8));
        assertThat(metacard.getAttribute(Nitf.IMAGE_ID)
                .getValue(), is("- BASE IMAGE -"));
        assertThat(metacard.getAttribute(Nitf.NUMBER_OF_COLUMNS).getValue(), is(1024L));
        assertThat(metacard.getAttribute(Nitf.NUMBER_OF_ROWS).getValue(), is(1024L));
        assertThat(metacard.getAttribute(Nitf.COMPRESSION).getValue(), is("NOTCOMPRESSED"));
        assertThat(metacard.getAttribute(Nitf.IMAGE_MODE).getValue(), is("BLOCKINTERLEVE"));
        assertThat(metacard.getAttribute(Nitf.REPRESENTATION).getValue(), is("MONOCHROME"));
        assertThat(metacard.getAttribute(Nitf.SUBCATEGORY).getValue(), is("VISUAL"));
        assertThat(metacard.getAttribute(Nitf.BITS_PER_PIXEL_PER_BAND).getValue(), is(8));
        assertThat(metacard.getAttribute(Nitf.NUMBER_OF_BANDS).getValue(), is(1));
        assertThat(metacard.getAttribute(Nitf.ORIGINATING_STATION_ID).getValue(), is("i_3001a"));
        assertThat(metacard.getAttribute(Nitf.COMPLEXITY_LEVEL).getValue(), is(3));
    }

    private void validateDate(Metacard metacard, Date date, String expectedDate) {
        assertNotNull(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(date), is(expectedDate));
        LOGGER.info("metacard = {}", metacard.getMetadata());
    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }
}