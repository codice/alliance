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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.codice.alliance.video.stream.mpegts.OutputStreamFactory;
import org.codice.alliance.video.stream.mpegts.filename.TempFileGenerator;
import org.codice.alliance.video.stream.mpegts.filename.TempFileGeneratorImpl;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buffers raw MPEG-TS packet data and writes the data to a temporary file so that the data written
 * is on a clean IDR boundary. If an IDR boundary cannot be found, the data will be eventually flush
 * on a arbitrary point to avoid memory exhaustion. This implementation is thread-safe.
 *
 * <p>NOTE: This implementation could probably be improved by using some kind of circular buffer
 * with read and write pointers
 */
public class PacketBuffer {

  /**
   * After this number of milliseconds of no activity, the current frameset in memory will be
   * considered complete.
   */
  public static final long ACTIVITY_TIMEOUT = TimeUnit.SECONDS.toMillis(1);

  private static final Logger LOGGER = LoggerFactory.getLogger(PacketBuffer.class);

  /**
   * If we receive an IDR frame followed by an unbounded number of non-IDR frames, then memory will
   * be exhausted. This limits the frameset size while detecting a complete frameset when flushing
   * to disk.
   */
  private static final long DEFAULT_MAX_FRAMESET_SIZE = 1000;

  /**
   * The default maximum number of incomplete frame bytes before the data is forcibly flushed to
   * disk.
   */
  private static final long DEFAULT_MAX_INCOMPLETE_FRAME_BYTES = 50000000;

  /** This is a RolloverCondition that always indicates that the rollover is ready. */
  private static final RolloverCondition ALWAYS_TRUE =
      new RolloverCondition() {
        @Override
        public boolean isRolloverReady(PacketBuffer packetBuffer) {
          return true;
        }

        @Override
        public void accept(Visitor visitor) {
          // This method is not expected to be called
        }
      };

  private static final long ACTIVITY_LOG_PERIOD = TimeUnit.SECONDS.toMillis(10);

  private final Timer timer = new Timer();

  private List<Frame> frames = new ArrayList<>();

  private List<byte[]> incompleteFrame = new ArrayList<>();

  private Lock lock = new ReentrantLock();

  private TempFileGenerator tempFileGenerator = new TempFileGeneratorImpl();

  private File currentTempFile = null;

  private Long tempFileCreateTime = null;

  private long bytesWrittenToTempFile = 0;

  private long incompleteFrameBytes = 0;

  private long maxIncompleteFrameBytes = DEFAULT_MAX_INCOMPLETE_FRAME_BYTES;

  private OutputStreamFactory outputStreamFactory = FileOutputStream::new;

  private long bytesReceived = 0;

  private long packetsReceived = 0;

  private long bytesWritten = 0;

  private long filesWritten = 0;

  /**
   * Timestamp of most recent activity. Updated to current time when a packet is sent to the
   * PacketBuffer.
   */
  private long lastActivity = 0;

  /** By default, new Date objects are created by calling {@link Date#Date()}. */
  private Supplier<Date> dateSupplier = Date::new;

