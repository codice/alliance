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
package org.codice.alliance.security.banner.marking;

import java.io.Serializable;

public class ValidationError implements Serializable {
  private final String message;

  private final String appendix;

  private final String paragraph;

  public ValidationError(String message) {
    this(message, "", "-");
  }

  public ValidationError(String message, String paragraph) {
    this(message, "", paragraph);
  }

  public ValidationError(String message, String appendix, String paragraph) {
    this.message = message;
    this.appendix = appendix;
    this.paragraph = paragraph;
  }

  public String getMessage() {
    return message;
  }

  public String getAppendix() {
    return appendix;
  }

  public String getParagraph() {
    return paragraph;
  }

  @Override
  public String toString() {
    final StringBuilder sb =
        new StringBuilder("{")
            .append(message)
            .append(": DoD MANUAL NUMBER 5200.01, Volume 2, Enc 4");
    if (!appendix.isEmpty()) {
      sb.append(", Appendix ").append(appendix);
    }
    if (!paragraph.equals("-")) {
      sb.append(", Para ").append(paragraph);
    }
    sb.append("}");
    return sb.toString();
  }
}
