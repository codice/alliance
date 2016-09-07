/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codice.alliance.nsili.common.UCO.DAG;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.data.types.Core;

public class ResultDAGConverterTest {
    private static final String CARD_TITLE = "Test Title";

    private static final String CARD_SOURCE = "Test Source";

    private static final String STATUS_ATTR_NAME =
            NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "."
                    + NsiliConstants.STATUS;

    private static final Date TEST_CREATE_DATE = new Date(1000);

    private ORB orb;

    private POA rootPOA;

    @Before
    public void setUp() throws AdapterInactive, InvalidName {
        orb = ORB.init(new String[0], null);
        rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();
    }

    @Test
    public void testResultAttributes() throws Exception {
        MetacardImpl card = getTestCard();

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        String sourceAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "."
                + NsiliConstants.SOURCE_LIBRARY;

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(checkDagContains(dag, sourceAttr), is(true));

        List<String> singleAttrList = new ArrayList<>();
        singleAttrList.add(NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_CARD + "."
                + NsiliConstants.IDENTIFIER);
        DAG oneAttrDAG = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                singleAttrList,
                new HashMap<>());
        assertThat(checkDagContains(oneAttrDAG, sourceAttr), is(false));
    }

    @Test
    public void testAdvancedGeospatial() throws Exception {
        MetacardImpl card = getTestCard();
        card.setLocation("POLYGON((1 1,1 2,2 2,2 1,1 1))");

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        String advGeoAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_PART + ":"
                + NsiliConstants.NSIL_COVERAGE + "." + NsiliConstants.ADVANCED_GEOSPATIAL;

        String boundingGeoAttr = NsiliConstants.NSIL_PRODUCT + ":" + NsiliConstants.NSIL_PART + ":"
                + NsiliConstants.NSIL_COVERAGE + "." + NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX;

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(checkDagContains(dag, advGeoAttr), is(true));
        assertThat(checkDagContains(dag, boundingGeoAttr), is(true));
    }

    @Test(expected = DagParsingException.class)
    public void testMandatoryAttributesFail() throws Exception {
        MetacardImpl card = getTestCard();

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        Map<String, List<String>> mandatoryAttrs = new HashMap<>();
        mandatoryAttrs.put(NsiliConstants.NSIL_COMMON,
                Arrays.asList(NsiliConstants.IDENTIFIER_MISSION));
        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                mandatoryAttrs);
    }

    @Test
    public void testMandatoryAttributesSuccess() throws Exception {
        MetacardImpl card = getTestCard();

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        Map<String, List<String>> mandatoryAttrs = new HashMap<>();
        mandatoryAttrs.put(NsiliConstants.NSIL_CARD, Arrays.asList(NsiliConstants.IDENTIFIER));
        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                mandatoryAttrs);
        assertThat(dag, notNullValue());
    }

    @Test
    public void testCreateAttribute() throws Exception {
        MetacardImpl card = getTestCard();

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(dag, notNullValue());

        String value = ResultDAGConverter.getAttributeMap(dag)
                .get(STATUS_ATTR_NAME);
        assertThat(value, is(NsiliCardStatus.NEW.name()));
    }

    @Test
    public void testChangeAttribute() throws Exception {
        MetacardImpl card = getTestCard();
        Date testModifiedDate = new Date(2000);
        card.setAttribute(new AttributeImpl(Core.METACARD_MODIFIED, testModifiedDate));

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(dag, notNullValue());

        String value = ResultDAGConverter.getAttributeMap(dag)
                .get(STATUS_ATTR_NAME);
        assertThat(value, is(NsiliCardStatus.CHANGED.name()));
    }

    @Test
    public void testDeleteAttribute() throws Exception {
        MetacardImpl card = getTestCard();
        Date testModifiedDate = new Date(2000);
        card.setModifiedDate(testModifiedDate);
        card.setAttribute(new AttributeImpl(MetacardVersion.ACTION,
                MetacardVersion.Action.DELETED.getKey()));

        ResultImpl result = new ResultImpl();
        result.setMetacard(card);

        DAG dag = ResultDAGConverter.convertResult(result,
                orb,
                rootPOA,
                new ArrayList<>(),
                new HashMap<>());
        assertThat(dag, notNullValue());

        String value = ResultDAGConverter.getAttributeMap(dag)
                .get(STATUS_ATTR_NAME);
        assertThat(value, is(NsiliCardStatus.OBSOLETE.name()));
    }

    private static boolean checkDagContains(DAG dag, String attribute) {
        List<String> dagAttrs = ResultDAGConverter.getAttributes(dag);
        return dagAttrs.contains(attribute);
    }

    private MetacardImpl getTestCard() {
        String id = UUID.randomUUID()
                .toString();

        MetacardImpl card = new MetacardImpl();
        card.setId(id);
        card.setTitle(CARD_TITLE);
        card.setSourceId(CARD_SOURCE);
        card.setAttribute(new AttributeImpl(Core.METACARD_CREATED, TEST_CREATE_DATE));
        card.setCreatedDate(TEST_CREATE_DATE);
        card.setAttribute(new AttributeImpl(Core.METACARD_MODIFIED, TEST_CREATE_DATE));
        card.setModifiedDate(TEST_CREATE_DATE);

        return card;
    }
}
