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

;(function() {
  'use strict';

require.config({
    paths: {

        bootstrap: '../../webjars/bootstrap/3.3.7/dist/js/bootstrap.min',
        moment: '../../webjars/moment/2.20.1/min/moment.min',

        // backbone
        backbone: '../../webjars/backbone/1.1.2/backbone',

        underscore: '../../webjars/underscore/1.8.3/underscore-min',

        marionette: '../../webjars/marionette/1.8.8/lib/backbone.marionette.min',

        modelbinder: '../../webjars/backbone.modelbinder/1.1.0/Backbone.ModelBinder',

        // application
        application: 'js/application',

        // jquery
        jquery: '../../webjars/jquery/3.2.1/dist/jquery.min',
        jqueryuiCore: '../../webjars/jquery-ui/1.10.4/ui/minified/jquery.ui.core.min',
        "jquery.ui.widget": '../../webjars/jquery-ui/1.10.4/ui/minified/jquery.ui.widget.min',

        // handlebars
        handlebars: '../../webjars/handlebars/2.0.0/handlebars.min',
        icanhaz: 'js/ich',

        // require plugins
        text: '../../webjars/requirejs-plugins/1.0.3/lib/text',
        css: '../../webjars/require-css/0.1.10/css.min'
    },

    shim: {

        backbone: {
            deps: ['underscore', 'jquery'],
            exports: 'Backbone'
        },

        modelbinder: {
            deps: ['underscore', 'jquery', 'backbone']
        },

        marionette: {
            deps: ['jquery', 'underscore', 'backbone'],
            exports: 'Marionette'
        },

        underscore: {
            exports: '_'
        },

        handlebars: {
            exports: 'Handlebars'
        },

        icanhaz: {
            deps: ['jquery', 'handlebars'],
            exports: 'ich'
        },

        moment: {
            exports: 'moment'
        },

        jqueryuiCore: ['jquery'],

        bootstrap: ['jquery']
    },

    waitSeconds: 0
});

  require([
    'jquery',
    'backbone',
    'marionette',
    'icanhaz',
    'js/application',
    'modelbinder',
    'bootstrap',
  ], function($, Backbone, Marionette, ich, Application) {
    var app = Application.App;
    // Once the application has been initialized (i.e. all initializers have completed), start up
    // Backbone.history.
    app.on('initialize:after', function() {
      Backbone.history.start();
      //bootstrap call for tabs
      $('tabs').tab();
    });

    if (window) {
      // make ddf object available on window.  Makes debugging in chrome console much easier
      window.app = app;
      if (!window.console) {
        window.console = {
          log: function() {
            // no op
          },
        };
      }
    }

    // Actually start up the application.
    app.start();

    require(['js/module'], function() {});
  });
})();