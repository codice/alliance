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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.federation.FederationException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.transform.CatalogTransformerException;

public class TestNitfInputTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestNitfInputTransformer.class);

    private static final String BE_NUM_NITF = "/WithBE.ntf";

    private static final String TRE_NITF = "/i_3128b.ntf";

    private static final String GEO_NITF = "/i_3001a.ntf";

    private NitfInputTransformer transformer = null;

    @Before
    public void createTransformer()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException {
        transformer = new NitfInputTransformer();
        transformer.setNitfMetacardType(new NitfMetacardType());
    }

    @Test(expected = CatalogTransformerException.class)
    public void testNullInput()
            throws Exception {
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
            throws Exception {
        Metacard metacard = transformer.transform(getInputStream(BE_NUM_NITF));

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "2014-08-17 07:22:41");
    }

    @Test
    public void testTreParsing()
            throws Exception {
        Metacard metacard = transformer.transform(getInputStream(TRE_NITF));

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "1999-02-10 14:01:44");
    }

    @Test
    public void testNitfParsing()
            throws Exception {
        Metacard metacard = transformer.transform(getInputStream(GEO_NITF));

        assertNotNull(metacard);

        validateDate(metacard, metacard.getCreatedDate(), "1997-12-17 10:26:30");
        validateDate(metacard, metacard.getEffectiveDate(), "1997-12-17 10:26:30");
        validateDate(metacard, metacard.getModifiedDate(), "1997-12-17 10:26:30");
        assertThat(metacard.getMetacardType().getName(), is("nitf"));
        assertThat(
                "Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield",
                is(metacard.getTitle()));
        String wkt = metacard.getLocation();
        assertTrue(wkt.matches(
                "^POLYGON \\(\\(85 32.98\\d*, 85.00\\d* 32.98\\d*, 85.00\\d* 32.98\\d*, 85 32.98\\d*, 85 32.98\\d*\\)\\)"));

        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_PROFILE_NAME).getValue(), is("NITF_TWO_ONE"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_VERSION).getValue(), is("NITF_TWO_ONE"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.COMPLEXITY_LEVEL).getValue(), is(3));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.STANDARD_TYPE).getValue(), is("BF01"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.ORIGINATING_STATION_ID).getValue(), is("i_3001a"));

        Date fileDateAndTime = (Date) metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_DATE_AND_TIME).getValue();
        String fileDateAndTimeString = DateTimeFormatter.ISO_INSTANT.format(fileDateAndTime.toInstant());
        assertThat(fileDateAndTimeString, is("1997-12-17T10:26:30Z"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_TITLE).getValue(), is("Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_SECURITY_CLASSIFICATION).getValue(), is("UNCLASSIFIED"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CLASSIFICATION_SECURITY_SYSTEM), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CODE_WORDS), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CONTROL_AND_HANDLING), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_RELEASING_INSTRUCTIONS), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_DECLASSIFICATION_TYPE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_DECLASSIFICATION_DATE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_DECLASSIFICATION_EXEMPTION), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_DOWNGRADE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_DOWNGRADE_DATE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CLASSIFICATION_TEXT), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CLASSIFICATION_AUTHORITY_TYPE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CLASSIFICATION_AUTHORITY), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_CLASSIFICATION_REASON), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_SECURITY_SOURCE_DATE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_SECURITY_CONTROL_NUMBER), is(nullValue()));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_COPY_NUMBER).getValue(), is("00000"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_NUMBER_OF_COPIES).getValue(), is("00000"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.FILE_BACKGROUND_COLOR).getValue(), is("[0xff,0xff,0xff]"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.ORIGINATORS_NAME).getValue(), is("JITC Fort Huachuca, AZ"));
        assertThat(metacard.getAttribute("nitf." + NitfHeaderAttribute.ORIGINATORS_PHONE_NUMBER).getValue(), is("(520) 538-5458"));

        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.FILE_PART_TYPE).getValue(), is("IM"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_IDENTIFIER_1).getValue(), is("Missing ID"));

        Date imageDateTime = (Date) metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DATE_AND_TIME).getValue();
        String imageDateTimeString = DateTimeFormatter.ISO_INSTANT.format(imageDateTime.toInstant());
        assertThat(imageDateTimeString, is("1996-12-17T10:26:30Z"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.TARGET_IDENTIFIER), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_IDENTIFIER_2).getValue(), is("- BASE IMAGE -"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_SECURITY_CLASSIFICATION).getValue(), is("UNCLASSIFIED"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CLASSIFICATION_SECURITY_SYSTEM), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CODEWORDS), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CONTROL_AND_HANDLING), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_RELEASING_INSTRUCTIONS), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DECLASSIFICATION_TYPE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DECLASSIFICATION_DATE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DECLASSIFICATION_EXEMPTION), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DOWNGRADE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DOWNGRADE_DATE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CLASSIFICATION_TEXT), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CLASSIFICATION_AUTHORITY_TYPE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CLASSIFICATION_AUTHORITY), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CLASSIFICATION_REASON), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_SECURITY_SOURCE_DATE), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_SECURITY_CONTROL_NUMBER), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_SOURCE).getValue(), is("Unknown"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE).getValue(), is(1024L));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE).getValue(), is(1024L));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.PIXEL_VALUE_TYPE).getValue(), is("INTEGER"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_REPRESENTATION).getValue(), is("MONOCHROME"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_CATEGORY).getValue(), is("VISUAL"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.ACTUAL_BITS_PER_PIXEL_PER_BAND).getValue(), is(8));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.PIXEL_JUSTIFICATION).getValue(), is("RIGHT"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_COORDINATE_REPRESENTATION).getValue(), is("GEOGRAPHIC"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_IMAGE_COMMENTS).getValue(), is(0));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_COMMENT_1), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_COMMENT_2), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_COMMENT_3), is(nullValue()));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_COMPRESSION).getValue(), is("NOTCOMPRESSED"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_BANDS).getValue(), is(1));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_MODE).getValue(), is("BLOCKINTERLEVE"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_BLOCKS_PER_ROW).getValue(), is(1));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_BLOCKS_PER_COLUMN).getValue(), is(1));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_PIXELS_PER_BLOCK_HORIZONTAL).getValue(), is(1024));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_PIXELS_PER_BLOCK_VERTICAL).getValue(), is(1024));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.NUMBER_OF_BITS_PER_PIXEL).getValue(), is(8));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_DISPLAY_LEVEL).getValue(), is(1));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_ATTACHMENT_LEVEL).getValue(), is(0));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_LOCATION).getValue(), is("0,0"));
        assertThat(metacard.getAttribute("nitf.image." + ImageAttribute.IMAGE_MAGNIFICATION).getValue(), is("1.0"));
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