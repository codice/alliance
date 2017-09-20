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
package org.codice.alliance.video.stream.mpegts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/** Factory for create OutputStream objects. */
public interface OutputStreamFactory {

  /**
   * Create an OutputStream that writes to a file and supports appending.
   *
   * @param file must be non-null
   * @param append must be non-null
   * @return a non-null value
   * @throws FileNotFoundException
   */
  OutputStream create(File file, boolean append) throws FileNotFoundException;
}
