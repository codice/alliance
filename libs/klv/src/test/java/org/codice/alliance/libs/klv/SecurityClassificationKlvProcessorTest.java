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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.alliance.catalog.core.internal.api.classification.SecurityClassificationService;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SecurityClassificationKlvProcessorTest {

  private static final String UNCLASSIFIED = "unclassified";

  private static final String RESTRICTED = "restricted";

  private static final String DEFAULT = "default";

  private Map<Short, String> codes;

  private SecurityClassificationService securityClassificationService;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    codes = new HashMap<>();
    codes.put((short) 1, UNCLASSIFIED);
    codes.put((short) 2, RESTRICTED);
    securityClassificationService = mock(SecurityClassificationService.class);
    Comparator<String> comparator = mock(Comparator.class);
    when(comparator.compare(UNCLASSIFIED, UNCLASSIFIED)).thenReturn(0);
    when(comparator.compare(UNCLASSIFIED, RESTRICTED)).thenReturn(-1);
    when(securityClassificationService.getSecurityClassificationComparator())
        .thenReturn(comparator);
  }

  @Test
  public void testSingleClassificationCode() {

    Short id1 = 1;

    ArgumentCaptor<Attribute> argumentCaptor = callTest(Collections.singletonList(id1));

    assertAttribute(argumentCaptor, UNCLASSIFIED);
  }

  @Test
  public void testMultipleIdenticalClassificationCodes() {

    Short id1 = 1;
    Short id2 = 1;

    ArgumentCaptor<Attribute> argumentCaptor = callTest(Arrays.asList(id1, id2));

    assertAttribute(argumentCaptor, UNCLASSIFIED);
  }

  /**
   * Make sure that when multiple classification codes are found, that the code with the highest
   * value is selected.
   */
  @Test
  public void testMultipleDifferentClassificationCodes() {

    Short id1 = 1;
    Short id2 = 2;

    ArgumentCaptor<Attribute> argumentCaptor = callTest(Arrays.asList(id1, id2));

    assertAttribute(argumentCaptor, RESTRICTED);
  }

  @Test
  public void testDefaultClassificationCodes() {

    Short id1 = 999;

    ArgumentCaptor<Attribute> argumentCaptor = callTest(Collections.singletonList(id1));

    assertAttribute(argumentCaptor, DEFAULT);
  }

  private void assertAttribute(ArgumentCaptor<Attribute> argumentCaptor, String value) {
    assertThat(
        argumentCaptor.getValue().getName(), is(AttributeNameConstants.SECURITY_CLASSIFICATION));
    assertThat(argumentCaptor.getValue().getValues(), is(Collections.singletonList(value)));
  }

  private ArgumentCaptor<Attribute> callTest(List<Serializable> codeList) {
    return KlvUtilities.testKlvProcessor(
        new SecurityClassificationKlvProcessor(securityClassificationService, codes, DEFAULT),
        Stanag4609TransportStreamParser.SECURITY_CLASSIFICATION,
        codeList);
  }
}
