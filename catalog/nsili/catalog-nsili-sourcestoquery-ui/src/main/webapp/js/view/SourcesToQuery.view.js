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
        'text!templates/sourcesToQueryPage.handlebars',
        'text!templates/sourcesToQueryTable.handlebars',
        'text!templates/addQueriedSourceModal.handlebars',
        'js/view/Modal'
    ],
    function ($, Backbone, _, Marionette, Handlebars, ich, sourcesToQueryPage, sourcesToQueryTable, addQueriedSourceModal, Modal) {

        var SourcesToQueryView = {};

        ich.addTemplate('sourcesToQueryPage', sourcesToQueryPage);
        ich.addTemplate('sourcesToQueryTable', sourcesToQueryTable);
        ich.addTemplate('addQueriedSourceModal', addQueriedSourceModal);

        Handlebars.registerHelper('exists', function(variable, options) {
            if (typeof variable !== 'undefined') {
                return options.fn(this);
            } else {
                return options.inverse(this);
            }
        });

        SourcesToQueryView.SourcesToQueryPage = Marionette.Layout.extend({
            template: 'sourcesToQueryPage',
            regions: {
                usageTable: '.queriedSourcesDataTable'
            },
            initialize : function () {
              _.bindAll.apply(_, [this].concat(_.functions(this)));
            },
            onRender: function () {
                this.setupPopOver('[data-toggle="sources-to-query-popover"]', 'NSILI Endpoint Sources to Query', 'Configured sources to query from the NSILI Endpoint. Empty list defaults to local only.');
                this.usageTable.show(new SourcesToQueryView.SourcesToQueryTable({model : this.model}));
            },
            setupPopOver : function(selector, title, description) {
                var options = {
                    trigger: 'hover',
                    content: description,
                    title: title
                };
                this.$el.find(selector).popover(options);
            }
        });

        SourcesToQueryView.SourcesToQueryTable = Marionette.Layout.extend({
            template: 'sourcesToQueryTable',
            events : {
                'click .delete-icon' : 'deleteQueriedSource',
                'click .show-create-modal' : 'showCreateModal'
            },
            regions: {
                modalRegion: '.updateModalRegion'
            },
            initialize : function () {
              _.bindAll.apply(_, [this].concat(_.functions(this)));
                this.listenTo(this.model, 'change:queriedSources', this.render);
            },
            deleteQueriedSource: function(data) {
                var user = $(data.target);
                var sourceId = user[0].name;
                this.model.removeQueriedSource(sourceId);
            },
            showCreateModal : function() {
                this.modal = new SourcesToQueryView.Modal({model : this.model});
                this.showModal();
            },
            showModal : function() {
                this.modalRegion.show(this.modal);
                this.modal.show();
                this.$(".modal").removeClass('fade');
            }
        });

        SourcesToQueryView.Modal = Modal.extend({
            template: 'addQueriedSourceModal',
            events: {
                'click .submit-queried-source-button' : 'addQueriedSource'
            },
            initialize : function() {
                 Modal.prototype.initialize.apply(this, arguments);
            },
            addQueriedSource : function() {
                var sourceId = this.$("#selectQueryableSource option:selected").val();
                this.model.addQueriedSource(sourceId);
            }
        });
        return SourcesToQueryView;
    });