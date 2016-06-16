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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.codice.alliance.imaging.chip.service.api.ChipOutOfBoundsException;
import org.codice.alliance.imaging.chip.service.api.ChipService;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.CatalogFramework;
import ddf.catalog.Constants;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;

public class TestWorkflow {
    private ChipService mockChipService;

    private CatalogFramework catalogFramework;

    private MetacardImpl mockMetacard;

    @Before
    public void setUp() throws Exception {
        mockMetacard();
        mockCatalogFramework();
        mockChipService();
    }

    private void mockMetacard() throws ParseException {
        this.mockMetacard = new MetacardImpl();
        String location =
                "POLYGON ((0.4897 52.7403, 0.4994 52.7294, 0.48 52.72, 0.4703 52.7331, 0.4897 52.7403))";
        this.mockMetacard.setLocation(location);
        Attribute attribute = new AttributeImpl(Metacard.DERIVED_RESOURCE_URI,
                "content:10123#overview");
        this.mockMetacard.setAttribute(attribute);
    }

    private void mockCatalogFramework()
            throws IOException, ResourceNotFoundException, ResourceNotSupportedException {
        this.catalogFramework = mock(CatalogFramework.class);
        ResourceResponse response = mock(ResourceResponse.class);
        Map<String, Serializable> properties = new HashMap<>();
        properties.put(Constants.METACARD_PROPERTY, mockMetacard);
        when(response.getProperties()).thenReturn(properties);
        Resource resource = mock(Resource.class);
        InputStream contentInputStream = getInputStream("/i_3001a.png");

        when(resource.getInputStream()).thenReturn(contentInputStream);
        when(response.getResource()).thenReturn(resource);
        when(catalogFramework.getLocalResource(any(ResourceRequest.class))).thenReturn(response);
    }

    private void mockChipService() throws IOException, ChipOutOfBoundsException {
        this.mockChipService = mock(ChipService.class);

        BufferedImage chipImage = ImageIO.read(getInputStream("/i_3001a-chip.png"));

        when(mockChipService.chip(any(BufferedImage.class),
                any(Polygon.class),
                any(Polygon.class))).thenReturn(chipImage);
    }

    @Test
    public void testWorkflow() throws Exception {
        CatalogInputAdapter catalogInputAdapter = new CatalogInputAdapter();
        CatalogOutputAdapter catalogOutputAdapter = new CatalogOutputAdapter();
        WKTReader wktReader = new WKTReader();

        ResourceRequest resourceRequest = catalogInputAdapter.buildReadRequest(mockMetacard,
                "overview");
        assertThat(resourceRequest, is(notNullValue()));

        ResourceResponse resourceResponse = catalogFramework.getLocalResource(resourceRequest);
        assertThat(resourceResponse, is(notNullValue()));

        Geometry requestGeo = wktReader.read(
                "POLYGON ((0.4771 52.7257, 0.4784 52.7257, 0.4784 52.7353, 0.4771 52.7353, 0.4771 52.7257))");
        assertThat(requestGeo, is(notNullValue()));

        String overviewGeoString = catalogOutputAdapter.getLocation(resourceResponse);
        assertThat(overviewGeoString, is(notNullValue()));

        Geometry overviewGeo = wktReader.read(overviewGeoString);
        assertThat(overviewGeo, is(notNullValue()));

        BufferedImage overviewImage = catalogOutputAdapter.getImage(resourceResponse);
        assertThat(overviewImage, is(notNullValue()));

        BufferedImage chipImage = mockChipService.chip(overviewImage,
                (Polygon) overviewGeo,
                (Polygon) requestGeo);
        assertThat(chipImage, is(notNullValue()));

        BinaryContent binaryContent = catalogOutputAdapter.getBinaryContent(chipImage);
        assertThat(binaryContent, is(notNullValue()));
    }

    private InputStream getInputStream(String filename) {
        assertThat(String.format("Test file missing - %s", filename),
                getClass().getResource(filename),
                is(notNullValue()));
        return getClass().getResourceAsStream(filename);
    }
}
