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
package org.codice.alliance.nsili.common;

public class GeoUtils {

    private static final double EARTH_RADIUS_B = 6356752.3142;
    private static final double EARTH_RADIUS_A = 6378137.0;
    private static final double PI_TIMES_RADIUS = Math.PI * EARTH_RADIUS_A;
    private static final double E_2 = (Math.pow(EARTH_RADIUS_A,2)-Math.pow(EARTH_RADIUS_B,2))/Math.pow(EARTH_RADIUS_A,2);

    /*
     * Formula from : https://en.wikipedia.org/wiki/Latitude#Length_of_a_degree_of_latitude
     */

    /**
     * Get the length of a degree of latitude at a given latitude.
     * @param latitudeDeg
     * @return
     */
    public static long getLatLengthAtLatitude(double latitudeDeg) {
        double latRads = Math.toRadians(latitudeDeg);
        return Math.round(111132.954 - 559.822*Math.cos(2*latRads)+1.175*Math.cos(4*latRads));
    }

    /**
     * Get the length of a degree of longitude at a given latitude.
     * @param latitudeDeg
     * @return
     */
    public static long getLongLengthAtLatitude(double latitudeDeg) {
        double latRads = Math.toRadians(latitudeDeg);
        double numerator = PI_TIMES_RADIUS * Math.cos(latRads);
        double denomPortion = Math.sqrt(1 - E_2*Math.pow(Math.sin(latRads), 2));
        return Math.round(numerator/(180*denomPortion));
    }
}
