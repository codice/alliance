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
package org.codice.alliance.nsili.mockserver.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.codice.alliance.nsili.mockserver.impl.LibraryImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockNsili {
  private static final Logger LOGGER = LoggerFactory.getLogger(MockNsili.class);

  private String iorString;

  private Path ftpHomeDirectoryPath;

  public static final String MOCK_SERVER_USERNAME = "admin";

  public static final String MOCK_SERVER_PASSWORD = "admin";

  // Singleton providing access to IOR string from reflection instantiated web service
  private static final MockNsili mockNsili = new MockNsili();

  private MockNsili() {}

  public static MockNsili getInstance() {
    return mockNsili;
  }

  public String getIorString() {
    return iorString;
  }

  public void startMockServer(int corbaPort) {
    ORB orb = null;

    try {
      orb = getOrbForServer(corbaPort);
      LOGGER.info("Server Started...");
      orb.run(); // blocks the current thread until the ORB is shutdown
    } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
      LOGGER.error("Unable to start the CORBA server.", e);
    } catch (IOException e) {
      LOGGER.error("Unable to generate the IOR file.", e);
    }

    if (orb != null) {
      orb.destroy();
    }
  }

  public void startWebServer(int port) {
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setResourceClasses(MockWebService.class);
    sf.setAddress("http://localhost:" + port + "/");
    sf.create();
  }

  public void startFtpWebServer(int port) {
    FtpServerFactory ftpServerFactory = new FtpServerFactory();

    ListenerFactory listenerFactory = new ListenerFactory();
    listenerFactory.setPort(port);
    ftpServerFactory.addListener("default", listenerFactory.createListener());

    PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
    UserManager userManager = propertiesUserManagerFactory.createUserManager();
    BaseUser baseUser = new BaseUser();
    baseUser.setName(MOCK_SERVER_USERNAME);
    baseUser.setPassword(MOCK_SERVER_PASSWORD);

    try {
      ftpHomeDirectoryPath = Files.createTempDirectory("home_");
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(() -> FileUtils.deleteQuietly(ftpHomeDirectoryPath.toFile())));
      baseUser.setHomeDirectory(ftpHomeDirectoryPath.toString());
    } catch (IOException e) {
      LOGGER.info("Unable to set ftp endpoint to a temporary home directory.");
    }

    try {
      userManager.save(baseUser);
      ftpServerFactory.setUserManager(userManager);

      FtpServer ftpServer = ftpServerFactory.createServer();
      ftpServer.start();

    } catch (FtpException e) {
      LOGGER.error("Unable to start FTP server.", e);
    }

    LOGGER.info("Setting the ftp server's publish address to be ftp://localhost:{}/", port);
  }

  private ORB getOrbForServer(int port)
      throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException {

    System.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(port));

    final ORB orb = ORB.init(new String[0], null);

    System.clearProperty("org.omg.CORBA.ORBInitialPort");

    POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    rootPOA.the_POAManager().activate();

    org.omg.CORBA.Object objref = rootPOA.servant_to_reference(new LibraryImpl(rootPOA));

    // set ior.txt for http web server
    iorString = orb.object_to_string(objref);

    // set ior.txt for ftp web server
    File ftpIorFile = new File(ftpHomeDirectoryPath + "/data/ior.txt");

    File ftpDataDirectory = new File(ftpHomeDirectoryPath + "/data");
    ftpDataDirectory.mkdirs();

    if (!ftpIorFile.createNewFile()) {
      LOGGER.error("Unable to create new file");
    }

    PrintWriter printWriter = new PrintWriter(new FileWriter(ftpIorFile.getPath()));
    printWriter.print(orb.object_to_string(objref));

    if (printWriter.checkError()) {
      LOGGER.error("Unable to write ior string to ftp server temporary file");
    }

    printWriter.close();

    return orb;
  }

  public static void main(String args[]) {
    if (args.length != 1) {
      System.out.println(
          "Cannot start the mock NSILI server; No ports specified \nProvide arguments in format: HTTP_PORT,FTP_PORT,CORBA_PORT");
      return;
    }

    String[] ports = args[0].split(",");

    if (ports.length != 3) {
      System.err.println(
          "Cannot start the mock NSILI server; Incorrect number of ports specified.\nProvide arguments in format: HTTP_PORT,FTP_PORT,CORBA_PORT");
      return;
    }

    int httpPort = Integer.parseInt(ports[0]);
    int ftpPort = Integer.parseInt(ports[1]);
    int corbaPort = Integer.parseInt(ports[2]);

    mockNsili.startWebServer(httpPort);
    mockNsili.startFtpWebServer(ftpPort);
    mockNsili.startMockServer(corbaPort);

    System.exit(0);
  }
}
