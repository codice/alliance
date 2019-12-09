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
package org.codice.alliance.plugin.nitf;

import static org.apache.commons.lang3.Validate.notNull;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriter;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteSource;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.codice.ddf.catalog.async.data.api.internal.ProcessCreateItem;
import org.codice.ddf.catalog.async.data.api.internal.ProcessDeleteItem;
import org.codice.ddf.catalog.async.data.api.internal.ProcessRequest;
import org.codice.ddf.catalog.async.data.api.internal.ProcessResource;
import org.codice.ddf.catalog.async.data.api.internal.ProcessResourceItem;
import org.codice.ddf.catalog.async.data.api.internal.ProcessUpdateItem;
import org.codice.ddf.catalog.async.data.impl.ProcessCreateItemImpl;
import org.codice.ddf.catalog.async.data.impl.ProcessResourceImpl;
import org.codice.ddf.catalog.async.data.impl.ProcessUpdateItemImpl;
import org.codice.ddf.catalog.async.plugin.api.internal.PostProcessPlugin;
import org.codice.ddf.platform.util.TemporaryFileBackedOutputStream;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link PostProcessPlugin} creates and stores the NITF thumbnail, overview, and original
 * images. The thumbnail is stored in the metacard, and the overview and original are stored as
 * derived resources in the content store.
 */
public class NitfPostProcessPlugin implements PostProcessPlugin {

  private static final double DEFAULT_MAX_SIDE_LENGTH = 1024.0;

  private static final String IMAGE_NITF = "image/nitf";

