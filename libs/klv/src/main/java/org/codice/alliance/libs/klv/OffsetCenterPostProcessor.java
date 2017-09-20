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

import java.util.Map;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.codice.ddf.libs.klv.KlvDataElement;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;

/** Use frame center and offset corner data to calculate the corner data. */
public class OffsetCenterPostProcessor implements PostProcessor {

  private boolean isCornerLatitude(Map<String, KlvHandler> handlers) {
    return handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LATITUDE_1)
        && handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LATITUDE_2)
        && handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LATITUDE_3)
        && handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LATITUDE_4);
  }

  private boolean isCornerLongitude(Map<String, KlvHandler> handlers) {
    return handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LONGITUDE_1)
        && handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LONGITUDE_2)
        && handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LONGITUDE_3)
        && handlers.containsKey(Stanag4609TransportStreamParser.CORNER_LONGITUDE_4);
  }

  private boolean isOffsetCornerLatitude(Map<String, KlvDataElement> dataElements) {
    return dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_1)
        && dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_2)
        && dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_3)
        && dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_4);
  }

  private boolean isOffsetCornerLongitude(Map<String, KlvDataElement> dataElements) {
    return dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_1)
        && dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_2)
        && dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_3)
        && dataElements.containsKey(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_4);
  }

  private boolean isFrameCenter(Map<String, KlvDataElement> dataElements) {
    return dataElements.containsKey(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE)
        && dataElements.containsKey(Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);
  }

  @Override
  public void postProcess(
      Map<String, KlvDataElement> dataElements, Map<String, KlvHandler> handlers) {

    if (!(isCornerLatitude(handlers) && isCornerLongitude(handlers))) {
      return;
    }

    if (isFrameCenter(dataElements)
        && isOffsetCornerLatitude(dataElements)
        && isOffsetCornerLongitude(dataElements)) {

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LATITUDE_1,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_1,
          Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LONGITUDE_1,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_1,
          Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LATITUDE_2,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_2,
          Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LONGITUDE_2,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_2,
          Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LATITUDE_3,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_3,
          Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LONGITUDE_3,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_3,
          Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LATITUDE_4,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_4,
          Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE);

      doField(
          dataElements,
          handlers,
          Stanag4609TransportStreamParser.CORNER_LONGITUDE_4,
          Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_4,
          Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);
    }
  }

  private void doField(
      Map<String, KlvDataElement> dataElements,
      Map<String, KlvHandler> handlers,
      String cornerField,
      String offsetField,
      String frameField) {

    if (!(handlers.get(cornerField) instanceof GeoBoxHandler)) {
      return;
    }

    if (!(dataElements.get(offsetField) instanceof KlvIntegerEncodedFloatingPoint)) {
      return;
    }

    if (!(dataElements.get(frameField) instanceof KlvIntegerEncodedFloatingPoint)) {
      return;
    }

    ((GeoBoxHandler) handlers.get(cornerField))
        .accept(
            cornerField,
            ((KlvIntegerEncodedFloatingPoint) dataElements.get(offsetField)).getValue()
                + ((KlvIntegerEncodedFloatingPoint) dataElements.get(frameField)).getValue());
  }
}
