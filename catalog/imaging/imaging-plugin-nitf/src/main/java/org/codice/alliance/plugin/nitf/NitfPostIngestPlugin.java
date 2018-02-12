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

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriter;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;
import com.google.common.io.ByteSource;
import ddf.catalog.CatalogFramework;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.content.operation.impl.UpdateStorageRequestImpl;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.Update;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.operation.impl.ResourceRequestById;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PostIngestPlugin;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.imaging.nitf.api.NitfParserService;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This post-ingest plugin creates and stores the NITF thumbnail and NITF overview images. The
 * thumbnail is stored with the Metacard while the overview and original are stored in the content
 * store.
 */
public class NitfPostIngestPlugin implements PostIngestPlugin {

  static final String IMAGE_NITF = "image/nitf";

  private static final String IMAGE_JPEG = "image/jpeg";

  private static final String IMAGE_JPEG2K = "image/jp2";

  private static final String NITF_PROCESSING_KEY = "NitfPostIngestPlugin.Processed";

  private static final int THUMBNAIL_WIDTH = 200;

  private static final int THUMBNAIL_HEIGHT = 200;

  private static final long MEGABYTE = 1024L * 1024L;

  private static final String JPG = "jpg";

  private static final String JP2 = "jp2";

  private static final Logger LOGGER = LoggerFactory.getLogger(NitfPostIngestPlugin.class);

  private static final String OVERVIEW = "overview";

  private static final String ORIGINAL = "original";

  private static final String DERIVED_IMAGE_FILENAME_PATTERN = "%s-%s.%s";

  // non-word characters equivalent to [^a-zA-Z0-9_]
  private static final String INVALID_FILENAME_CHARACTER_REGEX = "[\\W]";

  private static final double DEFAULT_MAX_SIDE_LENGTH = 1024.0;

  private static final int ARGB_COMPONENT_COUNT = 4;

  private static final int DEFAULT_MAX_NITF_SIZE = 120;

  private int maxNitfSizeMB = DEFAULT_MAX_NITF_SIZE;

  private boolean createOverview = true;

  private boolean storeOriginalImage = true;

  private CatalogFramework catalogFramework;

  private NitfParserService nitfParserService;

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  private double maxSideLength = DEFAULT_MAX_SIDE_LENGTH;

  @Override
  public CreateResponse process(CreateResponse createResponse) throws PluginExecutionException {
    if (createResponse == null) {
      throw new PluginExecutionException("process(): argument 'createResponse' may not be null.");
    }
    updateContent(
        new HashSet<>(createResponse.getCreatedMetacards()),
        createResponse.getRequest().getProperties());
    return createResponse;
  }

  @Override
  public UpdateResponse process(UpdateResponse updateResponse) throws PluginExecutionException {
    if (updateResponse == null) {
      throw new PluginExecutionException("process(): argument 'updateResponse' may not be null.");
    }
    updateContent(
        updateResponse
            .getUpdatedMetacards()
            .stream()
            .map(Update::getNewMetacard)
            .collect(Collectors.toSet()),
        updateResponse.getRequest().getProperties());
    return updateResponse;
  }

  @Override
  public DeleteResponse process(DeleteResponse deleteResponse) throws PluginExecutionException {
    return deleteResponse;
  }

  private void updateContent(Set<Metacard> metacards, Map<String, Serializable> properties) {
    List<Metacard> metacardUpdates = new ArrayList<>();
    List<ContentItem> contentUpdates = new ArrayList<>();
    for (Metacard mcard : metacards) {
      if (shouldGenerateContentItems(mcard, properties)) {
        generateImages(mcard, metacardUpdates, contentUpdates);
      }
    }

    Map<String, Serializable> reprocessProperties = new HashMap<>();
    reprocessProperties.put(NITF_PROCESSING_KEY, true);

    if (!contentUpdates.isEmpty()) {
      UpdateStorageRequest updateStorageRequest =
          new UpdateStorageRequestImpl(contentUpdates, reprocessProperties);
      try {
        catalogFramework.update(updateStorageRequest);
      } catch (IngestException | SourceUnavailableException e) {
        LOGGER.debug("Error storing thumbnail/overview/original", e);
      }
    }

    if (!metacardUpdates.isEmpty()) {
      UpdateRequest updateRequest =
          new UpdateRequestImpl(
              metacardUpdates
                  .stream()
                  .map(
                      mcard ->
                          new AbstractMap.SimpleEntry<Serializable, Metacard>(mcard.getId(), mcard))
                  .collect(Collectors.toList()),
              Core.ID,
              reprocessProperties);
      try {
        catalogFramework.update(updateRequest);
      } catch (IngestException | SourceUnavailableException e) {
        LOGGER.debug("Error updating metacard thumbnail", e);
      }
    }
  }

  private boolean shouldGenerateContentItems(
      Metacard metacard, Map<String, Serializable> properties) {
    Attribute type = metacard.getAttribute(Media.TYPE);
    return type != null
        && IMAGE_NITF.equals(type.getValue())
        && !(boolean) properties.getOrDefault(NITF_PROCESSING_KEY, false);
  }

