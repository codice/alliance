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
import ddf.catalog.filter.FilterBuilder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.GIAS.AccessManagerHelper;
import org.codice.alliance.nsili.common.GIAS.AvailabilityRequirement;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.OrderContents;
import org.codice.alliance.nsili.common.GIAS.OrderMgrPOA;
import org.codice.alliance.nsili.common.GIAS.OrderRequest;
import org.codice.alliance.nsili.common.GIAS.OrderRequestHelper;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SetAvailabilityRequest;
import org.codice.alliance.nsili.common.GIAS.ValidationResults;
import org.codice.alliance.nsili.common.GIAS._SetAvailabilityRequestStub;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.PackagingSpecFormatType;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.endpoint.requests.OrderRequestImpl;
import org.codice.alliance.nsili.transformer.DAGConverter;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

public class OrderMgrImpl extends OrderMgrPOA {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderMgrImpl.class);

  private AccessManagerImpl accessManager = null;

  private CatalogFramework catalogFramework;

  private FilterBuilder filterBuilder;

  private Set<String> querySources = new HashSet<>();

  private EmailConfiguration emailConfiguration;

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setFilterBuilder(FilterBuilder filterBuilder) {
    this.filterBuilder = filterBuilder;
  }

  public void setQuerySources(Set<String> querySources) {
    this.querySources.clear();
    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
  }

  @Override
  public String[] get_package_specifications() throws ProcessingFault, SystemFault {
    List<String> supportedPackagingSpecs = new ArrayList<>(PackagingSpecFormatType.values().length);
    for (PackagingSpecFormatType packagingSpecFormatType : PackagingSpecFormatType.values()) {
      supportedPackagingSpecs.add(packagingSpecFormatType.name());
    }
    return supportedPackagingSpecs.toArray(new String[0]);
  }

  @Override
  public ValidationResults validate_order(OrderContents order, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return new ValidationResults(true, false, "Order Received");
  }

  @Override
  public OrderRequest order(OrderContents order, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {

    String protocol = "http";
    int port = 80;
    for (NameValue prop : properties) {
      if (prop.aname.equals(NsiliConstants.PROP_PROTOCOL)) {
        protocol = DAGConverter.getString(prop.value);
      } else if (prop.aname.equals(NsiliConstants.PROP_PORT)) {
        Integer portInteger = DAGConverter.getInteger(prop.value);
        if (portInteger != null) {
          port = portInteger;
        }
      }
    }

    OrderRequestImpl orderRequestImpl =
        new OrderRequestImpl(
            order, protocol, port, getAccessManager(), catalogFramework, emailConfiguration);

    String id = UUID.randomUUID().toString();
    try {
      _poa()
          .activate_object_with_id(
              id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), orderRequestImpl);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.debug("order : Unable to activate orderRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        _poa()
            .create_reference_with_id(
                id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), OrderRequestHelper.id());

    OrderRequest orderRequest = OrderRequestHelper.narrow(obj);

    return orderRequest;
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
    return getAccessManager().query_availability_delay(prod, availability_requirement, use_mode);
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
    return getAccessManager().get_default_timeout();
  }

  @Override
  public void set_default_timeout(int new_default)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    getAccessManager().set_default_timeout(new_default);
  }

  @Override
  public int get_timeout(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return getAccessManager().get_timeout(aRequest);
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
    return new String[] {NsiliConstants.PROP_PORT, NsiliConstants.PROP_PROTOCOL};
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
          LOGGER.debug("Error activating AcccessMgr: ", e);
        }
      }

      _poa()
          .create_reference_with_id(
              managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
              AccessManagerHelper.id());
    }

    return accessManager;
  }

  public void setEmailConfiguration(EmailConfiguration emailConfiguration) {
    this.emailConfiguration = emailConfiguration;
  }
}
