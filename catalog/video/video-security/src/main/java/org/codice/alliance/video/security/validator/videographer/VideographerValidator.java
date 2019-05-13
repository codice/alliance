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
package org.codice.alliance.video.security.validator.videographer;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.token.validator.TokenValidator;
import org.apache.cxf.sts.token.validator.TokenValidatorParameters;
import org.apache.cxf.sts.token.validator.TokenValidatorResponse;
import org.apache.cxf.ws.security.sts.provider.model.secext.BinarySecurityTokenType;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.codice.alliance.video.security.principal.videographer.VideographerPrincipal;
import org.codice.alliance.video.security.token.videographer.VideographerAuthenticationToken;
import org.codice.ddf.security.handler.api.BaseAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideographerValidator implements TokenValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(VideographerValidator.class);

  private static final String WILDCARD = "*";

  private VideographerAuthenticationToken getVideographerTokenFromTarget(
      ReceivedToken validateTarget) {

    LOGGER.debug("get videographer token from target: {}", validateTarget);

    Object token = validateTarget.getToken();
    if ((token instanceof BinarySecurityTokenType)
        && VideographerAuthenticationToken.VIDEOGRAPHER_TOKEN_VALUE_TYPE.equals(
            ((BinarySecurityTokenType) token).getValueType())) {
      String credential = ((BinarySecurityTokenType) token).getValue();
      try {
        BaseAuthenticationToken base = VideographerAuthenticationToken.parse(credential, true);
        return new VideographerAuthenticationToken(
            VideographerPrincipal.parseAddressFromName(base.getPrincipal().toString()));
      } catch (WSSecurityException e) {
        LOGGER.debug(
            "Unable to parse {} from encodedToken.",
            VideographerAuthenticationToken.class.getSimpleName(),
            e);
      }
    }
    return null;
  }

  private boolean validIpAddress(String address) {
    String processedAddress = address.split("%")[0];
    LOGGER.debug("checking address: {}", address);
    return InetAddressValidator.getInstance().isValid(processedAddress);
  }

  @Override
  public boolean canHandleToken(ReceivedToken validateTarget) {

    return canHandleToken(validateTarget, null);
  }

  @Override
  public boolean canHandleToken(ReceivedToken validateTarget, String realm) {
    VideographerAuthenticationToken videographerToken =
        getVideographerTokenFromTarget(validateTarget);
    // currently realm is not being passed through (no RealmParser that determines the realm
    // based on the web context. So this just looks at the realm passed in the credentials.
    // This generic instance just looks for the default realms (DDF and Karaf)
    if (videographerToken != null) {
      LOGGER.trace("canHandletoken = true");
      return true;
    } else {
      LOGGER.trace("canHandleToken = false");
      return false;
    }
  }

  @Override
  public TokenValidatorResponse validateToken(TokenValidatorParameters tokenParameters) {
    TokenValidatorResponse response = new TokenValidatorResponse();
    ReceivedToken validateTarget = tokenParameters.getToken();
    validateTarget.setState(ReceivedToken.STATE.INVALID);

    VideographerAuthenticationToken videographerToken =
        getVideographerTokenFromTarget(validateTarget);

    response.setToken(validateTarget);

    if (videographerToken != null) {
      response.setPrincipal(new VideographerPrincipal(videographerToken.getIpAddress()));

      if (videographerToken
              .getCredentials()
              .equals(VideographerAuthenticationToken.VIDEOGRAPHER_CREDENTIALS)
          && validIpAddress(videographerToken.getIpAddress())) {
        validateTarget.setState(ReceivedToken.STATE.VALID);
        validateTarget.setPrincipal(new VideographerPrincipal(videographerToken.getIpAddress()));
      }
    }
    return response;
  }
}
