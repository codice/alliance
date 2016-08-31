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
package org.codice.alliance.nsili.sourcestoquery.ui.service;

import static org.apache.commons.lang.Validate.notNull;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.codice.alliance.nsili.endpoint.QuerySources;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;

public class SourcesToQuery implements SourcesToQueryMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourcesToQuery.class);

    private ObjectName objectName;

    private MBeanServer mBeanServer;

    private final BundleContext blueprintBundleContext;

    private final CatalogFramework catalogFramework;

    private final QuerySources querySources;

    public static final String SOURCE_ID_TITLE = "sourceId";

    public SourcesToQuery(BundleContext blueprintBundleContext, CatalogFramework catalogFramework,
            QuerySources querySources) {
        notNull(blueprintBundleContext, "blueprintBundleContext must not be null");
        notNull(catalogFramework, "catalogFramework must not be null");
        notNull(querySources, "querySources must not be null");
        this.blueprintBundleContext = blueprintBundleContext;
        this.catalogFramework = catalogFramework;
        this.querySources = querySources;
    }

    public void init() {
        registerMbean();
    }

    @Override
    public Set<Map<String, String>> queriedSources() {
        return toSourceIdMappedSet(querySources.getQuerySources());
    }

    @Override
    public Set<Map<String, String>> queryableSources() {
        Set<String> queryableSources = new HashSet<>(catalogFramework.getSourceIds());
        queryableSources.removeAll(querySources.getQuerySources());

        return toSourceIdMappedSet(queryableSources);
    }

    private Set<Map<String, String>> toSourceIdMappedSet(Collection<String> sourceIdList) {
        return sourceIdList.stream()
                .map(s -> Collections.singletonMap(SOURCE_ID_TITLE, s))
                .collect(Collectors.toSet());
    }

    @Override
    public void addQueriedSource(String sourceId) {
        querySources.addQuerySource(sourceId);
    }

    @Override
    public void removeQueriedSource(String sourceId) {
        querySources.removeQuerySource(sourceId);
    }

    private void registerMbean() {
        try {
            objectName = new ObjectName(SourcesToQuery.class.getName() + ":service=stream");
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (MalformedObjectNameException e) {
            LOGGER.error("Unable to create Sources to Query Helper MBean.", e);
        }
        if (mBeanServer == null) {
            return;
        }
        try {
            try {
                mBeanServer.registerMBean(this, objectName);
                LOGGER.info("Registered Sources to Query Helper MBean under object name: {}",
                        objectName.toString());
            } catch (InstanceAlreadyExistsException e) {
                mBeanServer.unregisterMBean(objectName);
                mBeanServer.registerMBean(this, objectName);
                LOGGER.info("Re-registered Sources to Query Helper MBean");
            }
        } catch (MBeanRegistrationException | InstanceNotFoundException | InstanceAlreadyExistsException | NotCompliantMBeanException e) {
            LOGGER.error("Could not register MBean [{}].", objectName.toString(), e);
        }

    }

    public void destroy() {
        try {
            if (objectName != null && mBeanServer != null) {
                mBeanServer.unregisterMBean(objectName);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception unregistering MBean: ", e);
        }
    }

    public BundleContext getContext() {
        return this.blueprintBundleContext;
    }

    public CatalogFramework getCatalogFramework() {
        return this.catalogFramework;
    }

    public QuerySources getQuerySources() {
        return this.querySources;
    }
}
