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
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.security.service.SecurityServiceException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.GIAS.CreateAssociationRequest;
import org.codice.alliance.nsili.common.GIAS.CreateMetaDataRequest;
import org.codice.alliance.nsili.common.GIAS.CreateRequest;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.endpoint.managers.AccessManagerImpl;
import org.codice.alliance.nsili.endpoint.managers.CreationMgrImpl;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreationMgrImplTest extends NsiliTestCommon {

  private CreationMgrImpl creationMgr;

  private static final Logger LOGGER = LoggerFactory.getLogger(CreationMgrImplTest.class);

  @Before
  public void setUp() throws Exception {
    try {
      setupOrb();
      orbRunThread = new Thread(() -> orb.run());
      orbRunThread.start();
    } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
      LOGGER.error("Unable to start the CORBA server", e);
    } catch (IOException e) {
      LOGGER.error("Unable to generate the IOR file", e);
    } catch (SecurityServiceException e) {
      LOGGER.error("Unable to setup guest security credentials", e);
    }

    String managerId = UUID.randomUUID().toString();
    creationMgr = new CreationMgrImpl();
    creationMgr.setFilterBuilder(new GeotoolsFilterBuilder());
    creationMgr.setCatalogFramework(mockCatalogFramework);

    if (!CorbaUtils.isIdActive(
        rootPOA, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        rootPOA.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), creationMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.error("Error activating ProductMgr: {}", e);
      }
    }

    rootPOA.create_reference_with_id(
        managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), ProductMgrHelper.id());
  }

  @Test
  public void testCreate() throws InvalidInputParameter, SystemFault, ProcessingFault {
    CreateRequest createRequest = creationMgr.create(null, null, null, null);
    assertThat(createRequest, nullValue());
  }

  @Test
  public void testCreateMetadata() throws InvalidInputParameter, SystemFault, ProcessingFault {
    CreateMetaDataRequest createMetaDataRequest =
        creationMgr.create_metadata(null, null, null, null);
    assertThat(createMetaDataRequest, nullValue());
  }

  @Test
  public void testCreateAssociation() throws InvalidInputParameter, SystemFault, ProcessingFault {
    CreateAssociationRequest createAssociationRequest =
        creationMgr.create_association(null, null, null, null);
    assertThat(createAssociationRequest, nullValue());
  }

  @Test
  public void testGetPropertyNames() throws SystemFault, ProcessingFault {
    String[] propertyNames = creationMgr.get_property_names();
    assertThat(propertyNames.length, is(2));
  }

  @Test
  public void testGetPropertyValues() throws Exception {
    NameValue[] props = creationMgr.get_property_values(null);
    assertThat(props.length, is(0));
  }

  @Test(expected = NO_IMPLEMENT.class)
  public void testGetLibraries() throws Exception {
    creationMgr.get_libraries();
  }

  @Test
  public void testGetActiveRequests() throws SystemFault, ProcessingFault {
    Request[] requests = creationMgr.get_active_requests();
    assertThat(requests.length, is(0));
  }

  @Test
  public void testGetTimeout() throws Exception {
    int timeout = creationMgr.get_timeout(null);
    assertThat(timeout, is(AccessManagerImpl.DEFAULT_TIMEOUT));
  }

  @Test
  public void testGetDefaultTimeout() throws Exception {
    creationMgr.set_default_timeout(5000);
    int timeout = creationMgr.get_default_timeout();
    assertThat(timeout, greaterThan(0));
  }

  @Test
  public void testDeleteRequest() throws InvalidInputParameter, SystemFault, ProcessingFault {
    creationMgr.delete_request(null);
  }

  @Test
  public void testSetTimeout() throws InvalidInputParameter, SystemFault, ProcessingFault {
    creationMgr.set_timeout(null, 1000);
  }
}
