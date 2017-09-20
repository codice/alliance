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
package org.codice.alliance.libs.mpegts;

import java.io.IOException;
import java.util.function.Consumer;
import org.taktik.mpegts.MTSPacket;

public interface MpegTsDecoder {
  /**
   * Submit an MTSPacket for decoding. If the packet triggers the completion of a PESPacket, then
   * {@code callback} will be called.
   *
   * @param mtsPacket must be non-null
   * @param callback must be non-null
   * @throws IOException
   */
  void read(MTSPacket mtsPacket, Consumer<PESPacket> callback) throws IOException;
}
