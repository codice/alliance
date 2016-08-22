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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.MTSSources;
import org.taktik.mpegts.sources.ResettableMTSSource;

import com.google.common.io.ByteSource;

/**
 * Iterate through the PESPackets contained in an MPEG-TS. Note: this does not return the
 * incomplete packets at the end of the stream.
 */
public class PESPacketIterator implements Iterator<PESPacket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PESPacketIterator.class);

    private PESPacket nextPesPacket;

    private ResettableMTSSource source;

    private MpegTsDecoder mpegTsDecoder = new MpegTsDecoderImpl();

    public PESPacketIterator(ByteSource byteSource) throws IOException {
        source = MTSSources.from(byteSource);
    }

    private void setPesPacket(PESPacket pesPacket) {
        nextPesPacket = pesPacket;
    }

    @Override
    public boolean hasNext() {
        if (nextPesPacket != null) {
            return true;
        }

        try {
            MTSPacket mtsPacket;
            while (nextPesPacket == null && (mtsPacket = source.nextPacket()) != null) {
                mpegTsDecoder.read(mtsPacket, this::setPesPacket);
            }
        } catch (Exception e) {
            LOGGER.debug("unable to get next PESPacket", e);
            return false;
        }

        return nextPesPacket != null;
    }

    @Override
    public PESPacket next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        PESPacket tmp = nextPesPacket;
        nextPesPacket = null;
        return tmp;
    }

}
