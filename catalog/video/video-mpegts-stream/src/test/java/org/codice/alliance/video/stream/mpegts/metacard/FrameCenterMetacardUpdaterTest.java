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
package org.codice.alliance.video.stream.mpegts.metacard;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.codice.alliance.libs.klv.GeometryOperator;
import org.junit.Before;
import org.junit.Test;

public class FrameCenterMetacardUpdaterTest {

    private FrameCenterMetacardUpdater frameCenterMetacardUpdater;

    @Before
    public void setup() {
        frameCenterMetacardUpdater = new FrameCenterMetacardUpdater(GeometryOperator.IDENTITY);
    }

    @Test
    public void testToString() {
        assertThat(frameCenterMetacardUpdater.toString(), notNullValue());
    }

    @Test
    public void testAccept() {
        MetacardUpdater.Visitor visitor = mock(MetacardUpdater.Visitor.class);
        frameCenterMetacardUpdater.accept(visitor);
        verify(visitor).visit(frameCenterMetacardUpdater);
    }
}
