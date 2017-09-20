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
package org.codice.alliance.nsili.common;

public enum PackagingSpecFormatType {
  TARUNC(".tar", "application/x-tar"),
  FILESUNC("", ""),
  TARZIP(".tar.zip", "application/x-zip"),
  FILESZIP(".zip", "application/x-zip"),
  TARGZIP(".tar.gz", "application/x-gzip"),
  FILESGZIP(".gz", "application/x-gzip"),
  TARCOMPRESS(".tar.Z", "application/x-zip"),
  FILESCOMPRESS(".Z", "application/x-zip");

  private String extension;
  private String contentType;

  PackagingSpecFormatType(String extension, String contentType) {
    this.extension = extension;
    this.contentType = contentType;
  }

  public String getExtension() {
    return extension;
  }

  public String getContentType() {
    return contentType;
  }
}
