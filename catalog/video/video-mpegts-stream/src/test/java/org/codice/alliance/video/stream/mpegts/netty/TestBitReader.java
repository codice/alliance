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
package org.codice.alliance.video.stream.mpegts.netty;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.EOFException;
import java.util.Optional;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestBitReader {

    @Test
    public void testReadBit() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBit(), is((byte) 1));
        assertThat(bitReader.readBit(), is((byte) 0));
        assertThat(bitReader.readBit(), is((byte) 1));
        assertThat(bitReader.readBit(), is((byte) 1));
        assertThat(bitReader.readBit(), is((byte) 1));
        assertThat(bitReader.readBit(), is((byte) 0));
        assertThat(bitReader.readBit(), is((byte) 1));
        assertThat(bitReader.readBit(), is((byte) 0));
    }

    @Test
    public void testReadBits1() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(1), is((long) 1));
        assertThat(bitReader.readBits(1), is((long) 0));
        assertThat(bitReader.readBits(1), is((long) 1));
        assertThat(bitReader.readBits(1), is((long) 1));
        assertThat(bitReader.readBits(1), is((long) 1));
        assertThat(bitReader.readBits(1), is((long) 0));
        assertThat(bitReader.readBits(1), is((long) 1));
        assertThat(bitReader.readBits(1), is((long) 0));
    }

    @Test
    public void testReadBits2() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(2), is((long) 0b10));
        assertThat(bitReader.readBits(2), is((long) 0b11));
        assertThat(bitReader.readBits(2), is((long) 0b10));
        assertThat(bitReader.readBits(2), is((long) 0b10));
    }

    @Test
    public void testReadBits3() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(3), is((long) 0b101));
        assertThat(bitReader.readBits(3), is((long) 0b110));
        assertThat(bitReader.readBits(2), is((long) 0b10));
    }

    @Test
    public void testReadBits4() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(4), is((long) 0b1011));
        assertThat(bitReader.readBits(4), is((long) 0b1010));
    }

    @Test
    public void testReadBits5() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(5), is((long) 0b10111));
        assertThat(bitReader.readBits(3), is((long) 0b010));
    }

    @Test
    public void testReadBits6() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(6), is((long) 0b101110));
        assertThat(bitReader.readBits(2), is((long) 0b10));
    }

    @Test
    public void testReadBits7() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(7), is((long) 0b1011101));
        assertThat(bitReader.readBits(1), is((long) 0b0));
    }

    @Test
    public void testReadBits8() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) 0b10111010});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readBits(8), is((long) 0b10111010));
    }

    @Test(expected = EOFException.class)
    public void testEOF() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.testBit();
    }

    @Test
    public void testReadableBits1() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {});
        BitReader bitReader = new BitReader(byteBuf);
        assertThat(bitReader.readableBits(), is(0L));
    }

    @Test
    public void testReadableBits2() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x0});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.readBit();
        assertThat(bitReader.readableBits(), is(7L));
    }

    @Test
    public void testSkipBits1() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x0});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.skipBits(2);
        assertThat(bitReader.readableBits(), is(6L));
    }

    @Test
    public void testSkipBits2() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0x0});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.skipBits(8);
        assertThat(bitReader.readableBits(), is(0L));
    }

    @Test
    public void testSkipBits3() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.readBit();
        bitReader.skipBits(63);
        assertThat(bitReader.readableBits(), is(0L));
    }

    @Test
    public void testSkipBits4() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.testBit();
        bitReader.skipBits(8);
        assertThat(bitReader.readableBits(), is(0L));
    }

    @Test
    public void testFindStart1() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {0, 0, 1, 1});
        BitReader bitReader = new BitReader(byteBuf);
        Optional<Long> start = bitReader.findStart();
        assertThat(start.isPresent(), is(true));
        assertThat(start.get(), is(1L));
    }

    @Test
    public void testFindStart2() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {-1, 0, 0, 1, 1});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.skipBits(1);
        Optional<Long> start = bitReader.findStart();
        assertThat(start.isPresent(), is(true));
        assertThat(start.get(), is(1L));
    }

    @Test
    public void testFindStartEOF() throws EOFException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {-1, 0, 0, 2, 1});
        BitReader bitReader = new BitReader(byteBuf);
        bitReader.skipBits(1);
        Optional<Long> start = bitReader.findStart();
        assertThat(start.isPresent(), is(false));
    }

}
