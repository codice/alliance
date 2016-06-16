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
        'backbone.marionette',
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

        StreamMonitorView.StreamMonitorPage = Marionette.LayoutView.extend({
            template: 'streamMonitorPage',
            regions: {
                usageTable: '.streamDataTable'
            },
            initialize : function () {
                _.bindAll(this);
            },
            onRender: function () {
                this.usageTable.show(new StreamMonitorView.StreamMonitorTable({model : this.model}));
            }
        });

        StreamMonitorView.StreamMonitorTable = Marionette.LayoutView.extend({
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
                _.bindAll(this);
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
                'click .addConfiguration' : 'submitConfiguration'
            },
            initialize: function() {
                 Modal.prototype.initialize.apply(this, arguments);
            },
            onRender: function() {
                this.setupPopOver('[data-toggle="feed-name-popover"]', 'Title of the parent metacard.');
                this.setupPopOver('[data-toggle="url-popover"]', 'Specifies the network address (e.g. udp://localhost:50000) to be monitored. The address must be resolvable.');
                this.setupPopOver('[data-toggle="max-dur-popover"]', 'Maximum file size (MB) before rollover. Must be >=1.');
                this.setupPopOver('[data-toggle="max-size-popover"]', 'Maximum elapsed time in minutes before rollover. Must be >=1.');
                this.setupPopOver('[data-toggle="file-templ-popover"]', 'Filename template for each chunk. The template may contain any number of the sequence "%{date=FORMAT}" where FORMAT is a Java SimpleDateFormat. Must be non-blank.');
                this.setupPopOver('[data-toggle="delay-popover"]', 'Delay updates when creating metacards to avoid retries. Slower systems require a longer delay. The minimum value is 0 seconds and the maximum value is 60 seconds. (seconds)');
                if(typeof this.configuration !== "undefined") {
                    this.renderFields();
                    this.$(".modal-title").text("Update Stream");
                } else {
                    this.renderDefaultFields();
                }
            },
            renderDefaultFields: function() {
                this.$(".feedName").val("MPEG-TS UDP Stream");
                this.$(".feedUrl").val("udp://127.0.0.1:50000");
                this.$(".feedMaxDuration").val(1);
                this.$(".feedMaxClipSize").val(10);
                this.$(".feedFileNameTemplate").val("mpegts-stream-%{date=yyyy-MM-dd_hh:mm:ss}");
                this.$(".feedInitialDelay").val(2);
            },
            renderFields: function() {
                this.$(".feedName").val(this.configuration.title);
                this.$(".feedUrl").val(this.configuration.url);
                this.$(".feedMaxDuration").val(this.configuration.elapsedTimeRolloverCondition);
                this.$(".feedMaxClipSize").val(this.configuration.byteCountRolloverCondition);
                this.$(".feedFileNameTemplate").val(this.configuration.fileNameTemplate);
                this.$(".feedInitialDelay").val(this.configuration.metacardUpdateInitialDelay);
            },
            submitConfiguration: function() {
                var name = this.$(".feedName").val();
                var url = this.$(".feedUrl").val().split("/").join("!/");
                var elapsedTimeRolloverCondition = parseInt(this.$(".feedMaxDuration").val()) * 60 * 1000;
                var byteCountRolloverCondition = parseInt(this.$(".feedMaxClipSize").val()) * 1000 * 1000;
                var metacardUpdateInitialDelay =  parseInt(this.$(".feedInitialDelay").val());

                if(isNaN(metacardUpdateInitialDelay) || isNaN(elapsedTimeRolloverCondition) || isNaN(byteCountRolloverCondition)) {
                    return;
                }

                var fileNameTemplate = encodeURIComponent(this.$(".feedFileNameTemplate").val());

                var properties = {parentTitle : name,
                    monitoredAddress : url, elapsedTimeRolloverCondition : elapsedTimeRolloverCondition,
                    byteCountRolloverCondition : byteCountRolloverCondition,
                    filenameTemplate : fileNameTemplate,
                    metacardUpdateInitialDelay: metacardUpdateInitialDelay };

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