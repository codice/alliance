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
import ddf.catalog.filter.FilterBuilder;
import ddf.security.Subject;
import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.codice.alliance.core.email.EmailSender;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.endpoint.managers.EmailConfiguration;
import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.codice.alliance.nsili.orb.api.CorbaServiceListener;
import org.codice.ddf.security.common.Security;
import org.codice.ddf.security.handler.api.AuthenticationHandler;
import org.codice.ddf.security.handler.api.BaseAuthenticationToken;
import org.codice.ddf.security.handler.api.GuestAuthenticationToken;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiliEndpoint implements CorbaServiceListener, QuerySources {

  public static final String ENCODING = StandardCharsets.ISO_8859_1.name();

  public static final int DEFAULT_MAX_NUM_RESULTS = 500;

  private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";

  private int maxNumResults = DEFAULT_MAX_NUM_RESULTS;

  private ORB orb = null;

  private LibraryImpl library = null;

  private BundleContext context;

  private EmailSender emailSender;

  private CatalogFramework framework;

  private String iorString;

  private AuthenticationHandler securityHandler;

  private static SecurityManager securityManager;

  private FilterBuilder filterBuilder;

  private int defaultUpdateFrequencySec = 60;

  private long maxWaitToStartTimeSec = TimeUnit.MINUTES.toSeconds(5);

  private int maxPendingResults = 10000;

  private POA rootPOA = null;

  private CorbaOrb corbaOrb = null;

  private org.omg.CORBA.Object libraryRef = null;

  private boolean outgoingValidationEnabled = false;

  private Set<String> querySources = new HashSet<>();

  private String libraryVersion;

  private boolean removeSourceLibrary = true;

  private static Subject guestSubject = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(NsiliEndpoint.class);

  public NsiliEndpoint() {
    LOGGER.debug("NSILI Endpoint constructed");
  }

  public String getIorString() {
    return iorString;
  }

  public int getMaxNumResults() {
    return maxNumResults;
  }

  private void setLibraryQuerySources(Set<String> querySources) {
    Optional.ofNullable(library).ifPresent(l -> l.setQuerySources(querySources));
  }

  public void setMaxNumResults(int maxNumResults) {
    this.maxNumResults = maxNumResults;
    Optional.ofNullable(library).ifPresent(l -> l.setMaxNumResults(maxNumResults));
  }

  @Override
  public Set<String> getQuerySources() {
    return querySources;
  }

  /**
   * framework.getSourceIds() must contain all of the sourceIds in querySources. this.querySources
   * is set to an empty set when the parameter is null.
   *
   * @param querySources
   */
  public void setQuerySources(Set<String> querySources) {
    if (querySources == null) {
      this.querySources.clear();
    } else {
      if (querySources != null && framework.getSourceIds().containsAll(querySources)) {
        this.querySources.clear();
        this.querySources.addAll(querySources);
        setLibraryQuerySources(querySources);
      } else {
        LOGGER.debug(
            "The set of source ids to add to the querySources list must be nonnull and contain valid connected sources.");
      }
    }
  }

  @Override
  public void addQuerySource(String sourceId) {
    if (!querySources.contains(sourceId) && framework.getSourceIds().contains(sourceId)) {
      this.querySources.add(sourceId);
      setLibraryQuerySources(querySources);
    } else {
      LOGGER.debug(
          "The sourceId to add to the querySources list must be a valid connected source.");
    }
  }

  @Override
  public void removeQuerySource(String sourceId) {
    if (querySources.contains(sourceId)) {
      this.querySources.remove(sourceId);
      setLibraryQuerySources(querySources);
    }
  }

  public void setDefaultUpdateFrequencySec(int defaultUpdateFrequencySec) {
    this.defaultUpdateFrequencySec = defaultUpdateFrequencySec;
    if (library != null) {
      library.setDefaultUpdateFrequencyMsec(TimeUnit.SECONDS.toMillis(defaultUpdateFrequencySec));
    }
  }

  public void setMaxWaitToStartTimeMinutes(long maxWaitToStartTimeMinutes) {
    this.maxWaitToStartTimeSec = TimeUnit.MINUTES.toSeconds(maxWaitToStartTimeMinutes);
    if (library != null) {
      library.setMaxWaitToStartTimeMsecs(TimeUnit.MINUTES.toMillis(maxWaitToStartTimeMinutes));
    }
  }

  public void setSecurityHandler(AuthenticationHandler securityHandler) {
    this.securityHandler = securityHandler;
  }

  public void setSecurityManager(SecurityManager securityManager) {
    NsiliEndpoint.securityManager = securityManager;
    if (library != null) {
      library.setSecurityManager(securityManager);
    }
  }

  public BundleContext getContext() {
    return context;
  }

  public void setContext(BundleContext context) {
    this.context = context;
  }

  public CatalogFramework getFramework() {
    return framework;
  }

  public void setFramework(CatalogFramework framework) {
    this.framework = framework;
    if (library != null) {
      library.setCatalogFramework(framework);
    }
  }

  public void setFilterBuilder(FilterBuilder filterBuilder) {
    this.filterBuilder = filterBuilder;
    if (library != null) {
      library.setFilterBuilder(filterBuilder);
    }
  }

  public void setMaxPendingResults(int maxPendingResults) {
    this.maxPendingResults = maxPendingResults;
    if (library != null) {
      library.setMaxPendingResults(maxPendingResults);
    }
  }

  public void setCorbaOrb(CorbaOrb corbaOrb) {
    this.corbaOrb = corbaOrb;
    this.orb = corbaOrb.getOrb();
    corbaOrb.addCorbaServiceListener(this);
  }

  public void setLibraryVersion(String libraryVersion) {
    this.libraryVersion = libraryVersion;
    if (library != null) {
      library.setLibraryVersion(libraryVersion);
    }
  }

  public void setRemoveSourceLibrary(boolean removeSourceLibrary) {
    this.removeSourceLibrary = removeSourceLibrary;
    if (library != null) {
      library.setRemoveSourceLibrary(removeSourceLibrary);
    }
  }

  public void setOrb(ORB orb) {
    this.orb = orb;
  }

  public void setOutgoingValidationEnabled(boolean outgoingValidationEnabled) {
    this.outgoingValidationEnabled = outgoingValidationEnabled;
    if (library != null) {
      library.setOutgoingValidationEnabled(outgoingValidationEnabled);
    }
  }

  private EmailConfiguration emailConfiguration = new EmailConfiguration();

  /**
   * Method sets emailSender field of library
   *
   * @param emailSender: Cannot be null
   */
  public void setEmailSender(EmailSender emailSender) {
    notNull(emailSender, "emailSender must be non-null");

    this.emailConfiguration.setEmailSender(emailSender);
    if (library != null) {
      library.setEmailConfiguration(emailConfiguration);
    }
  }

  /** @param emailFrom must be non-null */
  public void setEmailFrom(String emailFrom) {
    notNull(emailFrom, "emailFrom must be non-null");

    this.emailConfiguration.setFromEmail(emailFrom);
    if (library != null) {
      library.setEmailConfiguration(emailConfiguration);
    }
  }

  /** @param emailSubject must be non-null */
  public void setEmailSubject(String emailSubject) {
    notNull(emailSubject, "emailSubject must be non-null");

    this.emailConfiguration.setSubject(emailSubject);
    if (library != null) {
      library.setEmailConfiguration(emailConfiguration);
    }
  }

  /** @param emailBody must be non-null */
  public void setEmailBody(String emailBody) {
    notNull(emailBody, "emailBody must be non-null");

    this.emailConfiguration.setBody(emailBody);
    if (library != null) {
      library.setEmailConfiguration(emailConfiguration);
    }
  }

  public void destroy() {
    LOGGER.debug("Destroying NSILI Endpoint");
    if (corbaOrb != null) {
      corbaOrb.removeCorbaServiceListener(this);
    }
    library = null;
    iorString = "";
  }

  @Override
  public void corbaInitialized() {
    try {
      orb = corbaOrb.getOrb();
      initCorba();
    } catch (InvalidName
        | AdapterInactive
        | WrongPolicy
        | ServantNotActive
        | IOException
        | SecurityServiceException e) {
      LOGGER.info("Unable to initialize Corba connection ", e);
    }
  }

  @Override
  public void corbaShutdown() {
    if (library != null && rootPOA != null && libraryRef != null) {
      try {
        rootPOA.deactivate_object(rootPOA.reference_to_id(libraryRef));
      } catch (ObjectNotActive | WrongPolicy | WrongAdapter e) {
        LOGGER.info("Unable to deactivate LibraryImpl", e);
      }
      rootPOA.destroy(true, true);
    }

    orb = null;
    library = null;
  }

  public void init() {
    LOGGER.debug("Initializing NSILI Endpoint");
    try {
      initCorba();
    } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
      LOGGER.info(
          "Unable to start the CORBA server. Set to DEBUG for a full stack trace {}",
          NsilCorbaExceptionUtil.getExceptionDetails(e));
      LOGGER.debug("CORBA server startup exception details", e);
    } catch (IOException e) {
      LOGGER.info("Unable to generate the IOR file.", e);
    } catch (SecurityServiceException e) {
      LOGGER.info("Unable to setup guest security credentials", e);
    }
  }

  public LibraryImpl getLibrary() {
    return library;
  }

  private void initCorba()
      throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException,
          SecurityServiceException {

    rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

    rootPOA.the_POAManager().activate();

    library = new LibraryImpl(rootPOA);
    library.setCatalogFramework(framework);
    library.setFilterBuilder(filterBuilder);
    library.setDefaultUpdateFrequencyMsec(TimeUnit.SECONDS.toMillis(defaultUpdateFrequencySec));
    library.setMaxPendingResults(maxPendingResults);
    library.setQuerySources(querySources);
    library.setLibraryVersion(libraryVersion);
    library.setRemoveSourceLibrary(removeSourceLibrary);
    library.setOutgoingValidationEnabled(outgoingValidationEnabled);
    library.setMaxWaitToStartTimeMsecs(TimeUnit.SECONDS.toMillis(maxWaitToStartTimeSec));
    library.setEmailConfiguration(emailConfiguration);

    libraryRef = rootPOA.servant_to_reference(library);

    iorString = orb.object_to_string(libraryRef);

    LOGGER.debug("Initialized NSILI Endpoint with IOR: {}", iorString);
  }

  public static synchronized Subject getGuestSubject() throws SecurityServiceException {
    if (guestSubject == null || Security.getInstance().tokenAboutToExpire(guestSubject)) {

      String ip = DEFAULT_IP_ADDRESS;
      try {
        ip = InetAddress.getLocalHost().getHostAddress();
        LOGGER.debug("Guest token ip: {}", ip);
      } catch (UnknownHostException e) {
        LOGGER.info("Could not get IP address for localhost", e);
      }

      String guestTokenId = ip;
      GuestAuthenticationToken guestToken =
          new GuestAuthenticationToken(BaseAuthenticationToken.ALL_REALM, guestTokenId);
      guestSubject = securityManager.getSubject(guestToken);
    }

    return guestSubject;
  }

  public static void setGuestSubject(Subject subject) {
    guestSubject = subject;
  }
}
