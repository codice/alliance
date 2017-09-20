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
package org.codice.alliance.video.stream.mpegts.rollover;

import ddf.catalog.data.impl.MetacardImpl;
import java.io.File;
import java.util.List;

/** Chains a list of RolloverAction objects together. */
public class ListRolloverAction extends BaseRolloverAction {

  private List<RolloverAction> actionList;

  public ListRolloverAction(List<RolloverAction> actionList) {
    this.actionList = actionList;
  }

  @Override
  public MetacardImpl doAction(MetacardImpl metacard, File tempFile)
      throws RolloverActionException {
    MetacardImpl tmp = metacard;

    for (RolloverAction rolloverAction : actionList) {
      tmp = rolloverAction.doAction(tmp, tempFile);
    }

    return tmp;
  }

  @Override
  public String toString() {
    return "ListRolloverAction{" + "actionList=" + actionList + '}';
  }
}
