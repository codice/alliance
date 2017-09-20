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

/**
 * This implementation provides an empty body for each visit method, since most implementations only
 * need to define a limited number of visit methods.
 */
public abstract class BaseKlvProcessorVisitor implements KlvProcessor.Visitor {

  @Override
  public void visit(DistinctKlvProcessor distinctKlvProcessor) {}

  @Override
  public void visit(DistinctSingleKlvProcessor distinctSingleKlvProcessor) {}

  @Override
  public void visit(CopyPresentKlvProcessor copyPresentKlvProcessor) {}

  @Override
  public void visit(FrameCenterKlvProcessor frameCenterKlvProcessor) {}

  @Override
  public void visit(LocationKlvProcessor locationKlvProcessor) {}

  @Override
  public void visit(SetDatesKlvProcessor setDatesKlvProcessor) {}

  @Override
  public void visit(ClassifyingCountryKlvProcessor classifyingCountryKlvProcessor) {}

  @Override
  public void visit(UnionKlvProcessor abstractUnionKlvProcessor) {}

  @Override
  public void visit(SensorAltitudeKlvProcessor sensorAltitudeKlvProcessor) {}

  @Override
  public void visit(SecurityClassificationKlvProcessor securityClassificationKlvProcessor) {}
}
