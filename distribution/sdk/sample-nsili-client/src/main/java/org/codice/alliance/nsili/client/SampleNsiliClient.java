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
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.TCKind;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.resource.impl.URLResourceReader;

public class SampleNsiliClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleNsiliClient.class);

    private static final Query QUERY = new Query(NsiliConstants.NSIL_ALL_VIEW,
            "NSIL_CARD.identifier like '%' and NSIL_CARD.sourceLibrary = 'test'");

    private static final Query STANDING_ALL_QUERY = new Query(NsiliConstants.NSIL_ALL_VIEW,
            "NSIL_CARD.identifier like '%'");

    private int listenPort;

    private String emailAddress;

    private ORB orb;

    private POA poa;

    private Library library;

    private CatalogMgr catalogMgr;

    private OrderMgr orderMgr;

    private ProductMgr productMgr;

    private DataModelMgr dataModelMgr;

    private StandingQueryMgr standingQueryMgr;

    private List<TestNsiliCallback> callbacks = new ArrayList<>();

    public List<TestNsiliStandingQueryCallback> standingQueryCallbacks = new ArrayList<>();

    private SubmitStandingQueryRequest standingQueryRequest = null;

    public SampleNsiliClient(int listenPort, String iorUrl, String emailAddress) throws Exception {
        this.listenPort = listenPort;
        this.emailAddress = emailAddress;
        initOrb(iorUrl);
        initPoa();
        initCorbaLibrary(iorUrl);
        initManagers();
    }

    private void initOrb(String iorUrl) {
        orb = ORB.init(new String[] {iorUrl}, null);
    }

    private void initPoa() throws InvalidName, AdapterInactive {
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootPOA.the_POAManager()
                .activate();
        poa = rootPOA;
    }

    private void initCorbaLibrary(String iorUrl) throws Exception {
        final String iorFilePath = getIorString(iorUrl);
        org.omg.CORBA.Object obj = orb.string_to_object(iorFilePath);
        if (obj == null) {
            LOGGER.error("Cannot read {}", iorUrl);
        }
        library = LibraryHelper.narrow(obj);
        LOGGER.info("Library Initialized");
    }

    private String[] getManagerTypes() throws Exception {
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

    private void initManagers() throws Exception {
        final AccessCriteria accessCriteria = new AccessCriteria("", "", "");

        for (String managerType : getManagerTypes()) {
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

    public int getHitCount() throws Exception {
        if (catalogMgr != null) {
            LOGGER.info("Getting Hit Count From Query...");
            HitCountRequest hitCountRequest = catalogMgr.hit_count(QUERY, new NameValue[0]);
            IntHolder intHolder = new IntHolder();
            hitCountRequest.complete(intHolder);
            LOGGER.info("Server responded with {} hit(s). ", intHolder.value);
            return intHolder.value;
        } else {
            LOGGER.warn("CatalogMgr was not initialized, unable to find hit count");
            return -1;
        }
    }

    public DAG[] submitQuery() throws Exception {
        if (catalogMgr != null) {
            LOGGER.info("Submitting Query To Server...");
            DAGListHolder dagListHolder = new DAGListHolder();
            SortAttribute[] sortAttributes = getSortableAttributes();
            String[] resultAttributes = getResultAttributes();

            SubmitQueryRequest submitQueryRequest = catalogMgr.submit_query(QUERY,
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

    private void printDagAttributes(DAG[] dags) {
        Arrays.stream(dags)
                .forEach(this::printDagAttributes);
    }

    /**
     * Prints attributes from dag.
     *
     * @param dag
     * @return hashmap of attributes and their values
     */
    public void printDagAttributes(DAG dag) {
        LOGGER.info("--------------------");
        LOGGER.info("PRINTING DAG ATTRIBUTES");
        Arrays.stream(dag.nodes)
                .filter(node -> node.node_type.equals(NodeType.ATTRIBUTE_NODE))
                .forEach(node -> LOGGER.info("{} = {}",
                        node.attribute_name,
                        CorbaUtils.getNodeValue(node.value)));
        LOGGER.info("--------------------");
    }

    public void downloadProductFromDAG(DAG dag) {
        LOGGER.info("Downloading products...");

        for (Node node : dag.nodes) {
            if (node.attribute_name.equals(NsiliConstants.PRODUCT_URL)) {
                URI fileDownloadUri = null;
                try {
                    fileDownloadUri = getEncodedUriFromString(node.value.extract_string());
                } catch (URISyntaxException | MalformedURLException e) {
                    LOGGER.error("Unable to encode fileDownloadUrl. {}", e);
                    return;
                }

                final String productPath = "product.jpg";
                LOGGER.info("Downloading product : {}", fileDownloadUri);
                try {
                    try (BufferedInputStream inputStream = new BufferedInputStream(fileDownloadUri.toURL()
                            .openStream())) {
                        try (FileOutputStream outputStream = new FileOutputStream(new File(
                                productPath))) {

                            final byte data[] = new byte[1024];
                            int count;
                            while ((count = inputStream.read(data, 0, 1024)) != -1) {
                                outputStream.write(data, 0, count);
                            }

                            LOGGER.info("Successfully downloaded product from {}.",
                                    fileDownloadUri);
                            Files.deleteIfExists(Paths.get(productPath));
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Unable to download product from {}.", fileDownloadUri, e);
                }
            }
        }
    }

    private URI getEncodedUriFromString(String urlString)
            throws URISyntaxException, MalformedURLException {
        URL url = new URL(urlString);

        return new URI(url.getProtocol(),
                url.getUserInfo(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                url.getQuery(),
                url.getRef());
    }

    /**
     * Called only by NsiliEndpointTest to order and extract the first metacard from the order. This
     * method assumes that at least one metacard is in the Catalog so that the order response will
     * contain at least one package element.
     *
     * @param dag of the record to order
     * @return the first file name from the order response
     * @throws Exception, NullPointerException when there is no package element is in the order
     * response
     */
    public String testOrder(DAG dag) throws Exception {
        PackageElement[] packageElements = order(dag);
        return packageElements[0].files[0];
    }

    public PackageElement[] order(DAG dag) throws Exception {
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
                supportedPackageSpecs = new String[0];
            }

            LOGGER.info("Getting OrderMgr Use Modes");
            String[] useModes = orderMgr.get_use_modes();
            for (String useMode : useModes) {
                LOGGER.info("\t {}", useMode);
            }

            LOGGER.info("Order Mgr num of priorities: {} ", orderMgr.get_number_of_priorities());

            Product product = getProductFromDag(dag);
            String filename = getAttributeFromDag(dag, NsiliConstants.FILENAME) + ".dat";

            //Product available
            boolean productAvail = orderMgr.is_available(product, useModes[0]);
            LOGGER.info("Product available: {}", productAvail);

            LOGGER.info("Creating order request...");

            Any portAny = orb.create_any();
            Any protocolAny = orb.create_any();
            protocolAny.insert_string("http");
            portAny.insert_long(listenPort);
            NameValue portProp = new NameValue("PORT", portAny);
            NameValue protocolProp = new NameValue("PROTOCOL", protocolAny);

            NameValue[] properties = new NameValue[] {portProp, protocolProp};

            OrderContents order = createFileOrder(product, supportedPackageSpecs, filename);

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
            PackageElement[] elements;
            try {
                orderRequest.complete(deliveryManifestHolder);

                if (emailAddress != null) {
                    order = createEmailOrder(orb, product, supportedPackageSpecs);

                    //Validating Order
                    LOGGER.info("Validating Email Order...");
                    validationResults = orderMgr.validate_order(order, properties);

                    LOGGER.info("Email Validation Results: ");
                    LOGGER.info("\tValid : " + validationResults.valid + "\n\tWarning : "
                            + validationResults.warning + "\n\tDetails : "
                            + validationResults.details + "\n");

                    orderRequest = orderMgr.order(order, properties);
                    orderRequest.set_user_info("Alliance");
                    orderRequest.complete(deliveryManifestHolder);
                }

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

                return elements;
            } catch (Exception e) {
                LOGGER.error("Error completing order request",
                        NsilCorbaExceptionUtil.getExceptionDetails(e));
                return null;
            }
        } else {
            LOGGER.warn("OrderMgr is not initialized, unable to submit order");
            return null;
        }
    }

    public void testStandingQueryMgr() throws Exception {
        if (standingQueryMgr != null) {
            LOGGER.info("----------------------");
            LOGGER.info("Standing Query Manager Test");

            Event[] events = standingQueryMgr.get_event_descriptions();
            if (events != null) {
                Arrays.stream(events)
                        .forEach(event -> LOGGER.info("Event: {}\n Name: {}\n Desc: {}",
                                event.event_type.value(),
                                event.event_name,
                                event.event_description));
            }

            LifeEvent start = new LifeEvent();
            java.util.Date startDate = new java.util.Date();
            start.at(ResultDAGConverter.getAbsTime(startDate));

            LifeEvent end = new LifeEvent();
            final long ONE_YEAR_IN_MS = TimeUnit.DAYS.toMillis(365);
            long endTime = System.currentTimeMillis() + ONE_YEAR_IN_MS;
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
                standingQueryRequest = standingQueryMgr.submit_standing_query(STANDING_ALL_QUERY,
                        getResultAttributes(),
                        getSortableAttributes(),
                        queryLifeSpan,
                        props);

                standingQueryRequest.set_user_info("Alliance");
                standingQueryRequest.set_number_of_hits(200);

                TestNsiliStandingQueryCallback nsiliCallback = new TestNsiliStandingQueryCallback(
                        standingQueryRequest);

                final String ENCODING = "ISO-8859-1";
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
                LOGGER.info("Standing Query Submitted");
            } catch (Exception e) {
                LOGGER.debug("Error submitting standing query: ",
                        NsilCorbaExceptionUtil.getExceptionDetails(e));
                throw (e);
            }
        } else {
            LOGGER.info("StandingQueryMgr is not initialized, unable to test");
        }
    }

    public void testProductMgr(DAG dag) throws Exception {
        if (productMgr != null) {
            LOGGER.info("--------------------------");
            LOGGER.info("Getting ProductMgr Use Modes");
            final String[] useModes = productMgr.get_use_modes();
            for (String useMode : useModes) {
                LOGGER.info("\t {}", useMode);
            }

            final short numPriorities = productMgr.get_number_of_priorities();
            LOGGER.info("Product Mgr num of priorities: {}", numPriorities);

            final Product product = getProductFromDag(dag);

            LOGGER.info("Product is available tests ");
            final boolean avail = productMgr.is_available(product, useModes[0]);
            LOGGER.info("\t {} : {}", useModes[0], avail);

            final String productID = getProductIdFromDag(dag);

            LOGGER.info("Getting CORE, ALL, and ORDER parameters for : {}", productID);
            getParameters(product);

            LOGGER.info("Getting related file types for : {}", productID);
            getRelatedFileTypes(product);

            LOGGER.info("Getting thumbnail for : {}", productID);
            getRelatedFiles(product);
        } else {
            LOGGER.warn("ProductMgr is not initialized, unable to test");
        }
    }

    private Product getProductFromDag(DAG dag) {
        Product product = ProductHelper.extract(dag.nodes[0].value);
        LOGGER.info("Product: {}", product.toString());
        LOGGER.info("Product ID: {}", getProductIdFromDag(dag));
        return product;
    }

    public DAG getParameters(DAG dag) throws InvalidInputParameter, SystemFault, ProcessingFault {
        return getParameters(getProductFromDag(dag));
    }

    public DAG getParameters(Product product)
            throws InvalidInputParameter, SystemFault, ProcessingFault {
        if (productMgr != null) {
            LOGGER.info("Sending Get Parameters Request...");
            String[] desired_parameters = new String[] {"CORE", "ALL", "ORDER"};  //CORE, ALL, ORDER
            NameValue[] properties = new NameValue[0];

            GetParametersRequest getParametersRequest = productMgr.get_parameters(product,
                    desired_parameters,
                    properties);
            getParametersRequest.set_user_info("Alliance");

            DAGHolder dagHolder = new DAGHolder();
            getParametersRequest.complete(dagHolder);
            LOGGER.info("Resulting Parameters From Server :");
            DAG dag = dagHolder.value;
            printDagAttributes(dag);
            return dag;
        } else {
            LOGGER.warn("ProductMgr is not initialized, unable to get parameters");
        }
        return null;
    }

    public String[] getRelatedFileTypes(DAG dag) throws Exception {
        return getRelatedFileTypes(getProductFromDag(dag));
    }

    public String[] getRelatedFileTypes(Product product) throws Exception {
        if (productMgr != null) {
            LOGGER.info("Sending Get Related File Types Request...");
            String[] relatedFileTypes = productMgr.get_related_file_types(product);
            if (relatedFileTypes != null && relatedFileTypes.length > 0) {
                for (String relatedFileType : relatedFileTypes) {
                    LOGGER.info("Related File Types : ");
                    LOGGER.info("\t {}", relatedFileType);
                }
            } else {
                LOGGER.info("No types returned from Get Related File Types Request");
            }

            return relatedFileTypes;
        } else {
            LOGGER.warn("ProductMgr is not initialized, unable to get related file types");
            return null;
        }
    }

    public String[] getRelatedFiles(DAG dag) throws Exception {
        return getRelatedFiles(getProductFromDag(dag));
    }

    public String[] getRelatedFiles(Product product) throws Exception {
        if (productMgr != null) {
            LOGGER.info("Sending Get Related Files Request...");

            final FileLocation fileLocation = new FileLocation("user",
                    "pass",
                    "localhost",
                    "/nsili/file",
                    "");
            Any portAny = orb.create_any();
            portAny.insert_string(String.valueOf(listenPort));
            NameValue portProp = new NameValue("PORT", portAny);
            NameValue[] properties = new NameValue[] {portProp};
            Product[] products = {product};

            GetRelatedFilesRequest relatedFilesRequest = productMgr.get_related_files(products,
                    fileLocation,
                    NsiliConstants.THUMBNAIL_TYPE,
                    properties);
            relatedFilesRequest.set_user_info("Alliance");
            NameListHolder locations = new NameListHolder();

            relatedFilesRequest.complete(locations);

            String[] locationList = locations.value;
            if (locationList.length > 0) {
                LOGGER.info("Location List : ");

                for (String location : locationList) {
                    LOGGER.info("\t Stored File: {}", location);
                }
            } else {
                LOGGER.info("No locations returned from Get Related Files Request");
            }

            return locationList;
        } else {
            LOGGER.warn("ProductMgr is not initialized, unable to get related files");
            return null;
        }
    }

    private OrderContents createFileOrder(Product product, String[] supportedPackagingSpecs,
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

    private OrderContents createEmailOrder(ORB orb, Product product,
            String[] supportedPackagingSpecs) throws Exception {
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

        Destination destination = new Destination();
        destination.e_dest(emailAddress);

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

    private String getIorString(String iorURL) throws Exception {
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
            return myString.trim();
        }

        throw new Exception("Error receiving IOR File");
    }

    public void testCallbackCatalogMgr() throws Exception {
        if (catalogMgr != null) {
            LOGGER.info("Testing Query Results via Callback ...");
            SortAttribute[] sortAttributes = getSortableAttributes();
            String[] resultAttributes = getResultAttributes();
            LOGGER.info("Query: {}", STANDING_ALL_QUERY.bqs_query);

            SubmitQueryRequest catalogSearchQueryRequest = catalogMgr.submit_query(
                    STANDING_ALL_QUERY,
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

    public void cleanup() {
        deregisterStandingQueryCallbacks();
        orb.shutdown(true);
    }

    private void deregisterStandingQueryCallbacks() {
        LOGGER.info("Deregistering StandingQueryCallbacks ...");

        try {
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
        } catch (InvalidInputParameter | SystemFault | ProcessingFault e) {
            LOGGER.error("Unable to deregister StandingQueryCallbacks : {}",
                    NsilCorbaExceptionUtil.getExceptionDetails(e),
                    e);
        }
    }

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

        for (int c = 0; c < attributeInformationArray.length; c++) {
            AttributeInformation attributeInformation = attributeInformationArray[c];

            if (attributeInformation.sortable) {
                sortableAttributesList.add(new SortAttribute(attributeInformation.attribute_name,
                        Polarity.DESCENDING));
            }

        }

        return sortableAttributesList.toArray(new SortAttribute[sortableAttributesList.size()]);
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

    public String getProductIdFromDag(DAG dag) {
        DAGConverter dagConverter = new DAGConverter(new URLResourceReader());
        dagConverter.setNsiliMetacardType(new MetacardTypeImpl("TestNsiliMetacardType", new ArrayList<>()));
        Metacard metacard = dagConverter.convertDAG(dag, false, "");
        return metacard.getId();
    }

    public static String getAttributeFromDag(DAG dag, String attributeName) {
        for (Node node : dag.nodes) {
            if (node.attribute_name.equalsIgnoreCase(attributeName)) {
                return DAGConverter.getString(node.value);
            }
        }

        return null;
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

        public SubmitQueryRequest getQueryRequest() {
            return queryRequest;
        }

        public String getCallbackID() {
            return callbackID;
        }

        public void setCallbackID(String callbackID) {
            this.callbackID = callbackID;
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
                printDagAttributes(dagListHolder.value);

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

        public SubmitStandingQueryRequest getQueryRequest() {
            return queryRequest;
        }

        public String getCallbackID() {
            return callbackID;
        }

        public void setCallbackID(String callbackID) {
            this.callbackID = callbackID;
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
                        printDagAttributes(dagListHolder.value);
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