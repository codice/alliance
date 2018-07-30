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

import ddf.catalog.data.Metacard;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

public class NitfHeaderTransformer extends SegmentHandler {

  private static final String NULL_ARGUMENT_MESSAGE = "Cannot transform null input.";

  public NitfSegmentsFlow transform(NitfSegmentsFlow nitfSegmentsFlow, Metacard metacard) {
    if (nitfSegmentsFlow == null) {
      throw new IllegalArgumentException(NULL_ARGUMENT_MESSAGE);
    }

    nitfSegmentsFlow.fileHeader(header -> handleNitfHeader(metacard, header));
    return nitfSegmentsFlow;
  }

  private void handleNitfHeader(Metacard metacard, NitfHeader header) {
    handleSegmentHeader(metacard, header, NitfHeaderAttribute.getAttributes());
    handleTres(metacard, header);
  }
}
