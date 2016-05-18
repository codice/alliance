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
package org.codice.alliance.nsili.mockserver.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.CB.CallbackHelper;
import org.codice.alliance.nsili.common.CB.CallbackPOA;
import org.codice.alliance.nsili.common.GIAS.AccessCriteria;
import org.codice.alliance.nsili.common.GIAS.AlterationSpec;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.CatalogMgr;
import org.codice.alliance.nsili.common.GIAS.CatalogMgrHelper;
import org.codice.alliance.nsili.common.GIAS.DataModelMgr;
import org.codice.alliance.nsili.common.GIAS.DataModelMgrHelper;
import org.codice.alliance.nsili.common.GIAS.DeliveryDetails;
import org.codice.alliance.nsili.common.GIAS.DeliveryManifest;
import org.codice.alliance.nsili.common.GIAS.DeliveryManifestHolder;
import org.codice.alliance.nsili.common.GIAS.Destination;
import org.codice.alliance.nsili.common.GIAS.Event;
import org.codice.alliance.nsili.common.GIAS.GeoRegionType;
import org.codice.alliance.nsili.common.GIAS.GetParametersRequest;
import org.codice.alliance.nsili.common.GIAS.GetRelatedFilesRequest;
import org.codice.alliance.nsili.common.GIAS.HitCountRequest;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.LibraryDescription;
import org.codice.alliance.nsili.common.GIAS.LibraryHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.GIAS.LifeEvent;
import org.codice.alliance.nsili.common.GIAS.MediaType;
import org.codice.alliance.nsili.common.GIAS.NamedEventType;
import org.codice.alliance.nsili.common.GIAS.OrderContents;
import org.codice.alliance.nsili.common.GIAS.OrderMgr;
import org.codice.alliance.nsili.common.GIAS.OrderMgrHelper;
import org.codice.alliance.nsili.common.GIAS.OrderRequest;
import org.codice.alliance.nsili.common.GIAS.PackageElement;
import org.codice.alliance.nsili.common.GIAS.PackagingSpec;
import org.codice.alliance.nsili.common.GIAS.Polarity;
import org.codice.alliance.nsili.common.GIAS.ProductDetails;
import org.codice.alliance.nsili.common.GIAS.ProductMgr;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.QueryHelper;
import org.codice.alliance.nsili.common.GIAS.QueryLifeSpan;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.StandingQueryMgr;
import org.codice.alliance.nsili.common.GIAS.StandingQueryMgrHelper;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequest;
import org.codice.alliance.nsili.common.GIAS.SubmitStandingQueryRequest;
import org.codice.alliance.nsili.common.GIAS.TailoringSpec;
import org.codice.alliance.nsili.common.GIAS.ValidationResults;
import org.codice.alliance.nsili.common.NsilCorbaExceptionUtil;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliManagerType;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.Coordinate2d;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.DAGHolder;
import org.codice.alliance.nsili.common.UCO.DAGListHolder;
import org.codice.alliance.nsili.common.UCO.Date;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameListHolder;
import org.codice.alliance.nsili.common.UCO.NameName;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.common.UID.ProductHelper;
import org.codice.alliance.nsili.transformer.DAGConverter;

import ddf.catalog.data.Metacard;
import ddf.catalog.resource.impl.URLResourceReader;

public class NsiliClient {

    private static final long ONE_YEAR = 365L * 24L * 60L * 60L * 1000L;

    private static Library library;

    private static CatalogMgr catalogMgr;

    private static OrderMgr orderMgr;

    private static ProductMgr productMgr;

    private static DataModelMgr dataModelMgr;

    private static StandingQueryMgr standingQueryMgr;

    private static final String ENCODING = "ISO-8859-1";

    private static final AccessCriteria accessCriteria = new AccessCriteria("", "", "");

    private ORB orb;

    private POA poa;

    private SubmitStandingQueryRequest standingQueryRequest = null;

    private SubmitQueryRequest catalogSearchQueryRequest = null;

    protected List<TestNsiliCallback> callbacks = new ArrayList<>();

    protected List<TestNsiliStandingQueryCallback> standingQueryCallbacks = new ArrayList<>();

    public NsiliClient(ORB orb, POA poa) {
        this.orb = orb;
        this.poa = poa;
    }

