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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.codice.alliance.nsili.endpoint.managers.EmailConfiguration;

import ddf.catalog.data.Metacard;

/**
 * Send data as an email attachment.
 */
public class EmailDestinationSink implements DestinationSink {

    private static final String METACARD_IDS = "%metacard.ids%";

    private static final String METACARD_TITLES = "%metacard.titles%";

    private final String emailDest;

    private final EmailConfiguration emailConfiguration;

    public EmailDestinationSink(String emailDest, EmailConfiguration emailConfiguration) {
        this.emailDest = emailDest;
        this.emailConfiguration = emailConfiguration;
    }

    @Override
    public void writeFile(InputStream fileData, long size, String name, String contentType,
            List<Metacard> metacards) throws IOException {
        emailConfiguration.getEmailSender()
                .sendEmail(emailConfiguration.getFromEmail(),
                        emailDest,
                        emailConfiguration.getSubject(),
                        replaceTitles(replaceIds(emailConfiguration.getBody(), metacards), metacards),
                        Collections.singletonList(new ImmutablePair<>(name, fileData)));
    }

    private String replaceIds(String bodyTemplate, List<Metacard> metacards) {
        return bodyTemplate.replace(METACARD_IDS,
                metacards.stream()
                        .map(Metacard::getId)
                        .collect(Collectors.joining(", ")));
    }

    private String replaceTitles(String bodyTemplate, List<Metacard> metacards) {
        return bodyTemplate.replace(METACARD_TITLES,
                metacards.stream()
                        .map(Metacard::getTitle)
                        .collect(Collectors.joining(", ")));
    }

}