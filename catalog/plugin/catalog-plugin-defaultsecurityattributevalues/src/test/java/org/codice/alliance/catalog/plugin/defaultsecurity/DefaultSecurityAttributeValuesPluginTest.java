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
package org.codice.alliance.catalog.plugin.defaultsecurity;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.UpdateRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.ddf.security.SystemHighAttributes;
import org.junit.Test;

public class DefaultSecurityAttributeValuesPluginTest {

  private static final String CLASSIFICATION_CONFIGURATION_KEY = "classification";

  private static final String RELEASABILITY_CONFIGURATION_KEY = "releasability";

  private static final String CODEWORDS_CONFIGURATION_KEY = "codewords";

  private static final String DISSEMINATION_CONTROLS_CONFIGURATION_KEY = "disseminationControls";

  private static final String OTHER_DISSEMINATION_CONTROLS_CONFIGURATION_KEY =
      "otherDisseminationControls";

  private static final String OWNER_PRODUCER_CONFIGURATION_KEY = "ownerProducer";

  private static final String DEFAULT_MARKINGS_TAG = "defaultMarkings";

  @Test
  public void testProcessUnmarkedMetacard() {
    // given
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(),
            createTestSystemHighAttributes(),
            createTestInitialConfiguration());

    // when
    final CreateRequest modifiedCreateRequest =
        defaultSecurityAttributeValuesPlugin.process(
            createCreateRequest(createUnmarkedMetacardWithoutSecurityDescriptors()));

