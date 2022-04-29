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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.impl.MetacardImpl;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.codice.alliance.libs.mpegts.MpegStreamType;
import org.codice.alliance.libs.mpegts.PESPacket;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.klv.KlvConsumer;
import org.junit.Before;
import org.junit.Test;

public class PESPacketToKLVPacketDecoderTest {

  private Context context;

  private KlvConsumer consumer;

  private MetacardImpl parent;

  private UdpStreamProcessor processor;

  private PESPacketToKLVPacketDecoder decoder;

  private Map<String, String> properties = new HashMap<>();

  @Before
  public void setUp() throws Exception {
    parent = new MetacardImpl();
    parent.setId("parentId");
    processor = mock(UdpStreamProcessor.class);
    when(processor.getAdditionalProperties()).thenReturn(properties);
    context = mock(Context.class);
    when(context.getUdpStreamProcessor()).thenReturn(processor);
    when(context.getParentMetacard()).thenReturn(Optional.of(parent));
    consumer = mock(KlvConsumer.class);
    decoder = new PESPacketToKLVPacketDecoder(consumer, context);
  }

  @Test
  public void acceptInboundMessageNonPesPacket() {
    assertThat(decoder.acceptInboundMessage("notpespacket"), is(false));
  }

  @Test
  public void acceptInboundMessageWrongStreamType() {
    PESPacket packet = new PESPacket(new byte[0], MpegStreamType.VIDEO_H264, 123);
    assertThat(decoder.acceptInboundMessage(packet), is(false));
  }

  @Test
  public void acceptInboundMessageMetaType() {
    PESPacket packet = new PESPacket(new byte[0], MpegStreamType.META_PES, 123);
    assertThat(decoder.acceptInboundMessage(packet), is(true));
  }

  @Test
  public void acceptInboundMessagePrivateType() {
    PESPacket packet = new PESPacket(new byte[0], MpegStreamType.PRIVATE_DATA, 123);
    assertThat(decoder.acceptInboundMessage(packet), is(true));
  }

  @Test
  public void decodeKlvPacket() throws Exception {
    byte[] packetBytes = IOUtils.readFully(getClass().getResourceAsStream("/pes-klv-packet"), 315);
    PESPacket packet = new PESPacket(packetBytes, MpegStreamType.PRIVATE_DATA, 123);
    ChannelHandlerContext chc = mock(ChannelHandlerContext.class);
    decoder.decode(chc, packet, new ArrayList<>());
    verify(consumer).postKlvForStream(any());
  }
}
