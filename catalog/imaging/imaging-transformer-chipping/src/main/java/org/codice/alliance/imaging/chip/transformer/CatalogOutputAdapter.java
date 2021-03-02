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

import static org.apache.commons.lang.Validate.notNull;

import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.impl.BinaryContentImpl;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;
import ddf.catalog.transform.CatalogTransformerException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.imaging.chip.service.impl.CoordinateConverter;
import org.codice.ddf.platform.util.TemporaryFileBackedOutputStream;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.TaggedRecordExtensionHandler;
import org.codice.imaging.nitf.core.common.impl.DateTimeImpl;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.header.impl.NitfHeaderFactory;
import org.codice.imaging.nitf.core.image.ImageBand;
import org.codice.imaging.nitf.core.image.ImageCoordinatePair;
import org.codice.imaging.nitf.core.image.ImageCoordinates;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.ImageRepresentation;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.core.image.PixelJustification;
import org.codice.imaging.nitf.core.image.PixelValueType;
import org.codice.imaging.nitf.core.image.impl.ImageBandImpl;
import org.codice.imaging.nitf.core.image.impl.ImageCoordinatePairImpl;
import org.codice.imaging.nitf.core.image.impl.ImageCoordinatesImpl;
import org.codice.imaging.nitf.core.image.impl.ImageSegmentFactory;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreCollection;
import org.codice.imaging.nitf.core.tre.TreSource;
import org.codice.imaging.nitf.core.tre.impl.TreEntryImpl;
import org.codice.imaging.nitf.core.tre.impl.TreFactory;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.codice.imaging.nitf.fluent.impl.NitfCreationFlowImpl;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs various conversion functions required to wire the CatalogFramework interface to the
 * MetacardTransformer interface.
 */
public class CatalogOutputAdapter {

  private static final String IMAGE_JPG = "image/jpeg";

  private static final String IMAGE_NITF = "image/nitf";

