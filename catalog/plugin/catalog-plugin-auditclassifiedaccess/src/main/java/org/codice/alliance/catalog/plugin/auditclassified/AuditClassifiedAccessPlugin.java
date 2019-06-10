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
package org.codice.alliance.catalog.plugin.auditclassified;

import com.google.common.annotations.VisibleForTesting;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PostQueryPlugin;
import ddf.catalog.plugin.StopProcessingException;
import ddf.security.common.audit.SecurityLogger;
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

public class AuditClassifiedAccessPlugin implements PostQueryPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditClassifiedAccessPlugin.class);

  private Map<String, List<String>> classifiedValuesMap = new HashMap<String, List<String>>();

  public QueryResponse process(QueryResponse input)
      throws PluginExecutionException, StopProcessingException {

    List<Result> results = input.getResults();

    results.stream().map(Result::getMetacard).filter(Objects::nonNull).forEach(this::handleAudits);

    LOGGER.trace("Response went through the Audit Classified Access Plugin");
    return input;
  }

  private void handleAudits(Metacard metacard) {
    boolean isClassified = false;
    Iterator<Entry<String, List<String>>> it = classifiedValuesMap.entrySet().iterator();

    while (it.hasNext() && !isClassified) {
      Entry<String, List<String>> entry = it.next();
      if (hasClassifiedValues(entry.getValue(), entry.getKey(), metacard)) {
        isClassified = true;
      }
    }

    if (isClassified) {
      auditClassifiedMetacard(metacard.getId());
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

  private boolean hasClassifiedValues(
      List<String> classifiedValues, String attributeString, Metacard metacard) {

    List<Serializable> metacardAttributeValues =
        getMetacardAttributeValues(metacard, attributeString);
    if (metacardAttributeValues == null) {
      return false;
    }

    return classifiedValues
        .stream()
        .map(String::trim)
        .anyMatch(
            classifiedValue ->
                metacardAttributeValues
                    .stream()
                    .anyMatch(
                        metacardAttributeValue ->
                            classifiedValue.equals(metacardAttributeValue)
                                && !StringUtils.isEmpty(classifiedValue)));
  }

  @VisibleForTesting
  void auditClassifiedMetacard(String metacardId) {
    SecurityLogger.audit("The classified metacard with id " + metacardId + " is being returned.");
  }

  public void setClassifiedClassificationValues(List<String> classifiedClassificationValues) {
    classifiedValuesMap.put(Security.CLASSIFICATION, classifiedClassificationValues);
  }

  public void setClassifiedReleasabilityValues(List<String> classifiedReleasabilityValues) {
    classifiedValuesMap.put(Security.RELEASABILITY, classifiedReleasabilityValues);
  }

  public void setClassifiedDisseminationControlsValues(
      List<String> classifiedDisseminationControlsValues) {
    classifiedValuesMap.put(Security.DISSEMINATION_CONTROLS, classifiedDisseminationControlsValues);
  }

  public void setClassifiedCodewordsValues(List<String> classifiedCodewordsValues) {
    classifiedValuesMap.put(Security.CODEWORDS, classifiedCodewordsValues);
  }
}
