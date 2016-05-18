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
package org.codice.alliance.libs.stanag4609;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.io.IOUtils;
import org.codice.ddf.libs.klv.KlvContext;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.set.KlvLocalSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.io.ByteSource;

public class Stanag4609TransportStreamParserTest {
    private static final Map<String, Object> EXPECTED_VALUES = new HashMap<>();

    @BeforeClass
    public static void setUpClass() {
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.TIMESTAMP, 1245257585099653L);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.IMAGE_SOURCE_SENSOR, "EON");
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.IMAGE_COORDINATE_SYSTEM,
                "Geodetic WGS84");
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.SENSOR_LATITUDE, 54.681323);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.SENSOR_LONGITUDE, -110.168559);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.SENSOR_TRUE_ALTITUDE, 1532.272831);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.SLANT_RANGE, 10928.624544);

        // In this test file, target width is encoded in 4 bytes, but the standard says it should be
        // encoded in 2.
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.TARGET_WIDTH, 0.0);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE, 54.749123);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE, -110.046638);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.FRAME_CENTER_ELEVATION, -4.522774);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.TARGET_LOCATION_LATITUDE, 54.749123);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.TARGET_LOCATION_LONGITUDE, -110.046638);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.TARGET_LOCATION_ELEVATION, -4.522774);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.GROUND_RANGE, 10820.674945);
        EXPECTED_VALUES.put(Stanag4609TransportStreamParser.CHECKSUM, 7263);
    }

    private Stanag4609TransportStreamParser getParser() throws IOException {
        final ByteSource byteSource =
                ByteSource.wrap(IOUtils.toByteArray(getClass().getClassLoader()
                        .getResourceAsStream("dayflight.mpg")));

        return new Stanag4609TransportStreamParser(byteSource);
    }

    @Test
    public void testParseTransportStreamWithKLVCallback() throws Exception {
        final Stanag4609TransportStreamParser parser = getParser();

        // Mockito can't spy anonymous classes.
        final BiConsumer<Integer, DecodedKLVMetadataPacket> callback =
                new BiConsumer<Integer, DecodedKLVMetadataPacket>() {
                    @Override
                    public void accept(Integer integer,
                            DecodedKLVMetadataPacket decodedKLVMetadataPacket) {
                    }
                };
        final BiConsumer<Integer, DecodedKLVMetadataPacket> callbackSpy = spy(callback);

        parser.parse(callbackSpy);

        final ArgumentCaptor<DecodedKLVMetadataPacket> decodedPacketCaptor =
                ArgumentCaptor.forClass(DecodedKLVMetadataPacket.class);
        // The packet ID of the metadata stream in this file is 497.
        verify(callbackSpy, times(1)).accept(eq(497), decodedPacketCaptor.capture());
        verifyDecodedMetadataPacket(decodedPacketCaptor.getValue());
    }

    @Test
    public void testParseTransportStreamWithKLVAll() throws Exception {
        final Stanag4609TransportStreamParser parser = getParser();

        final Map<Integer, List<DecodedKLVMetadataPacket>> decodedStreams = parser.parse();

        assertThat(decodedStreams.size(), is(1));
        // The packet ID of the metadata stream in this file is 497.
        assertThat(decodedStreams, hasKey(497));
        final List<DecodedKLVMetadataPacket> decodedPackets = decodedStreams.get(497);
        assertThat(decodedPackets.size(), is(1));
        verifyDecodedMetadataPacket(decodedPackets.get(0));
    }

    private void verifyDecodedMetadataPacket(final DecodedKLVMetadataPacket packet) {
        final KlvContext outerContext = packet.getDecodedKLV();
        assertThat(outerContext.getDataElements()
                .size(), is(1));

        assertThat(outerContext.hasDataElement(Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET),
                is(true));
        final KlvContext localSetContext = ((KlvLocalSet) outerContext.getDataElementByName(
                Stanag4609TransportStreamParser.UAS_DATALINK_LOCAL_SET)).getValue();
        final Map<String, KlvDataElement> localSetDataElements = localSetContext.getDataElements();

        assertThat(localSetDataElements.size(), is(EXPECTED_VALUES.size()));

        localSetDataElements.forEach((name, dataElement) -> {
            final Object expectedValue = EXPECTED_VALUES.get(name);
            final Object actualValue = dataElement.getValue();

            if (actualValue instanceof Double) {
                assertThat(String.format("%s is not close to %s", name, expectedValue),
                        (Double) actualValue,
                        is(closeTo((Double) expectedValue, 1e-6)));
            } else {
                assertThat(String.format("%s is not %s", name, expectedValue),
                        actualValue,
                        is(expectedValue));
            }
        });
    }
}