    public void initLibrary(String iorFilePath) {
        org.omg.CORBA.Object obj = orb.string_to_object(iorFilePath);
        if (obj == null) {
            System.err.println("Cannot read " + iorFilePath);
        }
        library = LibraryHelper.narrow(obj);
        System.out.println("Library Initialized");
    }

    public String[] getManagerTypes() throws Exception {
        LibraryDescription libraryDescription = library.get_library_description();
        System.out.println("NAME: " + libraryDescription.library_name + ", DESCRIPTION: "
                + libraryDescription.library_description + ", VERSION: "
                + libraryDescription.library_version_number);
        String[] types = library.get_manager_types();
        System.out.println("Got Manager Types from " + libraryDescription.library_name + " : ");
        for (int i = 0; i < types.length; i++) {
            System.out.println("\t" + types[i]);
        }
        System.out.println();
        return types;
    }

    public void initManagers(String[] managers) throws Exception {
        for (String managerType : managers) {
            if (managerType.equals(NsiliManagerType.CATALOG_MGR.getSpecName())) {
                // Get Mandatory Managers
                System.out.println("Getting CatalogMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.CATALOG_MGR.getSpecName(),
                                accessCriteria);
                catalogMgr = CatalogMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + catalogMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.ORDER_MGR.getSpecName())) {
                System.out.println("Getting OrderMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.ORDER_MGR.getSpecName(),
                                accessCriteria);
                orderMgr = OrderMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + orderMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.PRODUCT_MGR.getSpecName())) {
                System.out.println("Getting ProductMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.PRODUCT_MGR.getSpecName(),
                                accessCriteria);
                productMgr = ProductMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + productMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.DATA_MODEL_MGR.getSpecName())) {
                System.out.println("Getting DataModelMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.DATA_MODEL_MGR.getSpecName(),
                                accessCriteria);
                dataModelMgr = DataModelMgrHelper.narrow(libraryManager);
                System.out.println("Source returned : " + dataModelMgr.getClass() + "\n");
            } else if (managerType.equals(NsiliManagerType.STANDING_QUERY_MGR.getSpecName())) {
                System.out.println("Getting StandingQueryMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.STANDING_QUERY_MGR.getSpecName(),
                                accessCriteria);
                standingQueryMgr = StandingQueryMgrHelper.narrow(libraryManager);
                System.out.println("Source returned: " + standingQueryMgr.getClass() + "\n");
            }
        }
    }

    public int getHitCount(Query query) throws Exception {
        if (catalogMgr != null) {
            System.out.println("Getting Hit Count From Query...");
            HitCountRequest hitCountRequest = catalogMgr.hit_count(query, new NameValue[0]);
            IntHolder intHolder = new IntHolder();
            hitCountRequest.complete(intHolder);
            System.out.println("Server responded with " + intHolder.value + " hit(s).\n");
            return intHolder.value;
        } else {
            System.out.println("catalogMgr was not initialized, unable to find hit count");
            return -1;
        }
    }

    public DAG[] submit_query(Query query) throws Exception {
        if (catalogMgr != null) {
            System.out.println("Submitting Query To Server...");
            DAGListHolder dagListHolder = new DAGListHolder();
            SortAttribute[] sortAttributes = getSortableAttributes();
            String[] resultAttributes = getResultAttributes();

            SubmitQueryRequest submitQueryRequest = catalogMgr.submit_query(query,
                    resultAttributes,
                    sortAttributes,
                    new NameValue[0]);
            submitQueryRequest.set_user_info("AllianceQuerySubmit");
            submitQueryRequest.set_number_of_hits(200);
            submitQueryRequest.complete_DAG_results(dagListHolder);
            System.out.println(
                    "Server Responded with " + dagListHolder.value.length + " result(s).\n");
            return dagListHolder.value;
        } else {
            System.out.println("catalogMgr is not initialized, unable to submit queries");
            return null;
        }
    }

    public void processAndPrintResults(DAG[] results, boolean downloadProduct) {
        System.out.println("Printing DAG Attribute Results...");
        for (int i = 0; i < results.length; i++) {
            printDAGAttributes(results[i]);
            if (downloadProduct) {
                try {
                    retrieveProductFromDAG(results[i]);
                } catch (MalformedURLException e) {
                    System.out.println("Invalid URL used for product retrieval.");
                }
            }
        }
    }

    public void printDAGAttributes(DAG dag) {
        System.out.println("--------------------");
        for (int i = 0; i < dag.nodes.length; i++) {
            Node node = dag.nodes[i];
            if (node.node_type.equals(NodeType.ATTRIBUTE_NODE)) {
                String name = node.attribute_name;
                String value = DAGConverter.getNodeValue(node.value);
                System.out.println(name + " = " + value);
            }
        }
        System.out.println("--------------------");
    }

    public void retrieveProductFromDAG(DAG dag) throws MalformedURLException {
        System.out.println("Downloading products...");
        for (int i = 0; i < dag.nodes.length; i++) {
            Node node = dag.nodes[i];
            if (node.attribute_name.equals("productUrl")) {

                String url = node.value.extract_string();
                URL fileDownload = new URL(url);
                String productPath = "product.jpg";
                System.out.println("Downloading product : " + url);
                try (FileOutputStream outputStream = new FileOutputStream(new File(productPath));
                        BufferedInputStream inputStream = new BufferedInputStream(fileDownload.openStream());
                ) {

                    byte[] data = new byte[1024];
                    int count;
                    while ((count = inputStream.read(data, 0, 1024)) != -1) {
                        outputStream.write(data, 0, count);
                    }

                    System.out.println("Successfully downloaded product from " + url + ".\n");
                    Files.deleteIfExists(Paths.get(productPath));

                } catch (IOException e) {
                    System.out.println("Unable to download product from " + url + ".\n");
                    e.printStackTrace();
                }
            }
        }
    }

    public PackageElement[] order(ORB orb, POA poa, DAG[] dags) throws Exception {
        if (orderMgr != null) {
            System.out.println("--------------------------");
            System.out.println("OrderMgr getting package specifications");
            String[] supportedPackageSpecs = orderMgr.get_package_specifications();
            if (supportedPackageSpecs != null && supportedPackageSpecs.length > 0) {
                for (String supportedPackageSpec : supportedPackageSpecs) {
                    System.out.println("\t" + supportedPackageSpec);
                }
            } else {
                System.out.println("Server returned no packaging specifications");
            }

            System.out.println("Getting OrderMgr Use Modes");
            String[] useModes = orderMgr.get_use_modes();
            for (String useMode : useModes) {
                System.out.println("\t" + useMode);
            }

            short numPriorities = orderMgr.get_number_of_priorities();
            System.out.println("Order Mgr num of priorities: " + numPriorities);

            Product product = ProductHelper.extract(dags[0].nodes[0].value);
            System.out.println("Product: " + product.toString());
            String productId = getProductID(dags[0]);
            System.out.println("Product ID: " + productId);
            String filename = getFileName(dags[0], productId);

            //Product available
            boolean productAvail = orderMgr.is_available(product, useModes[0]);
            System.out.println("Product available: " + productAvail);

            System.out.println("Creating order request...");
            NameValue[] properties = new NameValue[0];
            OrderContents order = createOrder(orb, product, supportedPackageSpecs, filename);

            //Validating Order
            System.out.println("Validating Order...");
            ValidationResults validationResults = orderMgr.validate_order(order, properties);

            System.out.println("Validation Results: ");
            System.out.println("\tValid : " + validationResults.valid + "\n\tWarning : "
                    + validationResults.warning + "\n\tDetails : " + validationResults.details
                    + "\n");

            OrderRequest orderRequest = orderMgr.order(order, properties);

            System.out.println("Completing OrderRequest...");
            DeliveryManifestHolder deliveryManifestHolder = new DeliveryManifestHolder();
            orderRequest.set_user_info("Alliance");
            PackageElement[] elements = null;
            try {
                orderRequest.complete(deliveryManifestHolder);

                DeliveryManifest deliveryManifest = deliveryManifestHolder.value;

                System.out.println("Completed Order :");
                System.out.println(deliveryManifest.package_name);

                elements = deliveryManifest.elements;
                for (int i = 0; i < elements.length; i++) {

                    String[] files = elements[i].files;

                    for (int c = 0; c < files.length; c++) {
                        System.out.println("\t" + files[c]);
                    }

                }
                System.out.println();
            } catch (Exception e) {
                System.out.println("Error completing order request");
                System.out.println(NsilCorbaExceptionUtil.getExceptionDetails(e));
            }

            return elements;
        } else {
            System.out.println("orderMgr is not initialized, unable to submit order");
            return null;
        }
    }

    public void testStandingQueryMgr(ORB orb, POA poa, Query query) throws Exception {
        if (standingQueryMgr != null) {
            System.out.println("----------------------");
            System.out.println("Standing Query Manager Test");

            if (standingQueryMgr != null) {
                Event[] events = standingQueryMgr.get_event_descriptions();
                if (events != null) {
                    for (Event event : events) {
                        NamedEventType namedEventType = event.event_type;
                        System.out.println("Event: " + event.event_type.value() + " name: " + event.event_name
                                + " desc: " + event.event_description);
                    }
                }
            }

            LifeEvent start = new LifeEvent();
            java.util.Date startDate = new java.util.Date();
            start.at(ResultDAGConverter.getAbsTime(startDate));

            LifeEvent end = new LifeEvent();
            long endTime = System.currentTimeMillis() + ONE_YEAR;
            java.util.Date endDate = new java.util.Date();
            endDate.setTime(endTime);
            end.at(ResultDAGConverter.getAbsTime(endDate));

            LifeEvent[] frequency = new LifeEvent[1];
            LifeEvent freqOne = new LifeEvent();
            Time time = new Time((short) 0, (short) 0, 30.0f);
            freqOne.rt(time);
            frequency[0] = freqOne;
            QueryLifeSpan queryLifeSpan = new QueryLifeSpan(start, end, frequency);

            NameValue[] props = new NameValue[0];

            String callbackId = UUID.randomUUID()
                    .toString();

            try {
                standingQueryRequest = standingQueryMgr.submit_standing_query(query,
                        getResultAttributes(),
                        getSortableAttributes(),
                        queryLifeSpan,
                        props);

                standingQueryRequest.set_user_info("Alliance");
                standingQueryRequest.set_number_of_hits(200);

                TestNsiliStandingQueryCallback nsiliCallback = new TestNsiliStandingQueryCallback(
                        standingQueryRequest);

                try {
                    poa.activate_object_with_id(callbackId.getBytes(Charset.forName(ENCODING)),
                            nsiliCallback);
                } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                    System.err.println("order : Unable to activate callback object, already active.");
                }

                org.omg.CORBA.Object obj = poa.create_reference_with_id(callbackId.getBytes(Charset.forName(ENCODING)),
                        CallbackHelper.id());

                Callback callback = CallbackHelper.narrow(obj);

                String standingQueryCallbackId = standingQueryRequest.register_callback(callback);
                nsiliCallback.setCallbackID(standingQueryCallbackId);
                standingQueryCallbacks.add(nsiliCallback);

            } catch (Exception e) {
                System.err.println("Error submitting standing query: " + NsilCorbaExceptionUtil.getExceptionDetails(e));
                e.printStackTrace(System.err);
                throw (e);
            }

            System.out.println("Standing Query Submitted");
        }
    }

    public void testProductMgr(ORB orb, POA poa, DAG[] dags) throws Exception {
        if (productMgr != null) {
            System.out.println("--------------------------");
            System.out.println("Getting ProductMgr Use Modes");
            String[] useModes = productMgr.get_use_modes();
            for (String useMode : useModes) {
                System.out.println("\t" + useMode);
            }

            short numPriorities = productMgr.get_number_of_priorities();
            System.out.println("Product Mgr num of priorities: " + numPriorities);

            Product product = ProductHelper.extract(dags[0].nodes[0].value);
            System.out.println("Product: " + product.toString());

            System.out.println("Product is available tests ");
            boolean avail = productMgr.is_available(product, useModes[0]);
            System.out.println("\t" + useModes[0] + " : " + avail);

            System.out.println("Getting ALL Parameters for Product");
            //CORE, ALL, ORDER
            String[] desiredParams = new String[] {"ALL"};

            Any protocolAny = orb.create_any();
            protocolAny.insert_string("https");

            Any portAny = orb.create_any();
            portAny.insert_string(String.valueOf(Client.LISTEN_PORT));

            NameValue protocolProp = new NameValue("PROTOCOL", protocolAny);
            NameValue portProp = new NameValue("PORT", portAny);

            NameValue[] getRelatedFileProps = new NameValue[] {portProp};
            NameValue[] getParamProps = new NameValue[0];

            DAGHolder dagHolder = new DAGHolder();
            GetParametersRequest getParametersRequest = productMgr.get_parameters(product,
                    desiredParams,
                    getParamProps);
            getParametersRequest.set_user_info("Alliance");
            getParametersRequest.complete(dagHolder);

            DAG dag = dagHolder.value;
            printDAGAttributes(dag);

            System.out.println("Getting related file types");
            String[] relatedFileTypes = productMgr.get_related_file_types(product);
            if (relatedFileTypes != null) {
                for (String relatedFileType : relatedFileTypes) {
                    System.out.println("\t" + relatedFileType);
                }
            }

            try {
                String productID = getProductID(dags[0]);
                String thumbFile = productID + "-thumbnail" + ".jpg";
                System.out.println(
                        "Getting thumbnail for : " + productID + " filename: " + thumbFile);
                FileLocation thumbnailLoc = new FileLocation("user",
                        "pass",
                        "localhost",
                        "/nsili/file",
                        thumbFile);
                GetRelatedFilesRequest request = productMgr.get_related_files(new Product[] {
                        product}, thumbnailLoc, "THUMBNAIL", getRelatedFileProps);
                request.set_user_info("Alliance");
                NameListHolder locations = new NameListHolder();
                request.complete(locations);

                for (String location : locations.value) {
                    System.out.println("\t Stored File: " + location);
                }
            } catch (Exception e) {
                System.out.println("Unable to get product thumbnail: "
                        + NsilCorbaExceptionUtil.getExceptionDetails(e));
                e.printStackTrace();
            }

            try {
                String productID = getProductID(dags[0]);
                System.out.println("Getting overview for : " + productID);
                FileLocation thumbnailLoc = new FileLocation("user",
                        "pass",
                        "localhost",
                        "/tmp/files",
                        productID + "-overview" + ".jpg");
                GetRelatedFilesRequest request = productMgr.get_related_files(new Product[] {
                        product}, thumbnailLoc, "OVERVIEW", getRelatedFileProps);
                request.set_user_info("Alliance");
                NameListHolder locations = new NameListHolder();
                request.complete(locations);
            } catch (Exception e) {
                System.out.println("Unable to get product overview: "
                        + NsilCorbaExceptionUtil.getExceptionDetails(e));
                e.printStackTrace();
            }
        } else {
            System.out.println("Unable to test ProductMgr as it is not set");
        }
    }

    public void get_parameters(ORB orb, Product product) throws Exception {
        if (productMgr != null) {
            System.out.println("Sending Get Parameters Request to Server...");

            String[] desired_parameters = {"CORE", "ALL", "ORDER"};
            NameValue[] properties = new NameValue[0]; //{new NameValue("", orb.create_any())};

            GetParametersRequest parametersRequest = productMgr.get_parameters(product,
                    desired_parameters,
                    properties);
            System.out.println("Completing GetParameters Request ...");

            DAGHolder dagHolder = new DAGHolder();
            parametersRequest.complete(dagHolder);

            DAG dag = dagHolder.value;
            System.out.println("Resulting Parameters From Server :");
            printDAGAttributes(dag);
            System.out.println();
        } else {
            System.out.println("productMgr is not initialized, unable to get parameters");
        }
    }

    public void get_related_file_types(Product product) throws Exception {
        if (productMgr != null) {
            System.out.println("Sending Get Related File Types Request...");
            String[] related_file_types = productMgr.get_related_file_types(product);
            System.out.println("Related File Types : ");
            for (int i = 0; i < related_file_types.length; i++) {
                System.out.println(related_file_types[i]);
            }
            System.out.println();
        } else {
            System.out.println("productMgr is not initialized, unable to get related file types");
        }
    }

    public void get_related_files(ORB orb, Product product) throws Exception {
        if (productMgr != null) {
            System.out.println("Sending Get Related Files Request...");

            FileLocation fileLocation = new FileLocation("", "", "", "", "");
            NameValue[] properties = {new NameValue("", orb.create_any())};
            Product[] products = {product};

            GetRelatedFilesRequest relatedFilesRequest = productMgr.get_related_files(products,
                    fileLocation,
                    "",
                    properties);
            System.out.println("Completing GetRelatedFilesRequest...");

            NameListHolder locations = new NameListHolder();

            relatedFilesRequest.complete(locations);

            System.out.println("Location List : ");
            String[] locationList = locations.value;
            for (int i = 0; i < locationList.length; i++) {
                System.out.println(locationList[i]);
            }
            System.out.println();
        } else {
            System.out.println("productMgr is not initialized, unable to get related files");
        }
    }

    public OrderContents createOrder(ORB orb, Product product, String[] supportedPackagingSpecs,
            String filename) throws Exception {
        NameName nameName[] = {new NameName("", "")};

        String orderPackageId = UUID.randomUUID()
                .toString();

        TailoringSpec tailoringSpec = new TailoringSpec(nameName);
        PackagingSpec pSpec = new PackagingSpec(orderPackageId, supportedPackagingSpecs[0]);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        int year = cal.get(Calendar.YEAR);
        year++;

        AbsTime needByDate = new AbsTime(new Date((short) year, (short) 2, (short) 10),
                new Time((short) 10, (short) 0, (short) 0));

        MediaType[] mTypes = {new MediaType("", (short) 1)};
        String[] benums = new String[0];
        Rectangle region = new Rectangle(new Coordinate2d(1.1, 1.1), new Coordinate2d(2.2, 2.2));

        //TODO -- Need to figure out which output formats are acceptable - enum?
        AlterationSpec aSpec = new AlterationSpec("JPEG",
                orb.create_any(),
                region,
                GeoRegionType.NULL_REGION);

        Any portAny = orb.create_any();
        portAny.insert_string(String.valueOf(Client.LISTEN_PORT));
        NameValue portProp = new NameValue("PORT", portAny);

        NameValue[] getRelatedFileProps = new NameValue[] {portProp};

        FileLocation fileLocation = new FileLocation("user",
                "pass",
                "localhost:" + Client.LISTEN_PORT,
                "/nsili/file",
                filename);
        Destination destination = new Destination();
        destination.f_dest(fileLocation);

        ProductDetails[] productDetails = {new ProductDetails(mTypes,
                benums,
                aSpec,
                product,
                "Alliance")};
        DeliveryDetails[] deliveryDetails = {new DeliveryDetails(destination, "", "")};

        OrderContents order = new OrderContents("Alliance",
                tailoringSpec,
                pSpec,
                needByDate,
                "Give me an order!",
                (short) 1,
                productDetails,
                deliveryDetails);

        return order;
    }

    public String getIorTextFile(String iorURL) throws Exception {
        System.out.println("Downloading IOR File From Server...");
        String myString = "";

        try {
            //Disable certificate checking as this is only a test client
            doTrustAllCertificates();
            URL fileDownload = new URL(iorURL);
            BufferedInputStream inputStream = new BufferedInputStream(fileDownload.openStream());
            myString = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (StringUtils.isNotBlank(myString)) {
            System.out.println("Successfully Downloaded IOR File From Server.\n");
            return myString;
        }

        throw new Exception("Error recieving IOR File");
    }

    public void cleanup() {
        try {
            deregisterStandingQueryCallbacks();
        } catch (InvalidInputParameter invalidInputParameter) {
            invalidInputParameter.printStackTrace();
        } catch (SystemFault systemFault) {
            systemFault.printStackTrace();
        } catch (ProcessingFault processingFault) {
            processingFault.printStackTrace();
        } catch (ObjectNotActive objectNotActive) {
            objectNotActive.printStackTrace();
        } catch (WrongPolicy wrongPolicy) {
            wrongPolicy.printStackTrace();
        }
    }

    public void testCallbackCatalogMgr(ORB orb, POA poa, Query query) throws Exception {
        if (catalogMgr != null) {
            System.out.println("Testing Query Results via Callback ...");
            SortAttribute[] sortAttributes = getSortableAttributes();
            String[] resultAttributes = getResultAttributes();
            System.out.println("Query: " + query.bqs_query);

            catalogSearchQueryRequest = catalogMgr.submit_query(query,
                    resultAttributes,
                    sortAttributes,
                    new NameValue[0]);
            catalogSearchQueryRequest.set_user_info("Alliance");
            catalogSearchQueryRequest.set_number_of_hits(200);
            TestNsiliCallback nsiliCallback = new TestNsiliCallback(catalogSearchQueryRequest);
            byte[] poaObjId = poa.activate_object(nsiliCallback);
            org.omg.CORBA.Object obj = poa.id_to_reference(poaObjId);
            String catalogSearchCallbackID = catalogSearchQueryRequest.register_callback(
                    CallbackHelper.narrow(obj));
            nsiliCallback.setCallbackID(catalogSearchCallbackID);
            callbacks.add(nsiliCallback);

            System.out.println(
                    "Callback Catalog Mgr Callback registered: " + catalogSearchCallbackID);
        } else {
            System.out.println("CatalogMgr is not initialized, unable to submit queries");
        }
    }

    private void deregisterStandingQueryCallbacks()
            throws InvalidInputParameter, SystemFault, ProcessingFault, ObjectNotActive,
            WrongPolicy {
        for (TestNsiliCallback callback : callbacks) {
            System.out.println("Freeing callback: " + callback.getCallbackID());
            callback.getQueryRequest()
                    .free_callback(callback.getCallbackID());
        }

        for (TestNsiliStandingQueryCallback callback : standingQueryCallbacks) {
            System.out.println("Freeing standing query callback: " + callback.getCallbackID());
            callback.getQueryRequest()
                    .free_callback(callback.getCallbackID());
        }
    }

    // Trust All Certifications
    private void doTrustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {
                return;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        // Set HttpsURLConnection settings
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HostnameVerifier hostnameVerifier =
                (s, sslSession) -> s.equalsIgnoreCase(sslSession.getPeerHost());
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    private SortAttribute[] getSortableAttributes()
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        List<SortAttribute> sortableAttributesList = new ArrayList<>();

        AttributeInformation[] attributeInformationArray = dataModelMgr.get_attributes(
                NsiliConstants.NSIL_ALL_VIEW,
                new NameValue[0]);
        String[] resultAttributes = new String[attributeInformationArray.length];

        for (int c = 0; c < attributeInformationArray.length; c++) {
            AttributeInformation attributeInformation = attributeInformationArray[c];
            resultAttributes[c] = attributeInformation.attribute_name;

            if (attributeInformation.sortable) {
                sortableAttributesList.add(new SortAttribute(attributeInformation.attribute_name,
                        Polarity.DESCENDING));
            }

        }

        return sortableAttributesList.toArray(new SortAttribute[0]);
    }

    private String[] getResultAttributes()
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        AttributeInformation[] attributeInformationArray = dataModelMgr.get_attributes(
                NsiliConstants.NSIL_ALL_VIEW,
                new NameValue[0]);
        String[] resultAttributes = new String[attributeInformationArray.length];

        for (int c = 0; c < attributeInformationArray.length; c++) {
            AttributeInformation attributeInformation = attributeInformationArray[c];
            resultAttributes[c] = attributeInformation.attribute_name;
        }

        return resultAttributes;
    }

    private String getProductID(DAG dag) {
        DAGConverter dagConverter = new DAGConverter(new URLResourceReader());
        Metacard metacard = dagConverter.convertDAG(dag, false, "");
        return metacard.getId();
    }

    private String getFileName(DAG dag, String productId) {
        for (Node node : dag.nodes) {
            if (node.attribute_name.equalsIgnoreCase("filename")) {
                return DAGConverter.getString(node.value);
            }
        }
        return productId + ".dat";
    }

    private String getString(Any any) {
        String value = "UNKNOWN: (" + any.type()
                .kind()
                .value() + ")";
        if (any.type()
                .kind() == TCKind.tk_wstring) {
            value = any.extract_wstring();
        } else if (any.type()
                .kind() == TCKind.tk_string) {
            value = any.extract_string();
        } else if (any.type()
                .kind() == TCKind.tk_long) {
            value = String.valueOf(any.extract_long());
        } else if (any.type()
                .kind() == TCKind.tk_ulong) {
            value = String.valueOf(any.extract_ulong());
        } else if (any.type()
                .kind() == TCKind.tk_short) {
            value = String.valueOf(any.extract_short());
        } else if (any.type()
                .kind() == TCKind.tk_ushort) {
            value = String.valueOf(any.extract_ushort());
        }

        return value;
    }

    private class TestNsiliCallback extends CallbackPOA {

        private String callbackID;

        private SubmitQueryRequest queryRequest;

        public TestNsiliCallback(SubmitQueryRequest queryRequest) {
            this.queryRequest = queryRequest;
        }

        public void setCallbackID(String callbackID) {
            this.callbackID = callbackID;
        }

        public SubmitQueryRequest getQueryRequest() {
            return queryRequest;
        }

        public String getCallbackID() {
            return callbackID;
        }

        @Override
        public void _notify(State theState, RequestDescription description)
                throws InvalidInputParameter, ProcessingFault, SystemFault {
            System.out.println("**************************************************************");
            System.out.println("******************* NOTIFY CALLED ****************************");
            System.out.println("**************************************************************");
            try {
                System.out.println("--------  TestNsiliCallback.notify --------");
                System.out.println("State: " + theState);
                System.out.println("Request: ");
                if (description != null) {
                    System.out.println("\t user_info: " + description.user_info);
                    System.out.println("\t type: " + description.request_type);
                    System.out.println("\t request_info: " + description.request_info);
                    if (description.request_details != null
                            && description.request_details.length > 0) {
                        System.out.println("\t details: " + description.request_details.length);
                        for (NameValue nameValue : description.request_details) {
                            if (nameValue.aname != null && nameValue.value != null) {
                                String value = getString(nameValue.value);
                                if (nameValue.aname.equalsIgnoreCase("query")) {
                                    Query q = QueryHelper.extract(nameValue.value);
                                    value = q.bqs_query;
                                }

                                if (value != null) {
                                    System.out.println("\t\t" + nameValue.aname + " = " + value);
                                } else {
                                    System.out.println(
                                            "\t\t" + nameValue.aname + " = " + nameValue.value
                                                    + " (non-string)");
                                }
                            }
                        }
                    } else {
                        System.out.println("Notified with no details");
                    }
                }

                System.out.println("Results from notification: ");
                DAGListHolder dagListHolder = new DAGListHolder();
                queryRequest.complete_DAG_results(dagListHolder);
                processAndPrintResults(dagListHolder.value, false);

                System.out.println("----------------");
            } catch (Exception e) {
                System.err.println("Unable to process _notify: " + e);
                e.printStackTrace();
            }
        }

        @Override
        public void release() throws ProcessingFault, SystemFault {
            System.out.println("TestNsiliCallback.release");

        }
    }

    private class TestNsiliStandingQueryCallback extends CallbackPOA {

        private String callbackID;

        private SubmitStandingQueryRequest queryRequest;

        public TestNsiliStandingQueryCallback(SubmitStandingQueryRequest queryRequest) {
            this.queryRequest = queryRequest;
        }

        public void setCallbackID(String callbackID) {
            this.callbackID = callbackID;
        }

        public SubmitStandingQueryRequest getQueryRequest() {
            return queryRequest;
        }

        public String getCallbackID() {
            return callbackID;
        }

        @Override
        public void _notify(State theState, RequestDescription description)
                throws InvalidInputParameter, ProcessingFault, SystemFault {
            System.out.println("******************* NOTIFY CALLED ****************************");
            try {
                System.out.println("State: " + theState.value());
                if (theState == State.RESULTS_AVAILABLE) {
                    System.out.println("Results are available");
                    System.out.println("Request: ");
                    if (description != null) {
                        System.out.println("\t user_info: " + description.user_info);
                        System.out.println("\t type: " + description.request_type);
                        System.out.println("\t request_info: " + description.request_info);
                        if (description.request_details != null
                                && description.request_details.length > 0) {
                            System.out.println("\t details: " + description.request_details.length);
                            for (NameValue nameValue : description.request_details) {
                                if (nameValue.aname != null && nameValue.value != null) {
                                    String value = getString(nameValue.value);
                                    if (nameValue.aname.equalsIgnoreCase("query")) {
                                        Query q = QueryHelper.extract(nameValue.value);
                                        value = q.bqs_query;
                                    }

                                    if (value != null) {
                                        System.out.println("\t\t" + nameValue.aname + " = " + value);
                                    } else {
                                        System.out.println("\t\t" + nameValue.aname + " = " + nameValue.value + " (non-string)");
                                    }
                                }
                            }
                        } else {
                            System.out.println("Notified with no details");
                        }
                    }
                    System.out.println("Results from notification: ");
                    DAGListHolder dagListHolder = new DAGListHolder();
                    queryRequest.complete_DAG_results(dagListHolder);
                    processAndPrintResults(dagListHolder.value, false);
                } else {
                    System.out.println("No results available");
                }

                System.out.println("**************************************************************");
            } catch (Exception e) {
                System.err.println("Unable to process _notify: " + NsilCorbaExceptionUtil.getExceptionDetails(e));
                e.printStackTrace();
            }
        }

        @Override
        public void release() throws ProcessingFault, SystemFault {
            System.out.println("TestNsiliCallback.release");

        }
    }
}