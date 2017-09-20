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
package org.codice.alliance.distribution.branding;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class AllianceBrandingPluginTest {

  private AllianceBrandingPlugin allianceBrandingPlugin;

  private static String propertiesFilePath = "/META-INF/branding.properties";

  private static String invalidPropertiesFile = "/path/to/file/invalid.properties";

  private static String productName = "Alliance";

  private static String productURL = "http://github.com/codice/alliance";

  private static String productImage = "/alliance/alliance.png";

  private static String vendorName = "Codice Foundation";

  private static String vendorURL = "http://github.com/codice/alliance";

  private static String vendorImage = "/alliance/alliance.png";

  private static String favIcon = "/alliance/favicon.ico";

  @Before
  public void setupAllianceBrandingPlugin() {
    allianceBrandingPlugin = new AllianceBrandingPlugin(propertiesFilePath);
  }

  @Test
  public void testInit() {
    allianceBrandingPlugin.init();
    assertThat(
        allianceBrandingPlugin.getProductName(),
        is(equalTo(productName + " " + System.getProperty("projectVersion"))));
    assertThat(allianceBrandingPlugin.getProductURL(), is(equalTo(productURL)));
    assertThat(allianceBrandingPlugin.getProductImage(), is(equalTo(productImage)));
    assertThat(allianceBrandingPlugin.getVendorName(), is(equalTo(vendorName)));
    assertThat(allianceBrandingPlugin.getVendorURL(), is(equalTo(vendorURL)));
    assertThat(allianceBrandingPlugin.getVendorImage(), is(equalTo(vendorImage)));
    assertThat(allianceBrandingPlugin.getFavIcon(), is(equalTo(favIcon)));
  }

  @Test
  public void testInitException() {
    AllianceBrandingPlugin allianceBrandingPlugin =
        new AllianceBrandingPlugin(invalidPropertiesFile);
    allianceBrandingPlugin.init();
    assertThat(allianceBrandingPlugin.getProductName(), is(equalTo(productName)));
    assertThat(allianceBrandingPlugin.getProductURL(), is(equalTo(productURL)));
    assertThat(allianceBrandingPlugin.getProductImage(), is(equalTo(productImage)));
    assertThat(allianceBrandingPlugin.getVendorName(), is(equalTo(vendorName)));
    assertThat(allianceBrandingPlugin.getVendorURL(), is(equalTo(vendorURL)));
    assertThat(allianceBrandingPlugin.getVendorImage(), is(equalTo(vendorImage)));
    assertThat(allianceBrandingPlugin.getFavIcon(), is(equalTo(favIcon)));
  }

  @Test
  public void testProductImage() throws IOException {
    allianceBrandingPlugin.init();
    assertThat(
        allianceBrandingPlugin.getBase64ProductImage(),
        is(
            equalTo(
                Base64.getEncoder()
                    .encodeToString(
                        IOUtils.toByteArray(
                            AllianceBrandingPluginTest.class.getResourceAsStream(productImage))))));
  }

  @Test
  public void testVendorImage() throws IOException {
    allianceBrandingPlugin.init();
    assertThat(
        allianceBrandingPlugin.getBase64VendorImage(),
        is(
            equalTo(
                Base64.getEncoder()
                    .encodeToString(
                        IOUtils.toByteArray(
                            AllianceBrandingPluginTest.class.getResourceAsStream(vendorImage))))));
  }

  @Test
  public void testFavIcon() throws IOException {
    allianceBrandingPlugin.init();
    assertThat(
        allianceBrandingPlugin.getBase64FavIcon(),
        is(
            equalTo(
                Base64.getEncoder()
                    .encodeToString(
                        IOUtils.toByteArray(
                            AllianceBrandingPluginTest.class.getResourceAsStream(favIcon))))));
  }
}
