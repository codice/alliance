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
package org.codice.alliance.libs.stanag4609;

import org.codice.ddf.libs.klv.KlvContext;

/**
 * Represents a decoded KLV metadata packet. The presentation timestamp is non-negative only if this
 * object represents a synchronous KLV metadata packet.
 */
public class DecodedKLVMetadataPacket {
    private final long presentationTimestamp;

    private final KlvContext decodedKLV;

    DecodedKLVMetadataPacket(final long presentationTimestamp, final KlvContext decodedKLV) {
        this.presentationTimestamp = presentationTimestamp;
        this.decodedKLV = decodedKLV;
    }

    public long getPresentationTimestamp() {
        return presentationTimestamp;
    }

    public KlvContext getDecodedKLV() {
        return decodedKLV;
    }
}
