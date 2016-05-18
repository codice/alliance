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
package org.codice.alliance.nsili.endpoint.managers;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.omg.CORBA.NO_IMPLEMENT;

import org.codice.alliance.nsili.common.GIAS.Association;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.ConceptualAttributeType;
import org.codice.alliance.nsili.common.GIAS.DataModelMgrPOA;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.View;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.Date;
import org.codice.alliance.nsili.common.UCO.EntityGraph;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameName;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.common.datamodel.NsiliDataModel;

public class DataModelMgrImpl extends DataModelMgrPOA {

    private static final String[] VIEW_NAMES =
            new String[] {NsiliConstants.NSIL_ALL_VIEW, NsiliConstants.NSIL_IMAGERY_VIEW,
                    NsiliConstants.NSIL_GMTI_VIEW, NsiliConstants.NSIL_MESSAGE_VIEW,
                    NsiliConstants.NSIL_VIDEO_VIEW, NsiliConstants.NSIL_ASSOCIATION_VIEW,
                    NsiliConstants.NSIL_REPORT_VIEW, NsiliConstants.NSIL_TDL_VIEW,
                    NsiliConstants.NSIL_CCIRM_VIEW};

    private static View[] VIEWS;

    private static final AbsTime LAST_UPDATED = new AbsTime(new Date((short) 2016,
            (short) 2,
            (short) 9), new Time((short) 2, (short) 0, (short) 0));

    private static final short MAX_VERTICES = 10;

    private NsiliDataModel nsiliDataModel = new NsiliDataModel();

    static {
        VIEWS = new View[VIEW_NAMES.length];
        for (int i = 0; i < VIEW_NAMES.length; i++) {
            VIEWS[i] = new View(VIEW_NAMES[i], true, new String[0]);
        }
    }

    @Override
    public AbsTime get_data_model_date(NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return LAST_UPDATED;
    }

    @Override
    public String[] get_alias_categories(NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        return nsiliDataModel.getAliasCategories()
                .toArray(new String[0]);
    }

    @Override
    public NameName[] get_logical_aliases(String category, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        NameName[] logicalAliases = new NameName[0];
        List<Pair<String, String>> aliases = nsiliDataModel.getAliasesForCategory(category);
        if (aliases != null && !aliases.isEmpty()) {
            logicalAliases = aliases.stream()
                    .map(alias -> new NameName(alias.getLeft(), alias.getRight()))
                    .collect(Collectors.toList())
                    .toArray(new NameName[0]);
        }
        return logicalAliases;
    }

    @Override
    public String get_logical_attribute_name(String view_name,
            ConceptualAttributeType attribute_type, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        String logicalAttrName = "";

        List<Pair<ConceptualAttributeType, String>> pairs =
                nsiliDataModel.getConceptualAttrsForView(view_name);
        for (Pair<ConceptualAttributeType, String> pair : pairs) {
            if (pair.getLeft() == attribute_type) {
                logicalAttrName = pair.getRight();
                break;
            }
        }

        return logicalAttrName;
    }

    @Override
    public View[] get_view_names(NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return VIEWS;
    }

    @Override
    public AttributeInformation[] get_attributes(String view_name, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        AttributeInformation[] attributes = nsiliDataModel.getAttributesForView(view_name)
                .toArray(new AttributeInformation[0]);
        return attributes;
    }

    @Override
    public AttributeInformation[] get_queryable_attributes(String view_name, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return nsiliDataModel.getAttributesForView(view_name)
                .toArray(new AttributeInformation[0]);
    }

    @Override
    public EntityGraph get_entities(String view_name, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return nsiliDataModel.getEntityGraph(view_name);
    }

    @Override
    public AttributeInformation[] get_entity_attributes(String aEntity, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return nsiliDataModel.getAttributeInformation(aEntity)
                .toArray(new AttributeInformation[0]);
    }

    @Override
    public Association[] get_associations(NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return nsiliDataModel.getAssociations()
                .toArray(new Association[0]);
    }

    @Override
    public short get_max_vertices(NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return MAX_VERTICES;
    }

    // LibraryMgr
    @Override
    public String[] get_property_names() throws ProcessingFault, SystemFault {
        throw new NO_IMPLEMENT();
    }

    @Override
    public NameValue[] get_property_values(String[] desired_properties)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        throw new NO_IMPLEMENT();
    }

    @Override
    public Library[] get_libraries() throws ProcessingFault, SystemFault {
        throw new NO_IMPLEMENT();
    }
}