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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.codice.alliance.video.stream.mpegts.Context;
import org.junit.Test;

public class ListStreamCreationPluginTest {

    @Test
    public void testOnCreate() throws StreamCreationException {

        Context context = mock(Context.class);
        StreamCreationPlugin streamCreationPlugin = mock(StreamCreationPlugin.class);

        ListStreamCreationPlugin listStreamCreationPlugin =
                new ListStreamCreationPlugin(Collections.singletonList(streamCreationPlugin));

        listStreamCreationPlugin.onCreate(context);

        verify(streamCreationPlugin).onCreate(context);

    }

}
