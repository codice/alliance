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
package org.codice.alliance.imaging.chip.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.io.ByteSource;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import org.codice.ddf.platform.util.TemporaryFileBackedOutputStream;
import org.codice.imaging.nitf.core.DataSource;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.impl.DateTimeImpl;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.image.ImageCategory;
import org.codice.imaging.nitf.core.image.ImageCoordinatePair;
import org.codice.imaging.nitf.core.image.ImageCoordinates;
import org.codice.imaging.nitf.core.image.ImageRepresentation;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.core.image.PixelValueType;
import org.codice.imaging.nitf.core.image.impl.ImageCoordinatePairImpl;
import org.codice.imaging.nitf.core.image.impl.ImageCoordinatesImpl;
import org.codice.imaging.nitf.core.image.impl.TargetIdImpl;
import org.codice.imaging.nitf.core.impl.RGBColourImpl;
import org.codice.imaging.nitf.core.security.FileSecurityMetadata;
import org.codice.imaging.nitf.core.security.SecurityClassification;
import org.codice.imaging.nitf.core.security.SecurityMetadata;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreEntry;
import org.codice.imaging.nitf.core.tre.impl.TreCollectionImpl;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.codice.imaging.nitf.fluent.impl.NitfSegmentsFlowImpl;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

public class CatalogOutputAdapterTest {
  private static final String I_3001A = "/i_3001a.png";

  private static final Pattern COORD_REGEX = Pattern.compile("^[ 0-9]{8}\\.[0-9]{3}$");

  private static final Pattern TWO_DIGIT_REGEX = Pattern.compile("^[0-9]{2}$");

  private static final Pattern EIGHT_DIGIT_REGEX = Pattern.compile("^[0-9]{8}$");

  private static final String REAL = "real";

  private static final String UINT = "UINT";

  private CatalogOutputAdapter catalogOutputAdapter;

  private Semaphore lock;

