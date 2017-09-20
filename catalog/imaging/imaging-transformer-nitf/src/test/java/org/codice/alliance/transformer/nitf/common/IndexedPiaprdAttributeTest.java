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
package org.codice.alliance.transformer.nitf.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.tre.Tre;
import org.junit.Before;
import org.junit.Test;

public class IndexedPiaprdAttributeTest {

  private static final String KEYWORD =
      "FIRST                                                   "
          + "                                                                                    "
          + "                                                                                    "
          + "                          -END-";

  private Tre tre;

  @Before
  public void setUp() {
    tre = mock(Tre.class);
  }

  @Test
  public void testIndexedPiaprdAttribute() {
    IndexedPiaprdAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getShortName(), notNullValue()));
    IndexedPiaprdAttribute.getAttributes()
        .forEach(attribute -> assertThat(attribute.getLongName(), notNullValue()));
  }

  @Test
  public void testKeyword() throws NitfFormatException {
    when(tre.getFieldValue(IndexedPiaprdAttribute.KEYWORD_ATTRIBUTE.getShortName()))
        .thenReturn(KEYWORD);

    String keyword =
        (String) IndexedPiaprdAttribute.KEYWORD_ATTRIBUTE.getAccessorFunction().apply(tre);

    assertThat(keyword, is(KEYWORD));
  }
}
