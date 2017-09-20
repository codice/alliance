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

import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;

public class NsilCorbaExceptionUtil {

  public static String getExceptionDetails(Exception e) {
    String exceptionText = e.getClass().getCanonicalName();
    if (e instanceof ProcessingFault) {
      exceptionText = exceptionText + " " + getExceptionDetails((ProcessingFault) e);
    } else if (e instanceof InvalidInputParameter) {
      exceptionText = exceptionText + " " + getExceptionDetails((InvalidInputParameter) e);
    } else if (e instanceof SystemFault) {
      exceptionText = exceptionText + " " + getExceptionDetails((SystemFault) e);
    } else {
      exceptionText = exceptionText + " " + e.toString();
    }
    return exceptionText;
  }

  public static String getExceptionDetails(ProcessingFault e) {
    StringBuilder sb = new StringBuilder();
    sb.append(e.details.exception_name);
    sb.append(" : ");
    sb.append(e.details.exception_desc);
    return sb.toString();
  }

  public static String getExceptionDetails(InvalidInputParameter e) {
    StringBuilder sb = new StringBuilder();
    sb.append(e.details.exception_name);
    sb.append(" : ");
    sb.append(e.details.exception_desc);
    return sb.toString();
  }

  public static String getExceptionDetails(SystemFault e) {
    StringBuilder sb = new StringBuilder();
    sb.append(e.details.exception_name);
    sb.append(" : ");
    sb.append(e.details.exception_desc);
    return sb.toString();
  }
}
