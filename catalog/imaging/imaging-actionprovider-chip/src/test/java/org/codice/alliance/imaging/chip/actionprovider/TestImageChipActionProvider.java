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
package org.codice.alliance.imaging.chip.actionprovider;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.codice.ddf.configuration.SystemBaseUrl;
import org.junit.Before;
import org.junit.Test;

import ddf.action.Action;
import ddf.catalog.data.impl.MetacardImpl;

public class TestImageChipActionProvider {

    private static final String ZIP_CONTENT_TYPE = "application/zip";

    private static final String ID = "12345";

    private static final String SOURCE = "alliance.distribution";

    private static final String LOCATION =
            "POLYGON((0.1234 2.222, 0.4444 1.222, 0.1234 1.222, 0.1234 2.222, 0.1234 2.222))";

    private ImagingChipActionProvider imagingChipActionProvider;

    private MetacardImpl metacard;

    @Before
    public void setUp() {
        imagingChipActionProvider = new ImagingChipActionProvider();
        metacard = new MetacardImpl();
        metacard.setContentTypeName(ImagingChipActionProvider.NITF_CONTENT_TYPE);
        metacard.setId(ID);
        metacard.setSourceId(SOURCE);
        metacard.setLocation(LOCATION);
    }

    @Test
    public void testCanHandleNullMetacard() {
        assertThat(imagingChipActionProvider.canHandle(null), is(false));
    }

    @Test
    public void testCanHandleInvalidMetacard() {
        assertThat(imagingChipActionProvider.canHandle(new Object()), is(false));
    }

    @Test
    public void testCanHandleInvalidLocationOnMetacard() {
        metacard.setLocation(null);
        assertThat(imagingChipActionProvider.canHandle(metacard), is(false));
    }

    @Test
    public void testCanHandleNullContentTypeMetacard() {
        MetacardImpl metacard = new MetacardImpl();
        assertThat(imagingChipActionProvider.canHandle(metacard), is(false));
    }

    @Test
    public void testCanHandleNonImageryMetacard() {
        MetacardImpl metacard = new MetacardImpl();
        metacard.setContentTypeName(ZIP_CONTENT_TYPE);
        assertThat(imagingChipActionProvider.canHandle(metacard), is(false));
    }

    @Test
    public void testCanHandleImageryMetacard() {
        assertThat(imagingChipActionProvider.canHandle(metacard), is(true));
    }

    @Test
    public void testGetId() {
        assertThat(imagingChipActionProvider.getId(), is(ImagingChipActionProvider.ID));
    }

    @Test
    public void testGetActionsNullMetacard() {
        assertThat(imagingChipActionProvider.getActions(null), hasSize(0));
    }

    @Test
    public void testGetActionsCanNotHandleMetacard() {
        metacard.setLocation(null);
        assertThat(imagingChipActionProvider.getActions(metacard), hasSize(0));
    }

    @Test
    public void testGetActionsUnsupportedProtocolOnMetacard() {
        String protocol = SystemBaseUrl.getProtocol();
        System.setProperty("org.codice.ddf.system.protocol", "udp://");
        assertThat(imagingChipActionProvider.getActions(metacard), hasSize(0));
        System.setProperty("org.codice.ddf.system.protocol", protocol);
    }

    @Test
    public void testGetActionsMetacard() {
        List<Action> actions = imagingChipActionProvider.getActions(metacard);
        assertThat(actions, hasSize(1));

        Action action = actions.get(0);
        assertThat(action.getId(), is(ImagingChipActionProvider.ID));
        assertThat(action.getDescription(), is(ImagingChipActionProvider.DESCRIPTION));
        assertThat(action.getTitle(), is(ImagingChipActionProvider.TITLE));

        String url = action.getUrl()
                .toString();
        assertThat(url, containsString(ImagingChipActionProvider.PATH));
        assertThat(url, containsString("id=" + ID));
        assertThat(url, containsString("source=" + SOURCE));
    }
}
