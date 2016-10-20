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
package org.codice.alliance.distribution.sdk.video.stream.mpegts;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * This client is used for testing/development to transmit an MPEG-TS file as a stream of UDP
 * packets.
 */
public class MpegTsUdpClient {

    private static final Logger LOGGER;

    static {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
        LOGGER = LoggerFactory.getLogger(MpegTsUdpClient.class);
    }

    private static final int PACKET_SIZE = 188;

    private static final String DEFAULT_IP = "127.0.0.1";

    private static final int DEFAULT_PORT = 50000;

    private static final String SUPPRESS_PRINTING_BANNER_FLAG = "-hide_banner";

    private static final String USAGE_MESSAGE =
            "mvn -Pmpegts.stream -Dexec.args=mpegPath,[ip address],[port]";

    private static final String INPUT_FILE_FLAG = "-i";

    private static final boolean HANDLE_QUOTING = false;

    public static void main(String[] args) {
        String[] arguments = args[0].split(",");

        if (arguments.length < 1) {
            LOGGER.error("Unable to start stream: no arguments specified.");
            LOGGER.error(USAGE_MESSAGE);
            return;
        }

        String videoFilePath = arguments[0];
        if (StringUtils.isBlank(videoFilePath)) {
            LOGGER.error("Unable to start stream: no video file path specified.");
            LOGGER.error(USAGE_MESSAGE);
            return;
        }

        String ip;
        int port;

        if (arguments.length == 1) {
            ip = DEFAULT_IP;
            port = DEFAULT_PORT;
            LOGGER.debug("No IP or port provided. Using defaults: {}:{}", DEFAULT_IP, DEFAULT_PORT);
        } else if (arguments.length == 2) {
            ip = arguments[1];
            port = DEFAULT_PORT;
            LOGGER.debug("No port provided. Using default: {}", DEFAULT_PORT);
        } else {
            ip = arguments[1];
            try {
                port = Integer.parseInt(arguments[2]);
            } catch (NumberFormatException e) {
                LOGGER.debug("Unable to parse specified port: {}. Using default: {}",
                        arguments[2],
                        DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        LOGGER.trace("Video file path: {}", videoFilePath);

        LOGGER.trace("Streaming address: {}:{}", ip, port);

        Duration videoDuration = getVideoDuration(videoFilePath);
        if (videoDuration == null) {
            return;
        }

        long tsDurationMillis = videoDuration.toMillis();

        LOGGER.trace("Video Duration: {}", tsDurationMillis);

        broadcastVideo(videoFilePath, ip, port, tsDurationMillis);
    }

    public static void broadcastVideo(String videoFilePath, String ip, int port,
            long tsDurationMillis) {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                DatagramPacket datagramPacket) throws Exception {
                            LOGGER.trace("Reading datagram from channel");
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                throws Exception {
                            LOGGER.error("Exception occurred while handling datagram packet.",
                                    cause);
                            ctx.close();
                        }
                    });

            Channel ch = bootstrap.bind(0)
                    .sync()
                    .channel();

            File videoFile = new File(videoFilePath);

            long bytesSent = 0;

            long tsPacketCount = videoFile.length() / PACKET_SIZE;

            double delayPerPacket = tsDurationMillis / (double) tsPacketCount;

            long startTime = System.currentTimeMillis();

            int packetsSent = 0;

            try (final InputStream fis = new BufferedInputStream(new FileInputStream(videoFile))) {
                byte[] buffer = new byte[PACKET_SIZE];
                int c;
                while ((c = fis.read(buffer)) != -1) {
                    bytesSent += c;

                    ChannelFuture cf = ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
                            buffer), new InetSocketAddress(ip, port)));

                    cf.await();

                    packetsSent++;

                    if (packetsSent % 100 == 0) {
                        Thread.sleep((long) (delayPerPacket * 100));
                    }
                    if (packetsSent % 10000 == 0) {
                        LOGGER.trace("Packets sent: {}", packetsSent);
                    }
                }
            }

            long endTime = System.currentTimeMillis();

            LOGGER.trace("Time Elapsed: {}", endTime - startTime);

            if (!ch.closeFuture()
                    .await(100)) {
                LOGGER.error("Channel timeout");
            }

            LOGGER.trace("Bytes sent: {} ", bytesSent);
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Unable to generate stream.", e);
        } finally {
            // Shut down the event loop to terminate all threads.
            eventLoopGroup.shutdownGracefully();
        }
    }

    private static CommandLine getFFmpegInfoCommand(final String videoFilePath) {
        final String bundledFFmpegBinaryPath = getBundledFFmpegBinaryPath();
        File file = new File("target/ffmpeg/" + bundledFFmpegBinaryPath);
        return new CommandLine(file.getAbsolutePath()).addArgument(SUPPRESS_PRINTING_BANNER_FLAG)
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
            throw new IllegalStateException("OS is not Linux, Mac, Solaris, or Windows."
                    + " No FFmpeg binary is available for this OS, so this client will not work.");
        }
    }

    private static Duration getVideoDuration(final String videoFilePath) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            final CommandLine command = getFFmpegInfoCommand(videoFilePath);
            final DefaultExecuteResultHandler resultHandler = executeFFmpeg(command,
                    3,
                    streamHandler);
            resultHandler.waitFor();
            final String output = outputStream.toString(StandardCharsets.UTF_8.name());
            return parseVideoDuration(output);
        } catch (InterruptedException e) {
            LOGGER.error("Thread interrupted while executing ffmpeg command.", e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unsupported encoding in ffmpeg output.", e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unable to parse video duration.", e);
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Unable to execute ffmpeg command.", e);
        }
        return null;
    }

    private static Duration parseVideoDuration(final String ffmpegOutput)
            throws IllegalArgumentException {
        final Pattern pattern = Pattern.compile("Duration: \\d\\d:\\d\\d:\\d\\d\\.\\d+");
        final Matcher matcher = pattern.matcher(ffmpegOutput);

        if (matcher.find()) {
            final String durationString = matcher.group();
            final String[] durationParts = durationString.substring("Duration: ".length())
                    .split(":");
            final String hours = durationParts[0];
            final String minutes = durationParts[1];
            final String seconds = durationParts[2];

            return Duration.parse(String.format("PT%sH%sM%sS", hours, minutes, seconds));
        } else {
            throw new IllegalArgumentException("Video duration not found in FFmpeg output.");
        }
    }

    private static DefaultExecuteResultHandler executeFFmpeg(final CommandLine command,
            final int timeoutSeconds, final PumpStreamHandler streamHandler) throws IOException {
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
