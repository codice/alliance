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
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.security.service.SecurityServiceException;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.shiro.subject.ExecutionException;
import org.codice.alliance.nsili.common.BqsConverter;
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.DagParsingException;
import org.codice.alliance.nsili.common.GIAS.DayEvent;
import org.codice.alliance.nsili.common.GIAS.DayEventTime;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.LifeEvent;
import org.codice.alliance.nsili.common.GIAS.LifeEventType;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.QueryLifeSpan;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS.RequestManagerHelper;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.SubmitStandingQueryRequestPOA;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.StringDAGListHolder;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.common.datamodel.NsiliDataModel;
import org.codice.alliance.nsili.endpoint.LibraryImpl;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.endpoint.managers.RequestManagerImpl;
import org.codice.alliance.nsili.transformer.DAGConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.StringHolder;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.LoggerFactory;

public class SubmitStandingQueryRequestImpl extends SubmitStandingQueryRequestPOA {

  private static final long DEFAULT_UPDATE_RATE = 60L * 1000L;

  private static final int HOUR_MSEC = 60 * 60 * 1000;

  private static final int MINUTE_MSEC = 60 * 1000;

  private String id;

  private Map<String, Callback> callbacks = new HashMap<>();

  private BqsConverter bqsConverter;

  private List<String> resultAttributes = new ArrayList<>();

  private SortAttribute[] sortAttributes;

  private QueryLifeSpan lifespan;

  private NameValue[] properties;

  private String user = "alliance";

  private boolean paused = false;

  private ExecutionThread executionThread;

  private int pageSize = NsiliEndpoint.DEFAULT_MAX_NUM_RESULTS;

  private CatalogFramework catalogFramework;

  private FilterBuilder filterBuilder;

  private int maxPendingResults;

  private Date startDate = null;

  private Date endDate = null;

  private Filter bqsFilter = null;

  private Query query = null;

  private StandingQueryData standingQueryData = new StandingQueryData();

  private final Object callbackLockObj = new Object();

  private RequestManager requestManager;

  private long updateFrequencyMsec;

  private Set<String> querySources;

  private boolean outgoingValidationEnabled;

  private long maxWaitToStartTimeMsecs;

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(SubmitStandingQueryRequestImpl.class);

  public SubmitStandingQueryRequestImpl(
      Query aQuery,
      String[] resultAttributes,
      SortAttribute[] sortAttributes,
      QueryLifeSpan lifespan,
      NameValue[] properties,
      CatalogFramework catalogFramework,
      FilterBuilder filterBuilder,
      long defaultUpdateFrequencyMsec,
      Set<String> querySources,
      int maxPendingResults,
      boolean removeSourceLibrary,
      boolean outgoingValidationEnabled,
      long maxWaitToStartTimeMsecs) {
    id = UUID.randomUUID().toString();
    LOGGER.trace("SubmitStandingQueryRequestImpl created with id {}", id);
    if (resultAttributes != null) {
      this.resultAttributes.addAll(Arrays.asList(resultAttributes));
    }
    this.sortAttributes = sortAttributes;
    this.lifespan = lifespan;
    this.properties = properties;
    this.catalogFramework = catalogFramework;
    this.filterBuilder = filterBuilder;
    this.maxPendingResults = maxPendingResults;
    this.bqsConverter = new BqsConverter(filterBuilder, removeSourceLibrary);
    this.query = aQuery;
    if (querySources != null) {
      this.querySources = new HashSet<>(querySources);
    }
    this.bqsFilter = bqsConverter.convertBQSToDDF(aQuery);
    this.outgoingValidationEnabled = outgoingValidationEnabled;
    this.maxWaitToStartTimeMsecs = maxWaitToStartTimeMsecs;

    parseLifeSpan(lifespan);
    if (LOGGER.isTraceEnabled()) {
      printLifeSpan(lifespan);
    }

    if (resultAttributes != null && LOGGER.isDebugEnabled()) {
      LOGGER.debug("Requested result_attributes:");
      Arrays.stream(resultAttributes).forEach(attr -> LOGGER.debug(attr));
    }

    this.updateFrequencyMsec = defaultUpdateFrequencyMsec;

    executionThread = new ExecutionThread();
    executionThread.setUpdateRate(defaultUpdateFrequencyMsec);
    LOGGER.trace("Starting standing query");
    executionThread.start();
  }

