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
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;

/**
 * Test to determine if {@link PacketBuffer#getByteCount()} ()} is greater than or equal to a specific
 * threshold.
 */
public class MegabyteCountRolloverCondition implements RolloverCondition {

    private static final long MEGABYTE_TO_BYTE_FACTOR = 1000000;

    public static final long MIN_VALUE = 1;

    public static final long MAX_VALUE = Long.MAX_VALUE / MEGABYTE_TO_BYTE_FACTOR;

    private long megabyteCountThreshold;

    /**
     *
     * @param megabyteCountThreshold must be <= 9_223_372_036_854 and >= 1
     */
    public MegabyteCountRolloverCondition(long megabyteCountThreshold) {
        validate(megabyteCountThreshold);
        this.megabyteCountThreshold = megabyteCountThreshold;
    }

    public long getMegabyteCountThreshold() {
        return megabyteCountThreshold;
    }

    /**
     *
     * @param megabyteCountThreshold must be <= 9_223_372_036_854 and >= 1
     */
    public void setMegabyteCountThreshold(long megabyteCountThreshold) {
        validate(megabyteCountThreshold);
        this.megabyteCountThreshold = megabyteCountThreshold;
    }

    @Override
    public boolean isRolloverReady(PacketBuffer packetBuffer) {
        notNull(packetBuffer, "packetBuffer must be non-null");
        return packetBuffer.getByteCount() >= getByteCountThreshold();
    }


    @Override
    public void accept(Visitor visitor) {
        notNull(visitor, "visitor must be non-null");
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MegabyteCountRolloverCondition{" + "megabyteCountThreshold=" + megabyteCountThreshold
                + '}';
    }

    private long getByteCountThreshold() {
        return Math.multiplyExact(megabyteCountThreshold, MEGABYTE_TO_BYTE_FACTOR);
    }

    private void validate(long megabyteCountThreshold) {
        inclusiveBetween(MIN_VALUE, MAX_VALUE, megabyteCountThreshold);
    }

}
