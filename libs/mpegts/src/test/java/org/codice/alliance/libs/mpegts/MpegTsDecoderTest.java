/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.libs.mpegts;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.jcodec.containers.mps.MTSUtils;
import org.jcodec.containers.mps.psi.PMTSection;
import org.junit.Test;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.PATSection;

public class MpegTsDecoderTest {

    @Test
    public void testRead() {

        MTSUtils.StreamType streamType = MTSUtils.StreamType.VIDEO_H264;

        byte expectedByte1 = 0x01;
        byte expectedByte2 = 0x02;
        byte expectedByte3 = 0x03;
        byte expectedByte4 = 0x04;

        int programMapTableId = 1;
        int videoPacketId = 2;

        MpegTsDecoderImpl decoder = new MpegTsDecoderImpl();

        PATSection patSection = mock(PATSection.class);
        when(patSection.getPrograms()).thenReturn(Collections.singletonMap(1, programMapTableId));

        MpegTsDecoderImpl.PATSectionParser patSectionParser =
                mock(MpegTsDecoderImpl.PATSectionParser.class);
        when(patSectionParser.parse(any())).thenReturn(patSection);
        decoder.setPatSectionParser(patSectionParser);

        MTSPacket programAssociationTablePacket = mock(MTSPacket.class);
        when(programAssociationTablePacket.getPid()).thenReturn(Constants.PROGRAM_ASSOCIATION_TABLE_PID);
        when(programAssociationTablePacket.isPayloadUnitStartIndicator()).thenReturn(true);
        when(programAssociationTablePacket.getPayload()).thenReturn(ByteBuffer.wrap(new byte[] {
                0x00}));

        PMTSection.PMTStream pmtStream = mock(PMTSection.PMTStream.class);
        when(pmtStream.getStreamType()).thenReturn(streamType);
        when(pmtStream.getPid()).thenReturn(videoPacketId);

        PMTSection pmtSection = mock(PMTSection.class);
        when(pmtSection.getStreams()).thenReturn(new PMTSection.PMTStream[] {pmtStream});

        MpegTsDecoderImpl.PMTSectionParser pmtSectionParser =
                mock(MpegTsDecoderImpl.PMTSectionParser.class);
        when(pmtSectionParser.parse(any())).thenReturn(pmtSection);
        decoder.setPmtSectionParser(pmtSectionParser);

        MTSPacket programMapTablePacket = mock(MTSPacket.class);
        when(programMapTablePacket.getPid()).thenReturn(programMapTableId);
        when(programMapTablePacket.isPayloadUnitStartIndicator()).thenReturn(true);
        when(programMapTablePacket.getPayload()).thenReturn(ByteBuffer.wrap(new byte[] {0x00}));

        MTSPacket elementaryStreamPacket1 = createElementary(true, videoPacketId, expectedByte1);
        MTSPacket elementaryStreamPacket2 = createElementary(false, videoPacketId, expectedByte2);
        MTSPacket elementaryStreamPacket3 = createElementary(false, videoPacketId, expectedByte3);
        MTSPacket elementaryStreamPacket4 = createElementary(false, videoPacketId, expectedByte4);
        MTSPacket elementaryStreamPacket5 = createElementary(true, videoPacketId, (byte) 0x00);

        List<Object> outputList = new LinkedList<>();

        Stream.of(programAssociationTablePacket,
                programMapTablePacket,
                elementaryStreamPacket1,
                elementaryStreamPacket2,
                elementaryStreamPacket3,
                elementaryStreamPacket4,
                elementaryStreamPacket5)
                .forEach(mtsPacket -> {
                    try {
                        decoder.read(mtsPacket, outputList::add);
                    } catch (IOException e) {
                        fail();
                    }
                });

        assertThat(outputList, hasSize(1));
        assertThat(outputList.get(0), is(instanceOf(PESPacket.class)));
        PESPacket pesPacket = (PESPacket) outputList.get(0);
        assertThat(pesPacket.getPacketId(), is(videoPacketId));
        assertThat(pesPacket.getStreamType(), is(MpegStreamType.lookup(streamType)));
        assertThat(pesPacket.getPayload(),
                is(new byte[] {expectedByte1, expectedByte2, expectedByte3, expectedByte4}));
    }

    private MTSPacket createElementary(boolean isStart, int pid, byte data) {
        MTSPacket elementaryStreamPacket = mock(MTSPacket.class);
        when(elementaryStreamPacket.getPid()).thenReturn(pid);
        when(elementaryStreamPacket.isPayloadUnitStartIndicator()).thenReturn(isStart);
        when(elementaryStreamPacket.getPayload()).thenReturn(ByteBuffer.wrap(new byte[] {data}));
        when(elementaryStreamPacket.isContainsPayload()).thenReturn(true);
        return elementaryStreamPacket;
    }
}