  public PacketBuffer() {
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            LOGGER.debug(
                "packet buffer activity: bytesReceived={} packetsReceived={} bytesWritten={} filesWritten={}",
                bytesReceived,
                packetsReceived,
                bytesWritten,
                filesWritten);
          }
        },
        0,
        ACTIVITY_LOG_PERIOD);
  }

  /** @param tempFileGenerator must be non-null */
  public void setTempFileGenerator(TempFileGenerator tempFileGenerator) {
    notNull(tempFileGenerator, "temFileGenerator must be non-null");
    this.tempFileGenerator = tempFileGenerator;
  }

  /** @param outputStreamFactory must be non-null */
  public void setOutputStreamFactory(OutputStreamFactory outputStreamFactory) {
    notNull(outputStreamFactory, "outputStreamFactory must be non-null");
    this.outputStreamFactory = outputStreamFactory;
  }

  /** @param maxIncompleteFrameBytes must be non-null */
  public void setMaxIncompleteFrameBytes(long maxIncompleteFrameBytes) {
    notNull(maxIncompleteFrameBytes, "maxIncompleteFrameBytes must be non-null");
    this.maxIncompleteFrameBytes = maxIncompleteFrameBytes;
  }

  @Override
  public String toString() {
    return "PacketBuffer{"
        + "bytesWrittenToTempFile="
        + bytesWrittenToTempFile
        + ", incompleteFrameBytes="
        + incompleteFrameBytes
        + '}';
  }

  /** Clear all stored data and reset to the initial state. */
  public void reset() {
    lock.lock();
    try {
      frames.clear();
      incompleteFrame.clear();
      currentTempFile = null;
      tempFileCreateTime = null;
      bytesWrittenToTempFile = 0;
      incompleteFrameBytes = 0;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Get the age (milliseconds) of the temporary data file. Returns 0 if there is no file.
   *
   * @return age in milliseconds
   */
  public long getAge() {
    lock.lock();
    try {
      return tempFileCreateTime == null ? 0 : dateSupplier.get().getTime() - tempFileCreateTime;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Get the number of bytes that have been written to the temporary data file.
   *
   * @return bytes
   */
  public long getByteCount() {
    return bytesWrittenToTempFile;
  }

  /**
   * Write raw data into the buffer. Empty or null values are handled. If the size of the incomplete
   * frame data exceeds {@link #maxIncompleteFrameBytes}, then the current incomplete frame data
   * will be push to the frame list as type {@link FrameType#UNKNOWN} and a flush to disk will be
   * attempted.
   *
   * @param rawPacket may be null or empty
   */
  public void write(byte[] rawPacket) {

    if (rawPacket == null || rawPacket.length == 0) {
      return;
    }
    lock.lock();
    try {
      lastActivity = System.currentTimeMillis();
      incompleteFrame.add(rawPacket);
      incompleteFrameBytes += rawPacket.length;
      bytesReceived += rawPacket.length;
      packetsReceived++;
      if (incompleteFrameBytes > maxIncompleteFrameBytes) {
        frames.add(new Frame(FrameType.UNKNOWN, incompleteFrame));
        incompleteFrame = new ArrayList<>();
        incompleteFrameBytes = 0;
        flushIfDataAvailable();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Tell the packet buffer that the recently written data represents a complete frame. A flush to
   * disk will be attempted.
   *
   * @param frameType must be non-null
   */
  public void frameComplete(FrameType frameType) {
    notNull(frameType, "frameType must be non-null");
    lock.lock();
    try {
      frames.add(new Frame(frameType, incompleteFrame));
      incompleteFrame = new ArrayList<>();

      flushIfDataAvailable();

    } finally {
      lock.unlock();
    }
  }

  /** If a full frameset is in the frame list, then flush the frameset to disk. */
  private void flushIfDataAvailable() {
    findLastFramesetIndex()
        .ifPresent(
            (index) -> {
              try {
                flushFrameset(index);
              } catch (IOException e) {
                LOGGER.debug("unable to write to temp file", e);
              }
            });
  }

  private void flushAllData() throws IOException {
    flushFrameset(frames.size() - 1);
  }

  /**
   * @param index the index of the last frame of the last frameset
   * @throws IOException
   */
  private void flushFrameset(int index) throws IOException {

    try (OutputStream os = outputStreamFactory.create(getTempFile(), true)) {

      List<byte[]> outgoingPackets =
          frames
              .subList(0, index + 1)
              .stream()
              .flatMap(frame -> frame.packets.stream())
              .collect(Collectors.toList());
      frames = new ArrayList<>(frames.subList(index + 1, frames.size()));

      for (byte[] outgoingPacket : outgoingPackets) {
        os.write(outgoingPacket);
        bytesWrittenToTempFile += outgoingPacket.length;
        bytesWritten += outgoingPacket.length;
      }
    }
  }

  /**
   * If the rollover condition is not met, then the method will return {@link Optional#empty()}. If
   * the rollover condition is met, then the method <b>may</b> return a temp file. The only reason a
   * temp file may not be returned is when the condition is <code>true</code> even when no data has
   * been written to file. The caller is responsible for deleting the temp file.
   *
   * @param rolloverCondition the rollover condition
   * @return an optional temp file
   */
  public RotateResult rotate(RolloverCondition rolloverCondition) {
    lock.lock();
    try {
      if (isActivityTimeout()) {
        LOGGER.debug("activity timeout detected, flushing data and rolling over file");
        if (!incompleteFrame.isEmpty()) {
          flushIncompleteFrames();
        }
        flushIfDataAvailable();
        return new RotateResult(getFile().orElse(null), true);
      }

      flushIfDataAvailable();

      if (!rolloverCondition.isRolloverReady(this)) {
        return new RotateResult(null, false);
      }
      if (currentTempFile == null || bytesWrittenToTempFile == 0) {
        return new RotateResult(null, false);
      }
      return new RotateResult(getFile().orElse(null), false);
    } finally {
      lock.unlock();
    }
  }

  private Optional<File> getFile() {
    File tempFile = currentTempFile;
    currentTempFile = null;
    bytesWrittenToTempFile = 0;
    return Optional.of(tempFile);
  }

  private void flushIncompleteFrames() {
    frames.add(new Frame(FrameType.UNKNOWN, incompleteFrame));
    incompleteFrame = new ArrayList<>();
  }

  /**
   * Flush all buffered data to disk and rotate. Will return {@link Optional#empty()} if no data has
   * been written to file. The caller is responsible for deleting the temp file.
   *
   * @return an optional temp file
   * @throws IOException
   */
  public RotateResult flushAndRotate() throws IOException {
    lock.lock();
    try {

      if (!incompleteFrame.isEmpty()) {
        flushIncompleteFrames();
      }

      if (!frames.isEmpty()) {
        flushAllData();
      }

      if (bytesWrittenToTempFile == 0) {
        return new RotateResult(null, false);
      }

      return rotate(ALWAYS_TRUE);
    } finally {
      lock.unlock();
    }
  }

  public void cancelTimer() {
    timer.cancel();
  }

  private File getTempFile() throws IOException {
    if (currentTempFile == null) {
      tempFileCreateTime = dateSupplier.get().getTime();
      bytesWrittenToTempFile = 0;
      currentTempFile = tempFileGenerator.generate();
      filesWritten++;
    }
    return currentTempFile;
  }

  private Set<FrameType> summarizeFrameTypes() {
    return frames.stream().map(frame -> frame.frameType).collect(Collectors.toSet());
  }

  private long millisSinceLastActivity() {
    return lastActivity == 0 ? 0 : System.currentTimeMillis() - lastActivity;
  }

  private boolean isActivityTimeout() {
    return millisSinceLastActivity() >= ACTIVITY_TIMEOUT;
  }

  private Optional<Integer> allFrames() {
    return Optional.of(frames.size() - 1);
  }

  private boolean isMaxFramesetSizeExceeded() {
    return frames.size() > DEFAULT_MAX_FRAMESET_SIZE;
  }

  private boolean isAllUnknownFrameType(Set<FrameType> frameTypeSummary) {
    return frameTypeSummary.size() == 1 && frameTypeSummary.contains(FrameType.UNKNOWN);
  }

  /**
   * Search the frame list for the last frame in a frameset. This can only be detected when the
   * following occurs: IDR? NON-IDR* IDR. We are never guaranteed to have the leading IDR because we
   * could start reading a stream in the middle of a frameset. If the frame list only contains
   * UNKNOWN frame types, then always return the last index of the frame list. If the frame list
   * contains more than maxFramesetSize, then it is considered to be a complete framset in order to
   * avoid memory exhaustion.
   *
   * @return non-null optional value, may contain index to the last frame in a frameset
   */
  private Optional<Integer> findLastFramesetIndex() {

    if (isActivityTimeout() || isMaxFramesetSizeExceeded()) {
      resetLastActivity();
      return allFrames();
    }

    Set<FrameType> frameTypeSummary = summarizeFrameTypes();

    if (isAllUnknownFrameType(frameTypeSummary)) {
      return allFrames();
    }

    if (!frameTypeSummary.contains(FrameType.IDR)) {
      return Optional.empty();
    }

    for (int i = frames.size() - 1; i > 0; i--) {
      if (frames.get(i).frameType == FrameType.IDR) {
        return Optional.of(i - 1);
      }
    }
    return Optional.empty();
  }

  private void resetLastActivity() {
    lastActivity = 0;
  }

  public enum FrameType {
    IDR,
    NON_IDR,
    UNKNOWN
  }

  /**
   * Contains all of the raw packets associated with a video frame. May contain non-video data that
   * was intermixed with the video data.
   */
  private static class Frame {

    private List<byte[]> packets;

    private FrameType frameType;

    public Frame(FrameType frameType, List<byte[]> packets) {
      this.frameType = frameType;
      this.packets = packets;
    }
  }
}
