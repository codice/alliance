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
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Matchers.doubleThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.junit.Test;

public class OffsetCenterPostProcessorTest {

  @Test
  public void test() throws KlvDecodingException {

    OffsetCenterPostProcessor offsetCenterPostProcessor = new OffsetCenterPostProcessor();

    Map<String, KlvHandler> handlers = new HashMap<>();

    GeoBoxHandler cornerHandler = mock(GeoBoxHandler.class);
    GeoBoxHandler offsetHandler = mock(GeoBoxHandler.class);
    LatitudeLongitudeHandler centerHandler = mock(LatitudeLongitudeHandler.class);

    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_1, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_2, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_3, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_4, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_1, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_2, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_3, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_4, cornerHandler);

    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_1, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_2, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_3, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_4, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_1, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_2, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_3, offsetHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_4, offsetHandler);

    handlers.put(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE, centerHandler);
    handlers.put(Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE, centerHandler);

    Map<String, KlvDataElement> dataElements = new HashMap<>();

    double lat1 = 1;
    double lat2 = 2;
    double lat3 = 3;
    double lat4 = 4;
    double lon1 = 5;
    double lon2 = 6;
    double lon3 = 7;
    double lon4 = 8;

    double lat = 30;
    double lon = 50;

    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_1, lat1);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_2, lat2);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_3, lat3);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_4, lat4);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_1, lon1);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_2, lon2);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_3, lon3);
    add(dataElements, Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_4, lon4);

    add(dataElements, Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE, lat);
    add(dataElements, Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE, lon);

    offsetCenterPostProcessor.postProcess(dataElements, handlers);

    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LATITUDE_1, lat1 + lat);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LATITUDE_2, lat2 + lat);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LATITUDE_3, lat3 + lat);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LATITUDE_4, lat4 + lat);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LONGITUDE_1, lon1 + lon);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LONGITUDE_2, lon2 + lon);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LONGITUDE_3, lon3 + lon);
    verifyThat(cornerHandler, Stanag4609TransportStreamParser.CORNER_LONGITUDE_4, lon4 + lon);
  }

  private void verifyThat(GeoBoxHandler cornerHandler, String name, double value) {
    verify(cornerHandler).accept(eq(name), doubleThat(closeTo(value, 0.01)));
  }

  private void add(Map<String, KlvDataElement> dataElements, String name, double value)
      throws KlvDecodingException {
    dataElements.put(name, KlvUtilities.createTestFloat(name, value));
  }
}
