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

import java.util.Arrays;
import java.util.Collections;

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ddf.catalog.data.Attribute;

public class TestSecurityClassificationKlvProcessor {

    @Test
    public void test() {

        Short id1 = 10;

        ArgumentCaptor<Attribute> argumentCaptor =
                KlvUtilities.testKlvProcessor(new SecurityClassificationKlvProcessor(),
                        Stanag4609TransportStreamParser.SECURITY_CLASSIFICATION,
                        Arrays.asList(id1, id1));

        assertThat(argumentCaptor.getValue()
                .getName(), is(AttributeNameConstants.SECURITY_CLASSIFICATION));
        assertThat(argumentCaptor.getValue()
                .getValues(), is(Collections.singletonList(id1)));

    }

}
