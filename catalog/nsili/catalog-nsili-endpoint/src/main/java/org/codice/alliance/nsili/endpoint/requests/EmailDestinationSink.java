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

package org.codice.alliance.nsili.endpoint.requests;

import java.io.IOException;
import java.io.InputStream;

import org.codice.alliance.nsili.endpoint.EmailSender;
import org.codice.alliance.nsili.endpoint.EmailSenderImpl;
import org.slf4j.LoggerFactory;

public class EmailDestinationSink implements DestinationSink {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderRequestImpl.class);

    String emailDest;

    EmailSender emailSender;

    public EmailDestinationSink(String emailDest) {
        this.emailDest = emailDest;
    }

    @Override
    public void writeFile(InputStream fileData, long size, String name, String contentType)
            throws IOException {
        emailSender = new EmailSenderImpl("smtp.mail.host");
        emailSender.sendEmail("", emailDest, "", "", name, fileData);

    }
}