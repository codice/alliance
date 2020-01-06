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

import com.google.common.io.ByteSource;
import ddf.security.Subject;
import ddf.security.service.SecurityServiceException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.codice.alliance.libs.mpegts.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.MTSSources;
import org.taktik.mpegts.sources.ResettableMTSSource;

/**
 * Converts datagrams to a series of MTSPackets. Will discard data while looking for the MPEG-TS
 * sync byte.
 */
class RawUdpDataToMTSPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {

  public static final byte TS_SYNC = (byte) 0x47;

  public static final int BUFFER_SIZE = 4096 * 16;

  public static final int TS_PACKET_SIZE = Constants.TS_PACKET_SIZE;

  /** Milliseconds to wait until checking the subject token for expiration. */
  public static final long TOKEN_CHECK_PERIOD = TimeUnit.SECONDS.toMillis(5);

  private static final Logger LOGGER = LoggerFactory.getLogger(RawUdpDataToMTSPacketDecoder.class);

  private static final Lock LOCK = new ReentrantLock();

  private ByteBuf byteBuf;

  private PacketBuffer packetBuffer;

  private MTSParser mtsParser = MTSSources::from;

  private UdpStreamProcessor udpStreamProcessor;

  /** Milliseconds since the subject token was checked for expiration. */
  private long lastTokenCheck = 0;

  public RawUdpDataToMTSPacketDecoder(
      PacketBuffer packetBuffer, UdpStreamProcessor udpStreamProcessor) {
    this.packetBuffer = packetBuffer;
    this.udpStreamProcessor = udpStreamProcessor;
  }

  public void setMtsParser(MTSParser mtsParser) {
    this.mtsParser = mtsParser;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (byteBuf != null) {
      byteBuf.release();
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    byteBuf = ctx.alloc().buffer(BUFFER_SIZE);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> outputList)
      throws Exception {

    notNull(ctx, "ctx must be non-null");
    notNull(msg, "msg must be non-null");
    notNull(outputList, "outputList must be non-null");

    LOCK.lock();
    try {
      checkSecuritySubject(msg);

      byteBuf.writeBytes(msg.content());

      skipToSyncByte();

      while (byteBuf.readableBytes() >= TS_PACKET_SIZE) {
        parseMpegTsPacket(outputList);
      }

      byteBuf.discardReadBytes();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * Attempt to parse the first {@link #TS_PACKET_SIZE} bytes from the ByteBuf. If the parsing
   * succeeds, then add the new mpeg-ts packet to the output list and add the raw bytes to the
   * packet buffer. If parsing fails, then rewind the read operation on the ByteBuf and discard the
   * first byte of the ByteBuf, which was a potential sync byte. In either case, skip to the next
   * sync byte.
   *
   * <p>Note: {@link ResettableMTSSource#nextPacket()} can throw unchecked exceptions when parsing
   * fails.
   *
   * @param outputList write parsed mpeg-ts packets to this list
   */
  private void parseMpegTsPacket(List<Object> outputList) {

    byte[] payload = new byte[TS_PACKET_SIZE];

    byteBuf.markReaderIndex();

    byteBuf.readBytes(payload);

    MTSPacket packet = null;
    try {
      ResettableMTSSource src = mtsParser.parse(ByteSource.wrap(payload));
      packet = src.nextPacket();
    } catch (Exception e) {
      LOGGER.debug("unable to parse mpeg-ts packet", e);
      byteBuf.resetReaderIndex();
      byteBuf.skipBytes(1);
    }

    if (packet != null) {
      packetBuffer.write(payload);
      outputList.add(packet);
    }

    skipToSyncByte();
  }

  private void checkSecuritySubject(DatagramPacket msg) throws SecurityServiceException {
    Subject subject = udpStreamProcessor.getSubject();

    if (subject == null) {
      String ip = getIpAddress(msg);
      Subject createdSubject = udpStreamProcessor.getSecuritySubject(ip);
      udpStreamProcessor.setSubject(createdSubject);
      lastTokenCheck = System.currentTimeMillis();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOGGER.debug("caught an exception while decoding raw udp packets", cause);
  }

  private String getIpAddress(DatagramPacket msg) {
    return msg.sender().getAddress().getHostAddress();
  }

  private void skipToSyncByte() {

    int bytesBefore;

    if ((bytesBefore = byteBuf.bytesBefore(TS_SYNC)) > 0) {
      LOGGER.trace("skipping bytes in raw data stream, looking for MPEG-TS sync {}", bytesBefore);
      byteBuf.skipBytes(bytesBefore);
    }
  }

  public interface MTSParser {
    ResettableMTSSource parse(ByteSource byteSource) throws IOException;
  }
}
