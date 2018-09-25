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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ddf.catalog.CatalogFramework;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.security.service.SecurityServiceException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.codice.alliance.core.email.EmailSender;
import org.codice.alliance.nsili.common.GIAS.AccessCriteria;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.NsiliManagerType;
import org.codice.alliance.nsili.orb.api.CorbaOrb;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class NsiliEndpointTest extends NsiliTestCommon {

  private static final int MAX_PENDING_RESULTS = 10000;

  private static final String VALID_SOURCE_ID = "Valid Source ID 2";

  private static final String INVALID_SOURCE_ID = "Invalid Source ID";

  private static final String[] FRAMEWORK_SOURCE_IDS =
      new String[] {"Valid Source ID 1", "Valid Source ID 2", "Valid Source ID 3"};

  private static final String[] INVALID_SOURCE_IDS =
      new String[] {"Invalid Source ID 1", "Valid Source ID 1"};

  private static final String[] VALID_SOURCE_IDS =
      new String[] {"Valid Source ID 3", "Valid Source ID 2"};

  private static final String[] VALID_ATTRIBUTE_OVERRIDES =
      new String[] {"NSIL_CARD.numberOfParts", "NSIL_COMMON.language"};

  private static final String[] VALID_ATTRIBUTE_EXCLUSIONS =
      new String[] {"NSIL_FILE.isProductLocal", "NSIL_RELATED_FILE.extent"};

  private NsiliEndpoint nsiliEndpoint;

  private AccessCriteria testAccessCriteria;

  private CorbaOrb mockCorbaOrb = mock(CorbaOrb.class);

  private String mailHost = "mailhost.dummy.com";

  @Before
  public void setUp()
      throws SecurityServiceException, AdapterInactive, InvalidName, ServantNotActive, WrongPolicy,
          IOException {
    setupCommonMocks();
    setupOrb();
    doReturn(orb).when(mockCorbaOrb).getOrb();
    testAccessCriteria = new AccessCriteria("", "", "");
    createEndpoint();
  }

  @After
  public void tearDown() {
    nsiliEndpoint.corbaShutdown();
    nsiliEndpoint.destroy();
    if (orb != null) {
      orb.destroy();
    }

    orb = null;
  }

  @Test
  public void testIORString() {
    String iorString = nsiliEndpoint.getIorString();
    assertThat(iorString, notNullValue());
  }

  @Test
  public void testEndpointManagers() throws Exception {
    String[] supportedManagerTypes = nsiliEndpoint.getLibrary().get_manager_types();
    assertThat(supportedManagerTypes.length, notNullValue());
  }

  @Test
  public void testGetCatalogMgr() throws Exception {
    LibraryManager catalogMgr =
        nsiliEndpoint
            .getLibrary()
            .get_manager(NsiliManagerType.CATALOG_MGR.getSpecName(), testAccessCriteria);
    assertThat(catalogMgr, notNullValue());
  }

  @Test
  public void testGetOrderMgr() throws Exception {
    LibraryManager orderMgr =
        nsiliEndpoint
            .getLibrary()
            .get_manager(NsiliManagerType.ORDER_MGR.getSpecName(), testAccessCriteria);
    assertThat(orderMgr, notNullValue());
  }

  @Test
  public void testGetProductMgr() throws Exception {
    LibraryManager productMgr =
        nsiliEndpoint
            .getLibrary()
            .get_manager(NsiliManagerType.PRODUCT_MGR.getSpecName(), testAccessCriteria);
    assertThat(productMgr, notNullValue());
  }

  @Test
  public void testGetDataModelMgr() throws Exception {
    LibraryManager dataModelMgr =
        nsiliEndpoint
            .getLibrary()
            .get_manager(NsiliManagerType.DATA_MODEL_MGR.getSpecName(), testAccessCriteria);
    assertThat(dataModelMgr, notNullValue());
  }

  @Test
  public void testCreationMgr() throws Exception {
    LibraryManager creationMgr =
        nsiliEndpoint
            .getLibrary()
            .get_manager(NsiliManagerType.CREATION_MGR.getSpecName(), testAccessCriteria);
    assertThat(creationMgr, notNullValue());
  }

  @Test
  public void testStandingQueryMgr() throws Exception {
    LibraryManager standingQueryMgr =
        nsiliEndpoint
            .getLibrary()
            .get_manager(NsiliManagerType.STANDING_QUERY_MGR.getSpecName(), testAccessCriteria);
    assertThat(standingQueryMgr, notNullValue());
  }

  @Test
  public void testMaxNumResults() throws Exception {
    nsiliEndpoint.setMaxNumResults(100);
    int setNumResults = nsiliEndpoint.getMaxNumResults();
    assertThat(setNumResults, is(100));
  }

  @Test
  public void testValidSetAttributeOverrides() throws Exception {
    Set<String> overrides = new HashSet<>(Arrays.asList(VALID_ATTRIBUTE_OVERRIDES));
    nsiliEndpoint.setAttributeOverrides(overrides);
    assertThat(nsiliEndpoint.getAttributeOverrides(), is(overrides));
  }

  @Test
  public void testValidSetAttributeExclusions() throws Exception {
    Set<String> exclusions = new HashSet<>(Arrays.asList(VALID_ATTRIBUTE_EXCLUSIONS));
    nsiliEndpoint.setAttributeExclusions(exclusions);
    assertThat(nsiliEndpoint.getAttributeExclusions(), is(exclusions));
  }

  @Test
  public void testValidSetQuerySources() throws Exception {
    Set<String> validSources = new HashSet<>(Arrays.asList(VALID_SOURCE_IDS));
    nsiliEndpoint.setQuerySources(validSources);
    assertThat(nsiliEndpoint.getQuerySources(), is(validSources));
  }

  @Test
  public void testInvalidSetQuerySources() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.setQuerySources(new HashSet<>(Arrays.asList(INVALID_SOURCE_IDS)));
    assertThat(nsiliEndpoint.getQuerySources(), is(existingSources));
  }

  @Test
  public void testNullSetQuerySources() throws Exception {
    nsiliEndpoint.setQuerySources(null);
    assertThat(nsiliEndpoint.getQuerySources(), IsEmptyCollection.empty());
  }

  @Test
  public void testValidAddQuerySource() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.addQuerySource(VALID_SOURCE_ID);
    assertThat(nsiliEndpoint.getQuerySources(), hasItem(VALID_SOURCE_ID));
    assertThat(nsiliEndpoint.getQuerySources(), containsAll(existingSources));
  }

  @Test
  public void testInvalidAddQuerySource() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.addQuerySource(INVALID_SOURCE_ID);
    assertThat(nsiliEndpoint.getQuerySources(), is(existingSources));
  }

  @Test
  public void testNullAddQuerySource() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.addQuerySource(null);
    assertThat(nsiliEndpoint.getQuerySources(), is(existingSources));
  }

  @Test
  public void testValidRemoveQuerySource() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.removeQuerySource(VALID_SOURCE_ID);
    assertThat(nsiliEndpoint.getQuerySources(), not(hasItem(VALID_SOURCE_ID)));
    assertThat(nsiliEndpoint.getQuerySources(), containsAll(existingSources));

    nsiliEndpoint.addQuerySource(VALID_SOURCE_ID);
    nsiliEndpoint.removeQuerySource(VALID_SOURCE_ID);

    assertThat(nsiliEndpoint.getQuerySources(), not(hasItem(VALID_SOURCE_ID)));
    assertThat(nsiliEndpoint.getQuerySources(), containsAll(existingSources));
  }

  @Test
  public void testInvalidRemoveQuerySource() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.removeQuerySource(INVALID_SOURCE_ID);
    assertThat(nsiliEndpoint.getQuerySources(), not(hasItem(INVALID_SOURCE_ID)));
    assertThat(nsiliEndpoint.getQuerySources(), containsAll(existingSources));

    nsiliEndpoint.addQuerySource(INVALID_SOURCE_ID);
    nsiliEndpoint.removeQuerySource(INVALID_SOURCE_ID);

    assertThat(nsiliEndpoint.getQuerySources(), not(hasItem(INVALID_SOURCE_ID)));
    assertThat(nsiliEndpoint.getQuerySources(), containsAll(existingSources));
  }

  @Test
  public void testNullRemoveQuerySource() throws Exception {
    Set<String> existingSources = new HashSet<>(nsiliEndpoint.getQuerySources());

    nsiliEndpoint.removeQuerySource(null);
    assertThat(nsiliEndpoint.getQuerySources(), is(existingSources));
  }

  @Test
  public void testSetFilterBuilder() throws Exception {
    GeotoolsFilterBuilder filterBuilder = new GeotoolsFilterBuilder();
    nsiliEndpoint.setFilterBuilder(filterBuilder);
  }

  @Test
  public void testGetFramework() throws Exception {
    CatalogFramework testFramework = mock(CatalogFramework.class);
    nsiliEndpoint.setFramework(testFramework);
    assertThat(nsiliEndpoint.getFramework(), notNullValue());
    nsiliEndpoint.setFramework(null);
    assertThat(nsiliEndpoint.getFramework(), nullValue());
  }

  private void createEndpoint() {
    nsiliEndpoint = new NsiliEndpoint();
    nsiliEndpoint.setCorbaOrb(mockCorbaOrb);
    nsiliEndpoint.setSecurityManager(securityManager);
    nsiliEndpoint.setDefaultUpdateFrequencySec(1);
    nsiliEndpoint.setMaxWaitToStartTimeMinutes(1);
    nsiliEndpoint.setOutgoingValidationEnabled(true);
    nsiliEndpoint.setRemoveSourceLibrary(true);
    nsiliEndpoint.setLibraryVersion("NSILI|3.2");
    nsiliEndpoint.setMaxPendingResults(MAX_PENDING_RESULTS);

    CatalogFramework mockFramework = mock(CatalogFramework.class);
    doReturn(new HashSet<>(Arrays.asList(FRAMEWORK_SOURCE_IDS))).when(mockFramework).getSourceIds();
    nsiliEndpoint.setFramework(mockFramework);
    nsiliEndpoint.setEmailSender(mock(EmailSender.class));
    nsiliEndpoint.init();
  }

  private static Matcher containsAll(final Object smallSet) {

    return new BaseMatcher() {
      @Override
      public void describeTo(Description description) {
        description.appendText("The set should contain ").appendValue(smallSet);
      }

      @Override
      public boolean matches(final Object bigSet) {
        return ((Set<String>) bigSet).containsAll((Set<String>) smallSet);
      }
    };
  }
}
