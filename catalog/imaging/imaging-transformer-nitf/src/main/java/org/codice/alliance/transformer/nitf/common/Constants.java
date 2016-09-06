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
package org.codice.alliance.transformer.nitf.common;

import java.util.regex.Pattern;

public class Constants {

    static final Pattern NIIRS_FORMAT = Pattern.compile("^[0-9]\\.[0-9]$");

    static final String GRD_COVER = "GRD_COVER";

    static final String SNOW_DEPTH_CAT = "SNOW_DEPTH_CAT";

    static final String PREDICTED_NIIRS = "PREDICTED_NIIRS";

    static final String SYSTYPE = "SYSTYPE";

    static final String CLOUDCVR = "CLOUDCVR";

    static final String PLATFORM_CODE = "PLATFORM CODE";

    static final String VEHICLE_ID = "VEHICLE ID";

}
