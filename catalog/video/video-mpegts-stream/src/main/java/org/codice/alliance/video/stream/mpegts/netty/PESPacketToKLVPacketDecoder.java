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
import static org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET;
import static org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET_CONTEXT;
import static org.codice.alliance.video.stream.mpegts.klv.KlvData.PARENT_ID_KEY;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.alliance.libs.mpegts.MpegStreamType;
import org.codice.alliance.libs.mpegts.PESPacket;
import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.codice.alliance.libs.stanag4609.PESUtilities;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.klv.KlvConsumer;
import org.codice.alliance.video.stream.mpegts.klv.KlvData;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.KlvDecoder;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PESPacketToKLVPacketDecoder extends MessageToMessageDecoder<PESPacket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PESPacketToKLVPacketDecoder.class);

  private final KlvDecoder decoder;

  private final KlvConsumer klvService;

  private final Context context;

  private Map<String, String> properties = new HashMap<>();

  public PESPacketToKLVPacketDecoder(
      KlvDecoder klvDecoder, KlvConsumer klvService, Context context) {
    this.decoder = klvDecoder;
    this.klvService = klvService;
    this.context = context;
    properties.putAll(context.getUdpStreamProcessor().getAdditionalProperties());
    context
        .getParentMetacard()
        .ifPresent(metacard -> properties.put(PARENT_ID_KEY, metacard.getId()));
  }

  public PESPacketToKLVPacketDecoder(KlvConsumer service, Context context) {
    this(new KlvDecoder(UAS_DATALINK_LOCAL_SET_CONTEXT), service, context);
  }

  @Override
  public boolean acceptInboundMessage(Object msg) {
    return klvService != null
        && msg instanceof PESPacket
        && (((PESPacket) msg).getStreamType() == MpegStreamType.PRIVATE_DATA
            || ((PESPacket) msg).getStreamType() == MpegStreamType.META_PES);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, PESPacket pesPacket, List<Object> outputList)
      throws Exception {

    notNull(ctx, "ctx must be non-null");
    notNull(pesPacket, "pesPacket must be non-null");
    notNull(outputList, "outputList must be non-null");

    if (properties.containsKey(PARENT_ID_KEY)) {
      context
          .getParentMetacard()
          .ifPresent(metacard -> properties.put(PARENT_ID_KEY, metacard.getId()));
    }
    if (klvService == null || !properties.containsKey(PARENT_ID_KEY)) {
      return;
    }
    if (pesPacket.getStreamType() == MpegStreamType.PRIVATE_DATA
        || pesPacket.getStreamType() == MpegStreamType.META_PES) {
      DecodedKLVMetadataPacket klvPacket;
      try {
        klvPacket = PESUtilities.handlePESPacketBytes(pesPacket.getPayload(), decoder);
      } catch (KlvDecodingException e) {
        LOGGER.debug("Couldn't decode KLV packet. Skipping...");
        return;
      }
      if (klvPacket != null) {
        KlvDataElement<KlvContext> klvData =
            klvPacket.getDecodedKLV().getDataElementByName(UAS_DATALINK_LOCAL_SET);
        if (klvData != null) {
          klvService.postKlvForStream(
              new KlvData(klvData.getValue().getDataElements(), properties));
        }
      }
    }
    outputList.add(pesPacket);
  }
}
