/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.video.stream.mpegts.rollover;

import static org.apache.commons.lang3.Validate.notNull;

import com.connexta.alliance.video.stream.mpegts.netty.PacketBuffer;

/**
 * Test to determine if {@link PacketBuffer#getByteCount()} ()} is greater than or equal to a specific
 * threshold.
 */
public class ByteCountRolloverCondition implements RolloverCondition {

    private long byteCountThreshold;

    public ByteCountRolloverCondition(long byteCountThreshold) {
        this.byteCountThreshold = byteCountThreshold;
    }

    public long getByteCountThreshold() {
        return byteCountThreshold;
    }

    public void setByteCountThreshold(long byteCountThreshold) {
        this.byteCountThreshold = byteCountThreshold;
    }

    @Override
    public boolean isRolloverReady(PacketBuffer packetBuffer) {
        notNull(packetBuffer, "packetBuffer must be non-null");
        return packetBuffer.getByteCount() >= byteCountThreshold;
    }

    @Override
    public void accept(Visitor visitor) {
        notNull(visitor, "visitor must be non-null");
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ByteCountRolloverCondition{" +
                "byteCountThreshold=" + byteCountThreshold +
                '}';
    }
}
