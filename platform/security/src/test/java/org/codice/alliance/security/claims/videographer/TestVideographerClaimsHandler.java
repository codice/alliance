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
package org.codice.alliance.security.claims.videographer;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.apache.wss4j.common.principal.CustomTokenPrincipal;
import org.codice.alliance.security.principal.videographer.VideographerPrincipal;
import org.junit.Test;

public class TestVideographerClaimsHandler {

    private static final String CLAIM_URI_1 =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private static final String CLAIM_URI_2 =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

    private static final String CLAIM_URI_3 =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname";

    private static final String CLAIM_VALUE_1 = "Videographer";

    private static final String CLAIM_VALUE_2a = "Videographer@videographer.com";

    private static final String CLAIM_VALUE_2b = "someguy@somesite.com";

    private static final String CLAIM_VALUE_2c = "somedude@cool.com";

    private static final String CLAIM_VALUE_3 = "Videographer";

    private static final String CLAIM1 = CLAIM_URI_1 + "=" + CLAIM_VALUE_1;

    private static final String CLAIM2 = CLAIM_URI_2 + "=" + CLAIM_VALUE_2a;

    private static final String CLAIM3 = CLAIM_URI_3 + "=" + CLAIM_VALUE_3;

    @Test
    public void testSettingClaimsMapList() throws URISyntaxException {
        VideographerClaimsHandler claimsHandler = new VideographerClaimsHandler();
        claimsHandler.setAttributes(Arrays.asList(CLAIM1, CLAIM2, CLAIM3));

        Map<URI, List<String>> claimsMap = claimsHandler.getClaimsMap();

        List<String> value = claimsMap.get(new URI(CLAIM_URI_1));
        assertThat(value.get(0), is(CLAIM_VALUE_1));

        value = claimsMap.get(new URI(CLAIM_URI_2));
        assertThat(value.get(0), is(CLAIM_VALUE_2a));

        value = claimsMap.get(new URI(CLAIM_URI_3));
        assertThat(value.get(0), is(CLAIM_VALUE_3));

    }

    @Test
    public void testRetrieveClaims() throws URISyntaxException {
        VideographerClaimsHandler claimsHandler = new VideographerClaimsHandler();
        claimsHandler.setAttributes(Arrays.asList(CLAIM1,
                CLAIM_URI_2 + "=" + CLAIM_VALUE_2a + "|" + CLAIM_VALUE_2b + "|" + CLAIM_VALUE_2c,
                CLAIM3));

        ClaimCollection requestClaims = new ClaimCollection();
        Claim requestClaim = new Claim();
        URI nameURI = new URI(CLAIM_URI_1);
        requestClaim.setClaimType(nameURI);
        requestClaims.add(requestClaim);
        requestClaim = new Claim();
        URI emailURI = new URI(CLAIM_URI_2);
        requestClaim.setClaimType(emailURI);
        requestClaims.add(requestClaim);
        requestClaim = new Claim();
        URI fooURI = new URI("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/foobar");
        requestClaim.setClaimType(fooURI);
        requestClaim.setOptional(true);
        requestClaims.add(requestClaim);

        ClaimsParameters claimsParameters = new ClaimsParameters();
        claimsParameters.setPrincipal(new VideographerPrincipal("127.0.0.1"));

        List<URI> supportedClaims = claimsHandler.getSupportedClaimTypes();

        assertThat(supportedClaims, hasSize(3));

        ProcessedClaimCollection claimsCollection = claimsHandler.retrieveClaimValues(requestClaims,
                claimsParameters);

        assertThat(claimsCollection, hasSize(3));

        for (ProcessedClaim claim : claimsCollection) {
            if (claim.getClaimType()
                    .equals(nameURI)) {
                assertThat(claim.getValues(), hasSize(1));
                assertThat(claim.getValues()
                        .get(0), is(CLAIM_VALUE_1));
            } else if (claim.getClaimType()
                    .equals(emailURI)) {
                assertThat(claim.getValues(), hasSize(3));
                List<Object> values = claim.getValues();
                assertThat(values.get(0), is(CLAIM_VALUE_2a));
                assertThat(values.get(1), is(CLAIM_VALUE_2b));
                assertThat(values.get(2), is(CLAIM_VALUE_2c));
            }
            assertThat(claim.getClaimType(), not(fooURI));
        }

        claimsParameters = new ClaimsParameters();
        claimsCollection = claimsHandler.retrieveClaimValues(requestClaims, claimsParameters);

        assertThat(claimsCollection, hasSize(2));

        claimsParameters = new ClaimsParameters();
        claimsParameters.setPrincipal(new CustomTokenPrincipal("SomeValue"));
        claimsCollection = claimsHandler.retrieveClaimValues(requestClaims, claimsParameters);

        assertThat(claimsCollection, hasSize(2));
    }
}
