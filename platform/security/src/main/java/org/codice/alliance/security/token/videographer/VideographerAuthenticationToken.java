package org.codice.alliance.security.token.videographer;

import org.apache.commons.lang3.StringUtils;
import org.codice.alliance.security.principal.videographer.VideographerPrincipal;
import org.codice.ddf.security.handler.api.BSTAuthenticationToken;

import ddf.security.common.audit.SecurityLogger;

public class VideographerAuthenticationToken extends BSTAuthenticationToken {

    public static final String VIDEOGRAPHER_CREDENTIALS = "Videographer";

    public static final String BST_VIDEOGRAPHER_LN = "Videographer";

    public static final String VIDEOGRAPHER_TOKEN_VALUE_TYPE =
            BSTAuthenticationToken.BST_NS + BSTAuthenticationToken.TOKEN_VALUE_SEPARATOR
                    + BST_VIDEOGRAPHER_LN;

    public VideographerAuthenticationToken(String realm, String ip) {
        super(new VideographerPrincipal(ip), VIDEOGRAPHER_CREDENTIALS, realm);
        setTokenValueType(BSTAuthenticationToken.BST_NS, BST_VIDEOGRAPHER_LN);
        setTokenId(BST_VIDEOGRAPHER_LN);

        if (!StringUtils.isEmpty(ip)) {
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
        return "Videographer IP: " +
                getIpAddress() +
                "; realm: " +
                realm;
    }
}
