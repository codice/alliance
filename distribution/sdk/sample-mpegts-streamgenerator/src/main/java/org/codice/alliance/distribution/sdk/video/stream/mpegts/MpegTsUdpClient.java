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
package org.codice.alliance.distribution.sdk.video.stream.mpegts;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This client is used for testing/development to transmit an MPEG-TS file as a stream of UDP
 * packets.
 */
public class MpegTsUdpClient {

  public static final int PACKET_SIZE = 188;

  private static final Logger LOGGER;

  private static final String DEFAULT_IP = "127.0.0.1";

  private static final int DEFAULT_PORT = 50000;

  private static final int DISK_IO_BUFFER_SIZE = 4096;

  private static final long PACKET_LOG_PERIOD = 10000;

  private static final long BYTE_LOG_PERIOD = 10000000;

  private static final String SUPPRESS_PRINTING_BANNER_FLAG = "-hide_banner";

  private static final String USAGE_MESSAGE =
      "mvn -Pmpegts.stream -Dexec.args=path=mpegPath,[ip=ip address],[port=port],[datagramSize=size|min-max],[fractionalTs=yes|no],[interface=name]";

  private static final String INPUT_FILE_FLAG = "-i";

  private static final boolean HANDLE_QUOTING = false;

  static {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
    LOGGER = LoggerFactory.getLogger(MpegTsUdpClient.class);
  }

  public static void main(String[] args) throws InterruptedException {

    LOGGER.info("args: {}", args[0]);

    String[] arguments = args[0].split(",");

    if (arguments.length < 1) {
      LOGGER.error("Unable to start stream: no arguments specified.");
      LOGGER.error(USAGE_MESSAGE);
      return;
    }

    String ip = DEFAULT_IP;
    int port = DEFAULT_PORT;
    String videoFilePath = null;
    int minDatagramSize = PACKET_SIZE;
    int maxDatagramSize = PACKET_SIZE;
    boolean fractionalTs = Boolean.FALSE;
    String networkInterface = null;

    for (String argument : arguments) {
      String[] parts = argument.split("=");
      switch (parts[0]) {
        case "path":
          videoFilePath = parts[1];
          break;
        case "ip":
          ip = parts[1];
          break;
        case "port":
          try {
            port = Integer.parseInt(parts[1]);
          } catch (NumberFormatException e) {
            LOGGER.debug(
                "Unable to parse specified port: {}. Using default: {}", parts[1], DEFAULT_PORT);
            port = DEFAULT_PORT;
          }
          break;
        case "datagramSize":
          try {
            if (parts[1].contains("-")) {
              int hyphenIndex = parts[1].indexOf("-");
              minDatagramSize = Integer.parseInt(parts[1].substring(0, hyphenIndex));
              maxDatagramSize = Integer.parseInt(parts[1].substring(hyphenIndex + 1));
            } else {
              minDatagramSize = Integer.parseInt(parts[1]);
              maxDatagramSize = minDatagramSize;
            }
          } catch (NumberFormatException e) {
            LOGGER.debug(
                "Unable to parse specified datagram size: {}. Using default: {}",
                parts[1],
                PACKET_SIZE);
            minDatagramSize = PACKET_SIZE;
            maxDatagramSize = PACKET_SIZE;
          }
          break;
        case "fractionalTs":
          switch (parts[1]) {
            case "yes":
              fractionalTs = true;
              break;
            case "no":
              fractionalTs = false;
              break;
            default:
              fractionalTs = false;
          }
          break;
        case "interface":
          networkInterface = parts[1];
          break;
        default:
          LOGGER.error("unrecognized command-line option: {}", parts[0]);
          return;
      }
    }

    if (videoFilePath == null) {
      LOGGER.error("Unable to start stream: no video file path specified.");
      LOGGER.error(USAGE_MESSAGE);
      return;
    }

    LOGGER.trace("Video file path: {}", videoFilePath);

    LOGGER.trace("Streaming address: {}:{}", ip, port);

    Duration videoDuration = getVideoDuration(videoFilePath);
    if (videoDuration == null) {
      return;
    }

    long tsDurationMillis = videoDuration.toMillis();

    LOGGER.trace("Video Duration: {}", tsDurationMillis);

    broadcastVideo(
        videoFilePath,
        ip,
        port,
        tsDurationMillis,
        minDatagramSize,
        maxDatagramSize,
        fractionalTs,
        networkInterface);
  }

