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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.codice.alliance.nsili.common.GIAS.Event;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.NamedEventType;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.QueryLifeSpan;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.StandingQueryMgrPOA;
import org.codice.alliance.nsili.common.GIAS.SubmitStandingQueryRequest;
import org.codice.alliance.nsili.common.GIAS.SubmitStandingQueryRequestHelper;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.exception_details;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.endpoint.requests.SubmitStandingQueryRequestImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

public class StandingQueryMgrImpl extends StandingQueryMgrPOA {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(StandingQueryMgrImpl.class);

  private Event[] eventTypes;

  private CatalogFramework catalogFramework;

  private FilterBuilder filterBuilder;

  private long defaultUpdateFrequencyMsec;

  private int maxPendingResults;

  private Set<String> querySources = new HashSet<>();

  private boolean removeSourceLibrary;

  private boolean outgoingValidationEnabled;

  private long maxWaitToStartTimeMsecs;

  private long defaultTimeout = AccessManagerImpl.DEFAULT_TIMEOUT;

  public StandingQueryMgrImpl(Set<String> querySources) {
    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
    init();
  }

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setFilterBuilder(FilterBuilder filterBuilder) {
    this.filterBuilder = filterBuilder;
  }

  public void setDefaultUpdateFrequencyMsec(long defaultUpdateFrequencyMsec) {
    this.defaultUpdateFrequencyMsec = defaultUpdateFrequencyMsec;
  }

  public void setMaxPendingResults(int maxPendingResults) {
    this.maxPendingResults = maxPendingResults;
  }

  public void setRemoveSourceLibrary(boolean removeSourceLibrary) {
    this.removeSourceLibrary = removeSourceLibrary;
  }

  public void setOutgoingValidationEnabled(boolean outgoingValidationEnabled) {
    this.outgoingValidationEnabled = outgoingValidationEnabled;
  }

  public void setMaxWaitToStartTimeMsecs(long maxWaitToStartTimeMsecs) {
    this.maxWaitToStartTimeMsecs = maxWaitToStartTimeMsecs;
  }

  protected void init() {
    NamedEventType startEventType = NamedEventType.from_int(NamedEventType._START_EVENT);
    Event startEvent = new Event("START_EVENT", startEventType, "");
    NamedEventType stopEventType = NamedEventType.from_int(NamedEventType._STOP_EVENT);
    Event stopEvent = new Event("STOP_EVENT", stopEventType, "");
    NamedEventType frequencyEventType = NamedEventType.from_int(NamedEventType._FREQUENCY_EVENT);
    Event frequencyEvent = new Event("FREQUENCY_EVENT", frequencyEventType, "");
    eventTypes = new Event[] {startEvent, stopEvent, frequencyEvent};
  }

  @Override
  public Event[] get_event_descriptions() throws ProcessingFault, SystemFault {
    return eventTypes;
  }

  @Override
  public SubmitStandingQueryRequest submit_standing_query(
      Query aQuery,
      String[] result_attributes,
      SortAttribute[] sort_attributes,
      QueryLifeSpan lifespan,
      NameValue[] properties)
      throws InvalidInputParameter, ProcessingFault, SystemFault {

    if (aQuery == null) {
      InvalidInputParameter except = new InvalidInputParameter();
      exception_details details = new exception_details();
      details.exception_name = "No Query Specified";
      details.exception_desc = "Query must be specified for standing query request";
      except.details = details;
      throw except;
    }

    LOGGER.debug("Registering Standing Query View: {}, BQS: {}", aQuery.view, aQuery.bqs_query);

    SubmitStandingQueryRequestImpl standingQueryRequest =
        new SubmitStandingQueryRequestImpl(
            aQuery,
            result_attributes,
            sort_attributes,
            lifespan,
            properties,
            catalogFramework,
            filterBuilder,
            defaultUpdateFrequencyMsec,
            querySources,
            maxPendingResults,
            removeSourceLibrary,
            outgoingValidationEnabled,
            maxWaitToStartTimeMsecs);

    String id = UUID.randomUUID().toString();
    try {
      _poa()
          .activate_object_with_id(
              id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), standingQueryRequest);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.debug(
          "submit_standing_query : Unable to activate submitStandingQueryRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        _poa()
            .create_reference_with_id(
                id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                SubmitStandingQueryRequestHelper.id());

    SubmitStandingQueryRequest submitStandingQueryRequest =
        SubmitStandingQueryRequestHelper.narrow(obj);

    return submitStandingQueryRequest;
  }

  @Override
  public String[] get_property_names() throws ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT();
  }

  @Override
  public NameValue[] get_property_values(String[] desired_properties)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT();
  }

  @Override
  public Library[] get_libraries() throws ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT();
  }

  @Override
  public Request[] get_active_requests() throws ProcessingFault, SystemFault {
    return new Request[0];
  }

  @Override
  public int get_default_timeout() throws ProcessingFault, SystemFault {
    return (int) defaultTimeout;
  }

  @Override
  public void set_default_timeout(int new_default)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    this.defaultTimeout = new_default;
  }

  @Override
  public int get_timeout(Request aRequest)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    return (int) defaultTimeout;
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
}
