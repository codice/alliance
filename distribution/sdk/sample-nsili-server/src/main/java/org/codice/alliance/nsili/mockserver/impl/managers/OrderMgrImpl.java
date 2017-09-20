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
package org.codice.alliance.nsili.mockserver.impl.managers;

import java.nio.charset.Charset;
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
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.mockserver.impl.requests.OrderRequestImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderMgrImpl extends OrderMgrPOA {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderMgrImpl.class);

  private static final int QUERY_AVAILABILITY_DELAY = 10;

  private static final int NUM_PRIORITIES = 1;

  private static final int TIMEOUT = 1;

  private static final String ENCODING = "UTF-8";

  @Override
  public String[] get_package_specifications() throws ProcessingFault, SystemFault {
    return new String[0];
  }

  @Override
  public ValidationResults validate_order(OrderContents order, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return new ValidationResults(true, false, "Order Received");
  }

  @Override
  public OrderRequest order(OrderContents order, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    OrderRequestImpl orderRequest = new OrderRequestImpl();

    try {
      _poa().activate_object_with_id("order".getBytes(Charset.forName(ENCODING)), orderRequest);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.warn("Order : Unable to activate orderRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        _poa()
            .create_reference_with_id(
                "order".getBytes(Charset.forName(ENCODING)), OrderRequestHelper.id());
    OrderRequest queryRequest = OrderRequestHelper.narrow(obj);

    return queryRequest;
  }

  // Access Mgr
  @Override
  public String[] get_use_modes() throws ProcessingFault, SystemFault {
    return new String[] {"Orders"};
  }

  @Override
  public boolean is_available(Product prod, String use_mode)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return true;
  }

  @Override
  public int query_availability_delay(
      Product prod, AvailabilityRequirement availability_requirement, String use_mode)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return QUERY_AVAILABILITY_DELAY;
  }

  @Override
  public short get_number_of_priorities() throws ProcessingFault, SystemFault {
    return NUM_PRIORITIES;
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
    return;
  }

  @Override
  public int get_timeout(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return TIMEOUT;
  }

  @Override
  public void set_timeout(Request aRequest, int new_lifetime)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return;
  }

  @Override
  public void delete_request(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return;
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
