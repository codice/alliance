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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.codice.alliance.transformer.nitf.MetacardFactory;
import org.codice.alliance.transformer.nitf.TreTestUtility;
import org.codice.alliance.transformer.nitf.common.NitfHeaderTransformer;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.transform.CatalogTransformerException;

public class NitfGmtiTransformerTest {
    private static final String GMTI_TEST_NITF = "src/test/resources/gmti-test.ntf";

    private MetacardFactory metacardFactory;

    private NitfHeaderTransformer nitfHeaderTransformer;

    private NitfGmtiTransformer nitfGmtiTransformer;

    private static final String GMTI_METACARD = "isr.gmti";

    List<MetacardType> metacardTypeList = new ArrayList<>();

    @Before
    public void setUp() {
        this.metacardFactory = new MetacardFactory();
        metacardFactory.setMetacardType(new MetacardTypeImpl(
                GMTI_METACARD, metacardTypeList));
        this.nitfHeaderTransformer = new NitfHeaderTransformer();
        this.nitfGmtiTransformer = new NitfGmtiTransformer();
        this.nitfGmtiTransformer.setGeometryFactory(new GeometryFactory());

        TreTestUtility.createFileIfNecessary(GMTI_TEST_NITF, TreTestUtility::createNitfNoImageTres);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNitfSegmentsFlow() throws Exception {
        nitfGmtiTransformer.transform(null, new MetacardImpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMetacard() throws Exception {
        NitfSegmentsFlow nitfSegmentsFlow = mock(NitfSegmentsFlow.class);
        nitfGmtiTransformer.transform(nitfSegmentsFlow, null);
    }

    @Test
    public void testTre() throws IOException, CatalogTransformerException, NitfFormatException {
        NitfSegmentsFlow nitfSegmentsFlow = new NitfParserInputFlow().inputStream(getInputStream(
                GMTI_TEST_NITF))
                .allData();

        Metacard metacard = metacardFactory.createMetacard("101");
        nitfSegmentsFlow = nitfHeaderTransformer.transform(nitfSegmentsFlow, metacard);
        metacard = nitfGmtiTransformer.transform(nitfSegmentsFlow, metacard);

        assertNotNull(metacard);

        validateDate(metacard.getCreatedDate(), "2016-06-22 23:39:22");
        validateDate(metacard.getEffectiveDate(), "2016-06-22 23:39:22");
        validateDate(metacard.getModifiedDate(), "2016-06-22 23:39:22");
        assertThat(metacard.getMetacardType()
                .getName(), is("isr.gmti"));

        assertThat(metacard.getAttribute(MtirpbAttribute.NUMBER_OF_VALID_TARGETS.getAttributeDescriptor()
                .getName())
                .getValue(), is("001"));
        assertThat(metacard.getAttribute(MtirpbAttribute.AIRCRAFT_LOCATION.getAttributeDescriptor()
                .getName())
                .getValue(), is("POINT (+52.123456 -004.123456)"));
        assertThat(metacard.getAttribute(IndexedMtirpbAttribute.INDEXED_TARGET_LOCATION.getAttributeDescriptor()
                .getName())
                .getValue(), is("MULTIPOINT ((52.1234567 -4.1234567))"));
    }

    private void validateDate(Date date, String expectedDate) {
        assertNotNull(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(date), is(expectedDate));
    }

    private InputStream getInputStream(String filename) throws FileNotFoundException {
        return new FileInputStream(new File(filename));
    }
}
