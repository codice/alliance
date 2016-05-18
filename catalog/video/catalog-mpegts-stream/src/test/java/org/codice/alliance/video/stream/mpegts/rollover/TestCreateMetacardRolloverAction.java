/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Collections;

import org.codice.alliance.video.stream.mpegts.Constants;
import org.hamcrest.Matchers;
import org.junit.Test;

import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;

public class TestCreateMetacardRolloverAction {

    @Test
    public void testDoAction() throws RolloverActionException {
        MetacardType metacardType = mock(MetacardType.class);
        CreateMetacardRolloverAction createMetacardRolloverAction =
                new CreateMetacardRolloverAction(Collections.singletonList(metacardType));
        MetacardImpl metacard = createMetacardRolloverAction.doAction(null, new File("a"));
        assertThat(metacard.getMetacardType(), is(metacardType));
        assertThat(metacard.getContentTypeName(), Matchers.is(Constants.MPEGTS_MIME_TYPE));
    }

    @Test(expected = RolloverActionException.class)
    public void testException() throws RolloverActionException {
        CreateMetacardRolloverAction createMetacardRolloverAction =
                new CreateMetacardRolloverAction(Collections.emptyList());
        createMetacardRolloverAction.doAction(null, new File("a"));
    }

    @Test
    public void testToString() {
        CreateMetacardRolloverAction createMetacardRolloverAction =
                new CreateMetacardRolloverAction(Collections.emptyList());
        assertThat(createMetacardRolloverAction.toString(), notNullValue());
    }
}
