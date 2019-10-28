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
package org.codice.alliance.test.itests;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasXPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.jayway.restassured.response.ValidatableResponse;
import ddf.catalog.Constants;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Media;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codice.alliance.distribution.sdk.video.stream.mpegts.MpegTsUdpClient;
import org.codice.alliance.test.itests.common.AbstractAllianceIntegrationTest;
import org.codice.alliance.video.stream.mpegts.UdpStreamMonitor;
import org.codice.ddf.itests.common.annotations.ConditionalIgnoreRule;
import org.codice.ddf.itests.common.annotations.ConditionalIgnoreRule.ConditionalIgnore;
import org.codice.ddf.itests.common.annotations.SkipUnstableTest;
import org.codice.ddf.test.common.annotations.BeforeExam;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.InvalidSyntaxException;

/** Tests Alliance video capabilities. */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class VideoTest extends AbstractAllianceIntegrationTest {

  private static final String METACARD_COUNT_XPATH = "count(/metacards/metacard)";

  private static final String METACARD_ID_XMLPATH = "metacards.metacard.@gml:id";

  private static final String LOCALHOST = "127.0.0.1";

  private static final String NIGHTFLIGHT = "nightflight.mpg";

  private static final int NIGHTFLIGHT_DURATION_MS = 20030;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

  private DynamicPort udpPort;

  private int udpPortNum;

  @BeforeExam
  public void beforeExam() throws Exception {
    waitForSystemReady();
    getSecurityPolicy().configureRestForBasic();
    waitForSystemReady();
    udpPort = new DynamicPort(6);
    udpPortNum = Integer.parseInt(udpPort.getPort());
  }

  @After
  public void tearDown() {
    clearCatalog();
  }

  @Test
  @ConditionalIgnore(condition = SkipUnstableTest.class) // CAL-300
  public void testStreamingVideo() throws Exception {
    getServiceManager().startFeature(true, "sample-mpegts-streamgenerator");

    final String videoFilePath =
        FilenameUtils.concat(temporaryFolder.getRoot().getCanonicalPath(), NIGHTFLIGHT);
    final File videoFile = new File(videoFilePath);

    copyResourceToFile(NIGHTFLIGHT, videoFile);

    final String streamTitle = "UDP Stream Test";
    final String udpStreamAddress = String.format("udp://%s:%d", LOCALHOST, udpPortNum);

    final Map<String, Object> streamMonitorProperties = new HashMap<>();
    streamMonitorProperties.put(UdpStreamMonitor.METATYPE_TITLE, streamTitle);
    streamMonitorProperties.put(UdpStreamMonitor.METATYPE_MONITORED_ADDRESS, udpStreamAddress);
    streamMonitorProperties.put(UdpStreamMonitor.METATYPE_METACARD_UPDATE_INITIAL_DELAY, 0);
    streamMonitorProperties.put(UdpStreamMonitor.METATYPE_BYTE_COUNT_ROLLOVER_CONDITION, 5);
    streamMonitorProperties.put("startImmediately", true);

    startUdpStreamMonitor(streamMonitorProperties);

    waitForUdpStreamMonitorStart();

    MpegTsUdpClient.broadcastVideo(
        videoFilePath,
        LOCALHOST,
        udpPortNum,
        NIGHTFLIGHT_DURATION_MS,
        MpegTsUdpClient.PACKET_SIZE,
        MpegTsUdpClient.PACKET_SIZE,
        false,
        null);

    Thread.sleep(2000);

    await("The parent and child metacards to be created")
        .atMost(15, TimeUnit.SECONDS)
        .pollDelay(1, TimeUnit.SECONDS)
        .until(
            () ->
                executeOpenSearch("xml", "q=*")
                        .extract()
                        .xmlPath()
                        .getList("metacards.metacard")
                        .size()
                    == 3);

    final ValidatableResponse parentMetacardResponse =
        executeOpenSearch("xml", "q=" + streamTitle)
            .log()
            .all()
            .assertThat()
            .body(hasXPath(METACARD_COUNT_XPATH, is("1")))
            .body(hasXPath("/metacards/metacard/string[@name='title']/value", is(streamTitle)))
            .body(
                hasXPath(
                    "/metacards/metacard/string[@name='resource-uri']/value", is(udpStreamAddress)))
            .body(
                hasXPath(
                    "/metacards/metacard/geometry[@name='media.frame-center']/value/*[local-name()='LineString']/*[local-name()='pos'][2]",
                    is("-110.058257 54.791167")))
            .body(
                hasXPath(
                    "/metacards/metacard/geometry[@name='location']/value/*[local-name()='MultiPoint']/*[local-name()='pointMember'][2]/*[local-name()='Point']/*[local-name()='pos']",
                    is("-110.058257 54.791167")));

    final String parentMetacardId =
        parentMetacardResponse.extract().xmlPath().getString(METACARD_ID_XMLPATH);

    await("The child metacards to be linked to the parent")
        .atMost(3, TimeUnit.SECONDS)
        .until(
            () ->
                executeOpenSearch("xml", "q=mpegts-stream*")
                        .extract()
                        .xmlPath()
                        .getInt(
                            "metacards.metacard.string.findAll { it.@name == 'metacard.associations.derived' }.size()")
                    == 2);

    final String chunkDividerDate = "2009-06-19T07:26:30Z";

    verifyChunkMetacard(
        "dtend=" + chunkDividerDate, 1212.82825971, "-110.058257 54.791167", parentMetacardId);

    verifyChunkMetacard(
        "dtstart=" + chunkDividerDate, 1206.75516899, "-110.058421 54.791636", parentMetacardId);

    getServiceManager().stopFeature(true, "sample-mpegts-streamgenerator");
  }

  @Test
  public void testWithEmptyGeos()
      throws InvalidSyntaxException, CatalogTransformerException, IOException {
    String resource = "/2019-06-11-14.58.44.129_0_NULL_NULL_000000_4609.ts";
    InputTransformer inputTransformer = findInputTransformer();
    try (InputStream inputStream = getAllianceItestResourceAsStream(resource)) {
      Metacard metacard = inputTransformer.transform(inputStream);

      assertThat(metacard.getAttribute(Core.LOCATION), is(nullValue()));
      assertThat(metacard.getAttribute(Media.FRAME_CENTER), is(nullValue()));
    }
  }

  private InputTransformer findInputTransformer() throws InvalidSyntaxException {
    String transformerId = "mpegts";

    Optional<InputTransformer> inputTransformer =
        getServiceManager()
            .getServiceReferences(
                InputTransformer.class,
                String.format("(%s=%s)", Constants.SERVICE_ID, transformerId))
            .stream()
            .findFirst()
            .map(sr -> getServiceManager().getService(sr));
    if (!inputTransformer.isPresent()) {
      fail(String.format("Error finding input transformer with id %s", transformerId));
    }

    return inputTransformer.get();
  }

  private void copyResourceToFile(String resource, File file) throws IOException {
    // @formatter:off
    try (InputStream is = getAllianceItestResourceAsStream(resource);
        FileOutputStream fos = new FileOutputStream(file)) {
      IOUtils.copy(is, fos);
    }
    // @formatter:on
  }

  private void startUdpStreamMonitor(Map<String, Object> propertyOverrides) throws IOException {
    final Map<String, Object> properties =
        getServiceManager()
            .getMetatypeDefaults(
                "video-mpegts-stream", "org.codice.alliance.video.stream.mpegts.UdpStreamMonitor");

    properties.putAll(propertyOverrides);

    getServiceManager()
        .createManagedService(
            "org.codice.alliance.video.stream.mpegts.UdpStreamMonitor", properties);
  }

  private void waitForUdpStreamMonitorStart() {
    await("The UDP stream monitor to start on port " + udpPort.getPort())
        .atMost(5, TimeUnit.SECONDS)
        .until(
            () -> {
              try (DatagramSocket socket = new DatagramSocket(udpPortNum)) {
                return false;
              } catch (SocketException e) {
                return true;
              }
            });
  }

  private void verifyChunkMetacard(
      String dateBound,
      double expectedAltitude,
      String expectedFrameCenterWkt,
      String expectedParentId) {
    final ValidatableResponse response =
        executeOpenSearch("xml", "q=mpegts-stream*", dateBound)
            .log()
            .all()
            .assertThat()
            .body(hasXPath(METACARD_COUNT_XPATH, is("1")))
            .body(
                hasXPath(
                    "/metacards/metacard/base64Binary[@name='thumbnail']/value",
                    not(isEmptyOrNullString())))
            .body(hasXPath("/metacards/metacard/string[@name='isr.sensor-id']/value", is("IR")))
            .body(
                hasXPath(
                    "/metacards/metacard/string[@name='location.crs-name']/value",
                    is("Geodetic WGS84")))
            .body(
                hasXPath(
                    "/metacards/metacard/geometry[@name='media.frame-center']/value/*[local-name()='Point']/*[local-name()='pos']",
                    is(expectedFrameCenterWkt)))
            .body(
                hasXPath(
                    "/metacards/metacard/string[@name='metacard.associations.derived']/value",
                    is(expectedParentId)));

    final double altitude =
        response
            .extract()
            .xmlPath()
            .getDouble(
                "metacards.metacard.double.find { it.@name == 'location.altitude-meters' }.value");

    assertThat(altitude, is(closeTo(expectedAltitude, 1e-8)));
  }
}
