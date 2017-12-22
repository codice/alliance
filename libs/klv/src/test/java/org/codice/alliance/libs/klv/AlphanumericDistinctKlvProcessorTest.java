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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import java.io.Serializable;
import java.util.List;
import org.junit.Test;

public class AlphanumericDistinctKlvProcessorTest {
  public static final String ATTRIBUTE_NAME = "title";

  @Test
  public void doProcessWithAlphanumericValue() {
    Metacard metacard = setupAndExecuteDoProcess(ImmutableList.of("testvalue"));

    assertThat(metacard.getAttribute(ATTRIBUTE_NAME), is(not(nullValue())));
    assertThat(metacard.getAttribute(ATTRIBUTE_NAME).getValue().toString(), equalTo("testvalue"));
  }

  @Test
  public void doProcessWithNonalphanumericValues() {
    Metacard metacard = setupAndExecuteDoProcess(ImmutableList.of("a-fasd", "//"));

    assertThat(metacard.getAttribute(ATTRIBUTE_NAME), is(nullValue()));
  }

  @Test
  public void doProcessWithCombination() {
    Metacard metacard = setupAndExecuteDoProcess(ImmutableList.of("testvalue", "//"));

    assertThat(metacard.getAttribute(ATTRIBUTE_NAME), is(not(nullValue())));
    assertThat(metacard.getAttribute(ATTRIBUTE_NAME).getValue().toString(), equalTo("testvalue"));
  }

  private Metacard setupAndExecuteDoProcess(List<Serializable> values) {
    AlphanumericDistinctKlvProcessor alphanumericDistinctKlvProcessor =
        new AlphanumericDistinctKlvProcessor(ATTRIBUTE_NAME, "b");

    Attribute mockAttribute = mock(Attribute.class);
    when(mockAttribute.getValues()).thenReturn(values);

    Metacard metacard = new MetacardImpl();

    alphanumericDistinctKlvProcessor.doProcess(mockAttribute, metacard);

    return metacard;
  }
}
