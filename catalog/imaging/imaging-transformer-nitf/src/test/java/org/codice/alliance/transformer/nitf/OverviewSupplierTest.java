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
package org.codice.alliance.transformer.nitf;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

public class OverviewSupplierTest {

  private OverviewSupplier supplier;

  private class IsMetacardWithDerivedOverviewResource extends ArgumentMatcher<Metacard> {
    private final OverviewPredicate predicate = new OverviewPredicate();

    @Override
    public boolean matches(Object o) {
      return predicate.test((Metacard) o);
    }
  }

  private IsMetacardWithDerivedOverviewResource isMetacardWithDerivedOverviewResource() {
    return new IsMetacardWithDerivedOverviewResource();
  }

  @Before
  public void setUp() throws CatalogTransformerException {
    final BinaryContent overviewContent = mock(BinaryContent.class);
    doAnswer(invocationOnMock -> getClass().getClassLoader().getResourceAsStream("flower.jpg"))
        .when(overviewContent)
        .getInputStream();

    final MetacardTransformer resourceMetacardTransformer = mock(MetacardTransformer.class);
    doReturn(overviewContent)
        .when(resourceMetacardTransformer)
        .transform(
            argThat(isMetacardWithDerivedOverviewResource()),
            eq(Collections.singletonMap(ContentItem.QUALIFIER_KEYWORD, "overview")));
    doThrow(CatalogTransformerException.class)
        .when(resourceMetacardTransformer)
        .transform(
            argThat(not(isMetacardWithDerivedOverviewResource())),
            eq(Collections.singletonMap(ContentItem.QUALIFIER_KEYWORD, "overview")));

    supplier = new OverviewSupplier(resourceMetacardTransformer);
  }

  @Test
  public void testOverview() {
    final Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Core.DERIVED_RESOURCE_URI, "content:abc123#overview"));
    assertThat(supplier.apply(metacard, null).isPresent(), is(true));
  }

  @Test
  public void testNoOverview() {
    assertThat(supplier.apply(new MetacardImpl(), null).isPresent(), is(false));
  }
}
