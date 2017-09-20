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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.MetacardImpl;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class CopyPresentKlvProcessorTest {

  private CopyPresentKlvProcessor copyPresentKlvProcessor;

  @Before
  public void setup() {
    copyPresentKlvProcessor = new CopyPresentKlvProcessor();
  }

  @Test
  public void testProcess() {

    String name = "point-of-contact";
    String value = "John Doe";

    Attribute attribute = new AttributeImpl(name, value);

    KlvHandler klvHandler = mock(KlvHandler.class);

    when(klvHandler.asAttribute()).thenReturn(Optional.of(attribute));

    Map<String, KlvHandler> handlers = Collections.singletonMap("someStanagFieldName", klvHandler);

    MetacardImpl metacard = new MetacardImpl(BasicTypes.BASIC_METACARD);

    KlvProcessor.Configuration klvConfiguration = new KlvProcessor.Configuration();

    copyPresentKlvProcessor.process(handlers, metacard, klvConfiguration);

    assertThat(metacard.getAttribute(name).getValue(), is(value));
  }

  @Test
  public void testAccept() {
    KlvProcessor.Visitor visitor = mock(KlvProcessor.Visitor.class);
    copyPresentKlvProcessor.accept(visitor);
    verify(visitor).visit(copyPresentKlvProcessor);
  }
}
