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
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.security.service.SecurityServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.shiro.subject.ExecutionException;
import org.codice.alliance.nsili.common.GIAS.AccessManagerPOA;
import org.codice.alliance.nsili.common.GIAS.AvailabilityRequirement;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SetAvailabilityRequest;
import org.codice.alliance.nsili.common.GIAS._SetAvailabilityRequestStub;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.opengis.filter.Filter;
import org.slf4j.LoggerFactory;

public class AccessManagerImpl extends AccessManagerPOA {

  public static final int DEFAULT_TIMEOUT = -1;

  private static final int QUERY_AVAILABILITY_DELAY = 10;

  private static final int NUM_PRIORITIES = 1;

  private static final String ORDER_ACCESS_USE_MODE = "OrderAccess";

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AccessManagerImpl.class);

  private CatalogFramework catalogFramework;

  private FilterBuilder filterBuilder;

  private Set<String> querySources = new HashSet<>();

  private int defaultTimeout = DEFAULT_TIMEOUT;

  public AccessManagerImpl() {}

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setFilterBuilder(FilterBuilder filterBuilder) {
    this.filterBuilder = filterBuilder;
  }

  public void setQuerySources(Set<String> querySources) {
    this.querySources.clear();
    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
  }

  @Override
  public String[] get_use_modes() throws ProcessingFault, SystemFault {
    return new String[] {ORDER_ACCESS_USE_MODE};
  }

  @Override
  public boolean is_available(Product product, String use_mode)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    if (product != null) {
      Metacard metacard;
      try {
        metacard = getMetacard(product);
        if (metacard != null) {
          Attribute downloadUrlAttr = metacard.getAttribute(Metacard.RESOURCE_DOWNLOAD_URL);
          if (downloadUrlAttr != null) {
            return isUrlValid(downloadUrlAttr.getValue().toString());
          }
        }
      } catch (IOException | WrongPolicy | WrongAdapter e) {
        org.codice.alliance.nsili.common.UCO.exception_details exceptionDetails =
            new org.codice.alliance.nsili.common.UCO.exception_details();
        exceptionDetails.exception_name = e.getClass().getName();
        exceptionDetails.exception_desc = e.getMessage();
        throw new ProcessingFault(exceptionDetails);
      }
    }

    return false;
  }

  @Override
  public int query_availability_delay(
      Product product, AvailabilityRequirement availability_requirement, String use_mode)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
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
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    return new _SetAvailabilityRequestStub();
  }

  @Override
  public Request[] get_active_requests() throws ProcessingFault, SystemFault {
    return new Request[0];
  }

  @Override
  public int get_default_timeout() throws ProcessingFault, SystemFault {
    return defaultTimeout;
  }

  @Override
  public void set_default_timeout(int new_default)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    this.defaultTimeout = new_default;
  }

  @Override
  public int get_timeout(Request aRequest)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    return defaultTimeout;
  }

  @Override
  public void set_timeout(Request aRequest, int new_lifetime)
      throws InvalidInputParameter, ProcessingFault, SystemFault {}

  @Override
  public void delete_request(Request aRequest)
      throws InvalidInputParameter, ProcessingFault, SystemFault {}

  public String getProductId(Product product)
      throws UnsupportedEncodingException, WrongPolicy, WrongAdapter {
    byte[] productOidBytes = _poa().reference_to_id(product);
    return new String(productOidBytes, NsiliEndpoint.ENCODING);
  }

  public Metacard getMetacard(Product product)
      throws UnsupportedEncodingException, WrongAdapter, WrongPolicy {
    if (product != null) {
      return getMetacard(getProductId(product));
    }
    return null;
  }

  public Metacard getMetacard(String id) {
    Metacard metacard = null;
    List<Result> results = new ArrayList<>();

    Filter filter = filterBuilder.attribute(Metacard.ID).is().equalTo().text(id);

    QueryImpl catalogQuery = new QueryImpl(filter);
    catalogQuery.setRequestsTotalResultsCount(false);
    catalogQuery.setPageSize(10);

    QueryRequestImpl catalogQueryRequest;

    if (querySources == null || querySources.isEmpty()) {
      catalogQueryRequest = new QueryRequestImpl(catalogQuery);
    } else {
      catalogQueryRequest = new QueryRequestImpl(catalogQuery, false, querySources, null);
    }

    try {
      QueryResultsCallable queryCallable = new QueryResultsCallable(catalogQueryRequest);
      results.addAll(NsiliEndpoint.getGuestSubject().execute(queryCallable));

    } catch (ExecutionException | SecurityServiceException e) {
      LOGGER.debug("Unable to query catalog", e);
    }

    if (!results.isEmpty()) {
      metacard = results.iterator().next().getMetacard();
    }

    return metacard;
  }

  public boolean isUrlValid(String urlStr) {
    if (urlStr != null) {
      try {
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();

        int contentLength = urlConnection.getContentLength();
        if (contentLength > 0) {
          return true;
        }
      } catch (IOException ignore) {
      }
    }

    return false;
  }

  class QueryResultsCallable implements Callable<List<Result>> {
    QueryRequestImpl catalogQueryRequest;

    public QueryResultsCallable(QueryRequestImpl catalogQueryRequest) {
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
