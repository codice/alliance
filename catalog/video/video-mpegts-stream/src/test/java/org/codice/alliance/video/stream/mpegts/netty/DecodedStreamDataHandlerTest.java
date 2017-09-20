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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.LinkedList;
import java.util.List;
import org.jcodec.codecs.h264.io.model.NALUnit;
import org.jcodec.codecs.h264.io.model.NALUnitType;
import org.junit.Before;
import org.junit.Test;

public class DecodedStreamDataHandlerTest {

  private PacketBuffer packetBuffer;

  @Before
  public void setup() {
    packetBuffer = mock(PacketBuffer.class);
  }

  @Test
  public void testWrongArgumentType() throws Exception {

    EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer));

    channel.writeInbound("not a DecodedStreamData.class");

    verify(packetBuffer, never()).frameComplete(any());
  }

  @Test
  public void testDetectIDR() throws Exception {

    List<NALUnit> nalUnitList = new LinkedList<>();
    NALUnit nalUnit = new NALUnit(NALUnitType.IDR_SLICE, 0);
    nalUnitList.add(nalUnit);

    Mpeg4DecodedStreamData decodedStreamData = mock(Mpeg4DecodedStreamData.class);
    when(decodedStreamData.getNalUnits()).thenReturn(nalUnitList);
    doCallRealMethod().when(decodedStreamData).accept(any());

    EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer));

    channel.writeInbound(decodedStreamData);

    verify(packetBuffer).frameComplete(PacketBuffer.FrameType.IDR);
  }

  @Test
  public void testDetectNonIDR() throws Exception {

    EmbeddedChannel channel = new EmbeddedChannel(new DecodedStreamDataHandler(packetBuffer));

    List<NALUnit> nalUnitList = new LinkedList<>();
    NALUnit nalUnit = new NALUnit(NALUnitType.NON_IDR_SLICE, 0);
    nalUnitList.add(nalUnit);

    Mpeg4DecodedStreamData decodedStreamData = mock(Mpeg4DecodedStreamData.class);
    when(decodedStreamData.getNalUnits()).thenReturn(nalUnitList);
    doCallRealMethod().when(decodedStreamData).accept(any());

    channel.writeInbound(decodedStreamData);

    verify(packetBuffer).frameComplete(PacketBuffer.FrameType.NON_IDR);
  }
}
