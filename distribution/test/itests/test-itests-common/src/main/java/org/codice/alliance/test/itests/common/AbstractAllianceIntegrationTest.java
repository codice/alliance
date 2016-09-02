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
package org.codice.alliance.test.itests.common;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import java.io.File;
import java.io.InputStream;

import org.codice.ddf.itests.common.AbstractIntegrationTest;
import org.ops4j.pax.exam.Option;
import com.google.common.collect.ImmutableMap;


public abstract class AbstractAllianceIntegrationTest extends AbstractIntegrationTest {

    public static final String[]
            DEFAULT_ALLIANCE_APPS = {"catalog-app", "solr-app", "spatial-app", "security-app"};

    @Override
    protected Option[] configureDistribution() {
        return options(karafDistributionConfiguration(maven().groupId(
                "org.codice.alliance.distribution")
                .artifactId("alliance")
                .type("zip")
                .versionAsInProject()
                .getURL(), "alliance", KARAF_VERSION).unpackDirectory(new File("target/exam"))
                .useDeployFolder(false));
    }

    @Override
    protected Option[] configureStartScript() {
        //add test dependencies to the test-dependencies-app instead of here
        return options(junitBundles(),

                features(maven().groupId("org.codice.alliance.test.itests")
                        .artifactId("test-itests-dependencies-app")
                        .type("xml")
                        .classifier("features")
                        .versionAsInProject(), "alliance-itest-dependencies"),

                features(maven().groupId("ddf.distribution")
                        .artifactId("sdk-app")
                        .type("xml")
                        .classifier("features")
                        .versionAsInProject()));
    }

    public static InputStream getAllianceItestResourceAsStream(String filePath) {
        return getFileContentAsStream(filePath, AbstractAllianceIntegrationTest.class);
    }

    public static String getAllianceItestResource(String filePath) {
        return getAllianceItestResource(filePath, ImmutableMap.of());
    }

    public static String getAllianceItestResource(String filePath, ImmutableMap params) {
        return getFileContent(filePath,
                params, AbstractAllianceIntegrationTest.class);

    }

}
