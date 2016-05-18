/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpParams;
import org.codice.alliance.nsili.endpoint.requests.GetRelatedFilesRequestImpl;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.NO_IMPLEMENT;

import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.NameListHolder;
import org.codice.alliance.nsili.common.UCO.State;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;

public class TestGetRelatedFilesRequestImpl {

    private static final int TEST_PORT = 21000;

    private GetRelatedFilesRequestImpl relatedFilesRequest;

    private HttpClient mockHttpClient = mock(HttpClient.class);

    @Before
    public void setUp() throws Exception {
        List<Metacard> testMetacards = getTestMetacards();
        FileLocation location = getTestFileLocation();
        relatedFilesRequest = new GetRelatedFilesRequestImpl(testMetacards,
                location,
                "THUMBNAIL",
                TEST_PORT);
        relatedFilesRequest.setHttpClient(mockHttpClient);
        setupMocks();
    }

    @Test
    public void testComplete200() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(getGoodResponse(200));
        NameListHolder locations = new NameListHolder();
        State state = relatedFilesRequest.complete(locations);
        assertValidState(state, locations);
    }

    @Test
    public void testComplete201() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(getGoodResponse(201));
        NameListHolder locations = new NameListHolder();
        State state = relatedFilesRequest.complete(locations);
        assertValidState(state, locations);
    }

    @Test
    public void testComplete202() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(getGoodResponse(202));
        NameListHolder locations = new NameListHolder();
        State state = relatedFilesRequest.complete(locations);
        assertValidState(state, locations);
    }

    @Test
    public void testComplete204() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(getGoodResponse(204));
        NameListHolder locations = new NameListHolder();
        State state = relatedFilesRequest.complete(locations);
        assertValidState(state, locations);
    }

    @Test
    public void testCompleteNoPort() throws Exception {
        NameListHolder locations = new NameListHolder();
        GetRelatedFilesRequestImpl noPortRequest = new GetRelatedFilesRequestImpl(getTestMetacards(),
                getTestFileLocation(),
                "THUMBNAIL",
                null);
        noPortRequest.setHttpClient(mockHttpClient);
        State state = noPortRequest.complete(locations);
        assertValidState(state, locations);
    }

    @Test
    public void testCompleteBadStatus() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(getBadResponse());
        NameListHolder locations = new NameListHolder();
        State state = relatedFilesRequest.complete(locations);
        assertValidStateNoLocs(state, locations);
    }

    @Test
    public void testNoHostLocation() throws Exception {
        FileLocation fileLocation = getTestFileLocation();
        fileLocation.host_name = "";
        NameListHolder locations = new NameListHolder();
        GetRelatedFilesRequestImpl noPortRequest = new GetRelatedFilesRequestImpl(getTestMetacards(),
                fileLocation,
                "THUMBNAIL",
                null);
        noPortRequest.setHttpClient(mockHttpClient);
        State state = noPortRequest.complete(locations);
        assertValidStateNoLocs(state, locations);
    }

    @Test
    public void testNoThumbnail() throws Exception {
        NameListHolder locations = new NameListHolder();
        GetRelatedFilesRequestImpl noPortRequest = new GetRelatedFilesRequestImpl(getBadMetacards(),
                getTestFileLocation(),
                "THUMBNAIL",
                null);
        noPortRequest.setHttpClient(mockHttpClient);
        State state = noPortRequest.complete(locations);
        assertValidStateNoLocs(state, locations);
    }

    @Test
    public void testNotThumbnailType() throws Exception {
        NameListHolder locations = new NameListHolder();
        GetRelatedFilesRequestImpl noPortRequest = new GetRelatedFilesRequestImpl(getBadMetacards(),
                getTestFileLocation(),
                "OVERVIEW",
                null);
        noPortRequest.setHttpClient(mockHttpClient);
        State state = noPortRequest.complete(locations);
        assertValidStateNoLocs(state, locations);
    }

    @Test (expected = NO_IMPLEMENT.class)
    public void testRegisterCallback() throws Exception {
        relatedFilesRequest.register_callback(null);
    }

    @Test
    public void testFreeCallback() throws Exception {
        relatedFilesRequest.free_callback("123");
    }

    private void assertValidState(State state, NameListHolder locations) {
        assertThat(state, is(State.COMPLETED));
        assertThat(locations, notNullValue());
        assertThat(locations.value, notNullValue());
        assertThat(locations.value.length, is(1));
    }

    private void assertValidStateNoLocs(State state, NameListHolder locations) {
        assertThat(state, is(State.COMPLETED));
        assertThat(locations, notNullValue());
        assertThat(locations.value, notNullValue());
        assertThat(locations.value.length, is(0));
    }

    private List<Metacard> getTestMetacards() {
        List<Metacard> testMetacards = new ArrayList<>();

        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(UUID.randomUUID()
                .toString());
        testMetacard.setTitle("JUnit Test Card");
        testMetacard.setThumbnail(new byte[0]);

        testMetacards.add(testMetacard);
        return testMetacards;
    }

    private List<Metacard> getBadMetacards() {
        List<Metacard> testMetacards = new ArrayList<>();

        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(UUID.randomUUID()
                .toString());
        testMetacard.setTitle("JUnit Test Card");

        testMetacards.add(testMetacard);
        return testMetacards;
    }

    private FileLocation getTestFileLocation() {
        FileLocation thumbnailLoc = new FileLocation("user",
                "pass",
                "localhost",
                "/nsili/file",
                null);
        return thumbnailLoc;
    }

    private void setupMocks() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(getGoodResponse(200));
    }

    private HttpResponse getGoodResponse(int statusCode) {
        HttpResponse response = new HttpResponse() {
            @Override
            public StatusLine getStatusLine() {
                StatusLine statusLine = new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public int getStatusCode() {
                        return statusCode;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "Success";
                    }
                };
                return statusLine;
            }

            @Override
            public void setStatusLine(StatusLine statusLine) {

            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i) {

            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

            }

            @Override
            public void setStatusCode(int i) throws IllegalStateException {

            }

            @Override
            public void setReasonPhrase(String s) throws IllegalStateException {

            }

            @Override
            public HttpEntity getEntity() {
                return null;
            }

            @Override
            public void setEntity(HttpEntity httpEntity) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public void setLocale(Locale locale) {

            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public boolean containsHeader(String s) {
                return false;
            }

            @Override
            public Header[] getHeaders(String s) {
                return new Header[0];
            }

            @Override
            public Header getFirstHeader(String s) {
                return null;
            }

            @Override
            public Header getLastHeader(String s) {
                return null;
            }

            @Override
            public Header[] getAllHeaders() {
                return new Header[0];
            }

            @Override
            public void addHeader(Header header) {

            }

            @Override
            public void addHeader(String s, String s1) {

            }

            @Override
            public void setHeader(Header header) {

            }

            @Override
            public void setHeader(String s, String s1) {

            }

            @Override
            public void setHeaders(Header[] headers) {

            }

            @Override
            public void removeHeader(Header header) {

            }

            @Override
            public void removeHeaders(String s) {

            }

            @Override
            public HeaderIterator headerIterator() {
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String s) {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public void setParams(HttpParams httpParams) {

            }
        };

        return response;
    }

    private HttpResponse getBadResponse() {
        HttpResponse response = new HttpResponse() {
            @Override
            public StatusLine getStatusLine() {
                StatusLine statusLine = new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public int getStatusCode() {
                        return 404;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "Test Bad Reponse";
                    }
                };
                return statusLine;
            }

            @Override
            public void setStatusLine(StatusLine statusLine) {

            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i) {

            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

            }

            @Override
            public void setStatusCode(int i) throws IllegalStateException {

            }

            @Override
            public void setReasonPhrase(String s) throws IllegalStateException {

            }

            @Override
            public HttpEntity getEntity() {
                return null;
            }

            @Override
            public void setEntity(HttpEntity httpEntity) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public void setLocale(Locale locale) {

            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public boolean containsHeader(String s) {
                return false;
            }

            @Override
            public Header[] getHeaders(String s) {
                return new Header[0];
            }

            @Override
            public Header getFirstHeader(String s) {
                return null;
            }

            @Override
            public Header getLastHeader(String s) {
                return null;
            }

            @Override
            public Header[] getAllHeaders() {
                return new Header[0];
            }

            @Override
            public void addHeader(Header header) {

            }

            @Override
            public void addHeader(String s, String s1) {

            }

            @Override
            public void setHeader(Header header) {

            }

            @Override
            public void setHeader(String s, String s1) {

            }

            @Override
            public void setHeaders(Header[] headers) {

            }

            @Override
            public void removeHeader(Header header) {

            }

            @Override
            public void removeHeaders(String s) {

            }

            @Override
            public HeaderIterator headerIterator() {
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String s) {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public void setParams(HttpParams httpParams) {

            }
        };

        return response;
    }
}