  @Before
  public void setUp() throws IOException {
    lock = mock(Semaphore.class);
    this.catalogOutputAdapter = new CatalogOutputAdapter(lock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetImageNullCatalogResponse() throws IOException {
    catalogOutputAdapter.getImage(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetImageNullResource() throws IOException {
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(null);
    catalogOutputAdapter.getImage(resourceResponse);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetImageNullInputStream() throws IOException {
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    Resource resource = mock(Resource.class);
    when(resourceResponse.getResource()).thenReturn(resource);
    when(resource.getInputStream()).thenReturn(null);
    catalogOutputAdapter.getImage(resourceResponse);
  }

  @Test
  public void testGetImage() throws IOException, InterruptedException {
    InputStream is = getInputStream(I_3001A);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    Resource resource = mock(Resource.class);
    when(resourceResponse.getResource()).thenReturn(resource);
    when(resource.getInputStream()).thenReturn(is);
    BufferedImage image = catalogOutputAdapter.getImage(resourceResponse);
    assertThat(image, is(notNullValue()));
    assertThat(image.getWidth(), is(1024));
    assertThat(image.getHeight(), is(1024));
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetBinaryContentNullImage() throws IOException, MimeTypeParseException {
    catalogOutputAdapter.getBinaryContent(null);
  }

  @Test
  public void testGetNitfSegmentsFlow()
      throws IOException, NitfFormatException, InterruptedException {

    Resource resource = mock(Resource.class);
    when(resource.getInputStream()).thenReturn(getInputStream("/i_3001a.ntf"));

    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    NitfSegmentsFlow nitfSegmentsFlow = catalogOutputAdapter.getNitfSegmentsFlow(resourceResponse);

    assertThat(nitfSegmentsFlow, notNullValue());
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test
  public void testGetBinaryContent()
      throws IOException, MimeTypeParseException, InterruptedException {
    BufferedImage suppliedImage = ImageIO.read(getInputStream(I_3001A));
    suppliedImage =
        new BufferedImage(
            suppliedImage.getWidth(), suppliedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    BinaryContent binaryContent = catalogOutputAdapter.getBinaryContent(suppliedImage);
    assertThat(binaryContent, is(notNullValue()));
    assertThat(binaryContent.getInputStream(), is(notNullValue()));

    BufferedImage returnedImage = ImageIO.read(binaryContent.getInputStream());
    assertThat(returnedImage.getWidth(), is(1024));
    assertThat(returnedImage.getHeight(), is(1024));
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test
  public void testGrayscale()
      throws MimeTypeParseException, NitfFormatException, IOException, NoSuchMethodException,
          IllegalAccessException, InvocationTargetException, InstantiationException {

    int originalWidth = 200;
    int originalHeight = 100;

    int chipWidth = 160;
    int chipHeight = 80;

    NitfSegmentsFlow nitfSegmentsFlow = createGenericNitfSegmentFlow(originalWidth, originalHeight);

    BufferedImage chipImage =
        new BufferedImage(chipWidth, chipHeight, BufferedImage.TYPE_BYTE_GRAY);

    BinaryContent binaryContent =
        catalogOutputAdapter.getNitfBinaryContent(chipImage, nitfSegmentsFlow, 0, 0);

    NitfSegmentsFlow chipNitfSegmentFlow =
        new NitfParserInputFlowImpl().inputStream(binaryContent.getInputStream()).allData();

    chipNitfSegmentFlow.forEachImageSegment(
        imageSegment1 -> {
          assertThat(imageSegment1.getPixelValueType(), is(PixelValueType.INTEGER));
          assertThat(imageSegment1.getImageRepresentation(), is(ImageRepresentation.MONOCHROME));
          assertThat(imageSegment1.getActualBitsPerPixelPerBand(), is(8));
          assertThat(imageSegment1.getNumberOfBitsPerPixelPerBand(), is(8));
          assertThat(imageSegment1.getImageBand(1).getImageRepresentation(), is("M"));
        });
  }

  @Test
  public void testByteIndexed()
      throws MimeTypeParseException, NitfFormatException, IOException, NoSuchMethodException,
          IllegalAccessException, InvocationTargetException, InstantiationException {

    int originalWidth = 200;
    int originalHeight = 100;

    int chipWidth = 160;
    int chipHeight = 80;

    NitfSegmentsFlow nitfSegmentsFlow = createGenericNitfSegmentFlow(originalWidth, originalHeight);

    BufferedImage chipImage =
        new BufferedImage(chipWidth, chipHeight, BufferedImage.TYPE_BYTE_INDEXED);

    BinaryContent binaryContent =
        catalogOutputAdapter.getNitfBinaryContent(chipImage, nitfSegmentsFlow, 0, 0);

    NitfSegmentsFlow chipNitfSegmentFlow =
        new NitfParserInputFlowImpl().inputStream(binaryContent.getInputStream()).allData();

    chipNitfSegmentFlow.forEachImageSegment(
        imageSegment1 -> {
          assertThat(imageSegment1.getPixelValueType(), is(PixelValueType.INTEGER));
          assertThat(imageSegment1.getImageRepresentation(), is(ImageRepresentation.RGBLUT));
          assertThat(imageSegment1.getActualBitsPerPixelPerBand(), is(8));
          assertThat(imageSegment1.getNumberOfBitsPerPixelPerBand(), is(8));
          assertThat(imageSegment1.getImageBand(1).getImageRepresentation(), is("LU"));
        });
  }

  @Test
  public void testIntARGB()
      throws MimeTypeParseException, NitfFormatException, IOException, NoSuchMethodException,
          IllegalAccessException, InvocationTargetException, InstantiationException {

    int originalWidth = 200;
    int originalHeight = 100;

    int chipWidth = 160;
    int chipHeight = 80;

    NitfSegmentsFlow nitfSegmentsFlow = createGenericNitfSegmentFlow(originalWidth, originalHeight);

    BufferedImage chipImage = new BufferedImage(chipWidth, chipHeight, BufferedImage.TYPE_INT_ARGB);

    BinaryContent binaryContent =
        catalogOutputAdapter.getNitfBinaryContent(chipImage, nitfSegmentsFlow, 0, 0);

    NitfSegmentsFlow chipNitfSegmentFlow =
        new NitfParserInputFlowImpl().inputStream(binaryContent.getInputStream()).allData();

    chipNitfSegmentFlow.forEachImageSegment(
        imageSegment1 -> {
          assertThat(imageSegment1.getPixelValueType(), is(PixelValueType.INTEGER));
          assertThat(imageSegment1.getImageRepresentation(), is(ImageRepresentation.RGBTRUECOLOUR));
          assertThat(imageSegment1.getActualBitsPerPixelPerBand(), is(8));
          assertThat(imageSegment1.getNumberOfBitsPerPixelPerBand(), is(8));
          assertThat(imageSegment1.getImageBand(1).getImageRepresentation(), is("R"));
          assertThat(imageSegment1.getImageBand(2).getImageRepresentation(), is("G"));
          assertThat(imageSegment1.getImageBand(3).getImageRepresentation(), is("B"));
        });
  }

  @Test
  public void testGetNitfBinaryContent()
      throws MimeTypeParseException, NitfFormatException, IOException, NoSuchMethodException,
          IllegalAccessException, InvocationTargetException, InstantiationException,
          InterruptedException {

    // 200000N0200000E 200000N0400000E
    // 000000N0200000E 000000N0400000E

    int originalWidth = 200;
    int originalHeight = 100;

    int chipX = 20;
    int chipY = 10;
    int chipWidth = 160;
    int chipHeight = 80;

    // assume original image is 200x100, chip is 20x10/160x80
    BufferedImage chipImage =
        new BufferedImage(chipWidth, chipHeight, BufferedImage.TYPE_3BYTE_BGR);

    FileSecurityMetadata fileSecurityMetadata = createFileSecurityMetadata();

    DateTime dateTime = DateTimeImpl.getNitfDateTimeForNow();

    NitfHeader nitfHeader = getNitfHeader(fileSecurityMetadata, dateTime);

    ImageCoordinates imageCoordinates = getImageCoordinates();

    SecurityMetadata imageSecurityMetadata = getImageSecurityMetadata();

    ImageSegment imageSegment =
        getImageSegment(
            originalWidth, originalHeight, dateTime, imageCoordinates, imageSecurityMetadata);

    DataSource dataSource = getDataSource(nitfHeader, Collections.singletonList(imageSegment));

    Constructor<NitfSegmentsFlowImpl> constructor;
    constructor =
        NitfSegmentsFlowImpl.class.getDeclaredConstructor(DataSource.class, Runnable.class);
    constructor.setAccessible(true);
    NitfSegmentsFlow nitfSegmentsFlow = constructor.newInstance(dataSource, (Runnable) () -> {});

    BinaryContent binaryContent =
        catalogOutputAdapter.getNitfBinaryContent(chipImage, nitfSegmentsFlow, chipX, chipY);

    NitfSegmentsFlow chipNitfSegmentFlow =
        new NitfParserInputFlowImpl().inputStream(binaryContent.getInputStream()).allData();

    assertThat(chipNitfSegmentFlow, notNullValue());

    chipNitfSegmentFlow.fileHeader(
        nh -> {
          assertThat(nh.getFileType(), is(FileType.NITF_TWO_ONE));
          assertThat(nh.getFileTitle(), is("FileTitle"));
          assertThat(nh.getStandardType(), is("BF01"));
          assertThat(nh.getOriginatingStationId(), is("U21SOO90"));
          assertThat(nh.getFileBackgroundColour().getRed(), is((byte) 0));
          assertThat(nh.getFileBackgroundColour().getGreen(), is((byte) 0));
          assertThat(nh.getFileBackgroundColour().getBlue(), is((byte) 0));
          assertThat(nh.getFileDateTime(), notNullValue());
          assertThat(nh.getOriginatorsName(), is("W.TEMPEL"));
          assertThat(nh.getOriginatorsPhoneNumber(), is("44 1480 84 5611"));

          assertThat(
              nh.getFileSecurityMetadata().getSecurityClassification(),
              is(SecurityClassification.UNCLASSIFIED));
          assertThat(nh.getFileSecurityMetadata().getSecurityClassificationSystem(), is("AB"));
          assertThat(nh.getFileSecurityMetadata().getCodewords(), is("ABCDEFGHIJK"));
          assertThat(nh.getFileSecurityMetadata().getControlAndHandling(), is("AB"));
          assertThat(
              nh.getFileSecurityMetadata().getReleaseInstructions(), is("01234567890123456789"));
          assertThat(nh.getFileSecurityMetadata().getDeclassificationType(), is("DD"));
          assertThat(nh.getFileSecurityMetadata().getDeclassificationDate(), is("20160101"));
          assertThat(nh.getFileSecurityMetadata().getDeclassificationExemption(), is("abcd"));
          assertThat(nh.getFileSecurityMetadata().getDowngrade(), is("S"));
          assertThat(nh.getFileSecurityMetadata().getDowngradeDate(), is("20160202"));
          assertThat(nh.getFileSecurityMetadata().getClassificationText(), is("classtext"));
          assertThat(nh.getFileSecurityMetadata().getClassificationAuthorityType(), is("O"));
          assertThat(nh.getFileSecurityMetadata().getClassificationAuthority(), is("MyAuthority"));
          assertThat(nh.getFileSecurityMetadata().getClassificationReason(), is("A"));
          assertThat(nh.getFileSecurityMetadata().getSecuritySourceDate(), is("20160303"));
          assertThat(
              nh.getFileSecurityMetadata().getSecurityControlNumber(), is("012345678901234"));
          assertThat(nh.getFileSecurityMetadata().getFileCopyNumber(), is("99999"));
          assertThat(nh.getFileSecurityMetadata().getFileNumberOfCopies(), is("99999"));
        });

    chipNitfSegmentFlow.forEachImageSegment(
        imageSegment1 -> {
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate00().getLongitude(),
              closeTo(22, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate00().getLatitude(),
              closeTo(18, 0.01));

          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate0MaxCol().getLongitude(),
              closeTo(38, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate0MaxCol().getLatitude(),
              closeTo(18, 0.01));

          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRowMaxCol().getLongitude(),
              closeTo(38, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRowMaxCol().getLatitude(),
              closeTo(2, 0.01));

          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRow0().getLongitude(),
              closeTo(22, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRow0().getLatitude(),
              closeTo(2, 0.01));

          assertThat(imageSegment1.getNumberOfColumns(), is((long) chipWidth));
          assertThat(imageSegment1.getNumberOfRows(), is((long) chipHeight));
          assertThat(imageSegment1.getImageCategory(), is(ImageCategory.UNKNOWN));
          assertThat(imageSegment1.getImageComments(), is(Collections.singletonList("MyComment")));
          assertThat(imageSegment1.getIdentifier(), is("0123456789"));
          assertThat(imageSegment1.getImageIdentifier2(), is("abc"));
          assertThat(imageSegment1.getImageMagnification(), is("1.0 "));
          try {
            assertThat(
                imageSegment1.getImageTargetId().textValue(),
                is(new TargetIdImpl("                 ").textValue()));
          } catch (NitfFormatException e) {
            fail(e.getMessage());
          }
          assertThat(imageSegment1.getImageSource(), is(""));
          assertThat(
              imageSegment1.getImageDateTime().getSourceString(), is(dateTime.getSourceString()));
          assertThat(imageSegment1.getImageRepresentation(), is(ImageRepresentation.RGBTRUECOLOUR));
        });
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test
  public void testGetNitfBinaryContentBlockedChip()
      throws MimeTypeParseException, NitfFormatException, IOException, NoSuchMethodException,
          IllegalAccessException, InvocationTargetException, InstantiationException,
          InterruptedException {

    // 200000N0200000E 200000N0400000E
    // 000000N0200000E 000000N0400000E

    int originalWidth = 2048;
    int originalHeight = 2048;

    int chipX = 0;
    int chipY = 0;
    int chipWidth = 2048;
    int chipHeight = 2048;

    BufferedImage chipImage =
        new BufferedImage(chipWidth, chipHeight, BufferedImage.TYPE_3BYTE_BGR);

    FileSecurityMetadata fileSecurityMetadata = createFileSecurityMetadata();

    DateTime dateTime = DateTimeImpl.getNitfDateTimeForNow();

    DataSource dataSource = mock(DataSource.class);
    NitfHeader nitfHeader = getNitfHeader(fileSecurityMetadata, dateTime);

    ImageCoordinates imageCoordinates = getImageCoordinates();

    SecurityMetadata imageSecurityMetadata = getImageSecurityMetadata();

    ImageSegment imageSegment =
        getImageSegment(
            originalWidth, originalHeight, dateTime, imageCoordinates, imageSecurityMetadata);

    when(dataSource.getNitfHeader()).thenReturn(nitfHeader);
    when(dataSource.getImageSegments()).thenReturn(Collections.singletonList(imageSegment));

    Constructor<NitfSegmentsFlowImpl> constructor;
    constructor =
        NitfSegmentsFlowImpl.class.getDeclaredConstructor(DataSource.class, Runnable.class);
    constructor.setAccessible(true);
    NitfSegmentsFlow nitfSegmentsFlow = constructor.newInstance(dataSource, (Runnable) () -> {});

    BinaryContent binaryContent =
        catalogOutputAdapter.getNitfBinaryContent(chipImage, nitfSegmentsFlow, chipX, chipY);

    NitfSegmentsFlow chipNitfSegmentFlow =
        new NitfParserInputFlowImpl().inputStream(binaryContent.getInputStream()).allData();

    assertThat(chipNitfSegmentFlow, notNullValue());

    chipNitfSegmentFlow.fileHeader(
        nh -> {
          assertThat(nh.getFileType(), is(FileType.NITF_TWO_ONE));
          assertThat(nh.getFileTitle(), is("FileTitle"));
          assertThat(nh.getStandardType(), is("BF01"));
          assertThat(nh.getOriginatingStationId(), is("U21SOO90"));
          assertThat(nh.getFileBackgroundColour().getRed(), is((byte) 0));
          assertThat(nh.getFileBackgroundColour().getGreen(), is((byte) 0));
          assertThat(nh.getFileBackgroundColour().getBlue(), is((byte) 0));
          assertThat(nh.getFileDateTime(), notNullValue());
          assertThat(nh.getOriginatorsName(), is("W.TEMPEL"));
          assertThat(nh.getOriginatorsPhoneNumber(), is("44 1480 84 5611"));

          assertThat(
              nh.getFileSecurityMetadata().getSecurityClassification(),
              is(SecurityClassification.UNCLASSIFIED));
          assertThat(nh.getFileSecurityMetadata().getSecurityClassificationSystem(), is("AB"));
          assertThat(nh.getFileSecurityMetadata().getCodewords(), is("ABCDEFGHIJK"));
          assertThat(nh.getFileSecurityMetadata().getControlAndHandling(), is("AB"));
          assertThat(
              nh.getFileSecurityMetadata().getReleaseInstructions(), is("01234567890123456789"));
          assertThat(nh.getFileSecurityMetadata().getDeclassificationType(), is("DD"));
          assertThat(nh.getFileSecurityMetadata().getDeclassificationDate(), is("20160101"));
          assertThat(nh.getFileSecurityMetadata().getDeclassificationExemption(), is("abcd"));
          assertThat(nh.getFileSecurityMetadata().getDowngrade(), is("S"));
          assertThat(nh.getFileSecurityMetadata().getDowngradeDate(), is("20160202"));
          assertThat(nh.getFileSecurityMetadata().getClassificationText(), is("classtext"));
          assertThat(nh.getFileSecurityMetadata().getClassificationAuthorityType(), is("O"));
          assertThat(nh.getFileSecurityMetadata().getClassificationAuthority(), is("MyAuthority"));
          assertThat(nh.getFileSecurityMetadata().getClassificationReason(), is("A"));
          assertThat(nh.getFileSecurityMetadata().getSecuritySourceDate(), is("20160303"));
          assertThat(
              nh.getFileSecurityMetadata().getSecurityControlNumber(), is("012345678901234"));
          assertThat(nh.getFileSecurityMetadata().getFileCopyNumber(), is("99999"));
          assertThat(nh.getFileSecurityMetadata().getFileNumberOfCopies(), is("99999"));
        });

    chipNitfSegmentFlow.forEachImageSegment(
        imageSegment1 -> {
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate00().getLongitude(),
              closeTo(20, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate00().getLatitude(),
              closeTo(20, 0.01));

          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate0MaxCol().getLongitude(),
              closeTo(40, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinate0MaxCol().getLatitude(),
              closeTo(20, 0.01));

          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRowMaxCol().getLongitude(),
              closeTo(40, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRowMaxCol().getLatitude(),
              closeTo(0, 0.01));

          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRow0().getLongitude(),
              closeTo(20, 0.01));
          assertThat(
              imageSegment1.getImageCoordinates().getCoordinateMaxRow0().getLatitude(),
              closeTo(0, 0.01));

          assertThat(imageSegment1.getNumberOfColumns(), is((long) chipWidth));
          assertThat(imageSegment1.getNumberOfRows(), is((long) chipHeight));
          assertThat(imageSegment1.getImageCategory(), is(ImageCategory.UNKNOWN));
          assertThat(imageSegment1.getImageComments(), is(Collections.singletonList("MyComment")));
          assertThat(imageSegment1.getIdentifier(), is("0123456789"));
          assertThat(imageSegment1.getImageIdentifier2(), is("abc"));
          assertThat(imageSegment1.getImageMagnification(), is("1.0 "));
          try {
            assertThat(
                imageSegment1.getImageTargetId().textValue(),
                is(new TargetIdImpl("                 ").textValue()));
          } catch (NitfFormatException e) {
            fail(e.getMessage());
          }
          assertThat(imageSegment1.getImageSource(), is(""));
          assertThat(
              imageSegment1.getImageDateTime().getSourceString(), is(dateTime.getSourceString()));
          assertThat(imageSegment1.getImageRepresentation(), is(ImageRepresentation.RGBTRUECOLOUR));
        });
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test
  public void testCreateIchipb() throws NitfFormatException {
    BufferedImage bufferedImage = mock(BufferedImage.class);
    int originalWidth = 200;
    int originalHeight = 100;
    when(bufferedImage.getWidth()).thenReturn(originalWidth);
    when(bufferedImage.getHeight()).thenReturn(originalHeight);
    int chipX = 20;
    int chipY = 10;
    Tre tre = catalogOutputAdapter.createIchipb(bufferedImage, chipX, chipY, 160, 80);

    assertTreEntry(tre, "XFRM_FLAG", TWO_DIGIT_REGEX, UINT);
    assertTreEntry(tre, "SCALE_FACTOR", Pattern.compile("^[0-9]{4}\\.[0-9]{5}$"), REAL);
    assertTreEntry(tre, "ANAMRPH_CORR", TWO_DIGIT_REGEX, UINT);
    assertTreEntry(tre, "SCANBLK_NUM", TWO_DIGIT_REGEX, UINT);
    assertTreEntry(tre, "OP_ROW_11", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_COL_11", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_ROW_12", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_COL_12", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_ROW_21", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_COL_21", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_ROW_22", COORD_REGEX, REAL);
    assertTreEntry(tre, "OP_COL_22", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_ROW_11", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_COL_11", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_ROW_12", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_COL_12", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_ROW_21", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_COL_21", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_ROW_22", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_COL_22", COORD_REGEX, REAL);
    assertTreEntry(tre, "FI_ROW", EIGHT_DIGIT_REGEX, UINT);
    assertTreEntry(tre, "FI_COL", EIGHT_DIGIT_REGEX, UINT);

    assertThat(tre.getDoubleValue("OP_ROW_11"), closeTo(0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_COL_11"), closeTo(0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_ROW_12"), closeTo(0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_COL_12"), closeTo(originalWidth - 0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_ROW_21"), closeTo(originalHeight - 0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_COL_21"), closeTo(0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_ROW_22"), closeTo(originalHeight - 0.5, 0.01));
    assertThat(tre.getDoubleValue("OP_COL_22"), closeTo(originalWidth - 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_ROW_11"), closeTo(chipY + 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_COL_11"), closeTo(chipX + 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_ROW_12"), closeTo(chipY + 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_COL_12"), closeTo(originalWidth - chipX - 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_ROW_21"), closeTo(originalHeight - chipY - 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_COL_21"), closeTo(chipX + 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_ROW_22"), closeTo(originalHeight - chipY - 0.5, 0.01));
    assertThat(tre.getDoubleValue("FI_COL_22"), closeTo(originalWidth - chipX - 0.5, 0.01));
  }

  /** Test that if the TFBOS throws an exception, the TFBOS is closed */
  @Test
  public void testGetNitfSegmentsFlowTFBOSThrows() throws IOException, NitfFormatException {

    TemporaryFileBackedOutputStream tfbos = mock(TemporaryFileBackedOutputStream.class);
    doThrow(IOException.class).when(tfbos).write(anyObject(), anyInt(), anyInt());

    catalogOutputAdapter =
        new CatalogOutputAdapter(new Semaphore(3, true)) {
          @Override
          protected TemporaryFileBackedOutputStream createTemporaryFileBackedOutputStream() {
            return tfbos;
          }
        };

    try {
      catalogOutputAdapter.getNitfSegmentsFlow(new ByteArrayInputStream(new byte[] {(byte) 0}));
      fail("expected an exception, shouldn't reach this line");
    } catch (IOException e) {
      assertThat(e, notNullValue());
    }

    verify(tfbos).close();
  }

  @Test
  public void testFormatToDMS() {
    assertThat(catalogOutputAdapter.formatToDMS(30, 30), is("300000N0300000E"));
    assertThat(catalogOutputAdapter.formatToDMS(-30, 30), is("300000S0300000E"));
    assertThat(catalogOutputAdapter.formatToDMS(30, -30), is("300000N0300000W"));
    assertThat(catalogOutputAdapter.formatToDMS(-30, -30), is("300000S0300000W"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetNitfSegmentsFlowTFBOSThrowsDuringRead()
      throws IOException, NitfFormatException {

    InputStream inputStream = mock(InputStream.class);
    when(inputStream.read()).thenThrow(IOException.class);
    when(inputStream.read(anyObject())).thenThrow(IOException.class);
    when(inputStream.read(anyObject(), anyInt(), anyInt())).thenThrow(IOException.class);

    ByteSource byteSource = mock(ByteSource.class);
    when(byteSource.openBufferedStream()).thenReturn(inputStream);

    TemporaryFileBackedOutputStream tfbos = mock(TemporaryFileBackedOutputStream.class);
    when(tfbos.asByteSource()).thenReturn(byteSource);

    catalogOutputAdapter =
        new CatalogOutputAdapter(new Semaphore(3, true)) {
          @Override
          protected TemporaryFileBackedOutputStream createTemporaryFileBackedOutputStream() {
            return tfbos;
          }
        };

    try {
      catalogOutputAdapter.getNitfSegmentsFlow(new ByteArrayInputStream(new byte[] {(byte) 0}));
      fail("expected an exception, shouldn't reach this line");
    } catch (IOException | NitfFormatException e) {
      assertThat(e, notNullValue());
    }

    verify(tfbos).close();
    verify(inputStream).close();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetNitfSegmentsFlowTFBOSAsBytesourceThrows()
      throws IOException, NitfFormatException {

    ByteSource byteSource = mock(ByteSource.class);
    when(byteSource.openBufferedStream()).thenThrow(IOException.class);

    TemporaryFileBackedOutputStream tfbos = mock(TemporaryFileBackedOutputStream.class);
    when(tfbos.asByteSource()).thenReturn(byteSource);

    catalogOutputAdapter =
        new CatalogOutputAdapter(new Semaphore(3, true)) {
          @Override
          protected TemporaryFileBackedOutputStream createTemporaryFileBackedOutputStream() {
            return tfbos;
          }
        };

    try {
      catalogOutputAdapter.getNitfSegmentsFlow(new ByteArrayInputStream(new byte[] {(byte) 0}));
      fail("expected an exception, shouldn't reach this line");
    } catch (IOException | NitfFormatException e) {
      assertThat(e, notNullValue());
    }

    verify(tfbos).close();
  }

  private ImageCoordinates getImageCoordinates() throws NitfFormatException {
    return new ImageCoordinatesImpl(
        new ImageCoordinatePair[] {
          icp("200000N0200000E"),
          icp("200000N0400000E"),
          icp("000000N0400000E"),
          icp("000000N0200000E")
        });
  }

  private ImageSegment getImageSegment(
      long originalWidth,
      long originalHeight,
      DateTime dateTime,
      ImageCoordinates imageCoordinates,
      SecurityMetadata imageSecurityMetadata)
      throws NitfFormatException {
    ImageSegment imageSegment = mock(ImageSegment.class);

    when(imageSegment.getImageCoordinates()).thenReturn(imageCoordinates);
    when(imageSegment.getNumberOfColumns()).thenReturn(originalWidth);
    when(imageSegment.getNumberOfRows()).thenReturn(originalHeight);
    when(imageSegment.getImageCategory()).thenReturn(ImageCategory.UNKNOWN);
    when(imageSegment.getImageComments()).thenReturn(Collections.singletonList("MyComment"));
    when(imageSegment.getIdentifier()).thenReturn("0123456789");
    when(imageSegment.getImageIdentifier2()).thenReturn("abc");
    when(imageSegment.getImageMagnification()).thenReturn("1.0 ");
    when(imageSegment.getImageTargetId()).thenReturn(new TargetIdImpl("                 "));
    when(imageSegment.getImageSource()).thenReturn("");
    when(imageSegment.getImageDateTime()).thenReturn(dateTime);
    when(imageSegment.getImageRepresentation()).thenReturn(ImageRepresentation.UNKNOWN);
    when(imageSegment.getSecurityMetadata()).thenReturn(imageSecurityMetadata);
    when(imageSegment.getTREsRawStructure()).thenReturn(new TreCollectionImpl());
    return imageSegment;
  }

  private DataSource getDataSource(NitfHeader nitfHeader, List<ImageSegment> value) {
    DataSource dataSource = mock(DataSource.class);
    when(dataSource.getNitfHeader()).thenReturn(nitfHeader);
    when(dataSource.getImageSegments()).thenReturn(value);
    return dataSource;
  }

  private SecurityMetadata getImageSecurityMetadata() {
    SecurityMetadata imageSecurityMetadata = mock(SecurityMetadata.class);

    when(imageSecurityMetadata.getFileType()).thenReturn(FileType.NITF_TWO_ONE);
    when(imageSecurityMetadata.getSecurityClassification())
        .thenReturn(SecurityClassification.UNCLASSIFIED);
    when(imageSecurityMetadata.getSecurityClassificationSystem()).thenReturn("");
    when(imageSecurityMetadata.getCodewords()).thenReturn("");
    when(imageSecurityMetadata.getControlAndHandling()).thenReturn("");
    when(imageSecurityMetadata.getReleaseInstructions()).thenReturn("");
    when(imageSecurityMetadata.getDeclassificationType()).thenReturn("");
    when(imageSecurityMetadata.getDeclassificationDate()).thenReturn("");
    when(imageSecurityMetadata.getDeclassificationExemption()).thenReturn("");
    when(imageSecurityMetadata.getDowngrade()).thenReturn("");
    when(imageSecurityMetadata.getDowngradeDate()).thenReturn("");
    when(imageSecurityMetadata.getDowngradeDateOrSpecialCase()).thenReturn("");
    when(imageSecurityMetadata.getDowngradeEvent()).thenReturn("");
    when(imageSecurityMetadata.getClassificationText()).thenReturn("");
    when(imageSecurityMetadata.getClassificationAuthorityType()).thenReturn("");
    when(imageSecurityMetadata.getClassificationAuthority()).thenReturn("");
    when(imageSecurityMetadata.getClassificationReason()).thenReturn("");
    when(imageSecurityMetadata.getSecuritySourceDate()).thenReturn("");
    when(imageSecurityMetadata.getSecurityControlNumber()).thenReturn("");
    when(imageSecurityMetadata.hasDowngradeMagicValue()).thenReturn(false);
    when(imageSecurityMetadata.getSerialisedLength()).thenReturn(0L);
    return imageSecurityMetadata;
  }

  private NitfHeader getNitfHeader(FileSecurityMetadata fileSecurityMetadata, DateTime dateTime) {
    NitfHeader nitfHeader = mock(NitfHeader.class);
    when(nitfHeader.getFileTitle()).thenReturn("FileTitle");
    when(nitfHeader.getStandardType()).thenReturn("BF01");
    when(nitfHeader.getOriginatingStationId()).thenReturn("U21SOO90");
    when(nitfHeader.getFileBackgroundColour())
        .thenReturn(new RGBColourImpl((byte) 0, (byte) 0, (byte) 0));
    when(nitfHeader.getFileDateTime()).thenReturn(dateTime);
    when(nitfHeader.getFileSecurityMetadata()).thenReturn(fileSecurityMetadata);
    when(nitfHeader.getOriginatorsName()).thenReturn("W.TEMPEL");
    when(nitfHeader.getOriginatorsPhoneNumber()).thenReturn("44 1480 84 5611");
    when(nitfHeader.getTREsRawStructure()).thenReturn(new TreCollectionImpl());
    return nitfHeader;
  }

  private FileSecurityMetadata createFileSecurityMetadata() {
    FileSecurityMetadata fileSecurityMetadata = mock(FileSecurityMetadata.class);

    when(fileSecurityMetadata.getSecurityClassification())
        .thenReturn(SecurityClassification.UNCLASSIFIED);
    when(fileSecurityMetadata.getSecurityClassificationSystem()).thenReturn("AB");
    when(fileSecurityMetadata.getCodewords()).thenReturn("ABCDEFGHIJK");
    when(fileSecurityMetadata.getControlAndHandling()).thenReturn("AB");
    when(fileSecurityMetadata.getReleaseInstructions()).thenReturn("01234567890123456789");
    when(fileSecurityMetadata.getDeclassificationType()).thenReturn("DD");
    when(fileSecurityMetadata.getDeclassificationDate()).thenReturn("20160101");
    when(fileSecurityMetadata.getDeclassificationExemption()).thenReturn("abcd");
    when(fileSecurityMetadata.getDowngrade()).thenReturn("S");
    when(fileSecurityMetadata.getDowngradeDate()).thenReturn("20160202");
    when(fileSecurityMetadata.getClassificationText()).thenReturn("classtext");
    when(fileSecurityMetadata.getClassificationAuthorityType()).thenReturn("O");
    when(fileSecurityMetadata.getClassificationAuthority()).thenReturn("MyAuthority");
    when(fileSecurityMetadata.getClassificationReason()).thenReturn("A");
    when(fileSecurityMetadata.getSecuritySourceDate()).thenReturn("20160303");
    when(fileSecurityMetadata.getSecurityControlNumber()).thenReturn("012345678901234");
    when(fileSecurityMetadata.getFileCopyNumber()).thenReturn("99999");
    when(fileSecurityMetadata.getFileNumberOfCopies()).thenReturn("99999");
    return fileSecurityMetadata;
  }

  private void assertTreEntry(Tre tre, String name, Pattern re, String dataType) {
    TreEntry treEntry = findAndAssertTreEntry(tre, name);
    assertThatRe(treEntry, re);
    assertThatDT(treEntry, dataType);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private TreEntry findAndAssertTreEntry(Tre tre, String name) {
    Optional<TreEntry> treEntry = findTreEntry(tre, name);
    assertThat(treEntry.isPresent(), is(true));
    return treEntry.get();
  }

  private void assertThatRe(TreEntry treEntry, Pattern re) {
    assertThat(treEntry.getFieldValue(), PrecompiledPatternMatcher.matchesPattern(re));
  }

  private void assertThatDT(TreEntry treEntry, String dataType) {
    assertThat(treEntry.getDataType(), is(dataType));
  }

  private Optional<TreEntry> findTreEntry(Tre tre, String name) {
    return tre.getEntries().stream()
        .filter(treEntry -> treEntry.getName().equals(name))
        .findFirst();
  }

  private InputStream getInputStream(String filename) {
    assertNotNull("Test file missing", getClass().getResource(filename));
    return getClass().getResourceAsStream(filename);
  }

  private ImageCoordinatePair icp(String value) throws NitfFormatException {
    ImageCoordinatePairImpl icp = new ImageCoordinatePairImpl();
    icp.setFromDMS(value);
    return icp;
  }

  private NitfSegmentsFlow createGenericNitfSegmentFlow(int originalWidth, int originalHeight)
      throws NitfFormatException, NoSuchMethodException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    DateTime dateTime = DateTimeImpl.getNitfDateTimeForNow();

    FileSecurityMetadata fileSecurityMetadata = createFileSecurityMetadata();

    NitfHeader nitfHeader = getNitfHeader(fileSecurityMetadata, dateTime);

    ImageCoordinates imageCoordinates = getImageCoordinates();

    SecurityMetadata imageSecurityMetadata = getImageSecurityMetadata();

    ImageSegment imageSegment =
        getImageSegment(
            originalWidth, originalHeight, dateTime, imageCoordinates, imageSecurityMetadata);

    DataSource dataSource = getDataSource(nitfHeader, Collections.singletonList(imageSegment));

    Constructor<NitfSegmentsFlowImpl> constructor;
    constructor =
        NitfSegmentsFlowImpl.class.getDeclaredConstructor(DataSource.class, Runnable.class);
    constructor.setAccessible(true);
    return constructor.newInstance(dataSource, (Runnable) () -> {});
  }

  private static class PrecompiledPatternMatcher extends TypeSafeMatcher<String> {

    private final Pattern pattern;

    PrecompiledPatternMatcher(Pattern pattern) {
      this.pattern = pattern;
    }

    @Factory
    static Matcher<String> matchesPattern(Pattern pattern) {
      return new PrecompiledPatternMatcher(pattern);
    }

    @Override
    protected boolean matchesSafely(String s) {
      return pattern.matcher(s).matches();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("a string matching the pattern '" + pattern + "'");
    }
  }
}
