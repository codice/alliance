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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.codice.alliance.libs.stanag4609.DecodedKLVMetadataPacket;
import org.jcodec.codecs.h264.io.model.NALUnit;
import org.junit.Test;

/**
 *
 */
public class TestDecodedStreamData {

    @Test
    public void testNALUnits() {

        List<NALUnit> nalUnitList = mock(List.class);

        DecodedStreamData decodedStreamData = new DecodedStreamData(nalUnitList, 1);

        assertThat(decodedStreamData.getNalUnits()
                .isPresent(), is(true));
        assertThat(decodedStreamData.getNalUnits()
                .get(), is(nalUnitList));
        assertThat(decodedStreamData.getPacketId(), is(1));
        assertThat(decodedStreamData.getDecodedKLVMetadataPacket()
                .isPresent(), is(false));

    }

    @Test
    public void testGetDecodedKLVMetadataPacket() {

        DecodedKLVMetadataPacket decodedKLVMetadataPacket = mock(DecodedKLVMetadataPacket.class);

        DecodedStreamData decodedStreamData = new DecodedStreamData(decodedKLVMetadataPacket, 1);

        assertThat(decodedStreamData.getDecodedKLVMetadataPacket()
                .isPresent(), is(true));
        assertThat(decodedStreamData.getDecodedKLVMetadataPacket()
                .get(), is(decodedKLVMetadataPacket));
        assertThat(decodedStreamData.getPacketId(), is(1));
        assertThat(decodedStreamData.getNalUnits()
                .isPresent(), is(false));

    }

}
