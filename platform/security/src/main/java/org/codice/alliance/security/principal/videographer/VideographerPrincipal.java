package org.codice.alliance.security.principal.videographer;

import java.io.Serializable;
import java.security.Principal;

import org.apache.commons.lang.StringUtils;

public class VideographerPrincipal implements Principal, Serializable {

    public static final String VIDEOGRAPHER_NAME_PREFIX = "Videographer";

    public static final String NAME_DELIMITER = "@";

    private static final long serialVersionUID = -4630425142287155229L;

    private String name;

    public VideographerPrincipal(String address) {
        this.name = VIDEOGRAPHER_NAME_PREFIX + NAME_DELIMITER + address;
    }

    /**
     * Parses the ip address out of a videographer principal name that has the format
     * Videographer@127.0.0.1
     *
     * @param fullName full name (e.g. Videographer@127.0.0.1)
     * @return ip address
     */
    public static String parseAddressFromName(String fullName) {
        if (!StringUtils.isEmpty(fullName)) {
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
        return parseAddressFromName(name);
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
