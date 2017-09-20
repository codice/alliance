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

import static org.apache.commons.lang3.Validate.notNull;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import org.codice.alliance.libs.mpegts.MpegTsDecoder;
import org.codice.alliance.libs.mpegts.MpegTsDecoderImpl;
import org.taktik.mpegts.MTSPacket;

/** Converts a series of MTSPackets to PESPackets. */
class MTSPacketToPESPacketDecoder extends MessageToMessageDecoder<MTSPacket> {

  private final MpegTsDecoder mpegTsDecoder;

  public MTSPacketToPESPacketDecoder(MpegTsDecoder mpegTsDecoder) {
    this.mpegTsDecoder = mpegTsDecoder;
  }

  public MTSPacketToPESPacketDecoder() {
    this(new MpegTsDecoderImpl());
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, MTSPacket mtsPacket, List<Object> outputList)
      throws Exception {

    notNull(ctx, "ctx must be non-null");
    notNull(mtsPacket, "mtsPacket must be non-null");
    notNull(outputList, "outputList must be non-null");

    mpegTsDecoder.read(mtsPacket, outputList::add);
  }
}
