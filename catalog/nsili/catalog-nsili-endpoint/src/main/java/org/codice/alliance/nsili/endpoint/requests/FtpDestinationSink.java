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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.codice.alliance.nsili.common.GIAS.Destination;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;

/**
 * Write data to an FTP server.
 */
public class FtpDestinationSink implements DestinationSink {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderRequestImpl.class);

    private Destination destination;

    private FileLocation fileLocation;

    private String protocol;

    private int port;

    FtpDestinationSink(FileLocation fileLocation, int port, String protocol) {
        this.fileLocation = fileLocation;
        this.protocol = protocol;
        this.port = port;
    }

    @Override
    public void writeFile(InputStream fileData, long size, String name, String contentType,
            List<Metacard> metacards) throws IOException {
        CloseableHttpClient httpClient = null;
        String urlPath = protocol + "://" + fileLocation.host_name + ":" + port + "/"
                + fileLocation.path_name + "/" + name;

        LOGGER.debug("Writing ordered file to URL: {}", urlPath);

        try {
            HttpPut putMethod = new HttpPut(urlPath);
            putMethod.addHeader(HTTP.CONTENT_TYPE, contentType);
            HttpEntity httpEntity = new InputStreamEntity(fileData, size);
            putMethod.setEntity(httpEntity);

            if (StringUtils.isNotEmpty(fileLocation.user_name) && fileLocation.password != null) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(fileLocation.host_name, port),
                        new UsernamePasswordCredentials(fileLocation.user_name,
                                fileLocation.password));
                httpClient = HttpClients.custom()
                        .setDefaultCredentialsProvider(credsProvider)
                        .build();
            } else {
                httpClient = HttpClients.createDefault();
            }

            httpClient.execute(putMethod);
            fileData.close();
            putMethod.releaseConnection();
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

}
