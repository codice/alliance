/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.transformer.nitf.image;

import static ddf.catalog.data.types.DateTime.END;
import static ddf.catalog.data.types.DateTime.START;
import static org.codice.alliance.transformer.nitf.TreUtilityTest.createImageSegment;
import static org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute.FILE_DATE_AND_TIME_CREATED_ATTRIBUTE;
import static org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute.FILE_DATE_AND_TIME_EFFECTIVE_ATTRIBUTE;
import static org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute.FILE_DATE_AND_TIME_MODIFIED_ATTRIBUTE;
import static org.codice.alliance.transformer.nitf.image.ImageAttribute.IMAGE_DATE_AND_TIME_ATTRIBUTE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.MetacardFactory;
import org.codice.alliance.transformer.nitf.NitfAttributeConverters;
import org.codice.alliance.transformer.nitf.NitfTestCommons;
import org.codice.alliance.transformer.nitf.TreUtilityTest;
import org.codice.alliance.transformer.nitf.common.AimidbAttribute;
import org.codice.alliance.transformer.nitf.common.ExpltbAttribute;
import org.codice.alliance.transformer.nitf.common.IndexedPiaprdAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderTransformer;
import org.codice.alliance.transformer.nitf.common.PiaprdAttribute;
import org.codice.alliance.transformer.nitf.common.PiatgbAttribute;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.header.impl.NitfHeaderFactory;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.core.image.impl.ImageSegmentFactory;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreGroup;
import org.codice.imaging.nitf.core.tre.TreSource;
import org.codice.imaging.nitf.core.tre.impl.TreEntryImpl;
import org.codice.imaging.nitf.core.tre.impl.TreFactory;
import org.codice.imaging.nitf.fluent.NitfCreationFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.codice.imaging.nitf.fluent.impl.NitfCreationFlowImpl;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.junit.Before;
import org.junit.Test;

public class ImageInputTransformerTest {

  private static final String GEO_NITF = "i_3001a.ntf";

  private static final String TEST_CLASSIFICATION_SYSTEM = "US";

  private NitfImageTransformer transformer = null;

  private NitfHeaderTransformer headerTransformer = null;

  private MetacardFactory metacardFactory = null;

