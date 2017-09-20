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

import java.util.Date;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorbaUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorbaUtils.class);

  public static boolean isIdActive(POA poa, byte[] oid) {
    boolean idActive = false;
    try {
      poa.id_to_servant(oid);
      idActive = true;
    } catch (ObjectNotActive | WrongPolicy | BAD_INV_ORDER ignore) {
    }

    return idActive;
  }

  public static String getNodeValue(Any any) {
    String value = null;
    if (any.type().kind() == TCKind.tk_wstring) {
      value = any.extract_wstring();
    } else if (any.type().kind() == TCKind.tk_string) {
      value = any.extract_string();
    } else if (any.type().kind() == TCKind.tk_longlong) {
      value = String.valueOf(any.extract_longlong());
    } else if (any.type().kind() == TCKind.tk_long) {
      value = String.valueOf(any.extract_long());
    } else if (any.type().kind() == TCKind.tk_ulong) {
      value = String.valueOf(any.extract_ulong());
    } else if (any.type().kind() == TCKind.tk_short) {
      value = String.valueOf(any.extract_short());
    } else if (any.type().kind() == TCKind.tk_ushort) {
      value = String.valueOf(any.extract_ushort());
    } else if (any.type().kind() == TCKind.tk_boolean) {
      value = String.valueOf(any.extract_boolean());
    } else if (any.type().kind() == TCKind.tk_double) {
      value = String.valueOf(any.extract_double());
    } else {
      try {
        if (any.type().name().equals(AbsTime.class.getSimpleName())) {
          Date date = convertDate(any);
          if (date != null) {
            value = date.toString();
          }
        }
      } catch (BadKind e) {
        //Ignore BadKind errors as this isn't a data parsing exception
      } catch (org.omg.CORBA.MARSHAL | IllegalFieldValueException e) {
        LOGGER.debug("Unable to parse date value from: {}", any, e);
      }
    }

    return value;
  }

  public static Date convertDate(Any any) {
    AbsTime absTime = AbsTimeHelper.extract(any);
    org.codice.alliance.nsili.common.UCO.Date ucoDate = absTime.aDate;
    org.codice.alliance.nsili.common.UCO.Time ucoTime = absTime.aTime;

    DateTime dateTime =
        new DateTime(
            (int) ucoDate.year,
            (int) ucoDate.month,
            (int) ucoDate.day,
            (int) ucoTime.hour,
            (int) ucoTime.minute,
            (int) ucoTime.second,
            0);
    return dateTime.toDate();
  }
}
