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
package org.codice.alliance.imaging.chip.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.codice.alliance.imaging.chip.service.api.ChipOutOfBoundsException;
import org.codice.alliance.imaging.chip.service.api.ChipService;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * ChipServiceImpl tests
 */
public class TestChipServiceImpl {

    private static final String OVERVIEW_FILE = "/i_3001a.png";

    private BufferedImage inputImage;

    private Polygon overviewPolygon;

    private static final String OVERVIEW_POLYGON_STRING =
            "POLYGON ((0.4897222222222222 52.74027777777778, 0.4994444444444444 52.72944444444445, 0.48 52.72222222222222, 0.4702777777777777 52.73305555555555, 0.4897222222222222 52.74027777777778))";

    private Polygon chipPolygon;

    private static final String CHIP_POLYGON_STRING =
            "POLYGON ((0.4771 52.7257, 0.4784 52.7257, 0.4784 52.7353, 0.4771 52.7353, 0.4771 52.7257))";

    private ChipService chipService;

    @Before
    public void setUp() throws IOException, ParseException {
        this.chipService = new ChipServiceImpl();
        this.inputImage = ImageIO.read(getInputStream(OVERVIEW_FILE));
        WKTReader wktReader = new WKTReader();
        this.overviewPolygon = (Polygon) wktReader.read(OVERVIEW_POLYGON_STRING);
        this.chipPolygon = (Polygon) wktReader.read(CHIP_POLYGON_STRING);
    }

    @Test
    public void testChip() throws ChipOutOfBoundsException {
        BufferedImage chipImage = chipService.chip(inputImage, overviewPolygon, chipPolygon);
        assertThat(chipImage, is(notNullValue()));
    }

    @Test(expected = ChipOutOfBoundsException.class)
    public void testChipOutOfBounds() throws ChipOutOfBoundsException {
        chipService.chip(inputImage, chipPolygon, overviewPolygon);
    }

    private InputStream getInputStream(String filename) {
        assertThat(String.format("Test file missing - %s", filename),
                getClass().getResource(filename),
                is(notNullValue()));
        return getClass().getResourceAsStream(filename);
    }
}
