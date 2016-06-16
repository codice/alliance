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

import java.io.EOFException;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

/**
 * The BitReader wraps a ByteBuf and provides bit-level tools for reading bits, skipping bits,
 * and searching for MPEG-2 (h.262) start codes.
 */
public class BitReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitReader.class);

    private static final int BYTE_BUF_EOF = -1;

    private static final int MAX_BIT_INDEX = 7;

    private static final int START_CODE_PREFIX_SIZE = 3;

    private static final int START_CODE_BIT_SIZE = 32;

    private final ByteBuf byteBuf;

    private Byte currentByte;

    private int currentIndex;

    private StartCode startCode = new StartCode();

    /**
     * @param byteBuf must be non-null
     */
    public BitReader(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    /**
     * Get the next bit's value, but do not actually consume the bit.
     *
     * @return true if the next bit is 1, false if the next bit is 0
     */
    public boolean testBit() throws EOFException {
        if (currentByte == null) {
            if (byteBuf.readableBytes() < 1) {
                throw new EOFException("read past end-of-file");
            }
            currentByte = byteBuf.readByte();
            currentIndex = MAX_BIT_INDEX;
        }

        byte result = (byte) ((currentByte >> currentIndex) & 0b1);

        return result == 0b1;
    }

    /**
     * Get the number of bits that can be read.
     *
     * @return number of readable bits
     */
    public long readableBits() {
        return byteBuf.readableBytes() * 8 + (currentByte != null ? currentIndex + 1 : 0);
    }

    /**
     * Only call this method if we are at a byte boundary. Negative values are handled correctly.
     *
     * @param numberOfBytes the number of bytes to skip
     */
    private void skipBytes(int numberOfBytes) {
        int remaining = numberOfBytes;
        if (remaining > 0 && currentByte != null) {
            currentByte = null;
            remaining--;
        }
        if (remaining > 0) {
            byteBuf.skipBytes(remaining);
        }
    }

    public void skipBits(int numberOfBits) throws EOFException {

        Validate.inclusiveBetween(0, Integer.MAX_VALUE, numberOfBits, "numberOfBits must >=0");

        if (isByteBoundary() && numberOfBits % 8 == 0) {
            skipBytes(numberOfBits / 8);
            return;
        }

        int tmp = numberOfBits;
        while (tmp >= 32) {
            readBits(32);
            tmp -= 32;
        }
        if (tmp > 0) {
            readBits(tmp);
        }
    }

    public byte readBit() throws EOFException {

        byte result = testBit() ? (byte) 1 : (byte) 0;

        consumeBit();

        return result;
    }

    private void consumeBit() {
        if (currentIndex > 0) {
            currentIndex--;
        } else if (currentIndex == 0) {
            currentByte = null;
        }
    }

    public long readBits(int numberOfBits) throws EOFException {

        Validate.inclusiveBetween(1, 32, numberOfBits, "numberOfBits must be [1,32]");

        if (numberOfBits == 1) {
            return readBit();
        }

        long result = 0;

        for (int i = 0; i < numberOfBits; i++) {
            result |= readBit() << (numberOfBits - i - 1);
        }

        return result;
    }

    public boolean isByteBoundary() {
        return currentByte == null || currentIndex == MAX_BIT_INDEX;
    }

    private void discardCurrentByte() {
        currentByte = null;
    }

    public Optional<Long> findStart() {

        if (!isByteBoundary()) {
            discardCurrentByte();
        }

        int startPos = byteBuf.readerIndex();

        startCode.init();

        int position = byteBuf.forEachByte(startCode);

        if (position == BYTE_BUF_EOF) {
            return Optional.empty();
        }

        skipBytes(position - startPos - START_CODE_PREFIX_SIZE);

        long startCodeSequence;
        try {
            startCodeSequence = readBits(START_CODE_BIT_SIZE);
        } catch (EOFException e) {
            LOGGER.warn(
                    "read past end of file, but should never happen because the code already determined that the bytes exist",
                    e);
            return Optional.empty();
        }

        assert (startCodeSequence & 0xffffff00)
                == 0x00000100 : "start code does not follow the expected format";

        return Optional.of(startCodeSequence & 0xFF);
    }

}
