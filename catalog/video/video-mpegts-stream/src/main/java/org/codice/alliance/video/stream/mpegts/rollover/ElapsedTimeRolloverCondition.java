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

import static org.apache.commons.lang3.Validate.notNull;

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;

/**
 * Test to determine if {@link PacketBuffer#getAge()} is greater than or equal to a specific
 * threshold.
 */
public class ElapsedTimeRolloverCondition implements RolloverCondition {

    private long elapsedTimeThreshold;

    /**
     * @param elapsedTimeThreshold milliseconds
     */
    public ElapsedTimeRolloverCondition(long elapsedTimeThreshold) {
        this.elapsedTimeThreshold = elapsedTimeThreshold;
    }

    public long getElapsedTimeThreshold() {
        return elapsedTimeThreshold;
    }

    public void setElapsedTimeThreshold(long elapsedTimeThreshold) {
        this.elapsedTimeThreshold = elapsedTimeThreshold;
    }

    @Override
    public boolean isRolloverReady(PacketBuffer packetBuffer) {
        notNull(packetBuffer, "packetBuffer must be non-null");
        return packetBuffer.getAge() >= elapsedTimeThreshold;
    }

    @Override
    public void accept(Visitor visitor) {
        notNull(visitor, "visitor must be non-null");
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ElapsedTimeRolloverCondition{" +
                "elapsedTimeThreshold=" + elapsedTimeThreshold +
                '}';
    }
}
