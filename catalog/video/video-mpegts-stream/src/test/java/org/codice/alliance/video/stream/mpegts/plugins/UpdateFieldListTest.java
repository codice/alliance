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
import java.util.List;

import org.junit.Test;

import ddf.catalog.data.Metacard;

public class UpdateFieldListTest {

    @Test
    public void testUpdateField() {

        UpdateParent.UpdateField updateField = mock(UpdateParent.UpdateField.class);

        UpdateFieldList updateFieldList =
                new UpdateFieldList(Collections.singletonList(updateField));

        Metacard parent = mock(Metacard.class);
        List<Metacard> children = mock(List.class);

        updateFieldList.updateField(parent, children);

        verify(updateField).updateField(parent, children);

    }

    @Test
    public void testEnd() {

        UpdateParent.UpdateField updateField = mock(UpdateParent.UpdateField.class);

        UpdateFieldList updateFieldList =
                new UpdateFieldList(Collections.singletonList(updateField));

        Metacard parent = mock(Metacard.class);

        updateFieldList.end(parent);

        verify(updateField).end(parent);

    }

}
