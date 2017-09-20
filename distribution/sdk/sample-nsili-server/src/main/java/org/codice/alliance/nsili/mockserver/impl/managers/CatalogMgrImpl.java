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
import org.codice.alliance.nsili.common.GIAS.CatalogMgrPOA;
import org.codice.alliance.nsili.common.GIAS.HitCountRequest;
import org.codice.alliance.nsili.common.GIAS.HitCountRequestHelper;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequest;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequestHelper;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.mockserver.impl.requests.HitCountRequestImpl;
import org.codice.alliance.nsili.mockserver.impl.requests.SubmitQueryRequestImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogMgrImpl extends CatalogMgrPOA {
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogMgrImpl.class);

  private static final String ENCODING = "UTF-8";

  private static final int DEFAULT_TIMEOUT = 1;

  private POA poa_;

  public CatalogMgrImpl(POA poa) {
    this.poa_ = poa;
  }

  @Override
  public Request[] get_active_requests() throws ProcessingFault, SystemFault {
    return new Request[0];
  }

  @Override
  public int get_default_timeout() throws ProcessingFault, SystemFault {
    return DEFAULT_TIMEOUT;
  }

  @Override
  public void set_default_timeout(int new_default)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return;
  }

  @Override
  public int get_timeout(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return DEFAULT_TIMEOUT;
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

  @Override
  public SubmitQueryRequest submit_query(
      Query aQuery,
      String[] result_attributes,
      SortAttribute[] sort_attributes,
      NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {

    SubmitQueryRequestImpl submitQueryRequest = new SubmitQueryRequestImpl();

    try {
      poa_.activate_object_with_id(
          "submit_query".getBytes(Charset.forName(ENCODING)), submitQueryRequest);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.warn("submit_query : Unable to activate submitQueryRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        poa_.create_reference_with_id(
            "submit_query".getBytes(Charset.forName(ENCODING)), SubmitQueryRequestHelper.id());
    SubmitQueryRequest queryRequest = SubmitQueryRequestHelper.narrow(obj);

    return queryRequest;
  }

  @Override
  public HitCountRequest hit_count(Query aQuery, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    HitCountRequestImpl hitCountRequest = new HitCountRequestImpl();

    try {
      poa_.activate_object_with_id(
          "hit_count".getBytes(Charset.forName(ENCODING)), hitCountRequest);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.warn("hit_count : Unable to activate hitCountRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        poa_.create_reference_with_id(
            "hit_count".getBytes(Charset.forName(ENCODING)), HitCountRequestHelper.id());
    HitCountRequest queryRequest = HitCountRequestHelper.narrow(obj);

    return queryRequest;
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
