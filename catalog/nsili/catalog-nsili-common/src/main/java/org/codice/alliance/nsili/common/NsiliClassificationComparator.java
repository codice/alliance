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

import java.util.Comparator;

public class NsiliClassificationComparator implements Comparator<NsiliClassification> {
  @Override
  public int compare(NsiliClassification o1, NsiliClassification o2) {
    if (o1 == o2) {
      return 0;
    }

    if (o1 != NsiliClassification.NO_CLASSIFICATION
        && (o2 == null || o2 == NsiliClassification.NO_CLASSIFICATION)) {
      return -1;
    } else if (o1 == null || o1 == NsiliClassification.NO_CLASSIFICATION) {
      return 1;
    } else {
      if (o1.getClassificationRank() < o2.getClassificationRank()) {
        return -1;
      } else {
        return 1;
      }
    }
  }
}
