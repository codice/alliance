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
package org.codice.alliance.core.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for sending email with optional attachments.
 *
 * <p><b> This code is experimental. While this interface is functional and tested, it may change or
 * be removed in a future version of the library. </b>
 */
public interface EmailSender {

  /**
   * @param fromEmail non-null from email address
   * @param toEmail non-null to email address
   * @param subject non-null subject line of email to be sent
   * @param body non-null body of email to be sent
   * @param attachments non-null list of filename/input stream pairs, the caller is responsible for
   *     closing the input streams
   * @throws IOException exception thrown if failed to send email
   */
  void sendEmail(
      String fromEmail,
      String toEmail,
      String subject,
      String body,
      List<Pair<String, InputStream>> attachments)
      throws IOException;

  /**
   * @param fromEmail non-null from email address
   * @param toEmail non-null to email address
   * @param subject non-null subject line of email to be sent
   * @param body non-null body of email to be sent
   * @throws IOException exception thrown if failed to send email
   */
  void sendEmail(String fromEmail, String toEmail, String subject, String body) throws IOException;
}
