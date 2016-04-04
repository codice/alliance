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
package com.connexta.alliance.libs.klv;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.connexta.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

import ddf.catalog.data.Attribute;

public class TestMissionIdKlvProcessor {

    @Test
    public void test() {

        String id1 = "ID1";
        String id2 = "ID2";

        ArgumentCaptor<Attribute> argumentCaptor =
                KlvUtilities.testKlvProcessor(new MissionIdKlvProcessor(),
                        Stanag4609TransportStreamParser.MISSION_ID,
                        Arrays.asList(id1, id2, id1, id2));

        assertThat(argumentCaptor.getValue()
                .getName(), is(AttributeNameConstants.MISSION_ID));
        assertThat(argumentCaptor.getValue()
                .getValues(), hasSize(2));
        assertThat(argumentCaptor.getValue()
                .getValues()
                .containsAll(Arrays.asList(id1, id2)), is(true));

    }

}
