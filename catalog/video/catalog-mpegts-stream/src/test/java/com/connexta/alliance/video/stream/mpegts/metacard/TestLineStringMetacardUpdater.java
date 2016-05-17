/**
 * Copyright (c) Connexta, LLC
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
package com.connexta.alliance.video.stream.mpegts.metacard;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;

public class TestLineStringMetacardUpdater {

    private Metacard parentMetacard;

    private Metacard childMetacard;

    private Attribute parentAttr;

    private Attribute childAttr;

    private String attrName = "foo";

    private LineStringMetacardUpdater lineStringMetacardUpdater;

    @Before
    public void setup() {
        parentMetacard = mock(Metacard.class);
        childMetacard = mock(Metacard.class);

        parentAttr = mock(Attribute.class);
        childAttr = mock(Attribute.class);

        lineStringMetacardUpdater = new LineStringMetacardUpdater(attrName);
    }

    @Test
    public void testParentMergedWithChild() throws ParseException {

        when(parentAttr.getValue()).thenReturn("LINESTRING(0 0, 1 1)");
        when(childAttr.getValue()).thenReturn("LINESTRING(2 2, 3 3)");

        when(parentMetacard.getAttribute(attrName)).thenReturn(parentAttr);
        when(childMetacard.getAttribute(attrName)).thenReturn(childAttr);

        lineStringMetacardUpdater.update(parentMetacard, childMetacard);

        ArgumentCaptor<Attribute> argumentCaptor = ArgumentCaptor.forClass(Attribute.class);

        verify(parentMetacard).setAttribute(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()
                .getValue(), is(normalize("LINESTRING(0 0, 1 1, 2 2, 3 3)")));

    }

    @Test
    public void testChildOnly() throws ParseException {

        String childWkt = normalize("LINESTRING(0 0, 1 1)");

        when(childAttr.getValue()).thenReturn(childWkt);

        when(childMetacard.getAttribute(attrName)).thenReturn(childAttr);

        lineStringMetacardUpdater.update(parentMetacard, childMetacard);

        ArgumentCaptor<Attribute> argumentCaptor = ArgumentCaptor.forClass(Attribute.class);

        verify(parentMetacard).setAttribute(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()
                .getValue(), is(childWkt));

    }

    private String normalize(String wkt) throws ParseException {
        return new WKTWriter().write(new WKTReader().read(wkt)
                .norm());
    }

}
