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

import io.netty.buffer.ByteBufProcessor;

/**
 * A start code is the byte sequence 0x00 0x00 0x01 0xXX, where XX is any byte value. When used with
 * ByteBuf.forEachByte, this caller will get the index position of the last byte of the start code.
 */
public class StartCode implements ByteBufProcessor {

  private States currentState = States.START;

  public void init() {
    currentState = States.START;
  }

  @Override
  public boolean process(byte currentByte) throws Exception {

    switch (currentState) {
      case START:
        switch (currentByte) {
          case 0:
            currentState = States.S1;
            break;
          default:
            // ignore current byte
        }
        break;
      case S1:
        switch (currentByte) {
          case 0:
            currentState = States.S2;
            break;
          default:
            currentState = States.START;
            break;
        }
        break;
      case S2:
        switch (currentByte) {
          case 0:
            // don't change states
            break;
          case 1:
            currentState = States.S3;
            break;
          default:
            currentState = States.START;
            break;
        }
        break;
      case S3:
        currentState = States.START;
        return false;
      default:
        assert false : "this should never happen!";
    }

    return true;
  }

  private enum States {
    START,
    S1,
    S2,
    S3
  }
}
