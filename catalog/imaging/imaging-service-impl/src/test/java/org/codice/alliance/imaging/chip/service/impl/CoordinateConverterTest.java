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
package org.codice.alliance.imaging.chip.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

public class CoordinateConverterTest {

  @Test
  public void testToLonLat() {
    List<Vector> vectors = new ArrayList<>();
    // lon lat
    vectors.add(new BasicVector(new double[] {-30, 30})); // upper left
    vectors.add(new BasicVector(new double[] {30, 30})); // upper right
    vectors.add(new BasicVector(new double[] {30, -30})); // lower right
    vectors.add(new BasicVector(new double[] {-30, -30})); // lower left

    CoordinateConverter coordinateConverter = new CoordinateConverter(1024, 768, vectors);

    /*
     *    0,0                                  1024,0
     *
     *         1024/3,768/3     2*1024/3,768/3
     *
     *
     *         1024/3,2*768/3   2*1024/3,2*768/3
     *
     *    0,768                                1024,768
     */

    /*
     *    -30,30                                  30,30
     *
     *             -10,10              10,10
     *
     *
     *             -10,-10             10,-10
     *
     *    -30,-30                                30,-30
     */

    List<Vector> pixels = new LinkedList<>();
    pixels.add(new BasicVector(new double[] {1024 / 3, 768 / 3}));
    pixels.add(new BasicVector(new double[] {2 * 1024 / 3, 768 / 3}));
    pixels.add(new BasicVector(new double[] {2 * 1024 / 3, 2 * 768 / 3}));
    pixels.add(new BasicVector(new double[] {1024 / 3, 2 * 768 / 3}));

    List<Vector> newLatLong = coordinateConverter.toLonLat(pixels);

    double xOneThird = 60.0 * (341.0 / 1024.0);
    double xTwoThirds = 60.0 * (682.0 / 1024.0);

    double yOneThird = 60.0 * (256.0 / 768.0);
    double yTwoThirds = 60.0 * (512.0 / 768.0);

    assertThat(newLatLong.get(0).get(0), Matchers.closeTo(-30 + xOneThird, 0.01));
    assertThat(newLatLong.get(0).get(1), Matchers.closeTo(-30 + yTwoThirds, 0.01));

    assertThat(newLatLong.get(1).get(0), Matchers.closeTo(-30 + xTwoThirds, 0.01));
    assertThat(newLatLong.get(1).get(1), Matchers.closeTo(-30 + yTwoThirds, 0.01));

    assertThat(newLatLong.get(2).get(0), Matchers.closeTo(-30 + xTwoThirds, 0.01));
    assertThat(newLatLong.get(2).get(1), Matchers.closeTo(-30 + yOneThird, 0.01));

    assertThat(newLatLong.get(3).get(0), Matchers.closeTo(-30 + xOneThird, 0.01));
    assertThat(newLatLong.get(3).get(1), Matchers.closeTo(-30 + yOneThird, 0.01));
  }
}
