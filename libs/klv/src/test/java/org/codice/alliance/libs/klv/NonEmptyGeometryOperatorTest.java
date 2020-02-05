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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

public class NonEmptyGeometryOperatorTest {

  private NonEmptyGeometryOperator nonEmptyGeometryOperator;

  @Before
  public void setup() {
    nonEmptyGeometryOperator = new NonEmptyGeometryOperator();
  }

  @Test
  public void testEmptyGeometry() {
    Geometry geometry = mock(Geometry.class);
    when(geometry.isEmpty()).thenReturn(true);
    Geometry actual = nonEmptyGeometryOperator.apply(geometry, new GeometryOperator.Context());
    assertThat(actual, is(nullValue()));
  }

  @Test
  public void testNonEmptyGeometry() {
    Geometry geometry = mock(Geometry.class);
    when(geometry.isEmpty()).thenReturn(false);
    Geometry actual = nonEmptyGeometryOperator.apply(geometry, new GeometryOperator.Context());
    assertThat(actual, is(geometry));
  }
}
