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
import java.util.Collections;
import java.util.List;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

public class SensorAltitudeKlvProcessor extends AbstractMultiKlvProcessor {
  public SensorAltitudeKlvProcessor() {
    super(Collections.singleton(Stanag4609TransportStreamParser.SENSOR_TRUE_ALTITUDE));
  }

  @Override
  protected void doProcess(List<Attribute> attributes, Metacard metacard) {
    attributes.stream()
        .flatMap(attribute -> attribute.getValues().stream())
        .filter(Double.class::isInstance)
        .mapToDouble(Double.class::cast)
        .average()
        .ifPresent(doubleValue -> setMetacard(doubleValue, metacard));
  }

  private void setMetacard(double value, Metacard metacard) {
    metacard.setAttribute(new AttributeImpl(AttributeNameConstants.SENSOR_TRUE_ALTITUDE, value));
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
