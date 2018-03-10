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
package org.codice.alliance.catalog.core.api.impl.types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Media;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.codice.alliance.catalog.core.api.types.Dod520001;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.junit.Test;

public class MetacardTypeAttributeTest {

  private static final String TEST_NAME = "testType";

  private static final IsrAttributes ISR_ATTRIBUTES = new IsrAttributes();

  private static final CoreAttributes CORE_ATTRIBUTES = new CoreAttributes();

  private static final SecurityAttributes SECURITY_ATTRIBUTES = new SecurityAttributes();

  private static final Dod520001Attributes DOD_520001_ATTRIBUTES = new Dod520001Attributes();

  @Test
  public void testIsrMetacardType() {
    List<MetacardType> metacardTypeList = new ArrayList<>();
    metacardTypeList.add(ISR_ATTRIBUTES);
    MetacardType metacardType = new MetacardTypeImpl(TEST_NAME, metacardTypeList);
    assertMetacardAttributes(metacardType, CORE_ATTRIBUTES.getAttributeDescriptors());
    assertMetacardAttributes(metacardType, ISR_ATTRIBUTES.getAttributeDescriptors());
    assertThat(ISR_ATTRIBUTES.getName(), is("isr"));
    assertThat(ISR_ATTRIBUTES.getAttributeDescriptor(Isr.CATEGORY), notNullValue());
    assertThat(ISR_ATTRIBUTES.getAttributeDescriptor(Media.BITS_PER_SAMPLE), nullValue());
  }

  @Test
  public void testSecurityMetacardType() {
    List<MetacardType> metacardTypeList = new ArrayList<>();
    metacardTypeList.add(SECURITY_ATTRIBUTES);
    MetacardType metacardType = new MetacardTypeImpl(TEST_NAME, metacardTypeList);
    assertMetacardAttributes(metacardType, CORE_ATTRIBUTES.getAttributeDescriptors());
    assertMetacardAttributes(metacardType, SECURITY_ATTRIBUTES.getAttributeDescriptors());
    assertThat(SECURITY_ATTRIBUTES.getName(), is("security"));
    assertThat(SECURITY_ATTRIBUTES.getAttributeDescriptor(Security.CODEWORDS), notNullValue());
    assertThat(SECURITY_ATTRIBUTES.getAttributeDescriptor(Media.BITS_PER_SAMPLE), nullValue());
  }

  @Test
  public void testDod520001MetacardType() {
    List<MetacardType> metacardTypeList = new ArrayList<>();
    metacardTypeList.add(DOD_520001_ATTRIBUTES);
    MetacardType metacardType = new MetacardTypeImpl(TEST_NAME, metacardTypeList);
    assertMetacardAttributes(metacardType, DOD_520001_ATTRIBUTES.getAttributeDescriptors());
    assertThat(DOD_520001_ATTRIBUTES.getName(), is("dod520001"));
    assertThat(
        DOD_520001_ATTRIBUTES.getAttributeDescriptor(Dod520001.SECURITY_DOD5200_AEA),
        notNullValue());
  }

  @Test
  public void testAllTypes() {
    List<MetacardType> metacardTypeList = new ArrayList<>();
    metacardTypeList.add(ISR_ATTRIBUTES);
    metacardTypeList.add(SECURITY_ATTRIBUTES);
    metacardTypeList.add(DOD_520001_ATTRIBUTES);

    MetacardType metacardType = new MetacardTypeImpl(TEST_NAME, metacardTypeList);
    assertMetacardAttributes(metacardType, CORE_ATTRIBUTES.getAttributeDescriptors());
    assertMetacardAttributes(metacardType, ISR_ATTRIBUTES.getAttributeDescriptors());
    assertMetacardAttributes(metacardType, SECURITY_ATTRIBUTES.getAttributeDescriptors());
    assertMetacardAttributes(metacardType, DOD_520001_ATTRIBUTES.getAttributeDescriptors());
  }

  private void assertMetacardAttributes(
      MetacardType metacardType, Set<AttributeDescriptor> expected) {
    Set<AttributeDescriptor> actual = metacardType.getAttributeDescriptors();
    assertThat(metacardType.getName(), is(TEST_NAME));
    for (AttributeDescriptor attributeDescriptor : expected) {
      assertThat(actual, hasItem(attributeDescriptor));
    }
  }
}
