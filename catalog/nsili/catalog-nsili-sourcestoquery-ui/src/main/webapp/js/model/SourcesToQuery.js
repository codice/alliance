/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
/*jshint browser: true */

define(['backbone',
        'jquery'],
    function (Backbone, $) {

        var SOURCES_TO_QUERY_URL = "/admin/jolokia/exec/org.codice.alliance.nsili.sourcestoquery.ui.service.SourcesToQuery:service=stream/";

        var SourcesToQuery = {};

        SourcesToQuery.MonitorModel = Backbone.Model.extend({

            initialize: function() {
                this.pollSources();
            },
            pollSources: function() {
                var that = this;
                (function poll() {
                    setTimeout(function() {
                        that.getQueriedSources();
                        that.getQueryableSources();
                        poll();
                    }, 1000);
                })();
            },
            getQueriedSources: function() {
                var url = SOURCES_TO_QUERY_URL + "queriedSources";
                var that = this;
                $.ajax({
                    url: url,
                    dataType: 'json',
                    success: function(data) {
                        that.set({'queriedSources' : data.value});
                    }
                });
            },
            getQueryableSources: function() {
                var url = SOURCES_TO_QUERY_URL + "queryableSources";
                var that = this;
                $.ajax({
                    url: url,
                    dataType: 'json',
                    success: function(data) {
                        that.set({'queryableSources' : data.value});
                    }
                });
            },
            addQueriedSource: function(sourceId) {
                $.ajax({
                    url: SOURCES_TO_QUERY_URL + "addQueriedSource/" + sourceId,
                    dataType: 'text'
                });
            },
            removeQueriedSource: function(sourceId) {
                $.ajax({
                    url: SOURCES_TO_QUERY_URL + "removeQueriedSource/" + sourceId,
                    dataType: 'text'
                });
            }
        });
        return SourcesToQuery;
    });