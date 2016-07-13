/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.security.token.videographer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codice.alliance.security.principal.videographer.VideographerPrincipal;
import org.junit.Test;

public class TestVideographerAuthenticationToken {

    @Test
    public void testConstructor() {
        final String realm = "someRealm";
        VideographerAuthenticationToken token = new VideographerAuthenticationToken(realm,
                "127.0.0.1");
        assertTrue(token.getPrincipal() instanceof VideographerPrincipal);
        assertEquals(VideographerAuthenticationToken.VIDEOGRAPHER_CREDENTIALS,
                token.getCredentials());
        assertEquals(realm, token.getRealm());
        assertEquals(VideographerAuthenticationToken.VIDEOGRAPHER_TOKEN_VALUE_TYPE,
                token.getTokenValueType());
        assertEquals(VideographerAuthenticationToken.BST_VIDEOGRAPHER_LN, token.getTokenId());
        assertEquals(token.getIpAddress(), "127.0.0.1");
    }
}
