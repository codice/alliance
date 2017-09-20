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
package org.codice.alliance.video.stream.mpegts.metacard;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import java.util.Optional;
import org.codice.alliance.libs.klv.AttributeNameConstants;
import org.codice.alliance.video.stream.mpegts.Context;

public class ModifiedDateMetacardUpdater implements MetacardUpdater {
  @Override
  public void update(Metacard parent, Metacard child, Context context) {
    Optional.ofNullable(child.getAttribute(AttributeNameConstants.TEMPORAL_END))
        .map(Attribute::getValue)
        .ifPresent(value -> parent.setAttribute(new AttributeImpl(Core.MODIFIED, value)));
  }

  @Override
  public String toString() {
    return "ModifiedDateMetacardUpdater{}";
  }
}