  private static final String INTERRUPT_IMAGE_PROCESSING =
      "Interrupt received while doing image processing.";

  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogOutputAdapter.class);

  private static final int DEFAULT_DISPLAY_LEVEL = 1;

  private static final String UINT = "UINT";

  private static final String REAL = "real";

  private static final int BLOCK_WIDTH = 1024;

  private static final int BLOCK_HEIGHT = 1024;

  private Semaphore lock;

  /**
   * These are the SDEs that should be copied to the NITF chip. This list was assembled from
   * information gathered from ASDE, CSDE, and GEOSDE.
   */
  private static final Set<String> SDE_PATTERNS =
      new HashSet<>(
          Arrays.asList(
              "ACFTA", "ACFTB", "AIMIDA", "AIMIDB", "BANDSA", "BANDSB", "BLOCKA", "EXOPTA",
              "EXPLTA", "EXPLTB", "MENSRA", "MENSRB", "MPDSRA", "MSTGTA", "MTIRPA", "MTIRPB",
              "PATCHA", "PATCHB", "RPC00B", "RPC00A", "SENSRA", "SENSRB", "SECTGA", "STREOB",
              "STDIDC", "USE00A", "TBR001", "CSCCGA", "CSCRNA", "CSDIDA", "CSEPHA", "CSEXRA",
              "CSPROA", "CSSFAA", "GEOPS.", "PRJPS.", "GRDPS.", "GEOLO.", "MAPLO.", "REGPT.",
              "BNDPL.", "ACCPO.", "ACCHZ.", "ACCVT.", "SOURC.", "SNSPS.", "FACCB."));

  /**
   * This is the compiled regex pattern that will match any SDE that should be copied to the NITF
   * chip.
   */
  private static final Pattern SDE_PATTERN =
      Pattern.compile("^" + StringUtils.join(SDE_PATTERNS, "|") + "$");

  private static final String JPG = "jpg";

  public CatalogOutputAdapter(Semaphore lock) {
    this.lock = lock;
  }

  /**
   * @param resourceResponse a ResourceResponse object returned by CatalogFramework.
   * @return the requested BufferedImage.
   * @throws IOException when there's a problem reading the image from the ResourceResponse
   *     InputStream.
   */
  @SuppressWarnings("WeakerAccess")
  public BufferedImage getImage(ResourceResponse resourceResponse) throws IOException {
    validateArgument(resourceResponse, "resourceResponse");
    validateArgument(resourceResponse.getResource(), "resourceResponse.resource");
    BufferedImage image = null;
    try (InputStream resourceStream = resourceResponse.getResource().getInputStream()) {
      validateObjectState(resourceStream, "resourceResponse.resource.inputStream");

      Resource resource = resourceResponse.getResource();
      try (InputStream inputStream = resource.getInputStream();
          BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
        lock.acquire();
        try {
          image = ImageIO.read(bufferedInputStream);
        } finally {
          lock.release();
        }
      } catch (InterruptedException e) {
        LOGGER.debug(INTERRUPT_IMAGE_PROCESSING, e);
        Thread.currentThread().interrupt();
      }
    }
    return image;
  }

  /**
   * @param image the BufferedImage to be converted.
   * @return a BinaryContent object containing the image data.
   * @throws IOException when the BufferedImage can't be written to temporary in-memory space.
   * @throws MimeTypeParseException thrown if the mime type is invalid
   */
  @SuppressWarnings("WeakerAccess")
  public BinaryContent getBinaryContent(BufferedImage image)
      throws IOException, MimeTypeParseException {
    validateArgument(image, "image");
    BinaryContent binaryContent = null;
    try {
      lock.acquire();
      try {
        BufferedImage rgbImage =
            new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D graphics = rgbImage.createGraphics();

        graphics.drawImage(image, 0, 0, null);

        InputStream fis = new ByteArrayInputStream(createJpg(rgbImage));
        binaryContent = new BinaryContentImpl(fis, new MimeType(IMAGE_JPG));
      } finally {
        lock.release();
      }
    } catch (InterruptedException e) {
      LOGGER.debug(INTERRUPT_IMAGE_PROCESSING, e);
      Thread.currentThread().interrupt();
    }
    return binaryContent;
  }

  private byte[] createJpg(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      try (ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(os)) {
        ImageIO.write(image, JPG, imageOutputStream);
      }
      return os.toByteArray();
    }
  }

  /**
   * @param resourceResponse resource response from the catalog framework
   * @return the nitf segments
   */
  @SuppressWarnings("unused")
  public NitfSegmentsFlow getNitfSegmentsFlow(ResourceResponse resourceResponse)
      throws NitfFormatException, IOException {
    notNull(resourceResponse, "resourceResponse must be non-null");
    notNull(resourceResponse.getResource(), "resourceResponse resource must be non-null");
    NitfSegmentsFlow nitfSegmentsFlow = null;
    try (final InputStream inputStream = resourceResponse.getResource().getInputStream()) {
      lock.acquire();
      try {
        nitfSegmentsFlow = getNitfSegmentsFlow(inputStream);
      } finally {
        lock.release();
      }
    } catch (InterruptedException e) {
      LOGGER.debug(INTERRUPT_IMAGE_PROCESSING, e);
      Thread.currentThread().interrupt();
    }
    return nitfSegmentsFlow;
  }

  /**
   * This method exists so unit tests can override and create a different TFBOS for testing
   * exceptions.
   */
  protected TemporaryFileBackedOutputStream createTemporaryFileBackedOutputStream() {
    return new TemporaryFileBackedOutputStream();
  }

  private NitfSegmentsFlow getNitfSegmentsFlow(InputStream resourceInputStream)
      throws NitfFormatException, IOException {
    notNull(resourceInputStream, "resourceInputStream must be non-null");

    NitfSegmentsFlow nitfSegmentsFlow;
    try (TemporaryFileBackedOutputStream tfbos = createTemporaryFileBackedOutputStream()) {

      IOUtils.copyLarge(resourceInputStream, tfbos);

      try (InputStream is = tfbos.asByteSource().openBufferedStream()) {
        nitfSegmentsFlow = new NitfParserInputFlowImpl().inputStream(is).allData();
      }
    }

    return nitfSegmentsFlow;
  }

  private List<ImageSegment> getImageSegments(NitfSegmentsFlow nitfSegmentsFlow) {
    List<ImageSegment> imageSegments = new LinkedList<>();
    nitfSegmentsFlow.forEachImageSegment(imageSegments::add);
    return imageSegments;
  }

  private boolean isSdeTre(Tre tre) {
    return SDE_PATTERN.matcher(tre.getName().trim()).matches();
  }

  private void copySDEs(TaggedRecordExtensionHandler in, TaggedRecordExtensionHandler out) {
    TreCollection treCollection = out.getTREsRawStructure();
    in.getTREsRawStructure().getTREs().stream().filter(this::isSdeTre).forEach(treCollection::add);
  }

  /**
   * Create a NITF of a chip that extracted from another NITF.
   *
   * @param chip the image data for the chipped area
   * @param nitfSegmentsFlow the segments from the original nitf
   * @param sourceX the x pixel coordinates of the original nitf where the chip was extracted
   * @param sourceY the y pixel coordinates of the original nitf where the chip was extracted
   * @return a nitf file containing the chip
   */
  @SuppressWarnings("unused")
  public BinaryContent getNitfBinaryContent(
      BufferedImage chip, NitfSegmentsFlow nitfSegmentsFlow, int sourceX, int sourceY)
      throws IOException, MimeTypeParseException, NitfFormatException {
    try {
      lock.acquire();
      try {
        NitfHeader chipHeader = createChipHeader(nitfSegmentsFlow);

        nitfSegmentsFlow.fileHeader(nitfHeader -> copySDEs(nitfHeader, chipHeader));

        ImageSegment chipImageSegment =
            createChipImageSegment(chip, sourceX, sourceY, nitfSegmentsFlow);

        addIchipbTre(chip, sourceX, sourceY, chipImageSegment);

        List<ImageSegment> imageSegments = getImageSegments(nitfSegmentsFlow);
        if (!imageSegments.isEmpty()) {
          copySDEs(imageSegments.get(0), chipImageSegment);
        }

        return nitfToBinaryContent(chipHeader, chipImageSegment);
      } finally {
        lock.release();
        nitfSegmentsFlow.end();
      }
    } catch (InterruptedException e) {
      LOGGER.debug(INTERRUPT_IMAGE_PROCESSING, e);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  private NitfHeader createChipHeader(NitfSegmentsFlow nitfSegmentsFlow) {
    NitfHeader chipHeader = NitfHeaderFactory.getDefault(FileType.NITF_TWO_ONE);

    nitfSegmentsFlow.fileHeader(
        originalHeader -> {
          chipHeader.setFileTitle(originalHeader.getFileTitle());
          chipHeader.setOriginatingStationId(originalHeader.getOriginatingStationId());
          chipHeader.setFileBackgroundColour(originalHeader.getFileBackgroundColour());
          chipHeader.setFileDateTime(DateTimeImpl.getNitfDateTimeForNow());
          chipHeader.setFileSecurityMetadata(originalHeader.getFileSecurityMetadata());
          chipHeader.setOriginatorsName(originalHeader.getOriginatorsName());
          chipHeader.setOriginatorsPhoneNumber(originalHeader.getOriginatorsPhoneNumber());
          chipHeader.setSecurityMetadata(originalHeader.getFileSecurityMetadata());
          chipHeader.setStandardType(originalHeader.getStandardType());
          chipHeader.setUserDefinedHeaderOverflow(0);
          chipHeader.setExtendedHeaderDataOverflow(0);
          chipHeader.setComplexityLevel(originalHeader.getComplexityLevel());
        });
    return chipHeader;
  }

  private void addIchipbTre(
      BufferedImage chip, int sourceX, int sourceY, ImageSegment imageSegment) {
    imageSegment
        .getTREsRawStructure()
        .add(createIchipb(chip, sourceX, sourceY, chip.getWidth(), chip.getHeight()));
  }

  private ImageSegment createChipImageSegment(
      BufferedImage chip, int sourceX, int sourceY, NitfSegmentsFlow nitfSegmentsFlow)
      throws IOException, NitfFormatException {

    List<ImageSegment> originalImageSegments = getImageSegments(nitfSegmentsFlow);

    if (originalImageSegments.isEmpty()) {
      throw new IOException("expected at least one image segment in nitf");
    }

    ImageSegment originalImageSegment = originalImageSegments.get(0);

    ImageSegment chipImageSegment = ImageSegmentFactory.getDefault(FileType.NITF_TWO_ONE);

    chipImageSegment.setImageCategory(originalImageSegment.getImageCategory());

    int numberOfBlocksPerColumn;
    int numberOfBlocksPerRow;
    int numberOfPixelsPerBlockHorizontalRaw;
    int numberOfPixelsPerBlockVerticalRaw;
    boolean isBlocking;

    if (chip.getWidth() > BLOCK_WIDTH || chip.getHeight() > BLOCK_HEIGHT) {
      numberOfBlocksPerRow = (int) Math.ceil((double) chip.getWidth() / BLOCK_WIDTH);
      numberOfBlocksPerColumn = (int) Math.ceil((double) chip.getHeight() / BLOCK_HEIGHT);
      numberOfPixelsPerBlockHorizontalRaw = BLOCK_WIDTH;
      numberOfPixelsPerBlockVerticalRaw = BLOCK_HEIGHT;
      isBlocking = true;
    } else {
      numberOfBlocksPerColumn = 1;
      numberOfBlocksPerRow = 1;
      numberOfPixelsPerBlockHorizontalRaw = 0;
      numberOfPixelsPerBlockVerticalRaw = 0;
      isBlocking = false;
    }

    originalImageSegment.getImageComments().forEach(chipImageSegment::addImageComment);
    chipImageSegment.setNumberOfBlocksPerColumn(numberOfBlocksPerColumn);
    chipImageSegment.setNumberOfBlocksPerRow(numberOfBlocksPerRow);
    chipImageSegment.setIdentifier(originalImageSegment.getIdentifier());
    chipImageSegment.setImageIdentifier2(originalImageSegment.getImageIdentifier2());
    chipImageSegment.setImageMagnification(originalImageSegment.getImageMagnification());
    chipImageSegment.setImageTargetId(originalImageSegment.getImageTargetId());
    chipImageSegment.setImageSource(originalImageSegment.getImageSource());
    chipImageSegment.setNumberOfColumns(chip.getWidth());
    chipImageSegment.setNumberOfRows(chip.getHeight());

    chipImageSegment.setNumberOfPixelsPerBlockHorizontalRaw(numberOfPixelsPerBlockHorizontalRaw);
    chipImageSegment.setNumberOfPixelsPerBlockVerticalRaw(numberOfPixelsPerBlockVerticalRaw);
    chipImageSegment.setImageDateTime(originalImageSegment.getImageDateTime());

    setImageCoordinates(
        sourceX,
        sourceY,
        chip.getWidth(),
        chip.getHeight(),
        chipImageSegment,
        originalImageSegment);

    chipImageSegment.setPixelJustification(PixelJustification.RIGHT);

    chipImageSegment.setImageDisplayLevel(DEFAULT_DISPLAY_LEVEL);
    chipImageSegment.setImageLocationRow(0);
    chipImageSegment.setImageLocationColumn(0);

    setImageDataFields(chip, chipImageSegment);

    setImageData(
        chip,
        chipImageSegment,
        isBlocking,
        numberOfPixelsPerBlockHorizontalRaw,
        numberOfPixelsPerBlockVerticalRaw);

    chipImageSegment.setUserDefinedHeaderOverflow(0);

    chipImageSegment.setSecurityMetadata(originalImageSegment.getSecurityMetadata());

    return chipImageSegment;
  }

  private void setImageDataFields(BufferedImage chip, ImageSegment chipImageSegment)
      throws IOException {

    int[] componentSizes = chip.getColorModel().getComponentSize();
    int pixelSize = chip.getColorModel().getPixelSize();

    switch (chip.getType()) {
      case BufferedImage.TYPE_BYTE_GRAY:
      case BufferedImage.TYPE_USHORT_GRAY:
      case BufferedImage.TYPE_BYTE_BINARY:
        setMonochrome(chipImageSegment, componentSizes[0], pixelSize);
        break;
      case BufferedImage.TYPE_3BYTE_BGR:
      case BufferedImage.TYPE_INT_BGR:
        setImageFieldHelper(
            chipImageSegment,
            PixelValueType.INTEGER,
            ImageRepresentation.RGBTRUECOLOUR,
            componentSizes[0],
            pixelSize / 3,
            new String[] {"B", "G", "R"});
        break;
      case BufferedImage.TYPE_4BYTE_ABGR:
      case BufferedImage.TYPE_4BYTE_ABGR_PRE:
        setImageFieldHelper(
            chipImageSegment,
            PixelValueType.INTEGER,
            ImageRepresentation.RGBTRUECOLOUR,
            componentSizes[0],
            pixelSize / 4,
            new String[] {"B", "G", "R"});
        break;
      case BufferedImage.TYPE_INT_ARGB_PRE:
      case BufferedImage.TYPE_INT_ARGB:
        setARGB(chipImageSegment, componentSizes[0], pixelSize);
        break;
      case BufferedImage.TYPE_INT_RGB:
      case BufferedImage.TYPE_USHORT_555_RGB:
        setRGB(chipImageSegment, componentSizes[0], pixelSize);
        break;
      case BufferedImage.TYPE_CUSTOM:
        if (componentSizes.length == 1) {
          setMonochrome(chipImageSegment, componentSizes[0], pixelSize);
        } else if (componentSizes.length == 3) {
          setRGB(chipImageSegment, componentSizes[0], pixelSize);
        } else if (componentSizes.length == 4) {
          setARGB(chipImageSegment, componentSizes[0], pixelSize);
        } else {
          throw new IOException(
              "unsupported color model for image type CUSTOM, only monochrome and 32-bit argb are supported");
        }
        break;
      case BufferedImage.TYPE_BYTE_INDEXED:
        setImageFieldHelper(
            chipImageSegment,
            PixelValueType.INTEGER,
            ImageRepresentation.RGBLUT,
            componentSizes[0],
            pixelSize,
            new String[] {"LU"});
        break;
      case BufferedImage.TYPE_USHORT_565_RGB:
        // don't know how to handle this one, since the bitsPerPixelPerBand is not consistent
        break;
      default:
        throw new IOException("unsupported image data type: type=" + chip.getType());
    }
  }

  private void setARGB(ImageSegment chipImageSegment, int componentSize, int pixelSize) {
    setImageFieldHelper(
        chipImageSegment,
        PixelValueType.INTEGER,
        ImageRepresentation.RGBTRUECOLOUR,
        componentSize,
        pixelSize / 4,
        new String[] {"R", "G", "B"});
  }

  private void setRGB(ImageSegment chipImageSegment, int componentSize, int pixelSize) {
    setImageFieldHelper(
        chipImageSegment,
        PixelValueType.INTEGER,
        ImageRepresentation.RGBTRUECOLOUR,
        componentSize,
        pixelSize / 3,
        new String[] {"R", "G", "B"});
  }

  private void setMonochrome(ImageSegment chipImageSegment, int componentSize, int pixelSize) {
    setImageFieldHelper(
        chipImageSegment,
        PixelValueType.INTEGER,
        ImageRepresentation.MONOCHROME,
        componentSize,
        pixelSize,
        new String[] {"M"});
  }

  private void setImageFieldHelper(
      ImageSegment imageSegment,
      PixelValueType pixelValueType,
      ImageRepresentation imageRepresentation,
      int actualBitsPerPixelPerBand,
      int bitsPerPixelPerBand,
      String[] bands) {
    imageSegment.setPixelValueType(pixelValueType);
    imageSegment.setImageRepresentation(imageRepresentation);
    imageSegment.setActualBitsPerPixelPerBand(actualBitsPerPixelPerBand);
    imageSegment.setNumberOfBitsPerPixelPerBand(bitsPerPixelPerBand);
    for (String band : bands) {
      imageSegment.addImageBand(createImageBand(band));
    }
  }

  private ImageBand createImageBand(String imageRepresentation) {
    ImageBandImpl imageBand = new ImageBandImpl();
    imageBand.setImageRepresentation(imageRepresentation);
    imageBand.setImageSubcategory("");
    imageBand.setNumLUTEntries(0);
    return imageBand;
  }

  private double degreesToMinutes(double degrees) {
    return degrees * 60d;
  }

  private double minutesToSeconds(double minutes) {
    return minutes * 60d;
  }

  private long[] doubleToDMS(double value) {
    double tmp = Math.abs(value);
    double degrees = Math.floor(tmp);
    tmp = degreesToMinutes(tmp - degrees);
    double minutes = Math.floor(tmp);
    double seconds = minutesToSeconds(tmp - minutes);
    return new long[] {Math.round(degrees), Math.round(minutes), Math.round(seconds)};
  }

  String formatToDMS(double lat, double lon) {
    long[] latDMS = doubleToDMS(lat);
    long[] lonDMS = doubleToDMS(lon);
    return String.format("%02d%02d%02d%s", latDMS[0], latDMS[1], latDMS[2], lat >= 0 ? "N" : "S")
        + String.format("%03d%02d%02d%s", lonDMS[0], lonDMS[1], lonDMS[2], lon >= 0 ? "E" : "W");
  }

  private void setImageCoordinates(
      int sourceX,
      int sourceY,
      int selectWidth,
      int selectHeight,
      ImageSegment chipImageSegment,
      ImageSegment originalImageSegment)
      throws NitfFormatException {
    CoordinateConverter coordinateConverter =
        new CoordinateConverter(
            (int) originalImageSegment.getNumberOfColumns(),
            (int) originalImageSegment.getNumberOfRows(),
            getFullImageCoordinates(originalImageSegment));

    List<Vector> chipCornerPixels =
        getChipCornerPixels(sourceX, sourceY, selectWidth, selectHeight);

    List<Vector> chipCornerCoords = coordinateConverter.toLonLat(chipCornerPixels);

    ImageCoordinatePairImpl icp0 = new ImageCoordinatePairImpl();
    icp0.setFromDMS(formatToDMS(chipCornerCoords.get(0).get(1), chipCornerCoords.get(0).get(0)));

    ImageCoordinatePairImpl icp1 = new ImageCoordinatePairImpl();
    icp1.setFromDMS(formatToDMS(chipCornerCoords.get(1).get(1), chipCornerCoords.get(1).get(0)));

    ImageCoordinatePairImpl icp2 = new ImageCoordinatePairImpl();
    icp2.setFromDMS(formatToDMS(chipCornerCoords.get(2).get(1), chipCornerCoords.get(2).get(0)));

    ImageCoordinatePairImpl icp3 = new ImageCoordinatePairImpl();
    icp3.setFromDMS(formatToDMS(chipCornerCoords.get(3).get(1), chipCornerCoords.get(3).get(0)));

    chipImageSegment.setImageCoordinates(
        new ImageCoordinatesImpl(new ImageCoordinatePair[] {icp0, icp1, icp2, icp3}));
    chipImageSegment.setImageCoordinatesRepresentation(ImageCoordinatesRepresentation.GEOGRAPHIC);

    logImageCoordinates("chip image coordinates", chipImageSegment.getImageCoordinates());
  }

  private void logImageCoordinates(String prefix, ImageCoordinates imageCoordinates) {
    logImageCoordinatePair(prefix, imageCoordinates.getCoordinate00());
    logImageCoordinatePair(prefix, imageCoordinates.getCoordinate0MaxCol());
    logImageCoordinatePair(prefix, imageCoordinates.getCoordinateMaxRow0());
    logImageCoordinatePair(prefix, imageCoordinates.getCoordinateMaxRowMaxCol());
  }

  private void logImageCoordinatePair(String prefix, ImageCoordinatePair imageCoordinatePair) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "{} - (lon,lat) {}",
          prefix,
          String.format(
              "%.5f,%.5f", imageCoordinatePair.getLongitude(), imageCoordinatePair.getLatitude()));
    }
  }

  private void logVectors(String prefix, List<Vector> vectors) {
    vectors.forEach(
        vector ->
            LOGGER.debug(
                "{} - (lon,lat) {}",
                prefix,
                String.format("%.5f,%.5f", vector.get(0), vector.get(1))));
  }

  private List<Vector> getFullImageCoordinates(ImageSegment originalImageSegment) {
    List<Vector> fullImageCoordinates = new LinkedList<>();
    fullImageCoordinates.add(
        new BasicVector(
            new double[] {
              originalImageSegment.getImageCoordinates().getCoordinate00().getLongitude(),
              originalImageSegment.getImageCoordinates().getCoordinate00().getLatitude()
            })); // upper left
    fullImageCoordinates.add(
        new BasicVector(
            new double[] {
              originalImageSegment.getImageCoordinates().getCoordinate0MaxCol().getLongitude(),
              originalImageSegment.getImageCoordinates().getCoordinate0MaxCol().getLatitude()
            })); // upper right
    fullImageCoordinates.add(
        new BasicVector(
            new double[] {
              originalImageSegment.getImageCoordinates().getCoordinateMaxRowMaxCol().getLongitude(),
              originalImageSegment.getImageCoordinates().getCoordinateMaxRowMaxCol().getLatitude()
            })); // lower right
    fullImageCoordinates.add(
        new BasicVector(
            new double[] {
              originalImageSegment.getImageCoordinates().getCoordinateMaxRow0().getLongitude(),
              originalImageSegment.getImageCoordinates().getCoordinateMaxRow0().getLatitude()
            })); // lower left

    logVectors("full image coordinates", fullImageCoordinates);

    return fullImageCoordinates;
  }

  private void setImageData(
      BufferedImage chip,
      ImageSegment chipImageSegment,
      boolean isBlocking,
      int numberOfPixelsPerBlockHorizontalRaw,
      int numberOfPixelsPerBlockVerticalRaw)
      throws IOException {

    JpegService jpeg2000Service = new Jpeg2000ServiceImpl();

    jpeg2000Service.createJpeg(
        chip,
        chipImageSegment,
        isBlocking,
        numberOfPixelsPerBlockHorizontalRaw,
        numberOfPixelsPerBlockVerticalRaw);
  }

  private List<Vector> getChipCornerPixels(
      int sourceX, int sourceY, int selectWidth, int selectHeight) {
    List<Vector> chipCornerPixels = new LinkedList<>();
    chipCornerPixels.add(new BasicVector(new double[] {sourceX, sourceY}));
    chipCornerPixels.add(new BasicVector(new double[] {sourceX + selectWidth, sourceY}));
    chipCornerPixels.add(
        new BasicVector(new double[] {sourceX + selectWidth, sourceY + selectHeight}));
    chipCornerPixels.add(new BasicVector(new double[] {sourceX, sourceY + selectHeight}));
    return chipCornerPixels;
  }

  private BinaryContent nitfToBinaryContent(NitfHeader header, ImageSegment imageSegment)
      throws IOException, MimeTypeParseException {
    byte[] data;
    File tmpFile = File.createTempFile("nitfchip-", ".ntf");
    try {
      new NitfCreationFlowImpl()
          .fileHeader(() -> header)
          .imageSegment(() -> imageSegment)
          .write(tmpFile.getAbsolutePath());

      try (FileInputStream fis = new FileInputStream(tmpFile)) {
        data = IOUtils.toByteArray(fis);
      }

    } finally {
      if (!tmpFile.delete()) {
        LOGGER.debug("unable to delete the temporary file '{}'", tmpFile);
      }
    }

    return new BinaryContentImpl(new ByteArrayInputStream(data), new MimeType(IMAGE_NITF));
  }

  @SuppressWarnings("UnnecessaryLocalVariable")
  Tre createIchipb(
      BufferedImage chip, int sourceX, int sourceY, float selectWidth, float selectHeight) {

    Tre ichipb = TreFactory.getDefault("ICHIPB", TreSource.ImageExtendedSubheaderData);

    ichipb.add(new TreEntryImpl("XFRM_FLAG", "00", UINT));
    ichipb.add(new TreEntryImpl("SCALE_FACTOR", "0001.00000", REAL));
    ichipb.add(new TreEntryImpl("ANAMRPH_CORR", "00", UINT));

    ichipb.add(new TreEntryImpl("SCANBLK_NUM", "01", UINT));

    float halfPixel = 0.5f;

    float chipX0 = halfPixel;
    float chipY0 = halfPixel;

    float chipX1 = (float) chip.getWidth() - halfPixel;
    float chipY1 = (float) chip.getHeight() - halfPixel;

    ichipb.add(new TreEntryImpl("OP_ROW_11", formatCoord(chipY0), REAL));
    ichipb.add(new TreEntryImpl("OP_COL_11", formatCoord(chipX0), REAL));
    ichipb.add(new TreEntryImpl("OP_ROW_12", formatCoord(chipY0), REAL));
    ichipb.add(new TreEntryImpl("OP_COL_12", formatCoord(chipX1), REAL));
    ichipb.add(new TreEntryImpl("OP_ROW_21", formatCoord(chipY1), REAL));
    ichipb.add(new TreEntryImpl("OP_COL_21", formatCoord(chipX0), REAL));
    ichipb.add(new TreEntryImpl("OP_ROW_22", formatCoord(chipY1), REAL));
    ichipb.add(new TreEntryImpl("OP_COL_22", formatCoord(chipX1), REAL));

    float origX0 = sourceX + halfPixel;
    float origY0 = sourceY + halfPixel;

    float origX1 = sourceX + selectWidth - halfPixel;
    float origY1 = sourceY + selectHeight - halfPixel;

    ichipb.add(new TreEntryImpl("FI_ROW_11", formatCoord(origY0), REAL));
    ichipb.add(new TreEntryImpl("FI_COL_11", formatCoord(origX0), REAL));
    ichipb.add(new TreEntryImpl("FI_ROW_12", formatCoord(origY0), REAL));
    ichipb.add(new TreEntryImpl("FI_COL_12", formatCoord(origX1), REAL));
    ichipb.add(new TreEntryImpl("FI_ROW_21", formatCoord(origY1), REAL));
    ichipb.add(new TreEntryImpl("FI_COL_21", formatCoord(origX0), REAL));
    ichipb.add(new TreEntryImpl("FI_ROW_22", formatCoord(origY1), REAL));
    ichipb.add(new TreEntryImpl("FI_COL_22", formatCoord(origX1), REAL));

    ichipb.add(new TreEntryImpl("FI_ROW", "00000000", UINT));
    ichipb.add(new TreEntryImpl("FI_COL", "00000000", UINT));

    return ichipb;
  }

  private String formatCoord(float coord) {
    return String.format("%012.3f", coord);
  }

  private void validateArgument(Object value, String argumentName) {
    if (value == null) {
      throw new IllegalArgumentException(
          String.format("argument '%s' may not be null.", argumentName));
    }
  }

  private void validateObjectState(Object value, String argumentName) {
    if (value == null) {
      throw new IllegalStateException(
          String.format("object property '%s' may not be null.", argumentName));
    }
  }

  /**
   * @param exception the exception to be wrapped.
   * @throws CatalogTransformerException in every case.
   */
  @SuppressWarnings("unused")
  public void wrapException(Exception exception) throws CatalogTransformerException {
    throw new CatalogTransformerException(exception);
  }
}
