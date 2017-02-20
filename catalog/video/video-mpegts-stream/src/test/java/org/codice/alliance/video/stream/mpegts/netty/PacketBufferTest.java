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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.codice.alliance.video.stream.mpegts.filename.TempFileGenerator;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.junit.Before;
import org.junit.Test;

public class PacketBufferTest {

    private PacketBuffer packetBuffer;

    private RolloverCondition rolloverCondition;

    private Optional<File> tempFile;

    private OutputStream outputStream;

    private ByteArrayOutputStream os;

    @Before
    public void setup() throws IOException {
        TempFileGenerator tempFileGenerator = mock(TempFileGenerator.class);
        when(tempFileGenerator.generate()).thenReturn(new File("x"));
        packetBuffer = new PacketBuffer();
        outputStream = mock(OutputStream.class);
        packetBuffer.setOutputStreamFactory((file, append) -> outputStream);
        packetBuffer.setTempFileGenerator(tempFileGenerator);
        rolloverCondition = mock(RolloverCondition.class);
        when(rolloverCondition.isRolloverReady(any())).thenReturn(true);
        tempFile = null;
        os = new ByteArrayOutputStream();
    }

    @Test
    public void testToString() {
        assertThat(packetBuffer.toString(), notNullValue());
    }

    @Test
    public void testGetAgeInitial() {
        assertThat(packetBuffer.getAge(), is(0L));
    }

    @Test
    public void testRotateWithNoData() {
        tempFile = packetBuffer.rotate(rolloverCondition).getFile();
        assertThat(tempFile.isPresent(), is(false));
    }

    @Test
    public void testRotateWithDataNoFrames() {
        packetBuffer.write(new byte[] {0x01});
        tempFile = packetBuffer.rotate(rolloverCondition).getFile();
        assertThat(tempFile.isPresent(), is(false));
    }

    /**
     * The data being written to the packet buffer exceeds the max incomplete frame byte count.
     *
     * @throws IOException
     */
    @Test
    public void testWriteWithOnlyUnknownFrames() throws IOException {
        byte[] payload = new byte[] {0x01, 0x02};
        packetBuffer.setMaxIncompleteFrameBytes(1);
        packetBuffer.write(payload);
        verify(outputStream).write(payload);
    }

    /**
     * With the sleep, the last three packets gets flushed.
     *
     * @throws InterruptedException
     */
    @Test
    public void testActivityTimeout() throws InterruptedException {

        packetBuffer.setOutputStreamFactory((file, append) -> os);

        completeVideoSequence(new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02, 0x03,
                0x01, 0x02, 0x03});

        RolloverCondition rc = mock(RolloverCondition.class);
        when(rc.isRolloverReady(any())).thenReturn(true);

        Thread.sleep(PacketBuffer.ACTIVITY_TIMEOUT);

        Optional<File> file = packetBuffer.rotate(rc).getFile();
        assertThat(file.isPresent(), is(true));

        assertThat(os.toByteArray(),
                is(new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02,
                        0x03}));
    }

    /**
     * A full frameset has been written, verify that only the compelete frameset has been flushed
     */
    @Test
    public void testWriteWithVideoData1() {

        packetBuffer.setOutputStreamFactory((file, append) -> os);

        completeVideoSequence(new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02, 0x03,
                0x01, 0x02, 0x03});

        assertThat(os.toByteArray(),
                is(new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02, 0x03}));

    }

    private void writePacket(byte b) {
        packetBuffer.write(new byte[] {b});
    }

    private void idr() {
        packetBuffer.frameComplete(PacketBuffer.FrameType.IDR);
    }

    private void nonidr() {
        packetBuffer.frameComplete(PacketBuffer.FrameType.NON_IDR);
    }

    /**
     * We don't know if the frameset is complete, so no data should be flushed
     */
    @Test
    public void testWriteWithVideoData2() {

        packetBuffer.setOutputStreamFactory((file, append) -> os);

        writePacket((byte) 0x01);
        writePacket((byte) 0x02);
        writePacket((byte) 0x03);
        idr();

        writePacket((byte) 0x01);
        writePacket((byte) 0x02);
        writePacket((byte) 0x03);
        nonidr();

        writePacket((byte) 0x01);
        writePacket((byte) 0x02);
        writePacket((byte) 0x03);
        nonidr();

        assertThat(os.toByteArray(), is(new byte[] {}));

    }

    /**
     * Test that the packet buffer does not rotate when the rollover condition is false.
     */
    @Test
    public void testRotate1() {

        completeVideoSequence(new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02, 0x03,
                0x01, 0x02, 0x03});

        RolloverCondition rc = mock(RolloverCondition.class);
        when(rc.isRolloverReady(any())).thenReturn(false);

        Optional<File> file = packetBuffer.rotate(rc).getFile();
        assertThat(file, is(Optional.empty()));

    }

    /**
     * Test that the packet buffer does rotate when the condition is true.
     */
    @Test
    public void testRotate2() {

        completeVideoSequence(new byte[] {0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x01, 0x02, 0x03,
                0x01, 0x02, 0x03});

        RolloverCondition rc = mock(RolloverCondition.class);
        when(rc.isRolloverReady(any())).thenReturn(true);

        Optional<File> file = packetBuffer.rotate(rc).getFile();
        assertThat(file.isPresent(), is(true));

    }

    /**
     * Always call with an array of 12 elements!
     */
    private void completeVideoSequence(byte[] data) {

        assertThat(data.length, is(12));

        writePacket(data[0]);
        writePacket(data[1]);
        writePacket(data[2]);
        idr();

        writePacket(data[3]);
        writePacket(data[4]);
        writePacket(data[5]);
        nonidr();

        writePacket(data[6]);
        writePacket(data[7]);
        writePacket(data[8]);
        nonidr();

        writePacket(data[9]);
        writePacket(data[10]);
        writePacket(data[11]);
        idr();
    }

}
