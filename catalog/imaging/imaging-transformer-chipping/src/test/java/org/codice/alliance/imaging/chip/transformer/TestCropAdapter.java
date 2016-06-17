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
package org.codice.alliance.imaging.chip.transformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;

public class TestCropAdapter {
    private BufferedImage originalImage;

    private BufferedImage overviewImage;

    private Map<String, Serializable> args;

    private CropAdapter cropAdapter;

    @Before
    public void setUp() {
        this.cropAdapter = new CropAdapter();
        this.originalImage = mock(BufferedImage.class);
        this.overviewImage = mock(BufferedImage.class);
        this.args = new HashedMap();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArguments() {
        cropAdapter.scaleChip(originalImage, overviewImage, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScaleChipNullHValue() {
        args.put("x", 10);
        args.put("y", 20);
        args.put("w", 30);
        cropAdapter.scaleChip(originalImage, overviewImage, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScaleChipNullWValue() {
        args.put("x", 10);
        args.put("y", 20);
        args.put("h", 30);
        cropAdapter.scaleChip(originalImage, overviewImage, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScaleChipNullXValue() {
        args.put("y", 10);
        args.put("w", 20);
        args.put("h", 30);
        cropAdapter.scaleChip(originalImage, overviewImage, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScaleChipNullYValue() {
        args.put("x", 10);
        args.put("w", 20);
        args.put("h", 30);
        cropAdapter.scaleChip(originalImage, overviewImage, args);
    }

    @Test
    public void testScaleChipNullOriginal() {
        when(overviewImage.getWidth()).thenReturn(1024);
        when(overviewImage.getHeight()).thenReturn(1024);
        int[] values = new int[]{100, 100, 512, 512};
        args.put("x", values[0]);
        args.put("y", values[1]);
        args.put("w", values[2]);
        args.put("h", values[3]);

        int[] scaledValues = cropAdapter.scaleChip(null, overviewImage, args);

        for (int i = 0; i < values.length; i++) {
            assertThat(scaledValues[i], is(values[i]));
        }
    }

    @Test
    public void testScaleChipNullOverview() {
        when(originalImage.getWidth()).thenReturn(1024);
        when(originalImage.getHeight()).thenReturn(1024);
        int[] values = new int[]{100, 100, 512, 512};
        args.put("x", values[0]);
        args.put("y", values[1]);
        args.put("w", values[2]);
        args.put("h", values[3]);

        int[] scaledValues = cropAdapter.scaleChip(originalImage, null, args);

        for (int i = 0; i < values.length; i++) {
            assertThat(scaledValues[i], is(values[i]));
        }
    }

    @Test
    public void testScaleChip() {
        when(overviewImage.getWidth()).thenReturn(1024);
        when(overviewImage.getHeight()).thenReturn(1024);
        when(originalImage.getWidth()).thenReturn(2048);
        when(originalImage.getHeight()).thenReturn(2048);
        int[] values = new int[]{100, 100, 400, 400};
        args.put("x", values[0]);
        args.put("y", values[1]);
        args.put("w", values[2]);
        args.put("h", values[3]);

        int[] scaledValues = cropAdapter.scaleChip(originalImage, overviewImage, args);

        for (int i = 0; i < values.length; i++) {
            assertThat(scaledValues[i], is(values[i] * 2));
        }
    }
}
