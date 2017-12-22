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

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.codice.ddf.branding.BrandingPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllianceBrandingPlugin implements BrandingPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AllianceBrandingPlugin.class);

  private static final String ALLIANCE_URL = "https://github.com/codice/alliance";

  private static final String ALLIANCE_LOGO = "/alliance/alliance-logo.png";

  private String brandingPropertiesFilePath;

  private String productName;

  private String productURL;

  private String productImage;

  private String vendorName;

  private String vendorURL;

  private String vendorImage;

  private String favIcon;

  public AllianceBrandingPlugin(String brandingPropertiesFilePath) {
    this.brandingPropertiesFilePath = brandingPropertiesFilePath;
  }

  public void init() {

    try {
      PropertiesConfiguration propertiesConfiguration =
          new PropertiesConfiguration(getClass().getResource(brandingPropertiesFilePath));
      productName = propertiesConfiguration.getString("branding.product.name", "Alliance");
      productURL = propertiesConfiguration.getString("branding.product.url", ALLIANCE_URL);
      productImage = propertiesConfiguration.getString("branding.product.image", ALLIANCE_LOGO);
      vendorName = propertiesConfiguration.getString("branding.vendor.name", "Codice Foundation");
      vendorURL = propertiesConfiguration.getString("branding.vendor.url", "http://codice.org");
      vendorImage = propertiesConfiguration.getString("branding.vendor.image", ALLIANCE_LOGO);
      favIcon = propertiesConfiguration.getString("branding.favicon", "/alliance/favicon.ico");
    } catch (ConfigurationException e) {
      LOGGER.info("Unable to read properties file {}", brandingPropertiesFilePath, e.getMessage());
      productName = "Alliance";
      productURL = ALLIANCE_URL;
      productImage = ALLIANCE_LOGO;
      vendorName = "Codice Foundation";
      vendorURL = ALLIANCE_URL;
      vendorImage = ALLIANCE_LOGO;
      favIcon = "/alliance/favicon.ico";
    }
  }

  @Override
  public String getFavIcon() {
    return favIcon;
  }

  @Override
  public String getProductImage() {
    return productImage;
  }

  @Override
  public String getProductName() {
    return productName;
  }

  @Override
  public String getProductURL() {
    return productURL;
  }

  @Override
  public String getVendorImage() {
    return vendorImage;
  }

  @Override
  public String getVendorName() {
    return vendorName;
  }

  @Override
  public String getVendorURL() {
    return vendorURL;
  }

  @Override
  public String getBase64VendorImage() throws IOException {
    try (InputStream inputStream =
        AllianceBrandingPlugin.class.getResourceAsStream(getVendorImage())) {
      byte[] vendorImageAsBytes = IOUtils.toByteArray(inputStream);
      if (vendorImageAsBytes.length > 0) {
        return Base64.getEncoder().encodeToString(vendorImageAsBytes);
      }
    }
    return "";
  }

  @Override
  public String getBase64FavIcon() throws IOException {
    try (InputStream inputStream = AllianceBrandingPlugin.class.getResourceAsStream(getFavIcon())) {
      byte[] favIconAsBytes = IOUtils.toByteArray(inputStream);
      if (favIconAsBytes.length > 0) {
        return Base64.getEncoder().encodeToString(favIconAsBytes);
      }
    }
    return "";
  }

  @Override
  public String getBase64ProductImage() throws IOException {
    try (InputStream inputStream =
        AllianceBrandingPlugin.class.getResourceAsStream(getProductImage())) {
      byte[] productImageAsBytes = IOUtils.toByteArray(inputStream);
      if (productImageAsBytes.length > 0) {
        return Base64.getEncoder().encodeToString(productImageAsBytes);
      }
    }
    return "";
  }
}