  private static Optional<InetAddress> findLocalAddress(String interfaceName) {

    if (interfaceName == null) {
      return Optional.empty();
    }

    try {

      NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);

      if (networkInterface != null) {
        return Collections.list(networkInterface.getInetAddresses()).stream()
            .filter(inetAddress -> inetAddress instanceof Inet4Address)
            .findFirst();
      }

    } catch (SocketException e) {
      LOGGER.info("unable to find the network interface {}", interfaceName, e);
    }

    return Optional.empty();
  }

  public static void broadcastVideo(
      String videoFilePath,
      String ip,
      int port,
      long tsDurationMillis,
      int minDatagramSize,
      int maxDatagramSize,
      boolean fractionalTs,
      String networkInterfaceName)
      throws InterruptedException {

    Optional<InetAddress> inetAddressOptional = findLocalAddress(networkInterfaceName);

    EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    try {
      Bootstrap bootstrap = new Bootstrap();

      bootstrap
          .group(eventLoopGroup)
          .channel(NioDatagramChannel.class)
          .option(ChannelOption.SO_BROADCAST, true)
          .handler(
              new SimpleChannelInboundHandler<DatagramPacket>() {
                @Override
                protected void channelRead0(
                    ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket)
                    throws Exception {
                  LOGGER.trace("Reading datagram from channel");
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                    throws Exception {
                  LOGGER.error("Exception occurred while handling datagram packet.", cause);
                  ctx.close();
                }
              });

      Channel ch;

      if (inetAddressOptional.isPresent()) {
        ch = bootstrap.bind(inetAddressOptional.get(), 0).sync().channel();
      } else {
        ch = bootstrap.bind(0).sync().channel();
      }

      File videoFile = new File(videoFilePath);

      long bytesSent = 0;

      long tsPacketCount = videoFile.length() / PACKET_SIZE;

      double delayPerPacket = tsDurationMillis / (double) tsPacketCount;

      long startTime = System.currentTimeMillis();

      Random rand = new Random(0);

      long nextPacketLog = PACKET_LOG_PERIOD;

      long nextByteLog = BYTE_LOG_PERIOD;

      try (final InputStream fis = new BufferedInputStream(new FileInputStream(videoFile))) {
        byte[] buffer = new byte[DISK_IO_BUFFER_SIZE];

        int datagramSize = getPacketSize(rand, minDatagramSize, maxDatagramSize, fractionalTs);

        byte[] dgramBuffer = new byte[datagramSize];

        int writeStart = 0;
        int writeEnd = datagramSize;

        int readEnd;
        while ((readEnd = fis.read(buffer)) != -1) {

          int readStart = 0;

          while (readStart < readEnd) {
            int bytesToCopy = Math.min(writeEnd - writeStart, readEnd - readStart);
            System.arraycopy(buffer, readStart, dgramBuffer, writeStart, bytesToCopy);
            readStart += bytesToCopy;
            writeStart += bytesToCopy;

            if (writeStart == writeEnd) {
              transmit(ch, dgramBuffer, ip, port);
              bytesSent += dgramBuffer.length;

              long packetsSent = bytesSent / PACKET_SIZE;

              long currentTime = System.currentTimeMillis();

              long elapsedTime = currentTime - startTime;

              double predictedTime = packetsSent * delayPerPacket;

              if ((predictedTime - elapsedTime) >= 50) {
                Thread.sleep((long) predictedTime - elapsedTime);
              }

              if (packetsSent >= nextPacketLog) {
                LOGGER.debug("Packets sent: {}, Bytes sent: {}", packetsSent, bytesSent);
                nextPacketLog += PACKET_LOG_PERIOD;
              }

              if (bytesSent >= nextByteLog) {
                LOGGER.debug("Packets sent: {}, Bytes sent: {}", packetsSent, bytesSent);
                nextByteLog += BYTE_LOG_PERIOD;
              }

              datagramSize = getPacketSize(rand, minDatagramSize, maxDatagramSize, fractionalTs);

              dgramBuffer = new byte[datagramSize];
              writeStart = 0;
              writeEnd = datagramSize;
            }
          }
        }

        if (writeStart > 0) {
          byte[] tmp = new byte[writeStart];
          System.arraycopy(dgramBuffer, 0, tmp, 0, tmp.length);
          transmit(ch, tmp, ip, port);
        }
      }

      long endTime = System.currentTimeMillis();

      LOGGER.trace("Time Elapsed: {}", endTime - startTime);
      LOGGER.trace(
          "Elapsed Time minus predicted time: {}", (endTime - startTime) - tsDurationMillis);

      if (!ch.closeFuture().await(100)) {
        LOGGER.error("Channel timeout");
      }

      LOGGER.trace("Bytes sent: {} ", bytesSent);
    } catch (IOException e) {
      LOGGER.error("Unable to generate stream.", e);
    } finally {
      // Shut down the event loop to terminate all threads.
      eventLoopGroup.shutdownGracefully();
    }
  }

  private static int getPacketSize(
      Random rand, int minDatagramSize, int maxDatagramSize, boolean fractionalTs) {
    int datagramSize = rand.nextInt((maxDatagramSize - minDatagramSize) + 1) + minDatagramSize;
    if (!fractionalTs) {
      datagramSize = ((int) Math.floor(datagramSize / PACKET_SIZE)) * PACKET_SIZE;
    }
    return datagramSize;
  }

  private static void transmit(Channel ch, byte[] buf, String ip, int port)
      throws InterruptedException {
    ChannelFuture cf =
        ch.writeAndFlush(
            new DatagramPacket(Unpooled.copiedBuffer(buf), new InetSocketAddress(ip, port)));
    cf.await();
  }

  private static CommandLine getFFmpegInfoCommand(final String videoFilePath) {
    final String bundledFFmpegBinaryPath = getBundledFFmpegBinaryPath();
    File file = new File("target/ffmpeg/" + bundledFFmpegBinaryPath);
    return new CommandLine(file.getAbsolutePath())
        .addArgument(SUPPRESS_PRINTING_BANNER_FLAG)
        .addArgument(INPUT_FILE_FLAG)
        .addArgument(videoFilePath, HANDLE_QUOTING);
  }

  private static String getBundledFFmpegBinaryPath() {
    if (SystemUtils.IS_OS_LINUX) {
      return "linux/ffmpeg";
    } else if (SystemUtils.IS_OS_MAC) {
      return "osx/ffmpeg";
    } else if (SystemUtils.IS_OS_SOLARIS) {
      return "solaris/ffmpeg";
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return "windows/ffmpeg.exe";
    } else {
      throw new IllegalStateException(
          "OS is not Linux, Mac, Solaris, or Windows."
              + " No FFmpeg binary is available for this OS, so this client will not work.");
    }
  }

  private static Duration getVideoDuration(final String videoFilePath) throws InterruptedException {
    try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
      final CommandLine command = getFFmpegInfoCommand(videoFilePath);
      final DefaultExecuteResultHandler resultHandler = executeFFmpeg(command, 3, streamHandler);
      resultHandler.waitFor();
      final String output = outputStream.toString(StandardCharsets.UTF_8.name());
      return parseVideoDuration(output);
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Unsupported encoding in ffmpeg output.", e);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Unable to parse video duration.", e);
    } catch (IOException | IllegalStateException e) {
      LOGGER.error("Unable to execute ffmpeg command.", e);
    }
    return null;
  }

  private static Duration parseVideoDuration(final String ffmpegOutput) {
    final Pattern pattern = Pattern.compile("Duration: \\d\\d:\\d\\d:\\d\\d\\.\\d+");
    final Matcher matcher = pattern.matcher(ffmpegOutput);

    if (matcher.find()) {
      final String durationString = matcher.group();
      final String[] durationParts = durationString.substring("Duration: ".length()).split(":");
      final String hours = durationParts[0];
      final String minutes = durationParts[1];
      final String seconds = durationParts[2];

      return Duration.parse(String.format("PT%sH%sM%sS", hours, minutes, seconds));
    } else {
      throw new IllegalArgumentException("Video duration not found in FFmpeg output.");
    }
  }

  private static DefaultExecuteResultHandler executeFFmpeg(
      final CommandLine command, final int timeoutSeconds, final PumpStreamHandler streamHandler)
      throws IOException {
    final ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutSeconds * 1000);
    final Executor executor = new DefaultExecutor();
    executor.setWatchdog(watchdog);

    if (streamHandler != null) {
      executor.setStreamHandler(streamHandler);
    }

    final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    executor.execute(command, resultHandler);

    return resultHandler;
  }
}
