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

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.codice.imaging.nitf.core.image.ImageSegment;

/** Service to convert a BufferedImage into JPEG image data. */
public interface JpegService {

  /**
   * Convert a buffered image into JPEG binary data.
   *
   * @param bufferedImage must be non-null
   * @return binary data
   * @throws IOException
   */
  byte[] createJpeg(BufferedImage bufferedImage) throws IOException;

  /**
   * Convert a buffered image into blocks of JPEG binary data.
   *
   * @param bufferedImage must be non-null
   * @param blockWidth the block width
   * @param blockHeight the block height
   * @return binary data
   * @throws IOException
   */
  byte[] createJpeg(BufferedImage bufferedImage, int blockWidth, int blockHeight)
      throws IOException;

  /**
   * Encode a buffered image into binary data and set the appropriate fields in the image segment.
   *
   * @param bufferedImage must be non-null
   * @param chipImageSegment must be non-null
   * @param isBlocking true if the imaging should be blocked
   * @param blockWidth the block width
   * @param blockHeight the block height
   * @throws IOException
   */
  void createJpeg(
      BufferedImage bufferedImage,
      ImageSegment chipImageSegment,
      boolean isBlocking,
      int blockWidth,
      int blockHeight)
      throws IOException;
}
