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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.MetacardImpl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class SetDatesKlvProcessorTest {

  private SetDatesKlvProcessor setDatesKlvProcessor;

  @Before
  public void setup() {
    setDatesKlvProcessor = new SetDatesKlvProcessor();
  }

  @Test
  public void testProcess() throws ParseException {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date firstDate = simpleDateFormat.parse("2016-04-01 02:00:00");
    Date secondDate = simpleDateFormat.parse("2016-04-01 03:00:00");
    Date thirdDate = simpleDateFormat.parse("2016-04-01 04:00:00");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getValues()).thenReturn(Arrays.asList(firstDate, secondDate, thirdDate));
    KlvHandler klvHandler = mock(KlvHandler.class);
    when(klvHandler.asAttribute()).thenReturn(Optional.of(attribute));

    Map<String, KlvHandler> handlers =
        Collections.singletonMap(AttributeNameConstants.TIMESTAMP, klvHandler);

    MetacardImpl metacard = new MetacardImpl(BasicTypes.BASIC_METACARD);

    setDatesKlvProcessor.process(handlers, metacard, new KlvProcessor.Configuration());

    assertThat(metacard.getCreatedDate(), is(firstDate));

    assertThat(
        metacard.getAttribute(AttributeNameConstants.TEMPORAL_START).getValue(), is(firstDate));
    assertThat(
        metacard.getAttribute(AttributeNameConstants.TEMPORAL_END).getValue(), is(thirdDate));
  }

  @Test
  public void testAccept() {
    KlvProcessor.Visitor visitor = mock(KlvProcessor.Visitor.class);
    setDatesKlvProcessor.accept(visitor);
    verify(visitor).visit(setDatesKlvProcessor);
  }
}
