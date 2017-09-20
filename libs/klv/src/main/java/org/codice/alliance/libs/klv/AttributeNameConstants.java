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

import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;

/**
 * All classes with this klv library should use these constants for metacard attribute names, even
 * for names already defined in Metacard so that all attribute names used by the library are in a
 * central location.
 */
public class AttributeNameConstants {

  public static final String TEMPORAL_START = DateTime.START;

  public static final String TEMPORAL_END = DateTime.END;

  public static final String CREATED = Core.CREATED;

  public static final String GEOGRAPHY = Core.LOCATION;

  public static final String MISSION_ID = Isr.MISSION_ID;

  public static final String PLATFORM_TAIL_NUMBER = Isr.PLATFORM_ID;

  public static final String PLATFORM_DESIGNATION = Isr.PLATFORM_NAME;

  public static final String OFFSET_CORNER = "offset-corner-location";

  public static final String TIMESTAMP = "timestamp";

  public static final String PLATFORM_CALL_SIGN = "platform-call-sign";

  public static final String EVENT_START_TIME = "event-start-time";

  public static final String OPERATIONAL_MODE = "operational-mode";

  public static final String CORNER = "corner-location";

  public static final String SECURITY_CLASSIFICATION = Security.CLASSIFICATION;

  public static final String CLASSIFYING_COUNTRY_CODING_METHOD =
      "classifying-country-coding-method";

  public static final String CLASSIFYING_COUNTRY = Security.CLASSIFICATION_SYSTEM;

  public static final String OBJECT_COUNTRY_CODING_METHOD = "object-country-coding-method";

  public static final String OBJECT_COUNTRY_CODES = Location.COUNTRY_CODE;

  public static final String CHECKSUM = "klv-packet-checksum";

  public static final String IMAGE_COORDINATE_SYSTEM = Location.COORDINATE_REFERENCE_SYSTEM_NAME;

  public static final String IMAGE_SOURCE_SENSOR = Isr.SENSOR_ID;

  public static final String TARGET_WIDTH_METERS = "target-width-meters";

  public static final String FRAME_CENTER_ELEVATION = "frame-center-elevation";

  public static final String SENSOR_TRUE_ALTITUDE = Location.ALTITUDE;

  public static final String GROUND_RANGE = "ground-range";

  public static final String SLANT_RANGE = "slant-range";

  public static final String TARGET_LOCATION_ELEVATION = "target-location-elevation";

  public static final String FRAME_CENTER = Media.FRAME_CENTER;

  public static final String TARGET_LOCATION = "target-location";

  public static final String SENSOR = "sensor";

  public static final String MEDIA_ENCODING = Media.ENCODING;

  public static final String SECURITY_SCI_SHI_INFORMATION = Security.CODEWORDS;

  public static final String CAVEATS = Security.DISSEMINATION_CONTROLS;

  public static final String RELEASING_INSTRUCTIONS = Security.RELEASABILITY;
}
