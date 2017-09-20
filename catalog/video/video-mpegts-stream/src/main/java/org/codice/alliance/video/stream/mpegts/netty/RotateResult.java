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
package org.codice.alliance.video.stream.mpegts.netty;

import java.io.File;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * This is a POJO that contains the result of a file rotation request. If a rotation occurred, then
 * {@link #getFile()} will return an {@link Optional} that contains the {@link File} for the rotated
 * file. If a rotation occurred because of a timeout, then {@link #isTimeout()} will return TRUE.
 */
public class RotateResult {

  private final File file;

  private final boolean isTimeout;

  /**
   * @param file the rotated file, may be null
   * @param isTimeout true if rotation occurred because of timeout
   */
  public RotateResult(@Nullable File file, boolean isTimeout) {
    this.file = file;
    this.isTimeout = isTimeout;
  }

  public Optional<File> getFile() {
    return Optional.ofNullable(file);
  }

  public boolean isTimeout() {
    return isTimeout;
  }
}
