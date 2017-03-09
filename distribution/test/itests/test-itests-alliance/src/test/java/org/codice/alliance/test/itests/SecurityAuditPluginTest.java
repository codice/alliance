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
package org.codice.alliance.test.itests;

import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.deleteMetacard;
import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.ingest;
import static org.codice.ddf.itests.common.catalog.CatalogTestCommons.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.codice.alliance.test.itests.common.AbstractAllianceIntegrationTest;
import org.codice.ddf.itests.common.WaitCondition;
import org.codice.ddf.itests.common.annotations.BeforeExam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.cm.Configuration;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SecurityAuditPluginTest extends AbstractAllianceIntegrationTest {
    private static final String[] REQUIRED_APPS = {"catalog-app", "solr-app", "security-app"};

    private String auditMessageFormat =
            "Attribute %s on metacard %s with value(s) %s was updated to value(s) %s";

    private String configUpdateMessage =
            "Security Audit Plugin configuration changed to audit : description";

    private String startedMessage = "Security Audit Plugin started";

    private String stoppedMessage = "Security Audit Plugin stopped";

    @BeforeExam
    public void beforeExam() throws Exception {
        basePort = getBasePort();
        getAdminConfig().setLogLevels();

        getServiceManager().waitForRequiredApps(REQUIRED_APPS);
        getServiceManager().waitForAllBundles();
        getCatalogBundle().waitForCatalogProvider();

        configureRestForGuest();
        getSecurityPolicy().waitForGuestAuthReady(REST_PATH.getUrl() + "?_wadl");
    }

    @Test
    public void testSecurityAuditPlugin() throws Exception {
        Configuration config = configAdmin.getConfiguration(
                "org.codice.alliance.catalog.plugin.security.audit.SecurityAuditPlugin",
                null);
        List attributes = new ArrayList<>();
        attributes.add("description");
        Dictionary properties = new Hashtable<>();
        properties.put("auditAttributes", attributes);
        config.update(properties);

        String logFilePath = System.getProperty("karaf.data") + "/log/security.log";

        WaitCondition.expect("Security log has log message: " + configUpdateMessage)
                .within(2, TimeUnit.MINUTES)
                .checkEvery(2, TimeUnit.SECONDS)
                .until(() -> getFileFromAbsolutePath(logFilePath).contains(configUpdateMessage));

        String id = ingest(getResourceAsString("metacard1.xml"), "text/xml");

        update(id, getResourceAsString("metacard2.xml"), "text/xml");

        String expectedLogMessage = String.format(auditMessageFormat,
                "description",
                id,
                "My Description",
                "My Description (Updated)");
        WaitCondition.expect("Securitylog has log message: " + expectedLogMessage)
                .within(2, TimeUnit.MINUTES)
                .checkEvery(2, TimeUnit.SECONDS)
                .until(() -> getFileFromAbsolutePath(logFilePath).contains(expectedLogMessage));

        deleteMetacard(id);
    }

    @Test
    public void testFeatureStartAndStop() throws Exception {
        String logFilePath = System.getProperty("karaf.data") + "/log/security.log";
        getServiceManager().stopFeature(true, "catalog-plugin-security-audit");
        WaitCondition.expect("Securitylog has log message: " + stoppedMessage)
                .within(2, TimeUnit.MINUTES)
                .checkEvery(2, TimeUnit.SECONDS)
                .until(() -> getFileFromAbsolutePath(logFilePath).contains(stoppedMessage));

        getServiceManager().startFeature(true, "catalog-plugin-security-audit");
        WaitCondition.expect("Securitylog has log message: " + startedMessage)
                .within(2, TimeUnit.MINUTES)
                .checkEvery(2, TimeUnit.SECONDS)
                .until(() -> getFileFromAbsolutePath(logFilePath).contains(startedMessage));
    }

    private String getResourceAsString(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(resourcePath);
        return IOUtils.toString(inputStream);
    }

    private String getFileFromAbsolutePath(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(new File(filePath));
        return IOUtils.toString(inputStream);
    }
}
