/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.imaging.chip.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import org.codice.alliance.imaging.chip.service.api.ChipOutOfBoundsException;
import org.codice.alliance.imaging.chip.service.api.ChipService;
import org.junit.Before;
import org.junit.Test;
import org.la4j.Vector;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/** ChipServiceImpl tests */
public class ChipServiceImplTest {

  private static final String OVERVIEW_FILE = "/i_3001a.png";

  private BufferedImage inputImage;

  private ChipService chipService;

  private WKTReader wktReader;

  private Semaphore lock;

  @Before
  public void setUp() throws IOException, ParseException {
    lock = mock(Semaphore.class);
    this.chipService = new ChipServiceImpl(lock);
    this.inputImage = ImageIO.read(getInputStream(OVERVIEW_FILE));

    this.wktReader = new WKTReader();
  }

  @Test(expected = ChipOutOfBoundsException.class)
  public void testCropInvalidHeight() throws ChipOutOfBoundsException {
    chipService.crop(inputImage, 10, 10, 100, -1);
  }

  @Test(expected = ChipOutOfBoundsException.class)
  public void testCropInvalidWidth() throws ChipOutOfBoundsException {
    chipService.crop(inputImage, 10, 10, -1, 100);
  }

  @Test(expected = ChipOutOfBoundsException.class)
  public void testCropInvalidX() throws ChipOutOfBoundsException {
    chipService.crop(inputImage, 10_000, 10, -1, 100);
  }

  @Test(expected = ChipOutOfBoundsException.class)
  public void testCropInvalidY() throws ChipOutOfBoundsException {
    chipService.crop(inputImage, 10, 10_000, 100, 100);
  }

  @Test
  public void testCropNegativeOrigin() throws ChipOutOfBoundsException, InterruptedException {
    BufferedImage result = chipService.crop(inputImage, -100, -100, 100, 100);
    assertThat(result.getWidth(), is(100));
    assertThat(result.getHeight(), is(100));
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test
  public void testCropExtremeWidthHeight() throws ChipOutOfBoundsException, InterruptedException {
    BufferedImage result = chipService.crop(inputImage, 0, 0, 10_000, 10_000);
    assertThat(result.getWidth(), is(inputImage.getWidth()));
    assertThat(result.getHeight(), is(inputImage.getHeight()));
    verify(lock).acquire();
    verify(lock).release();
  }

  @Test
  public void testChip() throws ChipOutOfBoundsException, ParseException, InterruptedException {
    Boundary mainImage = new Boundary(52.0, 15.0, 100, Math.toRadians(30));
    Boundary chip = new Boundary(52.0, 15.0, 10, Math.toRadians(30));
    Polygon mainPolygon = createPolygon(mainImage.getBoundary());
    Polygon chipPolygon = createPolygon(chip.getBoundary());
    BufferedImage result = chipService.chip(inputImage, mainPolygon, chipPolygon);
    assertThat(result.getWidth(), is(102));
    assertThat(result.getHeight(), is(102));
    verify(lock).acquire();
    verify(lock).release();
  }

  private Polygon createPolygon(List<Vector> vectors) throws ParseException {
    StringBuilder stringBuilder = new StringBuilder("POLYGON ((");

    vectors
        .stream()
        .map(v -> String.format("%s %s, ", v.get(0), v.get(1)))
        .forEach(stringBuilder::append);

    stringBuilder.append(String.format("%s %s ))", vectors.get(0).get(0), vectors.get(0).get(1)));
    return (Polygon) wktReader.read(stringBuilder.toString());
  }

  private InputStream getInputStream(String filename) {
    assertThat(
        String.format("Test file missing - %s", filename),
        getClass().getResource(filename),
        is(notNullValue()));
    return getClass().getResourceAsStream(filename);
  }
}
