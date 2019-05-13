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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.codice.alliance.video.security.principal.videographer.VideographerPrincipal;
import org.junit.Test;

public class VideographerAuthenticationTokenTest {

  @Test
  public void testConstructor() {
    VideographerAuthenticationToken token = new VideographerAuthenticationToken("127.0.0.1");
    assertThat(token.getPrincipal(), is(instanceOf(VideographerPrincipal.class)));
    assertThat(
        token.getCredentials(), is(VideographerAuthenticationToken.VIDEOGRAPHER_CREDENTIALS));
    assertThat(
        token.getTokenValueType(),
        is(VideographerAuthenticationToken.VIDEOGRAPHER_TOKEN_VALUE_TYPE));
    assertThat(token.getTokenId(), is(VideographerAuthenticationToken.BST_VIDEOGRAPHER_LN));
    assertThat(token.getIpAddress(), is("127.0.0.1"));
  }
}
