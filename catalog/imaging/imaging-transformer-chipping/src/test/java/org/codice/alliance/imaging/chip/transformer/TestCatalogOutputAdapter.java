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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import ddf.catalog.data.BinaryContent;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;

public class TestCatalogOutputAdapter {
    private static final String I_3001A = "/i_3001a.png";

    private CatalogOutputAdapter catalogOutputAdapter;

    @Before
    public void setUp() {
        this.catalogOutputAdapter = new CatalogOutputAdapter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetImageNullCatalogResponse() throws IOException {
        catalogOutputAdapter.getImage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetImageNullResource() throws IOException {
        ResourceResponse resourceResponse = mock(ResourceResponse.class);
        when(resourceResponse.getResource()).thenReturn(null);
        catalogOutputAdapter.getImage(resourceResponse);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetImageNullInputStream() throws IOException {
        ResourceResponse resourceResponse = mock(ResourceResponse.class);
        Resource resource = mock(Resource.class);
        when(resourceResponse.getResource()).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(null);
        catalogOutputAdapter.getImage(resourceResponse);
    }

    @Test
    public void testGetImage() throws IOException {
        InputStream is = getInputStream(I_3001A);
        ResourceResponse resourceResponse = mock(ResourceResponse.class);
        Resource resource = mock(Resource.class);
        when(resourceResponse.getResource()).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(is);
        BufferedImage image = catalogOutputAdapter.getImage(resourceResponse);
        assertThat(image, is(notNullValue()));
        assertThat(image.getWidth(), is(1024));
        assertThat(image.getHeight(), is(1024));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBinaryContentNullImage() throws IOException, MimeTypeParseException {
        catalogOutputAdapter.getBinaryContent(null);
    }

    @Test
    public void testGetBinaryContent() throws IOException, MimeTypeParseException {
        BufferedImage suppliedImage = ImageIO.read(getInputStream(I_3001A));
        suppliedImage = new BufferedImage(suppliedImage.getWidth(), suppliedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        BinaryContent binaryContent = catalogOutputAdapter.getBinaryContent(suppliedImage);
        assertThat(binaryContent, is(notNullValue()));
        assertThat(binaryContent.getInputStream(), is(notNullValue()));

        BufferedImage returnedImage = ImageIO.read(binaryContent.getInputStream());
        assertThat(returnedImage.getWidth(), is(1024));
        assertThat(returnedImage.getHeight(), is(1024));
    }

    private InputStream getInputStream(String filename) {
        assertNotNull("Test file missing", getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
    }
}
