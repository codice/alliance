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

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;
import ddf.security.Subject;
import io.netty.channel.ChannelHandler;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.StreamMonitor;
import org.codice.alliance.video.stream.mpegts.UdpStreamMonitor;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.codice.alliance.video.stream.mpegts.plugins.StreamCreationException;
import org.codice.alliance.video.stream.mpegts.plugins.StreamCreationPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamEndPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamShutdownException;
import org.codice.alliance.video.stream.mpegts.plugins.StreamShutdownPlugin;
import org.codice.alliance.video.stream.mpegts.rollover.BooleanOrRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.ElapsedTimeRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.MegabyteCountRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverAction;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverActionException;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.codice.ddf.platform.util.uuidgenerator.UuidGenerator;
import org.codice.ddf.security.common.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class UdpStreamProcessor implements StreamProcessor {

  public static final long MAX_METACARD_UPDATE_INITIAL_DELAY = TimeUnit.MINUTES.toSeconds(1);

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpStreamProcessor.class);

  /** Number of seconds to delay metacard updates. */
  private static final long DEFAULT_METACARD_UPDATE_INITIAL_DELAY = 2;

  private final Context context;

  private PacketBuffer packetBuffer = new PacketBuffer();

  private RolloverCondition rolloverCondition;

  private String filenameTemplate;

  private FilenameGenerator filenameGenerator;

  private Timer timer = new Timer();

  private List<MetacardType> metacardTypeList;

  private RolloverAction rolloverAction;

  private CatalogFramework catalogFramework;

  private StreamMonitor streamMonitor;

  private long metacardUpdateInitialDelay = DEFAULT_METACARD_UPDATE_INITIAL_DELAY;

  private StreamCreationPlugin streamCreationPlugin;

  private StreamShutdownPlugin streamShutdownPlugin;

  private Subject subject = null;

  private Subject streamCreationSubject;

  private MetacardUpdater parentMetacardUpdater;

  private StreamEndPlugin streamEndPlugin;

  private UuidGenerator uuidGenerator;

  public UdpStreamProcessor(StreamMonitor streamMonitor) {
    this.streamMonitor = streamMonitor;
    context = new Context(this);
  }

  public Subject getSubject() {
    return subject;
  }

  public void setSubject(Subject subject) {
    this.subject = subject;
  }

  /** @param streamCreationPlugin must be non-null */
  public void setStreamCreationPlugin(StreamCreationPlugin streamCreationPlugin) {
    notNull(streamCreationPlugin, "streamCreationPlugin must be non-null");
    this.streamCreationPlugin = streamCreationPlugin;
  }

  @Nullable
  public Double getDistanceTolerance() {
    return context.getGeometryOperatorContext().getDistanceTolerance();
  }

  public void setDistanceTolerance(@Nullable Double distanceTolerance) {
    context.getGeometryOperatorContext().setDistanceTolerance(distanceTolerance);
  }

  @Override
  public long getMetacardUpdateInitialDelay() {
    return metacardUpdateInitialDelay;
  }

  /**
   * @param metacardUpdateInitialDelay must be non-null and &gt;=0 and &lt;={@link
   *     #MAX_METACARD_UPDATE_INITIAL_DELAY}
   */
  public void setMetacardUpdateInitialDelay(Long metacardUpdateInitialDelay) {
    notNull(metacardUpdateInitialDelay, "metacardUpdateInitialDelay must be non-null");
    Validate.inclusiveBetween(
        0,
        MAX_METACARD_UPDATE_INITIAL_DELAY,
        metacardUpdateInitialDelay,
        String.format(
            "metacardUpdateInitialDelay must be >=0 and <=%d", MAX_METACARD_UPDATE_INITIAL_DELAY));
    this.metacardUpdateInitialDelay = metacardUpdateInitialDelay;
  }

  @Override
  public Optional<URI> getStreamUri() {
    return streamMonitor.getStreamUri();
  }

  @Override
  public Optional<String> getTitle() {
    return streamMonitor.getTitle();
  }

  @Override
  public String toString() {
    return "UdpStreamProcessor{"
        + ", filenameGenerator="
        + filenameGenerator
        + ", filenameTemplate='"
        + filenameTemplate
        + '\''
        + ", metacardTypeList="
        + metacardTypeList
        + ", packetBuffer="
        + packetBuffer
        + ", rolloverCondition="
        + rolloverCondition
        + ", metacardUpdateInitialDelay="
        + metacardUpdateInitialDelay
        + ", parentMetacardUpdater="
        + parentMetacardUpdater
        + '}';
  }

  /** @param count must be non-null and positive */
  public void setMegabyteCountRolloverCondition(Integer count) {
    notNull(count, "count must be non-null");
    inclusiveBetween(
        UdpStreamMonitor.MEGABYTE_COUNT_MIN,
        UdpStreamMonitor.MEGABYTE_COUNT_MAX,
        count,
        "count must be >0");
    rolloverCondition.accept(
        new RolloverCondition.Visitor() {
          @Override
          public void visit(BooleanOrRolloverCondition condition) {
            // This method is not expected to be called
          }

          @Override
          public void visit(ElapsedTimeRolloverCondition condition) {
            // This method is not expected to be called
          }

          @Override
          public void visit(MegabyteCountRolloverCondition condition) {
            condition.setMegabyteCountThreshold(count);
          }
        });
  }

  /** @param milliseconds must be non-null and positive */
  public void setElapsedTimeRolloverCondition(Long milliseconds) {
    notNull(milliseconds, "milliseconds must be non-null");
    inclusiveBetween(
        UdpStreamMonitor.ELAPSED_TIME_MIN,
        UdpStreamMonitor.ELAPSED_TIME_MAX,
        milliseconds,
        "milliseconds must be >0");
    rolloverCondition.accept(
        new RolloverCondition.Visitor() {
          @Override
          public void visit(BooleanOrRolloverCondition condition) {
            // This method is not expected to be called
          }

          @Override
          public void visit(ElapsedTimeRolloverCondition condition) {
            condition.setElapsedTimeThreshold(milliseconds);
          }

          @Override
          public void visit(MegabyteCountRolloverCondition condition) {
            // This method is not expected to be called
          }
        });
  }

  public PacketBuffer getPacketBuffer() {
    return packetBuffer;
  }

  /**
   * Shutdown the stream processor. Attempts to flush and ingest any partial stream data regardless
   * of IDR boundaries.
   */
  public void shutdown() {
    LOGGER.trace("Shutting down stream processor.");
    packetBuffer.cancelTimer();

    Security security = Security.getInstance();

    security.runAsAdmin(
        () -> {
          if (streamCreationSubject == null) {
            streamCreationSubject = security.getSystemSubject();
          }

          streamCreationSubject.execute(
              () -> {
                try {
                  streamShutdownPlugin.onShutdown(context);
                } catch (StreamShutdownException e) {
                  LOGGER.debug("unable to run stream shutdown plugin", e);
                }
                return null;
              });
          return null;
        });
  }

  public void setStreamEndPlugin(StreamEndPlugin streamEndPlugin) {
    this.streamEndPlugin = streamEndPlugin;
  }

  public void checkForRollover() {
    RotateResult rotateResult = packetBuffer.rotate(rolloverCondition);
    rotateResult.getFile().ifPresent(this::doRollover);
    if (rotateResult.isTimeout()) {
      streamEndPlugin.streamEnded(context);
    }
  }

  public void doRollover(File tempFile) {
    LOGGER.debug("performing video chunk rollover: tempFile={}", tempFile);
    try {
      rolloverAction.doAction(tempFile);
    } catch (RolloverActionException e) {
      LOGGER.debug("unable handle rollover file: tempFile={}", tempFile, e);
    } finally {
      if (!tempFile.delete()) {
        LOGGER.debug("unable to delete temp file: filename={}", tempFile);
      }
    }
  }

  private boolean areNonNull(List<Object> listofObjects) {
    return listofObjects.stream().allMatch(Objects::nonNull);
  }

  /**
   * Return <code>true</code> if the processor is ready to run.
   *
   * @return ready status
   */
  public boolean isReady() {
    return areNonNull(
        Arrays.asList(
            rolloverCondition,
            filenameTemplate,
            filenameGenerator,
            metacardTypeList,
            catalogFramework,
            streamCreationPlugin,
            parentMetacardUpdater));
  }

  public void setRolloverAction(RolloverAction rolloverAction) {
    this.rolloverAction = rolloverAction;
  }

  public CatalogFramework getCatalogFramework() {
    return catalogFramework;
  }

  /** @param catalogFramework must be non-null */
  public void setCatalogFramework(CatalogFramework catalogFramework) {
    notNull(catalogFramework, "catalogFramework must be non-null");
    this.catalogFramework = catalogFramework;
  }

  public FilenameGenerator getFilenameGenerator() {
    return filenameGenerator;
  }

  /** @param filenameGenerator must be non-null */
  public void setFilenameGenerator(FilenameGenerator filenameGenerator) {
    notNull(filenameGenerator, "filenameGenerator must be non-null");
    this.filenameGenerator = filenameGenerator;
  }

  public String getFilenameTemplate() {
    return filenameTemplate;
  }

  /** @param filenameTemplate must be non-null */
  public void setFilenameTemplate(String filenameTemplate) {
    notNull(filenameTemplate, "filenameTemplate must be non-null");
    this.filenameTemplate = filenameTemplate;
  }

  public List<MetacardType> getMetacardTypeList() {
    return metacardTypeList;
  }

  /** @param metacardTypeList must be non-null */
  public void setMetacardTypeList(List<MetacardType> metacardTypeList) {
    notNull(metacardTypeList, "metacardTypeList must be non-null");
    this.metacardTypeList = metacardTypeList;
  }

  public Timer getTimer() {
    return timer;
  }

  public void setTimer(Timer timer) {
    this.timer = timer;
  }

  /**
   * Only used for testing so unit tests can mock the subject.
   *
   * @param streamCreationSubject security subject
   */
  void setStreamCreationSubject(Subject streamCreationSubject) {
    this.streamCreationSubject = streamCreationSubject;
  }

  /**
   * Initializes the processor. Users should call {@link #isReady()} first to make sure the
   * processor is ready to run.
   */
  public void init() {
    Security security = Security.getInstance();

    security.runAsAdmin(
        () -> {
          if (streamCreationSubject == null) {
            streamCreationSubject = security.getSystemSubject();
          }

          streamCreationSubject.execute(
              () -> {
                try {
                  streamCreationPlugin.onCreate(context);
                } catch (StreamCreationException e) {
                  LOGGER.debug("unable to run stream creation plugin", e);
                }
                return null;
              });
          return null;
        });
  }

  /** @param rolloverCondition must be non-null */
  public void setRolloverCondition(RolloverCondition rolloverCondition) {
    notNull(rolloverCondition, "rolloverCondition must be non-null");
    this.rolloverCondition = rolloverCondition;
  }

  /**
   * Returns an array of ChannelHandler objects used by Netty as the pipeline.
   *
   * @return non-null array of channel handlers
   */
  public ChannelHandler[] createChannelHandlers() {
    return new ChannelHandler[] {
      new RawUdpDataToMTSPacketDecoder(packetBuffer, this),
      new MTSPacketToPESPacketDecoder(),
      new PESPacketToApplicationDataDecoder(),
      new DecodedStreamDataHandler(packetBuffer)
    };
  }

  /** @param streamShutdownPlugin must be non-null */
  public void setStreamShutdownPlugin(StreamShutdownPlugin streamShutdownPlugin) {
    notNull(streamShutdownPlugin, "streamShutdownPlugin must be non-null");
    this.streamShutdownPlugin = streamShutdownPlugin;
  }

  public MetacardUpdater getParentMetacardUpdater() {
    return parentMetacardUpdater;
  }

  public void setParentMetacardUpdater(MetacardUpdater parentMetacardUpdater) {
    this.parentMetacardUpdater = parentMetacardUpdater;
  }

  public UuidGenerator getUuidGenerator() {
    return uuidGenerator;
  }

  public void setUuidGenerator(UuidGenerator uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }
}
