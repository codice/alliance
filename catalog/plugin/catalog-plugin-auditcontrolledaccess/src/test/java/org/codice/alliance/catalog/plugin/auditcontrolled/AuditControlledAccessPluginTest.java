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
package org.codice.alliance.catalog.plugin.auditcontrolled;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.StopProcessingException;
import ddf.security.audit.SecurityLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codice.alliance.catalog.core.api.types.Security;
import org.junit.Before;
import org.junit.Test;

public class AuditControlledAccessPluginTest {

  private AuditControlledAccessPlugin auditControlledAccessPlugin;

  @Before
  public void setUp() {
    auditControlledAccessPlugin = spy(new AuditControlledAccessPlugin());

    auditControlledAccessPlugin.setSecurityLogger(mock(SecurityLogger.class));
  }

  @Test
  public void testNullMetacardAttribute() throws StopProcessingException, PluginExecutionException {
    List<String> classificationValue = Arrays.asList(new String[] {"TS"});
    auditControlledAccessPlugin.setControlledClassificationValues(classificationValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(null));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(0)).auditControlledMetacard(null);
  }

  @Test
  public void testNullAttributeValue() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION, ""));
    List<String> classificationValue = Arrays.asList(new String[] {"TS"});
    auditControlledAccessPlugin.setControlledClassificationValues(classificationValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(0)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testNullControlledValue() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.RELEASABILITY, "USA"));
    metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION, "TS"));
    List<String> classificationValue = Arrays.asList(new String[] {""});
    auditControlledAccessPlugin.setControlledClassificationValues(classificationValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(0)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testAuditClassification() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION, "TS"));
    List<String> classificationValue = Arrays.asList(new String[] {"TS"});
    auditControlledAccessPlugin.setControlledClassificationValues(classificationValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testAuditReleasability() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.RELEASABILITY, "USA"));
    List<String> releasabilityValue = Arrays.asList(new String[] {"USA"});
    auditControlledAccessPlugin.setControlledReleasabilityValues(releasabilityValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testAuditDisseminationControls()
      throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.DISSEMINATION_CONTROLS, "NF"));
    List<String> disseminationCOntrolsValue = Arrays.asList(new String[] {"NF"});
    auditControlledAccessPlugin.setControlledDisseminationControlsValues(
        disseminationCOntrolsValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testAuditCodewords() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.CODEWORDS, "SCI1"));
    List<String> codewordsValue = Arrays.asList(new String[] {"SCI1"});
    auditControlledAccessPlugin.setControlledCodewordsValues(codewordsValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testMultipleValues() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.CLASSIFICATION, "TS"));
    List<String> classificationValue = Arrays.asList(new String[] {"TS", "S"});
    metacard.setAttribute(new AttributeImpl(Security.RELEASABILITY, "USA, GBR"));
    List<String> releasabilityValues = Arrays.asList(new String[] {"USA", "GBR"});
    auditControlledAccessPlugin.setControlledClassificationValues(classificationValue);
    auditControlledAccessPlugin.setControlledReleasabilityValues(releasabilityValues);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 1);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard.getId());
  }

  @Test
  public void testMultipleMetacards() throws StopProcessingException, PluginExecutionException {
    Metacard metacard = new MetacardImpl();
    metacard.setAttribute(new AttributeImpl(Security.DISSEMINATION_CONTROLS, "NF"));
    List<String> disseminationCOntrolsValue = Arrays.asList(new String[] {"NF"});
    auditControlledAccessPlugin.setControlledDisseminationControlsValues(
        disseminationCOntrolsValue);

    Metacard metacard2 = new MetacardImpl();
    metacard2.setAttribute(new AttributeImpl(Security.CODEWORDS, "SCI1, SCI2"));
    List<String> codewordsValue = Arrays.asList(new String[] {"SCI1"});
    auditControlledAccessPlugin.setControlledCodewordsValues(codewordsValue);

    List<Result> resultList = new ArrayList<>();
    resultList.add(new ResultImpl(metacard));
    resultList.add(new ResultImpl(metacard2));
    QueryResponseImpl queryResponse = new QueryResponseImpl(null, resultList, 2);

    auditControlledAccessPlugin.process(queryResponse);
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard.getId());
    verify(auditControlledAccessPlugin, times(1)).auditControlledMetacard(metacard2.getId());
  }
}
