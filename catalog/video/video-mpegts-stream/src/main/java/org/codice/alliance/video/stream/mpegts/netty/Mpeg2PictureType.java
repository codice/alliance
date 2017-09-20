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
package org.codice.alliance.video.stream.mpegts.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Mpeg2PictureType {
  INTRA_CODED(1),
  PREDICTIVE_CODED(2),
  BIDIRECTIONALLY_PREDICTIVE_CODED(3);

  private static final Map<Long, Mpeg2PictureType> LOOKUP = new HashMap<>();

  static {
    for (Mpeg2PictureType mpeg2PictureType : values()) {
      LOOKUP.put(mpeg2PictureType.h262HeaderValue, mpeg2PictureType);
    }
  }

  /** This is the value that appears in an H262 encoded stream. */
  private final long h262HeaderValue;

  Mpeg2PictureType(long h262HeaderValue) {
    this.h262HeaderValue = h262HeaderValue;
  }

  /**
   * Find the Mpeg2PictureType that corresponds to the value encoded in an H262 header.
   *
   * @param h262HeaderValue header value
   * @return Mpeg2PictureType or empty
   */
  public static Optional<Mpeg2PictureType> fromH262HeaderValue(long h262HeaderValue) {
    return Optional.ofNullable(LOOKUP.getOrDefault(h262HeaderValue, null));
  }
}
