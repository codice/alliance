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
package org.codice.alliance.nsili.endpoint;

import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterBuilder;
import ddf.security.service.SecurityManager;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.GIAS.AccessCriteria;
import org.codice.alliance.nsili.common.GIAS.CatalogMgrHelper;
import org.codice.alliance.nsili.common.GIAS.CreationMgrHelper;
import org.codice.alliance.nsili.common.GIAS.DataModelMgrHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryDescription;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.GIAS.LibraryManagerHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryPOA;
import org.codice.alliance.nsili.common.GIAS.OrderMgrHelper;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.GIAS.StandingQueryMgrHelper;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliManagerType;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.exception_details;
import org.codice.alliance.nsili.endpoint.managers.CatalogMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.CreationMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.DataModelMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.EmailConfiguration;
import org.codice.alliance.nsili.endpoint.managers.OrderMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.ProductMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.StandingQueryMgrImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

public class LibraryImpl extends LibraryPOA {

  public static final String CARD_STATUS = NsiliConstants.NSIL_CARD + "." + NsiliConstants.STATUS;

  private List<String> managers =
      Arrays.asList(
          NsiliManagerType.ORDER_MGR.getSpecName(),
          NsiliManagerType.CATALOG_MGR.getSpecName(),
          NsiliManagerType.CREATION_MGR.getSpecName(),
          NsiliManagerType.PRODUCT_MGR.getSpecName(),
          NsiliManagerType.DATA_MODEL_MGR.getSpecName(),
          NsiliManagerType.STANDING_QUERY_MGR.getSpecName()
          /* Optional :
          "QueryOrderMgr",
          "UpdateMgr" */ );

  private POA poa;

  private CatalogFramework catalogFramework;

  private EmailConfiguration emailConfiguration;

  private FilterBuilder filterBuilder;

  private int maxNumResults = NsiliEndpoint.DEFAULT_MAX_NUM_RESULTS;

  private long defaultUpdateFrequencyMsec;

  private int maxPendingResults;

  private boolean outgoingValidationEnabled;

  private Set<String> querySources = new HashSet<>();

  private String libraryVersion = "NSILI|3.2";

  private boolean removeSourceLibrary = true;

  private SecurityManager securityManager;

