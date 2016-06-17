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
package org.codice.alliance.imaging.chip.transformer;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.impl.BinaryContentImpl;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;
import ddf.catalog.transform.CatalogTransformerException;

/**
 * Performs various conversion functions required to wire the CatalogFramework interface to the
 * MetacardTransformer interface.
 */
public class CatalogOutputAdapter {

    private static final String IMAGE_JPG = "image/jpg";

    private static final String JPG = "jpg";

    /**
     * @param resourceResponse a ResourceResponse object returned by CatalogFramework.
     * @return the requested BufferedImage.
     * @throws IOException when there's a problem reading the image from the ResourceResponse
     *                     InputStream.
     */
    public BufferedImage getImage(ResourceResponse resourceResponse) throws IOException {
        validateArgument(resourceResponse, "resourceResponse");
        validateArgument(resourceResponse.getResource(), "resourceResponse.resource");
        validateObjectState(resourceResponse.getResource()
                .getInputStream(), "resourceResponse.resource.inputStream");

        Resource resource = resourceResponse.getResource();
        InputStream inputStream = resource.getInputStream();
        BufferedImage image = ImageIO.read(new BufferedInputStream(inputStream));
        return image;
    }

    /**
     * @param image the BufferedImage to be converted.
     * @return a BinaryContent object containing the image data.
     * @throws IOException            when the BufferedImage can't be written to temporary in-memory space.
     * @throws MimeTypeParseException
     */
    public BinaryContent getBinaryContent(BufferedImage image)
            throws IOException, MimeTypeParseException {
        validateArgument(image, "image");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(os);
        ImageIO.write(image, JPG, imageOutputStream);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());
        BinaryContent binaryContent = new BinaryContentImpl(fis, new MimeType(IMAGE_JPG));
        return binaryContent;
    }

    private void validateArgument(Object value, String argumentName) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("argument '%s' may not be null.",
                    argumentName));
        }
    }

    private void validateObjectState(Object value, String argumentName) {
        if (value == null) {
            throw new IllegalStateException(String.format("object property '%s' may not be null.",
                    argumentName));
        }
    }

    /**
     *
     * @param exception the exception to be wrapped.
     * @throws CatalogTransformerException in every case.
     */
    public void wrapException(Exception exception) throws CatalogTransformerException {
        throw new CatalogTransformerException(exception);
    }
}
