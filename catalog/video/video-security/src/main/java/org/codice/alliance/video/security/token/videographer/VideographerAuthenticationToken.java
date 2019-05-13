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
package org.codice.alliance.video.security.token.videographer;

import ddf.security.common.audit.SecurityLogger;
import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.video.security.principal.videographer.VideographerPrincipal;
import org.codice.ddf.security.handler.api.BSTAuthenticationToken;

public class VideographerAuthenticationToken extends BSTAuthenticationToken {

  public static final String VIDEOGRAPHER_CREDENTIALS = "Videographer";

  public static final String BST_VIDEOGRAPHER_LN = "Videographer";

  public static final String VIDEOGRAPHER_TOKEN_VALUE_TYPE =
      BSTAuthenticationToken.BST_NS
          + BSTAuthenticationToken.TOKEN_VALUE_SEPARATOR
          + BST_VIDEOGRAPHER_LN;

  public VideographerAuthenticationToken(String ip) {
    super(new VideographerPrincipal(ip), VIDEOGRAPHER_CREDENTIALS);
    setTokenValueType(BSTAuthenticationToken.BST_NS, BST_VIDEOGRAPHER_LN);
    setTokenId(BST_VIDEOGRAPHER_LN);

    if (StringUtils.isNotEmpty(ip)) {
      SecurityLogger.audit("Videographer token generated for IP address: " + ip);
    }
  }

  public String getTokenValueType() {
    return tokenValueType;
  }

  public String getTokenId() {
    return tokenId;
  }

  public String getIpAddress() {
    String ip = null;
    if (principal instanceof VideographerPrincipal) {
      ip = ((VideographerPrincipal) principal).getAddress();
    } else if (principal instanceof String) {
      ip = VideographerPrincipal.parseAddressFromName((String) principal);
    }
    return ip;
  }

  @Override
  public String toString() {
    return "Videographer IP: " + getIpAddress();
  }
}
