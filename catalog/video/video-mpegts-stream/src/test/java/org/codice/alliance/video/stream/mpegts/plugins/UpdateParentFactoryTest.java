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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class UpdateParentFactoryTest {

    @Test
    public void testBuild() {
        UpdateParentFactory.Factory factory = mock(UpdateParentFactory.Factory.class);
        UpdateParent.UpdateField updateField = mock(UpdateParent.UpdateField.class);
        when(factory.build()).thenReturn(updateField);
        UpdateParentFactory updateParentFactory = new UpdateParentFactory(factory);
        FindChildrenStreamEndPlugin.Handler handler = updateParentFactory.build();
        assertThat(handler, is(instanceOf(UpdateParent.class)));
    }

}