  @Before
  public void createTransformer() {
    transformer = new NitfImageTransformer();

    metacardFactory = new MetacardFactory();
    metacardFactory.setMetacardType(new ImageMetacardType());

    headerTransformer = new NitfHeaderTransformer();

    NitfTestCommons.setupNitfUtilities(
        TEST_CLASSIFICATION_SYSTEM, Collections.singletonList("USA"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNitfSegmentsFlow() {
    transformer.transform(null, new MetacardImpl());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMetacard() {
    NitfSegmentsFlow nitfSegmentsFlow = mock(NitfSegmentsFlow.class);
    transformer.transform(nitfSegmentsFlow, null);
  }

  @Test
  public void testNitfParsing() throws Exception {
    NitfSegmentsFlow nitfSegmentsFlow =
        new NitfParserInputFlowImpl().inputStream(getInputStream(GEO_NITF)).allData();

    Metacard metacard = metacardFactory.createMetacard("101");
    nitfSegmentsFlow = headerTransformer.transform(nitfSegmentsFlow, metacard);
    metacard = transformer.transform(nitfSegmentsFlow, metacard);

    assertNotNull(metacard);

    assertThat(metacard.getMetacardType().getName(), is("isr.image"));
    assertThat(
        metacard.getTitle(),
        is("Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield"));
    String wkt = metacard.getLocation();
    assertTrue(
        wkt.matches(
            "^POLYGON \\(\\(85 32.98\\d*, 85.00\\d* 32.98\\d*, 85.00\\d* 32.98\\d*, 85 32.98\\d*, 85 32.98\\d*\\)\\)"));

    Map<NitfAttribute, NitfValue> map = initAttributesToBeAsserted();
    assertAttributesMap(metacard, map);

    validateDates(
        metacard,
        NitfTestCommons.createNitfDateTime(1997, 12, 17, 10, 26, 30),
        NitfTestCommons.createNitfDateTime(1996, 12, 17, 10, 26, 30));
  }

  @Test
  public void testHandleCommentsSuccessful() {
    final String blockComment =
        "The credit belongs to the man who is actually in the arena, whose face is marred"
            + " by dust and sweat and blood; who strives valiantly; who errs, who comes short"
            + " again and again, because there is no effort without error and shortcoming; but"
            + " who does actually strive to do the deeds. ";

    List<String> commentsList =
        Arrays.asList(
            "The credit belongs to the man who is actually in the arena, whose face is marred",
            "by dust and sweat and blood; who strives valiantly; who errs, who comes short",
            "again and again, because there is no effort without error and shortcoming; but",
            "who does actually strive to do the deeds.");
    Metacard metacard = metacardFactory.createMetacard("commentsTest");
    transformer.handleComments(metacard, commentsList);
    assertThat(metacard.getAttribute(Isr.COMMENTS).getValue(), is(blockComment));
  }

  @Test
  public void testHandleCommentsEmpty() {
    List<String> commentsList = new ArrayList<>();
    Metacard metacard = metacardFactory.createMetacard("commentsTest");
    transformer.handleComments(metacard, commentsList);
    assertThat(metacard.getAttribute(Isr.COMMENTS), nullValue());
  }

  private static String str(int length) {
    return IntStream.range(0, length).mapToObj(x -> "x").collect(Collectors.joining());
  }

  private static Map<NitfAttribute, NitfValue> createNitfWithAimidb(File file) {
    String acquisitionDate = "20161013121212";
    String missionNumber = "UNKN";
    String country = "US";
    String location = "4559N23345W";

    Tre aimidb = TreFactory.getDefault("AIMIDB", TreSource.ImageExtendedSubheaderData);
    aimidb.add(new TreEntryImpl("ACQUISITION_DATE", acquisitionDate, "string"));
    aimidb.add(new TreEntryImpl("MISSION_NO", missionNumber, "string"));
    aimidb.add(new TreEntryImpl("MISSION_IDENTIFICATION", "NOT AVAIL.", "string"));
    aimidb.add(new TreEntryImpl("FLIGHT_NO", "01", "string"));
    aimidb.add(new TreEntryImpl("OP_NUM", "001", "UINT"));
    aimidb.add(new TreEntryImpl("CURRENT_SEGMENT", "AA", "string"));
    aimidb.add(new TreEntryImpl("REPRO_NUM", "01", "UINT"));
    aimidb.add(new TreEntryImpl("REPLAY", "000", "string"));
    aimidb.add(new TreEntryImpl("RESERVED_1", " ", "string"));
    aimidb.add(new TreEntryImpl("START_TILE_COLUMN", "001", "UINT"));
    aimidb.add(new TreEntryImpl("START_TILE_ROW", "00001", "UINT"));
    aimidb.add(new TreEntryImpl("END_SEGMENT", "AA", "string"));
    aimidb.add(new TreEntryImpl("END_TILE_COLUMN", "001", "UINT"));
    aimidb.add(new TreEntryImpl("END_TILE_ROW", "00001", "UINT"));
    aimidb.add(new TreEntryImpl("COUNTRY", country, "string"));
    aimidb.add(new TreEntryImpl("RESERVED_2", "    ", "string"));
    aimidb.add(new TreEntryImpl("LOCATION", location, "string"));
    aimidb.add(new TreEntryImpl("RESERVED_3", "             ", "string"));
    ImageSegment imageSegment = TreUtilityTest.createImageSegment();
    imageSegment.getTREsRawStructure().add(aimidb);

    new NitfCreationFlowImpl()
        .fileHeader(TreUtilityTest::createFileHeader)
        .imageSegment(() -> imageSegment)
        .write(file.getAbsolutePath());

    // key value pair of nitf attributes and expected getAttributes
    Map<NitfAttribute, NitfValue> assertMap = new HashMap<>();
    assertMap.put(AimidbAttribute.ACQUISITION_DATE_ATTRIBUTE, new NitfValue(acquisitionDate));
    assertMap.put(AimidbAttribute.MISSION_NUMBER_ATTRIBUTE, new NitfValue(missionNumber));
    assertMap.put(AimidbAttribute.COUNTRY_CODE_ATTRIBUTE, new NitfValue(country));
    assertMap.put(AimidbAttribute.LOCATION_ATTRIBUTE, new NitfValue(location));
    return assertMap;
  }

  @Test
  public void testAimidb() throws IOException, NitfFormatException {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      Map<NitfAttribute, NitfValue> treMap = createNitfWithAimidb(nitfFile);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("aimidbTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        headerTransformer.transform(nitfSegmentsFlow, metacard);
        transformer.transform(nitfSegmentsFlow, metacard);
        assertAttributesMap(metacard, treMap);
      }
    } finally {
      nitfFile.delete();
    }
  }

  private static Map<NitfAttribute, NitfValue> createNitfWithExpltb(File file) {
    String angleToNorth = "150.001";
    String angleToNorthAccuracy = "03.001";
    String mode = "LBM";
    String primeId = "aaaaaaaaaaaa";

    Tre expltb = TreFactory.getDefault("EXPLTB", TreSource.ImageExtendedSubheaderData);
    expltb.add(new TreEntryImpl("ANGLE_TO_NORTH", angleToNorth, "float"));
    expltb.add(new TreEntryImpl("ANGLE_TO_NORTH_ACCY", angleToNorthAccuracy, "float"));
    expltb.add(new TreEntryImpl("SQUINT_ANGLE", "-59.002", "float"));
    expltb.add(new TreEntryImpl("SQUINT_ANGLE_ACCY", "44.002", "float"));
    expltb.add(new TreEntryImpl("MODE", mode, "string"));
    expltb.add(new TreEntryImpl("GRAZE_ANG", "50.00", "float"));
    expltb.add(new TreEntryImpl("GRAZE_ANG_ACCY", "00.01", "float"));
    expltb.add(new TreEntryImpl("SLOPE_ANG", "24.00", "float"));
    expltb.add(new TreEntryImpl("POLAR", "HH", "UINT"));
    expltb.add(new TreEntryImpl("NSAMP", "03333", "UINT"));
    expltb.add(new TreEntryImpl("SEQ_NUM", "4", "string"));
    expltb.add(new TreEntryImpl("PRIME_ID", primeId, "string"));
    expltb.add(new TreEntryImpl("PRIME_BE", "bbbbbbbbbbbbbbb", "string"));
    expltb.add(new TreEntryImpl("N_SEC", "11", "UINT"));
    expltb.add(new TreEntryImpl("IPR", "22", "UINT"));
    ImageSegment imageSegment = TreUtilityTest.createImageSegment();
    imageSegment.getTREsRawStructure().add(expltb);

    new NitfCreationFlowImpl()
        .fileHeader(TreUtilityTest::createFileHeader)
        .imageSegment(() -> imageSegment)
        .write(file.getAbsolutePath());

    // key value pair of nitf attributes and expected getAttributes
    Map<NitfAttribute, NitfValue> assertMap = new HashMap<>();
    assertMap.put(
        ExpltbAttribute.ANGLE_TO_NORTH_ATTRIBUTE, new NitfValue(Float.parseFloat(angleToNorth)));
    assertMap.put(
        ExpltbAttribute.ANGLE_TO_NORTH_ACCURACY_ATTRIBUTE,
        new NitfValue(Float.parseFloat(angleToNorthAccuracy)));
    assertMap.put(ExpltbAttribute.MODE_ATTRIBUTE, new NitfValue(mode));
    assertMap.put(ExpltbAttribute.PRIME_ID_ATTRIBUTE, new NitfValue(primeId));
    return assertMap;
  }

  @Test
  public void testExpltb() throws IOException, NitfFormatException {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      Map<NitfAttribute, NitfValue> treMap = createNitfWithExpltb(nitfFile);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("expltbTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        headerTransformer.transform(nitfSegmentsFlow, metacard);
        transformer.transform(nitfSegmentsFlow, metacard);
        assertAttributesMap(metacard, treMap);
      }
    } finally {
      nitfFile.delete();
    }
  }

  private static Map<NitfAttribute, NitfValue> createNitfWithPiaprd(File file) {
    String accessId = "THIS IS AN IPA FILE.                                       -END-";
    String keyword =
        "FIRST                                                             "
            + "                                                        "
            + "                                                        "
            + "                                                        "
            + "                -END-";

    Tre piaprd = TreFactory.getDefault("PIAPRD", TreSource.ImageExtendedSubheaderData);
    piaprd.add(new TreEntryImpl("ACCESSID", accessId, "string"));
    piaprd.add(new TreEntryImpl("FMCONTROL", "PXX                        -END-", "string"));
    piaprd.add(new TreEntryImpl("SUBDET", "P", "string"));
    piaprd.add(new TreEntryImpl("PRODCODE", "YY", "string"));
    piaprd.add(new TreEntryImpl("PRODUCERSE", "UNKNOW", "string"));
    piaprd.add(new TreEntryImpl("PRODIDNO", "X211           -END-", "string"));
    piaprd.add(new TreEntryImpl("PRODSNME", "JUNK FILE.", "string"));
    piaprd.add(new TreEntryImpl("PRODUCERCD", "27", "string"));
    piaprd.add(new TreEntryImpl("PRODCRTIME", "26081023ZOCT95", "string"));
    piaprd.add(new TreEntryImpl("MAPID", "132                                -END-", "string"));
    piaprd.add(new TreEntryImpl("SECTITLEREP", "01", "UINT"));
    TreEntryImpl secTitleEntry = new TreEntryImpl("SECTITLE", null, "string");
    TreGroup secTitleGroup =
        TreFactory.getDefault("SECTITLE", TreSource.ImageExtendedSubheaderData);
    secTitleGroup
        .getEntries()
        .add(0, new TreEntryImpl("SECTITLE", "                                   -END-", "string"));
    secTitleGroup.getEntries().add(1, new TreEntryImpl("PPNUM", "32/47", "string"));
    secTitleGroup.getEntries().add(2, new TreEntryImpl("TPP", "001", "UINT"));
    secTitleEntry.initGroups();
    secTitleEntry.addGroup(secTitleGroup);
    piaprd.add(secTitleEntry);
    piaprd.add(new TreEntryImpl("REQORGREP", "01", "UINT"));
    TreEntryImpl reqorgEntry = new TreEntryImpl("REQORG", null, "string");
    TreGroup reqorgGroup = TreFactory.getDefault("REQORG", TreSource.ImageExtendedSubheaderData);
    reqorgGroup
        .getEntries()
        .add(
            0,
            new TreEntryImpl(
                "REQORG",
                "FIRST                                                      -END-",
                "string"));
    reqorgEntry.initGroups();
    reqorgEntry.addGroup(reqorgGroup);
    piaprd.add(reqorgEntry);
    piaprd.add(new TreEntryImpl("KEYWORDREP", "01", "UINT"));
    TreEntryImpl keywordEntry = new TreEntryImpl("KEYWORD", null, "string");
    TreGroup keywordGroup = TreFactory.getDefault("KEYWORD", TreSource.ImageExtendedSubheaderData);
    keywordGroup.getEntries().add(0, new TreEntryImpl("KEYWORD", keyword, "string"));
    keywordEntry.initGroups();
    keywordEntry.addGroup(keywordGroup);
    piaprd.add(keywordEntry);
    piaprd.add(new TreEntryImpl("ASSRPTREP", "01", "UNIT"));
    TreEntryImpl assrptEntry = new TreEntryImpl("ASSRPT", null, "string");
    TreGroup asserptGroup = TreFactory.getDefault("ASSRPT", TreSource.ImageExtendedSubheaderData);
    asserptGroup.getEntries().add(0, new TreEntryImpl("ASSRPT", "FIRST          -END-", "string"));
    assrptEntry.initGroups();
    assrptEntry.addGroup(asserptGroup);
    piaprd.add(assrptEntry);
    piaprd.add(new TreEntryImpl("ATEXTREP", "01", "UINT"));
    TreEntryImpl atextEntry = new TreEntryImpl("ATEXT", null, "string");
    TreGroup atextGroup = TreFactory.getDefault("ATEXT", TreSource.ImageExtendedSubheaderData);
    atextGroup
        .getEntries()
        .add(
            0,
            new TreEntryImpl(
                "ATEXT",
                "FIRST                                                             "
                    + "                                                        "
                    + "                                                        "
                    + "                                                        "
                    + "                -END-",
                "string"));
    atextEntry.initGroups();
    atextEntry.addGroup(atextGroup);
    piaprd.add(atextEntry);

    ImageSegment imageSegment = TreUtilityTest.createImageSegment();
    imageSegment.getTREsRawStructure().add(piaprd);
    new NitfCreationFlowImpl()
        .fileHeader(TreUtilityTest::createFileHeader)
        .imageSegment(() -> imageSegment)
        .write(file.getAbsolutePath());

    // key value pair of nitf attributes and expected getAttributes
    Map<NitfAttribute, NitfValue> assertMap = new HashMap<>();
    assertMap.put(PiaprdAttribute.ACCESS_ID_ATTRIBUTE, new NitfValue(accessId));
    assertMap.put(IndexedPiaprdAttribute.KEYWORD_ATTRIBUTE, new NitfValue(keyword));
    return assertMap;
  }

  @Test
  public void testPiaprd() throws IOException, NitfFormatException {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      Map<NitfAttribute, NitfValue> treMap = createNitfWithPiaprd(nitfFile);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("piaprdTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        headerTransformer.transform(nitfSegmentsFlow, metacard);
        transformer.transform(nitfSegmentsFlow, metacard);
        assertAttributesMap(metacard, treMap);
      }
    } finally {
      nitfFile.delete();
    }
  }

  private static void createNitfWithCsdida(File file) {
    NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
    Tre csdida = TreFactory.getDefault("CSDIDA", TreSource.ExtendedHeaderData);
    csdida.add(new TreEntryImpl("DAY", "01", "UINT"));
    csdida.add(new TreEntryImpl("MONTH", str(3), "UINT"));
    csdida.add(new TreEntryImpl("YEAR", "1234", "UINT"));
    csdida.add(new TreEntryImpl("PLATFORM_CODE", "XY", "string"));
    csdida.add(new TreEntryImpl("VEHICLE_ID", "01", "string"));
    csdida.add(new TreEntryImpl("PASS", "01", "string"));
    csdida.add(new TreEntryImpl("OPERATION", "001", "UINT"));
    csdida.add(new TreEntryImpl("SENSOR_ID", str(2), "string"));
    csdida.add(new TreEntryImpl("PRODUCT_ID", str(2), "string"));
    csdida.add(new TreEntryImpl("RESERVED_0", str(4), "string"));
    csdida.add(new TreEntryImpl("TIME", "20000202010101", "UINT"));
    csdida.add(new TreEntryImpl("PROCESS_TIME", "20000202010101", "UINT"));
    csdida.add(new TreEntryImpl("RESERVED_1", str(2), "string"));
    csdida.add(new TreEntryImpl("RESERVED_2", str(2), "string"));
    csdida.add(new TreEntryImpl("RESERVED_3", str(1), "string"));
    csdida.add(new TreEntryImpl("RESERVED_4", str(1), "string"));
    csdida.add(new TreEntryImpl("SOFTWARE_VERSION_NUMBER", str(10), "string"));

    header.getTREsRawStructure().add(csdida);
    new NitfCreationFlowImpl().fileHeader(() -> header).write(file.getAbsolutePath());
  }

  @Test
  public void testCsdida() throws IOException, NitfFormatException {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      createNitfWithCsdida(nitfFile);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("csexraTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        headerTransformer.transform(nitfSegmentsFlow, metacard);
        assertThat(metacard.getAttribute(Isr.PLATFORM_ID).getValue(), is("XY01"));
      }

    } finally {
      nitfFile.delete();
    }
  }

