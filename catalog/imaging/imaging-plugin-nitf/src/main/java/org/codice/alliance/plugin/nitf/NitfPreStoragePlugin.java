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
package org.codice.alliance.plugin.nitf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.fluent.impl.NitfParserInputFlowImpl;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriter;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;
import com.google.common.io.ByteSource;

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.content.plugin.PreCreateStoragePlugin;
import ddf.catalog.content.plugin.PreUpdateStoragePlugin;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.plugin.PluginExecutionException;
import net.coobird.thumbnailator.Thumbnails;

/**
 * This pre-storage plugin creates and stores the NITF thumbnail and NITF overview images. The
 * thumbnail is stored with the Metacard while the overview is stored in the content store.
 */
public class NitfPreStoragePlugin implements PreCreateStoragePlugin, PreUpdateStoragePlugin {

    private static final String IMAGE_NITF = "image/nitf";

    static final MimeType NITF_MIME_TYPE;

    static {
        try {
            NITF_MIME_TYPE = new MimeType(IMAGE_NITF);
        } catch (MimeTypeParseException e) {
            throw new ExceptionInInitializerError(String.format(
                    "Unable to create MimeType from '%s': %s", IMAGE_NITF, e.getMessage()));
        }
    }


    private static final String IMAGE_JPEG = "image/jpeg";

    private static final String IMAGE_JPEG2K = "image/jp2";

    private static final int THUMBNAIL_WIDTH = 200;

    private static final int THUMBNAIL_HEIGHT = 200;

    private static final long  MEGABYTE = 1024L * 1024L;

    private static final String JPG = "jpg";

    private static final String JP2 = "jp2";

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfPreStoragePlugin.class);

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

    static {
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    private double maxSideLength = DEFAULT_MAX_SIDE_LENGTH;

    @Override
    public CreateStorageRequest process(CreateStorageRequest createStorageRequest)
            throws PluginExecutionException {
        if (createStorageRequest == null) {
            throw new PluginExecutionException(
                    "process(): argument 'createStorageRequest' may not be null.");
        }

        process(createStorageRequest.getContentItems());
        return createStorageRequest;
    }

    @Override
    public UpdateStorageRequest process(UpdateStorageRequest updateStorageRequest)
            throws PluginExecutionException {
        if (updateStorageRequest == null) {
            throw new PluginExecutionException(
                    "process(): argument 'updateStorageRequest' may not be null.");
        }

        process(updateStorageRequest.getContentItems());
        return updateStorageRequest;
    }

    private boolean isNitfMimeType(String rawMimeType) {
        try {
            return NITF_MIME_TYPE.match(rawMimeType);
        } catch (MimeTypeParseException e) {
            LOGGER.debug("unable to compare mime types: {} vs {}",
                    NITF_MIME_TYPE,
                    rawMimeType);
        }

        return false;
    }

    private void process(List<ContentItem> contentItems) {
        List<ContentItem> newContentItems = new ArrayList<>();
        contentItems.forEach(contentItem -> process(contentItem, newContentItems));
        contentItems.addAll(newContentItems);
    }

    private void process(ContentItem contentItem, List<ContentItem> contentItems) {
        Metacard metacard = contentItem.getMetacard();

        if (!isNitfMimeType(contentItem.getMimeTypeRawData())) {
            LOGGER.debug("skipping content item: filename={} mimeType={}",
                    contentItem.getFilename(),
                    contentItem.getMimeTypeRawData());
            return;
        }

        try {
            if (contentItem.getSize() / MEGABYTE > maxNitfSizeMB) {
                LOGGER.debug("Skipping large ({} MB) content item: filename={}",
                        contentItem.getSize() / MEGABYTE,
                        contentItem.getFilename());
                return;
            }

            BufferedImage renderedImage = renderImage(contentItem);

            if (renderedImage != null) {
                addThumbnailToMetacard(metacard, renderedImage);

                if(createOverview) {
                    ContentItem overviewContentItem = createDerivedImage(contentItem.getId(),
                            OVERVIEW,
                            renderedImage,
                            metacard,
                            calculateOverviewWidth(renderedImage),
                            calculateOverviewHeight(renderedImage));

                    contentItems.add(overviewContentItem);
                }

                if(storeOriginalImage) {
                    ContentItem originalImageContentItem = createOriginalImage(contentItem.getId(),
                            renderImageUsingOriginalDataModel(contentItem),
                            metacard);

                    contentItems.add(originalImageContentItem);
                }

            }
        } catch (IOException | ParseException | NitfFormatException | RuntimeException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    private BufferedImage renderImage(ContentItem contentItem)
            throws IOException, ParseException, NitfFormatException {

        return render(contentItem, input -> {
            try {
                return input.getRight()
                        .render(input.getLeft());
            } catch (IOException e) {
                LOGGER.debug(e.getMessage(), e);
            }
            return null;
        });

    }

    private BufferedImage renderImageUsingOriginalDataModel(ContentItem contentItem)
            throws IOException, ParseException, NitfFormatException {

        return render(contentItem, input -> {
            try {
                return input.getRight()
                        .renderToClosestDataModel(input.getLeft());
            } catch (IOException e) {
                LOGGER.debug(e.getMessage(), e);
            }
            return null;
        });

    }

    private BufferedImage render(ContentItem contentItem,
            Function<Pair<ImageSegment, NitfRenderer>, BufferedImage> imageSegmentFunction)
            throws IOException, ParseException, NitfFormatException {

        final ThreadLocal<BufferedImage> bufferedImage = new ThreadLocal<>();

        if (contentItem != null) {
            InputStream inputStream = contentItem.getInputStream();

            if (inputStream != null) {
                try {
                    NitfRenderer renderer = getNitfRenderer();

                    new NitfParserInputFlowImpl().inputStream(inputStream)
                            .allData()
                            .forEachImageSegment(segment -> {
                                if (bufferedImage.get() == null) {
                                    BufferedImage bi =
                                            imageSegmentFunction.apply(new ImmutablePair<>(segment,
                                                    renderer));
                                    if (bi != null) {
                                        bufferedImage.set(bi);
                                    }
                                }
                            })
                            .end();
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
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

    private ContentItem createDerivedImage(String id, String qualifier, BufferedImage image,
            Metacard metacard, int maxWidth, int maxHeight) {
        try {
            byte[] overviewBytes = scaleImage(image, maxWidth, maxHeight);

            ByteSource source = ByteSource.wrap(overviewBytes);
            ContentItem contentItem = new ContentItemImpl(id,
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
            ContentItem contentItem = new ContentItemImpl(id,
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
        if (StringUtils.isNotBlank(rootFileName) && StringUtils.isNotBlank(rootFileName.replaceAll(
                "[^A-Za-z0-9]",
                ""))) {
            String strippedFilename = rootFileName.replaceAll(INVALID_FILENAME_CHARACTER_REGEX, "");
            return String.format(DERIVED_IMAGE_FILENAME_PATTERN,
                    qualifier,
                    strippedFilename,
                    extension)
                    .toLowerCase();
        }

        return String.format("%s.%s", qualifier, JPG)
                .toLowerCase();
    }

    private byte[] scaleImage(final BufferedImage bufferedImage, int width, int height)
            throws IOException {
        BufferedImage thumbnail = Thumbnails.of(bufferedImage)
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

        if (bufferedImage.getColorModel()
                .getNumComponents() == ARGB_COMPONENT_COUNT) {

            imageToCompress = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR);

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
}
