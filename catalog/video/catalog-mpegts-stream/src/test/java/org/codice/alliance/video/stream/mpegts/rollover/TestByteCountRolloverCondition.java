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
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;
import org.junit.Before;
import org.junit.Test;

public class TestByteCountRolloverCondition {

    private static final long THRESHOLD = 100;

    private ByteCountRolloverCondition condition;

    private PacketBuffer packetBuffer;

    @Before
    public void setup() {
        condition = new ByteCountRolloverCondition(THRESHOLD);
        packetBuffer = mock(PacketBuffer.class);
    }

    @Test
    public void testCurrentLessThanThreshold() {

        when(packetBuffer.getByteCount()).thenReturn(THRESHOLD - 1);

        assertThat(condition.isRolloverReady(packetBuffer), is(false));

    }

    @Test
    public void testCurrentEqualToThreshold() {

        when(packetBuffer.getByteCount()).thenReturn(THRESHOLD);

        assertThat(condition.isRolloverReady(packetBuffer), is(true));

    }

    @Test
    public void testCurrentGreaterThanThreshold() {

        when(packetBuffer.getByteCount()).thenReturn(THRESHOLD + 1);

        assertThat(condition.isRolloverReady(packetBuffer), is(true));

    }

    @Test
    public void testSetter() {
        condition.setByteCountThreshold(THRESHOLD - 1);
        assertThat(condition.getByteCountThreshold(), is(THRESHOLD - 1));
    }

    @Test
    public void testAccept() {
        RolloverCondition.Visitor visitor = mock(RolloverCondition.Visitor.class);
        condition.accept(visitor);
        verify(visitor).visit(condition);
    }

    @Test
    public void testToString() {
        assertThat(condition.toString(), notNullValue());
    }
}
