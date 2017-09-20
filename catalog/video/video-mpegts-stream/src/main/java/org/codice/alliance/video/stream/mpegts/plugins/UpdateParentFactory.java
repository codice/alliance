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

/** Build a {@link UpdateParent}. */
public class UpdateParentFactory implements FindChildrenStreamEndPlugin.Factory {

  private final Factory factory;

  /** @param factory must be not-null, the */
  public UpdateParentFactory(Factory factory) {
    this.factory = factory;
  }

  public Factory getFactory() {
    return factory;
  }

  @Override
  public FindChildrenStreamEndPlugin.Handler build() {
    return new UpdateParent(factory.build());
  }

  /** Build a {@link UpdateParent.UpdateField}. */
  public interface Factory {
    UpdateParent.UpdateField build();
  }
}
