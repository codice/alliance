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
package org.codice.alliance.video.stream.mpegts;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.metacard.MetacardUpdater;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.alliance.video.stream.mpegts.plugins.StreamCreationPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamEndPlugin;
import org.codice.alliance.video.stream.mpegts.plugins.StreamShutdownPlugin;
import org.codice.alliance.video.stream.mpegts.rollover.MegabyteCountRolloverCondition;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.codice.ddf.platform.util.uuidgenerator.UuidGenerator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a Netty server with a pipeline specified by {@link UdpStreamProcessor}. The following
 * properties must be set:
 *
 * <ul>
 *   <li>{@link #setMonitoredAddress(String)}
 *   <li>{@link #setFilenameTemplate(String)}
 *   <li>{@link #setRolloverCondition(RolloverCondition)}
 *   <li>{@link #setFilenameGenerator(FilenameGenerator)}
 *   <li>{@link #setMetacardTypeList(List)}
 *   <li>{@link #setCatalogFramework(CatalogFramework)}
 *       <p>
 *       <p>NOTE: The unicast and multicast code can not be unit tested in a meaningful manner. And
 *       only unicast can be itest'ed. If any changes are made to the unicast/multicast code, be
 *       sure to manually test.
 *       <p>
 * </ul>
 */
public class UdpStreamMonitor implements StreamMonitor {

  public static final long MEGABYTE_COUNT_MIN = MegabyteCountRolloverCondition.MIN_VALUE;

  public static final long MEGABYTE_COUNT_MAX = MegabyteCountRolloverCondition.MAX_VALUE;

  public static final long ELAPSED_TIME_MIN = 1;

  public static final long ELAPSED_TIME_MAX = Long.MAX_VALUE;

  /** This is the id string used in metatype.xml. */
  public static final String METATYPE_TITLE = "parentTitle";

  /** This is the id string used in metatype.xml. */
  public static final String METATYPE_MONITORED_ADDRESS = "monitoredAddress";

  /** This is the id string used in metatype.xml. */
  public static final String METATYPE_BYTE_COUNT_ROLLOVER_CONDITION =
      "megabyteCountRolloverCondition";

  /** This is the id string used in metatype.xml. */
  public static final String METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION =
      "elapsedTimeRolloverCondition";

  /** This is the id string used in metatype.xml. */
  public static final String METATYPE_METACARD_UPDATE_INITIAL_DELAY = "metacardUpdateInitialDelay";

  /** This is the id string used in metatype.xml. */
  public static final String METATYPE_FILENAME_TEMPLATE = "filenameTemplate";

  public static final String METATYPE_DISTANCE_TOLERANCE = "distanceTolerance";

  public static final String METATYPE_NETWORK_INTERFACE = "networkInterface";

  static final int MONITORED_PORT_MIN = 1;

  static final int MONITORED_PORT_MAX = 65535;

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpStreamMonitor.class);

  /** This is the id string used in metatype.xml. */
  private static final String METATYPE_PARENT_TITLE = "parentTitle";

  private ChannelFuture channelFuture;

  private final UdpStreamProcessor udpStreamProcessor;

  private String monitoredAddress;

  private Integer monitoredPort;

  private EventLoopGroup eventLoopGroup;

  private Thread serverThread;

  private String parentTitle;

  private Integer byteCountRolloverCondition;

  private Long elapsedTimeRolloverCondition;

  private URI streamUri;

  private Boolean startImmediately = false;

  private boolean monitoring;

  private String filenameTemplate;

  private Date startTime;

  private String networkInterface;

  public UdpStreamMonitor(final BundleContext bundleContext) {
    udpStreamProcessor = new UdpStreamProcessor(this, bundleContext);
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

  public Boolean getStartImmediately() {
    return this.startImmediately;
  }

  public void setStartImmediately(Boolean startImmediately) {
    this.startImmediately = startImmediately;
  }

  public Double getDistanceTolerance() {
    return udpStreamProcessor.getDistanceTolerance();
  }

  public void setDistanceTolerance(Double distanceTolerance) {
    udpStreamProcessor.setDistanceTolerance(distanceTolerance);
  }

  public String getNetworkInterface() {
    return this.networkInterface;
  }

  /** @param networkInterface may be null or empty/blank string */
  public void setNetworkInterface(String networkInterface) {
    if (StringUtils.isBlank(networkInterface)) {
      this.networkInterface = null;
    } else {
      this.networkInterface = networkInterface;
    }
  }

  /** @param parentMetacardUpdater must be non-null */
  public void setParentMetacardUpdater(MetacardUpdater parentMetacardUpdater) {
    notNull(parentMetacardUpdater, "parentMetacardUpdater must be non-null");
    udpStreamProcessor.setParentMetacardUpdater(parentMetacardUpdater);
  }

  public Long getMetacardUpdateInitialDelay() {
    return udpStreamProcessor.getMetacardUpdateInitialDelay();
  }

  /**
   * @param metacardUpdateInitialDelay must be non-null and &gt;=0 and &lt;={@link
   *     UdpStreamProcessor#MAX_METACARD_UPDATE_INITIAL_DELAY}
   */
  public void setMetacardUpdateInitialDelay(Long metacardUpdateInitialDelay) {
    udpStreamProcessor.setMetacardUpdateInitialDelay(metacardUpdateInitialDelay);
  }

  /** @param filenameGenerator must be non-null */
  public void setFilenameGenerator(FilenameGenerator filenameGenerator) {
    notNull(filenameGenerator, "filenameGenerator must be non-null");
    udpStreamProcessor.setFilenameGenerator(filenameGenerator);
  }

  /** @param template must be non-null and non-blank */
  public void setFilenameTemplate(String template) {
    notNull(template, "template must be non-null");
    notBlank(template, "template must be non-blank");
    filenameTemplate = template;
    udpStreamProcessor.setFilenameTemplate(template);
  }

  public String getFileNameTemplate() {
    return this.filenameTemplate;
  }

  /** @param rolloverCondition must be non-null */
  public void setRolloverCondition(RolloverCondition rolloverCondition) {
    notNull(rolloverCondition, "rolloverCondition must be non-null");
    udpStreamProcessor.setRolloverCondition(rolloverCondition);
  }

  public void setUuidGenerator(final UuidGenerator uuidGenerator) {
    notNull(uuidGenerator, "uuidGenerator must be non-null");
    udpStreamProcessor.setUuidGenerator(uuidGenerator);
  }

  private boolean isReady() {
    return monitoredAddress != null && udpStreamProcessor.isReady();
  }

  /**
   * Called by osgi to initialize the monitor. Makes sure it is already shutdown before
   * initializing. Will throw a RuntimeException if not properly initialized.
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
    LOGGER.debug("start monitoring the udp stream");
    shutdown();
    if (isReady()) {
      udpStreamProcessor.init();
      serverThread = new Thread(new Server());
      serverThread.start();
      monitoring = true;
      startTime = new Date();
    } else {
      throw new RuntimeException(
          String.format(
              "the udp stream monitor cannot be initialized because it is not properly configured: monitoredAddress=%s, monitoredPort=%s, udpStreamProcessor=%s",
              monitoredAddress, monitoredPort, udpStreamProcessor));
    }
  }

  @Override
  public void stopMonitoring() {
    LOGGER.debug("stop monitoring the udp stream");
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

  /** @param metacardTypeList must be non-null */
  public void setMetacardTypeList(List<MetacardType> metacardTypeList) {
    notNull(metacardTypeList, "metacardTypeList must be non-null");
    udpStreamProcessor.setMetacardTypeList(metacardTypeList);
  }

  /** @param catalogFramework must be non-null */
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
    if (eventLoopGroup != null) {
      try {
        eventLoopGroup.shutdownGracefully().sync();
      } catch (InterruptedException e) {
        LOGGER.debug("Graceful shutdown of channel interrupted", e);
        Thread.currentThread().interrupt();
      }
    }

    if (channelFuture != null) {
      try {
        channelFuture.channel().closeFuture().sync();
      } catch (InterruptedException e) {
        LOGGER.debug("Graceful shutdown of channel future interrupted", e);
        Thread.currentThread().interrupt();
      }
    }

    if (serverThread != null) {
      LOGGER.debug("shutting down monitor server thread");
      joinServerThread();
      serverThread = null;
    }

    if (udpStreamProcessor != null) {
      udpStreamProcessor.shutdown();
    }

    channelFuture = null;
  }

  private void joinServerThread() {
    try {
      serverThread.join();
    } catch (InterruptedException e) {
      LOGGER.debug("interrupted while waiting for server thread to join", e);
      Thread.currentThread().interrupt();
    } finally {
      monitoring = false;
      startTime = null;
    }
  }

  /**
   * The StreamEndPlugin gets called when a stream ends by either being stopped or timed-out.
   *
   * @param streamEndPlugin the plugin
   */
  public void setStreamEndPlugin(StreamEndPlugin streamEndPlugin) {
    udpStreamProcessor.setStreamEndPlugin(streamEndPlugin);
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

      if (properties.containsKey(METATYPE_NETWORK_INTERFACE)
          && !checkMetaTypeClass(properties, METATYPE_NETWORK_INTERFACE, String.class)) {
        return;
      }

      if (!checkMetaTypeClass(properties, METATYPE_BYTE_COUNT_ROLLOVER_CONDITION, Integer.class)) {
        return;
      }
      if (!checkMetaTypeClass(properties, METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION, Long.class)) {
        return;
      }
      if (!checkMetaTypeClass(properties, METATYPE_FILENAME_TEMPLATE, String.class)) {
        return;
      }
      if (!checkMetaTypeClass(properties, METATYPE_PARENT_TITLE, String.class)) {
        return;
      }
      if (!checkMetaTypeClass(properties, METATYPE_METACARD_UPDATE_INITIAL_DELAY, Long.class)) {
        return;
      }

      if (!checkMetaTypeClass(properties, METATYPE_TITLE, String.class)) {
        return;
      }

      if (properties.containsKey(METATYPE_DISTANCE_TOLERANCE)
          && properties.get(METATYPE_DISTANCE_TOLERANCE) != null
          && !checkMetaTypeClass(properties, METATYPE_DISTANCE_TOLERANCE, Double.class)) {
        return;
      }

      setMonitoredAddress((String) properties.get(METATYPE_MONITORED_ADDRESS));
      setNetworkInterface((String) properties.get(METATYPE_NETWORK_INTERFACE));
      setMegabyteCountRolloverCondition(
          (Integer) properties.get(METATYPE_BYTE_COUNT_ROLLOVER_CONDITION));
      setElapsedTimeRolloverCondition(
          (Long) properties.get(METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION));
      setFilenameTemplate((String) properties.get(METATYPE_FILENAME_TEMPLATE));
      setMetacardUpdateInitialDelay((Long) properties.get(METATYPE_METACARD_UPDATE_INITIAL_DELAY));
      setParentTitle((String) properties.get(METATYPE_PARENT_TITLE));
      setDistanceTolerance((Double) properties.get(METATYPE_DISTANCE_TOLERANCE));

      init();
    }
  }

  private boolean checkMetaTypeClass(
      Map<String, Object> properties, String fieldName, Class<?> clazz) {
    if (!properties.containsKey(fieldName)) {
      LOGGER.debug("the metatype id {} field was null", fieldName);
      return false;
    }
    if (!clazz.isInstance(properties.get(fieldName))) {
      LOGGER.debug("the metatype id {} field should be type {}", fieldName, clazz);
      return false;
    }
    return true;
  }

  public String getMonitoredAddress() {
    return monitoredAddress;
  }

  /** @param monitoredAddress must be non-null and resolvable */
  public void setMonitoredAddress(String monitoredAddress) {
    notNull(monitoredAddress, "monitoredAddress must be non-null");

    URI uri;

    try {
      uri = new URI(monitoredAddress);
      InetAddress.getByName(uri.getHost());
      if (uri.getScheme() == null || !uri.getScheme().equals("udp")) {
        throw new URISyntaxException(uri.toString(), "Monitored Address is not UDP protocol");
      }
    } catch (UnknownHostException | URISyntaxException e) {
      throw new IllegalArgumentException(
          String.format(
              "the monitored address could not be resolved: monitoredAddress=%s",
              monitoredAddress));
    }

    inclusiveBetween(MONITORED_PORT_MIN, MONITORED_PORT_MAX, uri.getPort());

    this.streamUri = uri;
    this.monitoredPort = uri.getPort();
    this.monitoredAddress = uri.getHost();
  }

  public Integer getByteCountRolloverCondition() {
    return this.byteCountRolloverCondition;
  }

  /** @param count must be non-null and positive */
  public void setMegabyteCountRolloverCondition(Integer count) {
    notNull(count, "count must be non-null");

    inclusiveBetween(
        MEGABYTE_COUNT_MIN,
        MEGABYTE_COUNT_MAX,
        count,
        String.format("count must be >=%d", MEGABYTE_COUNT_MIN));
    byteCountRolloverCondition = count;
    udpStreamProcessor.setMegabyteCountRolloverCondition(count);
  }

  public Long getElapsedTimeRolloverCondition() {
    return this.elapsedTimeRolloverCondition;
  }

  /** @param milliseconds must be non-null and &gt;= {@link #ELAPSED_TIME_MIN} */
  public void setElapsedTimeRolloverCondition(Long milliseconds) {
    notNull(milliseconds, "milliseconds must be non-null");
    inclusiveBetween(
        ELAPSED_TIME_MIN,
        ELAPSED_TIME_MAX,
        milliseconds,
        String.format("milliseconds must be >=%d", ELAPSED_TIME_MIN));
    this.elapsedTimeRolloverCondition = milliseconds;
    udpStreamProcessor.setElapsedTimeRolloverCondition(milliseconds);
  }

  private boolean isMulticast(String address) {

    try {
      return InetAddress.getByName(address).isMulticastAddress();
    } catch (UnknownHostException e) {
      LOGGER.debug("unable to convert string to InetAddress: value={}", address, e);
    }

    return false;
  }

  private Pair<NetworkInterface, InetAddress> create(NetworkInterface ni, InetAddress inetAddress) {
    return new ImmutablePair<>(ni, inetAddress);
  }

  private Optional<Pair<NetworkInterface, InetAddress>> findLocalAddress(String interfaceName) {

    try {

      NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);

      if (networkInterface != null) {
        return Collections.list(networkInterface.getInetAddresses()).stream()
            .filter(inetAddress -> inetAddress instanceof Inet4Address)
            .map(inetAddress -> create(networkInterface, inetAddress))
            .findFirst();
      }

    } catch (SocketException e) {
      LOGGER.debug("unable to find the network interface {}", interfaceName, e);
    }

    return Optional.empty();
  }

  private void runMulticastServer(
      Bootstrap bootstrap, NetworkInterface networkInterface, InetAddress inetAddress) {

    bootstrap
        .group(eventLoopGroup)
        .channelFactory(() -> new NioDatagramChannel(InternetProtocolFamily.IPv4))
        .handler(new Pipeline(udpStreamProcessor))
        .localAddress(inetAddress, monitoredPort)
        .option(ChannelOption.IP_MULTICAST_IF, networkInterface)
        .option(ChannelOption.SO_REUSEADDR, true);

    try {
      channelFuture = bootstrap.bind(monitoredPort).sync();
      NioDatagramChannel ch = (NioDatagramChannel) channelFuture.channel();

      ch.joinGroup(new InetSocketAddress(monitoredAddress, monitoredPort), networkInterface).sync();
    } catch (InterruptedException e) {
      LOGGER.debug("interrupted while waiting for shutdown", e);
      Thread.currentThread().interrupt();
    }
  }

  private void runUnicastServer(Bootstrap bootstrap) {
    bootstrap
        .group(eventLoopGroup)
        .channel(NioDatagramChannel.class)
        .handler(new Pipeline(udpStreamProcessor));
    try {
      channelFuture = bootstrap.bind(monitoredAddress, monitoredPort).sync();
    } catch (InterruptedException e) {
      LOGGER.debug("interrupted while waiting for shutdown", e);
      Thread.currentThread().interrupt();
    }
  }

  private static class Pipeline extends ChannelInitializer<NioDatagramChannel> {

    private final UdpStreamProcessor udpStreamProcessor;

    private Pipeline(UdpStreamProcessor udpStreamProcessor) {
      this.udpStreamProcessor = udpStreamProcessor;
    }

    @Override
    protected void initChannel(NioDatagramChannel nioDatagramChannel) {
      nioDatagramChannel.pipeline().addLast(udpStreamProcessor.createChannelHandlers());
    }
  }

  private class Server implements Runnable {

    @Override
    public void run() {

      LOGGER.debug(
          "starting udp listening thread: address={} port={}", monitoredAddress, monitoredPort);

      Bootstrap bootstrap = new Bootstrap();

      eventLoopGroup = new NioEventLoopGroup();

      if (isMulticast(monitoredAddress)) {

        Optional<Pair<NetworkInterface, InetAddress>> networkPair =
            findLocalAddress(networkInterface);

        if (networkPair.isPresent()) {

          runMulticastServer(bootstrap, networkPair.get().getKey(), networkPair.get().getValue());
        } else {
          LOGGER.debug(
              "cannot start multicast server because the IPv4 address for interface '{}' cannot be found",
              networkInterface);
        }

      } else {
        runUnicastServer(bootstrap);
      }
    }
  }
}
