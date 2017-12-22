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
package org.codice.alliance.nsili.endpoint.requests;

import ddf.catalog.CatalogFramework;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.security.service.SecurityServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.shiro.subject.ExecutionException;
import org.codice.alliance.nsili.common.BqsConverter;
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.DagParsingException;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequestPOA;
import org.codice.alliance.nsili.common.GIAS._RequestManagerStub;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.StringDAGListHolder;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.datamodel.NsiliDataModel;
import org.codice.alliance.nsili.endpoint.LibraryImpl;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.omg.CORBA.NO_IMPLEMENT;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubmitQueryRequestImpl extends SubmitQueryRequestPOA {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubmitQueryRequestImpl.class);

  private int maxNumReturnedHits = NsiliEndpoint.DEFAULT_MAX_NUM_RESULTS;

  private Query query;

  private BqsConverter bqsConverter;

  private FilterBuilder filterBuilder;

  private long timeout = -1;

  private CatalogFramework catalogFramework;

  private int totalHitsReturned = 0;

  private Set<String> querySources = new HashSet<>();

  private Map<String, Callback> callbacks = new HashMap<>();

  private List<String> resultAttributes = new ArrayList<>();

  private boolean outgoingValidationEnabled;

  public SubmitQueryRequestImpl(
      Query query,
      FilterBuilder filterBuilder,
      BqsConverter bqsConverter,
      CatalogFramework catalogFramework,
      Set<String> querySources) {
    this.query = query;
    this.filterBuilder = filterBuilder;
    this.bqsConverter = bqsConverter;
    this.catalogFramework = catalogFramework;

    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public void set_number_of_hits(int hits)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    this.maxNumReturnedHits = hits;
  }

  public void setQuerySources(Set<String> querySources) {
    this.querySources.clear();
    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
  }

  public void setResultAttributes(String[] resultAttributes) {
    if (resultAttributes != null) {
      this.resultAttributes.addAll(Arrays.asList(resultAttributes));
    }
  }

  public void setOutgoingValidationEnabled(boolean outgoingValidationEnabled) {
    this.outgoingValidationEnabled = outgoingValidationEnabled;
  }

  @Override
  public State complete_DAG_results(DAGListHolder results) throws ProcessingFault, SystemFault {
    DAG[] noResults = new DAG[0];
    results.value = noResults;

    List<DAG> dags = new ArrayList<>();
    int totalHits = 0;
    List<Result> queryResults = getResults(query, totalHitsReturned);

    LOGGER.debug("Query: {} return NSILI results: {}", query.bqs_query, queryResults.size());

    Map<String, List<String>> mandatoryAttributes = new HashMap<>();
    if (outgoingValidationEnabled) {
      NsiliDataModel nsiliDataModel = new NsiliDataModel();
      mandatoryAttributes = nsiliDataModel.getRequiredAttrsForView(NsiliConstants.NSIL_ALL_VIEW);
    }
    for (Result result : queryResults) {
      try {
        DAG dag =
            ResultDAGConverter.convertResult(
                result, _orb(), _poa(), resultAttributes, mandatoryAttributes);
        if (dag != null) {
          dags.add(dag);
          totalHits++;
          totalHitsReturned++;
        }
      } catch (DagParsingException dpe) {
        LOGGER.debug("DAG could not be parsed and will not be returned to caller:", dpe);
      }

      if (totalHits >= maxNumReturnedHits) {
        break;
      }
    }
    if (!dags.isEmpty()) {
      results.value = dags.toArray(new DAG[0]);
      LOGGER.debug(
          "Number of results being returned: {}, requested: {}",
          results.value.length,
          maxNumReturnedHits);
    } else {
      LOGGER.debug("No results will be returned");
      results.value = noResults;
    }

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
    // This method is not expected to be called
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
    // This method is not expected to be called
  }

  @Override
  public String register_callback(Callback acallback)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    String id = UUID.randomUUID().toString();
    callbacks.put(id, acallback);

    return id;
  }

  @Override
  public void free_callback(String id) throws InvalidInputParameter, ProcessingFault, SystemFault {
    callbacks.remove(id);
  }

  @Override
  public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
    return new _RequestManagerStub();
  }

  private void notifyCallbacks() {
    if (!callbacks.isEmpty()) {
      for (Callback callback : callbacks.values()) {
        RequestDescription requestDescription = new RequestDescription();
        try {
          callback._notify(State.COMPLETED, requestDescription);
        } catch (InvalidInputParameter | SystemFault | ProcessingFault e) {
          LOGGER.debug(
              "Unable to notify callback {}", NsilCorbaExceptionUtil.getExceptionDetails(e), e);
        }
      }
    }
  }

  protected List<Result> getResults(Query aQuery, int offset) {
    List<Result> results = new ArrayList<>();

    Filter parsedFilter = bqsConverter.convertBQSToDDF(aQuery);

    // Always need to ask for the DEFAULT_TAG or we get non-resource metacards
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
    catalogQuery.setRequestsTotalResultsCount(false);
    catalogQuery.setPageSize(maxNumReturnedHits);

    if (offset > 0) {
      catalogQuery.setStartIndex(offset);
    }

    if (timeout > 0) {
      catalogQuery.setTimeoutMillis(timeout * 1000);
    }

    QueryRequestImpl catalogQueryRequest;
    if (querySources == null || querySources.isEmpty()) {
      catalogQueryRequest = new QueryRequestImpl(catalogQuery);
    } else {
      catalogQueryRequest = new QueryRequestImpl(catalogQuery, false, querySources, null);
    }

    try {
      QueryResultsCallable queryCallable = new QueryResultsCallable(catalogQueryRequest);
      List<Result> queryResults =
          LibraryImpl.getLatestResults(NsiliEndpoint.getGuestSubject().execute(queryCallable));
      results.addAll(queryResults);

    } catch (ExecutionException | SecurityServiceException e) {
      LOGGER.debug("Unable to query catalog", e);
    }

    return results;
  }

  class QueryResultsCallable implements Callable<List<Result>> {
    QueryRequestImpl catalogQueryRequest;

    public QueryResultsCallable(QueryRequestImpl catalogQueryRequest) {
      this.catalogQueryRequest = catalogQueryRequest;
    }

    @Override
    public List<Result> call() throws Exception {
      List<Result> results = new ArrayList<>();

      try {
        QueryResponse queryResponse = catalogFramework.query(catalogQueryRequest);
        if (queryResponse.getResults() != null) {
          results.addAll(queryResponse.getResults());
        }
        return results;
      } catch (Exception e) {
        LOGGER.debug("Unable to query catalog: {}", catalogQueryRequest.getQuery(), e);
        throw e;
      }
    }
  }
}
