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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;

public class UpdateFieldListFactoryTest {

    @Test
    public void testBuild() {

        UpdateParentFactory.Factory childFactory = mock(UpdateParentFactory.Factory.class);

        UpdateParent.UpdateField childUpdateField = mock(UpdateParent.UpdateField.class);
        when(childFactory.build()).thenReturn(childUpdateField);

        ListUpdateFieldFactory factory = new ListUpdateFieldFactory(Collections.singletonList(childFactory));

        UpdateParent.UpdateField updateField = factory.build();

        assertThat(updateField, is(instanceOf(UpdateFieldList.class)));

        UpdateFieldList updateFieldList = (UpdateFieldList) updateField;

        assertThat(updateFieldList.getUpdateFieldList(), hasSize(1));

        assertThat(updateFieldList.getUpdateFieldList().get(0), is(childUpdateField));

    }

}
