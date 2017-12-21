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
package org.codice.alliance.nsili.endpoint.requests;

import ddf.catalog.data.Metacard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.GetRelatedFilesRequestPOA;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS._RequestManagerStub;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameListHolder;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.endpoint.managers.ProductMgrImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.slf4j.LoggerFactory;

public class GetRelatedFilesRequestImpl extends GetRelatedFilesRequestPOA {

  private static final String DEFAULT_PROTOCOL = "http";

  private static final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(GetRelatedFilesRequestImpl.class);

  private List<Metacard> metacards;

  private FileLocation location;

  private String type;

  private Integer port;

  private HttpClient httpClient;

  public GetRelatedFilesRequestImpl(
      List<Metacard> metacards, FileLocation location, String type, Integer port) {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    httpClient = clientBuilder.build();

    setMetacards(metacards);
    setFileLocation(location);
    setType(type);
    setPort(port);
  }

  public void setMetacards(List<Metacard> metacards) {
    this.metacards = metacards;
  }

  public void setFileLocation(FileLocation location) {
    this.location = location;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  @Override
  public State complete(NameListHolder locations) throws ProcessingFault, SystemFault {
    List<String> fileNames = new ArrayList<>();
    if (type.equals(ProductMgrImpl.THUMBNAIL_RELATED_FILE)
        && StringUtils.isNotBlank(location.host_name)) {
      for (Metacard metacard : metacards) {
        evaluateThumbnailNames(fileNames, metacard);
      }
    }

    if (fileNames.isEmpty()) {
      locations.value = new String[0];
    } else {
      locations.value = fileNames.toArray(new String[0]);
    }
    return State.COMPLETED;
  }

  private void evaluateThumbnailNames(List<String> fileNames, Metacard metacard) {
    if (metacard.getThumbnail() != null) {
      try {
        String thumbnailName = storeThumbnail(metacard);
        if (thumbnailName != null) {
          fileNames.add(thumbnailName);
        }
      } catch (IOException | MimeTypeException e) {
        LOGGER.debug("Unable to store thumbnail:", e);
      }
    }
  }

  @Override
  public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
    return new RequestDescription();
  }

  @Override
  public void set_user_info(String message)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public Status get_status() throws ProcessingFault, SystemFault {
    return new Status();
  }

  @Override
  public DelayEstimate get_remaining_delay() throws ProcessingFault, SystemFault {
    return new DelayEstimate();
  }

  @Override
  public void cancel() throws ProcessingFault, SystemFault {
    return;
  }

  @Override
  public String register_callback(Callback acallback)
      throws InvalidInputParameter, ProcessingFault, SystemFault {
    throw new NO_IMPLEMENT("GetRelatedFilesRequest doesn't support callbacks");
  }

  @Override
  public void free_callback(String id) throws InvalidInputParameter, ProcessingFault, SystemFault {
    // This method is not expected to be called
  }

  @Override
  public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
    return new _RequestManagerStub();
  }

  private String storeThumbnail(Metacard metacard) throws IOException, MimeTypeException {
    String id = metacard.getId();

    byte[] thumbnailBytes = metacard.getThumbnail();
    TikaInputStream tis = TikaInputStream.get(thumbnailBytes);
    MediaType mediaType = DETECTOR.detect(tis, new Metadata());
    MimeType mimeType =
        TikaConfig.getDefaultConfig().getMimeRepository().forName(mediaType.toString());

    String fileName = id + "-THUMBNAIL" + mimeType.getExtension();

    String urlStr =
        DEFAULT_PROTOCOL
            + "://"
            + location.host_name
            + (port == null ? "" : ":" + port)
            + location.path_name
            + "/"
            + fileName;

    LOGGER.debug("Storing thumbnail for {} at location: {}", metacard.getTitle(), urlStr);

    HttpPut httpPut = new HttpPut(urlStr);
    HttpEntity entity = new ByteArrayEntity(thumbnailBytes);
    httpPut.setEntity(entity);

    Header contentTypeHeader = new BasicHeader("Content-Type", mediaType.toString());
    httpPut.addHeader(contentTypeHeader);
    HttpResponse response = httpClient.execute(httpPut);
    int statusCode = response.getStatusLine().getStatusCode();
    if (!(statusCode == HttpStatus.SC_OK
        || statusCode == HttpStatus.SC_CREATED
        || statusCode == HttpStatus.SC_ACCEPTED
        || statusCode == HttpStatus.SC_NO_CONTENT)) {
      fileName = null;
      LOGGER.debug(
          "Unable to PUT file: code: {}, status: {}",
          statusCode,
          response.getStatusLine().getReasonPhrase());
    }

    return fileName;
  }

  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }
}
