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
package org.codice.alliance.transformer.nitf.image;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.MetacardFactory;
import org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderTransformer;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

public class ImageInputTransformerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageInputTransformerTest.class);

    private static final String GEO_NITF = "/i_3001a.ntf";

    private NitfImageTransformer transformer = null;

    private NitfHeaderTransformer headerTransformer = null;

    private MetacardFactory metacardFactory = null;

    private static final String IMAGE_METACARD = "isr.image";

    List<MetacardType> metacardTypeList = new ArrayList<>();

    @Before
    public void createTransformer()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException {
        transformer = new NitfImageTransformer();

        this.metacardFactory = new MetacardFactory();
        metacardFactory.setMetacardType(new MetacardTypeImpl(
                IMAGE_METACARD, metacardTypeList));

        headerTransformer = new NitfHeaderTransformer();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNitfSegmentsFlow() throws Exception {
        transformer.transform(null, new MetacardImpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMetacard() throws Exception {
        NitfSegmentsFlow nitfSegmentsFlow = mock(NitfSegmentsFlow.class);
        transformer.transform(nitfSegmentsFlow, null);
    }

    @Test
    public void testNitfParsing() throws Exception {
        NitfSegmentsFlow nitfSegmentsFlow = new NitfParserInputFlow().inputStream(getInputStream(
                GEO_NITF))
                .allData();

        Metacard metacard = metacardFactory.createMetacard("101");
        nitfSegmentsFlow = headerTransformer.transform(nitfSegmentsFlow, metacard);
        metacard = transformer.transform(nitfSegmentsFlow, metacard);

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "1997-12-17 10:26:30");
        validateDate(metacard, metacard.getEffectiveDate(), "1997-12-17 10:26:30");
        validateDate(metacard, metacard.getModifiedDate(), "1997-12-17 10:26:30");
        assertThat(metacard.getMetacardType()
                .getName(), is("isr.image"));
        assertThat(
                "Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield",
                is(metacard.getTitle()));
        String wkt = metacard.getLocation();
        assertTrue(wkt.matches(
                "^POLYGON \\(\\(85 32.98\\d*, 85.00\\d* 32.98\\d*, 85.00\\d* 32.98\\d*, 85 32.98\\d*, 85 32.98\\d*\\)\\)"));

        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_PROFILE_NAME.toString())
                .getValue(), is("NITF_TWO_ONE"));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_VERSION.toString())
                .getValue(), is("NITF_TWO_ONE"));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.ORIGINATING_STATION_ID.toString())
                .getValue(), is("i_3001a"));

        Date fileDateAndTime = (Date) metacard.getAttribute(Core.CREATED)
                .getValue();
        String fileDateAndTimeString =
                DateTimeFormatter.ISO_INSTANT.format(fileDateAndTime.toInstant());
        assertThat(fileDateAndTimeString, is("1997-12-17T10:26:30Z"));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_TITLE.toString())
                        .getValue(),
                is("Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield"));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_SECURITY_CLASSIFICATION.toString())
                .getValue(), is("UNCLASSIFIED"));
        assertThat(metacard.getAttribute(
                NitfHeaderAttribute.FILE_CLASSIFICATION_SECURITY_SYSTEM.toString()),
                is(nullValue()));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_CODE_WORDS.toString()),
                is(nullValue()));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_CONTROL_AND_HANDLING.toString()),
                is(nullValue()));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.FILE_RELEASING_INSTRUCTIONS.toString()),
                is(nullValue()));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.ORIGINATORS_NAME.toString())
                .getValue(), is("JITC Fort Huachuca, AZ"));
        assertThat(metacard.getAttribute(NitfHeaderAttribute.ORIGINATORS_PHONE_NUMBER.toString())
                .getValue(), is("(520) 538-5458"));

        Date imageDateTime = (Date) metacard.getAttribute(
                ImageAttribute.IMAGE_DATE_AND_TIME.toString())
                .getValue();
        String imageDateTimeString =
                DateTimeFormatter.ISO_INSTANT.format(imageDateTime.toInstant());
        assertThat(imageDateTimeString, is("1996-12-17T10:26:30Z"));
        assertThat(metacard.getAttribute(ImageAttribute.TARGET_IDENTIFIER.toString()),
                is(nullValue()));
        assertThat(metacard.getAttribute(ImageAttribute.IMAGE_IDENTIFIER_2.toString())
                .getValue(), is("- BASE IMAGE -"));
        assertThat(metacard.getAttribute(ImageAttribute.IMAGE_SOURCE.toString())
                .getValue(), is("Unknown"));
        assertThat(metacard.getAttribute(
                ImageAttribute.NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE.toString())
                .getValue(), is(1024L));
        assertThat(metacard.getAttribute(
                ImageAttribute.NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE.toString())
                .getValue(), is(1024L));
        assertThat(metacard.getAttribute(ImageAttribute.IMAGE_REPRESENTATION.toString())
                .getValue(), is("MONOCHROME"));
        assertThat(metacard.getAttribute(ImageAttribute.IMAGE_CATEGORY.toString())
                .getValue(), is("VISUAL"));
        assertThat(metacard.getAttribute(ImageAttribute.IMAGE_COMPRESSION.toString())
                .getValue(), is("NOTCOMPRESSED"));

    }

    @Test
    public void testHandleMissionIdSuccessful() throws Exception {
        Metacard metacard = metacardFactory.createMetacard("missionIdTest");
        transformer.handleMissionIdentifier(metacard, "0123456789ABC");
        assertThat(metacard.getAttribute(Isr.MISSION_ID.toString()).getValue(),
                is("789A"));
    }

    @Test
    public void testHandleMissionIdEmpty() throws Exception {
        Metacard metacard = metacardFactory.createMetacard("noMissionIdTest");
        transformer.handleMissionIdentifier(metacard, "0123456");
        assertThat(metacard.getAttribute(Isr.MISSION_ID.toString()),
                is(nullValue()));
    }

    @Test
    public void testHandleMissionIdNoImageIdentifier() throws Exception {
        Metacard metacard = metacardFactory.createMetacard("noIdentifierTest");
        transformer.handleMissionIdentifier(metacard, null);
        assertThat(metacard.getAttribute(Isr.MISSION_ID.toString()),
                is(nullValue()));
    }

    @Test
    public void testHandleCommentsSuccessful() throws Exception {
        final String blockComment =
                "The credit belongs to the man who is actually in the arena, whose face is marred" +
                " by dust and sweat and blood; who strives valiantly; who errs, who comes short a" +
                "gain and again, because there is no effort without error and shortcoming; but wh" +
                "o does actually strive to do the deeds.";

        List<String> commentsList = Arrays.asList(
                "The credit belongs to the man who is actually in the arena, whose face is marred",
                " by dust and sweat and blood; who strives valiantly; who errs, who comes short a",
                "gain and again, because there is no effort without error and shortcoming; but wh",
                "o does actually strive to do the deeds.");
        Metacard metacard = metacardFactory.createMetacard("commentsTest");
        transformer.handleComments(metacard, commentsList);
        assertThat(metacard.getAttribute(Isr.COMMENTS.toString()).getValue(),
                is(blockComment));
    }

    @Test
    public void testHandleCommentsEmpty() throws Exception {
        List<String> commentsList = new ArrayList<>();
        Metacard metacard = metacardFactory.createMetacard("commentsTest");
        transformer.handleComments(metacard, commentsList);
        assertThat(metacard.getAttribute(Isr.COMMENTS.toString()),
                is(nullValue()));
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