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
package org.codice.alliance.nsili.common;

/**
 * Exception thrown when a {@link ResultDAGConverter} encounters problems turning metacards into
 * DAG.
 */
public class DagParsingException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new DagParsingException from a given string.
   *
   * @param message the string to use for the exception.
   */
  public DagParsingException(String message) {
    super(message);
  }

  /**
   * Instantiates a new DagParsingException with given {@link Throwable}.
   *
   * @param throwable the throwable
   */
  public DagParsingException(Throwable throwable) {
    super(throwable);
  }

  /**
   * Instantiates a new DagParsingException with a message.
   *
   * @param message the message
   * @param throwable the throwable
   */
  public DagParsingException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
