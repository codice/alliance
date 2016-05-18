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
package org.codice.alliance.libs.klv;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;

public class TestFrameCenterKlvProcessor {

    @Test
    public void test() throws ParseException {
        FrameCenterKlvProcessor frameCenterKlvProcessor = new FrameCenterKlvProcessor();

        KlvHandler klvHandler = mock(KlvHandler.class);

        Attribute attribute = mock(Attribute.class);

        when(klvHandler.asAttribute()).thenReturn(Optional.of(attribute));

        when(attribute.getValues()).thenReturn(Arrays.asList("POINT(0 0)",
                "POINT(1 1)",
                "POINT(2 2)"));

        Map<String, KlvHandler> handlerMap = new HashMap<>();
        handlerMap.put(Stanag4609TransportStreamParser.FRAME_CENTER_LATITUDE, klvHandler);
        handlerMap.put(Stanag4609TransportStreamParser.FRAME_CENTER_LONGITUDE, klvHandler);

        Metacard metacard = mock(Metacard.class);

        KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();

        frameCenterKlvProcessor.process(handlerMap, metacard, configuration);

        ArgumentCaptor<Attribute> argumentCaptor = ArgumentCaptor.forClass(Attribute.class);

        verify(metacard).setAttribute(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()
                .getValue(), is(normalize("LINESTRING (0 0, 1 1, 2 2)")));

    }

    private String normalize(String wkt) throws ParseException {
        return new WKTWriter().write(new WKTReader().read(wkt)
                .norm());
    }

}
