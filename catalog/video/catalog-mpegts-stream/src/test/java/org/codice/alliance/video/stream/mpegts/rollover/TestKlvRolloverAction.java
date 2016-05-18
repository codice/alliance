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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.codice.alliance.libs.klv.KlvHandler;
import org.codice.alliance.libs.klv.KlvProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ddf.catalog.data.impl.MetacardImpl;

public class TestKlvRolloverAction {

    private KlvHandler klvHandler;

    private Map<String, KlvHandler> klvHandlerMap;

    private Lock klvHandlerMapLock;

    private Integer klvLocationSubsampleCount;

    private KlvProcessor klvProcessor;

    private KlvRolloverAction klvRolloverAction;

    private MetacardImpl metacard;

    private File tempFile;

    @Before
    public void setup() {
        klvHandler = mock(KlvHandler.class);
        klvHandlerMap = Collections.singletonMap("klvFieldName", klvHandler);
        klvHandlerMapLock = mock(Lock.class);
        klvLocationSubsampleCount = 10;
        klvProcessor = mock(KlvProcessor.class);

        metacard = mock(MetacardImpl.class);
        tempFile = new File("a");

        klvRolloverAction = new KlvRolloverAction(klvHandlerMap,
                klvHandlerMapLock,
                klvLocationSubsampleCount,
                klvProcessor);
    }

    @Test
    public void testLock() throws RolloverActionException {

        klvRolloverAction.doAction(metacard, tempFile);

        verify(klvHandlerMapLock).lock();
        verify(klvHandlerMapLock).unlock();

    }

    @Test
    public void testSubsampleCount() throws RolloverActionException {

        klvRolloverAction.doAction(metacard, tempFile);

        ArgumentCaptor<KlvProcessor.Configuration> argumentCaptor = ArgumentCaptor.forClass(
                KlvProcessor.Configuration.class);

        verify(klvProcessor).process(eq(klvHandlerMap), eq(metacard), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()
                .get(KlvProcessor.Configuration.SUBSAMPLE_COUNT), is(klvLocationSubsampleCount));

    }

    @Test
    public void testHanlderResetCalled() throws RolloverActionException {

        klvRolloverAction.doAction(metacard, tempFile);

        verify(klvHandler).reset();
    }

}
