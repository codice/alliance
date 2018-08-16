/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.client;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Condition.method;
import static org.codice.alliance.nsili.client.SampleNsiliClient.getAttributeFromDag;

import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;
import org.codice.alliance.nsili.common.GIAS.PackageElement;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.NsiliCardStatus;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.glassfish.grizzly.http.Method;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

  private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

  private static final int LISTEN_PORT = 8200;

  private static final String NSILI_FILE_URI_PATH = "/nsili/file";

  private static final boolean SHOULD_PROCESS_PKG_ELEMENTS = false;

  private static final boolean SHOULD_TEST_STANDING_QUERY_MGR = false;

  private static final boolean SHOULD_DOWNLOAD_PRODUCT = true;

  private StubServer server;

  public void runTests(String iorUrl, String emailAddress) throws Exception {
    startHttpListener();
    Bundle bundle = FrameworkUtil.getBundle(Client.class);
    ServiceReference<ClientFactoryFactory> serviceReference =
        bundle.getBundleContext().getServiceReference(ClientFactoryFactory.class);
    ClientFactoryFactory clientFactoryFactory =
        bundle.getBundleContext().getService(serviceReference);
    SampleNsiliClient sampleNsiliClient =
        new SampleNsiliClient(LISTEN_PORT, iorUrl, emailAddress, clientFactoryFactory);

    // StandingQueryMgr
    if (SHOULD_TEST_STANDING_QUERY_MGR) {
      sampleNsiliClient.testStandingQueryMgr();
    }

    // CatalogMgr
    int hitCount = sampleNsiliClient.getHitCount();
    if (hitCount > 0) {
      DAG[] results = sampleNsiliClient.submitQuery();
      if (results != null && results.length > 0) {
        for (int i = 0; i < results.length; i++) {
          evaluateResult(sampleNsiliClient, results, i);
        }
      } else {
        LOGGER.info("No results from query");
      }
    }

    // CatalogMgr via Callback
    sampleNsiliClient.testCallbackCatalogMgr();

    LOGGER.info("Press a key to exit");
    System.in.read();

    sampleNsiliClient.cleanup();

    if (server != null) {
      server.stop();
    }

    LOGGER.info("Done. ");
    System.exit(0);
  }

  private void evaluateResult(SampleNsiliClient sampleNsiliClient, DAG[] results, int index)
      throws Exception {
    LOGGER.info("\t RESULT : {} of {} ", (index + 1), results.length);
    String obsoleteName = NsiliCardStatus.OBSOLETE.name();
    if (getAttributeFromDag(results[index], NsiliConstants.STATUS).equalsIgnoreCase(obsoleteName)) {
      LOGGER.info("Record is {}. Not testing result", obsoleteName);
    } else {
      sampleNsiliClient.printDagAttributes(results[index]);
      if (SHOULD_DOWNLOAD_PRODUCT) {
        sampleNsiliClient.downloadProductFromDAG(results[index]);
      }

      // ProductMgr
      LOGGER.info("-----------------------");
      try {
        sampleNsiliClient.testProductMgr(results[index]);
      } catch (Exception e) {
        LOGGER.info("Unable to test ProductMgr: {}", NsilCorbaExceptionUtil.getExceptionDetails(e));
      }
      LOGGER.info("-----------------------");

      // OrderMgr
      PackageElement[] packageElements = sampleNsiliClient.order(results[index]);

      // ProductMgr
      if (SHOULD_PROCESS_PKG_ELEMENTS) {
        for (PackageElement packageElement : packageElements) {
          Product product = packageElement.prod;
          sampleNsiliClient.getParameters(product);
          sampleNsiliClient.getRelatedFileTypes(product);
          sampleNsiliClient.getRelatedFiles(product);
        }
      }
    }
  }

  /**
   * Starts the sample nsili server from the command line and runs tests in the runTests method. See
   * README.md.
   *
   * @param args in the format `mvn -Pcorba.client -Dexec.args="url=IORURL,email=EMAIL"`, where the
   *     email address is optional
   */
  public static void main(String args[]) {
    Client client = new Client();
    if (args.length != 1) {
      LOGGER.info("Unable to obtain IOR File :  Must specify URL to IOR file.");
    }
    try {
      String iorUrl = null;
      String emailAddress = null;

      String[] arguments = args[0].split(",");
      for (String argument : arguments) {
        String[] parts = argument.split("=", 2);
        if (parts[0].equals("url")) {
          iorUrl = parts[1];
        } else if (parts[0].equals("email")) {
          emailAddress = parts[1];
        }
      }

      client.runTests(iorUrl, emailAddress);
    } catch (Exception e) {
      LOGGER.error("Unable to run tests with sample nsili client", e);
    }
  }

  private void startHttpListener() {
    server = new StubServer(LISTEN_PORT).run();
    whenHttp(server)
        .match(method(Method.PUT), Condition.startsWithUri(NSILI_FILE_URI_PATH))
        .then(Action.success());
  }
}
