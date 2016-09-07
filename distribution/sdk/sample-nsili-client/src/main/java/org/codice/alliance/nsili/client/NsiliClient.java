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
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.CB.CallbackHelper;
import org.codice.alliance.nsili.common.CB.CallbackPOA;
import org.codice.alliance.nsili.common.CorbaUtils;
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
import org.codice.alliance.nsili.common.GIAS.ImageSpec;
import org.codice.alliance.nsili.common.GIAS.ImageSpecHelper;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.LibraryDescription;
import org.codice.alliance.nsili.common.GIAS.LibraryHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.GIAS.LifeEvent;
import org.codice.alliance.nsili.common.GIAS.MediaType;
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
import org.codice.alliance.nsili.common.GIAS.SupportDataEncoding;
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
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.resource.impl.URLResourceReader;

public class NsiliClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliClient.class);

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
            LOGGER.error("Cannot read {}", iorFilePath);
        }
        library = LibraryHelper.narrow(obj);
        LOGGER.info("Library Initialized");
    }

    public String[] getManagerTypes() throws Exception {
        LibraryDescription libraryDescription = library.get_library_description();
        LOGGER.info("NAME : {} \n DESCRIPTION : {} \n VERSION : {}",
                libraryDescription.library_name,
                libraryDescription.library_description,
                libraryDescription.library_version_number);
        String[] types = library.get_manager_types();
        LOGGER.info("Got Manager Types from  {} : ", libraryDescription.library_name);
        for (int i = 0; i < types.length; i++) {
            LOGGER.info("\t {}", types[i]);
        }
        return types;
    }

    public void initManagers(String[] managers) throws Exception {
        for (String managerType : managers) {
            if (managerType.equals(NsiliManagerType.CATALOG_MGR.getSpecName())) {
                // Get Mandatory Managers
                LOGGER.info("Getting CatalogMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.CATALOG_MGR.getSpecName(),
                                accessCriteria);
                catalogMgr = CatalogMgrHelper.narrow(libraryManager);
                LOGGER.info("Source returned : {}", catalogMgr.getClass());
            } else if (managerType.equals(NsiliManagerType.ORDER_MGR.getSpecName())) {
                LOGGER.info("Getting OrderMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.ORDER_MGR.getSpecName(),
                                accessCriteria);
                orderMgr = OrderMgrHelper.narrow(libraryManager);
                LOGGER.info("Source returned : {}", orderMgr.getClass());
            } else if (managerType.equals(NsiliManagerType.PRODUCT_MGR.getSpecName())) {
                LOGGER.info("Getting ProductMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.PRODUCT_MGR.getSpecName(),
                                accessCriteria);
                productMgr = ProductMgrHelper.narrow(libraryManager);
                LOGGER.info("Source returned : {}", productMgr.getClass());
            } else if (managerType.equals(NsiliManagerType.DATA_MODEL_MGR.getSpecName())) {
                LOGGER.info("Getting DataModelMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.DATA_MODEL_MGR.getSpecName(),
                                accessCriteria);
                dataModelMgr = DataModelMgrHelper.narrow(libraryManager);
                LOGGER.info("Source returned : {}", dataModelMgr.getClass());
            } else if (managerType.equals(NsiliManagerType.STANDING_QUERY_MGR.getSpecName())) {
                LOGGER.info("Getting StandingQueryMgr from source...");
                LibraryManager libraryManager =
                        library.get_manager(NsiliManagerType.STANDING_QUERY_MGR.getSpecName(),
                                accessCriteria);
                standingQueryMgr = StandingQueryMgrHelper.narrow(libraryManager);
                LOGGER.info("Source returned : {}", standingQueryMgr.getClass());
            }
        }
    }

    public int getHitCount(Query query) throws Exception {
        if (catalogMgr != null) {
            LOGGER.info("Getting Hit Count From Query...");
            HitCountRequest hitCountRequest = catalogMgr.hit_count(query, new NameValue[0]);
            IntHolder intHolder = new IntHolder();
            hitCountRequest.complete(intHolder);
            LOGGER.info("Server responded with {} hit(s). ", intHolder.value);
            return intHolder.value;
        } else {
            LOGGER.warn("CatalogMgr was not initialized, unable to find hit count");
            return -1;
        }
    }

    public DAG[] submit_query(Query query) throws Exception {
        if (catalogMgr != null) {
            LOGGER.info("Submitting Query To Server...");
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
            LOGGER.info("Server Responded with {} result(s).", dagListHolder.value.length);
            return dagListHolder.value;
        } else {
            LOGGER.info("CatalogMgr is not initialized, unable to submit queries");
            return null;
        }
    }

    public void processAndPrintResults(DAG[] results, boolean downloadProduct) {
        LOGGER.info("Printing DAG Attribute Results...");
        for (int i = 0; i < results.length; i++) {
            LOGGER.info("\t RESULT : {} of {} ", (i + 1), results.length);
            printDAGAttributes(results[i]);
            if (downloadProduct) {
                try {
                    retrieveProductFromDAG(results[i]);
                } catch (MalformedURLException e) {
                    LOGGER.error("Invalid URL used for product retrieval.", e);
                }
            }
        }
    }

    public void printDAGAttributes(DAG dag) {
        LOGGER.info("--------------------");
        LOGGER.info("PRINTING DAG ATTRIBUTES");
        for (int i = 0; i < dag.nodes.length; i++) {
            Node node = dag.nodes[i];
            if (node.node_type.equals(NodeType.ATTRIBUTE_NODE)) {
                String name = node.attribute_name;
                String value = CorbaUtils.getNodeValue(node.value);
                LOGGER.info("{} = {}", name, value);
            }
        }
        LOGGER.info("--------------------");
    }

    public void retrieveProductFromDAG(DAG dag) throws MalformedURLException {
        LOGGER.info("Downloading products...");
        for (int i = 0; i < dag.nodes.length; i++) {
            Node node = dag.nodes[i];
            if (node.attribute_name.equals("productUrl")) {

                String url = node.value.extract_string();
                URL fileDownload = new URL(url);
                String productPath = "product.jpg";
                LOGGER.info("Downloading product : {}", url);
                try (FileOutputStream outputStream = new FileOutputStream(new File(productPath));
                        BufferedInputStream inputStream = new BufferedInputStream(fileDownload.openStream())) {

                    byte[] data = new byte[1024];
                    int count;
                    while ((count = inputStream.read(data, 0, 1024)) != -1) {
                        outputStream.write(data, 0, count);
                    }

                    LOGGER.info("Successfully downloaded product from {}.", url);
                    Files.deleteIfExists(Paths.get(productPath));

                } catch (IOException e) {
                    LOGGER.error("Unable to download product from {}.", url, e);
                }
            }
        }
    }

    public PackageElement[] order(ORB orb, DAG[] dags) throws Exception {
        if (orderMgr != null) {
            LOGGER.info("--------------------------");
            LOGGER.info("OrderMgr getting package specifications");
            String[] supportedPackageSpecs = orderMgr.get_package_specifications();
            if (supportedPackageSpecs != null && supportedPackageSpecs.length > 0) {
                for (String supportedPackageSpec : supportedPackageSpecs) {
                    LOGGER.info("\t {}", supportedPackageSpec);
                }
            } else {
                LOGGER.warn("Server returned no packaging specifications");
            }

            LOGGER.info("Getting OrderMgr Use Modes");
            String[] useModes = orderMgr.get_use_modes();
            for (String useMode : useModes) {
                LOGGER.info("\t {}", useMode);
            }

            short numPriorities = orderMgr.get_number_of_priorities();
            LOGGER.info("Order Mgr num of priorities: {} ", numPriorities);

            Product product = ProductHelper.extract(dags[0].nodes[0].value);
            LOGGER.info("Product: {}", product.toString());
            String productId = getProductID(dags[0]);
            LOGGER.info("Product ID: {}", productId);
            String filename = getFileName(dags[0], productId);

            //Product available
            boolean productAvail = orderMgr.is_available(product, useModes[0]);
            LOGGER.info("Product available: {}", productAvail);

            LOGGER.info("Creating order request...");

            Any portAny = orb.create_any();
            Any protocolAny = orb.create_any();
            protocolAny.insert_string("http");
            portAny.insert_long(Client.LISTEN_PORT);
            NameValue portProp = new NameValue("PORT", portAny);
            NameValue protocolProp = new NameValue("PROTOCOL", protocolAny);

            NameValue[] properties = new NameValue[] {portProp, protocolProp};

            OrderContents order = createOrder(orb, product, supportedPackageSpecs, filename);

            //Validating Order
            LOGGER.info("Validating Order...");
            ValidationResults validationResults = orderMgr.validate_order(order, properties);

            LOGGER.info("Validation Results: ");
            LOGGER.info("\tValid : {} \n" + "\tWarning : {} \n" + "\tDetails : {}",
                    validationResults.valid,
                    validationResults.warning,
                    validationResults.details);

            OrderRequest orderRequest = orderMgr.order(order, properties);

            LOGGER.info("Completing OrderRequest...");
            DeliveryManifestHolder deliveryManifestHolder = new DeliveryManifestHolder();
            orderRequest.set_user_info("Alliance");
            PackageElement[] elements = null;
            try {
                orderRequest.complete(deliveryManifestHolder);

                DeliveryManifest deliveryManifest = deliveryManifestHolder.value;

                LOGGER.info("Completed Order : {}", deliveryManifest.package_name);

                elements = deliveryManifest.elements;
                if (deliveryManifest.elements != null) {
                    for (int i = 0; i < elements.length; i++) {

                        String[] files = elements[i].files;

                        for (int c = 0; c < files.length; c++) {
                            LOGGER.info("\t {}", files[c]);
                        }

                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error completing order request",
                        NsilCorbaExceptionUtil.getExceptionDetails(e));
            }

            return elements;
        } else {
            LOGGER.warn("orderMgr is not initialized, unable to submit order");
            return null;
        }
    }

    public void testStandingQueryMgr(POA poa, Query query) throws Exception {
        if (standingQueryMgr != null) {
            LOGGER.info("----------------------");
            LOGGER.info("Standing Query Manager Test");

            if (standingQueryMgr != null) {
                Event[] events = standingQueryMgr.get_event_descriptions();
                if (events != null) {
                    for (Event event : events) {
                        LOGGER.info("Event: {}\n Name: {}\n Desc: {}",
                                event.event_type.value(),
                                event.event_name,
                                event.event_description);
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
                    LOGGER.error("Order : Unable to activate callback object, already active : {}",
                            NsilCorbaExceptionUtil.getExceptionDetails(e),
                            e);
                }

                org.omg.CORBA.Object obj =
                        poa.create_reference_with_id(callbackId.getBytes(Charset.forName(ENCODING)),
                                CallbackHelper.id());

                Callback callback = CallbackHelper.narrow(obj);

                String standingQueryCallbackId = standingQueryRequest.register_callback(callback);
                nsiliCallback.setCallbackID(standingQueryCallbackId);
                standingQueryCallbacks.add(nsiliCallback);

                LOGGER.info("Registered NSILI Callback: {}", standingQueryCallbackId);

            } catch (Exception e) {
                LOGGER.debug("Error submitting standing query: ",
                        NsilCorbaExceptionUtil.getExceptionDetails(e));
                throw (e);
            }

            LOGGER.info("Standing Query Submitted");
        }
    }

    public void testProductMgr(ORB orb, DAG[] dags) throws Exception {
        if (productMgr != null) {
            LOGGER.info("--------------------------");
            LOGGER.info("Getting ProductMgr Use Modes");
            String[] useModes = productMgr.get_use_modes();
            for (String useMode : useModes) {
                LOGGER.info("\t {}", useMode);
            }

            short numPriorities = productMgr.get_number_of_priorities();
            LOGGER.info("Product Mgr num of priorities: {}", numPriorities);

            Product product = ProductHelper.extract(dags[0].nodes[0].value);
            LOGGER.info("Product: {}", product.toString());

            LOGGER.info("Product is available tests ");
            boolean avail = productMgr.is_available(product, useModes[0]);
            LOGGER.info("\t {} : {}", useModes[0], avail);

            LOGGER.info("Getting ALL Parameters for Product");
            //CORE, ALL, ORDER
            String[] desiredParams = new String[] {"ALL"};

            Any protocolAny = orb.create_any();
            protocolAny.insert_string("https");

            Any portAny = orb.create_any();
            portAny.insert_string(String.valueOf(Client.LISTEN_PORT));

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

            LOGGER.info("Getting related file types");
            String[] relatedFileTypes = productMgr.get_related_file_types(product);
            if (relatedFileTypes != null) {
                for (String relatedFileType : relatedFileTypes) {
                    LOGGER.info("\t {}", relatedFileType);
                }
            }

            try {
                String productID = getProductID(dags[0]);
                String thumbFile = productID + "-thumbnail" + ".jpg";
                LOGGER.info("Getting thumbnail for : {}.  Filenamne : {}", productID, thumbFile);
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
                    LOGGER.info("\t Stored File: {}", location);
                }
            } catch (Exception e) {
                LOGGER.error("Unable to get product thumbnail.",
                        NsilCorbaExceptionUtil.getExceptionDetails(e));
            }

            try {
                String productID = getProductID(dags[0]);
                LOGGER.info("Getting overview for : {}", productID);
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
                LOGGER.error("Unable to get product overview: {}",
                        NsilCorbaExceptionUtil.getExceptionDetails(e),
                        e);
            }
        } else {
            LOGGER.warn("Unable to test ProductMgr as it is not set");
        }
    }

    public void get_parameters(Product product) throws Exception {
        if (productMgr != null) {
            LOGGER.info("Sending Get Parameters Request to Server...");

            String[] desired_parameters = {"CORE", "ALL", "ORDER"};
            NameValue[] properties = new NameValue[0];

            GetParametersRequest parametersRequest = productMgr.get_parameters(product,
                    desired_parameters,
                    properties);
            LOGGER.info("Completing GetParameters Request ...");

            DAGHolder dagHolder = new DAGHolder();
            parametersRequest.complete(dagHolder);

            DAG dag = dagHolder.value;
            LOGGER.info("Resulting Parameters From Server :");
            printDAGAttributes(dag);
        } else {
            LOGGER.warn("productMgr is not initialized, unable to get parameters");
        }
    }

    public void get_related_file_types(Product product) throws Exception {
        if (productMgr != null) {
            LOGGER.info("Sending Get Related File Types Request...");
            String[] related_file_types = productMgr.get_related_file_types(product);
            LOGGER.info("Related File Types : ");
            for (int i = 0; i < related_file_types.length; i++) {
                LOGGER.info("{}", related_file_types[i]);
            }

        } else {
            LOGGER.warn("ProductMgr is not initialized, unable to get related file types");
        }
    }

    public void get_related_files(ORB orb, Product product) throws Exception {
        if (productMgr != null) {
            LOGGER.info("Sending Get Related Files Request...");

            FileLocation fileLocation = new FileLocation("", "", "", "", "");
            NameValue[] properties = {new NameValue("", orb.create_any())};
            Product[] products = {product};

            GetRelatedFilesRequest relatedFilesRequest = productMgr.get_related_files(products,
                    fileLocation,
                    "",
                    properties);
            LOGGER.info("Completing GetRelatedFilesRequest...");

            NameListHolder locations = new NameListHolder();

            relatedFilesRequest.complete(locations);

            LOGGER.info("Location List : ");
            String[] locationList = locations.value;
            for (int i = 0; i < locationList.length; i++) {
                LOGGER.info("{}", locationList[i]);
            }
        } else {
            LOGGER.warn("ProductMgr is not initialized, unable to get related files");
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

        ImageSpec imageSpec = new ImageSpec();
        imageSpec.encoding = SupportDataEncoding.ASCII;
        imageSpec.rrds = new short[] {1};
        imageSpec.algo = "";
        imageSpec.bpp = 0;
        imageSpec.comp = "A";
        imageSpec.imgform = "A";
        imageSpec.imageid = "1234abc";
        imageSpec.geo_region_type = GeoRegionType.LAT_LON;

        Rectangle subSection = new Rectangle();
        subSection.lower_right = new Coordinate2d(0, 0);
        subSection.upper_left = new Coordinate2d(1, 1);
        imageSpec.sub_section = subSection;
        Any imageSpecAny = orb.create_any();
        ImageSpecHelper.insert(imageSpecAny, imageSpec);
        AlterationSpec aSpec = new AlterationSpec("JPEG",
                imageSpecAny,
                region,
                GeoRegionType.NULL_REGION);

        FileLocation fileLocation = new FileLocation("user",
                "pass",
                "localhost",
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
        LOGGER.info("Downloading IOR File From Server...");
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
            LOGGER.info("Successfully Downloaded IOR File From Server.");
            return myString;
        }

        throw new Exception("Error recieving IOR File");
    }

    public void cleanup() {
        try {
            deregisterStandingQueryCallbacks();
        } catch (InvalidInputParameter | SystemFault | ProcessingFault | ObjectNotActive | WrongPolicy e) {
            LOGGER.error("Unable to perform cleanup : {}",
                    NsilCorbaExceptionUtil.getExceptionDetails(e),
                    e);
        }
    }

    public void testCallbackCatalogMgr(POA poa, Query query) throws Exception {
        if (catalogMgr != null) {
            LOGGER.info("Testing Query Results via Callback ...");
            SortAttribute[] sortAttributes = getSortableAttributes();
            String[] resultAttributes = getResultAttributes();
            LOGGER.info("Query: {}", query.bqs_query);

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

            LOGGER.info("Callback Catalog Mgr Callback registered: {}", catalogSearchCallbackID);
        } else {
            LOGGER.warn("CatalogMgr is not initialized, unable to submit queries");
        }
    }

    private void deregisterStandingQueryCallbacks()
            throws InvalidInputParameter, SystemFault, ProcessingFault, ObjectNotActive,
            WrongPolicy {
        for (TestNsiliCallback callback : callbacks) {
            LOGGER.info("Freeing callback: {}", callback.getCallbackID());
            callback.getQueryRequest()
                    .free_callback(callback.getCallbackID());
        }

        if (standingQueryRequest != null) {
            standingQueryRequest.cancel();
        }

        for (TestNsiliStandingQueryCallback callback : standingQueryCallbacks) {
            LOGGER.info("Freeing standing query callback: {}", callback.getCallbackID());
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
            LOGGER.info("**************************************************************");
            LOGGER.info("******************* NOTIFY CALLED ****************************");
            LOGGER.info("**************************************************************");
            try {
                LOGGER.info("--------  TestNsiliCallback.notify --------");
                LOGGER.info("State: {}", theState);
                LOGGER.info("Request: ");
                if (description != null) {
                    LOGGER.info("\t user_info: {}", description.user_info);
                    LOGGER.info("\t type: {}", description.request_type);
                    LOGGER.info("\t request_info: {}", description.request_info);
                    if (description.request_details != null
                            && description.request_details.length > 0) {
                        LOGGER.info("\t details: {}", description.request_details.length);
                        for (NameValue nameValue : description.request_details) {
                            if (nameValue.aname != null && nameValue.value != null) {
                                String value = getString(nameValue.value);
                                if (nameValue.aname.equalsIgnoreCase("query")) {
                                    Query q = QueryHelper.extract(nameValue.value);
                                    value = q.bqs_query;
                                }

                                if (value != null) {
                                    LOGGER.info("\t\t {} = {}", nameValue.aname, value);
                                } else {
                                    LOGGER.info("\t\t {} = {} (non-string)",
                                            nameValue.aname,
                                            nameValue.value);
                                }
                            }
                        }
                    } else {
                        LOGGER.warn("Notified with no details");
                    }
                }

                LOGGER.info("Results from notification: ");
                DAGListHolder dagListHolder = new DAGListHolder();
                queryRequest.complete_DAG_results(dagListHolder);
                processAndPrintResults(dagListHolder.value, false);

                LOGGER.info("----------------");
            } catch (Exception e) {
                LOGGER.error("Unable to process _notify", e);
            }
        }

        @Override
        public void release() throws ProcessingFault, SystemFault {
            LOGGER.info("TestNsiliCallback.release");
        }
    }

    private class TestNsiliStandingQueryCallback extends CallbackPOA {

        private String callbackID;

        private SubmitStandingQueryRequest queryRequest;

        private long numResultsProcessed = 0;

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
            LOGGER.info("**************************************************************");
            LOGGER.info("******************* NOTIFY CALLED ****************************");
            LOGGER.info("**************************************************************");
            try {
                LOGGER.info("State: {}", theState.value());
                if (theState == State.RESULTS_AVAILABLE) {
                    LOGGER.info("Results are available");
                    LOGGER.info("Request: ");
                    if (description != null) {
                        LOGGER.info("\t user_info: {}", description.user_info);
                        LOGGER.info("\t type: {}", description.request_type);
                        LOGGER.info("\t request_info: {}", description.request_info);
                        if (description.request_details != null
                                && description.request_details.length > 0) {
                            LOGGER.info("\t details: {}", description.request_details.length);
                            for (NameValue nameValue : description.request_details) {
                                if (nameValue.aname != null && nameValue.value != null) {
                                    String value = getString(nameValue.value);
                                    if (nameValue.aname.equalsIgnoreCase("query")) {
                                        Query q = QueryHelper.extract(nameValue.value);
                                        value = q.bqs_query;
                                    }

                                    if (value != null) {
                                        LOGGER.info("\t\t {} = {}", nameValue.aname, value);
                                    } else {
                                        LOGGER.info("\t\t {} = {} (non-string)",
                                                nameValue.aname,
                                                nameValue.value);
                                    }
                                }
                            }
                        } else {
                            LOGGER.warn("Notified with no details");
                        }
                    }
                    LOGGER.info("Results from notification: ");
                    DAGListHolder dagListHolder = new DAGListHolder();
                    while (queryRequest.get_number_of_hits() > 0) {
                        queryRequest.complete_DAG_results(dagListHolder);
                        numResultsProcessed += dagListHolder.value.length;
                        processAndPrintResults(dagListHolder.value, false);
                    }

                    LOGGER.info("Number results processed: {}", numResultsProcessed);
                } else {
                    LOGGER.warn("No results available");
                }

                LOGGER.info("**************************************************************");
            } catch (Exception e) {
                LOGGER.error("Unable to process _notify : {}",
                        NsilCorbaExceptionUtil.getExceptionDetails(e),
                        e);
            }
        }

        @Override
        public void release() throws ProcessingFault, SystemFault {
            LOGGER.info("TestNsiliCallback.release");
        }
    }
}