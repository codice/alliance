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
package org.codice.alliance.transformer.nitf.image;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.MetacardFactory;
import org.codice.alliance.transformer.nitf.NitfParserAdapter;
import org.codice.alliance.transformer.nitf.TreTestUtility;
import org.codice.alliance.transformer.nitf.common.AimidbAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderTransformer;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.header.NitfHeaderFactory;
import org.codice.imaging.nitf.core.image.ImageBand;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.core.image.ImageSegmentFactory;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreEntry;
import org.codice.imaging.nitf.core.tre.TreFactory;
import org.codice.imaging.nitf.core.tre.TreSource;
import org.codice.imaging.nitf.fluent.NitfCreationFlow;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.federation.FederationException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

public class ImageInputTransformerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageInputTransformerTest.class);

    private static final String GEO_NITF = "i_3001a.ntf";

    private static final String IMAGE_METACARD = "isr.image";

    private NitfImageTransformer transformer = null;

    private NitfHeaderTransformer headerTransformer = null;

    private MetacardFactory metacardFactory = null;

    @Before
    public void createTransformer()
            throws UnsupportedQueryException, SourceUnavailableException, FederationException {
        transformer = new NitfImageTransformer();

        metacardFactory = new MetacardFactory();
        metacardFactory.setMetacardType(new ImageMetacardType());

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
        assertThat(metacard.getTitle(),
                is("Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield"));
        String wkt = metacard.getLocation();
        assertTrue(wkt.matches(
                "^POLYGON \\(\\(85 32.98\\d*, 85.00\\d* 32.98\\d*, 85.00\\d* 32.98\\d*, 85 32.98\\d*, 85 32.98\\d*\\)\\)"));

        Map<NitfAttribute, Object> map = initAttributesToBeAsserted();
        assertAttributesMap(metacard, map);

        Map<NitfAttribute, String> dateMap = initDateAttributesToBeAsserted();
        assertDateAttributesMap(metacard, dateMap);
    }

    @Test
    public void testHandleMissionIdSuccessful() throws Exception {
        Metacard metacard = metacardFactory.createMetacard("missionIdTest");
        transformer.handleMissionIdentifier(metacard, "0123456789ABC");
        assertThat(metacard.getAttribute(Isr.MISSION_ID)
                .getValue(), is("789A"));
    }

    @Test
    public void testHandleMissionIdEmpty() throws Exception {
        Metacard metacard = metacardFactory.createMetacard("noMissionIdTest");
        transformer.handleMissionIdentifier(metacard, "0123456");
        assertThat(metacard.getAttribute(Isr.MISSION_ID), nullValue());
    }

    @Test
    public void testHandleMissionIdNoImageIdentifier() throws Exception {
        Metacard metacard = metacardFactory.createMetacard("noIdentifierTest");
        transformer.handleMissionIdentifier(metacard, null);
        assertThat(metacard.getAttribute(Isr.MISSION_ID), nullValue());
    }

    @Test
    public void testHandleCommentsSuccessful() throws Exception {
        final String blockComment =
                "The credit belongs to the man who is actually in the arena, whose face is marred"
                        + " by dust and sweat and blood; who strives valiantly; who errs, who comes short a"
                        + "gain and again, because there is no effort without error and shortcoming; but wh"
                        + "o does actually strive to do the deeds.";

        List<String> commentsList = Arrays.asList(
                "The credit belongs to the man who is actually in the arena, whose face is marred",
                " by dust and sweat and blood; who strives valiantly; who errs, who comes short a",
                "gain and again, because there is no effort without error and shortcoming; but wh",
                "o does actually strive to do the deeds.");
        Metacard metacard = metacardFactory.createMetacard("commentsTest");
        transformer.handleComments(metacard, commentsList);
        assertThat(metacard.getAttribute(Isr.COMMENTS)
                .getValue(), is(blockComment));
    }

    @Test
    public void testHandleCommentsEmpty() throws Exception {
        List<String> commentsList = new ArrayList<>();
        Metacard metacard = metacardFactory.createMetacard("commentsTest");
        transformer.handleComments(metacard, commentsList);
        assertThat(metacard.getAttribute(Isr.COMMENTS), nullValue());
    }

    private String str(int length) {
        return IntStream.range(0, length)
                .mapToObj(x -> "x")
                .collect(Collectors.joining());
    }

    private Map<NitfAttribute, Object> createNitfWithAimidb(File file) {
        String acquisitionDate = "20161013121212";
        String missionNumber = "UNKN";
        String country = "US";
        String location = "4559N23345W";

        Tre aimidb = TreFactory.getDefault("AIMIDB", TreSource.ImageExtendedSubheaderData);
        aimidb.add(new TreEntry("ACQUISITION_DATE", acquisitionDate, "string"));
        aimidb.add(new TreEntry("MISSION_NO", missionNumber, "string"));
        aimidb.add(new TreEntry("MISSION_IDENTIFICATION", "NOT AVAIL.", "string"));
        aimidb.add(new TreEntry("FLIGHT_NO", "01", "string"));
        aimidb.add(new TreEntry("OP_NUM", "001", "UINT"));
        aimidb.add(new TreEntry("CURRENT_SEGMENT", "AA", "string"));
        aimidb.add(new TreEntry("REPRO_NUM", "01", "UINT"));
        aimidb.add(new TreEntry("REPLAY", "000", "string"));
        aimidb.add(new TreEntry("RESERVED_1", " ", "string"));
        aimidb.add(new TreEntry("START_TILE_COLUMN", "001", "UINT"));
        aimidb.add(new TreEntry("START_TILE_ROW", "00001", "UINT"));
        aimidb.add(new TreEntry("END_SEGMENT", "AA", "string"));
        aimidb.add(new TreEntry("END_TILE_COLUMN", "001", "UINT"));
        aimidb.add(new TreEntry("END_TILE_ROW", "00001", "UINT"));
        aimidb.add(new TreEntry("COUNTRY", country, "string"));
        aimidb.add(new TreEntry("RESERVED_2", "    ", "string"));
        aimidb.add(new TreEntry("LOCATION", location, "string"));
        aimidb.add(new TreEntry("RESERVED_3", "             ", "string"));
        ImageSegment imageSegment = TreTestUtility.createImageSegment();
        imageSegment.getTREsRawStructure()
                .add(aimidb);

        new NitfCreationFlow().fileHeader(() -> TreTestUtility.createFileHeader())
                .imageSegment(() -> imageSegment)
                .write(file.getAbsolutePath());

        // key value pair of nitf attributes and expected getAttributes
        Map<NitfAttribute, Object> assertMap = new HashMap<>();
        assertMap.put(AimidbAttribute.ACQUISITION_DATE, acquisitionDate);
        assertMap.put(AimidbAttribute.MISSION_NUMBER, missionNumber);
        assertMap.put(AimidbAttribute.COUNTRY, country);
        assertMap.put(AimidbAttribute.LOCATION, location);
        return assertMap;
    }

    @Test
    public void testAimidb() throws IOException, NitfFormatException {
        File nitfFile = File.createTempFile("nitf-", ".ntf");
        try {
            Map<NitfAttribute, Object> treMap = createNitfWithAimidb(nitfFile);

            try (InputStream inputStream = new FileInputStream(nitfFile)) {
                Metacard metacard = metacardFactory.createMetacard("aimidbTest");
                NitfSegmentsFlow nitfSegmentsFlow = new NitfParserAdapter().parseNitf(inputStream);
                headerTransformer.transform(nitfSegmentsFlow, metacard);
                transformer.transform(nitfSegmentsFlow, metacard);
                assertAttributesMap(metacard, treMap);
            }
        } finally {
            nitfFile.delete();
        }
    }

    private void createNitfWithCsdida(File file) {
        NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
        Tre csdida = TreFactory.getDefault("CSDIDA", TreSource.ExtendedHeaderData);
        csdida.add(new TreEntry("DAY", "01", "UINT"));
        csdida.add(new TreEntry("MONTH", str(3), "UINT"));
        csdida.add(new TreEntry("YEAR", "1234", "UINT"));
        csdida.add(new TreEntry("PLATFORM_CODE", "XY", "string"));
        csdida.add(new TreEntry("VEHICLE_ID", "01", "string"));
        csdida.add(new TreEntry("PASS", "01", "string"));
        csdida.add(new TreEntry("OPERATION", "001", "UINT"));
        csdida.add(new TreEntry("SENSOR_ID", str(2), "string"));
        csdida.add(new TreEntry("PRODUCT_ID", str(2), "string"));
        csdida.add(new TreEntry("RESERVED_0", str(4), "string"));
        csdida.add(new TreEntry("TIME", "20000202010101", "UINT"));
        csdida.add(new TreEntry("PROCESS_TIME", "20000202010101", "UINT"));
        csdida.add(new TreEntry("RESERVED_1", str(2), "string"));
        csdida.add(new TreEntry("RESERVED_2", str(2), "string"));
        csdida.add(new TreEntry("RESERVED_3", str(1), "string"));
        csdida.add(new TreEntry("RESERVED_4", str(1), "string"));
        csdida.add(new TreEntry("SOFTWARE_VERSION_NUMBER", str(10), "string"));

        header.getTREsRawStructure()
                .add(csdida);
        new NitfCreationFlow().fileHeader(() -> header)
                .write(file.getAbsolutePath());

    }

    @Test
    public void testCsdida() throws IOException, NitfFormatException {
        File nitfFile = File.createTempFile("nitf-", ".ntf");
        try {
            createNitfWithCsdida(nitfFile);

            try (InputStream inputStream = new FileInputStream(nitfFile)) {
                Metacard metacard = metacardFactory.createMetacard("csexraTest");
                NitfSegmentsFlow nitfSegmentsFlow = new NitfParserAdapter().parseNitf(inputStream);
                headerTransformer.transform(nitfSegmentsFlow, metacard);
                assertThat(metacard.getAttribute(Isr.PLATFORM_ID)
                        .getValue(), is("XY01"));
            }

        } finally {
            nitfFile.delete();
        }

    }

    private void createNitfWithCsexra(File file) {
        NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
        Tre csexra = TreFactory.getDefault("CSEXRA", TreSource.ImageExtendedSubheaderData);
        csexra.add(new TreEntry("SNOW_DEPTH_CAT", "1", "string"));
        csexra.add(new TreEntry("SENSOR", str(6), "string"));
        csexra.add(new TreEntry("TIME_FIRST_LINE_IMAGE", "12345.000000", "string"));
        csexra.add(new TreEntry("TIME_IMAGE_DURATION", "12345.000000", "string"));
        csexra.add(new TreEntry("MAX_GSD", "123.5", "string"));
        csexra.add(new TreEntry("ALONG_SCAN_GSD", "123.5", "string"));
        csexra.add(new TreEntry("CROSS_SCAN_GSD", "123.5", "string"));
        csexra.add(new TreEntry("GEO_MEAN_GSD", "123.5", "string"));
        csexra.add(new TreEntry("A_S_VERT_GSD", "123.5", "string"));
        csexra.add(new TreEntry("C_S_VERT_GSD", "123.5", "string"));
        csexra.add(new TreEntry("GEO_MEAN_VERT_GSD", "123.5", "string"));
        csexra.add(new TreEntry("GSD_BETA_ANGLE", "123.5", "string"));
        csexra.add(new TreEntry("DYNAMIC_RANGE", "02047", "string"));
        csexra.add(new TreEntry("NUM_LINES", "0000101", "string"));
        csexra.add(new TreEntry("NUM_SAMPLES", "00101", "string"));
        csexra.add(new TreEntry("ANGLE_TO_NORTH", "000.000", "string"));
        csexra.add(new TreEntry("OBLIQUITY_ANGLE", "00.000", "string"));
        csexra.add(new TreEntry("AZ_OF_OBLIQUITY", "000.000", "string"));
        csexra.add(new TreEntry("GRD_COVER", "1", "string"));
        csexra.add(new TreEntry("SNOW_DEPTH_CAT", "1", "string"));
        csexra.add(new TreEntry("SUN_AZIMUTH", "000.000", "string"));
        csexra.add(new TreEntry("SUN_ELEVATION", "-90.000", "string"));
        csexra.add(new TreEntry("PREDICTED_NIIRS", "1.0", "string"));
        csexra.add(new TreEntry("CIRCL_ERR", "000", "string"));
        csexra.add(new TreEntry("LINEAR_ERR", "000", "string"));

        ImageSegment imageSegment = ImageSegmentFactory.getDefault(FileType.NITF_TWO_ONE);
        imageSegment.addImageBand(new ImageBand());

        imageSegment.getTREsRawStructure()
                .add(csexra);
        new NitfCreationFlow().fileHeader(() -> header)
                .imageSegment(() -> imageSegment)
                .write(file.getAbsolutePath());

    }

    private void createNitfWithPiaimc(File file) {
        NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
        Tre piaimc = TreFactory.getDefault("PIAIMC", TreSource.ImageExtendedSubheaderData);
        piaimc.add(new TreEntry("CLOUDCVR", "070", "string"));
        piaimc.add(new TreEntry("SRP", "Y", "string"));
        piaimc.add(new TreEntry("SENSMODE", str(12), "string"));
        piaimc.add(new TreEntry("SENSNAME", str(18), "string"));
        piaimc.add(new TreEntry("SOURCE", str(255), "string"));
        piaimc.add(new TreEntry("COMGEN", "09", "string"));
        piaimc.add(new TreEntry("SUBQUAL", str(1), "string"));
        piaimc.add(new TreEntry("PIAMSNNUM", str(7), "string"));
        piaimc.add(new TreEntry("CAMSPECS", str(32), "string"));
        piaimc.add(new TreEntry("PROJID", str(2), "string"));
        piaimc.add(new TreEntry("GENERATION", "8", "string"));
        piaimc.add(new TreEntry("ESD", "Y", "string"));
        piaimc.add(new TreEntry("OTHERCOND", str(2), "string"));
        piaimc.add(new TreEntry("MEANGSD", "00000.0", "string"));
        piaimc.add(new TreEntry("IDATUM", str(3), "string"));
        piaimc.add(new TreEntry("IELLIP", str(3), "string"));
        piaimc.add(new TreEntry("PREPROC", str(2), "string"));
        piaimc.add(new TreEntry("IPROJ", str(2), "string"));
        piaimc.add(new TreEntry("SATTRACK_PATH", "0000", "string"));
        piaimc.add(new TreEntry("SATTRACK_ROW", "0000", "string"));

        ImageSegment imageSegment = ImageSegmentFactory.getDefault(FileType.NITF_TWO_ONE);
        imageSegment.addImageBand(new ImageBand());

        imageSegment.getTREsRawStructure()
                .add(piaimc);
        new NitfCreationFlow().fileHeader(() -> header)
                .imageSegment(() -> imageSegment)
                .write(file.getAbsolutePath());

    }

    @Test
    public void testPiaimc() throws IOException, NitfFormatException {
        File nitfFile = File.createTempFile("nitf-", ".ntf");
        try {
            createNitfWithPiaimc(nitfFile);

            try (InputStream inputStream = new FileInputStream(nitfFile)) {
                Metacard metacard = metacardFactory.createMetacard("csexraTest");
                NitfSegmentsFlow nitfSegmentsFlow = new NitfParserAdapter().parseNitf(inputStream);
                transformer.transform(nitfSegmentsFlow, metacard);
                assertThat(metacard.getAttribute(Isr.CLOUD_COVER)
                        .getValue(), is(70));
            }

        } finally {
            nitfFile.delete();
        }

    }

    @Test
    public void testCsexraSnowDepthMin() throws IOException, NitfFormatException {
        testCsexra(metacard -> assertThat(((Float) metacard.getAttribute(Isr.SNOW_DEPTH_MIN_CENTIMETERS)
                .getValue()).doubleValue(), is(closeTo(2.54, 0.01))));
    }

    @Test
    public void testCsexraSnowDepthMax() throws IOException, NitfFormatException {
        testCsexra(metacard -> assertThat(((Float) metacard.getAttribute(Isr.SNOW_DEPTH_MAX_CENTIMETERS)
                .getValue()).doubleValue(), is(closeTo(22.86, 0.01))));
    }

    @Test
    public void testCsexraSnowCover() throws IOException, NitfFormatException {
        testCsexra(metacard -> assertThat(metacard.getAttribute(Isr.SNOW_COVER)
                .getValue(), is(Boolean.TRUE)));
    }

    @Test
    public void testCsexraNIIRS() throws IOException, NitfFormatException {
        testCsexra(metacard -> assertThat(metacard.getAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE)
                .getValue(), is(1)));
    }

    private void testCsexra(Consumer<Metacard> consumer) throws IOException, NitfFormatException {
        File nitfFile = File.createTempFile("nitf-", ".ntf");
        try {
            createNitfWithCsexra(nitfFile);

            try (InputStream inputStream = new FileInputStream(nitfFile)) {
                Metacard metacard = metacardFactory.createMetacard("csexraTest");
                NitfSegmentsFlow nitfSegmentsFlow = new NitfParserAdapter().parseNitf(inputStream);
                transformer.transform(nitfSegmentsFlow, metacard);
                consumer.accept(metacard);
            }

        } finally {
            nitfFile.delete();
        }
    }

    private void validateDate(Metacard metacard, Date date, String expectedDate) {
        assertNotNull(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(formatter.format(date), is(expectedDate));
        LOGGER.info("metacard = {}", metacard.getMetadata());
    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing",
                getClass().getClassLoader()
                        .getResource(filename));
        return getClass().getClassLoader()
                .getResourceAsStream(filename);
    }

    private void assertDateAttributesMap(Metacard metacard, Map<NitfAttribute, String> map) {
        for (Map.Entry<NitfAttribute, String> entry : map.entrySet()) {
            for (AttributeDescriptor attributeDescriptor : (Set<AttributeDescriptor>) entry.getKey()
                    .getAttributeDescriptors()) {
                Attribute attribute = metacard.getAttribute(attributeDescriptor.getName());
                Date fileDateAndTime = (Date) attribute.getValue();
                String fileDateAndTimeString =
                        DateTimeFormatter.ISO_INSTANT.format(fileDateAndTime.toInstant());

                assertThat(fileDateAndTimeString, is(entry.getValue()));
            }
        }
    }

    private void assertAttributesMap(Metacard metacard, Map<NitfAttribute, Object> map) {
        for (Map.Entry<NitfAttribute, Object> entry : map.entrySet()) {
            for (AttributeDescriptor attributeDescriptor : (Set<AttributeDescriptor>) entry.getKey()
                    .getAttributeDescriptors()) {
                Attribute attribute = metacard.getAttribute(attributeDescriptor.getName());
                if (attribute != null) {
                    assertThat(attribute.getValue(), is(entry.getValue()));
                } else {
                    assertThat(attribute, nullValue());
                }
            }
        }
    }

    private Map<NitfAttribute, Object> initAttributesToBeAsserted() {
        //key value pair of attributes and expected getAttributes
        Map<NitfAttribute, Object> map = new HashMap<>();
        map.put(NitfHeaderAttribute.FILE_PROFILE_NAME, "NITF_TWO_ONE");
        map.put(NitfHeaderAttribute.FILE_VERSION, "NITF_TWO_ONE");
        map.put(NitfHeaderAttribute.COMPLEXITY_LEVEL, 3);
        map.put(NitfHeaderAttribute.STANDARD_TYPE, "BF01");
        map.put(NitfHeaderAttribute.ORIGINATING_STATION_ID, "i_3001a");
        map.put(NitfHeaderAttribute.FILE_TITLE,
                "Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield");
        map.put(NitfHeaderAttribute.FILE_SECURITY_CLASSIFICATION, "UNCLASSIFIED");
        map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_SECURITY_SYSTEM, null);
        map.put(NitfHeaderAttribute.FILE_CODE_WORDS, null);
        map.put(NitfHeaderAttribute.FILE_CONTROL_AND_HANDLING, null);
        map.put(NitfHeaderAttribute.FILE_RELEASING_INSTRUCTIONS, null);
        map.put(NitfHeaderAttribute.FILE_DECLASSIFICATION_TYPE, null);
        map.put(NitfHeaderAttribute.FILE_DECLASSIFICATION_DATE, null);
        map.put(NitfHeaderAttribute.FILE_DECLASSIFICATION_EXEMPTION, null);
        map.put(NitfHeaderAttribute.FILE_DOWNGRADE, null);
        map.put(NitfHeaderAttribute.FILE_RELEASING_INSTRUCTIONS, null);
        map.put(NitfHeaderAttribute.FILE_DOWNGRADE_DATE, null);
        map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_TEXT, null);
        map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_AUTHORITY_TYPE, null);
        map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_AUTHORITY, null);
        map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_REASON, null);
        map.put(NitfHeaderAttribute.FILE_SECURITY_SOURCE_DATE, null);
        map.put(NitfHeaderAttribute.FILE_SECURITY_CONTROL_NUMBER, null);
        map.put(NitfHeaderAttribute.FILE_COPY_NUMBER, "00000");
        map.put(NitfHeaderAttribute.FILE_NUMBER_OF_COPIES, "00000");
        map.put(NitfHeaderAttribute.FILE_BACKGROUND_COLOR, "[0xff,0xff,0xff]");
        map.put(NitfHeaderAttribute.ORIGINATORS_NAME, "JITC Fort Huachuca, AZ");
        map.put(NitfHeaderAttribute.ORIGINATORS_PHONE_NUMBER, "(520) 538-5458");
        map.put(ImageAttribute.FILE_PART_TYPE, "IM");
        map.put(ImageAttribute.IMAGE_IDENTIFIER_1, "Missing ID");
        map.put(ImageAttribute.TARGET_IDENTIFIER, null);
        map.put(ImageAttribute.IMAGE_IDENTIFIER_2, "- BASE IMAGE -");
        map.put(ImageAttribute.IMAGE_SECURITY_CLASSIFICATION, "UNCLASSIFIED");
        map.put(ImageAttribute.IMAGE_CLASSIFICATION_SECURITY_SYSTEM, null);
        map.put(ImageAttribute.IMAGE_CODEWORDS, null);
        map.put(ImageAttribute.IMAGE_CONTROL_AND_HANDLING, null);
        map.put(ImageAttribute.IMAGE_RELEASING_INSTRUCTIONS, null);
        map.put(ImageAttribute.IMAGE_DECLASSIFICATION_TYPE, null);
        map.put(ImageAttribute.IMAGE_DECLASSIFICATION_DATE, null);
        map.put(ImageAttribute.IMAGE_DECLASSIFICATION_EXEMPTION, null);
        map.put(ImageAttribute.IMAGE_DOWNGRADE, null);
        map.put(ImageAttribute.IMAGE_DOWNGRADE_DATE, null);
        map.put(ImageAttribute.IMAGE_CLASSIFICATION_TEXT, null);
        map.put(ImageAttribute.IMAGE_CLASSIFICATION_AUTHORITY_TYPE, null);
        map.put(ImageAttribute.IMAGE_CLASSIFICATION_AUTHORITY, null);
        map.put(ImageAttribute.IMAGE_CLASSIFICATION_REASON, null);
        map.put(ImageAttribute.IMAGE_SECURITY_SOURCE_DATE, null);
        map.put(ImageAttribute.IMAGE_SECURITY_CONTROL_NUMBER, null);
        map.put(ImageAttribute.IMAGE_SOURCE, "Unknown");
        map.put(ImageAttribute.NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE, 1024L);
        map.put(ImageAttribute.NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE, 1024L);
        map.put(ImageAttribute.PIXEL_VALUE_TYPE, "INTEGER");
        map.put(ImageAttribute.IMAGE_REPRESENTATION, "MONOCHROME");
        map.put(ImageAttribute.IMAGE_CATEGORY, "VISUAL");
        map.put(ImageAttribute.ACTUAL_BITS_PER_PIXEL_PER_BAND, 8);
        map.put(ImageAttribute.PIXEL_JUSTIFICATION, "RIGHT");
        map.put(ImageAttribute.IMAGE_COORDINATE_REPRESENTATION, "GEOGRAPHIC");
        map.put(ImageAttribute.NUMBER_OF_IMAGE_COMMENTS, 0);
        map.put(ImageAttribute.IMAGE_COMMENT_1, null);
        map.put(ImageAttribute.IMAGE_COMPRESSION, "NOTCOMPRESSED");
        return map;
    }

    private Map<NitfAttribute, String> initDateAttributesToBeAsserted() {
        //key value pair of attributes and expected getAttributes
        Map<NitfAttribute, String> map = new HashMap<>();
        map.put(NitfHeaderAttribute.FILE_DATE_AND_TIME, "1997-12-17T10:26:30Z");
        map.put(ImageAttribute.IMAGE_DATE_AND_TIME, "1996-12-17T10:26:30Z");
        return map;
    }
}