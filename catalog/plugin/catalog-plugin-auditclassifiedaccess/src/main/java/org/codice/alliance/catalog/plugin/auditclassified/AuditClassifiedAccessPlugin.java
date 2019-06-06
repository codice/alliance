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

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PostQueryPlugin;
import ddf.catalog.plugin.StopProcessingException;
import ddf.security.common.audit.SecurityLogger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.codice.alliance.catalog.core.api.types.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditClassifiedAccessPlugin implements PostQueryPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditClassifiedAccessPlugin.class);

  private List<String> classifiedClassificationValues = new ArrayList<>();

  private List<String> classifiedReleasabilityValues = new ArrayList<>();

  private List<String> classifiedDisseminationControlsValues = new ArrayList<>();

  private List<String> classifiedCodewordsValues = new ArrayList<>();

  public QueryResponse process(QueryResponse input)
      throws PluginExecutionException, StopProcessingException {

    List<Result> results = input.getResults();

    results.stream().map(Result::getMetacard).filter(Objects::nonNull).forEach(this::handleAudits);

    LOGGER.trace("Response went through the Audit Classified Access Plugin");
    return input;
  }

  private void handleAudits(Metacard metacard) {
    auditClassification(metacard);
    auditReleasability(metacard);
    auditDisseminationControls(metacard);
    auditCodewords(metacard);
  }

  private void auditClassification(Metacard metacard) {
    Attribute classificationAttribute = metacard.getAttribute(Security.CLASSIFICATION);
    if (classificationAttribute != null) {
      // NOTE: Single-valued attribute so being handled differently at the moment
      String classificationValue = (String) classificationAttribute.getValue();
      if (classificationValue != null) {
        for (String enteredClassificationValue : getClassifiedClassificationValues()) {
          if (classificationValue.equals(enteredClassificationValue)) {
            SecurityLogger.audit(
                "Audit "
                    + Security.CLASSIFICATION
                    + " attribute with value "
                    + enteredClassificationValue
                    + " on metacard "
                    + metacard.getId());
          }
        }
      }
    }
  }

  private void auditReleasability(Metacard metacard) {
    List<Serializable> releasabilityValues = getAttributeValues(metacard, Security.RELEASABILITY);
    if (releasabilityValues != null) {
      auditValues(releasabilityValues, Security.RELEASABILITY, metacard.getId());
    }
  }

  private void auditDisseminationControls(Metacard metacard) {
    List<Serializable> disseminationControlsValues =
        getAttributeValues(metacard, Security.DISSEMINATION_CONTROLS);
    if (disseminationControlsValues != null) {
      auditValues(disseminationControlsValues, Security.DISSEMINATION_CONTROLS, metacard.getId());
    }
  }

  private void auditCodewords(Metacard metacard) {
    List<Serializable> codewordsValues = getAttributeValues(metacard, Security.CODEWORDS);
    if (codewordsValues != null) {
      auditValues(codewordsValues, Security.CODEWORDS, metacard.getId());
    }
  }

  private List<Serializable> getAttributeValues(Metacard metacard, String attributeString) {
    List<Serializable> attributeValues = null;
    Attribute attribute = metacard.getAttribute(attributeString);
    if (attribute != null) {
      attributeValues = attribute.getValues();
    }
    return attributeValues;
  }

  private void auditValues(
      List<Serializable> valuesToCheckToAudit, String attributeString, String metacardId) {

    List<String> enteredValues = null;

    if (attributeString.equals(Security.RELEASABILITY)) {
      enteredValues = getClassifiedReleasabilityValues();
    }

    if (attributeString.equals(Security.DISSEMINATION_CONTROLS)) {
      enteredValues = getClassifiedDisseminationControlsValues();
    }

    if (attributeString.equals(Security.CODEWORDS)) {
      enteredValues = getClassifiedCodewordsValues();
    }

    for (Serializable valueToCheck : valuesToCheckToAudit) {
      for (String enteredValue : enteredValues) {
        if (valueToCheck.equals(enteredValue)) {
          SecurityLogger.audit(
              "Audit "
                  + attributeString
                  + " attribute with value "
                  + enteredValue
                  + " on metacard "
                  + metacardId);
        }
      }
    }
  }

  public List<String> getClassifiedClassificationValues() {
    return classifiedClassificationValues;
  }

  public void setClassifiedClassificationValues(List<String> classifiedClassificationValues) {
    this.classifiedClassificationValues = classifiedClassificationValues;
  }

  public List<String> getClassifiedReleasabilityValues() {
    return classifiedReleasabilityValues;
  }

  public void setClassifiedReleasabilityValues(List<String> classifiedReleasabilityValues) {
    this.classifiedReleasabilityValues = classifiedReleasabilityValues;
  }

  public List<String> getClassifiedDisseminationControlsValues() {
    return classifiedDisseminationControlsValues;
  }

  public void setClassifiedDisseminationControlsValues(
      List<String> classifiedDisseminationControlsValues) {
    this.classifiedDisseminationControlsValues = classifiedDisseminationControlsValues;
  }

  public List<String> getClassifiedCodewordsValues() {
    return classifiedCodewordsValues;
  }

  public void setClassifiedCodewordsValues(List<String> classifiedCodewordsValues) {
    this.classifiedCodewordsValues = classifiedCodewordsValues;
  }
}
