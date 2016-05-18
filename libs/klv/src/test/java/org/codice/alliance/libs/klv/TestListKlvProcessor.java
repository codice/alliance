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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import ddf.catalog.data.impl.MetacardImpl;

public class TestListKlvProcessor {

    @Test
    public void testProcess() {

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        KlvProcessor klvProcessor = (handlers, metacard, configuration) -> atomicBoolean.set(true);

        ListKlvProcessor listKlvProcessor = new ListKlvProcessor(Collections.singletonList(
                klvProcessor));

        listKlvProcessor.process(Collections.emptyMap(),
                mock(MetacardImpl.class),
                new KlvProcessor.Configuration());

        assertThat(atomicBoolean.get(), is(true));

    }

}
