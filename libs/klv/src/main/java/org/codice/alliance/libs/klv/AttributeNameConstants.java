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
package org.codice.alliance.libs.klv;

import ddf.catalog.data.Metacard;

/**
 * All classes with this klv library should use these constants for metacard attribute names, even
 * for names already defined in {@link Metacard} so that all attribute names used by the library
 * are in a central location.
 */
public class AttributeNameConstants {

    public static final String TEMPORAL_START = "temporal.start";

    public static final String TEMPORAL_END = "temporal.end";

    public static final String CREATED = Metacard.CREATED;

    public static final String MISSION_ID = "mission-id";

    public static final String PLATFORM_TAIL_NUMBER = "platform-tail-number";

    public static final String PLATFORM_DESIGNATION = "platform-designation";

    public static final String OFFSET_CORNER = "offset-corner-location";

    public static final String TIMESTAMP = "timestamp";

    public static final String PLATFORM_CALL_SIGN = "platform-call-sign";

    public static final String EVENT_START_TIME = "event-start-time";

    public static final String OPERATIONAL_MODE = "operational-mode";

    public static final String CORNER = "corner-location";

    public static final String SECURITY_CLASSIFICATION = "security-classification";

    public static final String CLASSIFYING_COUNTRY_CODING_METHOD =
            "classifying-country-coding-method";

    public static final String CLASSIFYING_COUNTRY = "classifying-country";

    public static final String OBJECT_COUNTRY_CODING_METHOD = "object-country-coding-method";

    public static final String OBJECT_COUNTRY_CODES = "object-country-codes";

    public static final String CHECKSUM = "checksum";

    public static final String IMAGE_COORDINATE_SYSTEM = "image-coordinate-system";

    public static final String IMAGE_SOURCE_SENSOR = "image-source-sensor";

    public static final String TARGET_WIDTH = "target-width";

    public static final String FRAME_CENTER_ELEVATION = "frame-center-elevation";

    public static final String SENSOR_TRUE_ALTITUDE = "sensor-true-altitude";

    public static final String GROUND_RANGE = "ground-range";

    public static final String SLANT_RANGE = "slant-range";

    public static final String TARGET_LOCATION_ELEVATION = "target-location-elevation";

    public static final String FRAME_CENTER = "frame-center-location";

    public static final String TARGET_LOCATION = "target-location";

    public static final String SENSOR = "sensor";

}
