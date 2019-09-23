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

import static org.codice.alliance.libs.klv.Utilities.safelySetAttribute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import java.util.Arrays;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/** Check the behavior of helper functions in {@link Utilities}. */
@RunWith(MockitoJUnitRunner.class)
public class UtilitiesTest {
  private static final String ATTRIBUTE_KEY_POC = "point-of-contact";

  private static final String ATTRIBUTE_KEY_TAG = "metacard-tags";

  private static final String EXPECTED_POC = "John Doe";

  private MetacardImpl metacard;

  @Before
  public void setup() throws Exception {
    metacard = new MetacardImpl();
  }

  @Test
  public void testSafelySetCorrectAttribute() throws Exception {
    safelySetAttribute(metacard, ATTRIBUTE_KEY_POC, EXPECTED_POC);
    assertThat(metacard.getPointOfContact(), is(EXPECTED_POC));
  }

  @Test
  public void testSafelySetIncorrectAttribute() throws Exception {
    safelySetAttribute(metacard, ATTRIBUTE_KEY_POC, 899);
    assertThat(metacard.getPointOfContact(), is(nullValue()));
  }

  @Test
  public void testSafelySetCorrectMultiValuedAttribute() throws Exception {
    safelySetAttribute(metacard, ATTRIBUTE_KEY_TAG, Arrays.asList("Tag-A", "Tag-B"));
    Set<String> tags = metacard.getTags();
    assertThat(tags, is(notNullValue()));
    assertThat(tags, containsInAnyOrder("Tag-A", "Tag-B"));
  }

  @Test
  public void testSafelySetIncorrectMultiValuedAttribute() throws Exception {
    safelySetAttribute(metacard, ATTRIBUTE_KEY_TAG, Arrays.asList("Tag-A", 899));
    assertThat(metacard.getTags(), is(empty()));
  }

  @Test
  public void testSafelySetCorrectlyUsingAttribute() throws Exception {
    AttributeImpl attribute = new AttributeImpl(ATTRIBUTE_KEY_POC, EXPECTED_POC);
    safelySetAttribute(metacard, attribute);
    assertThat(metacard.getPointOfContact(), is(EXPECTED_POC));
  }

  @Test
  public void testSafelySetIncorrectlyUsingAttribute() throws Exception {
    AttributeImpl attribute = new AttributeImpl(ATTRIBUTE_KEY_POC, 899);
    safelySetAttribute(metacard, attribute);
    assertThat(metacard.getPointOfContact(), is(nullValue()));
  }

  @Test
  public void testIsBlankString() throws Exception {
    assertThat(Utilities.isBlankString(" "), is(true));
    assertThat(Utilities.isNotBlankString(" "), is(false));
  }

  @Test
  public void testIsNotNoneString() throws Exception {
    assertThat(Utilities.isNotStringNone(" "), is(true));
    assertThat(Utilities.isNotStringNone("non"), is(true));
    assertThat(Utilities.isNotStringNone("none"), is(false));
    assertThat(Utilities.isNotStringNone("None"), is(false));
    assertThat(Utilities.isNotStringNone("NONE"), is(false));
    assertThat(Utilities.isNotStringNone("None "), is(false));
  }

  @Test
  public void testNullDescriptorDoesNotSetAttribute() throws Exception {
    AttributeImpl attribute = new AttributeImpl(ATTRIBUTE_KEY_POC, EXPECTED_POC);
    Metacard mockedMetacard = setupMetacardWithNullDescriptor();
    safelySetAttribute(mockedMetacard, attribute);
    assertThat(metacard.getPointOfContact(), is(nullValue()));
  }

  private Metacard setupMetacardWithNullDescriptor() throws Exception {
    Metacard metacard = mock(Metacard.class);
    MetacardType type = mock(MetacardType.class);

    when(metacard.getMetacardType()).thenReturn(type);
    when(type.getAttributeDescriptor(anyString())).thenReturn(null);

    return metacard;
  }
}
