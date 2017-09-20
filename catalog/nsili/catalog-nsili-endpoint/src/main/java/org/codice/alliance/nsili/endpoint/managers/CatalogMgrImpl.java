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
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Metacard;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.codice.alliance.nsili.common.BqsConverter;
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
import org.codice.alliance.nsili.endpoint.LibraryImpl;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.endpoint.requests.HitCountRequestImpl;
import org.codice.alliance.nsili.endpoint.requests.SubmitQueryRequestImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogMgrImpl extends CatalogMgrPOA {

  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogMgrImpl.class);

  private POA poa;

  private CatalogFramework catalogFramework;

  private int maxNumResults = NsiliEndpoint.DEFAULT_MAX_NUM_RESULTS;

  private long defaultTimeout = AccessManagerImpl.DEFAULT_TIMEOUT;

  private FilterBuilder filterBuilder;

  private boolean removeSourceLibrary = true;

  private Set<String> querySources = new HashSet<>();

  private boolean outgoingValidationEnabled;

  public CatalogMgrImpl(POA poa, FilterBuilder filterBuilder, Set<String> querySources) {
    this.poa = poa;
    this.filterBuilder = filterBuilder;
    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
  }

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setMaxNumResults(int maxNumResults) {
    this.maxNumResults = maxNumResults;
  }

  public void setRemoveSourceLibrary(boolean removeSourceLibrary) {
    this.removeSourceLibrary = removeSourceLibrary;
  }

  public void setOutgoingValidationEnabled(boolean outgoingValidationEnabled) {
    this.outgoingValidationEnabled = outgoingValidationEnabled;
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
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    this.defaultTimeout = new_default;
  }

  @Override
  public int get_timeout(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    return (int) defaultTimeout;
  }

  @Override
  public void set_timeout(Request aRequest, int new_lifetime)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    //Nothing to do here, as the query has already executed
  }

  @Override
  public void delete_request(Request aRequest)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    //Nothing to do here, we don't keep a list of requests. We work them as they are submitted.
  }

  @Override
  public SubmitQueryRequest submit_query(
      Query aQuery,
      String[] result_attributes,
      SortAttribute[] sort_attributes,
      NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    BqsConverter bqsConverter = new BqsConverter(filterBuilder, removeSourceLibrary);
    SubmitQueryRequestImpl submitQueryRequest =
        new SubmitQueryRequestImpl(
            aQuery, filterBuilder, bqsConverter, catalogFramework, querySources);
    submitQueryRequest.set_number_of_hits(maxNumResults);
    submitQueryRequest.setTimeout(defaultTimeout);
    submitQueryRequest.setOutgoingValidationEnabled(outgoingValidationEnabled);

    submitQueryRequest.setResultAttributes(result_attributes);

    String queryId = UUID.randomUUID().toString();
    try {
      poa.activate_object_with_id(
          queryId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), submitQueryRequest);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.debug("submit_query : Unable to activate submitQueryRequest object.", e);
    }

    org.omg.CORBA.Object obj =
        poa.create_reference_with_id(
            queryId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
            SubmitQueryRequestHelper.id());
    SubmitQueryRequest queryRequest = SubmitQueryRequestHelper.narrow(obj);
    return queryRequest;
  }

  @Override
  public HitCountRequest hit_count(Query aQuery, NameValue[] properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    //Force this to be an int per the NSILI API
    int numResults = (int) getResultCount(aQuery);

    HitCountRequestImpl hitCountRequest = new HitCountRequestImpl(numResults);

    String id = UUID.randomUUID().toString();

    try {
      poa.activate_object_with_id(
          id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), hitCountRequest);
    } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
      LOGGER.debug("hit_count : Unable to activate hitCountRequest object: {}", id, e);
    }

    org.omg.CORBA.Object obj =
        poa.create_reference_with_id(
            id.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), HitCountRequestHelper.id());
    HitCountRequest queryRequest = HitCountRequestHelper.narrow(obj);

    return queryRequest;
  }

  @Override
  public String[] get_property_names() throws ProcessingFault, SystemFault {
    //Per the spec throw NO_IMPLEMENT
    throw new NO_IMPLEMENT();
  }

  @Override
  public NameValue[] get_property_values(String[] desired_properties)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    //Per the spec throw NO_IMPLEMENT
    throw new NO_IMPLEMENT();
  }

  @Override
  public Library[] get_libraries() throws ProcessingFault, SystemFault {
    //Per the spec throw NO_IMPLEMENT
    throw new NO_IMPLEMENT();
  }

  protected long getResultCount(Query aQuery) {
    long resultCount = 0;

    BqsConverter bqsConverter = new BqsConverter(filterBuilder, removeSourceLibrary);
    Filter parsedFilter = bqsConverter.convertBQSToDDF(aQuery);

    //Always need to ask for the DEFAULT_TAG or we get non-resource metacards
    Filter resourceFilter =
        filterBuilder.allOf(
            parsedFilter,
            filterBuilder.attribute(Metacard.TAGS).is().like().text(Metacard.DEFAULT_TAG));

    if (!LibraryImpl.queryContainsStatus(aQuery.bqs_query)) {
      parsedFilter =
          filterBuilder.anyOf(
              resourceFilter,
              filterBuilder.allOf(
                  parsedFilter,
                  filterBuilder
                      .attribute(Metacard.TAGS)
                      .is()
                      .like()
                      .text(MetacardVersion.VERSION_TAG),
                  filterBuilder
                      .attribute(MetacardVersion.VERSION_TAGS)
                      .is()
                      .like()
                      .text(Metacard.DEFAULT_TAG),
                  filterBuilder
                      .attribute(MetacardVersion.ACTION)
                      .is()
                      .like()
                      .text(MetacardVersion.Action.DELETED.getKey())));
    }

    QueryImpl catalogQuery = new QueryImpl(parsedFilter);

    if (defaultTimeout > 0) {
      catalogQuery.setTimeoutMillis(defaultTimeout * 1000);
    }

    catalogQuery.setPageSize(1);

    QueryRequestImpl catalogQueryRequest = null;
    if (querySources == null || querySources.isEmpty()) {
      catalogQueryRequest = new QueryRequestImpl(catalogQuery);
    } else {
      catalogQueryRequest = new QueryRequestImpl(catalogQuery, false, querySources, null);
    }

    try {
      QueryCountCallable queryCallable = new QueryCountCallable(catalogQueryRequest);
      resultCount = NsiliEndpoint.getGuestSubject().execute(queryCallable);
    } catch (Exception e) {
      LOGGER.debug("Unable to query catalog", e);
    }

    return resultCount;
  }

  class QueryCountCallable implements Callable<Long> {
    QueryRequestImpl catalogQueryRequest;

    public QueryCountCallable(QueryRequestImpl catalogQueryRequest) {
      this.catalogQueryRequest = catalogQueryRequest;
    }

    @Override
    public Long call() throws Exception {
      QueryResponse queryResponse = catalogFramework.query(catalogQueryRequest);
      return queryResponse.getHits();
    }
  }
}