  private long maxWaitToStartTimeMsecs;

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LibraryImpl.class);

  public LibraryImpl(POA poa) {
    this.poa = poa;
  }

  public void setCatalogFramework(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  public void setMaxNumResults(int maxNumResults) {
    this.maxNumResults = maxNumResults;
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

  public void setQuerySources(Set<String> querySources) {
    this.querySources.clear();
    if (querySources != null) {
      this.querySources.addAll(querySources);
    }
  }

  public void setRemoveSourceLibrary(boolean removeSourceLibrary) {
    this.removeSourceLibrary = removeSourceLibrary;
  }

  public void setLibraryVersion(String libraryVersion) {
    this.libraryVersion = libraryVersion;
  }

  public void setSecurityManager(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  public void setOutgoingValidationEnabled(boolean outgoingValidationEnabled) {
    this.outgoingValidationEnabled = outgoingValidationEnabled;
  }

  public void setMaxWaitToStartTimeMsecs(long maxWaitToStartTimeMsecs) {
    this.maxWaitToStartTimeMsecs = maxWaitToStartTimeMsecs;
  }

  @Override
  public String[] get_manager_types() throws ProcessingFault, SystemFault {
    LOGGER.trace("get_manager_types() called");
    String[] managerArr = new String[managers.size()];
    return managers.toArray(managerArr);
  }

  @Override
  public LibraryManager get_manager(String manager_type, AccessCriteria access_criteria)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    org.omg.CORBA.Object obj;
    String managerId = UUID.randomUUID().toString();

    if (manager_type.equals(NsiliManagerType.CATALOG_MGR.getSpecName())) {
      obj = getCatalogMgrObject(managerId);
    } else if (manager_type.equals(NsiliManagerType.ORDER_MGR.getSpecName())) {
      obj = getOrderMgrObject(managerId);
    } else if (manager_type.equals(NsiliManagerType.PRODUCT_MGR.getSpecName())) {
      obj = getProductObject(managerId);
    } else if (manager_type.equals(NsiliManagerType.DATA_MODEL_MGR.getSpecName())) {
      obj = getDataModelMgrObject(managerId);
    } else if (manager_type.equals(NsiliManagerType.CREATION_MGR.getSpecName())) {
      obj = getCreationMgrObject(managerId);
    } else if (manager_type.equals(NsiliManagerType.STANDING_QUERY_MGR.getSpecName())) {
      obj = getStandingQueryMgrObject(managerId);
    } else {
      String[] bad_params = {manager_type};
      throw new InvalidInputParameter(
          "UnknownMangerType",
          new exception_details("UnknownMangerType", true, manager_type),
          bad_params);
    }

    LibraryManager libraryManager = LibraryManagerHelper.narrow(obj);

    LOGGER.trace("get_manager, type: {}, id: {}", manager_type, managerId);

    return libraryManager;
  }

  private Object getStandingQueryMgrObject(String managerId) {
    Object obj;
    StandingQueryMgrImpl standingQueryMgr = new StandingQueryMgrImpl(querySources);
    standingQueryMgr.setCatalogFramework(catalogFramework);
    standingQueryMgr.setFilterBuilder(filterBuilder);
    standingQueryMgr.setDefaultUpdateFrequencyMsec(defaultUpdateFrequencyMsec);
    standingQueryMgr.setMaxPendingResults(maxPendingResults);
    standingQueryMgr.setRemoveSourceLibrary(removeSourceLibrary);
    standingQueryMgr.setOutgoingValidationEnabled(outgoingValidationEnabled);
    standingQueryMgr.setMaxWaitToStartTimeMsecs(maxWaitToStartTimeMsecs);
    if (!CorbaUtils.isIdActive(poa, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        poa.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), standingQueryMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.info("Error activating StandingQueryMgr: ", e);
      }
    }

    obj =
        poa.create_reference_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)),
            StandingQueryMgrHelper.id());
    return obj;
  }

  private Object getCreationMgrObject(String managerId) {
    Object obj;
    CreationMgrImpl creationMgr = new CreationMgrImpl();
    if (!CorbaUtils.isIdActive(poa, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        poa.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), creationMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.info("Error activating CreationMgr: ", e);
      }
    }

    obj =
        poa.create_reference_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), CreationMgrHelper.id());
    return obj;
  }

  private Object getDataModelMgrObject(String managerId) {
    Object obj;
    DataModelMgrImpl dataModelMgr = new DataModelMgrImpl();
    if (!CorbaUtils.isIdActive(poa, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        poa.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), dataModelMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.info("Error activating DataModelMgr: ", e);
      }
    }

    obj =
        poa.create_reference_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), DataModelMgrHelper.id());
    return obj;
  }

  private Object getProductObject(String managerId) {
    Object obj;
    ProductMgrImpl productMgr = new ProductMgrImpl(querySources);
    productMgr.setCatalogFramework(catalogFramework);
    productMgr.setFilterBuilder(filterBuilder);
    productMgr.setOutgoingValidationEnabled(outgoingValidationEnabled);
    if (!CorbaUtils.isIdActive(poa, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        poa.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), productMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.info("Error activating ProductMgr: ", e);
      }
    }

    obj =
        poa.create_reference_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), ProductMgrHelper.id());
    return obj;
  }

  private Object getOrderMgrObject(String managerId) {
    Object obj;
    OrderMgrImpl orderMgr = new OrderMgrImpl();
    orderMgr.setCatalogFramework(catalogFramework);
    orderMgr.setFilterBuilder(filterBuilder);
    orderMgr.setEmailConfiguration(emailConfiguration);

    if (!CorbaUtils.isIdActive(poa, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        poa.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), orderMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.info("Error activating OrderMgr: ", e);
      }
    }

    obj =
        poa.create_reference_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), OrderMgrHelper.id());
    return obj;
  }

  private Object getCatalogMgrObject(String managerId) {
    Object obj;
    CatalogMgrImpl catalogMgr = new CatalogMgrImpl(poa, filterBuilder, querySources);
    catalogMgr.setCatalogFramework(catalogFramework);
    catalogMgr.setOutgoingValidationEnabled(outgoingValidationEnabled);
    catalogMgr.setRemoveSourceLibrary(removeSourceLibrary);
    if (!CorbaUtils.isIdActive(poa, managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)))) {
      try {
        poa.activate_object_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), catalogMgr);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.info("Error activating CatalogMgr: ", e);
      }
    }

    obj =
        poa.create_reference_with_id(
            managerId.getBytes(Charset.forName(NsiliEndpoint.ENCODING)), CatalogMgrHelper.id());
    return obj;
  }

  @Override
  public LibraryDescription get_library_description() throws ProcessingFault, SystemFault {
    LOGGER.trace("get_library_description called");
    String host = System.getProperty("org.codice.ddf.system.hostname");
    String country = System.getProperty("user.country");
    String organization = System.getProperty("org.codice.ddf.system.organization");
    String libraryDescr = country + "|" + organization;
    return new LibraryDescription(host, libraryDescr, libraryVersion);
  }

  @Override
  public LibraryDescription[] get_other_libraries(AccessCriteria access_criteria)
      throws ProcessingFault, InvalidInputParameter, SystemFault {
    LOGGER.trace("get_other_libraries called");
    throw new NO_IMPLEMENT();
  }

  public static List<Result> getLatestResults(List<Result> results) {
    Map<String, Result> resultMap = new HashMap<>();
    if (results == null) {
      return new ArrayList<>(resultMap.values());
    }
    for (Result result : results) {
      String metacardId = ResultDAGConverter.getMetacardId(result.getMetacard());
      Result mappedRes = resultMap.get(metacardId);
      if (mappedRes == null) {
        resultMap.put(metacardId, result);
      } else {
        if (mappedRes.getMetacard().getModifiedDate() != null) {
          if (result.getMetacard().getModifiedDate() != null
              && mappedRes
                      .getMetacard()
                      .getModifiedDate()
                      .compareTo(result.getMetacard().getModifiedDate())
                  < 0) {
            resultMap.put(metacardId, result);
          }
        } else {
          resultMap.put(metacardId, result);
        }
      }
    }
    return new ArrayList<>(resultMap.values());
  }

  public static boolean queryContainsStatus(String bqsQuery) {
    return bqsQuery.toLowerCase().contains(LibraryImpl.CARD_STATUS.toLowerCase());
  }

  /** @param emailConfiguration must be non-null */
  public void setEmailConfiguration(EmailConfiguration emailConfiguration) {
    notNull(emailConfiguration, "emailConfiguration must be non-null");
    this.emailConfiguration = emailConfiguration;
  }
}
