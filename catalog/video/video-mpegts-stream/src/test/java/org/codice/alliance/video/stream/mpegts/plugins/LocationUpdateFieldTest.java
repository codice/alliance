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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.codice.alliance.libs.klv.GeometryOperator;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;

public class LocationUpdateFieldTest {

    @Test
    public void testPolygon() {

        String wktChild1 = "POLYGON ((0  0, 10 0, 10 10, 0  10, 0  0))";
        String wktChild2 = "POLYGON ((10 0, 20 0, 20 10, 10 10, 10 0))";
        String wktChild3 = "POLYGON ((20 0, 30 0, 30 10, 20 10, 20 0))";

        String wktExpected = "POLYGON ((20 0, 10 0, 0 0, 0 10, 10 10, 20 10, 30 10, 30 0, 20 0))";

        Metacard parentMetacard = mock(Metacard.class);

        Metacard childMetacard1 = mock(Metacard.class);
        when(childMetacard1.getLocation()).thenReturn(wktChild1);
        Metacard childMetacard2 = mock(Metacard.class);
        when(childMetacard2.getLocation()).thenReturn(wktChild2);
        Metacard childMetacard3 = mock(Metacard.class);
        when(childMetacard3.getLocation()).thenReturn(wktChild3);

        LocationUpdateField locUpdateField = new LocationUpdateField(GeometryOperator.IDENTITY,
                GeometryOperator.IDENTITY);

        locUpdateField.updateField(parentMetacard, Collections.singletonList(childMetacard1));
        locUpdateField.updateField(parentMetacard, Arrays.asList(childMetacard2, childMetacard3));
        locUpdateField.end(parentMetacard);

        ArgumentCaptor<Attribute> captor = ArgumentCaptor.forClass(Attribute.class);

        verify(parentMetacard).setAttribute(captor.capture());

        assertThat(captor.getValue()
                .getValue(), is(wktExpected));

    }

}