  @VisibleForTesting static final MimeType NITF_MIME_TYPE;

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());

    try {
      NITF_MIME_TYPE = new MimeType(IMAGE_NITF);
    } catch (MimeTypeParseException e) {
      throw new ExceptionInInitializerError(
          String.format("Unable to create MimeType from '%s': %s", IMAGE_NITF, e.getMessage()));
    }
  }

  private static final String IMAGE_JPEG = "image/jpeg";

  private static final String IMAGE_JPEG2K = "image/jp2";

  private static final int THUMBNAIL_WIDTH = 200;

  private static final int THUMBNAIL_HEIGHT = 200;

  private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

  private static final String JPG = "jpg";

  private static final String JP2 = "jp2";

  private static final Logger LOGGER = LoggerFactory.getLogger(NitfPostProcessPlugin.class);

  private static final String OVERVIEW = "overview";

  private static final String ORIGINAL = "original";

  private static final String DERIVED_IMAGE_FILENAME_PATTERN = "%s-%s.%s";

  // non-word characters equivalent to [^a-zA-Z0-9_]
  private static final String INVALID_FILENAME_CHARACTER_REGEX = "[\\W]";

  private static final int ARGB_COMPONENT_COUNT = 4;

  private static final int DEFAULT_MAX_NITF_SIZE_MB = 120;

  private static final int BYTES_PER_KILOBYTE = 1024;

  private static final int DEFAULT_FILE_BACKED_OUTPUT_STREAM_THRESHOLD = 32 * BYTES_PER_KILOBYTE;

  private static final int MAX_THREAD_COUNT =
      Integer.parseInt(System.getProperty("default.nitf.thread.count", "3"));

  private Semaphore lock = new Semaphore(MAX_THREAD_COUNT, true);

  private volatile boolean createOverview = true;

  private volatile boolean storeOriginalImage = true;

  private volatile int maxNitfSizeMB = DEFAULT_MAX_NITF_SIZE_MB;

  private volatile double maxSideLength = DEFAULT_MAX_SIDE_LENGTH;

  private final Semaphore available = new Semaphore(2, true);

  private Supplier<NitfRenderer> nitfRendererSupplier;
  private Supplier<NitfParserInputFlow> nitfParserSupplier;

  public NitfPostProcessPlugin() {
    this(NitfRenderer::new, NitfParserInputFlowImpl::new);
  }

  public NitfPostProcessPlugin(
      Supplier<NitfRenderer> nitfRendererSupplier,
      Supplier<NitfParserInputFlow> nitfParserSupplier) {
    this.nitfRendererSupplier = nitfRendererSupplier;
    this.nitfParserSupplier = nitfParserSupplier;
  }

  public NitfPostProcessPlugin(
      Semaphore lock,
      Supplier<NitfRenderer> nitfRendererSupplier,
      Supplier<NitfParserInputFlow> nitfParserSupplier) {
    this.nitfRendererSupplier = nitfRendererSupplier;
    this.nitfParserSupplier = nitfParserSupplier;
    this.lock = lock;
  }

  @Override
  public ProcessRequest<ProcessCreateItem> processCreate(ProcessRequest<ProcessCreateItem> input) {
    handleProcessCreateItem(
        notNull(input, "processCreate(): argument 'input' may not be null.").getProcessItems());
    return input;
  }

  @Override
  public ProcessRequest<ProcessUpdateItem> processUpdate(ProcessRequest<ProcessUpdateItem> input) {
    handleProcessUpdateItem(
        notNull(input, "processUpdate(): argument 'input' may not be null.").getProcessItems());
    return input;
  }

  @Override
  public ProcessRequest<ProcessDeleteItem> processDelete(ProcessRequest<ProcessDeleteItem> input) {
    notNull(input, "processDelete(): argument 'input' may not be null.");
    return input;
  }

  private boolean isNitfMimeType(String rawMimeType) {
    try {
      return NITF_MIME_TYPE.match(rawMimeType);
    } catch (MimeTypeParseException e) {
      LOGGER.debug("unable to compare mime types: {} vs {}", NITF_MIME_TYPE, rawMimeType);
    }

    return false;
  }

  public void setMaxSideLength(int maxSideLength) {
    if (maxSideLength > 0) {
      LOGGER.trace("Setting derived image maxSideLength to {}", maxSideLength);
      this.maxSideLength = maxSideLength;
    } else {
      LOGGER.debug(
          "Invalid `maxSideLength` value [{}], must be greater than zero. Default value [{}] will be used instead.",
          maxSideLength,
          DEFAULT_MAX_SIDE_LENGTH);
      this.maxSideLength = DEFAULT_MAX_SIDE_LENGTH;
    }
  }

  public void setMaxNitfSizeMB(int maxNitfSizeMB) {
    this.maxNitfSizeMB = maxNitfSizeMB;
  }

  public void setCreateOverview(boolean createOverview) {
    this.createOverview = createOverview;
  }

  public void setStoreOriginalImage(boolean storeOriginalImage) {
    this.storeOriginalImage = storeOriginalImage;
  }

  private void handleProcessCreateItem(List<ProcessCreateItem> processCreateItems) {
    List<ProcessCreateItem> createItems =
        processCreateItems
            .stream()
            .flatMap(this::handleProcessCreateItem)
            .collect(Collectors.toList());
    processCreateItems.addAll(createItems);
  }

  private Stream<ProcessCreateItem> handleProcessCreateItem(ProcessCreateItem processCreateItem) {
    List<ProcessCreateItem> createdItems = null;
    Metacard metacard = processCreateItem.getMetacard();
    ProcessResource processResource = processCreateItem.getProcessResource();

    if (shouldProcess(processResource)) {

      try {
        lock.acquire();
        try {
          createdItems =
              process(
                  processCreateItem,
                  metacard,
                  null,
                  processResource,
                  constructorTriple ->
                      new ProcessCreateItemImpl(
                          constructorTriple.getLeft(), constructorTriple.getMiddle()));
        } finally {
          lock.release();
        }
      } catch (InterruptedException e) {
        LOGGER.debug("Interrupt received while doing image processing.", e);
        Thread.currentThread().interrupt();
      }
    }

    if (createdItems == null) {
      createdItems = new ArrayList<>();
    }
    return createdItems.stream();
  }

  private <T extends ProcessResourceItem> List<T> process(
      T processResourceItem,
      Metacard metacard,
      Metacard originalMetacard,
      ProcessResource processResource,
      Function<Triple<ProcessResource, Metacard, Metacard>, T> constructor) {
    List<T> items = new ArrayList<>();
    try (TemporaryFileBackedOutputStream fbos =
        new TemporaryFileBackedOutputStream(DEFAULT_FILE_BACKED_OUTPUT_STREAM_THRESHOLD)) {

      // TODO: 06/21/2018 oconnormi - can probably get rid of this once
      // ProcessResourceImpl.getInputStream can be called multiple times
      fbos.write(IOUtils.toByteArray(processResource.getInputStream()));
      ByteSource byteSource = fbos.asByteSource();
      BufferedImage renderedImage = renderImage(byteSource.openStream());

      if (renderedImage != null) {
        addThumbnailToMetacard(metacard, renderedImage);
        processResourceItem.markMetacardAsModified();
        if (createOverview) {
          ProcessResource overviewProcessResource = createOverviewResource(renderedImage, metacard);
          items.add(
              constructor.apply(
                  new ImmutableTriple<>(overviewProcessResource, metacard, originalMetacard)));
        }

        if (storeOriginalImage) {
          ProcessResource originalImageProcessResource =
              createOriginalImage(
                  renderImageUsingOriginalDataModel(byteSource.openStream()), metacard);

          items.add(
              constructor.apply(
                  new ImmutableTriple<>(originalImageProcessResource, metacard, originalMetacard)));
        }
      }
    } catch (IOException | NitfFormatException | RuntimeException e) {
      LOGGER.debug("An error occured when rendering a nitf for {}", processResource.getName(), e);
    } catch (InterruptedException e) {
      LOGGER.error("Rendering failed for {}", processResource.getName(), e);
      Thread.currentThread().interrupt();
      throw new RuntimeException(
          String.format("Rendering failed for %s", processResource.getName()));
    }
    return items;
  }

  private void handleProcessUpdateItem(List<ProcessUpdateItem> processUpdateItems) {
    List<ProcessUpdateItem> updateItems =
        processUpdateItems
            .stream()
            .flatMap(this::handleProcessUpdateItem)
            .collect(Collectors.toList());
    processUpdateItems.addAll(updateItems);
  }

  private Stream<ProcessUpdateItem> handleProcessUpdateItem(ProcessUpdateItem processUpdateItem) {
    List<ProcessUpdateItem> updatedItems = new ArrayList<>();
    Metacard metacard = processUpdateItem.getMetacard();
    Metacard originalMetacard = processUpdateItem.getOldMetacard();
    ProcessResource processResource = processUpdateItem.getProcessResource();

    if (shouldProcess(processResource)) {
      try {
        lock.acquire();
        try {
          updatedItems =
              process(
                  processUpdateItem,
                  metacard,
                  originalMetacard,
                  processResource,
                  constructorTriple ->
                      new ProcessUpdateItemImpl(
                          constructorTriple.getLeft(),
                          constructorTriple.getMiddle(),
                          constructorTriple.getRight()));
        } finally {
          lock.release();
        }
      } catch (InterruptedException e) {
        LOGGER.debug("Interrupt received while doing image processing.", e);
        Thread.currentThread().interrupt();
      }
    }
    return updatedItems.stream();
  }

  private ProcessResource createOverviewResource(BufferedImage renderedImage, Metacard metacard) {
    return createDerivedImage(
        OVERVIEW,
        renderedImage,
        metacard,
        calculateOverviewWidth(renderedImage),
        calculateOverviewHeight(renderedImage));
  }

  private BufferedImage renderImage(InputStream inputStream)
      throws NitfFormatException, InterruptedException {

    return render(
        inputStream,
        input -> {
          try {
            return input.getRight().render(input.getLeft());
          } catch (IOException e) {
            LOGGER.debug("An error occurred when rendering a nitf", e.getMessage(), e);
          }
          return null;
        });
  }

  private BufferedImage renderImageUsingOriginalDataModel(InputStream inputStream)
      throws NitfFormatException, InterruptedException {

    return render(
        inputStream,
        input -> {
          try {
            return input.getRight().renderToClosestDataModel(input.getLeft());
          } catch (IOException e) {
            LOGGER.debug("An error occurred when rendering a nitf", e.getMessage(), e);
          }
          return null;
        });
  }

  private BufferedImage render(
      InputStream inputStream,
      Function<Pair<ImageSegment, NitfRenderer>, BufferedImage> imageSegmentFunction)
      throws InterruptedException, NitfFormatException {

    final ThreadLocal<BufferedImage> bufferedImage = new ThreadLocal<>();

    if (inputStream != null) {
      try {
        available.acquire();
        NitfRenderer renderer = nitfRendererSupplier.get();
        NitfParserInputFlow parserInputFlow = nitfParserSupplier.get();

        parserInputFlow
            .inputStream(inputStream)
            .allData()
            .forEachImageSegment(
                segment -> {
                  if (bufferedImage.get() == null) {
                    BufferedImage bi =
                        imageSegmentFunction.apply(new ImmutablePair<>(segment, renderer));
                    if (bi != null) {
                      bufferedImage.set(bi);
                    }
                  }
                })
            .end();
      } finally {
        IOUtils.closeQuietly(inputStream);
        available.release();
      }
    }

    BufferedImage image = bufferedImage.get();
    bufferedImage.remove();
    return image;
  }

  private void addThumbnailToMetacard(Metacard metacard, BufferedImage bufferedImage) {
    try {
      byte[] thumbnailImage = scaleImage(bufferedImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

      if (thumbnailImage.length > 0) {
        metacard.setAttribute(new AttributeImpl(Core.THUMBNAIL, thumbnailImage));
      }
    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
    }
  }

  private ProcessResource createDerivedImage(
      String qualifier, BufferedImage image, Metacard metacard, int maxWidth, int maxHeight) {
    try {
      byte[] overviewBytes = scaleImage(image, maxWidth, maxHeight);
      InputStream overviewBytesInputStream = new ByteArrayInputStream(overviewBytes);

      ProcessResource processResource =
          new ProcessResourceImpl(
              metacard.getId(),
              overviewBytesInputStream,
              IMAGE_JPEG,
              buildDerivedImageTitle(metacard.getTitle(), qualifier, JPG),
              overviewBytes.length,
              qualifier);

      ((ProcessResourceImpl) processResource).markAsModified();
      addDerivedResourceAttribute(metacard, processResource);

      return processResource;
    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return null;
  }

  private ProcessResource createOriginalImage(BufferedImage image, Metacard metacard) {

    try {
      byte[] originalBytes = renderToJpeg2k(image);

      InputStream originalBytesInputStream = new ByteArrayInputStream(originalBytes);

      ProcessResource processResource =
          new ProcessResourceImpl(
              metacard.getId(),
              originalBytesInputStream,
              IMAGE_JPEG2K,
              buildDerivedImageTitle(metacard.getTitle(), ORIGINAL, JP2),
              originalBytes.length,
              ORIGINAL);

      ((ProcessResourceImpl) processResource).markAsModified();

      addDerivedResourceAttribute(metacard, processResource);

      return processResource;

    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return null;
  }

  @VisibleForTesting
  static String buildDerivedImageTitle(String title, String qualifier, String extension) {
    String rootFileName = FilenameUtils.getBaseName(title);

    // title must contain some alphanumeric, human readable characters, or use default filename
    if (StringUtils.isNotBlank(rootFileName)
        && StringUtils.isNotBlank(rootFileName.replaceAll("[^A-Za-z0-9]", ""))) {
      String strippedFilename = rootFileName.replaceAll(INVALID_FILENAME_CHARACTER_REGEX, "");
      return String.format(DERIVED_IMAGE_FILENAME_PATTERN, qualifier, strippedFilename, extension)
          .toLowerCase();
    }

    return String.format("%s.%s", qualifier, JPG).toLowerCase();
  }

  private byte[] scaleImage(final BufferedImage bufferedImage, int width, int height)
      throws IOException {
    BufferedImage thumbnail =
        Thumbnails.of(bufferedImage)
            .size(width, height)
            .outputFormat(JPG)
            .imageType(BufferedImage.TYPE_3BYTE_BGR)
            .asBufferedImage();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(thumbnail, JPG, outputStream);
    outputStream.flush();
    byte[] thumbnailBytes = outputStream.toByteArray();
    outputStream.close();
    return thumbnailBytes;
  }

  private byte[] renderToJpeg2k(final BufferedImage bufferedImage) throws IOException {

    BufferedImage imageToCompress = bufferedImage;

    if (bufferedImage.getColorModel().getNumComponents() == ARGB_COMPONENT_COUNT) {

      imageToCompress =
          new BufferedImage(
              bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

      Graphics2D g = imageToCompress.createGraphics();

      g.drawImage(bufferedImage, 0, 0, null);
    }

    ByteArrayOutputStream os = new ByteArrayOutputStream();

    J2KImageWriter writer = new J2KImageWriter(new J2KImageWriterSpi());
    J2KImageWriteParam writeParams = (J2KImageWriteParam) writer.getDefaultWriteParam();
    writeParams.setLossless(false);
    writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    writeParams.setCompressionType("JPEG2000");
    writeParams.setCompressionQuality(0.0f);

    ImageOutputStream ios = new MemoryCacheImageOutputStream(os);
    writer.setOutput(ios);
    writer.write(null, new IIOImage(imageToCompress, null, null), writeParams);
    writer.dispose();
    ios.close();

    return os.toByteArray();
  }

  private void addDerivedResourceAttribute(Metacard metacard, ProcessResource processResource) {
    Attribute attribute = metacard.getAttribute(Core.DERIVED_RESOURCE_URI);
    if (attribute == null) {
      attribute = new AttributeImpl(Core.DERIVED_RESOURCE_URI, processResource.getUri());
    } else {
      AttributeImpl newAttribute = new AttributeImpl(attribute);
      newAttribute.addValue(processResource.getUri());
      attribute = newAttribute;
    }

    metacard.setAttribute(attribute);
  }

  private int calculateOverviewHeight(BufferedImage image) {
    final int width = image.getWidth();
    final int height = image.getHeight();

    if (width >= height) {
      return (int) Math.round(height * (maxSideLength / width));
    }

    return Math.min(height, (int) maxSideLength);
  }

  private int calculateOverviewWidth(BufferedImage image) {
    final int width = image.getWidth();
    final int height = image.getHeight();

    if (width >= height) {
      return Math.min(width, (int) maxSideLength);
    }

    return (int) Math.round(width * (maxSideLength / height));
  }

  private boolean shouldProcess(ProcessResource processResource) {
    if (!isNitfMimeType(processResource.getMimeType())) {
      LOGGER.debug(
          "Skipping content item (name={}, mimeType={}) because it is not a NITF",
          processResource.getName(),
          processResource.getMimeType());
      return false;
    }

    if (processResource.getSize() / BYTES_PER_MEGABYTE > maxNitfSizeMB) {
      LOGGER.debug(
          "Skipping content item (name={}, size={} MB) because it is larger than the configured maximum NITF file size to process of {} MB",
          processResource.getName(),
          processResource.getSize() / BYTES_PER_MEGABYTE,
          maxNitfSizeMB);
      return false;
    }
    return true;
  }
}
