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

        var CONFIGURATION_ADMIN_URL = "/admin/jolokia/exec/org.codice.ddf.ui.admin.api.ConfigurationAdmin:service=ui,version=2.3.0/";

        var STREAM_MONITOR_URL = "/admin/jolokia/exec/org.codice.alliance.video.ui.service.StreamMonitorHelper:service=stream/";

        var StreamMonitor = {};

        StreamMonitor.MonitorModel = Backbone.Model.extend({

            initialize: function() {
                this.set({'configurations' : []});
                this.pollConfigurationData();
            },
            pollConfigurationData: function() {
                var that = this;
                (function poll() {
                    setTimeout(function() {
                        that.getServiceConfigurations();
                        poll();
                    }, 1000);
                })();
            },
            getServiceConfigurations: function() {
                var that = this;

                $.ajax({
                    url: STREAM_MONITOR_URL + "udpStreamMonitors",
                    dataType: 'json',
                    success: function(data) {
                        if(data.value !== null && typeof data.value !== "undefined") {
                            that.set({'configurations' : that.parseConfigurationData(data.value)});
                        } else {
                            that.set({'configurations' : []});
                        }
                    }
                });
            },
            parseConfigurationData: function(configurations) {
                    var parsedData = [];
                    $.each(configurations, function(index, value) {
                        var url = value.monitoredAddress;
                        var maxDuration = value.elapsedTimeRolloverCondition / (60 * 1000);
                        var maxSize = value.byteCountRolloverCondition / (1000 * 1000);

                        parsedData.push({id : value.id,
                            title : value.parentTitle,
                            url : url,
                            elapsedTimeRolloverCondition : maxDuration,
                            byteCountRolloverCondition : maxSize,
                            startTime : value.startTime,
                            running: value.monitoring,
                            fileNameTemplate : value.filenameTemplate,
                            metacardUpdateInitialDelay : value.metacardUpdateInitialDelay });
                    });
                   return parsedData;
            },
            deleteConfiguration: function(servicePid) {
                $.ajax({
                    url: CONFIGURATION_ADMIN_URL + "deleteConfigurations/(service.pid=" + servicePid + ")",
                    dataType: 'json'
                });
            },
            createConfiguration: function(properties) {
                var that = this;
                $.ajax({
                    url: CONFIGURATION_ADMIN_URL + "createFactoryConfiguration/org.codice.alliance.video.stream.mpegts.UdpStreamMonitor",
                    dataType: 'json',
                    success: function(data) {
                        that.updateConfiguration(data.value, properties);
                    }
                });
            },
            updateConfiguration: function(pid, properties) {
                $.ajax({
                    url: CONFIGURATION_ADMIN_URL + "update/" + pid + "/" + JSON.stringify(properties),
                    dataType: 'json'
                });
            },
            startMonitoring: function(pid) {
                $.ajax({
                    url: STREAM_MONITOR_URL + "callStartMonitoringStreamByServicePid/" + pid,
                    dataType: 'json'
                });
            },
            stopMonitoring: function(pid) {
                $.ajax({
                    url: STREAM_MONITOR_URL + "callStopMonitoringStreamByServicePid/" + pid,
                    dataType: 'json'
                });
            },
            getServiceConfigurationByPid: function(pid) {
                var configurations = this.get('configurations');
                var configuration;
                $.each(configurations, function(index, value) {
                    if( value.id === pid) {
                        configuration = value;
                    }
                });
                return configuration;
            },
        });
        return StreamMonitor;
    });