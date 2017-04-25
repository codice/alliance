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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.render.NitfRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriter;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;
import com.google.common.io.ByteSource;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.plugin.PluginExecutionException;
import net.coobird.thumbnailator.Thumbnails;

/**
 * This {@link PostProcessPlugin} creates and stores the NITF thumbnail, overview, and original
 * images. The thumbnail is stored in the {@link Metacard} of the {@link ProcessResourceItem}, while
 * the overview and original may be rendered and stored in the content store.
 */
public class NitfRenderPlugin implements PostProcessPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfRenderPlugin.class);

    private static final String NULL_INPUT_MSG = "Unable to process null input.";

    private static final String IMAGE_NITF = "image/nitf";

    static final MimeType NITF_MIME_TYPE;

    static {
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());

        try {
            NITF_MIME_TYPE = new MimeType(IMAGE_NITF);
        } catch (MimeTypeParseException e) {
            throw new ExceptionInInitializerError(String.format(
                    "Unable to create MimeType from '%s': %s",
                    IMAGE_NITF,
                    e.getMessage()));
        }
    }

    private static final String JPG = "jpg";

    public static final String ORIGINAL = "original";

    public static final String OVERVIEW = "overview";

    private static final double DEFAULT_OVERVIEW_MAX_SIDE_LENGTH = 1024.0;

    private double overviewMaxSideLength = DEFAULT_OVERVIEW_MAX_SIDE_LENGTH;

    private static final int DEFAULT_MAX_NITF_SIZE_MB = 120;

    private int maxNitfSizeMB = DEFAULT_MAX_NITF_SIZE_MB;

    private boolean storeOriginalImage = true;

    private boolean createOverview = true;

    @Override
    public ProcessRequest<ProcessCreateItem> processCreate(
            ProcessRequest<ProcessCreateItem> processRequest) throws PluginExecutionException {
        return processRequest(processRequest);
    }

    @Override
    public ProcessRequest<ProcessUpdateItem> processUpdate(
            ProcessRequest<ProcessUpdateItem> processRequest) throws PluginExecutionException {
        return processRequest(processRequest);
    }

    @Override
    public ProcessRequest<ProcessDeleteItem> processDelete(
            ProcessRequest<ProcessDeleteItem> processRequest) throws PluginExecutionException {
        if (processRequest == null) {
            throw new PluginExecutionException(NULL_INPUT_MSG);
        }

        // no work to do
        return processRequest;
    }

    private static boolean isNitfMimeType(String rawMimeType) {
        try {
            return NITF_MIME_TYPE.match(rawMimeType);
        } catch (MimeTypeParseException e) {
            LOGGER.debug("Unable to compare mime types: {} vs {}. NITF image will not be processed.",
                    NITF_MIME_TYPE,
                    rawMimeType,
                    e);
        }

        return false;
    }

    private <T extends ProcessResourceItem> ProcessRequest<T> processRequest(
            ProcessRequest<T> input) throws PluginExecutionException {
        if (input == null) {
            throw new PluginExecutionException(NULL_INPUT_MSG);
        }

        List<T> processResourceItems = input.getProcessItems();

        if (processResourceItems != null) {
            List<T> newProcessResourceItems = new ArrayList<>();

            for (T processResourceItem : processResourceItems) {
                ProcessResource processResource = processResourceItem.getProcessResource();
                Metacard metacard = processResourceItem.getMetacard();

                if (processResource == null || !isNitfMimeType(processResource.getMimeType())) {
                    // skip processing
                    break;
                }

                final long megabyte = 1024L * 1024L;
                if (processResource.getSize() / megabyte > maxNitfSizeMB) {
                    LOGGER.debug("Skipping large ({} MB) process resource: name={}.",
                            processResource.getSize() / megabyte,
                            processResource.getName());
                    // skip processing
                    break;
                }

                try {
                    final BufferedImage renderedImage = render(processResource);

                    if (renderedImage != null) {
                        // thumbnail
                        addThumbnailToMetacard(metacard, renderedImage);

                        // overview
                        if (createOverview) {
                            try {
                                ProcessResource overviewImageProcessResource = createOverviewImage(
                                        metacard.getId(),
                                        renderedImage,
                                        metacard,
                                        calculateOverviewWidth(renderedImage),
                                        calculateOverviewHeight(renderedImage));

                                T overviewProcessResourceItem = createResourceItem(
                                        processResourceItem,
                                        overviewImageProcessResource);
                                newProcessResourceItems.add(overviewProcessResourceItem);
                                LOGGER.trace(
                                        "Successfully rendered and stored {} image for NITF with id={}",
                                        OVERVIEW,
                                        metacard.getId());
                            } catch (IOException e) {
                                LOGGER.debug("Unable to generate overview image from NITF.", e);
                            }
                        }

                        // original
                        if (storeOriginalImage) {
                            try {
                                ProcessResource originalImageProcessResource = createOriginalImage(
                                        metacard.getId(),
                                        renderedImage,
                                        metacard);

                                T originalProcessResourceItem = createResourceItem(
                                        processResourceItem,
                                        originalImageProcessResource);
                                newProcessResourceItems.add(originalProcessResourceItem);
                                LOGGER.trace(
                                        "Successfully rendered and stored {} image for NITF with id={}",
                                        ORIGINAL,
                                        metacard.getId());
                            } catch (IOException | RuntimeException e) {
                                LOGGER.debug("Unable to generate original image from NITF for metacard with id={}.", metacard.getId(), e);
                            }
                        }
                    }
                } catch (IOException | NitfFormatException | RuntimeException e) {
                    LOGGER.debug("Unable to render image from NITF.", e);
                }
            }

            processResourceItems.addAll(newProcessResourceItems);
        }

        return input;
    }

    private static <T extends ProcessResourceItem> T createResourceItem(T processResourceItem,
            ProcessResource processResource) throws PluginExecutionException {
        T newProcessResourceItem;
        if (processResourceItem instanceof ProcessCreateItem) {
            newProcessResourceItem = (T) new ProcessCreateItemImpl(processResource,
                    processResourceItem.getMetacard());
        } else if (processResourceItem instanceof ProcessUpdateItem) {
            newProcessResourceItem = (T) new ProcessUpdateItemImpl(processResource,
                    processResourceItem.getMetacard(),
                    ((ProcessUpdateItem) processResourceItem).getOldMetacard());
        } else {
            throw new PluginExecutionException(String.format(
                    "Unable to create new ResourceItem from %s.",
                    processResourceItem.getClass()
                            .getName()));
        }

        addDerivedResourceAttribute(newProcessResourceItem);
        return newProcessResourceItem;
    }

    private BufferedImage render(ProcessResource processResource)
            throws IOException, NitfFormatException {
        final ThreadLocal<BufferedImage> bufferedImage = new ThreadLocal<>();

        if (processResource != null) {
            try (TemporaryFileBackedOutputStream tfbos = new TemporaryFileBackedOutputStream()) {
                IOUtils.copyLarge(processResource.getInputStream(), tfbos);

                try (InputStream inputStream = tfbos.asByteSource()
                        .openBufferedStream()) {
                    NitfRenderer renderer = getNitfRenderer();

                    new NitfParserInputFlow().inputStream(inputStream)
                            .allData()
                            .forEachImageSegment(segment -> {
                                if (bufferedImage.get() == null) {
                                    try {
                                        BufferedImage bi =
                                                renderer.renderToClosestDataModel(segment);
                                        if (bi != null) {
                                            bufferedImage.set(bi);
                                        }
                                    } catch (IOException e) {
                                        LOGGER.debug(e.getMessage(), e);
                                    }
                                }
                            })
                            .end();
                }
            }
        }

        return bufferedImage.get();
    }

    private void addThumbnailToMetacard(Metacard metacard, BufferedImage bufferedImage) {
        try {
            final int thumbnailWidth = 200;
            final int thumbnailHeight = 200;
            byte[] thumbnailImage = scaleImage(bufferedImage, thumbnailWidth, thumbnailHeight);

            if (thumbnailImage.length > 0) {
                metacard.setAttribute(new AttributeImpl(Core.THUMBNAIL, thumbnailImage));
            }
        } catch (IOException e) {
            LOGGER.debug("Unable to add thumbnail to metacard with id={}.", metacard.getId(), e);
        }
    }

    private static ProcessResource createOverviewImage(String id, BufferedImage image,
            Metacard metacard, int maxWidth, int maxHeight) throws IOException {
        byte[] overviewBytes = scaleImage(image, maxWidth, maxHeight);

        ByteSource source = ByteSource.wrap(overviewBytes);
        return new ProcessResourceImpl(id,
                source.openBufferedStream(),
                "image/jpeg",
                buildDerivedImageTitle(metacard.getTitle(), OVERVIEW, JPG),
                overviewBytes.length,
                OVERVIEW);
    }

    private static ProcessResource createOriginalImage(String id, BufferedImage image,
            Metacard metacard) throws IOException {
        byte[] originalBytes = renderToJpeg2k(image);

        ByteSource source = ByteSource.wrap(originalBytes);
        return new ProcessResourceImpl(id,
                source.openBufferedStream(),
                "image/jp2",
                buildDerivedImageTitle(metacard.getTitle(), ORIGINAL, "jp2"),
                originalBytes.length,
                ORIGINAL);
    }

    private static String buildDerivedImageTitle(String title, String qualifier, String extension) {
        String rootFileName = FilenameUtils.getBaseName(title);

        // title must contain some alphanumeric, human readable characters, or use default filename
        // non-word characters equivalent to [^a-zA-Z0-9_]
        final Pattern filenameCharacterRegex = Pattern.compile("[^A-Za-z0-9]");
        if (StringUtils.isNotBlank(rootFileName)
                && StringUtils.isNotBlank(filenameCharacterRegex.matcher(rootFileName)
                .replaceAll(""))) {
            final Pattern invalidFilenameCharacterRegex = Pattern.compile("[\\W]");
            String strippedFilename = invalidFilenameCharacterRegex.matcher(rootFileName)
                    .replaceAll("");
            return String.format("%s-%s.%s", qualifier, strippedFilename, extension)
                    .toLowerCase();
        }

        return String.format("%s.%s", qualifier, JPG)
                .toLowerCase();
    }

    private static byte[] scaleImage(final BufferedImage bufferedImage, int width, int height)
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

    private static byte[] renderToJpeg2k(final BufferedImage bufferedImage) throws IOException {
        BufferedImage imageToCompress = bufferedImage;

        final int argbComponentCount = 4;
        if (bufferedImage.getColorModel()
                .getNumComponents() == argbComponentCount) {

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

    private static void addDerivedResourceAttribute(ProcessResourceItem processResourceItem) {
        final Metacard metacard = processResourceItem.getMetacard();
        final String processResourceUri = processResourceItem.getProcessResource()
                .getUri()
                .toString();

        Attribute attribute = metacard.getAttribute(Core.DERIVED_RESOURCE_URI);
        if (attribute == null) {
            attribute = new AttributeImpl(Core.DERIVED_RESOURCE_URI, processResourceUri);
        } else {
            AttributeImpl newAttribute = new AttributeImpl(attribute);
            newAttribute.addValue(processResourceUri);
            attribute = newAttribute;
        }

        metacard.setAttribute(attribute);
    }

    private int calculateOverviewHeight(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        if (width >= height) {
            return (int) Math.round(height * (overviewMaxSideLength / width));
        }

        return Math.min(height, (int) overviewMaxSideLength);
    }

    private int calculateOverviewWidth(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        if (width >= height) {
            return Math.min(width, (int) overviewMaxSideLength);
        }

        return (int) Math.round(width * (overviewMaxSideLength / height));
    }

    public void setOverviewMaxSideLength(int overviewMaxSideLength) {
        if (overviewMaxSideLength > 0) {
            LOGGER.trace("Setting derived image overviewMaxSideLength to {}", overviewMaxSideLength);
            this.overviewMaxSideLength = overviewMaxSideLength;
        } else {
            LOGGER.debug(
                    "Invalid `overviewMaxSideLength` value [{}], must be greater than zero. Default value [{}] will be used instead.",
                    overviewMaxSideLength,
                    DEFAULT_OVERVIEW_MAX_SIDE_LENGTH);
            this.overviewMaxSideLength = DEFAULT_OVERVIEW_MAX_SIDE_LENGTH;
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