  private void generateImages(
      Metacard metacard, List<Metacard> metacardUpdates, List<ContentItem> contentUpdates) {
    ResourceResponse response;
    try {
      response = catalogFramework.getLocalResource(new ResourceRequestById(metacard.getId()));
    } catch (ResourceNotFoundException | ResourceNotSupportedException | IOException e) {
      LOGGER.debug("Error retrieving resource for thumbnail/overview/original creation", e);
      return;
    }

    byte[] originalThumbnail = metacard.getThumbnail();

    int contentCount = contentUpdates.size();
    process(metacard, response.getResource().getInputStream(), contentUpdates);

    if (contentCount == contentUpdates.size() && metacard.getThumbnail() != originalThumbnail) {
      metacardUpdates.add(metacard);
    }
  }

  private void process(Metacard metacard, InputStream input, List<ContentItem> contentItems) {
    try (InputStream source = input) {
      if (getResourceSizeInMB(metacard) > maxNitfSizeMB) {
        LOGGER.debug(
            "Skipping large ({} MB) content item: {}",
            getResourceSizeInMB(metacard),
            metacard.getId());
        return;
      }

      BufferedImage renderedImage = renderImageUsingOriginalDataModel(source);

      if (renderedImage != null) {
        addThumbnailToMetacard(metacard, renderedImage);

        if (createOverview) {
          ContentItem overviewContentItem =
              createDerivedImage(
                  metacard.getId(),
                  OVERVIEW,
                  renderedImage,
                  metacard,
                  calculateOverviewWidth(renderedImage),
                  calculateOverviewHeight(renderedImage));

          contentItems.add(overviewContentItem);
        }

        if (storeOriginalImage) {
          ContentItem originalImageContentItem =
              createOriginalImage(metacard.getId(), renderedImage, metacard);

          contentItems.add(originalImageContentItem);
        }
      }
    } catch (NumberFormatException e) {
      LOGGER.debug("Error getting resource size {}", e.getMessage(), e);
    } catch (IOException | NitfFormatException | RuntimeException e) {
      LOGGER.debug("Error creating and storing thumbnail/overview/original: {}", e.getMessage(), e);
    }
  }

  private BufferedImage renderImageUsingOriginalDataModel(InputStream source)
      throws NitfFormatException {

    return render(
        source,
        input -> {
          try {
            return input.getRight().renderToClosestDataModel(input.getLeft());
          } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e);
          }
          return null;
        });
  }

  private BufferedImage render(
      InputStream inputStream,
      Function<Pair<ImageSegment, NitfRenderer>, BufferedImage> imageSegmentFunction)
      throws NitfFormatException {

    final ThreadLocal<BufferedImage> bufferedImage = new ThreadLocal<>();

    if (inputStream != null) {
      NitfRenderer renderer = getNitfRenderer();
      nitfParserService
          .parseNitf(inputStream, true)
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
    }

    return bufferedImage.get();
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

  private ContentItem createDerivedImage(
      String id,
      String qualifier,
      BufferedImage image,
      Metacard metacard,
      int maxWidth,
      int maxHeight) {
    try {
      byte[] overviewBytes = scaleImage(image, maxWidth, maxHeight);

      ByteSource source = ByteSource.wrap(overviewBytes);
      ContentItem contentItem =
          new ContentItemImpl(
              id,
              qualifier,
              source,
              IMAGE_JPEG,
              buildDerivedImageTitle(metacard.getTitle(), qualifier, JPG),
              overviewBytes.length,
              metacard);

      addDerivedResourceAttribute(metacard, contentItem);

      return contentItem;
    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return null;
  }

  private ContentItem createOriginalImage(String id, BufferedImage image, Metacard metacard) {

    try {
      byte[] originalBytes = renderToJpeg2k(image);

      ByteSource source = ByteSource.wrap(originalBytes);
      ContentItem contentItem =
          new ContentItemImpl(
              id,
              ORIGINAL,
              source,
              IMAGE_JPEG2K,
              buildDerivedImageTitle(metacard.getTitle(), ORIGINAL, JP2),
              originalBytes.length,
              metacard);

      addDerivedResourceAttribute(metacard, contentItem);

      return contentItem;

    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return null;
  }

  private String buildDerivedImageTitle(String title, String qualifier, String extension) {
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

  private long getResourceSizeInMB(Metacard metacard) {
    return Long.parseLong(metacard.getResourceSize()) / MEGABYTE;
  }

  private void addDerivedResourceAttribute(Metacard metacard, ContentItem contentItem) {
    Attribute attribute = metacard.getAttribute(Core.DERIVED_RESOURCE_URI);
    if (attribute == null) {
      attribute = new AttributeImpl(Core.DERIVED_RESOURCE_URI, contentItem.getUri());
    } else {
      AttributeImpl newAttribute = new AttributeImpl(attribute);
      newAttribute.addValue(contentItem.getUri());
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

  NitfRenderer getNitfRenderer() {
    return new NitfRenderer();
  }

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setNitfParserService(NitfParserService nitfParserService) {
    this.nitfParserService = nitfParserService;
  }
}