  private static void createNitfWithCsexra(File file) {
    NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
    Tre csexra = TreFactory.getDefault("CSEXRA", TreSource.ImageExtendedSubheaderData);
    csexra.add(new TreEntryImpl("SNOW_DEPTH_CAT", "1", "string"));
    csexra.add(new TreEntryImpl("SENSOR", str(6), "string"));
    csexra.add(new TreEntryImpl("TIME_FIRST_LINE_IMAGE", "12345.000000", "string"));
    csexra.add(new TreEntryImpl("TIME_IMAGE_DURATION", "12345.000000", "string"));
    csexra.add(new TreEntryImpl("MAX_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("ALONG_SCAN_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("CROSS_SCAN_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("GEO_MEAN_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("A_S_VERT_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("C_S_VERT_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("GEO_MEAN_VERT_GSD", "123.5", "string"));
    csexra.add(new TreEntryImpl("GSD_BETA_ANGLE", "123.5", "string"));
    csexra.add(new TreEntryImpl("DYNAMIC_RANGE", "02047", "string"));
    csexra.add(new TreEntryImpl("NUM_LINES", "0000101", "string"));
    csexra.add(new TreEntryImpl("NUM_SAMPLES", "00101", "string"));
    csexra.add(new TreEntryImpl("ANGLE_TO_NORTH", "000.000", "string"));
    csexra.add(new TreEntryImpl("OBLIQUITY_ANGLE", "00.000", "string"));
    csexra.add(new TreEntryImpl("AZ_OF_OBLIQUITY", "000.000", "string"));
    csexra.add(new TreEntryImpl("GRD_COVER", "1", "string"));
    csexra.add(new TreEntryImpl("SNOW_DEPTH_CAT", "1", "string"));
    csexra.add(new TreEntryImpl("SUN_AZIMUTH", "000.000", "string"));
    csexra.add(new TreEntryImpl("SUN_ELEVATION", "-90.000", "string"));
    csexra.add(new TreEntryImpl("PREDICTED_NIIRS", "1.0", "string"));
    csexra.add(new TreEntryImpl("CIRCL_ERR", "000", "string"));
    csexra.add(new TreEntryImpl("LINEAR_ERR", "000", "string"));

    ImageSegment imageSegment = ImageSegmentFactory.getDefault(FileType.NITF_TWO_ONE);
    imageSegment.addImageBand(TreUtilityTest.createImageBand());

    imageSegment.getTREsRawStructure().add(csexra);
    new NitfCreationFlowImpl()
        .fileHeader(() -> header)
        .imageSegment(() -> imageSegment)
        .write(file.getAbsolutePath());
  }

  private static void createNitfWithPiaimc(File file) {
    NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
    Tre piaimc = TreFactory.getDefault("PIAIMC", TreSource.ImageExtendedSubheaderData);
    piaimc.add(new TreEntryImpl("CLOUDCVR", "070", "string"));
    piaimc.add(new TreEntryImpl("SRP", "Y", "string"));
    piaimc.add(new TreEntryImpl("SENSMODE", str(12), "string"));
    piaimc.add(new TreEntryImpl("SENSNAME", str(18), "string"));
    piaimc.add(new TreEntryImpl("SOURCE", str(255), "string"));
    piaimc.add(new TreEntryImpl("COMGEN", "09", "string"));
    piaimc.add(new TreEntryImpl("SUBQUAL", str(1), "string"));
    piaimc.add(new TreEntryImpl("PIAMSNNUM", str(7), "string"));
    piaimc.add(new TreEntryImpl("CAMSPECS", str(32), "string"));
    piaimc.add(new TreEntryImpl("PROJID", str(2), "string"));
    piaimc.add(new TreEntryImpl("GENERATION", "8", "string"));
    piaimc.add(new TreEntryImpl("ESD", "Y", "string"));
    piaimc.add(new TreEntryImpl("OTHERCOND", str(2), "string"));
    piaimc.add(new TreEntryImpl("MEANGSD", "00000.0", "string"));
    piaimc.add(new TreEntryImpl("IDATUM", str(3), "string"));
    piaimc.add(new TreEntryImpl("IELLIP", str(3), "string"));
    piaimc.add(new TreEntryImpl("PREPROC", str(2), "string"));
    piaimc.add(new TreEntryImpl("IPROJ", str(2), "string"));
    piaimc.add(new TreEntryImpl("SATTRACK_PATH", "0000", "string"));
    piaimc.add(new TreEntryImpl("SATTRACK_ROW", "0000", "string"));

    ImageSegment imageSegment = ImageSegmentFactory.getDefault(FileType.NITF_TWO_ONE);
    imageSegment.addImageBand(TreUtilityTest.createImageBand());

    imageSegment.getTREsRawStructure().add(piaimc);
    new NitfCreationFlowImpl()
        .fileHeader(() -> header)
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
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        transformer.transform(nitfSegmentsFlow, metacard);
        assertThat(metacard.getAttribute(Isr.CLOUD_COVER).getValue(), is(70.0));
      }

    } finally {
      nitfFile.delete();
    }
  }

  private static void createNitfWithPiatgb(File file) {
    NitfHeader header = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);
    Tre piatgb = TreFactory.getDefault("PIATGB", TreSource.ImageExtendedSubheaderData);
    piatgb.add(new TreEntryImpl("TGTUTM", "55HFA9359093610", "string"));
    piatgb.add(new TreEntryImpl("PIATGAID", "ABCDEFGHIJUVWXY", "string"));
    piatgb.add(new TreEntryImpl("PIACTRY", "AS", "string"));
    piatgb.add(new TreEntryImpl("PIACAT", "702XX", "string"));
    piatgb.add(new TreEntryImpl("TGTGEO", "351655S1490742E", "string"));
    piatgb.add(new TreEntryImpl("DATUM", "WGE", "string"));
    piatgb.add(new TreEntryImpl("TGTNAME", "Canberra Hill                         ", "string"));
    piatgb.add(new TreEntryImpl("PERCOVER", "57", "UINT"));
    piatgb.add(new TreEntryImpl("TGTLAT", "-35.30812 ", "float"));
    piatgb.add(new TreEntryImpl("TGTLON", "+149.12447 ", "float"));

    ImageSegment imageSegment = ImageSegmentFactory.getDefault(FileType.NITF_TWO_ONE);
    imageSegment.addImageBand(TreUtilityTest.createImageBand());

    imageSegment.getTREsRawStructure().add(piatgb);
    new NitfCreationFlowImpl()
        .fileHeader(() -> header)
        .imageSegment(() -> imageSegment)
        .write(file.getAbsolutePath());
  }

