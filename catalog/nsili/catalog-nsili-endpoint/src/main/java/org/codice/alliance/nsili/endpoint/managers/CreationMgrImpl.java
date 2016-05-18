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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.codice.alliance.nsili.common.GIAS.CreateAssociationRequest;
import org.codice.alliance.nsili.common.GIAS.CreateMetaDataRequest;
import org.codice.alliance.nsili.common.GIAS.CreateRequest;
import org.codice.alliance.nsili.common.GIAS.CreationMgrPOA;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.RelatedFile;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UID.Product;


public class CreationMgrImpl extends CreationMgrPOA {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreationMgrImpl.class);

    @Override
    public CreateRequest create(FileLocation[] new_product, RelatedFile[] related_files,
            DAG creation_metadata, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.create");

        return null;
    }

    @Override
    public CreateMetaDataRequest create_metadata(DAG creation_metadata, String view_name,
            RelatedFile[] related_files, NameValue[] properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.create_metadata");

        return null;
    }

    @Override
    public CreateAssociationRequest create_association(String assoc_name, Product view_a_object,
            Product[] view_b_objects, NameValue[] assoc_info)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.create_association");

        return null;
    }

    @Override
    public String[] get_property_names() throws ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.get_property_names");

        return new String[0];
    }

    @Override
    public NameValue[] get_property_values(String[] desired_properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.get_property_values");

        return new NameValue[0];
    }

    @Override
    public Library[] get_libraries() throws ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.get_libraries");

        return new Library[0];
    }

    @Override
    public Request[] get_active_requests() throws ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.get_active_requests");

        return new Request[0];
    }

    @Override
    public int get_default_timeout() throws ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.get_default_timeout");

        return 0;
    }

    @Override
    public void set_default_timeout(int new_default)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.set_default_timeout");

    }

    @Override
    public int get_timeout(Request aRequest)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.get_timeout");

        return 0;
    }

    @Override
    public void set_timeout(Request aRequest, int new_lifetime)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.set_timeout");

    }

    @Override
    public void delete_request(Request aRequest)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

        LOGGER.debug("CreationMgrImpl.delete_request");

    }


}
