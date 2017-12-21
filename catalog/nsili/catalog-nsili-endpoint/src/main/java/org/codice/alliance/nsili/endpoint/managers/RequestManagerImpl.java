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

import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS.RequestManagerPOA;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;

public class RequestManagerImpl extends RequestManagerPOA implements RequestManager {

  @Override
  public Request[] get_active_requests() throws ProcessingFault, SystemFault {
    return new Request[0];
  }

  @Override
  public int get_default_timeout() throws ProcessingFault, SystemFault {
    return 0;
  }

  @Override
  public void set_default_timeout(int new_default)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public int get_timeout(Request aRequest)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    return 0;
  }

  @Override
  public void set_timeout(Request aRequest, int new_lifetime)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public void delete_request(Request aRequest)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public boolean _is_equivalent(Object other) {
    return false;
  }

  @Override
  public int _hash(int maximum) {
    return 0;
  }

  @Override
  public Object _duplicate() {
    return null;
  }

  @Override
  public void _release() {
    // This method is not expected to be called
  }

  @Override
  public org.omg.CORBA.Request _request(String operation) {
    return null;
  }

  @Override
  public org.omg.CORBA.Request _create_request(
      Context ctx, String operation, NVList arg_list, NamedValue result) {
    return null;
  }

  @Override
  public org.omg.CORBA.Request _create_request(
      Context ctx,
      String operation,
      NVList arg_list,
      NamedValue result,
      ExceptionList exclist,
      ContextList ctxlist) {
    return null;
  }

  @Override
  public Policy _get_policy(int policy_type) {
    return null;
  }

  @Override
  public DomainManager[] _get_domain_managers() {
    return new DomainManager[0];
  }

  @Override
  public Object _set_policy_override(Policy[] policies, SetOverrideType set_add) {
    return null;
  }
}
