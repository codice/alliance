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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Arrays;
import java.util.Collections;

import org.codice.alliance.libs.klv.AttributeNameConstants;
import org.codice.alliance.libs.klv.GeometryOperator;
import org.codice.alliance.video.stream.mpegts.Context;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class FrameCenterUpdateFieldTest {

    @Test
    public void testLineString() {

        String wktChild1 = "LINESTRING (30 10, 10 30, 40 40)";
        String wktChild2 = "LINESTRING (50 50, 60 60, 70 70)";
        String wktChild3 = "LINESTRING (80 80, 85 85)";

        String wktExpected = "LINESTRING (30 10, 10 30, 40 40, 50 50, 60 60, 70 70, 80 80, 85 85)";

        Metacard parentMetacard = mock(Metacard.class);

        Metacard childMetacard1 = mock(Metacard.class);
        when(childMetacard1.getAttribute(AttributeNameConstants.FRAME_CENTER)).thenReturn(new AttributeImpl(
                AttributeNameConstants.FRAME_CENTER,
                wktChild1));
        Metacard childMetacard2 = mock(Metacard.class);
        when(childMetacard2.getAttribute(AttributeNameConstants.FRAME_CENTER)).thenReturn(new AttributeImpl(
                AttributeNameConstants.FRAME_CENTER,
                wktChild2));
        Metacard childMetacard3 = mock(Metacard.class);
        when(childMetacard3.getAttribute(AttributeNameConstants.FRAME_CENTER)).thenReturn(new AttributeImpl(
                AttributeNameConstants.FRAME_CENTER,
                wktChild3));

        FrameCenterUpdateField frameCenterUpdateField =
                new FrameCenterUpdateField(GeometryOperator.IDENTITY, new GeometryFactory());

        Context context = mock(Context.class);
        GeometryOperator.Context geometryOperatorContext = new GeometryOperator.Context();
        geometryOperatorContext.setSubsampleCount(1000);
        when(context.getGeometryOperatorContext()).thenReturn(geometryOperatorContext);

        frameCenterUpdateField.updateField(parentMetacard,
                Collections.singletonList(childMetacard1),
                context);
        frameCenterUpdateField.updateField(parentMetacard,
                Arrays.asList(childMetacard2, childMetacard3),
                context);
        frameCenterUpdateField.end(parentMetacard, context);

        ArgumentCaptor<Attribute> captor = ArgumentCaptor.forClass(Attribute.class);

        verify(parentMetacard).setAttribute(captor.capture());

        assertThat(captor.getValue()
                .getValue(), is(wktExpected));

    }

    @Test
    public void testThatGeoOperatorIsCalled() throws ParseException {

        String wktChild1 = "LINESTRING (30 10, 10 30, 40 40)";

        GeometryOperator geometryOperator = mock(GeometryOperator.class);
        Geometry geometry = new WKTReader().read("LINESTRING (0 0, 1 1)");

        Context context = mock(Context.class);

        GeometryOperator.Context geometryOperatorContext = new GeometryOperator.Context();
        geometryOperatorContext.setSubsampleCount(1000);

        when(context.getGeometryOperatorContext()).thenReturn(geometryOperatorContext);
        when(geometryOperator.apply(any(), any())).thenReturn(geometry);

        Metacard parentMetacard = mock(Metacard.class);

        Metacard childMetacard1 = mock(Metacard.class);
        when(childMetacard1.getAttribute(AttributeNameConstants.FRAME_CENTER)).thenReturn(new AttributeImpl(
                AttributeNameConstants.FRAME_CENTER,
                wktChild1));

        FrameCenterUpdateField frameCenterUpdateField = new FrameCenterUpdateField(geometryOperator,
                new GeometryFactory());

        frameCenterUpdateField.updateField(parentMetacard,
                Collections.singletonList(childMetacard1),
                context);

        verify(geometryOperator, times(0)).apply(any(), any());

        frameCenterUpdateField.end(parentMetacard, context);

        verify(geometryOperator, times(1)).apply(any(), any());

    }

}
