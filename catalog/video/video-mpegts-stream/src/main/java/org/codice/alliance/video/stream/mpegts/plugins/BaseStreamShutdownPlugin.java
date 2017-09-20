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
package org.codice.alliance.video.stream.mpegts.plugins;

import static org.apache.commons.lang3.Validate.notNull;

import org.codice.alliance.video.stream.mpegts.Context;

public abstract class BaseStreamShutdownPlugin implements StreamShutdownPlugin {
  @Override
  public final void onShutdown(Context context) throws StreamShutdownException {
    notNull(context, "context must be non-null");
    doOnShutdown(context);
  }

  protected abstract void doOnShutdown(Context context) throws StreamShutdownException;
}
