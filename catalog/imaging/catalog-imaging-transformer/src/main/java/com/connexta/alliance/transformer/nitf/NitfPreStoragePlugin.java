/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.transformer.nitf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.codice.imaging.nitf.core.AllDataExtractionParseStrategy;
import org.codice.imaging.nitf.core.NitfFileParser;
import org.codice.imaging.nitf.core.common.NitfInputStreamReader;
import org.codice.imaging.nitf.core.common.NitfReader;
import org.codice.imaging.nitf.core.image.NitfImageSegmentHeader;
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

    private static final String OVERVIEW_FILENAME_PATTERN = "%s-%s.%s";

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
        contentItems.forEach(contentItem -> process(contentItem).ifPresent(newContentItems::add));
        contentItems.addAll(newContentItems);
    }

    private Optional<ContentItem> process(ContentItem contentItem) {
        Metacard metacard = contentItem.getMetacard();

        if (!isNitfMimeType(contentItem.getMimeTypeRawData())) {
            LOGGER.debug("skipping content item: filename={} mimeType={}",
                    contentItem.getFilename(),
                    contentItem.getMimeTypeRawData());
            return Optional.empty();
        }

        try {
            BufferedImage renderedImage = renderImage(contentItem);

            if (renderedImage != null) {
                addThumbnailToMetacard(metacard, renderedImage);
                ContentItem overviewContentItem = createOverview(contentItem.getId(),
                        renderedImage,
                        metacard);

                return Optional.ofNullable(overviewContentItem);
            }
        } catch (IOException | ParseException e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return Optional.empty();
    }

    private BufferedImage renderImage(ContentItem contentItem) throws IOException, ParseException {
        NitfReader reader = new NitfInputStreamReader(contentItem.getInputStream());
        AllDataExtractionParseStrategy parsingStrategy = new AllDataExtractionParseStrategy();
        NitfFileParser.parse(reader, parsingStrategy);

        if (parsingStrategy.getImageSegmentData()
                .size() == 0) {
            return null;
        }

        NitfImageSegmentHeader header = parsingStrategy.getImageSegmentHeaders()
                .get(0);
        byte[] image = parsingStrategy.getImageSegmentData()
                .get(0);

        NitfRenderer renderer = new NitfRenderer();
        ImageInputStream inputStream = new MemoryCacheImageInputStream(new ByteArrayInputStream(
                image));
        return renderer.render(header, inputStream);
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

    private ContentItem createOverview(String id, BufferedImage image, Metacard metacard) {
        try {
            byte[] overviewBytes = scaleImage(image, image.getWidth(), image.getHeight());
            ByteSource source = ByteSource.wrap(overviewBytes);
            ContentItem contentItem = new ContentItemImpl(id,
                    OVERVIEW,
                    source,
                    IMAGE_JPEG,
                    buildOverviewTitle(metacard.getTitle()),
                    overviewBytes.length,
                    metacard);

            metacard.setAttribute(new AttributeImpl(Metacard.DERIVED_RESOURCE_URI,
                    contentItem.getUri()));

            return contentItem;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    private String buildOverviewTitle(String title) {
        String rootFileName = FilenameUtils.getBaseName(title);
        return String.format(OVERVIEW_FILENAME_PATTERN, OVERVIEW, rootFileName, JPG);
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
}
