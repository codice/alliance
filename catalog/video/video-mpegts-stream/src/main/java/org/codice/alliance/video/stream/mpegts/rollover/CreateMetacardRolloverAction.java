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
package org.codice.alliance.video.stream.mpegts.rollover;

import ddf.catalog.data.MetacardCreationException;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import java.io.File;
import java.util.List;
import org.codice.alliance.video.stream.mpegts.Constants;

public class CreateMetacardRolloverAction extends BaseRolloverAction {

  private List<MetacardType> metacardTypeList;

  public CreateMetacardRolloverAction(List<MetacardType> metacardTypeList) {
    this.metacardTypeList = metacardTypeList;
  }

  @Override
  public MetacardImpl doAction(MetacardImpl metacard, File tempFile)
      throws RolloverActionException {

    MetacardImpl newMetacard;
    try {
      newMetacard = new MetacardImpl(findMetacardType());
    } catch (MetacardCreationException e) {
      throw new RolloverActionException(
          String.format("unable to create metacard: tempFile=%s", tempFile), e);
    }
    newMetacard.setContentTypeName(Constants.MPEGTS_MIME_TYPE);

    return newMetacard;
  }

  private MetacardType findMetacardType() throws MetacardCreationException {
    return metacardTypeList.stream()
        .findFirst()
        .orElseThrow(() -> new MetacardCreationException("no metacard type found!"));
  }

  @Override
  public String toString() {
    return "CreateMetacardRolloverAction{" + "metacardTypeList=" + metacardTypeList + '}';
  }
}
