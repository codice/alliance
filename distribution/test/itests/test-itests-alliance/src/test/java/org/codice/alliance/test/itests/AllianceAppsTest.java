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

import static org.codice.ddf.admin.application.service.ApplicationStatus.ApplicationState.ACTIVE;
import static org.codice.ddf.admin.application.service.ApplicationStatus.ApplicationState.INACTIVE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.codice.alliance.test.itests.common.AbstractAllianceIntegrationTest;
import org.codice.ddf.admin.application.service.Application;
import org.codice.ddf.admin.application.service.ApplicationService;
import org.codice.ddf.admin.application.service.ApplicationServiceException;
import org.codice.ddf.admin.application.service.ApplicationStatus;
import org.codice.ddf.itests.common.annotations.BeforeExam;
import org.codice.ddf.security.common.Security;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import ddf.security.Subject;

/**
 * Ensures that all Alliance apps are able to be installed.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AllianceAppsTest extends AbstractAllianceIntegrationTest {

    private static final String[] APPS = {"security-app", "nsili-app", "imaging-app", "video-app"};

    @BeforeExam
    public void beforeAllianceTest() throws Exception {
        try {
            basePort = getBasePort();
            getAdminConfig().setLogLevels();

            getServiceManager().waitForRequiredApps(getDefaultRequiredApps());
            getServiceManager().waitForAllBundles();
            getCatalogBundle().waitForCatalogProvider();
        } catch (Exception e) {
            LOGGER.error("Failed in @BeforeExam: ", e);
            fail("Failed in @BeforeExam: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        clearCatalog();
    }

    @Test
    public void installAllianceApps() throws Exception {
        ApplicationService applicationService =
                getServiceManager().getService(ApplicationService.class);
        Subject systemSubject = Security.runAsAdmin(() -> Security.getInstance()
                .getSystemSubject());

        systemSubject.execute(() -> {
            for (String appName : APPS) {
                Application app = applicationService.getApplication(appName);
                assertNotNull(String.format("Application [%s] must not be null", appName), app);
                ApplicationStatus status = applicationService.getApplicationStatus(app);
                assertThat(String.format("%s should be INACTIVE", appName),
                        status.getState(),
                        is(INACTIVE));

                try {
                    applicationService.startApplication(app);
                } catch (ApplicationServiceException e) {
                    LOGGER.error("Failed starting app {}", appName, e);
                    fail(String.format("Failed to start the %s: %s", appName, e.getMessage()));
                }
                status = applicationService.getApplicationStatus(app);
                assertThat(String.format("%s should be ACTIVE after start, but was [%s]",
                        appName,
                        status.getState()), status.getState(), is(ACTIVE));
            }

            return null;
        });
    }
}
