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

import static org.apache.commons.lang3.Validate.notNull;

import com.google.common.io.ByteSource;
import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TSStream {

  /**
   * Create a stream of PESPackets from a byte source.
   *
   * @param byteSource must be non-null
   * @return stream of PESPackets
   * @throws IOException
   */
  public static Stream<PESPacket> from(ByteSource byteSource) throws IOException {
    notNull(byteSource, "byteSource must be non-null");
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            new PESPacketIterator(byteSource), Spliterator.ORDERED | Spliterator.NONNULL),
        false);
  }
}
