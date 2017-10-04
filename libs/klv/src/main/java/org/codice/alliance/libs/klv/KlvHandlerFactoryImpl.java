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

import java.util.HashMap;
import java.util.Map;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.codice.ddf.libs.klv.data.numerical.KlvIntegerEncodedFloatingPoint;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedByte;
import org.codice.ddf.libs.klv.data.numerical.KlvUnsignedShort;
import org.codice.ddf.libs.klv.data.text.KlvString;

public class KlvHandlerFactoryImpl implements KlvHandlerFactory {

  @Override
  public Map<String, KlvHandler> createStanag4609Handlers() {
    final Map<String, KlvHandler> handlers = new HashMap<>();

    handlers.put(
        Stanag4609TransportStreamParser.MISSION_ID,
        new ListOfBasicKlvDataTypesHandler<>(AttributeNameConstants.MISSION_ID, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.PLATFORM_TAIL_NUMBER,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.PLATFORM_TAIL_NUMBER, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.PLATFORM_DESIGNATION,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.PLATFORM_DESIGNATION, KlvString.class));

    GeoBoxHandler offsetCornerHandler =
        new GeoBoxHandler(
            AttributeNameConstants.OFFSET_CORNER,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_1,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_1,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_2,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_2,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_3,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_3,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_4,
            Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_4);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_1, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_1, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_2, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_2, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_3, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_3, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LATITUDE_4, offsetCornerHandler);
    handlers.put(Stanag4609TransportStreamParser.OFFSET_CORNER_LONGITUDE_4, offsetCornerHandler);

    handlers.put(
        Stanag4609TransportStreamParser.PLATFORM_CALL_SIGN,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.PLATFORM_CALL_SIGN, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.EVENT_START_TIME,
        new ListOfDatesHandler((AttributeNameConstants.EVENT_START_TIME)));

    handlers.put(
        Stanag4609TransportStreamParser.OPERATIONAL_MODE,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.OPERATIONAL_MODE, KlvUnsignedByte.class));

    GeoBoxHandler cornerHandler =
        new GeoBoxHandler(
            AttributeNameConstants.CORNER,
            Stanag4609TransportStreamParser.CORNER_LATITUDE_1,
            Stanag4609TransportStreamParser.CORNER_LONGITUDE_1,
            Stanag4609TransportStreamParser.CORNER_LATITUDE_2,
            Stanag4609TransportStreamParser.CORNER_LONGITUDE_2,
            Stanag4609TransportStreamParser.CORNER_LATITUDE_3,
            Stanag4609TransportStreamParser.CORNER_LONGITUDE_3,
            Stanag4609TransportStreamParser.CORNER_LATITUDE_4,
            Stanag4609TransportStreamParser.CORNER_LONGITUDE_4);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_1, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_1, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_2, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_2, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_3, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_3, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LATITUDE_4, cornerHandler);
    handlers.put(Stanag4609TransportStreamParser.CORNER_LONGITUDE_4, cornerHandler);

    handlers.put(
        Stanag4609TransportStreamParser.SECURITY_CLASSIFICATION,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.SECURITY_CLASSIFICATION, KlvUnsignedByte.class));

    handlers.put(
        Stanag4609TransportStreamParser.CLASSIFYING_COUNTRY_CODING_METHOD,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.CLASSIFYING_COUNTRY_CODING_METHOD, KlvUnsignedByte.class));

    handlers.put(
        Stanag4609TransportStreamParser.CLASSIFYING_COUNTRY,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.CLASSIFYING_COUNTRY, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.OBJECT_COUNTRY_CODING_METHOD,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.OBJECT_COUNTRY_CODING_METHOD, KlvUnsignedByte.class));

    handlers.put(
        Stanag4609TransportStreamParser.OBJECT_COUNTRY_CODES,
        new ObjectCountryCodesHandler(AttributeNameConstants.OBJECT_COUNTRY_CODES));

    handlers.put(
        Stanag4609TransportStreamParser.TIMESTAMP,
        new ListOfDatesHandler(AttributeNameConstants.TIMESTAMP));

    handlers.put(
        Stanag4609TransportStreamParser.CHECKSUM,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.CHECKSUM, KlvUnsignedShort.class));

    handlers.put(
        Stanag4609TransportStreamParser.IMAGE_COORDINATE_SYSTEM,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.IMAGE_COORDINATE_SYSTEM, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.IMAGE_SOURCE_SENSOR,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.IMAGE_SOURCE_SENSOR, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.TARGET_WIDTH,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.TARGET_WIDTH_METERS, KlvIntegerEncodedFloatingPoint.class));

    handlers.put(
        Stanag4609TransportStreamParser.FRAME_CENTER_ELEVATION,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.FRAME_CENTER_ELEVATION, KlvIntegerEncodedFloatingPoint.class));

    handlers.put(
        Stanag4609TransportStreamParser.SENSOR_TRUE_ALTITUDE,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.SENSOR_TRUE_ALTITUDE, KlvIntegerEncodedFloatingPoint.class));

    handlers.put(
        Stanag4609TransportStreamParser.GROUND_RANGE,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.GROUND_RANGE, KlvIntegerEncodedFloatingPoint.class));

    handlers.put(
        Stanag4609TransportStreamParser.SLANT_RANGE,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.SLANT_RANGE, KlvIntegerEncodedFloatingPoint.class));

    handlers.put(
        Stanag4609TransportStreamParser.TARGET_LOCATION_ELEVATION,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.TARGET_LOCATION_ELEVATION,
            KlvIntegerEncodedFloatingPoint.class));

    KlvHandler frameCenter =
        new LatitudeLongitudeHandler(
            AttributeNameConstants.FRAME_CENTER,
            Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE,
            Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE);
    handlers.put(Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE, frameCenter);
    handlers.put(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE, frameCenter);

    KlvHandler targetLocation =
        new LatitudeLongitudeHandler(
            AttributeNameConstants.TARGET_LOCATION,
            Stanag4609TransportStreamParser.TARGET_LOCATION_LATITUDE,
            Stanag4609TransportStreamParser.TARGET_LOCATION_LONGITUDE);
    handlers.put(Stanag4609TransportStreamParser.TARGET_LOCATION_LONGITUDE, targetLocation);
    handlers.put(Stanag4609TransportStreamParser.TARGET_LOCATION_LATITUDE, targetLocation);

    KlvHandler sensor =
        new LatitudeLongitudeHandler(
            AttributeNameConstants.SENSOR,
            Stanag4609TransportStreamParser.SENSOR_LATITUDE,
            Stanag4609TransportStreamParser.SENSOR_LONGITUDE);
    handlers.put(Stanag4609TransportStreamParser.SENSOR_LONGITUDE, sensor);
    handlers.put(Stanag4609TransportStreamParser.SENSOR_LATITUDE, sensor);

    handlers.put(
        Stanag4609TransportStreamParser.SECURITY_SCI_SHI_INFORMATION,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.SECURITY_SCI_SHI_INFORMATION, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.CAVEATS,
        new ListOfBasicKlvDataTypesHandler<>(AttributeNameConstants.CAVEATS, KlvString.class));

    handlers.put(
        Stanag4609TransportStreamParser.RELEASING_INSTRUCTIONS,
        new ListOfBasicKlvDataTypesHandler<>(
            AttributeNameConstants.RELEASING_INSTRUCTIONS, KlvString.class));

    return handlers;
  }
}
