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
package org.codice.alliance.video.security.videographer.principal;

import java.io.Serializable;
import java.security.Principal;
import org.apache.commons.lang.StringUtils;

public class VideographerPrincipal implements Principal, Serializable {

  private static final String VIDEOGRAPHER_NAME_PREFIX = "Videographer";

  private static final String NAME_DELIMITER = "@";

  private static final long serialVersionUID = -4630425142287155229L;

  private String name;

  private String address;

  public VideographerPrincipal(String address) {
    this.name = VIDEOGRAPHER_NAME_PREFIX + NAME_DELIMITER + address;
    this.address = address;
  }

  /**
   * Parses the ip address out of a videographer principal name that has the format
   * Videographer@127.0.0.1
   *
   * @param fullName full name (e.g. Videographer@127.0.0.1)
   * @return ip address
   */
  public static String parseAddressFromName(String fullName) {
    if (StringUtils.isNotEmpty(fullName)) {
      String[] parts = fullName.split(NAME_DELIMITER);
      if (parts.length == 2) {
        return parts[1];
      }
    }
    return null;
  }

  /**
   * Returns the ip address associated with this videographer principal
   *
   * @return ip address
   */
  public String getAddress() {
    return address;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
