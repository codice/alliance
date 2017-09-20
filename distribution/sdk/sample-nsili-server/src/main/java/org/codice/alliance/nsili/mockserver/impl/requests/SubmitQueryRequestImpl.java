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
package org.codice.alliance.nsili.mockserver.impl.requests;

import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequestPOA;
import org.codice.alliance.nsili.common.GIAS._RequestManagerStub;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.StringDAGListHolder;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.mockserver.data.DAGGenerator;
import org.omg.CORBA.NO_IMPLEMENT;

public class SubmitQueryRequestImpl extends SubmitQueryRequestPOA {

  @Override
  public void set_number_of_hits(int hits)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return;
  }

  @Override
  public State complete_DAG_results(DAGListHolder results) throws ProcessingFault, SystemFault {
    DAG[] result = DAGGenerator.generateDAGResultNSILAllView(_orb());
    results.value = result;
    return State.COMPLETED;
  }

  @Override
  public State complete_stringDAG_results(StringDAGListHolder results)
      throws ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT();
  }

  @Override
  public State complete_XML_results(org.omg.CORBA.StringHolder results)
      throws ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT();
  }

  @Override
  public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
    return new RequestDescription();
  }

  @Override
  public void set_user_info(String message)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    return;
  }

  @Override
  public Status get_status() throws ProcessingFault, SystemFault {
    return new Status();
  }

  @Override
  public DelayEstimate get_remaining_delay() throws ProcessingFault, SystemFault {
    return new DelayEstimate();
  }

  @Override
  public void cancel() throws ProcessingFault, SystemFault {
    return;
  }

  @Override
  public String register_callback(Callback acallback)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    return "";
  }

  @Override
  public void free_callback(String id) throws InvalidInputParameter, ProcessingFault, SystemFault {
    return;
  }

  @Override
  public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
    return new _RequestManagerStub();
  }
}
