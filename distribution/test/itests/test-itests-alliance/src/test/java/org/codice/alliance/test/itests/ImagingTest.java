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

package org.codice.alliance.test.itests;

import static org.codice.ddf.itests.common.AbstractIntegrationTest.DynamicUrl.SECURE_ROOT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.codice.alliance.test.itests.common.AbstractAllianceIntegrationTest;
import org.codice.ddf.itests.common.annotations.BeforeExam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.cm.Configuration;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ImagingTest extends AbstractAllianceIntegrationTest {
    private static final String[] REQUIRED_APPS =
            {"catalog-app", "solr-app", "spatial-app", "imaging-app"};

    private static final String TEST_IMAGE_NITF = "i_3001a.ntf";

    private static final String TEST_MTI_NITF = "gmti-test.ntf";

    @BeforeExam
    public void beforeAllianceTest() throws Exception {
        try {
            basePort = getBasePort();
            getAdminConfig().setLogLevels();
            getServiceManager().waitForRequiredApps(REQUIRED_APPS);
            getServiceManager().waitForAllBundles();
            getCatalogBundle().waitForCatalogProvider();
            configureSecurityStsClient();
        } catch (Exception e) {
            LOGGER.error("Failed in @BeforeExam: ", e);
            fail("Failed in @BeforeExam: " + e.getMessage());
        }
    }

    private String ingestNitfFile(String fileName) throws Exception {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fileName);
        byte[] fileBytes = IOUtils.toByteArray(inputStream);

        return given().multiPart("file", fileName, fileBytes, "image/nitf")
                .expect()
                .statusCode(HttpStatus.SC_CREATED)
                .when()
                .post(REST_PATH.getUrl())
                .getHeader("id");
    }

    @Test
    public void testValidImageNitfMetacard() throws Exception {
        String id = ingestNitfFile(TEST_IMAGE_NITF);

        String url = REST_PATH.getUrl() + id;

        when().get(url)
                .then()
                .assertThat()
                .contentType(MediaType.TEXT_XML)
                .body(hasXPath("/metacard[@id='" + id + "']/type", is("isr.image")));

        deleteMetacard(id);
    }

    @Test
    public void testValidMtiNitfMetacard() throws Exception {
        String id = ingestNitfFile(TEST_MTI_NITF);

        String url = REST_PATH.getUrl() + id;

        when().get(url)
                .then()
                .assertThat()
                .contentType(MediaType.TEXT_XML)
                .body(hasXPath("/metacard[@id='" + id + "']/type", is("isr.gmti")));

        deleteMetacard(id);
    }

    private void assertGetJpeg(String imageUrl) throws Exception {
        given().get(imageUrl)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, is("image/jpeg"));
    }

    @Test
    public void testNitfImageGeneration() throws Exception {
        String id = ingestNitfFile(TEST_IMAGE_NITF);

        assertGetJpeg(REST_PATH.getUrl() + id + "?transform=thumbnail");
        assertGetJpeg(REST_PATH.getUrl() + id + "?transform=resource&qualifier=original");
        assertGetJpeg(REST_PATH.getUrl() + id + "?transform=resource&qualifier=overview");

        deleteMetacard(id);
    }

    @Test
    public void testImageNitfChipCreation() throws Exception {
        String id = ingestNitfFile(TEST_IMAGE_NITF);

        String chippingUrl = SECURE_ROOT + HTTPS_PORT.getPort() + "/chipping/chipping.html?id=" + id
                + "&source=Alliance";
        given().get(chippingUrl)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        final int width = 350;
        final int height = 240;

        String chippedImageUrl =
                SERVICE_ROOT + "/catalog/" + id + "?transform=chip&qualifier=overview&x=" + 300
                        + "&y=" + 200 + "&w=" + width + "&h=" + height;
        InputStream chippedImageStream = given().get(chippedImageUrl)
                .asInputStream();
        BufferedImage chippedImage = ImageIO.read(chippedImageStream);

        assertThat(chippedImage.getWidth(), is(width));
        assertThat(chippedImage.getHeight(), is(height));

        deleteMetacard(id);
    }

    private static void deleteMetacard(String id) {
        delete(REST_PATH.getUrl() + id);
    }

    private void configureSecurityStsClient() throws IOException, InterruptedException {
        Configuration stsClientConfig = configAdmin.getConfiguration(
                "ddf.security.sts.client.configuration.cfg",
                null);
        Dictionary<String, Object> properties = new Hashtable<>();

        properties.put("address",
                SECURE_ROOT + HTTPS_PORT.getPort() + "/services/SecurityTokenService?wsdl");
        stsClientConfig.update(properties);
    }
}

