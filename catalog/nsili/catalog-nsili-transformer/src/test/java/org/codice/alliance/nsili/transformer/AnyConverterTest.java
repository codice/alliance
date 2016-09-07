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
package org.codice.alliance.nsili.transformer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import com.thoughtworks.xstream.XStream;

public class AnyConverterTest {

    private static final String VALUE = "somevalue";

    private static final String EXPECTED_RESULT = "<com.sun.corba.se.impl.corba.AnyImpl>somevalue</com.sun.corba.se.impl.corba.AnyImpl>";

    private ORB orb;

    private AnyConverter anyConverter;

    @Before
    public void setup() {
        this.orb = ORB.init();

        anyConverter = new AnyConverter();
    }

    @Test
    public void convertAnyToXML() {
        Any any = orb.create_any();
        any.insert_wstring(VALUE);

        assertThat(anyConverter.canConvert(any.getClass()), is(true));

        XStream xstream = new XStream();
        xstream.registerConverter(anyConverter);
        String result = xstream.toXML(any);

        assertThat(result, is(EXPECTED_RESULT));
    }
}
