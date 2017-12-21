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
package org.codice.alliance.nsili.endpoint.managers;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.filter.FilterBuilder;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.GIAS.AccessManagerHelper;
import org.codice.alliance.nsili.common.GIAS.AvailabilityRequirement;
import org.codice.alliance.nsili.common.GIAS.GetParametersRequest;
import org.codice.alliance.nsili.common.GIAS.GetParametersRequestHelper;
import org.codice.alliance.nsili.common.GIAS.GetRelatedFilesRequest;
import org.codice.alliance.nsili.common.GIAS.GetRelatedFilesRequestHelper;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.ProductMgrPOA;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SetAvailabilityRequest;
import org.codice.alliance.nsili.common.GIAS._SetAvailabilityRequestStub;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.endpoint.requests.GetParametersRequestImpl;
import org.codice.alliance.nsili.endpoint.requests.GetRelatedFilesRequestImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

public class ProductMgrImpl extends ProductMgrPOA {

  public static final String THUMBNAIL_RELATED_FILE = "THUMBNAIL";

  private static final String PORT_PROP = "PORT";

  private static final int QUERY_AVAILABILITY_DELAY = 10;

  private static final int TIMEOUT = 1;

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProductMgrImpl.class);

  private CatalogFramework catalogFramework;

  private FilterBuilder filterBuilder;

  private Set<String> querySources;

  private AccessManagerImpl accessManager;

  private boolean outgoingValidationEnabled;

  public ProductMgrImpl(Set<String> querySources) {
    if (querySources != null) {
      this.querySources = new HashSet<>(querySources);
    }
  }

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setFilterBuilder(FilterBuilder filterBuilder) {
    this.filterBuilder = filterBuilder;
  }

  public void setOutgoingValidationEnabled(boolean outgoingValidationEnabled) {
    this.outgoingValidationEnabled = outgoingValidationEnabled;
  }

  @Override
  public GetParametersRequest get_parameters(
      Product prod, String[] desired_parameters, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {

    GetParametersRequest getParamRequest = null;

    String id = UUID.randomUUID().toString();

    try {
      String productIdStr = getAccessManager().getProductId(prod);

      GetParametersRequestImpl getParametersRequest =
          new GetParametersRequestImpl(
              productIdStr,
              desired_parameters,
              catalogFramework,
              filterBuilder,
              querySources,
              outgoingValidationEnabled);
      _poa()
          .activate_object_with_id(
              id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), getParametersRequest);

      org.omg.CORBA.Object obj =
          _poa()
              .create_reference_with_id(
                  id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                  GetParametersRequestHelper.id());
      getParamRequest = GetParametersRequestHelper.narrow(obj);

    } catch (WrongAdapter | WrongPolicy | UnsupportedEncodingException e) {
      LOGGER.debug(
          "Unable to get ID from product reference: {}",
          NsilCorbaExceptionUtil.getExceptionDetails(e),
          e);
    } catch (ServantAlreadyActive | ObjectAlreadyActive e) {
      LOGGER.debug(
          "get_parameters : Unable to activate getParametersRequest object. {}",
          NsilCorbaExceptionUtil.getExceptionDetails(e),
          e);
    }

    return getParamRequest;
  }

  @Override
  public String[] get_related_file_types(Product prod)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return new String[] {THUMBNAIL_RELATED_FILE};
  }

  @Override
  public GetRelatedFilesRequest get_related_files(
      Product[] products, FileLocation location, String type, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    String id = UUID.randomUUID().toString();

    try {
      List<Metacard> metacards = new ArrayList<>();
      AccessManagerImpl accessMgr = getAccessManager();
      for (Product product : products) {
        Metacard metacard = accessMgr.getMetacard(accessMgr.getProductId(product));
        if (metacard != null) {
          metacards.add(metacard);
        }
      }

      Integer port = getPort(properties);
      GetRelatedFilesRequestImpl getRelatedFilesRequest =
          new GetRelatedFilesRequestImpl(metacards, location, type, port);
      _poa()
          .activate_object_with_id(
              id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), getRelatedFilesRequest);
    } catch (ServantAlreadyActive
        | ObjectAlreadyActive
        | WrongPolicy
        | WrongAdapter
        | UnsupportedEncodingException e) {
      LOGGER.debug("get_related_files : Unable to activate getRelatedFilesRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        _poa()
            .create_reference_with_id(
                id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                GetRelatedFilesRequestHelper.id());
    GetRelatedFilesRequest queryRequest = GetRelatedFilesRequestHelper.narrow(obj);

    return queryRequest;
  }

  // Access Mgr
  @Override
  public String[] get_use_modes() throws ProcessingFault, SystemFault {
    return getAccessManager().get_use_modes();
  }

  @Override
  public boolean is_available(Product prod, String use_mode)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return getAccessManager().is_available(prod, use_mode);
  }

  @Override
  public int query_availability_delay(
      Product prod, AvailabilityRequirement availability_requirement, String use_mode)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return QUERY_AVAILABILITY_DELAY;
  }

  @Override
  public short get_number_of_priorities() throws ProcessingFault, SystemFault {
    return getAccessManager().get_number_of_priorities();
  }

  @Override
  public SetAvailabilityRequest set_availability(
      Product[] products,
      AvailabilityRequirement availability_requirement,
      String use_mode,
      short priority)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return new _SetAvailabilityRequestStub();
  }

  // Request
  @Override
  public Request[] get_active_requests() throws ProcessingFault, SystemFault {
    return new Request[0];
  }

  @Override
  public int get_default_timeout() throws ProcessingFault, SystemFault {
    return TIMEOUT;
  }

  @Override
  public void set_default_timeout(int new_default)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public int get_timeout(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return TIMEOUT;
  }

  @Override
  public void set_timeout(Request aRequest, int new_lifetime)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public void delete_request(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    // This method is not expected to be called
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

  private Integer getPort(NameValue[] props) {
    Integer port = null;
    for (NameValue prop : props) {
      if (prop.aname.equals(PORT_PROP)) {
        String propValueStr = prop.value.extract_string();
        try {
          port = Integer.parseInt(propValueStr);
        } catch (NumberFormatException nfe) {
          LOGGER.debug("Unable to parse port from string: {}", propValueStr);
        }
        break;
      }
    }
    return port;
  }

  private AccessManagerImpl getAccessManager() {
    if (accessManager == null) {
      accessManager = new AccessManagerImpl();
      accessManager.setCatalogFramework(catalogFramework);
      accessManager.setFilterBuilder(filterBuilder);
      accessManager.setQuerySources(querySources);

      String managerId = UUID.randomUUID().toString();
      if (!CorbaUtils.isIdActive(
          _poa(), managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
        try {
          _poa()
              .activate_object_with_id(
                  managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), accessManager);
        } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
          LOGGER.debug(
              "Error activating AcccessMgr: {}", NsilCorbaExceptionUtil.getExceptionDetails(e), e);
        }
      }

      _poa()
          .create_reference_with_id(
              managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
              AccessManagerHelper.id());
    }

    return accessManager;
  }
}
