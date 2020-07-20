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
package org.codice.alliance.libs.klv;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.codice.alliance.catalog.core.internal.api.classification.SecurityClassificationService;
import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

/**
 * Classification mapping defined by http://www.gwg.nga.mil/misb//docs/standards/ST0102.7.pdf
 *
 * <p>1 ==> unclassified 2 ==> restricted 3 ==> confidential 4 ==> secret 5 ==> top secret
 */
public class SecurityClassificationKlvProcessor extends MultipleFieldKlvProcessor {

  private Map<Short, String> codeToSecurityClassification;

  private String defaultSecurityClassification;

  private SecurityClassificationService securityClassificationService;

  /**
   * @param codeToSecurityClassification map of stanag security codes to classification strings
   * @param defaultSecurityClassification classification string to be used if the map does not
   *     contain an entry for the stanag security code
   */
  public SecurityClassificationKlvProcessor(
      SecurityClassificationService securityClassificationService,
      Map<Short, String> codeToSecurityClassification,
      String defaultSecurityClassification) {
    super(Collections.singletonList(Stanag4609TransportStreamParser.SECURITY_CLASSIFICATION));
    this.securityClassificationService = securityClassificationService;
    this.codeToSecurityClassification = new HashMap<>(codeToSecurityClassification);
    this.defaultSecurityClassification = defaultSecurityClassification;
  }

  public void setSecurityClassification(Short code, String classification) {
    codeToSecurityClassification.put(code, classification);
  }

  public void setDefaultSecurityClassification(String classification) {
    defaultSecurityClassification = classification;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected void doProcess(Attribute attribute, Metacard metacard) {

    Comparator<String> comparator =
        securityClassificationService.getSecurityClassificationComparator();

    attribute.getValues().stream()
        .filter(Short.class::isInstance)
        .map(Short.class::cast)
        .map(this::codeToClassification)
        .max(comparator)
        .ifPresent(
            classification -> {
              metacard.setAttribute(
                  new AttributeImpl(
                      AttributeNameConstants.SECURITY_CLASSIFICATION, classification));
            });
  }

  private String codeToClassification(Short code) {
    return codeToSecurityClassification.getOrDefault(code, defaultSecurityClassification);
  }

  @Override
  public String toString() {
    return "SecurityClassificationKlvProcessor{"
        + "codeToSecurityClassification="
        + codeToSecurityClassification
        + ", defaultSecurityClassification='"
        + defaultSecurityClassification
        + '\''
        + '}';
  }
}
