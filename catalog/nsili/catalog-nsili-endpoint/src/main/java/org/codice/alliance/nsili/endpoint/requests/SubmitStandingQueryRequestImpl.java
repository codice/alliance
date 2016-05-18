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
package org.codice.alliance.nsili.endpoint.requests;

import org.omg.CORBA.StringHolder;

import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS.SubmitStandingQueryRequestPOA;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.StringDAGListHolder;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.Time;

public class SubmitStandingQueryRequestImpl extends SubmitStandingQueryRequestPOA {
    @Override
    public void set_number_of_hits(int hits)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

    }

    @Override
    public int get_number_of_hits() throws ProcessingFault, SystemFault {
        return 0;
    }

    @Override
    public int get_number_of_hits_in_interval(int interval)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return 0;
    }

    @Override
    public int get_number_of_intervals() throws ProcessingFault, SystemFault {
        return 0;
    }

    @Override
    public void clear_all() throws ProcessingFault, SystemFault {

    }

    @Override
    public void clear_intervals(int num_intervals)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

    }

    @Override
    public void clear_before(Time relative_time)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

    }

    @Override
    public void pause() throws ProcessingFault, SystemFault {

    }

    @Override
    public void resume() throws ProcessingFault, SystemFault {

    }

    @Override
    public AbsTime get_time_last_executed() throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public AbsTime get_time_next_execution() throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public State complete_DAG_results(DAGListHolder results) throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public State complete_stringDAG_results(StringDAGListHolder results)
            throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public State complete_XML_results(StringHolder results) throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public void set_user_info(String message)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

    }

    @Override
    public Status get_status() throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public DelayEstimate get_remaining_delay() throws ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public void cancel() throws ProcessingFault, SystemFault {

    }

    @Override
    public String register_callback(Callback acallback)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return null;
    }

    @Override
    public void free_callback(String id)
            throws InvalidInputParameter, ProcessingFault, SystemFault {

    }

    @Override
    public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
        return null;
    }
}
