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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.codice.alliance.transformer.nitf.image.ImageMetacardType;
import org.junit.Before;
import org.junit.Test;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;

public class TestMetacardFactory {
    private MetacardFactory metacardFactory;

    private static final String TEST_ID = "101";

    @Before
    public void setUp() {
        this.metacardFactory = new MetacardFactory();
        metacardFactory.setMetacardType(new ImageMetacardType());
    }

    @Test
    public void testCreateMetacardNullId() {
        Metacard metacard = metacardFactory.createMetacard(null);
        validateMetacard(metacard);
    }

    @Test
    public void testCreateMetacardNonNullId() {
        Metacard metacard = metacardFactory.createMetacard(TEST_ID);
        validateMetacard(metacard);

        Attribute attribute = metacard.getAttribute(Metacard.ID);
        String id = attribute.getValue()
                .toString();

        assertThat(id, is(notNullValue()));
        assertThat(id, is(TEST_ID));
    }

    private void validateMetacard(Metacard metacard) {
        Attribute attribute = metacard.getAttribute(Metacard.CONTENT_TYPE);

        assertThat(attribute, is(notNullValue()));
        assertThat(attribute.getValue(), is(MetacardFactory.MIME_TYPE_STRING));
    }

    @Test
    public void testToString() {
        String metacardFactoryString = metacardFactory.toString();

        assertThat(metacardFactoryString, is(notNullValue()));
        assertThat(metacardFactoryString, is("InputTransformer {Impl=org.codice.alliance.transformer.nitf.MetacardFactory, id=ddf/catalog/transformer/nitf, mime-type=image/nitf}"));
    }
}
