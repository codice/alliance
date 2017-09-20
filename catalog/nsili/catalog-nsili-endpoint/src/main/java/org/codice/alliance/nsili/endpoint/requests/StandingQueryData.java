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
import java.util.Iterator;
import java.util.List;
import org.codice.alliance.nsili.common.UCO.DAG;

public class StandingQueryData {

  private int totalSize = 0;

  private List<DAGQueryResult> resultOfResultsList = new ArrayList<>();

  private final Object dataLockObj = new Object();

  public StandingQueryData() {}

  public int size() {
    synchronized (dataLockObj) {
      return totalSize;
    }
  }

  public void clearAll() {
    synchronized (dataLockObj) {
      resultOfResultsList.clear();
      totalSize = 0;
    }
  }

  public void clearIntervals(int num_intervals) {
    synchronized (dataLockObj) {
      Iterator<DAGQueryResult> resultListIT = resultOfResultsList.iterator();
      int currItem = 0;
      while (resultListIT.hasNext() && currItem < num_intervals) {
        totalSize = totalSize - resultListIT.next().getResults().size();
        resultListIT.remove();
        currItem++;
      }
    }
  }

  public void clearBefore(long time) {
    synchronized (dataLockObj) {
      long offsetTime = System.currentTimeMillis() - time;
      Iterator<DAGQueryResult> resultIT = resultOfResultsList.iterator();
      while (resultIT.hasNext()) {
        DAGQueryResult result = resultIT.next();
        if (result.getTimeOfResult() < offsetTime) {
          totalSize = totalSize - resultIT.next().getResults().size();
          resultIT.remove();
        }
      }
    }
  }

  public int getNumberOfIntervals() {
    synchronized (dataLockObj) {
      return resultOfResultsList.size();
    }
  }

  public int getNumberOfHitsInInterval(int interval) {
    synchronized (dataLockObj) {
      if (resultOfResultsList.size() >= interval) {
        return resultOfResultsList.get(interval).getResults().size();
      } else {
        return 0;
      }
    }
  }

  public void add(DAGQueryResult queryResult) {
    synchronized (dataLockObj) {
      resultOfResultsList.add(queryResult);
      totalSize += queryResult.getResults().size();
    }
  }

  public List<DAG> getResultData(int maxNumResults) {
    synchronized (dataLockObj) {
      List<DAG> dagResults = new ArrayList<>();
      if (totalSize <= maxNumResults) {
        for (DAGQueryResult result : resultOfResultsList) {
          dagResults.addAll(result.getResults());
        }
        resultOfResultsList.clear();
        totalSize = 0;
      } else {
        int remainingCountNeeded = maxNumResults;
        Iterator<DAGQueryResult> resultIT = resultOfResultsList.iterator();
        while (resultIT.hasNext() && remainingCountNeeded > 0) {
          DAGQueryResult result = resultIT.next();
          List<DAG> results = result.getResults();
          if (results.size() <= remainingCountNeeded) {
            dagResults.addAll(result.getResults());
            remainingCountNeeded = remainingCountNeeded - results.size();
            totalSize = totalSize - results.size();
            resultIT.remove();
          } else {
            Iterator<DAG> dagResultIT = result.getResults().iterator();
            while (dagResultIT.hasNext() && remainingCountNeeded > 0) {
              dagResults.add(dagResultIT.next());
              dagResultIT.remove();
              remainingCountNeeded--;
              totalSize--;
            }
          }
        }
      }

      return dagResults;
    }
  }
}
