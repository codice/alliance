/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.netty;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.jcodec.containers.mps.psi.PMTSection;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.PATSection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Converts a series of MTSPackets to PESPackets.
 */
class MTSPacketToPESPacketDecoder extends MessageToMessageDecoder<MTSPacket> {

    public static final int PROGRAM_ASSOCIATION_TABLE_PID = 0;

    private static final int BYTE_MASK = 0xFF;

    private final Set<Integer> programMapTablePacketIdDirectory = new HashSet<>();

    private final Map<Integer, PMTSection.PMTStream> programElementaryStreams = new HashMap<>();

    private final Map<Integer, byte[]> currentPacketBytesByStream = new HashMap<>();

    private PATSectionParser patSectionParser = PATSection::parse;

    private PMTSectionParser pmtSectionParser = PMTSection::parsePMT;

    public void setPatSectionParser(PATSectionParser patSectionParser) {
        this.patSectionParser = patSectionParser;
    }

    public void setPmtSectionParser(PMTSectionParser pmtSectionParser) {
        this.pmtSectionParser = pmtSectionParser;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, MTSPacket mtsPacket, List<Object> outputList)
            throws Exception {

        notNull(ctx, "ctx must be non-null");
        notNull(mtsPacket, "mtsPacket must be non-null");
        notNull(outputList, "outputList must be non-null");

        int pid = mtsPacket.getPid();

        if (isProgramAssociationTable(mtsPacket, pid)) {

            handleProgramAssociationTable(mtsPacket);

        } else if (isProgramMapTable(mtsPacket)) {

            handleProgramMapTable(mtsPacket);

        } else if (isElementaryStream(pid)) {

            handleElementaryStream(mtsPacket, outputList, pid);

        }

    }

    private void handleElementaryStream(MTSPacket mtsPacket, List<Object> outputList, int pid) {
        final PMTSection.PMTStream stream = programElementaryStreams.get(pid);

        final byte[] currentPacketBytes = currentPacketBytesByStream.get(pid);

        final boolean startingNewPacket = mtsPacket.isPayloadUnitStartIndicator();
        final boolean currentPacketToHandle = currentPacketBytes != null;
        final boolean reachedEndOfCurrentPacket = startingNewPacket && currentPacketToHandle;

        final byte[] payloadBytes = getByteBufferAsBytes(mtsPacket.getPayload());

        if (reachedEndOfCurrentPacket) {
            outputList.add(new PESPacket(currentPacketBytes, stream.getStreamType(), pid));
            currentPacketBytesByStream.put(pid, payloadBytes);
        } else if (startingNewPacket) {
            currentPacketBytesByStream.put(pid, payloadBytes);
        } else if (currentPacketToHandle) {
            final byte[] concatenatedPacket = ArrayUtils.addAll(currentPacketBytes, payloadBytes);
            currentPacketBytesByStream.put(pid, concatenatedPacket);
        }
    }

    private boolean isElementaryStream(int pid) {
        return pid != PROGRAM_ASSOCIATION_TABLE_PID && !programMapTablePacketIdDirectory.contains(
                pid) && programElementaryStreams.containsKey(pid);
    }

    private boolean isProgramMapTable(MTSPacket mtsPacket) {
        return programMapTablePacketIdDirectory.contains(mtsPacket.getPid())
                && mtsPacket.isPayloadUnitStartIndicator();
    }

    private boolean isProgramAssociationTable(MTSPacket mtsPacket, int pid) {
        return pid == PROGRAM_ASSOCIATION_TABLE_PID && mtsPacket.isPayloadUnitStartIndicator();
    }

    private void handleProgramMapTable(MTSPacket mtsPacket) {
        final ByteBuffer payload = mtsPacket.getPayload();

        final int pointer = payload.get() & BYTE_MASK;
        payload.position(payload.position() + pointer);

        final PMTSection pmt = pmtSectionParser.parse(payload);

        for (final PMTSection.PMTStream stream : pmt.getStreams()) {
            programElementaryStreams.put(stream.getPid(), stream);
        }
    }

    private void handleProgramAssociationTable(MTSPacket mtsPacket) throws IOException {
        final ByteBuffer payload = mtsPacket.getPayload();

        final int pointer = payload.get() & BYTE_MASK;
        payload.position(payload.position() + pointer);
        final PATSection programAssociationTable = patSectionParser.parse(payload);
        programMapTablePacketIdDirectory.clear();
        programMapTablePacketIdDirectory.addAll(programAssociationTable.getPrograms()
                .values());

        if (programMapTablePacketIdDirectory.isEmpty()) {
            throw new IOException("No programs found in transport stream.");
        }
    }

    private byte[] getByteBufferAsBytes(final ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public interface PATSectionParser {
        PATSection parse(ByteBuffer payload);
    }

    public interface PMTSectionParser {
        PMTSection parse(ByteBuffer payload);
    }

}
