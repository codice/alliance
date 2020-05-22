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

import com.google.common.annotations.VisibleForTesting;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PostQueryPlugin;
import ddf.catalog.plugin.StopProcessingException;
import ddf.security.audit.SecurityLogger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditControlledAccessPlugin implements PostQueryPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditControlledAccessPlugin.class);

  private Map<String, List<String>> controlledValuesMap = new HashMap<String, List<String>>();

  private SecurityLogger securityLogger;

  public QueryResponse process(QueryResponse input)
      throws PluginExecutionException, StopProcessingException {

    List<Result> results = input.getResults();

    results.stream().map(Result::getMetacard).filter(Objects::nonNull).forEach(this::handleAudits);

    LOGGER.trace("Response went through the Audit Controlled Access Plugin");
    return input;
  }

  private void handleAudits(Metacard metacard) {
    boolean isControlled = false;
    Iterator<Entry<String, List<String>>> it = controlledValuesMap.entrySet().iterator();

    while (it.hasNext() && !isControlled) {
      Entry<String, List<String>> entry = it.next();
      if (hasControlledValues(entry.getValue(), entry.getKey(), metacard)) {
        isControlled = true;
      }
    }

    if (isControlled) {
      auditControlledMetacard(metacard.getId());
    }
  }

  private List<Serializable> getMetacardAttributeValues(Metacard metacard, String attributeString) {
    List<Serializable> attributeValues = null;
    Attribute attribute = metacard.getAttribute(attributeString);
    if (attribute != null) {
      attributeValues = attribute.getValues();
    }
    return attributeValues;
  }

  private boolean hasControlledValues(
      List<String> controlledValues, String attributeString, Metacard metacard) {

    List<Serializable> metacardAttributeValues =
        getMetacardAttributeValues(metacard, attributeString);
    if (metacardAttributeValues == null) {
      return false;
    }

    return controlledValues
        .stream()
        .map(String::trim)
        .anyMatch(
            controlledValue ->
                metacardAttributeValues
                    .stream()
                    .anyMatch(
                        metacardAttributeValue ->
                            controlledValue.equals(metacardAttributeValue)
                                && !StringUtils.isEmpty(controlledValue)));
  }

  @VisibleForTesting
  void auditControlledMetacard(String metacardId) {
    securityLogger.audit("The controlled metacard with id " + metacardId + " is being returned.");
  }

  public void setControlledClassificationValues(List<String> controlledClassificationValues) {
    controlledValuesMap.put(Security.CLASSIFICATION, controlledClassificationValues);
  }

  public void setControlledReleasabilityValues(List<String> controlledReleasabilityValues) {
    controlledValuesMap.put(Security.RELEASABILITY, controlledReleasabilityValues);
  }

  public void setControlledDisseminationControlsValues(
      List<String> controlledDisseminationControlsValues) {
    controlledValuesMap.put(Security.DISSEMINATION_CONTROLS, controlledDisseminationControlsValues);
  }

  public void setControlledCodewordsValues(List<String> controlledCodewordsValues) {
    controlledValuesMap.put(Security.CODEWORDS, controlledCodewordsValues);
  }

  public void setSecurityLogger(SecurityLogger securityLogger) {
    this.securityLogger = securityLogger;
  }
}
