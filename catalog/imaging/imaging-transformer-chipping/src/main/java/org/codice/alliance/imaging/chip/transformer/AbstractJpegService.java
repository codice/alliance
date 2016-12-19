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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStreamImpl;

import org.codice.imaging.nitf.core.image.ImageCompression;
import org.codice.imaging.nitf.core.image.ImageMode;
import org.codice.imaging.nitf.core.image.ImageSegment;

abstract class AbstractJpegService implements JpegService {

    private BufferedImage copyImage(BufferedImage source) {

        ColorModel colorModel = source.getColorModel();

        int width = source.getWidth();
        int height = source.getHeight();

        WritableRaster raster = source.getRaster()
                .createCompatibleWritableRaster(width, height);

        BufferedImage bufferedImage = new BufferedImage(colorModel,
                raster,
                colorModel.isAlphaPremultiplied(),
                null);

        Graphics g = bufferedImage.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }

    private ImageInputStreamImpl createImageInputStream(final byte[] jpegData) {
        return new ImageInputStreamImpl() {

            private InputStream inputStream = new ByteArrayInputStream(jpegData);

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return inputStream.read(b, off, len);
            }
        };
    }

    private void setImageDataLengthField(ImageSegment chipImageSegment, byte[] jpegData) {
        chipImageSegment.setDataLength(jpegData.length);
    }

    private void setImageDataField(ImageSegment chipImageSegment, final byte[] jpegData) {
        chipImageSegment.setData(createImageInputStream(jpegData));
    }

    abstract ImageCompression getImageCompressionType();

    abstract String getImageCompressionRateString(double actualBpppb);

    abstract ImageMode getImageMode();

    @Override
    public final void createJpeg(BufferedImage bufferedImage, ImageSegment chipImageSegment,
            boolean isBlocking, int blockWidth, int blockHeight) throws IOException {

        if (chipImageSegment.getNumBands() < 1) {
            throw new IllegalStateException("the number of bands must be set");
        }

        BufferedImage copy = copyImage(bufferedImage);

        byte[] jpegData;
        if (isBlocking) {
            jpegData = encodeBlocks(copy, blockWidth, blockHeight);
        } else {
            jpegData = encodeWholeImage(copy);
        }

        double bitsPerPixelPerBand = calculateActualBitsPerPixelPerBand(bufferedImage,
                chipImageSegment,
                jpegData);

        setImageCompressionTypeField(chipImageSegment);
        setCompresionRateField(chipImageSegment, bitsPerPixelPerBand);
        setImageDataField(chipImageSegment, jpegData);
        setImageDataLengthField(chipImageSegment, jpegData);
        setImageModeField(chipImageSegment);
    }

    private byte[] encodeBlocks(BufferedImage bufferedImage, int blockWidth, int blockHeight)
            throws IOException {
        return createJpeg(bufferedImage, blockWidth, blockHeight);
    }

    private byte[] encodeWholeImage(BufferedImage bufferedImage) throws IOException {
        return createJpeg(bufferedImage);
    }

    /**
     * bits per pixel per band
     */
    private double calculateActualBitsPerPixelPerBand(BufferedImage bufferedImage,
            ImageSegment chipImageSegment, byte[] jpegData) {
        return (getNumberOfBits(jpegData) / getTotalNumberOfPixels(bufferedImage))
                / getNumberOfBands(chipImageSegment);
    }

    private double getNumberOfBands(ImageSegment chipImageSegment) {
        return chipImageSegment.getNumBands();
    }

    private double getTotalNumberOfPixels(BufferedImage bufferedImage) {
        return bufferedImage.getWidth() * bufferedImage.getHeight();
    }

    private double getNumberOfBits(byte[] jpegData) {
        return bytesToBits(jpegData.length);
    }

    private void setImageModeField(ImageSegment chipImageSegment) {
        chipImageSegment.setImageMode(getImageMode());
    }

    private void setImageCompressionTypeField(ImageSegment chipImageSegment) {
        chipImageSegment.setImageCompression(getImageCompressionType());
    }

    private void setCompresionRateField(ImageSegment chipImageSegment, double bitsPerPixelPerBand) {
        chipImageSegment.setCompressionRate(getImageCompressionRateString(bitsPerPixelPerBand));
    }

    private long bytesToBits(long bytes) {
        return bytes * 8;
    }

}
