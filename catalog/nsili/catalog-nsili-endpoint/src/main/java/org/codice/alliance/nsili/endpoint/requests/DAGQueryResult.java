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
package org.codice.alliance.nsili.endpoint.requests;

import java.util.ArrayList;
import java.util.List;
import org.codice.alliance.nsili.common.UCO.DAG;

public class DAGQueryResult {

  private List<DAG> results = new ArrayList<>();

  private long timeOfResult;

  public DAGQueryResult(long timeOfResult, List<DAG> results) {
    if (results != null) {
      this.results.addAll(results);
    }
    this.timeOfResult = timeOfResult;
  }

  public List<DAG> getResults() {
    return results;
  }

  public long getTimeOfResult() {
    return timeOfResult;
  }
}
