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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.MetacardType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.alliance.video.stream.mpegts.filename.FilenameGenerator;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;
import org.codice.alliance.video.stream.mpegts.rollover.RolloverCondition;
import org.junit.Before;
import org.junit.Test;

public class UdpStreamMonitorTest {

  private UdpStreamProcessor udpStreamProcessor;

  private UdpStreamMonitor udpStreamMonitor;

  @Before
  public void setup() {
    udpStreamProcessor = mock(UdpStreamProcessor.class);
    udpStreamMonitor = new UdpStreamMonitor(udpStreamProcessor);
  }

  @Test
  public void testSetElapsedTimeRolloverCondition() {
    udpStreamMonitor.setElapsedTimeRolloverCondition(UdpStreamMonitor.ELAPSED_TIME_MIN);
    verify(udpStreamProcessor).setElapsedTimeRolloverCondition(UdpStreamMonitor.ELAPSED_TIME_MIN);
    assertThat(
        udpStreamMonitor.getElapsedTimeRolloverCondition(), is(UdpStreamMonitor.ELAPSED_TIME_MIN));
  }

  @Test(expected = NullPointerException.class)
  public void testSetElapsedTimeRolloverConditionNullArg() {
    udpStreamMonitor.setElapsedTimeRolloverCondition(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetElapsedTimeRolloverConditionBelowRangeArg() {
    udpStreamMonitor.setElapsedTimeRolloverCondition(UdpStreamMonitor.ELAPSED_TIME_MIN - 10);
  }

  @Test
  public void testSetStartImmediately() {
    assertThat(udpStreamMonitor.getStartImmediately(), is(false));
    udpStreamMonitor.setStartImmediately(true);
    assertThat(udpStreamMonitor.getStartImmediately(), is(true));
  }

  @Test
  public void testSetMegabyteCountRolloverCondition() {
    udpStreamMonitor.setMegabyteCountRolloverCondition(
        Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN));
    verify(udpStreamProcessor)
        .setMegabyteCountRolloverCondition(Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN));
    assertThat(
        udpStreamMonitor.getByteCountRolloverCondition(),
        is(Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN)));
  }

  @Test(expected = NullPointerException.class)
  public void testSetByteCountRolloverConditionNullArg() {
    udpStreamMonitor.setMegabyteCountRolloverCondition(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetByteCountRolloverConditionBelowRangeArg() {
    udpStreamMonitor.setMegabyteCountRolloverCondition(
        Math.toIntExact(UdpStreamMonitor.MEGABYTE_COUNT_MIN) - 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMonitoredAddressNoProtocol() {
    String addr = "127.0.0.1:50000";
    udpStreamMonitor.setMonitoredAddress(addr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMonitoredAddressUnsupportedProtocol() {
    String addr = "tcp://127.0.0.1";
    udpStreamMonitor.setMonitoredAddress(addr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMonitoredAddressNoPort() {
    String addr = "udp://127.0.0.1";
    udpStreamMonitor.setMonitoredAddress(addr);
  }

  @Test(expected = NullPointerException.class)
  public void testMonitoredAddressNullArg() {
    udpStreamMonitor.setMonitoredAddress(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMonitoredAddressUnresolvableArg() {
    udpStreamMonitor.setMonitoredAddress("127.0.0.0.1");
  }

  @Test
  public void testSetCatalogFramework() {
    CatalogFramework catalogFramework = mock(CatalogFramework.class);
    udpStreamMonitor.setCatalogFramework(catalogFramework);
    verify(udpStreamProcessor).setCatalogFramework(catalogFramework);
  }

  @Test(expected = NullPointerException.class)
  public void testSetCatalogFrameworkNullArg() {
    udpStreamMonitor.setCatalogFramework(null);
  }

  @Test
  public void testSetMetacardTypeList() {
    List<MetacardType> metacardTypeList = Collections.emptyList();
    udpStreamMonitor.setMetacardTypeList(metacardTypeList);
    verify(udpStreamProcessor).setMetacardTypeList(metacardTypeList);
  }

  @Test(expected = NullPointerException.class)
  public void testSetMetacardTypeListNullArg() {
    udpStreamMonitor.setMetacardTypeList(null);
  }

  @Test
  public void testSetTitle() {
    String title = "title";
    udpStreamMonitor.setParentTitle(title);
    assertThat(udpStreamMonitor.getTitle().get(), is(title));
  }

  @Test
  public void testGetNullStreamUri() {
    assertThat(udpStreamMonitor.getStreamUri().isPresent(), is(false));
  }

  @Test
  public void testGetStreamUri() {
    String addr = "udp://127.0.0.1:50000";
    udpStreamMonitor.setMonitoredAddress(addr);
    assertThat(udpStreamMonitor.getStreamUri().get().toString(), is(addr));
    assertThat(udpStreamMonitor.getMonitoredAddress(), is("127.0.0.1"));
  }

  @Test(expected = NullPointerException.class)
  public void testSetRolloverConditionNullArg() {
    udpStreamMonitor.setRolloverCondition(null);
  }

  @Test
  public void testSetRolloverCondition() {
    RolloverCondition rolloverCondition = mock(RolloverCondition.class);
    udpStreamMonitor.setRolloverCondition(rolloverCondition);
    verify(udpStreamProcessor).setRolloverCondition(rolloverCondition);
  }

  @Test(expected = NullPointerException.class)
  public void testSetFilenameTemplateNullArg() {
    udpStreamMonitor.setFilenameTemplate(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetFilenameTemplateBlankArg() {
    udpStreamMonitor.setFilenameTemplate("");
  }

  @Test
  public void testSetFilenameTemplate() {
    String filenameTemplate = "template";
    udpStreamMonitor.setFilenameTemplate(filenameTemplate);
    verify(udpStreamProcessor).setFilenameTemplate(filenameTemplate);
    assertThat(udpStreamMonitor.getFileNameTemplate(), is(filenameTemplate));
  }

  @Test(expected = NullPointerException.class)
  public void testSetFilenameGeneratorNullArg() {
    udpStreamMonitor.setFilenameGenerator(null);
  }

  @Test
  public void testSetFilenameGenerator() {
    FilenameGenerator filenameGenerator = mock(FilenameGenerator.class);
    udpStreamMonitor.setFilenameGenerator(filenameGenerator);
    verify(udpStreamProcessor).setFilenameGenerator(filenameGenerator);
  }

  @Test
  public void testUpdateCallbackUpdatesAllProperties() throws URISyntaxException {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(UdpStreamMonitor.METATYPE_TITLE, "Parent Metacard");
    properties.put(UdpStreamMonitor.METATYPE_STREAM_ID, "a1b2c3");
    properties.put(UdpStreamMonitor.METATYPE_MONITORED_ADDRESS, "udp://123.4.5.6:789");
    properties.put(UdpStreamMonitor.METATYPE_NETWORK_INTERFACE, "eth0");
    properties.put(UdpStreamMonitor.METATYPE_BYTE_COUNT_ROLLOVER_CONDITION, 25);
    properties.put(UdpStreamMonitor.METATYPE_ELAPSED_TIME_ROLLOVER_CONDITION, 30000L);
    properties.put(UdpStreamMonitor.METATYPE_FILENAME_TEMPLATE, "test-template");
    properties.put(UdpStreamMonitor.METATYPE_METACARD_UPDATE_INITIAL_DELAY, 5L);
    properties.put(UdpStreamMonitor.METATYPE_DISTANCE_TOLERANCE, 0.05);
    properties.put(UdpStreamMonitor.METATYPE_START_IMMEDIATELY, true);
    properties.put(
        UdpStreamMonitor.METATYPE_ADDITIONAL_PROPERTIES, new String[] {"foo=bar", "baz=bat"});

    try {
      udpStreamMonitor.updateCallback(properties);
    } catch (StreamMonitorException e) {
      // expected because udpStreamProcessor.isReady() returns false
    }

    assertThat(udpStreamMonitor.getTitle().orElse(null), is("Parent Metacard"));
    assertThat(udpStreamMonitor.getStreamId(), is("a1b2c3"));
    assertThat(udpStreamMonitor.getMonitoredAddress(), is("123.4.5.6"));
    assertThat(udpStreamMonitor.getStreamUri().orElse(null), is(new URI("udp://123.4.5.6:789")));
    assertThat(udpStreamMonitor.getNetworkInterface(), is("eth0"));
    assertThat(udpStreamMonitor.getByteCountRolloverCondition(), is(25));
    verify(udpStreamProcessor).setMegabyteCountRolloverCondition(25);
    assertThat(udpStreamMonitor.getElapsedTimeRolloverCondition(), is(30000L));
    verify(udpStreamProcessor).setElapsedTimeRolloverCondition(30000L);
    assertThat(udpStreamMonitor.getFileNameTemplate(), is("test-template"));
    verify(udpStreamProcessor).setFilenameTemplate("test-template");
    verify(udpStreamProcessor).setMetacardUpdateInitialDelay(5L);
    verify(udpStreamProcessor).setDistanceTolerance(0.05);
    assertThat(udpStreamMonitor.getStartImmediately(), is(true));
    verify(udpStreamProcessor)
        .setAdditionalProperties(argThat(allOf(hasEntry("foo", "bar"), hasEntry("baz", "bat"))));
  }
}
