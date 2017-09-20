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

/**
 * Interface for objects that contains decoded stream data (H264 video data, H262 video data or
 * metadata).
 */
interface DecodedStreamData {

  /** @return MPEG-TS packet identifier */
  int getPacketId();

  /** @param visitor must be non-null */
  void accept(Visitor visitor);

  interface Visitor {
    /** @param decodedStreamData must be non-null */
    void visit(Mpeg2DecodedStreamData decodedStreamData);

    /** @param decodedStreamData must be non-null */
    void visit(Mpeg4DecodedStreamData decodedStreamData);
  }
}
