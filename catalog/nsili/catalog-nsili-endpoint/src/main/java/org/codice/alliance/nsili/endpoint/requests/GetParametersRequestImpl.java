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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.shiro.subject.ExecutionException;
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.GetParametersRequestPOA;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS._RequestManagerStub;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.DAGHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.StringDAGHolder;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.omg.CORBA.NO_IMPLEMENT;
import org.opengis.filter.Filter;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.security.Subject;

public class GetParametersRequestImpl extends GetParametersRequestPOA {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(GetParametersRequestImpl.class);

    private String productIdStr;

    private String[] desiredParameters;

    private CatalogFramework catalogFramework;

    private FilterBuilder filterBuilder;

    private Subject guestSubject;

    private List<String> querySources;

    public GetParametersRequestImpl(String productIdStr, String[] desiredParameters,
            CatalogFramework catalogFramework, FilterBuilder filterBuilder, Subject guestSubject,
            List<String> querySources) {
        this.productIdStr = productIdStr;
        this.desiredParameters = desiredParameters;
        this.catalogFramework = catalogFramework;
        this.filterBuilder = filterBuilder;
        this.guestSubject = guestSubject;
        this.querySources = querySources;
    }

    @Override
    public State complete(DAGHolder parameters) throws ProcessingFault, SystemFault {
        Filter filter = filterBuilder.attribute(Metacard.ID)
                .is()
                .equalTo()
                .text(productIdStr);
        Query query = new QueryImpl(filter);
        Result result = getResult(query);

        if (result != null) {
            if (desiredParameters != null) {
                if (isParamContained(desiredParameters, "ALL")) {
                    parameters.value = ResultDAGConverter.convertResult(result,
                            _orb(),
                            _poa(),
                            new ArrayList<>());
                } else if (isParamContained(desiredParameters, "CORE")) {
                    throw new NO_IMPLEMENT("CORE desired_parameter not supported");
                } else if (isParamContained(desiredParameters, "ORDER")) {
                    throw new NO_IMPLEMENT("ORDER desired_parameter not supported");
                } else {
                    parameters.value = ResultDAGConverter.convertResult(result,
                            _orb(),
                            _poa(),
                            Arrays.asList(desiredParameters));
                }
            } else {
                if (result != null) {
                    parameters.value = ResultDAGConverter.convertResult(result,
                            _orb(),
                            _poa(),
                            new ArrayList<>());
                }
            }
        }

        return State.COMPLETED;
    }

    @Override
    public State complete_StringDAG(StringDAGHolder parameters)
            throws ProcessingFault, SystemFault {
        throw new NO_IMPLEMENT("complete_StringDAG not implemented");
    }

    @Override
    public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
        return new RequestDescription();
    }

    @Override
    public void set_user_info(String message)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
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
    }

    @Override
    public String register_callback(Callback acallback)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        throw new NO_IMPLEMENT("Callbacks are not supported");
    }

    @Override
    public void free_callback(String id)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
    }

    @Override
    public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
        return new _RequestManagerStub();
    }

    private Result getResult(Query query) {
        QueryRequestImpl queryRequest;
        if (querySources == null || querySources.isEmpty()) {
            LOGGER.trace("Query request will be local, no sources specified");
            queryRequest = new QueryRequestImpl(query);
        } else {
            if (LOGGER.isTraceEnabled()) {
                String sourceList = querySources.stream()
                        .sorted()
                        .collect(Collectors.joining(", "));
                LOGGER.trace("Query will use the following sources: {}", sourceList);
            }

            queryRequest = new QueryRequestImpl(query, false, querySources, null);
        }
        Result result = null;
        try {
            QueryResultsCallable queryCallable = new QueryResultsCallable(queryRequest);
            List<Result> results = guestSubject.execute(queryCallable);
            if (results != null && !results.isEmpty()) {
                result = results.iterator()
                        .next();
            }

        } catch (ExecutionException e) {
            LOGGER.warn("Unable to query catalog {}",
                    NsilCorbaExceptionUtil.getExceptionDetails(e));
            LOGGER.debug("Catalog Query details", e);
        }

        return result;
    }

    private boolean isParamContained(String[] params, String param) {
        for (String paramValue : params) {
            if (paramValue.equals(param)) {
                return true;
            }
        }
        return false;
    }

    class QueryResultsCallable implements Callable<List<Result>> {
        QueryRequest catalogQueryRequest;

        public QueryResultsCallable(QueryRequest catalogQueryRequest) {
            this.catalogQueryRequest = catalogQueryRequest;
        }

        @Override
        public List<Result> call() throws Exception {
            List<Result> results = new ArrayList<>();

            QueryResponse queryResponse = catalogFramework.query(catalogQueryRequest);
            if (queryResponse.getResults() != null) {
                results.addAll(queryResponse.getResults());
            }
            return results;
        }
    }
}
