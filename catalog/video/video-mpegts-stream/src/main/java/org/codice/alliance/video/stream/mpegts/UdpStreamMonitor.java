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
package org.codice.alliance.video.stream.mpegts;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvHandlerFactory;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.alliance.video.stream.mpegts.plugins.StreamCreationPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamShutdownPlugin;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Starts a Netty server with a pipeline specified by {@link UdpStreamProcessor}. The following
 * properties must be set:
 * <ul>
 * <li>{@link #setMonitoredAddress(String)}
 * <li>{@link #setFilenameTemplate(String)}
 * <li>{@link #setRolloverCondition(RolloverCondition)}
 * <li>{@link #setFilenameGenerator(FilenameGenerator)}
 * <li>{@link #setStanag4609Processor(Stanag4609Processor)}
 * <li>{@link #setKlvHandlerFactory(KlvHandlerFactory)}
 * <li>{@link #setDefaultKlvHandler(KlvHandler)}
 * <li>{@link #setKlvProcessor(KlvProcessor)}
 * <li>{@link #setMetacardTypeList(List)}
 * <li>{@link #setCatalogFramework(CatalogFramework)}
 * </ul>
 */
public class UdpStreamMonitor implements StreamMonitor {

    public static final int BYTE_COUNT_MIN = 1;

    public static final int BYTE_COUNT_MAX = Integer.MAX_VALUE;

    public static final long ELAPSED_TIME_MIN = 1;

    public static final long ELAPSED_TIME_MAX = Long.MAX_VALUE;

    public static final int SUBSAMPLE_COUNT_MIN = 1;

    public static final int SUBSAMPLE_COUNT_MAX = Integer.MAX_VALUE;

    /**
     * This is the id string used in metatype.xml.
     */
    public static final String METATYPE_TITLE = "parentTitle";

    /**
     * This is the id string used in metatype.xml.
     */
    public static final String METATYPE_MONITORED_ADDRESS = "monitoredAddress";

    /**
     * This is the id string used in metatype.xml.
     */
    public static final String METATYPE_BYTE_COUNT_ROLLOVER_CONDITION =
            "byteCountRolloverCondition";

    /**
     * This is the id string used in metatype.xml.
     */
    public static final String METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION =
            "elapsedTimeRolloverCondition";

    /**
     * This is the id string used in metatype.xml.
     */
    public static final String METATYPE_METACARD_UPDATE_INITIAL_DELAY =
            "metacardUpdateInitialDelay";

    /**
     * This is the id string used in metatype.xml.
     */
    public static final String METATYPE_FILENAME_TEMPLATE = "filenameTemplate";

    static final int MONITORED_PORT_MIN = 1;

    static final int MONITORED_PORT_MAX = 65535;

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpStreamMonitor.class);

    /**
     * This is the id string used in metatype.xml.
     */
    private static final String METATYPE_PARENT_TITLE = "parentTitle";

    private UdpStreamProcessor udpStreamProcessor;

    private String monitoredAddress;

    private Integer monitoredPort;

    private EventLoopGroup eventLoopGroup;

    private Thread serverThread;

    private String parentTitle;

    private Integer byteCountRolloverCondition;

    private Long elapsedTimeRolloverConditon;

    private URI streamUri;

    private Boolean startImmediately = false;

    private boolean monitoring;

    private String filenameTemplate;

    private Date startTime;

    public UdpStreamMonitor() {
        udpStreamProcessor = new UdpStreamProcessor(this);
    }

    UdpStreamMonitor(UdpStreamProcessor udpStreamProcessor) {
        this.udpStreamProcessor = udpStreamProcessor;
    }

    public void setStreamCreationPlugin(StreamCreationPlugin streamCreationPlugin) {
        udpStreamProcessor.setStreamCreationPlugin(streamCreationPlugin);
    }

    public void setStreamShutdownPlugin(StreamShutdownPlugin streamShutdownPlugin) {
        udpStreamProcessor.setStreamShutdownPlugin(streamShutdownPlugin);
    }

    public void setStartImmediately(Boolean startImmediately) {
        this.startImmediately = startImmediately;
    }

    public Boolean getStartImmediately() {
        return this.startImmediately;
    }

    /**
     * @param metacardUpdateInitialDelay must be non-null and >=0 and <={@link UdpStreamProcessor#MAX_METACARD_UPDATE_INITIAL_DELAY}
     */
    public void setMetacardUpdateInitialDelay(Long metacardUpdateInitialDelay) {
        udpStreamProcessor.setMetacardUpdateInitialDelay(metacardUpdateInitialDelay);
    }

    public Long getMetacardUpdateInitialDelay() {
        return udpStreamProcessor.getMetacardUpdateInitialDelay();
    }

    /**
     * @param klvHandlerFactory must be non-null
     */
    public void setKlvHandlerFactory(KlvHandlerFactory klvHandlerFactory) {
        notNull(klvHandlerFactory, "klvHandlerFactory must be non-null");
        udpStreamProcessor.setKlvHandlerFactory(klvHandlerFactory);
    }

    /**
     * @param klvProcessor must be non-null
     */
    public void setKlvProcessor(KlvProcessor klvProcessor) {
        notNull(klvProcessor, "klvProcessor must be non-null");
        udpStreamProcessor.setKlvProcessor(klvProcessor);
    }

    /**
     * @param klvLocationSubsampleCount must be non-null and >= {@link #SUBSAMPLE_COUNT_MIN}
     */
    public void setKlvLocationSubsampleCount(Integer klvLocationSubsampleCount) {
        notNull(klvLocationSubsampleCount, "klvLocationSubsampleCount mus be non-null");
        inclusiveBetween(SUBSAMPLE_COUNT_MIN,
                SUBSAMPLE_COUNT_MAX,
                klvLocationSubsampleCount,
                String.format("klvLocationSubsampleCount must be %d <= count <= %d",
                        SUBSAMPLE_COUNT_MIN,
                        SUBSAMPLE_COUNT_MAX));
        udpStreamProcessor.setKlvLocationSubsampleCount(klvLocationSubsampleCount);
    }

    /**
     * @param defaultKlvHandler must be non-null
     */
    public void setDefaultKlvHandler(KlvHandler defaultKlvHandler) {
        notNull(defaultKlvHandler, "defaultKlvHandler must be non-null");
        udpStreamProcessor.setDefaultKlvHandler(defaultKlvHandler);
    }

    /**
     * @param stanag4609Processor must be non-null
     */
    public void setStanag4609Processor(Stanag4609Processor stanag4609Processor) {
        notNull(stanag4609Processor, "stanag4609Processor must be non-null");
        udpStreamProcessor.setStanag4609Processor(stanag4609Processor);
    }

    /**
     * @param filenameGenerator must be non-null
     */
    public void setFilenameGenerator(FilenameGenerator filenameGenerator) {
        notNull(filenameGenerator, "filenameGenerator must be non-null");
        udpStreamProcessor.setFilenameGenerator(filenameGenerator);
    }

    /**
     * @param template must be non-null and non-blank
     */
    public void setFilenameTemplate(String template) {
        notNull(template, "template must be non-null");
        notBlank(template, "template must be non-blank");
        filenameTemplate = template;
        udpStreamProcessor.setFilenameTemplate(template);
    }

    public String getFileNameTemplate() {
        return this.filenameTemplate;
    }

    /**
     * @param rolloverCondition must be non-null
     */
    public void setRolloverCondition(RolloverCondition rolloverCondition) {
        notNull(rolloverCondition, "rolloverCondition must be non-null");
        udpStreamProcessor.setRolloverCondition(rolloverCondition);
    }

    private boolean isReady() {
        return monitoredAddress != null && udpStreamProcessor.isReady();
    }

    /**
     * Called by osgi to initialize the monitor. Makes sure it is already shutdown before initializing.
     * Will throw a RuntimeException if not properly initialized.
     */
    public void init() {
        LOGGER.debug("--init-- entering");
        LOGGER.info(
                "initializing udp stream monitor: monitoredAddress={}, monitoredPort={}, udpStreamProcessor={}",
                monitoredAddress,
                monitoredPort,
                udpStreamProcessor);
        if (startImmediately) {
            startMonitoring();
        }
    }

    @Override
    public void startMonitoring() {
        shutdown();
        if (isReady()) {
            udpStreamProcessor.init();
            serverThread = new Thread(new Server());
            serverThread.start();
            monitoring = true;
            startTime = new Date();
        } else {
            throw new RuntimeException(String.format(
                    "the udp stream monitor cannot be initialized because it is not properly configured: monitoredAddress=%s, monitoredPort=%s, udpStreamProcessor=%s",
                    monitoredAddress,
                    monitoredPort,
                    udpStreamProcessor));
        }
    }

    @Override
    public void stopMonitoring() {
        shutdown();
    }

    @Override
    public boolean isMonitoring() {
        return monitoring;
    }

    public void setParentTitle(String parentTitle) {
        notNull(parentTitle, "parentTitle must be non-null");
        this.parentTitle = parentTitle;
    }

    public String getStartDateAsString() {
        if (startTime == null) {
            return "Not Started";
        }
        return startTime.toString();

    }

    @Override
    public Optional<URI> getStreamUri() {
        if (streamUri == null) {
            return Optional.empty();
        }
        return Optional.of(streamUri);
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(parentTitle);
    }

    /**
     * @param metacardTypeList must be non-null
     */
    public void setMetacardTypeList(List<MetacardType> metacardTypeList) {
        notNull(metacardTypeList, "metacardTypeList must be non-null");
        udpStreamProcessor.setMetacardTypeList(metacardTypeList);
    }

    /**
     * @param catalogFramework must be non-null
     */
    public void setCatalogFramework(CatalogFramework catalogFramework) {
        notNull(catalogFramework, "catalogFramework must be non-null");
        udpStreamProcessor.setCatalogFramework(catalogFramework);
    }

    /**
     * Called by osgi to destroy the monitor.
     *
     * @param arg osgi destroy argument
     */
    public void destroy(int arg) {

        LOGGER.debug("--destroy-- : arg={}", arg);

        shutdown();
    }

    private void shutdown() {
        if (serverThread != null) {
            eventLoopGroup.shutdownGracefully();

            joinServerThread();

            serverThread = null;

            udpStreamProcessor.shutdown();
        }
    }

    private void joinServerThread() {
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            LOGGER.warn("interrupted while waiting for server thread to join", e);
        } finally {
            monitoring = false;
            startTime = null;
        }
    }

    /**
     * Called by osgi to update the properties defined in metatype.xml
     *
     * @param properties properties being updated
     */
    public void updateCallback(Map<String, Object> properties) {
        LOGGER.debug("--updateCallback-- properties={}", properties);
        if (properties != null) {

            if (!checkMetaTypeClass(properties, METATYPE_MONITORED_ADDRESS, String.class)) {
                return;
            }

            if (!checkMetaTypeClass(properties,
                    METATYPE_BYTE_COUNT_ROLLOVER_CONDITION,
                    Integer.class)) {
                return;
            }
            if (!checkMetaTypeClass(properties,
                    METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION,
                    Long.class)) {
                return;
            }
            if (!checkMetaTypeClass(properties, METATYPE_FILENAME_TEMPLATE, String.class)) {
                return;
            }
            if (!checkMetaTypeClass(properties, METATYPE_PARENT_TITLE, String.class)) {
                return;
            }
            if (!checkMetaTypeClass(properties,
                    METATYPE_METACARD_UPDATE_INITIAL_DELAY,
                    Long.class)) {
                return;
            }

            if (!checkMetaTypeClass(properties, METATYPE_TITLE, String.class)) {
                return;
            }

            setParentTitle((String) properties.get(METATYPE_TITLE));
            setMonitoredAddress((String) properties.get(METATYPE_MONITORED_ADDRESS));
            setByteCountRolloverCondition((Integer) properties.get(
                    METATYPE_BYTE_COUNT_ROLLOVER_CONDITION));
            setElapsedTimeRolloverCondition((Long) properties.get(
                    METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION));
            setFilenameTemplate((String) properties.get(METATYPE_FILENAME_TEMPLATE));
            setMetacardUpdateInitialDelay((Long) properties.get(
                    METATYPE_METACARD_UPDATE_INITIAL_DELAY));
            setParentTitle((String) properties.get(METATYPE_PARENT_TITLE));

            init();
        }
    }

    private boolean checkMetaTypeClass(Map<String, Object> properties, String fieldName,
            Class<?> clazz) {
        if (!properties.containsKey(fieldName)) {
            LOGGER.warn("the metatype id {} field was null", fieldName);
            return false;
        }
        if (!clazz.isInstance(properties.get(fieldName))) {
            LOGGER.warn("the metatype id {} field should be type {}", fieldName, clazz);
            return false;
        }
        return true;
    }

    public String getMonitoredAddress() {
        return monitoredAddress;
    }

    /**
     * @param monitoredAddress must be non-null and resolvable
     */
    public void setMonitoredAddress(String monitoredAddress) {
        notNull(monitoredAddress, "monitoredAddress must be non-null");

        URI uri;

        try {
            uri = new URI(monitoredAddress);
            InetAddress.getByName(uri.getHost());
            if (uri.getScheme() == null || !uri.getScheme()
                    .equals("udp")) {
                throw new URISyntaxException(uri.toString(),
                        "Monitored Address is not UDP protocol");
            }
        } catch (UnknownHostException | URISyntaxException e) {
            throw new IllegalArgumentException(String.format(
                    "the monitored address could not be resolved: monitoredAddress=%s",
                    monitoredAddress));
        }

        inclusiveBetween(MONITORED_PORT_MIN, MONITORED_PORT_MAX, uri.getPort());

        this.streamUri = uri;
        this.monitoredPort = uri.getPort();
        this.monitoredAddress = uri.getHost();
    }

    /**
     * @param count must be non-null and positive
     */
    public void setByteCountRolloverCondition(Integer count) {
        notNull(count, "count must be non-null");

        inclusiveBetween(BYTE_COUNT_MIN, BYTE_COUNT_MAX, count, String.format("count must be >=%d",
                BYTE_COUNT_MIN));
        byteCountRolloverCondition = count;
        udpStreamProcessor.setByteCountRolloverCondition(count);
    }

    public Integer getByteCountRolloverCondition() {
        return this.byteCountRolloverCondition;
    }

    /**
     * @param milliseconds must be non-null and >= {@link #ELAPSED_TIME_MIN}
     */
    public void setElapsedTimeRolloverCondition(Long milliseconds) {
        notNull(milliseconds, "milliseconds must be non-null");
        inclusiveBetween(ELAPSED_TIME_MIN, ELAPSED_TIME_MAX, milliseconds, String.format(
                "milliseconds must be >=%d",
                ELAPSED_TIME_MIN));
        this.elapsedTimeRolloverConditon = milliseconds;
        udpStreamProcessor.setElapsedTimeRolloverCondition(milliseconds);
    }

    public Long getElapsedTimeRolloverCondition() {
        return this.elapsedTimeRolloverConditon;
    }

    private class Server implements Runnable {

        @Override
        public void run() {

            Bootstrap bootstrap = new Bootstrap();

            eventLoopGroup = new NioEventLoopGroup();

            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {

                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel)
                                throws Exception {
                            nioDatagramChannel.pipeline()
                                    .addLast(udpStreamProcessor.createChannelHandlers());
                        }
                    });
            try {
                bootstrap.bind(monitoredAddress, monitoredPort)
                        .sync()
                        .channel()
                        .closeFuture()
                        .await();
            } catch (InterruptedException e) {
                LOGGER.warn("interrupted while waiting for shutdown", e);
            }

        }
    }

}
