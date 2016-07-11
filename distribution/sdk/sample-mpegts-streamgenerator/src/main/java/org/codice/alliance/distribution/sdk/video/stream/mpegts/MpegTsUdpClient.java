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
import java.util.concurrent.atomic.AtomicLong;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MpegTsUdpClient.class);

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
            logErrorMessage("Unable to start stream : no arguments specified.", true);
            return;
        }

        String videoFilePath = arguments[0];
        if (StringUtils.isBlank(videoFilePath)) {
            logErrorMessage("Unable to start stream : no video file path specified.", true);
            return;
        }

        String ip;
        int port;

        if (arguments.length == 1) {
            ip = DEFAULT_IP;
            port = DEFAULT_PORT;
            LOGGER.warn("No IP or port provided.  Using defaults : {}:{}",
                    DEFAULT_IP,
                    DEFAULT_PORT);
        } else if (arguments.length == 2) {
            ip = arguments[1];
            port = DEFAULT_PORT;
            LOGGER.warn("No port provided.  Using default : {}", DEFAULT_PORT);
        } else {
            ip = arguments[1];
            try {
                port = Integer.parseInt(arguments[2]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Unable to parse specified port : {}.  Using Default : {}",
                        arguments[2],
                        DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        LOGGER.info("Video file path : {}", videoFilePath);

        LOGGER.info("Streaming address : {}:{}", ip, port);

        final AtomicLong count = new AtomicLong(0);

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
                            LOGGER.info("Reading datagram from channel");
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                throws Exception {
                            logErrorMessage(String.format(
                                    "Exception occured handling datagram packet.  %s",
                                    cause), false);
                            ctx.close();
                        }
                    });

            Channel ch = bootstrap.bind(0)
                    .sync()
                    .channel();

            File videoFile = new File(videoFilePath);

            long bytesSent = 0;

            long tsPacketCount = videoFile.length() / PACKET_SIZE;

            Duration videoDuration = getVideoDuration(videoFilePath);
            if (videoDuration == null) {
                return;
            }

            long tsDurationMillis = videoDuration.toMillis();

            LOGGER.info("Video Duration : {}", tsDurationMillis);

            double delayPerPacket = (double) tsDurationMillis / (double) tsPacketCount;

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
                        LOGGER.info("Packet sent : {}", packetsSent);
                    }
                }
            }

            long endTime = System.currentTimeMillis();

            LOGGER.info("Time Elapsed: {}", endTime - startTime);

            if (!ch.closeFuture()
                    .await(100)) {
                logErrorMessage("Channel time out", false);
            }

            LOGGER.info("Bytes sent : {} ", bytesSent);

        } catch (InterruptedException | IOException e) {
            logErrorMessage(String.format("Unable to generate stream : %s", e), false);
        } finally {
            // Shut down the event loop to terminate all threads.
            eventLoopGroup.shutdownGracefully();
        }

        LOGGER.info("count = " + count.get());
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
                    + " No FFmpeg binary is available for this OS, so the plugin will not work.");
        }
    }

    private static Duration getVideoDuration(final String videoFilePath) {
        String utfOutputStream;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            final CommandLine command = getFFmpegInfoCommand(videoFilePath);
            final DefaultExecuteResultHandler resultHandler = executeFFmpeg(command,
                    3,
                    streamHandler);
            resultHandler.waitFor();
            utfOutputStream = outputStream.toString(StandardCharsets.UTF_8.name());
            return parseVideoDuration(utfOutputStream);
        } catch (InterruptedException e) {
            logErrorMessage(String.format("Thread interrupted when executing ffmpeg command. %s",
                    e), false);
            return null;
        } catch (UnsupportedEncodingException e) {
            logErrorMessage(String.format("Unsupported encoding in ffmpeg output. %s", e), false);
            return null;
        } catch (IllegalArgumentException e) {
            logErrorMessage(String.format("Unable to parse video duration. %s", e), false);
            return null;
        } catch (IOException | IllegalStateException e) {
            logErrorMessage(String.format("Unable to execute ffmpeg command. %s", e), false);
            return null;
        }
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

    private static void logErrorMessage(String errorMessage, boolean usageMessage) {
        LOGGER.error(errorMessage);
        if (usageMessage) {
            LOGGER.error(USAGE_MESSAGE);
        }
    }
}
