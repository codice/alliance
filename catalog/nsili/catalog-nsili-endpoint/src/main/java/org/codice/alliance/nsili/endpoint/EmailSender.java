/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */

package org.codice.alliance.nsili.endpoint;

import java.io.InputStream;

/**
 * Interface for handling emailSending events with attachments
 * -Takes mailHost as input argument for constructor, then calls to the sendMail method
 * require a fromEmail address, a subject field, an emailBody of text, a filename for an attachment,
 * and an inputstream for an attachment
 */
public interface EmailSender {

    void sendEmail(String fromEmail, String emailDest, String subject, String emailBody,
            String filename, InputStream attachment);

    String toString();

    String getFromEmail();

    void setFromEmail(String fromEmail);

    String getMailHost();

    void setMailHost(String mailHost);

    void setAttachment();
}
