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
package org.codice.alliance.transformer.nitf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import ddf.catalog.plugin.PluginExecutionException;
import net.coobird.thumbnailator.Thumbnails;

/**
 * This pre-storage plugin creates and stores the NITF thumbnail and NITF overview images.  The
 * thumbnail is stored with the Metacard while the overview is stored in the content store.
 */
public class NitfPreStoragePlugin implements PreCreateStoragePlugin, PreUpdateStoragePlugin {

    private static final String IMAGE_JPEG = "image/jpeg";

    private static final int THUMBNAIL_WIDTH = 200;

    private static final int THUMBNAIL_HEIGHT = 200;

    private static final String JPG = "jpg";

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfPreStoragePlugin.class);

    private static final String OVERVIEW = "overview";

    private static final String ORIGINAL = "original";

    private static final String DERIVED_IMAGE_FILENAME_PATTERN = "%s-%s.%s";

    private static final double DEFAULT_MAX_SIDE_LENGTH = 1024.0;

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
            return NitfInputTransformer.MIME_TYPE.match(rawMimeType);
        } catch (MimeTypeParseException e) {
            LOGGER.warn("unable to compare mime types: {} vs {}",
                    NitfInputTransformer.MIME_TYPE,
                    rawMimeType);
        }
        return false;
    }

    private void process(List<ContentItem> contentItems) {
        List<ContentItem> newContentItems = new LinkedList<>();
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
            BufferedImage renderedImage = renderImage(contentItem);

            if (renderedImage != null) {
                addThumbnailToMetacard(metacard, renderedImage);

                ContentItem overviewContentItem = createDerivedImage(contentItem.getId(),
                        OVERVIEW,
                        renderedImage,
                        metacard,
                        calculateOverviewWidth(renderedImage),
                        calculateOverviewHeight(renderedImage));

                contentItems.add(overviewContentItem);

                ContentItem originalImageContentItem = createDerivedImage(contentItem.getId(),
                        ORIGINAL,
                        renderedImage,
                        metacard,
                        renderedImage.getWidth(),
                        renderedImage.getHeight());

                contentItems.add(originalImageContentItem);
            }
        } catch (IOException | ParseException | NitfFormatException e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return;
    }

    private BufferedImage renderImage(ContentItem contentItem)
            throws IOException, ParseException, NitfFormatException {

        final ThreadLocal<BufferedImage> bufferedImage = new ThreadLocal<>();

        if (contentItem != null && contentItem.getInputStream() != null) {
            NitfRenderer renderer = new NitfRenderer();

            new NitfParserInputFlow().inputStream(contentItem.getInputStream())
                    .allData()
                    .forEachImageSegment(segment -> {
                        if (bufferedImage.get() == null) {
                            try {
                                bufferedImage.set(renderer.render(segment));
                            } catch (IOException e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                    });
        }

        return bufferedImage.get();
    }

    private void addThumbnailToMetacard(Metacard metacard, BufferedImage bufferedImage) {
        try {
            byte[] thumbnailImage = scaleImage(bufferedImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

            if (thumbnailImage.length > 0) {
                metacard.setAttribute(new AttributeImpl(Metacard.THUMBNAIL, thumbnailImage));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
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
                    buildDerivedImageTitle(metacard.getTitle(), qualifier),
                    overviewBytes.length,
                    metacard);

            addDerivedResourceAttribute(metacard, contentItem);

            return contentItem;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    private void addDerivedResourceAttribute(Metacard metacard, ContentItem contentItem) {
        Attribute attribute = metacard.getAttribute(Metacard.DERIVED_RESOURCE_URI);

        if (attribute == null) {
            attribute = new AttributeImpl(Metacard.DERIVED_RESOURCE_URI, contentItem.getUri());
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
            int newHeight = (int) Math.round(((height * (maxSideLength / width))));
            return newHeight;
        }

        return Math.min(height, (int) maxSideLength);
    }

    private int calculateOverviewWidth(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        if (width >= height) {
            return Math.min(width, (int) maxSideLength);
        }

        int newWidth = (int) Math.round(width * (maxSideLength / height));
        return newWidth;
    }

    private String buildDerivedImageTitle(String title, String qualifier) {
        String rootFileName = FilenameUtils.getBaseName(title);
        return String.format(DERIVED_IMAGE_FILENAME_PATTERN, qualifier, rootFileName, JPG);
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

    public void setMaxSideLength(int maxSideLength) {
        if (maxSideLength > 0) {
            this.maxSideLength = maxSideLength;
        } else {
            LOGGER.warn(
                    "method argument 'maxSideLength' must be greater than 0.  Using default value instead.");
            return;
        }

        LOGGER.debug(String.format("Setting maxSideLength to: %s", maxSideLength));
        this.maxSideLength = maxSideLength;
    }
}
