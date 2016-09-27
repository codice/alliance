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
package org.codice.alliance.nsili.client;

import org.codice.alliance.nsili.common.GIAS.PackageElement;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UID.Product;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class Client {
    private HttpServer server = null;

    public static int LISTEN_PORT = 8200;

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static final boolean SHOULD_PROCESS_PKG_ELEMENTS = false;

    private static final boolean SHOULD_TEST_STANDING_QUERY_MGR = false;

    private static final boolean SHOULD_DOWNLOAD_PRODUCT = true;

    public void runTest(String[] args) throws Exception {
        startHttpListener();

        String iorURL = null;
        String emailAddress = null;

        String[] arguments = args[0].split(",");
        for(String argument : arguments) {
            String[] parts = argument.split("=", 2);
            if(parts[0].equals("url")) {
                iorURL = parts[1];
            } else if(parts[0].equals("email")) {
                emailAddress = parts[1];
            }

        }

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();

        NsiliClient nsiliClient = new NsiliClient(orb, rootPOA);

        if(emailAddress != null) {
            nsiliClient.setEmailAddress(emailAddress);
        }

        // Get IOR File
        String iorFile = nsiliClient.getIorTextFile(iorURL);
        iorFile = iorFile.trim();

        // Initialize Corba Library
        nsiliClient.initLibrary(iorFile);

        // Get the Managers from the Library
        String[] managers = nsiliClient.getManagerTypes();
        nsiliClient.initManagers(managers);

        // Standing Query Mgr
        Query standingAllQuery = new Query(NsiliConstants.NSIL_ALL_VIEW,
                "NSIL_CARD.identifier like '%'");
        if (SHOULD_TEST_STANDING_QUERY_MGR) {
            nsiliClient.testStandingQueryMgr(rootPOA, standingAllQuery);
        }

        // CatalogMgr
        Query query = new Query(NsiliConstants.NSIL_ALL_VIEW,
                "NSIL_CARD.identifier like '%' and NSIL_CARD.sourceLibrary = 'test'");
        int hitCount = nsiliClient.getHitCount(query);
        if (hitCount > 0) {
            DAG[] results = nsiliClient.submit_query(query);
            if (results != null && results.length > 0) {
                nsiliClient.processAndPrintResults(results, SHOULD_DOWNLOAD_PRODUCT);

                //OrderMgr
                nsiliClient.order(orb, results);

                //ProductMgr
                LOGGER.info("-----------------------");
                try {
                    nsiliClient.testProductMgr(orb, results);
                } catch (Exception e) {
                    LOGGER.info("Unable to test ProductMgr: {}",
                            NsilCorbaExceptionUtil.getExceptionDetails(e));
                }
                LOGGER.info("-----------------------");

                // OrderMgr
                if (SHOULD_PROCESS_PKG_ELEMENTS) {
                    PackageElement[] packageElements = nsiliClient.order(orb, results);

                    // ProductMgr
                    // For each packageElement in the order response, get the parameters and
                    // related files for the product.
                    if (packageElements != null) {
                        for (PackageElement packageElement : packageElements) {
                            Product product = packageElement.prod;
                            nsiliClient.get_parameters(product);
                            nsiliClient.get_related_file_types(product);
                            nsiliClient.get_related_files(orb, product);
                        }
                    } else {
                        LOGGER.info("Order does not have any package elements");
                    }
                }

            } else {
                LOGGER.info("No results from query");
            }
        }

        //Catalog Mgr via Callback
        nsiliClient.testCallbackCatalogMgr(rootPOA, standingAllQuery);

        LOGGER.info("Press a key to exit");
        System.in.read();

        nsiliClient.cleanup();

        orb.shutdown(true);
        if (server != null) {
            server.stop();
        }

        LOGGER.info("Done. ");
        System.exit(0);
    }

    public static void main(String args[]) {
        Client client = new Client();
        if (args.length != 1) {
            LOGGER.info("Unable to obtain IOR File :  Must specify URL to IOR file.");
        }
        try {
            client.runTest(args);
        } catch (Exception e) {
            LOGGER.error("Unable to run tests on client", e);
        }
    }

    private void startHttpListener() {
        try {
            ResourceConfig rc = new PackagesResourceConfig("org.codice.alliance.nsili.client");
            server = GrizzlyServerFactory.createHttpServer("http://0.0.0.0:" + LISTEN_PORT, rc);
            NetworkListener networkListener = new NetworkListener("sample-listener",
                    "0.0.0.0",
                    8280);
            server.addListener(networkListener);
            server.start();
        } catch (Exception e) {
            LOGGER.error("HTTP Server initilization error: ", e);
        }
    }
}
