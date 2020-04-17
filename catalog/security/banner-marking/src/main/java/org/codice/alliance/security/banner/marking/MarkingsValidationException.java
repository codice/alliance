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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MarkingsValidationException extends Exception {
  private final String inputMarkings;

  private final Set<ValidationError> errors;

  /**
   * Constructs a new exception with the specified detail message. The cause is not initialized, and
   * may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   */
  public MarkingsValidationException(String message, String inputMarkings) {
    super(message);
    this.inputMarkings = inputMarkings;
    errors = ImmutableSet.of();
  }

  public MarkingsValidationException(
      String message, String inputMarkings, Set<ValidationError> errors) {
    super(message);
    this.inputMarkings = inputMarkings;
    this.errors = ImmutableSet.copyOf(errors);
  }

  public String getInputMarkings() {
    return inputMarkings;
  }

  public Set<ValidationError> getErrors() {
    return errors;
  }

  /**
   * Returns the detail message string of this throwable.
   *
   * @return the detail message string of this {@code Throwable} instance (which may be {@code
   *     null}).
   */
  @Override
  public String getMessage() {
    if (!errors.isEmpty()) {
      return errors.stream()
          .map(ValidationError::toString)
          .collect(
              Collectors.joining(
                  "\n", String.format("%s%n%s%n", super.getMessage(), inputMarkings), ""));
    }

    return String.format("%s%n%s", super.getMessage(), inputMarkings);
  }
}
