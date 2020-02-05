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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.codice.alliance.libs.klv.GeometryOperator;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryFactory;

public class FrameCenterUpdateFieldFactoryTest {

  @Test
  public void testBuild() {
    GeometryOperator geometryOperator = mock(GeometryOperator.class);
    FrameCenterUpdateFieldFactory factory =
        new FrameCenterUpdateFieldFactory(geometryOperator, new GeometryFactory());
    UpdateParent.UpdateField updateField = factory.build();
    assertThat(updateField, is(instanceOf(FrameCenterUpdateField.class)));
  }
}
