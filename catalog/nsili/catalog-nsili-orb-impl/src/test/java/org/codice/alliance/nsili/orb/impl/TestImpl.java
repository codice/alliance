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
package org.codice.alliance.nsili.orb.impl;

/** Test class to provide an object for the IOR string to reference. */
public class TestImpl extends org.codice.alliance.nsili.orb.testing.TestPOA {
  /**
   * Simple implementation of ping method.
   *
   * @return
   */
  @Override
  public String ping() {
    return "Ping successful";
  };

  /** Do nothing implementation of shutdown method. */
  @Override
  public void shutdown() {
    // do nothing here - just needed methods for an interface.
  };
}
