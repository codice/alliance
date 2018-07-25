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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.function.Consumer;
import org.codice.alliance.transformer.nitf.common.TreUtility;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.impl.DateTimeImpl;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.image.ImageBand;
import org.codice.imaging.nitf.core.image.ImageCategory;
import org.codice.imaging.nitf.core.image.ImageCompression;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.ImageMode;
import org.codice.imaging.nitf.core.image.ImageRepresentation;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.core.image.PixelJustification;
import org.codice.imaging.nitf.core.image.PixelValueType;
import org.codice.imaging.nitf.core.image.impl.TargetIdImpl;
import org.codice.imaging.nitf.core.impl.RGBColourImpl;
import org.codice.imaging.nitf.core.security.FileSecurityMetadata;
import org.codice.imaging.nitf.core.security.SecurityClassification;
import org.codice.imaging.nitf.core.security.SecurityMetadata;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreCollection;
import org.codice.imaging.nitf.core.tre.TreGroup;
import org.codice.imaging.nitf.core.tre.TreSource;
import org.codice.imaging.nitf.core.tre.impl.TreCollectionImpl;
import org.codice.imaging.nitf.core.tre.impl.TreEntryImpl;
import org.codice.imaging.nitf.core.tre.impl.TreFactory;
import org.codice.imaging.nitf.fluent.impl.NitfCreationFlowImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreUtilityTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TreUtilityTest.class);

  @Test
  public void testConvertToFloat() {
    Tre tre = TreFactory.getDefault("TestTre", TreSource.ImageExtendedSubheaderData);
    tre.add(new TreEntryImpl("FLOAT_VALID_1", "+123.456e2", "float"));
    tre.add(new TreEntryImpl("FLOAT_VALID_2", "-123", "float"));
    tre.add(new TreEntryImpl("FLOAT_VALID_3", "0x1FFF.0p0", "float"));
    tre.add(new TreEntryImpl("FLOAT_VALID_4", "0009.9000", "float"));
    tre.add(new TreEntryImpl("FLOAT_VALID_5", "-0009.9000", "float"));

    tre.add(new TreEntryImpl("FLOAT_INVALID_1", "0D2310", "float"));
    tre.add(new TreEntryImpl("FLOAT_INVALID_2", "-0w.D3", "float"));
    tre.add(new TreEntryImpl("FLOAT_INVALID_3", "-+0x1FFF.0p0", "float"));
    tre.add(new TreEntryImpl("FLOAT_INVALID_4", "12367.0x0FF", "float"));
    tre.add(new TreEntryImpl("FLOAT_INVALID_5", "1,0", "float"));

    assertThat(TreUtility.convertToFloat(tre, "FLOAT_VALID_1"), is(12345.6f));
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_VALID_2"), is(-123.0f));
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_VALID_3"), is(8191.0f));
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_VALID_4"), is(9.9f));
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_VALID_5"), is(-9.9f));

    assertThat(TreUtility.convertToFloat(tre, "FLOAT_INVALID_1"), nullValue());
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_INVALID_2"), nullValue());
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_INVALID_3"), nullValue());
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_INVALID_4"), nullValue());
    assertThat(TreUtility.convertToFloat(tre, "FLOAT_INVALID_5"), nullValue());
  }

  @Test
  public void testConvertToInteger() {
    Tre tre = TreFactory.getDefault("TestTre", TreSource.ImageExtendedSubheaderData);
    tre.add(new TreEntryImpl("INTEGER_VALID_1", "12345", "UINT"));
    tre.add(new TreEntryImpl("INTEGER_VALID_2", "-12345", "UINT"));
    tre.add(new TreEntryImpl("INTEGER_VALID_3", "-0120", "UINT"));

    tre.add(new TreEntryImpl("INTEGER_INVALID_1", "1.2", "UINT"));
    tre.add(new TreEntryImpl("INTEGER_INVALID_2", "-1.3-9", "UINT"));
    tre.add(new TreEntryImpl("INTEGER_INVALID_3", "ABCD", "UINT"));

    assertThat(TreUtility.convertToInteger(tre, "INTEGER_VALID_1"), is(12345));
    assertThat(TreUtility.convertToInteger(tre, "INTEGER_VALID_2"), is(-12345));
    assertThat(TreUtility.convertToInteger(tre, "INTEGER_VALID_3"), is(-120));

    assertThat(TreUtility.convertToInteger(tre, "INTEGER_INVALID_1"), nullValue());
    assertThat(TreUtility.convertToInteger(tre, "INTEGER_INVALID_2"), nullValue());
    assertThat(TreUtility.convertToInteger(tre, "INTEGER_INVALID_3"), nullValue());
  }

  public static void createFileIfNecessary(String filename, Consumer<String> consumer) {
    File file = new File(filename);

    if (!file.exists()) {
      consumer.accept(filename);
    }
  }

  public static void createNitfNoImageNoTres(String filename) {
    new NitfCreationFlowImpl().fileHeader(TreUtilityTest::createFileHeader).write(filename);
  }

  public static void createNitfImageNoTres(String filename) {
    new NitfCreationFlowImpl()
        .fileHeader(TreUtilityTest::createFileHeader)
        .imageSegment(TreUtilityTest::createImageSegment)
        .write(filename);
  }

  public static void createNitfNoImageTres(String filename) {
    new NitfCreationFlowImpl()
        .fileHeader(
            () -> {
              try {
                return createFileHeaderWithMtirpb();
              } catch (NitfFormatException e) {
                LOGGER.error(e.getMessage(), e);
              }

              return null;
            })
        .write(filename);
  }

  public static void createNitfImageTres(String filename) {
    new NitfCreationFlowImpl()
        .fileHeader(
            () -> {
              try {
                return createFileHeaderWithMtirpb();
              } catch (NitfFormatException e) {
                LOGGER.error(e.getMessage(), e);
              }

              return null;
            })
        .imageSegment(TreUtilityTest::createImageSegment)
        .write(filename);
  }

  private static NitfHeader createFileHeaderWithMtirpb() throws NitfFormatException {
    NitfHeader nitfHeader = createFileHeader();
    TreCollection treCollection = nitfHeader.getTREsRawStructure();
    treCollection.add(createMtirpbTre());
    return nitfHeader;
  }

  private static Tre createMtirpbTre() throws NitfFormatException {
    final String[] fieldNames = {
      "MTI_DP",
      "MTI_PACKET_ID",
      "PATCH_NO",
      "WAMTI_FRAME_NO",
      "WAMTI_BAR_NO",
      "DATIME",
      "ACFT_LOC",
      "ACFT_ALT",
      "ACFT_ALT_UNIT",
      "ACFT_HEADING",
      "MTI_LR",
      "SQUINT_ANGLE",
      "COSGRZ",
      "NO_VALID_TARGETS"
    };
    final String[] values = {
      "00",
      "001",
      "0001",
      "00001",
      "1",
      "20141108235219",
      "+52.123456-004.123456",
      "150000",
      "m",
      "000",
      " ",
      "      ",
      "0.03111",
      "001"
    };

    StringBuilder accumulator = new StringBuilder();
    Tre tre = mock(Tre.class);
    when(tre.getName()).thenReturn("MTIRPB");

    for (int i = 0; i < fieldNames.length; i++) {
      accumulator.append(values[i]);
      when(tre.getEntry(fieldNames[i]))
          .thenReturn(new TreEntryImpl(fieldNames[i], values[i], "string"));
    }

    TreGroup targetsGroup = createTreGroup(accumulator);
    TreEntryImpl targetsEntry = new TreEntryImpl("TARGETS");
    targetsEntry.addGroup(targetsGroup);
    when(tre.getEntry("TARGETS")).thenReturn(targetsEntry);
    when(tre.getSource()).thenReturn(TreSource.UserDefinedHeaderData);
    when(tre.getRawData()).thenReturn(accumulator.toString().getBytes());
    return tre;
  }

  private static TreGroup createTreGroup(StringBuilder stringBuilder) throws NitfFormatException {
    final String[] treGroupFields = {
      "TGT_LOC", "TGT_LOC_ACCY", "TGT_VEL_R", "TGT_SPEED", "TGT_HEADING", "TGT_AMPLITUDE", "TGT_CAT"
    };
    final String[] treGroupValues = {
      "+52.1234567-004.1234567", "000.00", "+013", "000", "000", "06", "U"
    };

    TreGroup treGroup = mock(TreGroup.class);

    for (int i = 0; i < treGroupFields.length; i++) {
      when(treGroup.getFieldValue(treGroupFields[i])).thenReturn(treGroupValues[i]);
      stringBuilder.append(treGroupValues[i]);
    }

    return treGroup;
  }

  public static NitfHeader createFileHeader() {
    return createFileHeader(DateTimeImpl.getNitfDateTimeForNow());
  }

  public static NitfHeader createFileHeader(DateTime fileDateTime) {
    TreCollection treCollection = new TreCollectionImpl();
    FileSecurityMetadata securityMetadata = createSecurityMetadata();
    NitfHeader nitfHeader = mock(NitfHeader.class);
    when(nitfHeader.getFileTitle()).thenReturn("TEST NITF");
    when(nitfHeader.getFileType()).thenReturn(FileType.NITF_TWO_ONE);
    when(nitfHeader.getComplexityLevel()).thenReturn(1);
    when(nitfHeader.getFileDateTime()).thenReturn(fileDateTime);
    when(nitfHeader.getOriginatingStationId()).thenReturn("LOCALHOST");
    when(nitfHeader.getStandardType()).thenReturn("BF01");
    when(nitfHeader.getFileBackgroundColour())
        .thenReturn(new RGBColourImpl((byte) 0, (byte) 0, (byte) 0));
    when(nitfHeader.getOriginatorsName()).thenReturn("");
    when(nitfHeader.getOriginatorsPhoneNumber()).thenReturn("");
    when(nitfHeader.getFileSecurityMetadata()).thenReturn(securityMetadata);
    when(nitfHeader.getTREsRawStructure()).thenReturn(treCollection);
    return nitfHeader;
  }

  public static FileSecurityMetadata createSecurityMetadata() {
    FileSecurityMetadata securityMetadata = mock(FileSecurityMetadata.class);
    when(securityMetadata.getFileType()).thenReturn(FileType.NITF_TWO_ONE);
    when(securityMetadata.getSecurityClassification())
        .thenReturn(SecurityClassification.UNCLASSIFIED);
    when(securityMetadata.getSecurityClassificationSystem()).thenReturn("");
    when(securityMetadata.getCodewords()).thenReturn("");
    when(securityMetadata.getControlAndHandling()).thenReturn("");
    when(securityMetadata.getReleaseInstructions()).thenReturn("");
    when(securityMetadata.getDeclassificationType()).thenReturn("");
    when(securityMetadata.getDeclassificationDate()).thenReturn("");
    when(securityMetadata.getDeclassificationExemption()).thenReturn("");
    when(securityMetadata.getDowngrade()).thenReturn("");
    when(securityMetadata.getDowngradeDate()).thenReturn("");
    when(securityMetadata.getClassificationText()).thenReturn("");
    when(securityMetadata.getClassificationAuthorityType()).thenReturn("");
    when(securityMetadata.getClassificationAuthority()).thenReturn("");
    when(securityMetadata.getClassificationReason()).thenReturn("");
    when(securityMetadata.getSecuritySourceDate()).thenReturn("");
    when(securityMetadata.getSecurityControlNumber()).thenReturn("");
    when(securityMetadata.getFileCopyNumber()).thenReturn("");
    when(securityMetadata.getFileNumberOfCopies()).thenReturn("");
    return securityMetadata;
  }

  public static NitfHeader createFileHeader(FileSecurityMetadata fileSecurityMetadata) {
    return createFileHeader(DateTimeImpl.getNitfDateTimeForNow(), fileSecurityMetadata);
  }

  private static NitfHeader createFileHeader(
      DateTime fileDateTime, FileSecurityMetadata fileSecurityMetadata) {
    NitfHeader nitfHeader = createFileHeader(fileDateTime);
    when(nitfHeader.getFileSecurityMetadata()).thenReturn(fileSecurityMetadata);
    return nitfHeader;
  }

  public static ImageSegment createImageSegment() {
    return createImageSegment(DateTimeImpl.getNitfDateTimeForNow());
  }

  public static ImageSegment createImageSegment(DateTime imageDateTime) {
    SecurityMetadata securityMetadata = createSecurityMetadata();
    ImageBand imageBand = createImageBand();
    ImageSegment imageSegment = mock(ImageSegment.class);
    TreCollection treCollection = new TreCollectionImpl();

    when(imageSegment.getFileType()).thenReturn(FileType.NITF_TWO_ONE);
    when(imageSegment.getNumBands()).thenReturn(3);
    when(imageSegment.getActualBitsPerPixelPerBand()).thenReturn(8);
    when(imageSegment.getIdentifier()).thenReturn("12345");
    when(imageSegment.getImageDateTime()).thenReturn(imageDateTime);
    when(imageSegment.getImageTargetId()).thenReturn(new TargetIdImpl());
    when(imageSegment.getImageIdentifier2()).thenReturn("");
    when(imageSegment.getSecurityMetadata()).thenReturn(securityMetadata);
    when(imageSegment.getImageSource()).thenReturn("");
    when(imageSegment.getNumberOfRows()).thenReturn(2048L);
    when(imageSegment.getNumberOfColumns()).thenReturn(2048L);
    when(imageSegment.getPixelValueType()).thenReturn(PixelValueType.INTEGER);
    when(imageSegment.getImageRepresentation()).thenReturn(ImageRepresentation.RGBTRUECOLOUR);
    when(imageSegment.getImageCategory()).thenReturn(ImageCategory.MULTISPECTRAL);
    when(imageSegment.getActualBitsPerPixelPerBand()).thenReturn(8);
    when(imageSegment.getPixelJustification()).thenReturn(PixelJustification.LEFT);
    when(imageSegment.getImageCoordinatesRepresentation())
        .thenReturn(ImageCoordinatesRepresentation.NONE);
    when(imageSegment.getImageCompression()).thenReturn(ImageCompression.NOTCOMPRESSED);
    when(imageSegment.getImageBandZeroBase(anyInt())).thenReturn(imageBand);
    when(imageSegment.getImageMode()).thenReturn(ImageMode.BANDSEQUENTIAL);
    when(imageSegment.getNumberOfBlocksPerRow()).thenReturn(4);
    when(imageSegment.getNumberOfBlocksPerColumn()).thenReturn(4);
    when(imageSegment.getNumberOfPixelsPerBlockHorizontal()).thenReturn(512L);
    when(imageSegment.getNumberOfPixelsPerBlockVertical()).thenReturn(512L);
    when(imageSegment.getImageDisplayLevel()).thenReturn(1);
    when(imageSegment.getAttachmentLevel()).thenReturn(2);
    when(imageSegment.getImageLocationRow()).thenReturn(0);
    when(imageSegment.getImageLocationColumn()).thenReturn(0);
    when(imageSegment.getImageMagnification()).thenReturn("1.00");
    when(imageSegment.getTREsRawStructure()).thenReturn(treCollection);
    return imageSegment;
  }

  public static ImageBand createImageBand() {
    ImageBand imageBand = mock(ImageBand.class);
    when(imageBand.getImageRepresentation()).thenReturn("RGB");
    when(imageBand.getSubCategory()).thenReturn("XXX");
    return imageBand;
  }
}
