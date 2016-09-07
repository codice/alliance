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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.FileNotFoundException;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfParserInputFlow;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;
import org.junit.Before;
import org.junit.Test;

public class RoutingSlipTest {

    private static final String IMAGE_NO_TRE_NTF_FILENAME = "src/test/resources/imageNoTre.ntf";

    private static final String IMAGE_TRE_NTF_FILENAME = "src/test/resources/imageTre.ntf";

    private static final String NO_IMAGE_NO_TRE_NTF_FILENAME =
            "src/test/resources/noImageNoTre.ntf";

    private static final String NO_IMAGE_TRE_NTF_FILENAME = "src/test/resources/noImageTre.ntf";

    private RoutingSlip routingSlip;

    @Before
    public void setUp() {
        this.routingSlip = new RoutingSlip();
        TreTestUtility.createFileIfNecessary(IMAGE_NO_TRE_NTF_FILENAME,
                TreTestUtility::createNitfImageNoTres);
        TreTestUtility.createFileIfNecessary(IMAGE_TRE_NTF_FILENAME,
                TreTestUtility::createNitfImageTres);
        TreTestUtility.createFileIfNecessary(NO_IMAGE_NO_TRE_NTF_FILENAME,
                TreTestUtility::createNitfNoImageNoTres);
        TreTestUtility.createFileIfNecessary(NO_IMAGE_TRE_NTF_FILENAME,
                TreTestUtility::createNitfNoImageTres);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChannelNullInput() {
        routingSlip.channel(null);
    }

    @Test
    public void testChannelImageNoTres() throws FileNotFoundException, NitfFormatException {
        NitfSegmentsFlow parserInputFlow = new NitfParserInputFlow().file(new File(
                IMAGE_NO_TRE_NTF_FILENAME))
                .headerOnly();

        String channel = routingSlip.channel(parserInputFlow);
        assertThat(channel, is(RoutingSlip.IMAGE_ROUTE));
    }

    @Test
    public void testChannelImageTre() throws FileNotFoundException, NitfFormatException {
        NitfSegmentsFlow parserInputFlow = new NitfParserInputFlow().file(new File(
                IMAGE_TRE_NTF_FILENAME))
                .headerOnly();

        String channel = routingSlip.channel(parserInputFlow);
        assertThat(channel, is(RoutingSlip.IMAGE_ROUTE));
    }

    @Test
    public void testChannelNoImageTre() throws FileNotFoundException, NitfFormatException {
        NitfSegmentsFlow parserInputFlow = new NitfParserInputFlow().file(new File(
                NO_IMAGE_TRE_NTF_FILENAME))
                .headerOnly();

        String channel = routingSlip.channel(parserInputFlow);
        assertThat(channel, is(RoutingSlip.GMTI_ROUTE));
    }

    @Test
    public void testChannelNoImageNoTre() throws FileNotFoundException, NitfFormatException {
        NitfSegmentsFlow parserInputFlow = new NitfParserInputFlow().file(new File(
                NO_IMAGE_NO_TRE_NTF_FILENAME))
                .headerOnly();

        String channel = routingSlip.channel(parserInputFlow);
        assertThat(channel, is(RoutingSlip.IMAGE_ROUTE));
    }

}
