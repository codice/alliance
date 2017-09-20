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

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriter;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.codice.imaging.nitf.core.image.ImageCompression;
import org.codice.imaging.nitf.core.image.ImageMode;

class Jpeg2000ServiceImpl extends AbstractJpegService {

  @Override
  public byte[] createJpeg(BufferedImage bufferedImage, int blockWidth, int blockHeight)
      throws IOException {

    J2KImageWriter writer = createWriter();
    J2KImageWriteParam writeParams = (J2KImageWriteParam) writer.getDefaultWriteParam();
    setCommonWriteParams(writeParams);
    writeParams.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
    writeParams.setTiling(blockWidth, blockHeight, 0, 0);
    writeParams.setSOP(true);

    return encodeToByteArray(bufferedImage, writer, writeParams);
  }

  private byte[] encodeToByteArray(
      BufferedImage bufferedImage, J2KImageWriter writer, J2KImageWriteParam writeParams)
      throws IOException {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      try (ImageOutputStream ios = new MemoryCacheImageOutputStream(os)) {
        writer.setOutput(ios);
        writer.write(null, new IIOImage(bufferedImage, null, null), writeParams);
        writer.dispose();
      }
      return os.toByteArray();
    }
  }

  private J2KImageWriter createWriter() {
    return new J2KImageWriter(new J2KImageWriterSpi());
  }

  private void setCommonWriteParams(J2KImageWriteParam writeParams) {
    writeParams.setLossless(false);
    writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    writeParams.setCompressionType("JPEG2000");
    writeParams.setCompressionQuality(0.0f);
  }

  @Override
  public byte[] createJpeg(BufferedImage bufferedImage) throws IOException {

    J2KImageWriter writer = createWriter();
    J2KImageWriteParam writeParams = (J2KImageWriteParam) writer.getDefaultWriteParam();
    setCommonWriteParams(writeParams);

    return encodeToByteArray(bufferedImage, writer, writeParams);
  }

  @Override
  ImageCompression getImageCompressionType() {
    return ImageCompression.JPEG2000;
  }

  /**
   * http://isotc.iso.org/livelink/livelink/fetch/2000/2122/327993/327973/654328/6208440/documents/24n3111%20(BPJ2K0110).pdf
   *
   * <p>wxyz = JPEG 2000 lossy, where "wxyz" is the target or expected bitrate (in
   * bitsPerPixelPerBand) for the final layer of each tile. Note: When there is no decimal point,
   * the decimal point is implicit and assumed to be in the middle (i.e. wx.yz).
   */
  @Override
  String getImageCompressionRateString(double bitsPerPixelPerBand) {
    return String.format("%04d", Math.round(bitsPerPixelPerBand * 100));
  }

  @Override
  ImageMode getImageMode() {
    return ImageMode.BLOCKINTERLEVE;
  }
}
