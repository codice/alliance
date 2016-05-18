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

import org.codice.alliance.video.stream.mpegts.netty.PacketBuffer;

/**
 * Checks a PacketBuffer to determine if a rollover is desired.
 */
public interface RolloverCondition {

    /**
     * @param packetBuffer must be non-null
     * @return true if rollover is indicated
     */
    boolean isRolloverReady(PacketBuffer packetBuffer);

    /**
     * @param visitor must be non-null
     */
    void accept(Visitor visitor);

    interface Visitor {

        /**
         * @param condition must be non-null
         */
        void visit(BooleanOrRolloverCondition condition);

        /**
         * @param condition must be non-null
         */
        void visit(ElapsedTimeRolloverCondition condition);

        /**
         * @param condition must be non-null
         */
        void visit(ByteCountRolloverCondition condition);
    }

}
