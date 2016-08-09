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
package org.codice.alliance.video.security.claims.videographer;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.apache.wss4j.common.principal.CustomTokenPrincipal;
import org.codice.alliance.video.security.principal.videographer.VideographerPrincipal;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

public class TestVideographerClaimsHandler {

    private static final String CLAIM_URI_1 =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private static final String CLAIM_URI_2 =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

    private static final String CLAIM_URI_3 =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname";

    private static final String CLAIM_VALUE_1 = "Videographer";

    private static final String CLAIM_VALUE_2A = "Videographer@videographer.com";

    private static final String CLAIM_VALUE_2B = "someguy@somesite.com";

    private static final String CLAIM_VALUE_2C = "somedude@cool.com";

    private static final String CLAIM_VALUE_3 = "Videographer";

    private static final String CLAIM1 = CLAIM_URI_1 + "=" + CLAIM_VALUE_1;

    private static final String CLAIM2 = CLAIM_URI_2 + "=" + CLAIM_VALUE_2A;

    private static final String CLAIM3 = CLAIM_URI_3 + "=" + CLAIM_VALUE_3;

    @Test
    public void testSettingClaimsMapList() throws URISyntaxException {
        VideographerClaimsHandler claimsHandler = new VideographerClaimsHandler();
        claimsHandler.setAttributes(Arrays.asList(CLAIM1, CLAIM2, CLAIM3));

        Map<URI, List<String>> claimsMap = claimsHandler.getClaimsMap();

        List<String> value = claimsMap.get(new URI(CLAIM_URI_1));
        assertThat(value.get(0), is(CLAIM_VALUE_1));

        value = claimsMap.get(new URI(CLAIM_URI_2));
        assertThat(value.get(0), is(CLAIM_VALUE_2A));

        value = claimsMap.get(new URI(CLAIM_URI_3));
        assertThat(value.get(0), is(CLAIM_VALUE_3));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieveClaims() throws URISyntaxException {
        VideographerClaimsHandler claimsHandler = new VideographerClaimsHandler();
        claimsHandler.setAttributes(Arrays.asList(CLAIM1,
                CLAIM_URI_2 + "=" + CLAIM_VALUE_2A + "|" + CLAIM_VALUE_2B + "|" + CLAIM_VALUE_2C,
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

        Matcher<ProcessedClaim> nameClaim = new CustomMatcher<ProcessedClaim>("claim by name") {
            @Override
            public boolean matches(Object o) {
                ProcessedClaim claim = (ProcessedClaim) o;
                return claim.getClaimType()
                        .equals(nameURI) && claim.getValues()
                        .size() == 1 && claim.getValues()
                        .get(0)
                        .equals(CLAIM_VALUE_1);
            }
        };

        Matcher<ProcessedClaim> emailClaim = new CustomMatcher<ProcessedClaim>("claim by email") {
            @Override
            public boolean matches(Object o) {
                ProcessedClaim claim = (ProcessedClaim) o;
                return claim.getClaimType()
                        .equals(emailURI) && claim.getValues()
                        .size() == 3 &&
                        claim.getValues()
                                .get(0)
                                .equals(CLAIM_VALUE_2A) &&
                        claim.getValues()
                                .get(1)
                                .equals(CLAIM_VALUE_2B) &&
                        claim.getValues()
                                .get(2)
                                .equals(CLAIM_VALUE_2C);
            }
        };

        Matcher<ProcessedClaim> ipClaim = new CustomMatcher<ProcessedClaim>("claim by ip") {
            @Override
            public boolean matches(Object o) {
                ProcessedClaim claim = (ProcessedClaim) o;
                return claim.getClaimType()
                        .toString()
                        .equals("IpAddress") && claim.getValues()
                        .size() == 1 && claim.getValues()
                        .get(0)
                        .equals("127.0.0.1");
            }
        };

        assertThat(claimsCollection, containsInAnyOrder(nameClaim, emailClaim, ipClaim));

        claimsParameters = new ClaimsParameters();
        claimsCollection = claimsHandler.retrieveClaimValues(requestClaims, claimsParameters);

        assertThat(claimsCollection, hasSize(2));

        claimsParameters = new ClaimsParameters();
        claimsParameters.setPrincipal(new CustomTokenPrincipal("SomeValue"));
        claimsCollection = claimsHandler.retrieveClaimValues(requestClaims, claimsParameters);

        assertThat(claimsCollection, hasSize(2));
    }
}
