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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import java.util.List;
import java.util.function.Function;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Security;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SegmentHandlerTest {

  @Test
  public void testCountryCodeConversion() {
    SegmentHandler handler = new SegmentHandler();

    Metacard mockMetacard = mock(Metacard.class);
    ArgumentCaptor<Attribute> attrCaptor = ArgumentCaptor.forClass(Attribute.class);

    Function mockAccessor = mock(Function.class);
    doReturn("us fr cn").when(mockAccessor).apply(anyObject());

    NitfAttribute mockNitfAttr = mock(NitfAttribute.class);
    doReturn(mockAccessor).when(mockNitfAttr).getAccessorFunction();
    doReturn(
            ImmutableSet.of(
                (new SecurityAttributes()).getAttributeDescriptor(Security.OWNER_PRODUCER)))
        .when(mockNitfAttr)
        .getAttributeDescriptors();

    handler.handleSegmentHeader(mockMetacard, null, ImmutableList.of(mockNitfAttr));
    verify(mockMetacard).setAttribute(attrCaptor.capture());

    List<String> expectedCC = ImmutableList.of("USA", "FRA", "CHN");
    for (Attribute attr : attrCaptor.getAllValues()) {
      assertThat(
          "Not all digraphs were properly converted.",
          expectedCC.contains(attr.getValue()),
          is(true));
    }
  }
}