  @Test
  public void testPiatgb() throws IOException, NitfFormatException {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      createNitfWithPiatgb(nitfFile);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("piatgbTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        transformer.transform(nitfSegmentsFlow, metacard);
        assertThat(
            metacard.getAttribute(PiatgbAttribute.TARGET_NAME_ATTRIBUTE.getLongName()).getValue(),
            is("Canberra Hill"));
      }
    } finally {
      nitfFile.delete();
    }
  }

  @Test
  public void testCsexraSnowDepthMin() throws IOException, NitfFormatException {
    testCsexra(
        metacard ->
            assertThat(
                ((Float) metacard.getAttribute(Isr.SNOW_DEPTH_MIN_CENTIMETERS).getValue())
                    .doubleValue(),
                is(closeTo(2.54, 0.01))));
  }

  @Test
  public void testCsexraSnowDepthMax() throws IOException, NitfFormatException {
    testCsexra(
        metacard ->
            assertThat(
                ((Float) metacard.getAttribute(Isr.SNOW_DEPTH_MAX_CENTIMETERS).getValue())
                    .doubleValue(),
                is(closeTo(22.86, 0.01))));
  }

  @Test
  public void testCsexraSnowCover() throws IOException, NitfFormatException {
    testCsexra(
        metacard -> assertThat(metacard.getAttribute(Isr.SNOW_COVER).getValue(), is(Boolean.TRUE)));
  }

  @Test
  public void testCsexraNIIRS() throws IOException, NitfFormatException {
    testCsexra(
        metacard ->
            assertThat(
                metacard
                    .getAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE)
                    .getValue(),
                is(1.0f)));
  }

  private void testCsexra(Consumer<Metacard> consumer) throws IOException, NitfFormatException {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      createNitfWithCsexra(nitfFile);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("csexraTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();
        transformer.transform(nitfSegmentsFlow, metacard);
        consumer.accept(metacard);
      }

    } finally {
      nitfFile.delete();
    }
  }

  private static void createNitfWithDifferentImageDateTimes(
      File file, DateTime fileDateTime, DateTime... imageDateTimes) {
    NitfCreationFlow nitfCreationFlow =
        new NitfCreationFlowImpl().fileHeader(() -> TreUtilityTest.createFileHeader(fileDateTime));
    Arrays.stream(imageDateTimes)
        .forEach(
            imageDateTime ->
                nitfCreationFlow.imageSegment(() -> createImageSegment(imageDateTime)));
    nitfCreationFlow.write(file.getAbsolutePath());
  }

  @Test
  public void testNitfWithDifferentImageDates() throws Exception {
    File nitfFile = File.createTempFile("nitf-", ".ntf");
    try {
      final DateTime fileDateTime = NitfTestCommons.createNitfDateTime(2016, 1, 1, 0, 0, 0);
      DateTime[] imageDateTimes = {
        NitfTestCommons.createNitfDateTime(2001, 1, 1, 0, 0, 0),
        NitfTestCommons.createNitfDateTime(2002, 1, 1, 0, 0, 0),
        NitfTestCommons.createNitfDateTime(2003, 1, 1, 0, 0, 0)
      };

      createNitfWithDifferentImageDateTimes(nitfFile, fileDateTime, imageDateTimes);

      try (InputStream inputStream = new FileInputStream(nitfFile)) {
        Metacard metacard = metacardFactory.createMetacard("differentImageDateTimesTest");
        NitfSegmentsFlow nitfSegmentsFlow =
            new NitfParserInputFlowImpl().inputStream(inputStream).headerOnly();

        nitfSegmentsFlow = headerTransformer.transform(nitfSegmentsFlow, metacard);
        metacard = transformer.transform(nitfSegmentsFlow, metacard);

        assertNotNull(metacard);
        validateDates(metacard, fileDateTime, imageDateTimes);
      }
    } finally {
      nitfFile.delete();
    }
  }

  private InputStream getInputStream(String filename) {
    assertNotNull("Test file missing", getClass().getClassLoader().getResource(filename));
    return getClass().getClassLoader().getResourceAsStream(filename);
  }

  /** expected attribute values for i_3001a.ntf */
  private static Map<NitfAttribute, NitfValue> initAttributesToBeAsserted() {
    // key value pair of attributes and expected getAttributes
    Map<NitfAttribute, NitfValue> map = new HashMap<>();
    map.put(NitfHeaderAttribute.FILE_PROFILE_NAME_ATTRIBUTE, new NitfValue("NITF"));
    map.put(NitfHeaderAttribute.FILE_VERSION_ATTRIBUTE, new NitfValue("2.1"));
    map.put(NitfHeaderAttribute.COMPLEXITY_LEVEL_ATTRIBUTE, new NitfValue(3));
    map.put(NitfHeaderAttribute.STANDARD_TYPE_ATTRIBUTE, new NitfValue("BF01"));
    map.put(NitfHeaderAttribute.ORIGINATING_STATION_ID_ATTRIBUTE, new NitfValue("i_3001a"));
    map.put(
        NitfHeaderAttribute.FILE_TITLE_ATTRIBUTE,
        new NitfValue(
            "Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield"));
    map.put(
        NitfHeaderAttribute.FILE_SECURITY_CLASSIFICATION_ATTRIBUTE, new NitfValue("UNCLASSIFIED"));
    map.put(
        NitfHeaderAttribute.FILE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE,
        new NitfValue(TEST_CLASSIFICATION_SYSTEM));
    map.put(NitfHeaderAttribute.FILE_CODE_WORDS_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_CONTROL_AND_HANDLING_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_DECLASSIFICATION_TYPE_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_DECLASSIFICATION_DATE_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_DECLASSIFICATION_EXEMPTION_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_DOWNGRADE_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_RELEASING_INSTRUCTIONS_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_DOWNGRADE_DATE_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_TEXT_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_AUTHORITY_TYPE_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_AUTHORITY_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_CLASSIFICATION_REASON_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_SECURITY_SOURCE_DATE_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_SECURITY_CONTROL_NUMBER_ATTRIBUTE, new NitfValue(null));
    map.put(NitfHeaderAttribute.FILE_COPY_NUMBER_ATTRIBUTE, new NitfValue("00000"));
    map.put(NitfHeaderAttribute.FILE_BACKGROUND_COLOR_ATTRIBUTE, new NitfValue("[0xff,0xff,0xff]"));
    map.put(
        NitfHeaderAttribute.ORIGINATORS_NAME_ATTRIBUTE, new NitfValue("JITC Fort Huachuca, AZ"));
    map.put(
        NitfHeaderAttribute.ORIGINATORS_PHONE_NUMBER_ATTRIBUTE, new NitfValue("(520) 538-5458"));
    map.put(ImageAttribute.FILE_PART_TYPE_ATTRIBUTE, new NitfValue("IM"));
    map.put(ImageAttribute.IMAGE_IDENTIFIER_1_ATTRIBUTE, new NitfValue("Missing ID"));
    map.put(ImageAttribute.ISR_TARGET_IDENTIFIER_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.NITF_TARGET_IDENTIFIER_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.TARGET_IDENTIFIER_COUNTRY_CODE_ATTRIBUTE, new NitfValue(null));
    final String imageIdentifier2 = "- BASE IMAGE -";
    map.put(ImageAttribute.MISSION_ID_ATTRIBUTE, new NitfValue(imageIdentifier2));
    map.put(ImageAttribute.IMAGE_IDENTIFIER_2_ATTRIBUTE, new NitfValue(imageIdentifier2));
    map.put(ImageAttribute.IMAGE_SECURITY_CLASSIFICATION_ATTRIBUTE, new NitfValue("UNCLASSIFIED"));
    map.put(ImageAttribute.IMAGE_CLASSIFICATION_SECURITY_SYSTEM_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_CODEWORDS_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_CONTROL_AND_HANDLING_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_RELEASING_INSTRUCTIONS_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_DECLASSIFICATION_TYPE_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_DECLASSIFICATION_DATE_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_DECLASSIFICATION_EXEMPTION_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_DOWNGRADE_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_DOWNGRADE_DATE_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_CLASSIFICATION_TEXT_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_CLASSIFICATION_AUTHORITY_TYPE_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_CLASSIFICATION_AUTHORITY_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_CLASSIFICATION_REASON_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_SECURITY_SOURCE_DATE_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_SECURITY_CONTROL_NUMBER_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_SOURCE_ATTRIBUTE, new NitfValue("Unknown"));
    map.put(ImageAttribute.NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE_ATTRIBUTE, new NitfValue(1024L));
    map.put(ImageAttribute.NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE_ATTRIBUTE, new NitfValue(1024L));
    map.put(ImageAttribute.PIXEL_VALUE_TYPE_ATTRIBUTE, new NitfValue("INTEGER"));
    map.put(ImageAttribute.IMAGE_REPRESENTATION_ATTRIBUTE, new NitfValue("MONOCHROME"));
    map.put(ImageAttribute.IMAGE_CATEGORY_ATTRIBUTE, new NitfValue("VISUAL", "VIS"));
    map.put(ImageAttribute.ACTUAL_BITS_PER_PIXEL_PER_BAND_ATTRIBUTE, new NitfValue(8));
    map.put(ImageAttribute.PIXEL_JUSTIFICATION_ATTRIBUTE, new NitfValue("RIGHT"));
    map.put(ImageAttribute.IMAGE_COORDINATE_REPRESENTATION_ATTRIBUTE, new NitfValue("GEOGRAPHIC"));
    map.put(ImageAttribute.NUMBER_OF_IMAGE_COMMENTS_ATTRIBUTE, new NitfValue(0));
    map.put(ImageAttribute.IMAGE_COMMENT_1_ATTRIBUTE, new NitfValue(null));
    map.put(ImageAttribute.IMAGE_COMPRESSION_ATTRIBUTE, new NitfValue("NOTCOMPRESSED"));
    return map;
  }

  private static void assertAttributesMap(Metacard metacard, Map<NitfAttribute, NitfValue> map) {
    for (Map.Entry<NitfAttribute, NitfValue> entry : map.entrySet()) {
      for (AttributeDescriptor attributeDescriptor :
          (Set<AttributeDescriptor>) entry.getKey().getAttributeDescriptors()) {
        Attribute attribute = metacard.getAttribute(attributeDescriptor.getName());
        if (attribute != null) {
          if (attribute.getName().contains(ExtNitfUtility.EXT_NITF_PREFIX)) {
            assertThat(attribute.getValue(), is(entry.getValue().getExtValue()));
          } else {
            assertThat(attribute.getValue(), is(entry.getValue().getTaxonomyValue()));
          }
        } else {
          assertThat(attribute, nullValue());
        }
      }
    }
  }

  private static void validateDates(
      Metacard metacard, DateTime fileDateTime, DateTime... imageDateTimes) {
    final DateTime firstImageDateTime = imageDateTimes[0];
    final DateTime lastImageDateTime =
        imageDateTimes.length > 1 ? imageDateTimes[imageDateTimes.length - 1] : firstImageDateTime;

    assertDateAttribute(
        "datetime.start should be the date and time of the first image segment",
        metacard.getAttribute(START),
        firstImageDateTime);
    assertDateAttribute(
        "effective should be the date and time of the first image segment",
        metacard,
        FILE_DATE_AND_TIME_EFFECTIVE_ATTRIBUTE,
        firstImageDateTime);
    assertDateAttribute(
        "effective should be the date and time of each image segment",
        metacard,
        IMAGE_DATE_AND_TIME_ATTRIBUTE,
        imageDateTimes);
    assertDateAttribute(
        "datetime.end should be the date and time of the last image segment",
        metacard.getAttribute(END),
        lastImageDateTime);
    assertDateAttribute(
        "created should be the file date and time of the header",
        metacard,
        FILE_DATE_AND_TIME_CREATED_ATTRIBUTE,
        fileDateTime);
    assertDateAttribute(
        "modified created should be the file date and time of the header",
        metacard,
        FILE_DATE_AND_TIME_MODIFIED_ATTRIBUTE,
        fileDateTime);
  }

  private static void assertDateAttribute(
      String reason, Attribute attribute, DateTime... expectedDateTimes) {
    List<Date> expectedDates =
        Stream.of(expectedDateTimes)
            .map(NitfAttributeConverters::nitfDate)
            .collect(Collectors.toList());

    assertThat(reason, attribute.getValues(), is(expectedDates));
  }

  private static void assertDateAttribute(
      String reason,
      Metacard metacard,
      NitfAttribute nitfAttribute,
      DateTime... expectedDateTimes) {
    for (AttributeDescriptor attributeDescriptor :
        (Set<AttributeDescriptor>) nitfAttribute.getAttributeDescriptors()) {
      final Attribute attribute = metacard.getAttribute(attributeDescriptor.getName());

      assertDateAttribute(reason, attribute, expectedDateTimes);
    }
  }

  private static final class NitfValue {
    private final Object taxonomyValue;

    private final Object extValue;

    NitfValue(@Nullable Object value, @Nullable Object extValue) {
      this.taxonomyValue = value;
      this.extValue = extValue;
    }

    NitfValue(@Nullable Object value) {
      this.taxonomyValue = value;
      this.extValue = value;
    }

    Object getTaxonomyValue() {
      return taxonomyValue;
    }

    Object getExtValue() {
      return extValue;
    }
  }
}
