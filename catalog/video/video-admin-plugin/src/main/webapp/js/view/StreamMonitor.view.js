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
/*global define*/
define([
        'jquery',
        'backbone',
        'underscore',
        'marionette',
        'handlebars',
        'icanhaz',
        'text!templates/streamMonitorPage.handlebars',
        'text!templates/streamMonitorTable.handlebars',
        'text!templates/addConfigurationModal.handlebars',
        'js/view/Modal'
    ],
    function ($, Backbone, _, Marionette, Handlebars, ich, streamMonitorPage, streamMonitorTable, addConfigurationModal, Modal) {

        var StreamMonitorView = {};

        ich.addTemplate('streamMonitorPage', streamMonitorPage);
        ich.addTemplate('streamMonitorTable', streamMonitorTable);
        ich.addTemplate('addConfigurationModal', addConfigurationModal);

        StreamMonitorView.StreamMonitorPage = Marionette.Layout.extend({
            template: 'streamMonitorPage',
            regions: {
                usageTable: '.streamDataTable'
            },
            initialize : function () {
              _.bindAll.apply(_, [this].concat(_.functions(this)));
            },
            onRender: function () {
                this.usageTable.show(new StreamMonitorView.StreamMonitorTable({model : this.model}));
            }
        });

        StreamMonitorView.StreamMonitorTable = Marionette.Layout.extend({
            template: 'streamMonitorTable',
            events : {
                'click .deleteicon' : 'deleteConfiguration',
                'click .startMonitoring' : 'startMonitoring',
                'click .stopMonitoring' : 'stopMonitoring',
                'click .showCreateModal': 'showCreateModal',
                'click .showUpdateModal': 'showUpdateModal'
            },
            regions: {
                modalRegion: '.updateModalRegion'
            },
            initialize : function () {
              _.bindAll.apply(_, [this].concat(_.functions(this)));
                this.listenTo(this.model, 'change:configurations', this.render);
            },
            deleteConfiguration: function(data) {
                var user = $(data.target);
                var id = user[0].name;
                this.model.deleteConfiguration(id);
            },
            showUpdateModal : function(data) {
                var td = $(data.target);
                var pid = td[0].getAttribute("name");

                if(this.model.getServiceConfigurationByPid(pid).running) {
                    return;
                }
                var configuration = this.model.getServiceConfigurationByPid(pid);
                this.modal = new StreamMonitorView.Modal({model : this.model});
                this.modal.configuration = configuration;
                this.showModal();
            },
            showCreateModal: function() {
                this.modal = new StreamMonitorView.Modal({model : this.model});
                this.showModal();
            },
            showModal : function() {
                this.modalRegion.show(this.modal);
                this.modal.show();
                this.$(".modal").removeClass('fade');
            },
            startMonitoring : function(data) {
                var user = $(data.target);
                var id = user[0].getAttribute("name");
                this.model.startMonitoring(id);
            },
            stopMonitoring : function(data) {
                var user = $(data.target);
                var id = user[0].getAttribute("name");
                this.model.stopMonitoring(id);
            }
        });

        StreamMonitorView.Modal = Modal.extend({
            template: 'addConfigurationModal',
            events: {
                'click .addConfiguration' : 'submitConfiguration',
                'change .feedUrl' : 'checkMulticast',
                'keyup .feedUrl' : 'checkMulticast'
            },
            initialize: function() {
                 Modal.prototype.initialize.apply(this, arguments);
            },
            onRender: function() {
                this.setupPopOver('[data-toggle="feed-name-popover"]', 'Title of the parent metacard.');
                this.setupPopOver('[data-toggle="url-popover"]', 'Specifies the network address (e.g. udp://localhost:50000) to be monitored. The address must be resolvable. ' +
                    'For multicast, the address must be an IP address. When a multicast IP address is entered, a list of available network interfaces will be displayed.');
                this.setupPopOver('[data-toggle="max-dur-popover"]', 'Maximum elapsed time in minutes before rollover. Must be >=1.');
                this.setupPopOver('[data-toggle="max-size-popover"]', 'Maximum file size (MB) before rollover. Must be >=1.');
                this.setupPopOver('[data-toggle="file-templ-popover"]', 'Filename template for each chunk. The template may contain any number of the sequence "%{date=FORMAT}" where FORMAT is a Java SimpleDateFormat. Must be non-blank.');
                this.setupPopOver('[data-toggle="delay-popover"]', 'Delay updates when creating metacards to avoid retries. Slower systems require a longer delay. The minimum value is 0 seconds and the maximum value is 60 seconds. (seconds)');
                this.setupPopOver('[data-toggle="distance-tolerance-popover"]', 'Distance tolerance used to simplify geospatial metadata during video stream processing. The tolerance must be non-negative and the units are degrees. ' +
                    'Large values for distance tolerance will reduce the accuracy of the geometric data. Extremely small values for distance tolerance can adversely affect system performance. The recommended range is [0.001,0.1].');
                this.setupPopOver('[data-toggle="network-interface-popover"]', 'Select the network interface to use when receiving multicast.');
                if(typeof this.configuration !== "undefined") {
                    this.renderFields();
                    this.$(".modal-title").text("Update Stream");
                } else {
                    this.renderDefaultFields();
                }
                this.checkMulticast();
            },
            renderDefaultFields: function() {
                this.$(".feedName").val("MPEG-TS UDP Stream");
                this.$(".feedUrl").val("udp://127.0.0.1:50000");
                this.$(".feedMaxDuration").val(1);
                this.$(".feedMaxClipSize").val(10);
                this.$(".feedFileNameTemplate").val("mpegts-stream-%{date=yyyy-MM-dd_hh:mm:ss}");
                this.$(".feedInitialDelay").val(2);
                this.$(".feedDistanceTolerance").val(0.01);
            },
            renderFields: function() {
                this.$(".feedName").val(this.configuration.title);
                this.$(".feedUrl").val(this.configuration.url);
                this.$(".networkInterface").val(this.configuration.networkInterface);
                this.$(".feedMaxDuration").val(this.configuration.elapsedTimeRolloverCondition);
                this.$(".feedMaxClipSize").val(this.configuration.megabyteCountRolloverCondition);
                this.$(".feedFileNameTemplate").val(this.configuration.fileNameTemplate);
                this.$(".feedInitialDelay").val(this.configuration.metacardUpdateInitialDelay);
                this.$(".feedDistanceTolerance").val(this.configuration.distanceTolerance);
            },
            ipToNumber: function(s) {
                var arr = s.split(".");
                var n = 0;
                for (var i = 0; i < 4; i++) {
                    n = n * 256;
                    n += parseInt(arr[i], 10);
                }
                return n;
            },
            isMulticastUrl: function(url) {
                var re = /udp:\/\/([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}):[0-9]+/;

                var parts = re.exec(url);

                if(parts !== null && parts.length >= 2) {
                    var ipNum = this.ipToNumber(parts[1]);

                    /* need to disable jslint because it complains about bit-wise operations */

                    /*jslint bitwise: true*/

                    ipNum = ipNum >> 24;

                    var isMulti = (ipNum & 0xF0) === 0xE0;

                    /*jslint bitwise: false*/

                    return isMulti;
                } else {
                    return false;
                }
            },
            checkMulticast: function() {
                var url = this.$el.find('.feedUrl').val();

                this.$el.toggleClass("parentmulti", this.isMulticastUrl(url));
            },
            submitConfiguration: function() {
                var name = this.$(".feedName").val();
                var url = this.$(".feedUrl").val().split("/").join("!/");
                var networkInterface = this.isMulticastUrl(this.$(".feedUrl").val()) ? this.$(".networkInterface").val() : null;
                var elapsedTimeRolloverCondition = parseInt(this.$(".feedMaxDuration").val()) * 60 * 1000;
                var megabyteCountRolloverCondition = parseInt(this.$(".feedMaxClipSize").val());
                var metacardUpdateInitialDelay =  parseInt(this.$(".feedInitialDelay").val());
                var distanceTolerance =  parseFloat(this.$(".feedDistanceTolerance").val());

                if(isNaN(distanceTolerance) || isNaN(metacardUpdateInitialDelay) || isNaN(elapsedTimeRolloverCondition) || isNaN(megabyteCountRolloverCondition)) {
                    return;
                }

                var fileNameTemplate = encodeURIComponent(this.$(".feedFileNameTemplate").val());

                var properties = {parentTitle : name,
                    monitoredAddress : url,
                    networkInterface : networkInterface,
                    elapsedTimeRolloverCondition : elapsedTimeRolloverCondition,
                    megabyteCountRolloverCondition : megabyteCountRolloverCondition,
                    filenameTemplate : fileNameTemplate,
                    metacardUpdateInitialDelay: metacardUpdateInitialDelay,
                    distanceTolerance : distanceTolerance};

                if(typeof this.configuration === "undefined") {
                    this.addConfiguration(properties);
                } else {
                    this.updateConfiguration(this.configuration.id, properties);
                }
            },
            updateConfiguration: function(pid, properties) {
                this.model.updateConfiguration(pid, properties);
            },
            addConfiguration: function (properties) {
                this.model.createConfiguration(properties);
            },
            setupPopOver: function(selector, content) {
                var options = {
                    trigger: 'hover',
                    content: content
                };
                this.$el.find(selector).popover(options);
            }
        });
        return StreamMonitorView;
    });
