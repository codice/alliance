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

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.Validate;
import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvHandlerFactory;
import org.codice.alliance.libs.klv.KlvProcessor;
import org.codice.alliance.libs.klv.Stanag4609Processor;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.UdpStreamMonitor;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.rollover.BooleanOrRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.ByteCountRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.CatalogRolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.CreateMetacardRolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.ElapsedTimeRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.KlvRolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.ListRolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverActionException;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.codice.ddf.security.common.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;
import io.netty.channel.ChannelHandler;

/**
 *
 */
public class UdpStreamProcessor implements StreamProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpStreamProcessor.class);

    private static final boolean IS_KLV_PARSING_ENABLED = false;

    private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);

    /**
     * Number of milliseconds between rollover checks. See {@link Timer#scheduleAtFixedRate(TimerTask, long, long)}.
     */
    private static final long ROLLOVER_CHECK_PERIOD = ONE_SECOND;

    /**
     * Number of milliseconds to wait until first rollover check. See {@link Timer#scheduleAtFixedRate(TimerTask, long, long)}.
     */
    private static final long ROLLOVER_CHECK_DELAY = ONE_SECOND;

    private PacketBuffer packetBuffer = new PacketBuffer();

    private Stanag4609Processor stanag4609Processor;

    private Map<String, KlvHandler> klvHandlerMap;

    private KlvHandler defaultKlvHandler;

    private RolloverCondition rolloverCondition;

    private String filenameTemplate;

    private KlvHandlerFactory klvHandlerFactory;

    private FilenameGenerator filenameGenerator;

    private KlvProcessor klvProcessor;

    private Timer timer = new Timer();

    private List<MetacardType> metacardTypeList;

    private RolloverAction rolloverAction;

    private Integer klvLocationSubsampleCount;

    private CatalogFramework catalogFramework;

    private Lock klvHandlerMapLock = new ReentrantLock();

    private StreamMonitor streamMonitor;

    public UdpStreamProcessor(StreamMonitor streamMonitor) {
        this.streamMonitor = streamMonitor;
    }

    @Override
    public Optional<URI> getStreamUri() {
        return streamMonitor.getStreamUri();
    }

    @Override
    public Optional<String> getTitle() {
        return streamMonitor.getTitle();
    }

    /**
     * @param klvLocationSubsampleCount must be non-null and >0
     */
    public void setKlvLocationSubsampleCount(Integer klvLocationSubsampleCount) {
        notNull(klvLocationSubsampleCount, "klvLocationSubsampleCount must be non-null");
        Validate.inclusiveBetween(UdpStreamMonitor.SUBSAMPLE_COUNT_MIN,
                UdpStreamMonitor.SUBSAMPLE_COUNT_MAX,
                klvLocationSubsampleCount,
                "klvLocationSubsampleCount must be >0");
        this.klvLocationSubsampleCount = klvLocationSubsampleCount;
    }

    /**
     * @param catalogFramework must be non-null
     */
    public void setCatalogFramework(CatalogFramework catalogFramework) {
        notNull(catalogFramework, "catalogFramework must be non-null");
        this.catalogFramework = catalogFramework;
    }

    @Override
    public String toString() {
        return "UdpStreamProcessor{" +
                "defaultKlvHandler=" + defaultKlvHandler +
                ", filenameGenerator=" + filenameGenerator +
                ", filenameTemplate='" + filenameTemplate + '\'' +
                ", klvHandlerFactory=" + klvHandlerFactory +
                ", klvProcessor=" + klvProcessor +
                ", metacardTypeList=" + metacardTypeList +
                ", packetBuffer=" + packetBuffer +
                ", rolloverCondition=" + rolloverCondition +
                ", stanag4609Processor=" + stanag4609Processor +
                '}';
    }

    /**
     * @param count must be non-null and positive
     */
    public void setByteCountRolloverCondition(Integer count) {
        notNull(count, "count must be non-null");
        inclusiveBetween(UdpStreamMonitor.BYTE_COUNT_MIN,
                UdpStreamMonitor.BYTE_COUNT_MAX,
                count,
                "count must be >0");
        rolloverCondition.accept(new RolloverCondition.Visitor() {
            @Override
            public void visit(BooleanOrRolloverCondition condition) {
            }

            @Override
            public void visit(ElapsedTimeRolloverCondition condition) {
            }

            @Override
            public void visit(ByteCountRolloverCondition condition) {
                condition.setByteCountThreshold(count);
            }
        });
    }

    /**
     * @param milliseconds must be non-null and positive
     */
    public void setElapsedTimeRolloverCondition(Long milliseconds) {
        notNull(milliseconds, "milliseconds must be non-null");
        inclusiveBetween(UdpStreamMonitor.ELAPSED_TIME_MIN,
                UdpStreamMonitor.ELAPSED_TIME_MAX,
                milliseconds,
                "milliseconds must be >0");
        rolloverCondition.accept(new RolloverCondition.Visitor() {
            @Override
            public void visit(BooleanOrRolloverCondition condition) {
            }

            @Override
            public void visit(ElapsedTimeRolloverCondition condition) {
                condition.setElapsedTimeThreshold(milliseconds);
            }

            @Override
            public void visit(ByteCountRolloverCondition condition) {
            }
        });
    }

    /**
     * Shutdown the stream processor. Attempts to flush and ingest any partial stream data regardless
     * of IDR boundaries.
     */
    public void shutdown() {

        timer.cancel();

        try {
            packetBuffer.flushAndRotate()
                    .ifPresent(this::doRollover);
        } catch (IOException e) {
            LOGGER.warn("unable to rotate and ingest final data during shutdown", e);
        }

        packetBuffer.reset();
        klvHandlerMap = null;
    }

    private void checkForRollover() {
        packetBuffer.rotate(rolloverCondition)
                .ifPresent(this::doRollover);
    }

    private void doRollover(File tempFile) {
        try {
            rolloverAction.doAction(tempFile);
        } catch (RolloverActionException e) {
            LOGGER.warn("unable handle rollover file: tempFile={}", tempFile, e);
        } finally {
            if (!tempFile.delete()) {
                LOGGER.warn("unable to delete temp file: filename={}", tempFile);
            }
        }
    }

    /**
     * @param metacardTypeList must be non-null
     */
    public void setMetacardTypeList(List<MetacardType> metacardTypeList) {
        notNull(metacardTypeList, "metacardTypeList must be non-null");
        this.metacardTypeList = metacardTypeList;
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                checkForRollover();
            }
        };
    }

    private boolean areNonNull(List<Object> listofObjects) {
        return listofObjects.stream()
                .allMatch(Objects::nonNull);
    }

    /**
     * Return <code>true</code> if the processor is ready to run.
     *
     * @return ready status
     */
    public boolean isReady() {
        return areNonNull(Arrays.asList(stanag4609Processor,
                defaultKlvHandler,
                rolloverCondition,
                filenameTemplate,
                klvHandlerFactory,
                filenameGenerator,
                klvProcessor,
                metacardTypeList,
                catalogFramework));
    }

    /**
     * Initializes the processor. Users should call {@link #isReady()} first to make sure the
     * processor is ready to run.
     */
    public void init() {

        klvHandlerMap = klvHandlerFactory.createStanag4609Handlers();

        rolloverAction = new ListRolloverAction(Arrays.asList(new CreateMetacardRolloverAction(
                        metacardTypeList),
                new KlvRolloverAction(klvHandlerMap,
                        klvHandlerMapLock,
                        klvLocationSubsampleCount,
                        klvProcessor),
                new CatalogRolloverAction(filenameGenerator,
                        filenameTemplate,
                        this,
                        catalogFramework,
                        Security.getInstance(),
                        metacardTypeList)));

        timer.scheduleAtFixedRate(createTimerTask(), ROLLOVER_CHECK_DELAY, ROLLOVER_CHECK_PERIOD);
    }

    /**
     * @param klvHandlerFactory must be non-null
     */
    public void setKlvHandlerFactory(KlvHandlerFactory klvHandlerFactory) {
        notNull(klvHandlerFactory, "klvHandlerFactory must be non-null");
        this.klvHandlerFactory = klvHandlerFactory;
    }

    /**
     * @param klvProcessor must be non-null
     */
    public void setKlvProcessor(KlvProcessor klvProcessor) {
        notNull(klvProcessor, "klvProcessor must be non-null");
        this.klvProcessor = klvProcessor;
    }

    /**
     * @param filenameGenerator must be non-null
     */
    public void setFilenameGenerator(FilenameGenerator filenameGenerator) {
        notNull(filenameGenerator, "filenameGenerator must be non-null");
        this.filenameGenerator = filenameGenerator;
    }

    /**
     * @param filenameTemplate must be non-null
     */
    public void setFilenameTemplate(String filenameTemplate) {
        notNull(filenameTemplate, "filenameTemplate must be non-null");
        this.filenameTemplate = filenameTemplate;
    }

    /**
     * @param rolloverCondition must be non-null
     */
    public void setRolloverCondition(RolloverCondition rolloverCondition) {
        notNull(rolloverCondition, "rolloverCondition must be non-null");
        this.rolloverCondition = rolloverCondition;
    }

    /**
     * @param defaultKlvHandler must be non-null
     */
    public void setDefaultKlvHandler(KlvHandler defaultKlvHandler) {
        notNull(defaultKlvHandler, "defaultKlvHandler must be non-null");
        this.defaultKlvHandler = defaultKlvHandler;
    }

    /**
     * @param stanag4609Processor must be non-null
     */
    public void setStanag4609Processor(Stanag4609Processor stanag4609Processor) {
        notNull(stanag4609Processor, "stanage4609Processor must be non-null");
        this.stanag4609Processor = stanag4609Processor;
    }

    /**
     * Returns an array of ChannelHandler objects used by Netty as the pipeline.
     *
     * @return non-null array of channel handlers
     */
    public ChannelHandler[] createChannelHandlers() {
        return new ChannelHandler[] {new RawUdpDataToMTSPacketDecoder(packetBuffer),
                new MTSPacketToPESPacketDecoder(), new PESPacketToApplicationDataDecoder(
                IS_KLV_PARSING_ENABLED), new DecodedStreamDataHandler(packetBuffer,
                stanag4609Processor,
                klvHandlerMap,
                defaultKlvHandler,
                klvHandlerMapLock)};
    }

}
