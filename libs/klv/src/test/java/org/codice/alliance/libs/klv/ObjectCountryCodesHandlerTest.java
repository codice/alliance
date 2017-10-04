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
package org.codice.alliance.libs.klv;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import org.codice.ddf.libs.klv.KlvDecodingException;
import org.codice.ddf.libs.klv.data.raw.KlvBytes;
import org.junit.Test;

public class ObjectCountryCodesHandlerTest {

  private String name = "testCountryCode";

  private String testString = "AUS";

  @Test
  public void testUTF8CountryCodeRemainsUTF8()
      throws UnsupportedEncodingException, KlvDecodingException {
    KlvBytes countryCodeElement = KlvUtilities.createTestBytes(name, testString.getBytes("UTF-8"));

    ObjectCountryCodesHandler objectCountryCodesHandler =
        new ObjectCountryCodesHandler("objectCountryCode");
    objectCountryCodesHandler.accept(countryCodeElement);
    assertThat(objectCountryCodesHandler.asAttribute().get().getValue(), is(testString));
  }

  @Test
  public void testUTF16CountryCodeIsHandled()
      throws UnsupportedEncodingException, KlvDecodingException {
    KlvBytes countryCodeElement =
        KlvUtilities.createTestBytes(name, testString.getBytes("UTF-16BE"));

    ObjectCountryCodesHandler objectCountryCodesHandler =
        new ObjectCountryCodesHandler("objectCountryCode");
    objectCountryCodesHandler.accept(countryCodeElement);
    assertThat(objectCountryCodesHandler.asAttribute().get().getValue(), is(testString));
  }
}
