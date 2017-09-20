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
package org.codice.alliance.transformer.video;

import org.codice.alliance.libs.klv.BaseKlvProcessorVisitor;
import org.codice.alliance.libs.klv.SecurityClassificationKlvProcessor;

/**
 * This visitor implementation calls {@link
 * SecurityClassificationKlvProcessor#setSecurityClassification(Short, String)} with a specific code
 * and classification string.
 */
public class SetSecurityClassificationString extends BaseKlvProcessorVisitor {

  private final Short code;

  private final String classification;

  public SetSecurityClassificationString(Short code, String classification) {
    this.code = code;
    this.classification = classification;
  }

  @Override
  public void visit(SecurityClassificationKlvProcessor securityClassificationKlvProcessor) {
    securityClassificationKlvProcessor.setSecurityClassification(code, classification);
  }
}
