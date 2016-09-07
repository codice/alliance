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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;

public class ListKlvProcessorTest {

    private LocationKlvProcessor childKlvProcessor;

    private ListKlvProcessor listKlvProcessor;

    @Before
    public void setup() {
        childKlvProcessor = mock(LocationKlvProcessor.class);
        listKlvProcessor = new ListKlvProcessor(Collections.singletonList(childKlvProcessor));
    }

    @Test
    public void testProcess() {

        Map<String, KlvHandler> handlers = Collections.emptyMap();
        Metacard metacard = mock(MetacardImpl.class);
        KlvProcessor.Configuration configuration = new KlvProcessor.Configuration();

        listKlvProcessor.process(handlers, metacard, configuration);

        verify(childKlvProcessor).process(handlers, metacard, configuration);

    }

    @Test
    public void testAccept() {
        KlvProcessor.Visitor visitor = mock(KlvProcessor.Visitor.class);
        listKlvProcessor.accept(visitor);
        verify(childKlvProcessor).accept(visitor);
    }

}
