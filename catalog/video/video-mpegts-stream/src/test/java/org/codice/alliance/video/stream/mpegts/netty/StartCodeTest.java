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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

public class StartCodeTest {

  private StartCode startCode;

  @Before
  public void setup() {
    startCode = new StartCode();
  }

  @Test
  public void testBasicCase() {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x00, 0x00, 0x01, 0x42});

    int position = byteBuf.forEachByte(startCode);

    assertThat(position, is(3));
  }

  @Test
  public void testExtraLeadingZeroes() {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x00, 0x00, 0x00, 0x00, 0x01, 0x42});

    int position = byteBuf.forEachByte(startCode);

    assertThat(position, is(5));
  }

  @Test
  public void testVariousLeadingBytes() {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x04, 0x05, 0x00, 0x00, 0x01, 0x42});

    int position = byteBuf.forEachByte(startCode);

    assertThat(position, is(5));
  }

  @Test
  public void testCodeDoesntExist() {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x04, 0x05, 0x02, 0x00, 0x01, 0x42});

    int position = byteBuf.forEachByte(startCode);

    assertThat(position, is(-1));
  }

  @Test
  public void testCodeDoesntExist2() {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x04, 0x05, 0x00, 0x00, 0x02, 0x42});

    int position = byteBuf.forEachByte(startCode);

    assertThat(position, is(-1));
  }

  @Test
  public void testEOF() {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x04, 0x05, 0x00, 0x00, 0x01});

    int position = byteBuf.forEachByte(startCode);

    assertThat(position, is(-1));
  }
}
