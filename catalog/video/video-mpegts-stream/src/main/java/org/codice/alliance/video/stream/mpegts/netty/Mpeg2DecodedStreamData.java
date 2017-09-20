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

public class Mpeg2DecodedStreamData implements DecodedStreamData {

  private final List<Mpeg2PictureType> listOfTypes;

  private final int packetId;

  /**
   * @param listOfTypes list of Mpeg2PictureType enums (must be non-null)
   * @param packetId MPEG-TS packet identifier
   */
  public Mpeg2DecodedStreamData(List<Mpeg2PictureType> listOfTypes, int packetId) {
    notNull(listOfTypes, "listOfTypes must be non-null");
    this.listOfTypes = listOfTypes;
    this.packetId = packetId;
  }

  @Override
  public int getPacketId() {
    return packetId;
  }

  /** @return list of Mpeg2PictureType enums */
  public List<Mpeg2PictureType> getListOfTypes() {
    return listOfTypes;
  }

  @Override
  public void accept(Visitor visitor) {
    notNull(visitor, "visitor must be non-null");
    visitor.visit(this);
  }
}