    // then
    final List<Metacard> resultMetacards = modifiedCreateRequest.getMetacards();
    assertThat(resultMetacards, hasSize(1));
    assertTestDefaultsAdded(resultMetacards.get(0));
  }

  @Test
  public void testProcessUnmarkedMetacardWithSecurityAttributeDescriptors() {
    // given
    final MetacardImpl unmarkedMetacardWithSecurityAttributeDescriptors =
        new MetacardImpl(new SecurityAttributes());
    unmarkedMetacardWithSecurityAttributeDescriptors.setTags(
        Collections.singleton(Metacard.DEFAULT_TAG));
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(),
            createTestSystemHighAttributes(),
            createTestInitialConfiguration());

    // when
    final CreateRequest modifiedCreateRequest =
        defaultSecurityAttributeValuesPlugin.process(
            createCreateRequest(unmarkedMetacardWithSecurityAttributeDescriptors));

    // then
    final List<Metacard> resultMetacards = modifiedCreateRequest.getMetacards();
    assertThat(resultMetacards, hasSize(1));
    assertTestDefaultsAdded(resultMetacards.get(0));
  }

  @Test
  public void testProcessUpdateRequest() {
    // given
    final UpdateRequest updateRequest = mock(UpdateRequest.class);
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(),
            createTestSystemHighAttributes(),
            createTestInitialConfiguration());

    // when
    final UpdateRequest modifiedRequest =
        defaultSecurityAttributeValuesPlugin.process(updateRequest);

    // then
    verifyZeroInteractions(updateRequest);
    assertThat(updateRequest, is(modifiedRequest));
  }

  @Test
  public void testProcessDeleteRequest() {
    // given
    final DeleteRequest deleteRequest = mock(DeleteRequest.class);
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(),
            createTestSystemHighAttributes(),
            createTestInitialConfiguration());

    // when
    final DeleteRequest modifiedRequest =
        defaultSecurityAttributeValuesPlugin.process(deleteRequest);

    // then
    verifyZeroInteractions(deleteRequest);
    assertThat(deleteRequest, is(modifiedRequest));
  }

  @Test
  public void testComponentManagedUpdateStrategy() {
    // given
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(),
            createTestSystemHighAttributes(),
            createTestInitialConfiguration());

    final List<String> newConfiguration = new ArrayList<>();
    newConfiguration.add(Security.CLASSIFICATION + "=classification");
    newConfiguration.add(Security.RELEASABILITY + "=releasableTo");
    // this configuration value is different than the first configuration
    newConfiguration.add(Security.CODEWORDS + "=FineAccessControls");
    newConfiguration.add(Security.DISSEMINATION_CONTROLS + "=disseminationControls");
    newConfiguration.add(Security.OTHER_DISSEMINATION_CONTROLS + "=otherDisseminationControls");
    newConfiguration.add(Security.OWNER_PRODUCER + "=ownerProducer");

    // when
    defaultSecurityAttributeValuesPlugin.setAttributeMappings(newConfiguration);

    // then
    final CreateRequest modifiedCreateRequest =
        defaultSecurityAttributeValuesPlugin.process(
            createCreateRequest(createUnmarkedMetacardWithoutSecurityDescriptors()));

    final List<Metacard> resultMetacards = modifiedCreateRequest.getMetacards();
    assertThat(resultMetacards, hasSize(1));
    final Metacard modifiedMetacard = resultMetacards.get(0);

    assertThat(
        modifiedMetacard.getAttribute(Security.CLASSIFICATION),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("U"))));
    assertThat(
        modifiedMetacard.getAttribute(Security.RELEASABILITY),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("USA"))));
    // this assert is different than the first configuration
    assertThat(
        modifiedMetacard.getAttribute(Security.CODEWORDS),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("SCI1", "SCI2"))));
    assertThat(
        modifiedMetacard.getAttribute(Security.DISSEMINATION_CONTROLS),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("NF"))));
    assertThat(modifiedMetacard.getAttribute(Security.OTHER_DISSEMINATION_CONTROLS), nullValue());
    assertThat(
        modifiedMetacard.getAttribute(Security.OWNER_PRODUCER),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("USA"))));

    assertThat(modifiedMetacard.getTags(), hasItem(DEFAULT_MARKINGS_TAG));
  }

  @Test
  public void testDefaultMarkingsFlagNotSetWhenNoneOfTheSystemHighAttributesExist() {
    // given
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(),
            createTestSystemHighAttributes(),
            createTestInitialConfiguration());

    // when
    final List<String> newConfiguration = new ArrayList<>();
    newConfiguration.add(Security.CLASSIFICATION + "=someAttributeNameThatIsn'tThere1");
    newConfiguration.add(Security.RELEASABILITY + "=someAttributeNameThatIsn'tThere2");
    newConfiguration.add(Security.CODEWORDS + "=someAttributeNameThatIsn'tThere3");
    newConfiguration.add(Security.DISSEMINATION_CONTROLS + "=someAttributeNameThatIsn'tThere4");
    newConfiguration.add(
        Security.OTHER_DISSEMINATION_CONTROLS + "=someAttributeNameThatIsn'tThere5");
    newConfiguration.add(Security.OWNER_PRODUCER + "=someAttributeNameThatIsn'tThere6");
    defaultSecurityAttributeValuesPlugin.setAttributeMappings(newConfiguration);

    // then
    final CreateRequest modifiedCreateRequest =
        defaultSecurityAttributeValuesPlugin.process(
            createCreateRequest(createUnmarkedMetacardWithoutSecurityDescriptors()));

    final List<Metacard> resultMetacards = modifiedCreateRequest.getMetacards();
    assertThat(resultMetacards, hasSize(1));
    final Metacard modifiedMetacard = resultMetacards.get(0);

    assertThat(modifiedMetacard.getAttribute(Security.CLASSIFICATION), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.RELEASABILITY), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.CODEWORDS), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.DISSEMINATION_CONTROLS), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.OTHER_DISSEMINATION_CONTROLS), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.OWNER_PRODUCER), nullValue());

    assertThat(modifiedMetacard.getTags(), not(hasItem(DEFAULT_MARKINGS_TAG)));
  }

  @Test
  public void testInvalidConfigEntries() {
    // given
    final DefaultSecurityAttributeValuesPlugin defaultSecurityAttributeValuesPlugin =
        new DefaultSecurityAttributeValuesPlugin(
            new SecurityAttributes(), createTestSystemHighAttributes(), new HashMap<>());

    // when
    final List<String> configuration = new ArrayList<>();
    configuration.add(Security.CLASSIFICATION + "=classification");
    configuration.add(Security.RELEASABILITY + " releasableTo");
    configuration.add(Security.CODEWORDS + "=");
    configuration.add("=disseminationControls");
    defaultSecurityAttributeValuesPlugin.setAttributeMappings(configuration);

    // then
    final CreateRequest modifiedCreateRequest =
        defaultSecurityAttributeValuesPlugin.process(
            createCreateRequest(createUnmarkedMetacardWithoutSecurityDescriptors()));

    final List<Metacard> resultMetacards = modifiedCreateRequest.getMetacards();
    assertThat(resultMetacards, hasSize(1));
    final Metacard modifiedMetacard = resultMetacards.get(0);

    assertThat(
        modifiedMetacard.getAttribute(Security.CLASSIFICATION),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("U"))));
    assertThat(modifiedMetacard.getAttribute(Security.RELEASABILITY), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.CODEWORDS), nullValue());
    assertThat(modifiedMetacard.getAttribute(Security.DISSEMINATION_CONTROLS), nullValue());
  }

  private static SystemHighAttributes createTestSystemHighAttributes() {
    final SystemHighAttributes testSystemHighAttributes = mock(SystemHighAttributes.class);

    when(testSystemHighAttributes.getValues(anyString())).thenReturn(Collections.emptySet());
    when(testSystemHighAttributes.getValues("Clearance")).thenReturn(Collections.singleton("U"));
    when(testSystemHighAttributes.getValues("CountryOfAffiliation"))
        .thenReturn(Collections.singleton("USA"));
    when(testSystemHighAttributes.getValues("classification"))
        .thenReturn(Collections.singleton("U"));
    when(testSystemHighAttributes.getValues(
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"))
        .thenReturn(Collections.singleton("system@testHostname"));
    when(testSystemHighAttributes.getValues("ownerProducer"))
        .thenReturn(Collections.singleton("USA"));
    when(testSystemHighAttributes.getValues("releasableTo"))
        .thenReturn(Collections.singleton("USA"));
    when(testSystemHighAttributes.getValues("FineAccessControls"))
        .thenReturn(Collections.unmodifiableSet(new HashSet<>(Arrays.asList("SCI1", "SCI2"))));
    when(testSystemHighAttributes.getValues("disseminationControls"))
        .thenReturn(Collections.singleton("NF"));

    return testSystemHighAttributes;
  }

  private static Map<String, String> createTestInitialConfiguration() {
    final Map<String, String> configuration = new HashMap<>();
    configuration.put(Security.CLASSIFICATION, "classification");
    configuration.put(Security.RELEASABILITY, "releasableTo");
    configuration.put(Security.CODEWORDS, "sciControls");
    configuration.put(Security.DISSEMINATION_CONTROLS, "disseminationControls");
    configuration.put(Security.OTHER_DISSEMINATION_CONTROLS, "otherDisseminationControls");
    configuration.put(Security.OWNER_PRODUCER, "ownerProducer");
    return configuration;
  }

  private static Metacard createUnmarkedMetacardWithoutSecurityDescriptors() {
    MetacardImpl mcard = new MetacardImpl();
    mcard.setTags(Collections.singleton(Metacard.DEFAULT_TAG));
    return mcard;
  }

  private static CreateRequest createCreateRequest(Metacard metacard) {
    final CreateRequest createRequest = mock(CreateRequest.class);
    when(createRequest.getMetacards()).thenReturn(Collections.singletonList(metacard));
    return createRequest;
  }

  private static void assertTestDefaultsAdded(Metacard metacard) {
    assertThat(
        metacard.getAttribute(Security.CLASSIFICATION),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("U"))));
    assertThat(
        metacard.getAttribute(Security.RELEASABILITY),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("USA"))));
    assertThat(metacard.getAttribute(Security.CODEWORDS), nullValue());
    assertThat(
        metacard.getAttribute(Security.DISSEMINATION_CONTROLS),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("NF"))));
    assertThat(metacard.getAttribute(Security.OTHER_DISSEMINATION_CONTROLS), nullValue());
    assertThat(
        metacard.getAttribute(Security.OWNER_PRODUCER),
        allOf(notNullValue(), hasProperty("values", containsInAnyOrder("USA"))));

    assertThat(metacard.getTags(), hasItem(DEFAULT_MARKINGS_TAG));
  }
}
