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
package org.codice.alliance.video.stream.mpegts.rollover;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import ddf.catalog.data.impl.MetacardImpl;

public class TestListRolloverAction {

    @Test
    public void testDoAction() throws RolloverActionException {
        RolloverAction rolloverAction1 = mock(RolloverAction.class);
        RolloverAction rolloverAction2 = mock(RolloverAction.class);

        MetacardImpl metacard = mock(MetacardImpl.class);
        File tempFile = new File("a");

        when(rolloverAction1.doAction(metacard, tempFile)).thenReturn(metacard);
        when(rolloverAction2.doAction(metacard, tempFile)).thenReturn(metacard);

        ListRolloverAction listRolloverAction =
                new ListRolloverAction(Arrays.asList(rolloverAction1, rolloverAction2));

        MetacardImpl result = listRolloverAction.doAction(metacard, tempFile);

        assertThat(result, is(metacard));
        verify(rolloverAction1).doAction(metacard, tempFile);
        verify(rolloverAction2).doAction(metacard, tempFile);

    }

    @Test
    public void testToString() {
        assertThat(new ListRolloverAction(Collections.emptyList()).toString(), notNullValue());
    }

}
