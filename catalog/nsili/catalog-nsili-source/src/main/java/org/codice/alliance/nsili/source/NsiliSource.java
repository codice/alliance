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
package org.codice.alliance.nsili.source;

import ddf.catalog.data.ContentType;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.SourceResponseImpl;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.resource.ResourceReader;
import ddf.catalog.service.ConfiguredService;
import ddf.catalog.source.ConnectedSource;
import ddf.catalog.source.FederatedSource;
import ddf.catalog.source.SourceMonitor;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.util.impl.MaskableImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.cxf.helpers.IOUtils;
import org.codice.alliance.nsili.common.GIAS.AccessCriteria;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.CatalogMgr;
import org.codice.alliance.nsili.common.GIAS.CatalogMgrHelper;
import org.codice.alliance.nsili.common.GIAS.DataModelMgr;
import org.codice.alliance.nsili.common.GIAS.DataModelMgrHelper;
import org.codice.alliance.nsili.common.GIAS.HitCountRequest;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.LibraryDescription;
import org.codice.alliance.nsili.common.GIAS.LibraryHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.GIAS.OrderMgr;
import org.codice.alliance.nsili.common.GIAS.OrderMgrHelper;
import org.codice.alliance.nsili.common.GIAS.Polarity;
import org.codice.alliance.nsili.common.GIAS.ProductMgr;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.GIAS.RequirementMode;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequest;
import org.codice.alliance.nsili.common.GIAS.View;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.Nsili;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.codice.alliance.nsili.orb.api.CorbaServiceListener;
import org.codice.alliance.nsili.transformer.DAGConverter;
import org.codice.ddf.cxf.SecureCxfClientFactory;
import org.codice.ddf.spatial.ogc.catalog.common.AvailabilityCommand;
import org.codice.ddf.spatial.ogc.catalog.common.AvailabilityTask;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.opengis.filter.sort.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiliSource extends MaskableImpl
    implements FederatedSource, ConnectedSource, ConfiguredService, CorbaServiceListener {

  public static final String SERVER_PASSWORD = "serverPassword";

  public static final String SERVER_USERNAME = "serverUsername";

  public static final String CLIENT_TIMEOUT = "clientTimeout";

  public static final String ID = "id";

  public static final String KEY = "key";

  public static final String IOR_URL = "iorUrl";

  public static final String ADDITIONAL_QUERY_PARAMS = "additionalQueryParams";

  public static final String POLL_INTERVAL = "pollInterval";

  public static final String MAX_HIT_COUNT = "maxHitCount";

  public static final String ACCESS_USERID = "accessUserId";

  public static final String ACCESS_PASSWORD = "accessPassword";

  public static final String ACCESS_LICENSE_KEY = "accessLicenseKey";

  private static final Logger LOGGER = LoggerFactory.getLogger(NsiliSource.class);

  private static final String DESCRIBABLE_PROPERTIES_FILE = "/describable.properties";

  private static final String DESCRIPTION = "description";

  private static final String ORGANIZATION = "organization";

  private static final String VERSION = "version";

  private static final String TITLE = "name";

  private static final String WGS84 = "WGS84";

  private static final String GEOGRAPHIC_DATUM = "GeographicDatum";

  private static final String ASC = "ASC";

  private static final String CATALOG_MGR = "CatalogMgr";

  private static final String ORDER_MGR = "OrderMgr";

  private static final String PRODUCT_MGR = "ProductMgr";

  private static final String DATA_MODEL_MGR = "DataModelMgr";

  private static final String DEFAULT_USER_INFO = "Alliance";

  private static final String HTTP_SCHEME = "http";

  private static final String HTTPS_SCHEME = "https";

  private static final String FILE_SCHEME = "file";

  private static final String FTP_SCHEME = "ftp";

  private static Library library;

  private static Properties describableProperties = new Properties();

  /* Mandatory STANAG 4559 Managers */

  private CatalogMgr catalogMgr;

  private OrderMgr orderMgr;

  private ProductMgr productMgr;

  private DataModelMgr dataModelMgr;

  /* ---------------------------  */

  private AvailabilityTask availabilityTask;

  private String iorUrl;

  private String serverUsername;

  private String serverPassword;

  private int clientTimeout;

  private String sourceId;

  private String iorString;

  private Integer maxHitCount;

  private FilterAdapter filterAdapter;

  private org.omg.CORBA.ORB orb;

  private AccessCriteria accessCriteria;

  private Set<SourceMonitor> sourceMonitors = new HashSet<>();

  private ScheduledFuture<?> availabilityPollFuture;

  private ScheduledExecutorService scheduler;

  private Integer pollInterval;

  private String configurationPid;

  private SecureCxfClientFactory<Nsili> factory;

  private NsiliFilterDelegate nsiliFilterDelegate;

  private Set<ContentType> contentTypes = NsiliConstants.getContentTypes();

  private View[] views;

  private HashMap<String, List<AttributeInformation>> queryableAttributes;

  private HashMap<String, String[]> resultAttributes;

  private HashMap<String, List<String>> sortableAttributes;

  private String sourceDescription;

  private String ddfOrgName = DEFAULT_USER_INFO;

  private ResourceReader resourceReader;

  private String accessUserId = "";

  private String accessPassword = "";

  private String accessLicenseKey = "";

  private boolean excludeSortOrder = false;

  private boolean swapCoordinates = false;

  private String additionalQueryParams = "";

  private ExecutorService executorService;

  private CompletionService<Result> completionService;

  private CorbaOrb corbaOrb = null;

  private Object queryLockObj = new Object();

  private MetacardType nsiliMetacardType = null;

  static {
    try (InputStream properties =
        NsiliSource.class.getResourceAsStream(DESCRIBABLE_PROPERTIES_FILE)) {
      describableProperties.load(properties);
    } catch (IOException e) {
      LOGGER.info("Failed to load properties", e);
    }
  }

  /** Constructor used for testing. */
  NsiliSource(
      SecureCxfClientFactory factory,
      HashMap<String, String[]> resultAttributes,
      HashMap<String, List<String>> sortableAttributes,
      NsiliFilterDelegate filterDelegate,
      ORB orb) {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    this.factory = factory;
    this.resultAttributes = resultAttributes;
    this.nsiliFilterDelegate = filterDelegate;
    this.sortableAttributes = sortableAttributes;
    ddfOrgName = System.getProperty("org.codice.ddf.system.organization", DEFAULT_USER_INFO);
    this.orb = orb;
  }

  public NsiliSource(CorbaOrb corbaOrb) {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    setCorbaOrb(corbaOrb);
  }

  public void init() {
    corbaOrb.addCorbaServiceListener(this);
    initCorbaClient();
    setupAvailabilityPoll();
  }

  @Override
  public void corbaInitialized() {
    orb = corbaOrb.getOrb();
    initCorbaClient();
  }

  @Override
  public void corbaShutdown() {
    orb = null;
    corbaOrb = null;
  }

  private void createClientFactory() {
    int timeoutMsec = clientTimeout * 1000;
    if (StringUtils.isNotBlank(serverUsername) && StringUtils.isNotBlank(serverPassword)) {
      factory =
          new SecureCxfClientFactory(
              iorUrl,
              Nsili.class,
              null,
              null,
              true,
              true,
              timeoutMsec,
              timeoutMsec,
              serverUsername,
              serverPassword);
    } else {
      factory =
          new SecureCxfClientFactory(
              iorUrl, Nsili.class, null, null, true, true, timeoutMsec, timeoutMsec);
    }
  }

  /**
   * Initializes the Corba Client ORB and gets the mandatory interfaces that are required for a
   * STANAG 4559 complaint Federated Source, also queries the source for the queryable attributes
   * and views that it provides.
   */
  private void initCorbaClient() {
    getIorString();
    if (iorString != null) {
      initLibrary();
      setSourceDescription();
      initMandatoryManagers();
      initServerViews();
      initQueryableAttributes();
      initSortableAndResultAttributes();
      setFilterDelegate();

      LOGGER.debug("Initialized source {} with IOR: {}", getId(), iorString);
    }
  }

  /**
   * Determines which protocol is specified and attempts to retrive the IOR String appropriately.
   */
  private void getIorString() {
    URI uri;
    try {
      uri = new URI(iorUrl);
    } catch (URISyntaxException e) {
      LOGGER.debug("{} : Invalid URL specified for IOR string: {}", sourceId, iorUrl, e);
      return;
    }

    if (uri.getScheme().equals(HTTP_SCHEME) || uri.getScheme().equals(HTTPS_SCHEME)) {
      getIorStringFromHttpSource();
    } else if (uri.getScheme().equals(FTP_SCHEME)) {
      getIorStringFromFtpSource();
    } else if (uri.getScheme().equals(FILE_SCHEME)) {
      getIorStringFromLocalDisk();
    } else {
      LOGGER.debug("Invalid protocol specified for IOR string: {}", iorUrl);
    }

    if (StringUtils.isNotBlank(iorString)) {
      LOGGER.debug("{} : Successfully obtained IOR file from {}", getId(), iorUrl);
    } else {
      LOGGER.debug("{} : Received an empty or null IOR String.", sourceId);
    }
  }

  /** Obtains the IOR string from a local file. */
  private void getIorStringFromLocalDisk() {
    try (InputStream inputStream = new FileInputStream(iorUrl.substring(7))) {
      iorString = IOUtils.toString(inputStream, StandardCharsets.ISO_8859_1.name());
    } catch (IOException e) {
      LOGGER.debug("{} : Unable to process IOR String.", sourceId, e);
    }
  }

  /** Uses the SecureClientCxfFactory to obtain the IOR string from the provided URL via HTTP(S). */
  private void getIorStringFromHttpSource() {
    createClientFactory();
    Nsili nsili = factory.getClient();

    try (InputStream inputStream = nsili.getIorFile()) {
      iorString = IOUtils.toString(inputStream, StandardCharsets.ISO_8859_1.name());
      // Remove leading/trailing whitespace as the CORBA init can't handle that.
      iorString = iorString.trim();
    } catch (IOException e) {
      LOGGER.debug("{} : Unable to process IOR String.", sourceId, e);
    } catch (Exception e) {
      LOGGER.debug("{} : Error retrieving IOR file for {}.", sourceId, iorUrl, e);
    }
  }

  /** Uses FTPClient to obtain the IOR string from the provided URL via FTP. */
  private void getIorStringFromFtpSource() {
    URI uri = null;
    try {
      uri = new URI(iorUrl);
    } catch (URISyntaxException e) {
      LOGGER.debug("{} : Invalid URL specified for IOR string: {}", sourceId, iorUrl, e);
      return;
    }

    FTPClient ftpClient = new FTPClient();
    try {
      if (uri.getPort() > 0) {
        ftpClient.connect(uri.getHost(), uri.getPort());
      } else {
        ftpClient.connect(uri.getHost());
      }

      if (!ftpClient.login(serverUsername, serverPassword)) {
        LOGGER.debug("{} : FTP server log in unsuccessful.", sourceId);
      } else {
        int timeoutMsec = clientTimeout * 1000;
        ftpClient.setConnectTimeout(timeoutMsec);
        ftpClient.setControlKeepAliveReplyTimeout(timeoutMsec);
        ftpClient.setDataTimeout(timeoutMsec);

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        InputStream inputStream = ftpClient.retrieveFileStream(uri.getPath());

        iorString = IOUtils.toString(inputStream, StandardCharsets.ISO_8859_1.name());
        // Remove leading/trailing whitespace as the CORBA init can't handle that.
        iorString = iorString.trim();
      }
    } catch (Exception e) {
      LOGGER.debug("{} : Error retrieving IOR file for {}.", sourceId, iorUrl, e);
    }
  }

  /** Initializes the Root STANAG 4559 Library Interface */
  private void initLibrary() {
    if (iorString != null) {
      org.omg.CORBA.Object obj = orb.string_to_object(iorString);

      library = LibraryHelper.narrow(obj);
      if (library != null) {
        LOGGER.debug("{} : Initialized Library Interface", getId());
      } else {
        LOGGER.debug("{} : Unable to initialize the library interface.", getId());
      }
    }
  }

  /** Initializes all STANAG 4559 mandatory managers: CatalogMgr OrderMgr DataModelMgr ProductMgr */
  private void initMandatoryManagers() {
    try {
      accessCriteria = new AccessCriteria(accessUserId, accessPassword, accessLicenseKey);

      LibraryManager libraryManager = library.get_manager(CATALOG_MGR, accessCriteria);
      setCatalogMgr(CatalogMgrHelper.narrow(libraryManager));

      libraryManager = library.get_manager(ORDER_MGR, accessCriteria);
      setOrderMgr(OrderMgrHelper.narrow(libraryManager));

      libraryManager = library.get_manager(PRODUCT_MGR, accessCriteria);
      setProductMgr(ProductMgrHelper.narrow(libraryManager));

      libraryManager = library.get_manager(DATA_MODEL_MGR, accessCriteria);
      setDataModelMgr(DataModelMgrHelper.narrow(libraryManager));

    } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
      LOGGER.debug("{} : Unable to retrieve mandatory managers.", sourceId, e);
    }

    if (catalogMgr != null && orderMgr != null && productMgr != null && dataModelMgr != null) {
      LOGGER.debug("{} : Initialized STANAG mandatory managers.", getId());
    } else {
      LOGGER.debug("{} : Unable to initialize mandatory mangers.", getId());
    }
  }

  /**
   * Obtains all possible views that the Federated Source can provide. EX: NSIL_ALL_VIEW,
   * NSIL_IMAGERY According to ANNEX D, TABLE D-6, the passed parameter in get_view_names is an
   * empty list(not used).
   *
   * @return an array of views
   */
  private void initServerViews() {
    View[] views = null;
    try {
      views = dataModelMgr.get_view_names(new NameValue[0]);
    } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
      LOGGER.debug("{} : Unable to retrieve views.", sourceId, e);
    }
    if (views == null) {
      LOGGER.debug("{} : Unable to retrieve views.", sourceId);
    }
    this.views = views;
  }

  /**
   * Obtains all possible attributes for all possible views that the Federated Source can provide,
   * and populates a sortableAttributes map, as well as resultAttributes map that will be used for
   * querying the server. According to ANNEX D, TABLE D-6, the passed parameter in get_view_names is
   * an empty list(not used).
   *
   * @return a map of each view and the attributes provided by the source for that view
   */
  private void initSortableAndResultAttributes() {
    if (views == null || views.length == 0) {
      return;
    }
    HashMap<String, String[]> resultAttributesMap = new HashMap<>();
    HashMap<String, List<String>> sortableAttributesMap = new HashMap<>();

    try {
      for (int i = 0; i < views.length; i++) {

        List<String> sortableAttributesList = new ArrayList<>();

        AttributeInformation[] attributeInformationArray =
            dataModelMgr.get_attributes(views[i].view_name, new NameValue[0]);
        String[] resultAttributes = new String[attributeInformationArray.length];

        LOGGER.debug("Attributes for view: {}", views[i].view_name);

        for (int c = 0; c < attributeInformationArray.length; c++) {
          AttributeInformation attributeInformation = attributeInformationArray[c];
          resultAttributes[c] = attributeInformation.attribute_name;

          if (LOGGER.isDebugEnabled()) {
            getModeStr(attributeInformation);
          }

          if (attributeInformation.sortable) {
            sortableAttributesList.add(attributeInformation.attribute_name);
          }
        }
        sortableAttributesMap.put(views[i].view_name, sortableAttributesList);
        resultAttributesMap.put(views[i].view_name, resultAttributes);
      }
    } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
      LOGGER.debug("{} : Unable to retrieve queryable attributes.", sourceId, e);
    }

    if (resultAttributesMap.size() == 0) {
      LOGGER.debug("{} : Received empty attributes list from STANAG source.", getId());
    }

    this.sortableAttributes = sortableAttributesMap;
    this.resultAttributes = resultAttributesMap;
  }

  private void getModeStr(AttributeInformation attributeInformation) {
    if (attributeInformation.mode == RequirementMode.MANDATORY) {
      String modeStr = getMode(attributeInformation.mode);
      LOGGER.debug(
          "\t {} mode: {}, sortable: {}",
          attributeInformation.attribute_name,
          modeStr,
          String.valueOf(attributeInformation.sortable));
    } else if (LOGGER.isTraceEnabled()) {
      String modeStr = getMode(attributeInformation.mode);
      LOGGER.trace(
          "\t {} mode: {}, sortable: {}",
          attributeInformation.attribute_name,
          modeStr,
          String.valueOf(attributeInformation.sortable));
    }
  }

  /**
   * Obtains all queryable attributes for all possible views that the Federated Source can provide.
   * According to ANNEX D, TABLE D-6, the passed parameter in get_view_names is an empty list(not
   * used).
   *
   * @return a map of each view and the queryable attributes provided by the source for that view
   */
  private void initQueryableAttributes() {
    if (views == null || views.length == 0) {
      return;
    }
    HashMap<String, List<AttributeInformation>> map = new HashMap<>();

    try {
      for (int i = 0; i < views.length; i++) {
        AttributeInformation[] attributeInformationArray =
            dataModelMgr.get_queryable_attributes(views[i].view_name, new NameValue[0]);
        List<AttributeInformation> attributeInformationList = new ArrayList<>();
        for (int c = 0; c < attributeInformationArray.length; c++) {
          attributeInformationList.add(attributeInformationArray[c]);
        }
        map.put(views[i].view_name, attributeInformationList);
      }
    } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
      LOGGER.debug("{} : Unable to retrieve queryable attributes.", sourceId, e);
    }

    if (map.size() == 0) {
      LOGGER.debug("{} : Received empty queryable attributes from STANAG source.", getId());
    }
    queryableAttributes = map;
  }

  /**
   * Obtains the sourceDescription of the source from the Library interface.
   *
   * @return a sourceDescription of the source
   */
  private void setSourceDescription() {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      LibraryDescription libraryDescription = library.get_library_description();
      stringBuilder.append(libraryDescription.library_name + " : ");
      stringBuilder.append(libraryDescription.library_description);
    } catch (ProcessingFault | SystemFault e) {
      LOGGER.debug("{} : Unable to retrieve source sourceDescription.", sourceId, e);
    }
    String description = stringBuilder.toString();
    if (StringUtils.isBlank(description)) {
      LOGGER.debug("{} :  Unable to retrieve source sourceDescription.", getId());
    }
    this.sourceDescription = description;
  }

  public void destroy() {
    if (corbaOrb != null) {
      corbaOrb.removeCorbaServiceListener(this);
    }
    availabilityPollFuture.cancel(true);
    scheduler.shutdownNow();
  }

  public void refresh(Map<String, Object> configuration) {
    LOGGER.debug("Entering Refresh : {}", getId());

    if (MapUtils.isEmpty(configuration)) {
      LOGGER.debug("{} : Received null or empty configuration during refresh.", getId());
      return;
    }

    String serverUsername = (String) configuration.get(SERVER_USERNAME);
    if (StringUtils.isNotBlank(serverUsername) && !serverUsername.equals(this.serverUsername)) {
      setServerUsername(serverUsername);
    }
    String serverPassword = (String) configuration.get(SERVER_PASSWORD);
    if (StringUtils.isNotBlank(serverPassword) && !serverPassword.equals(this.serverPassword)) {
      setServerPassword(serverPassword);
    }
    Integer clientTimeout = (Integer) configuration.get(CLIENT_TIMEOUT);
    if (clientTimeout != null && clientTimeout != this.clientTimeout) {
      setClientTimeout(clientTimeout);
    }
    String id = (String) configuration.get(ID);
    if (StringUtils.isNotBlank(id) && !id.equals(this.sourceId)) {
      setId(id);
    }
    String iorUrl = (String) configuration.get(IOR_URL);
    if (StringUtils.isNotBlank(iorUrl) && !iorUrl.equals(this.iorUrl)) {
      setIorUrl(iorUrl);
    }

    String additionalQueryParams = (String) configuration.get(ADDITIONAL_QUERY_PARAMS);
    setAdditionalQueryParams(additionalQueryParams);

    Integer pollInterval = (Integer) configuration.get(POLL_INTERVAL);
    if (pollInterval != null && !pollInterval.equals(this.pollInterval)) {
      setPollInterval(pollInterval);
    }
    Integer maxHitCount = (Integer) configuration.get(MAX_HIT_COUNT);
    if (maxHitCount != null && !maxHitCount.equals(this.maxHitCount)) {
      setMaxHitCount(maxHitCount);
    }
    String accessUserId = (String) configuration.get(ACCESS_USERID);
    if (StringUtils.isNotBlank(accessUserId)) {
      setAccessUserId(accessUserId);
    }
    String accessPassword = (String) configuration.get(ACCESS_PASSWORD);
    if (StringUtils.isNotBlank(accessPassword)) {
      setAccessPassword(accessPassword);
    }
    String accessLicenseKey = (String) configuration.get(ACCESS_LICENSE_KEY);
    if (StringUtils.isNotBlank(accessLicenseKey)) {
      setAccessLicenseKey(accessLicenseKey);
    }
    init();
  }

  @Override
  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append(describableProperties.getProperty(DESCRIPTION))
        .append(System.getProperty(System.lineSeparator()))
        .append(sourceDescription);
    return sb.toString();
  }

  @Override
  public String getId() {
    String sourceId = super.getId();
    return sourceId;
  }

  @Override
  public SourceResponse query(QueryRequest queryRequest) throws UnsupportedQueryException {
    org.codice.alliance.nsili.common.GIAS.Query query = createQuery(queryRequest.getQuery());

    String[] results = resultAttributes.get(NsiliConstants.NSIL_ALL_VIEW);

    SortAttribute[] sortAttributes = getSortAttributes(queryRequest.getQuery().getSortBy());
    NameValue[] propertiesList = getDefaultPropertyList();
    LOGGER.debug(
        "{} : Sending BQS query to source.\n Sort Attributes : {}", getId(), sortAttributes);
    return submitQuery(queryRequest, query, results, sortAttributes, propertiesList);
  }

  /**
   * Uses the NsiliFilterDelegate to create a STANAG 4559 BQS (Boolean Syntax Query) from the DDF
   * Query
   *
   * @param query - the query recieved from the Search-Ui
   * @return - a STANAG4559 complaint query
   * @throws UnsupportedQueryException
   */
  private org.codice.alliance.nsili.common.GIAS.Query createQuery(Query query)
      throws UnsupportedQueryException {
    String filter = createFilter(query);
    if (StringUtils.isNotBlank(additionalQueryParams)) {
      filter = filter + " " + additionalQueryParams;
    }
    LOGGER.debug("{} : BQS Query : {}", getId(), filter);
    return new org.codice.alliance.nsili.common.GIAS.Query(NsiliConstants.NSIL_ALL_VIEW, filter);
  }

  /**
   * Obtains the number of hits that the given query has received from the server.
   *
   * @param query - a BQS query
   * @param properties - a list of properties for the query
   * @return - the hit count
   */
  private int getHitCount(
      org.codice.alliance.nsili.common.GIAS.Query query, NameValue[] properties) {
    IntHolder intHolder = new IntHolder();
    try {
      synchronized (queryLockObj) {
        HitCountRequest hitCountRequest = catalogMgr.hit_count(query, properties);
        hitCountRequest.complete(intHolder);
      }
    } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
      LOGGER.debug(
          "{} : Unable to get hit count for query. : {}",
          getId(),
          NsilCorbaExceptionUtil.getExceptionDetails(e),
          e);
    }

    LOGGER.debug("{} :  Received {} hit(s) from query.", getId(), intHolder.value);
    return intHolder.value;
  }

  /**
   * Submits and completes a BQS Query to the STANAG 4559 server and returns the response.
   *
   * @param queryRequest - the query request generated from the search
   * @param query - a BQS query
   * @param resultAttributes - a list of desired result attributes
   * @param sortAttributes - a list of attributes to sort by
   * @param properties - a list of properties for the query
   * @return - the server's response
   */
  private SourceResponse submitQuery(
      QueryRequest queryRequest,
      org.codice.alliance.nsili.common.GIAS.Query query,
      String[] resultAttributes,
      SortAttribute[] sortAttributes,
      NameValue[] properties) {
    DAGListHolder dagListHolder = new DAGListHolder();

    SourceResponseImpl sourceResponse = null;

    long numHits = 0;
    try {
      synchronized (queryLockObj) {
        LOGGER.debug("{} : Submit query: {}", sourceId, query.bqs_query);
        LOGGER.debug(
            "{} : Requesting result attributes: {}", sourceId, Arrays.toString(resultAttributes));
        LOGGER.debug("{} : Sort Attributes: {}", sourceId, Arrays.toString(sortAttributes));
        LOGGER.debug("{} : Properties: {}", sourceId, Arrays.toString(properties));
        HitCountRequest hitCountRequest = catalogMgr.hit_count(query, properties);
        IntHolder hitHolder = new IntHolder();
        hitCountRequest.complete(hitHolder);
        numHits = hitHolder.value;
        SubmitQueryRequest submitQueryRequest;
        if (hitHolder.value > 1) {
          submitQueryRequest =
              catalogMgr.submit_query(query, resultAttributes, sortAttributes, properties);
        } else {
          submitQueryRequest =
              catalogMgr.submit_query(
                  query, resultAttributes, new SortAttribute[0], new NameValue[0]);
        }
        submitQueryRequest.set_user_info(ddfOrgName);
        submitQueryRequest.set_number_of_hits(maxHitCount);
        submitQueryRequest.complete_DAG_results(dagListHolder);
      }
    } catch (ProcessingFault | SystemFault | InvalidInputParameter e) {
      LOGGER.debug(
          "{} : Unable to query source. {}",
          sourceId,
          NsilCorbaExceptionUtil.getExceptionDetails(e),
          e);
    }

    if (dagListHolder.value != null) {
      List<Result> results = new ArrayList<>();
      String id = getId();
      List<Future> futures = new ArrayList<>(dagListHolder.value.length);

      for (DAG dag : dagListHolder.value) {
        Callable<Result> convertRunner =
            () -> {
              DAGConverter dagConverter = new DAGConverter(resourceReader);
              dagConverter.setNsiliMetacardType(nsiliMetacardType);
              Metacard card = dagConverter.convertDAG(dag, swapCoordinates, id);
              if (card != null) {
                if (LOGGER.isTraceEnabled()) {
                  DAGConverter.logMetacard(card, getId());
                }
                return new ResultImpl(card);
              } else {
                LOGGER.debug(
                    "{} : Unable to convert DAG to metacard, returned card is null", getId());
              }
              return null;
            };
        futures.add(completionService.submit(convertRunner));
      }

      Future<Result> completedFuture;
      while (!futures.isEmpty()) {
        try {
          completedFuture = completionService.take();
          futures.remove(completedFuture);
          results.add(completedFuture.get());
        } catch (ExecutionException e) {
          LOGGER.debug("Unable to create result.", e);
        } catch (InterruptedException ignore) {
          // ignore
        }
      }

      sourceResponse = new SourceResponseImpl(queryRequest, results, numHits);

    } else {
      LOGGER.debug("{} : Source returned empty DAG list", getId());
    }

    return sourceResponse;
  }

  private void setFilterDelegate() {
    nsiliFilterDelegate =
        new NsiliFilterDelegate(queryableAttributes, NsiliConstants.NSIL_ALL_VIEW);
  }

  @Override
  public void maskId(String newSourceId) {
    final String methodName = "maskId";
    LOGGER.debug("ENTERING: {} with sourceId = {}", methodName, newSourceId);
    if (newSourceId != null) {
      super.maskId(newSourceId);
    }
    LOGGER.debug("EXITING: {}", methodName);
  }

  @Override
  public String getOrganization() {
    return describableProperties.getProperty(ORGANIZATION);
  }

  @Override
  public String getTitle() {
    return describableProperties.getProperty(TITLE);
  }

  @Override
  public String getVersion() {
    return describableProperties.getProperty(VERSION);
  }

  @Override
  public boolean isAvailable() {
    return availabilityTask.isAvailable();
  }

  @Override
  public boolean isAvailable(SourceMonitor sourceMonitor) {
    sourceMonitors.add(sourceMonitor);
    return isAvailable();
  }

  @Override
  public Set<String> getOptions(Metacard arg0) {
    return null;
  }

  @Override
  public Set<String> getSupportedSchemes() {
    return null;
  }

  @Override
  public ResourceResponse retrieveResource(
      URI resourceUri, Map<String, Serializable> requestProperties)
      throws IOException, ResourceNotFoundException, ResourceNotSupportedException {
    LOGGER.debug(
        "{}, {}, {}, {}",
        resourceUri.getHost(),
        resourceUri.getPath(),
        resourceUri.getPort(),
        requestProperties.toString());
    return resourceReader.retrieveResource(resourceUri, requestProperties);
  }

  @Override
  public String getConfigurationPid() {
    return configurationPid;
  }

  @Override
  public void setConfigurationPid(String configurationPid) {
    this.configurationPid = configurationPid;
  }

  @Override
  public Set<ContentType> getContentTypes() {
    return contentTypes;
  }

  private String createFilter(Query query) throws UnsupportedQueryException {
    String filter = filterAdapter.adapt(query, nsiliFilterDelegate);
    LOGGER.debug("Converted internal filter to BQS: {}", filter);
    return filter;
  }

  public void setServerUsername(String serverUsername) {
    this.serverUsername = serverUsername;
  }

  public void setServerPassword(String serverPassword) {
    this.serverPassword = serverPassword;
  }

  public Integer getClientTimeout() {
    return clientTimeout;
  }

  public void setClientTimeout(Integer clientTimeout) {
    this.clientTimeout = clientTimeout;
  }

  public void setId(String id) {
    this.sourceId = id;
    super.setId(id);
  }

  public void setIorUrl(String iorUrl) {
    if (iorUrl != null) {
      this.iorUrl = iorUrl.trim();
    }
  }

  public void setMaxHitCount(Integer maxHitCount) {
    this.maxHitCount = maxHitCount;
  }

  public void setCorbaOrb(CorbaOrb corbaOrb) {
    this.corbaOrb = corbaOrb;
    this.orb = corbaOrb.getOrb();
    corbaOrb.addCorbaServiceListener(this);
  }

  public String getIorUrl() {
    return iorUrl;
  }

  public String getServerPassword() {
    return serverPassword;
  }

  public String getServerUsername() {
    return serverUsername;
  }

  public Integer getMaxHitCount() {
    return maxHitCount;
  }

  public Integer getPollInterval() {
    return pollInterval;
  }

  public boolean getExcludeSortOrder() {
    return excludeSortOrder;
  }

  public void setExcludeSortOrder(boolean excludeSortOrder) {
    this.excludeSortOrder = excludeSortOrder;
  }

  public boolean getSwapCoordinates() {
    return swapCoordinates;
  }

  public void setSwapCoordinates(boolean swapCoordinates) {
    this.swapCoordinates = swapCoordinates;
  }

  public String getAdditionalQueryParams() {
    return additionalQueryParams;
  }

  public void setAdditionalQueryParams(String additionalQueryParams) {
    this.additionalQueryParams = additionalQueryParams;
  }

  public String getAccessUserId() {
    return accessUserId;
  }

  public void setAccessUserId(String accessUserId) {
    this.accessUserId = accessUserId;
  }

  public String getAccessPassword() {
    return accessPassword;
  }

  public void setAccessPassword(String accessPassword) {
    this.accessPassword = accessPassword;
  }

  public String getAccessLicenseKey() {
    return accessLicenseKey;
  }

  public void setAccessLicenseKey(String accessLicenseKey) {
    this.accessLicenseKey = accessLicenseKey;
  }

  public void setNumberWorkerThreads(int numberWorkerThreads) {
    List<Runnable> waitingTasks = null;
    if (executorService != null) {
      waitingTasks = executorService.shutdownNow();
    }

    executorService = Executors.newFixedThreadPool(numberWorkerThreads);
    completionService = new ExecutorCompletionService(executorService);
    if (waitingTasks != null) {
      for (Runnable task : waitingTasks) {
        executorService.submit(task);
      }
    }
  }

  public void setResourceReader(ResourceReader resourceReader) {
    this.resourceReader = resourceReader;
  }

  public void setPollInterval(Integer interval) {
    this.pollInterval = interval;
  }

  public void setFilterAdapter(FilterAdapter filterAdapter) {
    this.filterAdapter = filterAdapter;
  }

  public void setCatalogMgr(CatalogMgr catalogMgr) {
    this.catalogMgr = catalogMgr;
  }

  public void setOrderMgr(OrderMgr orderMgr) {
    this.orderMgr = orderMgr;
  }

  public void setDataModelMgr(DataModelMgr dataModelMgr) {
    this.dataModelMgr = dataModelMgr;
  }

  public void setProductMgr(ProductMgr productMgr) {
    this.productMgr = productMgr;
  }

  public void setSortableAttributes(HashMap<String, List<String>> sortableAttributes) {
    this.sortableAttributes = sortableAttributes;
  }

  public void setAvailabilityTask(AvailabilityTask availabilityTask) {
    this.availabilityTask = availabilityTask;
  }

  public void setupAvailabilityPoll() {
    LOGGER.debug(
        "Setting Availability poll task for {} minute(s) on Source {}", getPollInterval(), getId());
    Stanag4559AvailabilityCommand command = new Stanag4559AvailabilityCommand();
    long interval = TimeUnit.MINUTES.toMillis(getPollInterval());
    if (availabilityPollFuture == null || availabilityPollFuture.isCancelled()) {
      if (availabilityTask == null) {
        availabilityTask = new AvailabilityTask(interval, command, getId());
      } else {
        availabilityTask.setInterval(interval);
      }

      // Run the availability check immediately prior to scheduling it in a thread.
      // This is necessary to allow the catalog framework to have the correct
      // availability when the source is bound
      availabilityTask.run();
      // Schedule the availability check every 1 second. The actually call to
      // the remote server will only occur if the pollInterval has
      // elapsed.
      availabilityPollFuture =
          scheduler.scheduleWithFixedDelay(
              availabilityTask,
              AvailabilityTask.NO_DELAY,
              AvailabilityTask.ONE_SECOND,
              TimeUnit.SECONDS);
    } else {
      LOGGER.debug("No changes being made on the poller.");
    }
  }

  public void setNsiliMetacardType(MetacardType nsiliMetacardType) {
    this.nsiliMetacardType = nsiliMetacardType;
  }

  private void availabilityChanged(boolean isAvailable) {

    if (isAvailable) {
      LOGGER.info("STANAG 4559 source {} is available.", getId());
    } else {
      LOGGER.info("STANAG 4559 source {} is unavailable.", getId());
    }

    for (SourceMonitor monitor : this.sourceMonitors) {
      if (isAvailable) {
        LOGGER.debug("Notifying source monitor that STANAG 4559 source {} is available.", getId());
        monitor.setAvailable();
      } else {
        LOGGER.debug(
            "Notifying source monitor that STANAG 4559 source {} is unavailable.", getId());
        monitor.setUnavailable();
      }
    }
  }

  /**
   * Returns the Default Property List defined in the STANAG 4559 specification.
   *
   * @return - default WGS84 Geographic Datum.
   */
  private NameValue[] getDefaultPropertyList() {
    Any defaultAnyProperty = orb.create_any();
    defaultAnyProperty.insert_string(WGS84);
    NameValue[] result = {new NameValue(GEOGRAPHIC_DATUM, defaultAnyProperty)};
    return result;
  }

  /**
   * Sets a SortAttribute[] to be used in a query. The STANAG 4559 Spec has no mechanism to sort
   * queries by RELEVANCE or Shortest/Longest distance from a point, so they are ignored.
   *
   * @param sortBy - sortBy object specified in the Search UI
   * @return - an array of SortAttributes sent in the query to the source.
   */
  private SortAttribute[] getSortAttributes(SortBy sortBy) {
    if (excludeSortOrder) {
      return new SortAttribute[0];
    }

    if (sortBy == null || sortableAttributes == null) {
      // Default to sorting by Date/Time modified if no sorting provided
      return new SortAttribute[] {
        new SortAttribute(
            NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED, Polarity.DESCENDING)
      };
    }

    String sortAttribute = sortBy.getPropertyName().getPropertyName();
    Polarity sortPolarity;

    if (sortBy.getSortOrder().toSQL().equals(ASC)) {
      sortPolarity = Polarity.ASCENDING;
    } else {
      sortPolarity = Polarity.DESCENDING;
    }

    String cardDateTimeModifiedAttribute =
        NsiliConstants.NSIL_CARD + "." + NsiliConstants.DATE_TIME_MODIFIED;
    String cardSourceDateTimeModified =
        NsiliConstants.NSIL_CARD + "." + NsiliConstants.SOURCE_DATE_TIME_MODIFIED;
    String dateTimeDeclaredAttribute =
        NsiliConstants.NSIL_FILE + "." + NsiliConstants.DATE_TIME_DECLARED;

    if (sortAttribute.equals(Metacard.MODIFIED)) {
      List<SortAttribute> modifiedAttrs = new ArrayList<>();
      if (isAttributeSupported(cardDateTimeModifiedAttribute)) {
        modifiedAttrs.add(new SortAttribute(cardDateTimeModifiedAttribute, sortPolarity));
      }
      if (isAttributeSupported(cardSourceDateTimeModified)) {
        modifiedAttrs.add(new SortAttribute(cardSourceDateTimeModified, sortPolarity));
      }

      return modifiedAttrs.toArray(new SortAttribute[0]);
    } else if (sortAttribute.equals(Metacard.CREATED)
        && isAttributeSupported(dateTimeDeclaredAttribute)) {
      SortAttribute[] sortAttributeArray = {
        new SortAttribute(dateTimeDeclaredAttribute, sortPolarity)
      };
      return sortAttributeArray;
    } else {
      return new SortAttribute[0];
    }
  }

  /** Verifies that a given attribute exists in the list of sortableAttributes for NSIL_ALL_VIEW */
  private boolean isAttributeSupported(String attribute) {
    List<String> attributeInformationList = sortableAttributes.get(NsiliConstants.NSIL_ALL_VIEW);

    for (String sortableAttribute : attributeInformationList) {
      if (attribute.equals(sortableAttribute)) {
        return true;
      }
    }
    return false;
  }

  private String getMode(RequirementMode requirementMode) {
    if (requirementMode == RequirementMode.MANDATORY) {
      return "MANDATORY";
    } else if (requirementMode == RequirementMode.OPTIONAL) {
      return "OPTIONAL";
    } else {
      return String.valueOf(requirementMode);
    }
  }

  /**
   * Callback class to check the Availability of the NsiliSource.
   *
   * <p>NOTE: Ideally, the framework would call isAvailable on the Source and the SourcePoller would
   * have an AvailabilityTask that cached each Source's availability. Until that is done, allow the
   * command to handle the logic of managing availability.
   */
  private class Stanag4559AvailabilityCommand implements AvailabilityCommand {

    @Override
    public boolean isAvailable() {
      LOGGER.debug("Checking availability for source {} ", getId());
      boolean oldAvailability = NsiliSource.this.isAvailable();
      String[] managers = null;

      // Refresh IOR String when polling for availability in case server conditions change
      try {
        getIorString();
        initLibrary();
        managers = library.get_manager_types();
      } catch (Exception e) {
        LOGGER.debug("{} : Connection Failure for source.", getId(), e);
      }

      // If the IOR string is not valid, or the source cannot communicate with the library, the
      // source is unavailable
      boolean newAvailability = (managers != null && StringUtils.isNotBlank(iorString));
      if (oldAvailability != newAvailability) {
        availabilityChanged(newAvailability);
        // If the source becomes available, configure it.
        if (newAvailability) {
          initCorbaClient();
        }
      }
      return newAvailability;
    }
  }
}
