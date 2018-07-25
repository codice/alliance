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

import java.util.List;
import org.codice.imaging.nitf.core.header.NitfHeader;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreCollection;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

public class RoutingSlip {

  public static final String GMTI_ROUTE = "direct://gmti";

  public static final String IMAGE_ROUTE = "direct://image";

  static final String MTIRPB = "MTIRPB";

  public String channel(NitfSegmentsFlow nitfSegmentsFlow) {
    if (nitfSegmentsFlow == null) {
      throw new IllegalArgumentException("method argument 'nitfSegmentsFlow' may not be null.");
    }

    final ThreadLocal<Boolean> tresExist = new ThreadLocal<>();
    tresExist.set(false);

    final ThreadLocal<Boolean> imagesExist = new ThreadLocal<>();
    imagesExist.set(false);

    nitfSegmentsFlow
        .fileHeader(header -> tresExist.set(tresExist(header)))
        .forEachImageSegment(header -> imagesExist.set(true));

    if (!imagesExist.get() && tresExist.get()) {
      return GMTI_ROUTE;
    }

    return IMAGE_ROUTE;
  }

  private Boolean tresExist(NitfHeader header) {
    TreCollection treCollection = header.getTREsRawStructure();
    List<Tre> mtirpbList = treCollection.getTREsWithName(MTIRPB);
    return !mtirpbList.isEmpty();
  }
}
