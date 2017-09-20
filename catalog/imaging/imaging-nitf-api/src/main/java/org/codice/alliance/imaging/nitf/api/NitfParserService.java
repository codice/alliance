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
package org.codice.alliance.imaging.nitf.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.fluent.NitfSegmentsFlow;

public interface NitfParserService {

  /**
   * @param inputStream an InputStream containing the NITF to be parsed.
   * @param allData if 'true', then image data will be included in the parsed result. If 'false',
   *     then that data will be skipped, saving heap space.
   * @return a NitfSegmentsFlow object containing the parsed NITF data.
   * @throws NitfFormatException when 'inputStream' can't be successfully parsed.
   */
  NitfSegmentsFlow parseNitf(InputStream inputStream, Boolean allData) throws NitfFormatException;

  /**
   * @param nitfFile a file handle to the NITF to be parsed.
   * @param allData if 'true', then image data will be included in the parsed result. If 'false',
   *     then that data will be skipped, saving heap space.
   * @return a NitfSegmentsFlow object containing the parsed NITF data.
   * @throws FileNotFoundException when 'nitfFile' doesn't exist.
   * @throws NitfFormatException when 'nitfFile' can't be successfully parsed.
   */
  NitfSegmentsFlow parseNitf(File nitfFile, Boolean allData)
      throws NitfFormatException, FileNotFoundException;

  /**
   * @param nitfSegmentsFlow - the NitfSegmentsFlow object to end. This method call will delete any
   *     temp files created by this route.
   */
  void endNitfSegmentsFlow(NitfSegmentsFlow nitfSegmentsFlow);
}
