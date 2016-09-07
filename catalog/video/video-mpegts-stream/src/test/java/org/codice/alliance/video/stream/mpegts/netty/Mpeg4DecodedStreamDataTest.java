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
package org.codice.alliance.video.stream.mpegts.netty;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.jcodec.codecs.h264.io.model.NALUnit;
import org.junit.Before;
import org.junit.Test;

public class Mpeg4DecodedStreamDataTest {

    private static final int PACKET_ID = 1;

    private List<NALUnit> nalUnitList;

    private Mpeg4DecodedStreamData mpeg4DecodedStreamData;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        nalUnitList = mock(List.class);
        mpeg4DecodedStreamData = new Mpeg4DecodedStreamData(nalUnitList, PACKET_ID);
    }

    @Test
    public void testGetNALUnits() {
        assertThat(mpeg4DecodedStreamData.getNalUnits(), is(nalUnitList));
    }

    @Test
    public void testGetPacketId() {
        assertThat(mpeg4DecodedStreamData.getPacketId(), is(PACKET_ID));
    }

    @Test
    public void testAccept() {
        DecodedStreamData.Visitor visitor = mock(DecodedStreamData.Visitor.class);
        mpeg4DecodedStreamData.accept(visitor);
        verify(visitor).visit(mpeg4DecodedStreamData);
    }

}