  public String getId() {
    return id;
  }

  @Override
  public void set_number_of_hits(int hits)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    this.pageSize = hits;
  }

  @Override
  public int get_number_of_hits() throws ProcessingFault, SystemFault {
    LOGGER.trace("get_number_of_hits invoked - returning {}", standingQueryData.size());
    return standingQueryData.size();
  }

  @Override
  public int get_number_of_hits_in_interval(int interval)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    LOGGER.trace(
        "get_number_of_hits_in_interval invoked in interval {} - returning {}",
        interval,
        standingQueryData.getNumberOfHitsInInterval(interval));
    return standingQueryData.getNumberOfHitsInInterval(interval);
  }

  @Override
  public int get_number_of_intervals() throws ProcessingFault, SystemFault {
    LOGGER.trace(
        "get_number_of_intervals invoked - returning {}", standingQueryData.getNumberOfIntervals());
    return standingQueryData.getNumberOfIntervals();
  }

  @Override
  public void clear_all() throws ProcessingFault, SystemFault {
    LOGGER.trace("clear_all invoked");
    standingQueryData.clearAll();
  }

  @Override
  public void clear_intervals(int numIntervals)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    LOGGER.trace("clear_intervals invoked with {}", numIntervals);
    standingQueryData.clearIntervals(numIntervals);
  }

  @Override
  public void clear_before(Time relativeTime)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    long msecOffSet =
        (long) relativeTime.hour * HOUR_MSEC
            + relativeTime.minute * MINUTE_MSEC
            + ((int) relativeTime.second * 1000);
    LOGGER.trace("clear_before invoked with relative ms of {}", msecOffSet);
    standingQueryData.clearBefore(msecOffSet);
  }

  @Override
  public void pause() throws ProcessingFault, SystemFault {
    LOGGER.trace("puase invoked");
    this.paused = true;
  }

  @Override
  public void resume() throws ProcessingFault, SystemFault {
    this.paused = false;
    LOGGER.trace("resume invoked");
    executionThread.interrupt();
  }

  @Override
  public AbsTime get_time_last_executed() throws ProcessingFault, SystemFault {
    long lastExecMillis = executionThread.getLastCompletedExecutionTime();
    LOGGER.trace("get_time_last_executed invoked - returning {}", lastExecMillis);
    return ResultDAGConverter.getAbsTime(new Date(lastExecMillis));
  }

  @Override
  public AbsTime get_time_next_execution() throws ProcessingFault, SystemFault {
    long nextExecMillis = executionThread.getNextExecutionTime();
    LOGGER.trace("get_time_next_execution invoked - returning {}", nextExecMillis);
    return ResultDAGConverter.getAbsTime(new Date(nextExecMillis));
  }

  @Override
  public State complete_DAG_results(DAGListHolder results) throws ProcessingFault, SystemFault {
    LOGGER.trace("complete_dag_results invoked");
    if (standingQueryData.size() == 0) {
      try {
        LOGGER.trace("no results available - sleeping for {} ms", updateFrequencyMsec);
        Thread.sleep(updateFrequencyMsec);
      } catch (Exception ignore) {
      }
    }

    List<DAG> returnData = standingQueryData.getResultData(pageSize);
    LOGGER.debug("Retrieved {} DAG results", returnData.size());
    if (LOGGER.isTraceEnabled()) {
      returnData.forEach(
          dag -> {
            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace(DAGConverter.printDAG(dag));
            }
          });
    }

    results.value = returnData.toArray(new DAG[0]);
    if (standingQueryData.size() == 0) {
      LOGGER.trace("Returning IN_PROGRESS state");
      return State.IN_PROGRESS;
    } else {
      LOGGER.trace("Returning RESULTS_AVAILABLE state");
      return State.RESULTS_AVAILABLE;
    }
  }

  @Override
  public State complete_stringDAG_results(StringDAGListHolder results)
      throws ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT("String DAG Results not supported");
  }

  @Override
  public State complete_XML_results(StringHolder results) throws ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT("XML DAG Results are not supported");
  }

  @Override
  public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
    return new RequestDescription(user, lifespan.toString(), id, properties);
  }

  @Override
  public void set_user_info(String user)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    this.user = user;
  }

  @Override
  public Status get_status() throws ProcessingFault, SystemFault {
    if (!executionThread.isRunning()) {
      return new Status(State.CANCELED, false, "Request has been cancelled");
    }
    if (paused) {
      return new Status(State.SUSPENDED, false, "Processing is paused");
    }
    if (standingQueryData.size() > 0) {
      return new Status(State.RESULTS_AVAILABLE, false, "Results Available");
    } else {
      return new Status(State.PENDING, false, "No Results Available");
    }
  }

  @Override
  public DelayEstimate get_remaining_delay() throws ProcessingFault, SystemFault {
    long delayUntilNextExec = executionThread.getNextExecutionTime() - System.currentTimeMillis();
    int delayTimeSecs = (int) delayUntilNextExec / 1000;
    return new DelayEstimate(delayTimeSecs, delayTimeSecs > 0);
  }

  @Override
  public void cancel() throws ProcessingFault, SystemFault {
    LOGGER.trace("cancel invoked");
    executionThread.stopRunning();
  }

  @Override
  public String register_callback(Callback acallback)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    synchronized (callbackLockObj) {
      String id = UUID.randomUUID().toString();
      LOGGER.debug("Registering callback with id {}", id);
      callbacks.put(id, acallback);
      return id;
    }
  }

  @Override
  public void free_callback(String id) throws InvalidInputParameter, ProcessingFault, SystemFault {
    synchronized (callbackLockObj) {
      LOGGER.debug("Freeing callback with id {}", id);
      callbacks.remove(id);
    }
  }

  public void freeCallback(Callback callback) {
    synchronized (callbackLockObj) {
      LOGGER.debug("Freeing callback by value");
      callbacks.values().remove(callback);
    }
  }

  @Override
  public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
    LOGGER.trace("get_request_manager invoked");
    if (requestManager == null) {
      String requestManagerId = UUID.randomUUID().toString();
      RequestManagerImpl requestManagerImpl = new RequestManagerImpl();

      try {
        _poa()
            .activate_object_with_id(
                requestManagerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                requestManagerImpl);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.debug("Error activating RequestManager: ", e);
      }

      org.omg.CORBA.Object obj =
          _poa()
              .create_reference_with_id(
                  requestManagerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
                  RequestManagerHelper.id());

      requestManager = RequestManagerHelper.narrow(obj);
    }
    return requestManager;
  }

  class ExecutionThread extends Thread {

    private static final String UNABLE_TO_NOTIFY_CALLBACK = "Unable to notify callback";

    private boolean running = true;

    private long updateRate = DEFAULT_UPDATE_RATE;

    private long lastExecutionTime = 0;

    private long lastCompletedExecutionTime = 0;

    private boolean moreResultsAvailOnLastQuery = false;

    private int startIndex = 1;

    private QueryImpl catalogQuery = null;

    public boolean hasMoreResultsAvailOnLastQuery() {
      return moreResultsAvailOnLastQuery;
    }

    public void run() {
      LOGGER.trace("starting execution thread");
      while (running) {
        long queryTime = lastExecutionTime - 1000;

        // Don't want to change the query time until we process all of the results from the
        // last query
        if (!moreResultsAvailOnLastQuery && !paused) {
          lastExecutionTime = System.currentTimeMillis();
          LOGGER.trace(
              "No outstanding results to process - updating execution time to {}",
              lastExecutionTime);
        }

        if (endDate != null && lastExecutionTime > endDate.getTime()) {
          LOGGER.debug("Reached end of execution time -signalling complete");
          running = false;
          break;
        }

        if (startDate != null) {
          long now = System.currentTimeMillis();
          if (startDate.getTime() > now) {
            long waitToStart = startDate.getTime() - now;
            long waitSecs = TimeUnit.MILLISECONDS.toSeconds(waitToStart);
            waitToStart = Math.min(waitToStart, maxWaitToStartTimeMsecs);
            LOGGER.debug(
                "Start time for subscription is in the future, waiting {} seconds", waitSecs);
            try {
              Thread.sleep(waitToStart);
            } catch (InterruptedException exception) {
              LOGGER.info("Standing query interrupted - aborting.");
              Thread.currentThread().interrupt();
            }
          }
        }

        // Right now we don't produce the Association View
        if (!query.view.equals(NsiliConstants.NSIL_ASSOCIATION_VIEW) && !paused) {
          if (standingQueryData.size() <= maxPendingResults) {
            LOGGER.trace(
                "Room for more query results (current size: {} max size: {}",
                standingQueryData.size(),
                maxPendingResults);
            DAGQueryResult queryResult = getData(queryTime);
            if (queryResult != null
                && queryResult.getResults() != null
                && queryResult.getResults().size() > 0) {
              LOGGER.trace(
                  "Adding {} query results to standing query data structure",
                  queryResult.getResults().size());
              standingQueryData.add(queryResult);
            } else {
              LOGGER.trace("No data added to results");
            }
          }

          LOGGER.trace("StandingQueryData size: {}", standingQueryData.size());
          if (standingQueryData.size() > 0) {
            List<Callback> failedCallbacks = new ArrayList<>();
            LOGGER.trace("Iterating through {} callbacks to notify of results", callbacks.size());
            for (Callback callback : callbacks.values()) {
              try {
                if (standingQueryData.size() > 0) {
                  LOGGER.trace(
                      "Notifying callback that {} results are available", standingQueryData.size());
                  callback._notify(
                      org.codice.alliance.nsili.common.UCO.State.RESULTS_AVAILABLE,
                      get_request_description());
                }
              } catch (InvalidInputParameter | ProcessingFault | SystemFault fault) {
                LOGGER.debug(UNABLE_TO_NOTIFY_CALLBACK, fault);
              } catch (Exception e) {
                LOGGER.debug("Unable to notify callback", e);
                failedCallbacks.add(callback);
              }
            }

            LOGGER.trace("Freeing {} failed callbacks", failedCallbacks.size());
            failedCallbacks.stream().forEach(SubmitStandingQueryRequestImpl.this::freeCallback);
          }
          lastCompletedExecutionTime = System.currentTimeMillis();
        }

        // Don't sleep if more results available. Need client to pick up results as fast
        // as possible to catch up.
        if (!executionThread.hasMoreResultsAvailOnLastQuery()) {
          try {
            LOGGER.debug("No more results available yet - sleeping until next update");
            Thread.sleep(updateRate);
          } catch (Exception ignore) {
            LOGGER.debug("Interrupted sleep");
          }
        } else {
          LOGGER.debug("More results available - continuing to process");
        }
      }

      LOGGER.trace("Exiting execution thread run loop");
      synchronized (callbackLockObj) {
        LOGGER.debug("Clearing all callbacks");
        callbacks.clear();
      }
    }

    protected DAGQueryResult getData(long queryTime) {
      DAGQueryResult result = null;

      LOGGER.trace("getData called with queryTime of {}", queryTime);
      List<Result> catalogResults = new ArrayList<>();

      Filter parsedFilter = getFilter(queryTime);

      catalogQuery = new QueryImpl(parsedFilter);
      catalogQuery.setRequestsTotalResultsCount(true);
      catalogQuery.setPageSize(pageSize);
      SortBy sortBy = new SortByImpl(Core.CREATED, SortOrder.ASCENDING);
      catalogQuery.setSortBy(sortBy);
      if (moreResultsAvailOnLastQuery) {
        catalogQuery.setStartIndex(startIndex);
      }

      QueryRequestImpl catalogQueryRequest;
      catalogQueryRequest = getQueryRequest(parsedFilter);

      try {
        QueryResultsCallable queryCallable = new QueryResultsCallable(catalogQueryRequest);

        try {
          LOGGER.debug("Executing query...");
          QueryResponse queryResponse = NsiliEndpoint.getGuestSubject().execute(queryCallable);
          int numHits = (int) queryResponse.getHits();
          LOGGER.trace("Hits received: {}", numHits);
          List<Result> results = queryResponse.getResults();
          int origResultSize = results.size();
          LOGGER.trace("Query returned {} results", origResultSize);
          results = massageResults(results);
          int filteredResultSize = results.size();
          catalogResults.addAll(results);
          int accumResults = origResultSize + (startIndex - 1);

          LOGGER.trace(
              "Cleaned up results added to be processed: {}, total hits handled: {}",
              filteredResultSize,
              accumResults);

          if (accumResults < numHits) {
            moreResultsAvailOnLastQuery = true;
            startIndex = accumResults + 1;
          } else {
            moreResultsAvailOnLastQuery = false;
            startIndex = 1;
          }

          LOGGER.trace("Set startIndex to {}", startIndex);
        } catch (SecurityServiceException e) {
          LOGGER.debug("Unable to update subject on NSILI Library", e);
        }

      } catch (ExecutionException e) {
        LOGGER.debug("Unable to query catalog", e);
      }

      List<DAG> dags = new ArrayList<>();

      Map<String, List<String>> mandatoryAttributes = new HashMap<>();
      if (outgoingValidationEnabled) {
        NsiliDataModel nsiliDataModel = new NsiliDataModel();
        mandatoryAttributes = nsiliDataModel.getRequiredAttrsForView(NsiliConstants.NSIL_ALL_VIEW);
      }

      LOGGER.debug("Converting {} results to DAG format", catalogResults.size());
      for (Result catalogResult : catalogResults) {
        try {
          DAG dag =
              ResultDAGConverter.convertResult(
                  catalogResult, _orb(), _poa(), resultAttributes, mandatoryAttributes);
          dags.add(dag);
        } catch (DagParsingException dpe) {
          LOGGER.debug("DAG could not be parsed and will not be returned to caller:", dpe);
        }
      }

      if (!dags.isEmpty()) {
        LOGGER.debug(
            "Returning {} DAG results at time {}", dags.size(), System.currentTimeMillis());
        result = new DAGQueryResult(System.currentTimeMillis(), dags);
      }
      return result;
    }

    private QueryRequestImpl getQueryRequest(Filter parsedFilter) {
      QueryRequestImpl catalogQueryRequest;
      if (querySources == null || querySources.isEmpty()) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(
              "Query request will be local, no sources specified - CatalogQuery: {}",
              catalogQuery.toString());
        }
        catalogQueryRequest = new QueryRequestImpl(catalogQuery);
      } else {
        if (LOGGER.isTraceEnabled()) {
          String sourceList = querySources.stream().sorted().collect(Collectors.joining(", "));
          LOGGER.trace("Query will use the following sources: {}", sourceList);
        }
        catalogQueryRequest = new QueryRequestImpl(catalogQuery, false, querySources, null);
      }
      return catalogQueryRequest;
    }

    private Filter getFilter(long queryTime) {
      /*
       * Determine what types of records to query for. If the provided query has NSILI:CARD:status
       * specified, we only need to make sure we are pulling resources - the BQSConverter has
       * already handled the requested states.
       */
      boolean checkChanged = true;
      boolean checkObsolete = true;
      List<Filter> additionalChecks = new ArrayList<>();
      if (LibraryImpl.queryContainsStatus(query.bqs_query)) {
        checkChanged = false;
        checkObsolete = false;
      }

      Filter parsedFilter = bqsFilter;

      if (!moreResultsAvailOnLastQuery && queryTime > 0) {
        LOGGER.trace("Adding after modified time to BQS filter...");

        // add in the date/time constraint and the metacard-tags = resource constraint to
        // eliminate non-resource cards
        parsedFilter =
            filterBuilder.allOf(
                bqsFilter,
                filterBuilder.attribute(Metacard.MODIFIED).is().after().date(new Date(queryTime)),
                filterBuilder.attribute(Metacard.TAGS).is().like().text(Metacard.DEFAULT_TAG));

        // add in changed metacards if necessary
        if (checkChanged) {
          LOGGER.trace("Adding terms for changed metacards to BQS filter...");
          additionalChecks.add(
              filterBuilder.allOf(
                  filterBuilder
                      .attribute(Metacard.TAGS)
                      .is()
                      .like()
                      .text(MetacardVersion.VERSION_TAG),
                  filterBuilder
                      .attribute(MetacardVersion.ACTION)
                      .is()
                      .like()
                      .text(MetacardVersion.Action.VERSIONED.getKey()),
                  filterBuilder
                      .attribute(MetacardVersion.VERSIONED_ON)
                      .is()
                      .after()
                      .date(new Date(queryTime))));
        }

        // add in deleted metacards if necessary
        if (checkObsolete) {
          LOGGER.trace("Adding terms for obsolete/deleted metacards to BQS filter...");
          additionalChecks.add(
              filterBuilder.allOf(
                  filterBuilder
                      .attribute(Metacard.TAGS)
                      .is()
                      .like()
                      .text(MetacardVersion.VERSION_TAG),
                  filterBuilder
                      .attribute(MetacardVersion.VERSIONED_ON)
                      .is()
                      .after()
                      .date(new Date(queryTime)),
                  filterBuilder.anyOf(
                      filterBuilder
                          .attribute(MetacardVersion.ACTION)
                          .is()
                          .like()
                          .text(MetacardVersion.Action.DELETED.getKey()),
                      filterBuilder
                          .attribute(MetacardVersion.ACTION)
                          .is()
                          .like()
                          .text(MetacardVersion.Action.DELETED_CONTENT.getKey()))));
        }

        // OR in the changed and deleted if necessary
        if (additionalChecks.size() == 1) {
          parsedFilter = filterBuilder.anyOf(parsedFilter, additionalChecks.get(0));
        } else if (additionalChecks.size() == 2) {
          parsedFilter =
              filterBuilder.anyOf(parsedFilter, additionalChecks.get(0), additionalChecks.get(1));
        }
      }
      return parsedFilter;
    }

    public long getLastExecutionTime() {
      return lastExecutionTime;
    }

    public long getLastCompletedExecutionTime() {
      LOGGER.trace("getLastCompletedExecutionTime invoked");
      return lastCompletedExecutionTime;
    }

    public long getNextExecutionTime() {
      LOGGER.trace("getNextExecutionTime invoked");
      return lastCompletedExecutionTime + updateRate;
    }

    public void setUpdateRate(long updateRate) {
      this.updateRate = updateRate;
      interrupt();
    }

    public boolean isRunning() {
      return running;
    }

    public void stopRunning() {
      LOGGER.trace("stopRunning invoked");
      this.running = false;
      interrupt();
    }
  }

  /**
   * Split into three lists of results for new resources, updated resources, and deleted resources.
   * Then process each list in order to generate a set of results for further processing and
   * converting to DAG.
   *
   * @param results
   * @return
   */
  protected List<Result> massageResults(List<Result> results) {
    Map<String, Result> resourceRecords = new HashMap<>();
    Map<String, Result> versionedRecords = new HashMap<>();
    Map<String, Result> deletedRecords = new HashMap<>();
    Map<String, Result> resultsMap = new HashMap<>();
    Attribute attribute = null;
    String action = null;

    if (results != null) {
      for (Result result : results) {
        // Get id for original id for any revised card (updated or deleted)
        String metacardId = ResultDAGConverter.getMetacardId(result.getMetacard());
        attribute = result.getMetacard().getAttribute(MetacardVersion.ACTION);
        action = attribute == null ? null : (String) attribute.getValue();
        if (action == null) {
          // this is a normal resource that has been added to the system - use as is
          resultsMap.put(metacardId, result);
        } else if (action.contains(MetacardVersion.Action.VERSIONED.getKey())) {
          // if versioned, update original metacard with action of Versioned

          result.getMetacard().setAttribute(new AttributeImpl(Metacard.ID, metacardId));
          if (isNewer(result, versionedRecords.get(result.getMetacard().getId()))) {
            LOGGER.trace("New result found for id {} - updating map", metacardId);
            versionedRecords.put(metacardId, result);
          }
        } else {
          // for deleted, update with original id and save
          result.getMetacard().setAttribute(new AttributeImpl(Metacard.ID, metacardId));
          LOGGER.trace("Deleted result found with id {} - adding to list", metacardId);
          deletedRecords.put(metacardId, result);
        }
      }
    }

    // process the versioned list first in case we deleted the updated record
    for (String id : versionedRecords.keySet()) {
      if (isNewer(versionedRecords.get(id), resultsMap.get(id))) {
        Result result = resourceRecords.get(id);
        // if record exists, update in place with action and versioned-on fields
        if (result != null) {
          LOGGER.trace("Updating record from this resultset with changes for id {}", id);
          result
              .getMetacard()
              .setAttribute(
                  versionedRecords.get(id).getMetacard().getAttribute(MetacardVersion.ACTION));
          result
              .getMetacard()
              .setAttribute(
                  versionedRecords
                      .get(id)
                      .getMetacard()
                      .getAttribute(MetacardVersion.VERSIONED_ON));
          // move over updated record to resultsMap
          resultsMap.put(id, result);
        } else {
          LOGGER.trace("Versioned record not found in resource records - just adding this record.");
          resultsMap.put(id, versionedRecords.get(id));
        }
      } else {
        LOGGER.trace("New record for this update exists - ignoring");
      }
    }

    // just put all the deleted results over the top of existing ones
    for (String id : deletedRecords.keySet()) {
      resultsMap.put(id, deletedRecords.get(id));
    }

    LOGGER.trace("Returning list with {} entries", resultsMap.size());
    return new ArrayList<>(resultsMap.values());
  }

  /**
   * Returns true if the new result is newer (more recent) than the existing result. If the new
   * result is null this returns false. If the existing result is null, this returns true. If the
   * two results have the same timestamps, this returns false.
   *
   * @param newResult new result to compare timestamps against the existing result
   * @param existingResult existing result to be compared against
   * @return true if the newResult is newer than the existingResult
   */
  public static boolean isNewer(Result newResult, Result existingResult) {
    boolean newer = true;
    if (existingResult == null) {
      return true;
    }
    if (newResult == null) {
      return false;
    }

    Attribute attribute = existingResult.getMetacard().getAttribute(MetacardVersion.VERSIONED_ON);
    if (attribute == null) {
      return true;
    }
    Date existingDate = (Date) attribute.getValue();

    if (attribute == null) {
      return true;
    }

    attribute = newResult.getMetacard().getAttribute(MetacardVersion.VERSIONED_ON);
    if (attribute == null) {
      return false;
    }

    if (existingDate.compareTo((Date) attribute.getValue()) >= 0) {
      newer = false;
    }
    return newer;
  }

  protected void parseLifeSpan(QueryLifeSpan lifespan) {
    startDate = getDate(lifespan.start);
    endDate = getDate(lifespan.stop);
  }

  public static Date getDate(LifeEvent lifeEvent) {
    Date date = null;
    if (lifeEvent != null) {
      if (lifeEvent.discriminator() == LifeEventType.ABSOLUTE_TIME) {
        date = convertAbsTime(lifeEvent.at());
      } else if (lifeEvent.discriminator() == LifeEventType.DAY_EVENT_TIME) {
        date = convertDayEventTime(lifeEvent.day_event());
      } else if (lifeEvent.discriminator() == LifeEventType.RELATIVE_TIME) {
        long now = System.currentTimeMillis();
        Time relTime = lifeEvent.rt();
        long offsetMsec = convertTimeToMillis(relTime);
        long time = now + offsetMsec;
        date = new Date(time);
      } else if (lifeEvent.discriminator() == LifeEventType.NAMED_EVENT) {
        LOGGER.debug("NAMED_EVENT not a supported LifeEvent type");
      }
    }

    return date;
  }

  public static Date convertDayEventTime(DayEventTime dayEventTime) {
    DayEvent dayEvent = dayEventTime.day_event;
    Date convertedDate = null;
    long millis = 0;

    if (dayEvent == DayEvent.END_OF_MONTH) {
      LocalDateTime localDate = LocalDateTime.now().toLocalDate().atStartOfDay();
      LocalDateTime firstDayOfMonth =
          localDate.withDayOfMonth(localDate.toLocalDate().lengthOfMonth());
      millis = firstDayOfMonth.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if (dayEvent == DayEvent.FIRST_OF_MONTH) {
      LocalDateTime localDate = LocalDateTime.now().toLocalDate().atStartOfDay();
      LocalDateTime firstDayOfMonth = localDate.withDayOfMonth(1);
      millis = firstDayOfMonth.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if (dayEvent == DayEvent.SUN) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    } else if (dayEvent == DayEvent.MON) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    } else if (dayEvent == DayEvent.TUE) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    } else if (dayEvent == DayEvent.WED) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    } else if (dayEvent == DayEvent.THU) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    } else if (dayEvent == DayEvent.FRI) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    } else if (dayEvent == DayEvent.SAT) {
      LocalDateTime localDate = LocalDateTime.now();
      millis =
          localDate
              .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli();
    }

    if (millis > 0) {
      millis = millis + convertTimeToMillis(dayEventTime.time);
      convertedDate = new Date(millis);
    }

    return convertedDate;
  }

  public static long convertTimeToMillis(Time time) {
    return (long) time.hour * HOUR_MSEC + time.minute * MINUTE_MSEC + ((int) time.second * 1000);
  }

  private void printLifeSpan(QueryLifeSpan lifespan) {
    if (lifespan != null) {
      LifeEvent start = lifespan.start;
      LifeEvent stop = lifespan.stop;
      LifeEvent[] freq = lifespan.frequency;

      LOGGER.trace("Query Lifespan: ");
      LOGGER.trace("\tStart:");
      printLifeEvent(start);
      LOGGER.trace("\tStop:");
      printLifeEvent(stop);
      if (freq != null) {
        int count = 0;
        for (LifeEvent lifeEvent : freq) {
          LOGGER.trace("Freq[{}]: ", count);
          printLifeEvent(lifeEvent);
          count++;
        }
      }
    }
  }

  private void printLifeEvent(LifeEvent lifeEvent) {
    String discriminatorText = getLifeEventDiscriminator(lifeEvent.discriminator());
    Date atDate = null;
    String relTimeStr = null;
    String dayEventTimeStr = null;
    String dayEventStr = null;
    String event = null;
    if (lifeEvent.discriminator() == LifeEventType.ABSOLUTE_TIME) {
      AbsTime atTime = lifeEvent.at();
      atDate = convertAbsTime(atTime);
    } else if (lifeEvent.discriminator() == LifeEventType.RELATIVE_TIME) {
      Time relTime = lifeEvent.rt();
      relTimeStr = relTime.hour + ":" + relTime.minute + ":" + relTime.second;
    } else if (lifeEvent.discriminator() == LifeEventType.NAMED_EVENT) {
      event = lifeEvent.ev();
    } else if (lifeEvent.discriminator() == LifeEventType.DAY_EVENT_TIME) {
      DayEventTime dayEventTime = lifeEvent.day_event();
      dayEventStr = getDayEvent(dayEventTime.day_event);
      dayEventTimeStr =
          dayEventTime.time.hour + ":" + dayEventTime.time.minute + ":" + dayEventTime.time.second;
    }

    LOGGER.trace("\t\tDiscriminator: {}", discriminatorText);
    if (atDate != null) {
      LOGGER.trace("\t\tAt Time: {}", atDate);
    }
    LOGGER.trace("\t\tRel Time: {}", relTimeStr);
    LOGGER.trace("\t\tEvent: {}", event);
    LOGGER.trace("\t\tDayEvent: {}, time: {}", dayEventStr, dayEventTimeStr);
  }

  public static String getLifeEventDiscriminator(LifeEventType eventType) {
    if (eventType == LifeEventType.DAY_EVENT_TIME) {
      return "DAY_EVENT_TIME";
    } else if (eventType == LifeEventType.NAMED_EVENT) {
      return "NAMED_EVENT";
    } else if (eventType == LifeEventType.ABSOLUTE_TIME) {
      return "ABSOLUTE_TIME";
    } else if (eventType == LifeEventType.RELATIVE_TIME) {
      return "RELATIVE_TIME";
    }
    return "UNKNOWN : " + eventType.value();
  }

  public static String getDayEvent(DayEvent dayEvent) {
    if (dayEvent == DayEvent.END_OF_MONTH) {
      return "END_OF_MONTH";
    } else if (dayEvent == DayEvent.FIRST_OF_MONTH) {
      return "FIRST_OF_MONTH";
    } else if (dayEvent == DayEvent.SUN) {
      return "SUN";
    } else if (dayEvent == DayEvent.MON) {
      return "MON";
    } else if (dayEvent == DayEvent.TUE) {
      return "TUE";
    } else if (dayEvent == DayEvent.WED) {
      return "WED";
    } else if (dayEvent == DayEvent.THU) {
      return "THU";
    } else if (dayEvent == DayEvent.FRI) {
      return "FRI";
    } else if (dayEvent == DayEvent.SAT) {
      return "SAT";
    }

    return "UNKNOWN : " + dayEvent.value();
  }

  public int getPageSize() {
    return pageSize;
  }

  public static Date convertAbsTime(AbsTime absTime) {
    org.codice.alliance.nsili.common.UCO.Date ucoDate = absTime.aDate;
    org.codice.alliance.nsili.common.UCO.Time ucoTime = absTime.aTime;

    DateTime dateTime =
        new DateTime(
            (int) ucoDate.year,
            (int) ucoDate.month,
            (int) ucoDate.day,
            (int) ucoTime.hour,
            (int) ucoTime.minute,
            (int) ucoTime.second,
            0,
            DateTimeZone.UTC);
    return dateTime.toDate();
  }

  class QueryResultsCallable implements Callable<QueryResponse> {
    QueryRequest catalogQueryRequest;

    public QueryResultsCallable(QueryRequest catalogQueryRequest) {
      this.catalogQueryRequest = catalogQueryRequest;
    }

    @Override
    public QueryResponse call() throws Exception {
      return catalogFramework.query(catalogQueryRequest);
    }
  }
}
