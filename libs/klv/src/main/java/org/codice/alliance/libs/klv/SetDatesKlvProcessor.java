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

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

/**
 * Set the "temporal.start", "temporal.end" and "created" metacard attributes based on the klv
 * metadata timestamp entries.
 */
public class SetDatesKlvProcessor extends SingleFieldKlvProcessor {

  public SetDatesKlvProcessor() {
    super(Stanag4609TransportStreamParser.TIMESTAMP);
  }

  @Override
  protected void doProcess(Attribute attribute, Metacard metacard) {
    List<Date> values =
        attribute.getValues().stream()
            .filter(serializable -> serializable instanceof Date)
            .map(s -> (Date) s)
            .collect(Collectors.toList());

    if (!values.isEmpty()) {
      Date firstDate = values.get(0);
      Date lastDate = values.get(values.size() - 1);
      metacard.setAttribute(new AttributeImpl(AttributeNameConstants.TEMPORAL_START, firstDate));
      metacard.setAttribute(new AttributeImpl(AttributeNameConstants.TEMPORAL_END, lastDate));
      metacard.setAttribute(new AttributeImpl(AttributeNameConstants.CREATED, firstDate));
      metacard.setAttribute(new AttributeImpl(Core.MODIFIED, lastDate));
    }
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
