/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.netty;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;
import org.jcodec.codecs.h264.io.model.NALUnit;

public class Mpeg4DecodedStreamData implements DecodedStreamData {

  private final List<NALUnit> nalUnits;

  private final int packetId;

  /**
   * @param nalUnits list of NAL units (must be non-null)
   * @param packetId MPEG-TS packet identifier
   */
  public Mpeg4DecodedStreamData(List<NALUnit> nalUnits, int packetId) {
    notNull(nalUnits, "nalUnits must be non-null");
    this.nalUnits = nalUnits;
    this.packetId = packetId;
  }

  /** @return list of NAL units */
  public List<NALUnit> getNalUnits() {
    return nalUnits;
  }

  @Override
  public int getPacketId() {
    return packetId;
  }

  @Override
  public void accept(Visitor visitor) {
    notNull(visitor, "visitor must be non-null");
    visitor.visit(this);
  }
}
