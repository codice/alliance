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
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Test;
import org.omg.CORBA.NO_IMPLEMENT;

import org.codice.alliance.nsili.common.GIAS.Association;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.ConceptualAttributeType;
import org.codice.alliance.nsili.common.GIAS.View;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.EntityGraph;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameName;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.endpoint.managers.DataModelMgrImpl;

public class DataModelMgrImplTest {

    private DataModelMgrImpl dataModelMgr = new DataModelMgrImpl();

    @Test
    public void testGetModelDate() throws InvalidInputParameter, SystemFault, ProcessingFault {
        AbsTime modelDate = dataModelMgr.get_data_model_date(null);
        assertThat(modelDate, notNullValue());
    }

    @Test
    public void testGetAliasCategories()
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        String[] aliasCategories = dataModelMgr.get_alias_categories(null);
        assertThat(aliasCategories, notNullValue());
        assertThat(aliasCategories.length, is(6));
    }

    @Test
    public void testGetLogicalAliases() throws InvalidInputParameter, SystemFault, ProcessingFault {
        NameName[] aliases = dataModelMgr.get_logical_aliases(NsiliConstants.NSIL_CORE, null);
        assertThat(aliases, notNullValue());
    }

    @Test
    public void testGetLogicalAttributeName()
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        String logicalAttr = dataModelMgr.get_logical_attribute_name(NsiliConstants.NSIL_ALL_VIEW,
                ConceptualAttributeType.CLASSIFICATION, null);
        assertThat(logicalAttr, notNullValue());
        assertThat(logicalAttr, is("NSIL_SECURITY.classification"));
    }

    @Test
    public void testGetViewNames() throws InvalidInputParameter, SystemFault, ProcessingFault {
        View[] viewNames = dataModelMgr.get_view_names(null);
        assertThat(viewNames.length, is(9));
    }

    @Test
    public void testGetAttributes() throws InvalidInputParameter, SystemFault, ProcessingFault {
        AttributeInformation[] attributes = dataModelMgr.get_attributes(NsiliConstants.NSIL_ALL_VIEW, null);
        assertThat(attributes, notNullValue());
    }

    @Test
    public void testGetEntities() throws InvalidInputParameter, SystemFault, ProcessingFault {
        EntityGraph graph = dataModelMgr.get_entities(NsiliConstants.NSIL_ALL_VIEW, null);
        assertThat(graph, notNullValue());
    }

    @Test
    public void testGetEntityAttributes()
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        AttributeInformation[] attributes = dataModelMgr.get_entity_attributes(NsiliConstants.NSIL_CARD, null);
        assertThat(attributes, notNullValue());
    }

    @Test
    public void testGetQueryableAttributes()
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        AttributeInformation[] attributes =  dataModelMgr.get_queryable_attributes(NsiliConstants.NSIL_ALL_VIEW, null);
        assertThat(attributes, notNullValue());
        assertThat(attributes.length, greaterThan(0));
    }

    @Test
    public void testGetAssociations() throws InvalidInputParameter, SystemFault, ProcessingFault {
        Association[] associations = dataModelMgr.get_associations(null);
        assertThat(associations.length, is(6));
    }

    @Test
    public void testGetMaxVertices() throws InvalidInputParameter, SystemFault, ProcessingFault {
        short maxVertices = dataModelMgr.get_max_vertices(null);
        assertThat(maxVertices, greaterThan((short)0));
    }

    @Test(expected = NO_IMPLEMENT.class)
    public void testGetPropertyNames() throws SystemFault, ProcessingFault {
        dataModelMgr.get_property_names();
    }

    @Test(expected = NO_IMPLEMENT.class)
    public void testGetPropertyValues() throws InvalidInputParameter, SystemFault, ProcessingFault {
        dataModelMgr.get_property_values(null);
    }

    @Test(expected = NO_IMPLEMENT.class)
    public void testGetLibraries() throws SystemFault, ProcessingFault {
        dataModelMgr.get_libraries();
    }
}
