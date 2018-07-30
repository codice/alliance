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
import static org.junit.Assert.assertThat;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import org.junit.Test;

public class OverviewPredicateTest {

  private final OverviewPredicate predicate = new OverviewPredicate();

  @Test
  public void testOverview() {
    final Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Core.DERIVED_RESOURCE_URI, "content:abc123#overview"));
    assertThat(predicate.test(metacard), is(true));
  }

  @Test
  public void testNoDerivedResourceUris() {
    assertThat(predicate.test(new MetacardImpl()), is(false));
  }

  @Test
  public void testNoOverview() {
    final Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Core.DERIVED_RESOURCE_URI, "content:abc123"));
    assertThat(predicate.test(metacard), is(false));
  }
}
