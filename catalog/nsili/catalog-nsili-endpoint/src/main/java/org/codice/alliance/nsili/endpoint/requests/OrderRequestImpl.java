/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.endpoint.requests;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.shiro.subject.ExecutionException;
import org.codice.alliance.nsili.common.CB.Callback;
import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.DeliveryDetails;
import org.codice.alliance.nsili.common.GIAS.DeliveryManifest;
import org.codice.alliance.nsili.common.GIAS.DeliveryManifestHolder;
import org.codice.alliance.nsili.common.GIAS.Destination;
import org.codice.alliance.nsili.common.GIAS.DestinationType;
import org.codice.alliance.nsili.common.GIAS.OrderContents;
import org.codice.alliance.nsili.common.GIAS.OrderRequestPOA;
import org.codice.alliance.nsili.common.GIAS.PackageElement;
import org.codice.alliance.nsili.common.GIAS.PackagingSpec;
import org.codice.alliance.nsili.common.GIAS.ProductDetails;
import org.codice.alliance.nsili.common.GIAS.RequestManager;
import org.codice.alliance.nsili.common.GIAS._RequestManagerStub;
import org.codice.alliance.nsili.common.PackagingSpecFormatType;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.endpoint.managers.AccessManagerImpl;
import org.codice.ddf.platform.util.TemporaryFileBackedOutputStream;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarOutputStream;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.impl.ResourceRequestById;
import ddf.catalog.resource.Resource;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.security.Subject;

public class OrderRequestImpl extends OrderRequestPOA {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderRequestImpl.class);

    private static final String FILE_COUNT_FORMAT = "%02d";

    private static final int DEFAULT_TAR_PERMISSION = 660;

    private static final int MB = 1024 * 1024;

    private static final int MAX_MEMORY_SIZE = 100 * MB;

    private OrderContents order;

    private AccessManagerImpl accessManager;

    private CatalogFramework catalogFramework;

    private Subject subject;

    private String protocol;

    private int port;

    public OrderRequestImpl(OrderContents order, String protocol, int port,
            AccessManagerImpl accessManager, CatalogFramework catalogFramework, Subject subject) {
        this.order = order;
        this.protocol = protocol;
        this.port = port;
        this.accessManager = accessManager;
        this.catalogFramework = catalogFramework;
        this.subject = subject;
    }

    @Override
    public State complete(DeliveryManifestHolder deliveryManifestHolder)
            throws ProcessingFault, SystemFault {
        DeliveryManifest deliveryManifest = new DeliveryManifest();
        List<PackageElement> packageElements = new ArrayList<>();

        if (orderContainsSupportedDelivery()) {
            try {
                String filename = null;
                PackagingSpecFormatType packageFormatType = PackagingSpecFormatType.FILESUNC;

                List<ResourceContainer> files = new ArrayList<>();

                if (order.prod_list != null) {
                    for (ProductDetails productDetails : order.prod_list) {
                        if (productDetails != null) {
                            Metacard metacard = accessManager.getMetacard(productDetails.aProduct);
                            ResourceRequest resourceRequest =
                                    new ResourceRequestById(metacard.getId());
                            ResourceResponse resourceResponse;

                            ResourceRequestCallable resourceRequestCallable =
                                    new ResourceRequestCallable(resourceRequest,
                                            metacard.getSourceId());
                            resourceResponse = subject.execute(resourceRequestCallable);

                            if (resourceResponse != null
                                    && resourceResponse.getResource() != null) {
                                Resource resource = resourceResponse.getResource();
                                ResourceContainer file =
                                        new ResourceContainer(resource.getInputStream(),
                                                resource.getName(),
                                                resource.getSize(),
                                                resource.getMimeTypeValue());
                                files.add(file);

                                // Alterations aren't supported, so we will only return original content
                            }
                        } else {
                            LOGGER.info("Order requested for a null product detail");
                        }
                    }
                } else {
                    throw new BAD_OPERATION("No products specified for the order");
                }

                if (order.pSpec != null) {
                    PackagingSpec packagingSpec = order.pSpec;
                    filename = packagingSpec.package_identifier;
                    packageFormatType =
                            PackagingSpecFormatType.valueOf(packagingSpec.packaging_format_and_compression);
                }

                if (order.del_list != null) {
                    for (DeliveryDetails deliveryDetails : order.del_list) {
                        Destination destination = deliveryDetails.dests;
                        FileLocation httpDestination = destination.f_dest();
                        if (httpDestination != null) {
                            String outputName = filename;

                            if (StringUtils.isBlank(outputName)) {
                                if (StringUtils.isNotBlank(httpDestination.file_name)) {
                                    outputName = httpDestination.file_name;
                                } else {
                                    outputName = UUID.randomUUID()
                                            .toString();
                                }
                            }

                            List<String> filesSent = writeData(httpDestination,
                                    packageFormatType,
                                    files,
                                    outputName);
                            PackageElement packageElement = new PackageElement();
                            packageElement.files = filesSent.toArray(new String[filesSent.size()]);
                            packageElements.add(packageElement);
                        }
                    }
                }
            } catch (UnsupportedEncodingException | WrongAdapter | WrongPolicy e) {
                LOGGER.error("Unable to get Metacard for product: {}", e);
                LOGGER.debug("Metacard retrieval error details", e);
            } catch (IOException | ExecutionException e) {
                LOGGER.error("Unable to retrieve resource: {}", e);
                LOGGER.debug("Retrieve resource error details", e);
            }
        } else {
            throw new NO_IMPLEMENT("Only HTTP(s) is supported");
        }

        if (order.pSpec != null) {
            deliveryManifest.package_name = order.pSpec.package_identifier;
        }

        deliveryManifest.elements =
                packageElements.toArray(new PackageElement[packageElements.size()]);
        deliveryManifestHolder.value = deliveryManifest;

        return State.COMPLETED;
    }

    @Override
    public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
        return new RequestDescription();
    }

    @Override
    public void set_user_info(String message)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
    }

    @Override
    public Status get_status() throws ProcessingFault, SystemFault {
        return new Status();
    }

    @Override
    public DelayEstimate get_remaining_delay() throws ProcessingFault, SystemFault {
        return new DelayEstimate();
    }

    @Override
    public void cancel() throws ProcessingFault, SystemFault {
    }

    @Override
    public String register_callback(Callback acallback)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return "";
    }

    @Override
    public void free_callback(String id)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
    }

    @Override
    public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
        return new _RequestManagerStub();
    }

    private boolean orderContainsSupportedDelivery() {
        if (order.del_list != null) {
            for (DeliveryDetails deliveryDetails : order.del_list) {
                Destination destination = deliveryDetails.dests;
                if (destination.discriminator() == DestinationType.FTP
                        && destination.f_dest() != null) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<String> writeData(FileLocation destination,
            PackagingSpecFormatType packagingSpecFormatType, List<ResourceContainer> files,
            String filename) throws IOException {

        List<String> sentFiles = new ArrayList<>();

        if (!files.isEmpty()) {
            if (files.size() > 1) {
                int totalNum = files.size() + 1;
                String totalNumPortion = String.format(FILE_COUNT_FORMAT, totalNum);

                switch (packagingSpecFormatType) {
                case FILESUNC: {
                    int currNum = 1;
                    for (ResourceContainer file : files) {
                        String currNumPortion = String.format(FILE_COUNT_FORMAT, currNum);
                        String currFileName =
                                filename + "." + currNumPortion + "." + totalNumPortion;
                        writeFile(destination,
                                file.getInputStream(),
                                file.getSize(),
                                currFileName,
                                file.getMimeTypeValue());
                        currNum++;
                        sentFiles.add(currFileName);
                    }
                }
                break;
                case FILESCOMPRESS: {
                    int currNum = 1;
                    for (ResourceContainer file : files) {
                        try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                            getZip(zipOut, file.getInputStream(), file.getName());
                            ByteSource contents = fos.asByteSource();
                            String currNumPortion = String.format(FILE_COUNT_FORMAT, currNum);
                            String currFileName =
                                    filename + "." + currNumPortion + "." + totalNumPortion
                                            + packagingSpecFormatType.getExtension();
                            writeFile(destination,
                                    contents.openStream(),
                                    contents.size(),
                                    currFileName,
                                    packagingSpecFormatType.getContentType());
                            sentFiles.add(currFileName);
                            currNum++;
                        }
                    }
                }
                break;
                case FILESZIP: {
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                        getZip(zipOut, files);
                        ByteSource zip = fos.asByteSource();
                        writeFile(destination, packagingSpecFormatType, filename, sentFiles, zip);
                    }
                }
                break;
                case FILESGZIP: {
                    int currNum = 1;
                    for (ResourceContainer file : files) {
                        try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                GZIPOutputStream zipOut = new GZIPOutputStream(fos)) {
                            getGzip(zipOut, file.getInputStream());
                            ByteSource contents = fos.asByteSource();
                            String currNumPortion = String.format(FILE_COUNT_FORMAT, currNum);
                            String currFileName =
                                    filename + "." + currNumPortion + "." + totalNumPortion
                                            + packagingSpecFormatType.getExtension();
                            writeFile(destination,
                                    contents.openStream(),
                                    contents.size(),
                                    currFileName,
                                    packagingSpecFormatType.getContentType());
                            sentFiles.add(currFileName);
                            currNum++;
                        }
                    }
                }
                break;
                case TARUNC: {
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(fos)) {
                        getTar(tarOut, files);
                        ByteSource tar = fos.asByteSource();
                        writeFile(destination, packagingSpecFormatType, filename, sentFiles, tar);
                    }
                }
                break;
                case TARZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, files);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                ZipOutputStream zipOut = new ZipOutputStream(zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            ByteSource zip = zipFos.asByteSource();
                            writeFile(destination,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    zip);
                        }
                    }
                }
                break;
                case TARGZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, files);
                        try (TemporaryFileBackedOutputStream gzipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                GZIPOutputStream zipOut = new GZIPOutputStream(gzipFos)) {
                            getGzip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream());
                            ByteSource zip = gzipFos.asByteSource();
                            writeFile(destination,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    zip);
                        }
                    }
                }
                break;
                case TARCOMPRESS: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, files);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                ZipOutputStream zipOut = new ZipOutputStream(zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            writeFile(destination,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    zipFos.asByteSource());
                        }
                    }
                }
                break;
                default:
                    break;
                }

            } else {
                ResourceContainer file = files.iterator()
                        .next();

                switch (packagingSpecFormatType) {
                case FILESUNC: {
                    writeFile(destination,
                            file.getInputStream(),
                            file.getSize(),
                            filename,
                            file.getMimeTypeValue());
                    sentFiles.add(filename);
                }
                break;
                case FILESCOMPRESS: {
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                        getZip(zipOut, file.getInputStream(), file.getName());
                        ByteSource contents = fos.asByteSource();

                        writeFile(destination,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents);
                    }
                }
                break;
                case TARUNC:
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(fos)) {
                        getTar(tarOut, file);
                        ByteSource contents = fos.asByteSource();
                        writeFile(destination,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents);
                    }
                    break;
                case TARZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, file);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                ZipOutputStream zipOut = new ZipOutputStream(zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            ByteSource contents = zipFos.asByteSource();

                            writeFile(destination,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    contents);
                        }
                    }
                }
                break;
                case FILESZIP:
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            GZIPOutputStream zipOut = new GZIPOutputStream(fos)) {
                        getGzip(zipOut, file.getInputStream());
                        ByteSource contents = fos.asByteSource();
                        writeFile(destination,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents);
                    }
                    break;
                case TARGZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, file);
                        try (TemporaryFileBackedOutputStream gzipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                GZIPOutputStream zipOut = new GZIPOutputStream(gzipFos)) {
                            getGzip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream());
                            ByteSource contents = gzipFos.asByteSource();
                            writeFile(destination,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    contents);
                        }
                    }
                }
                break;
                case FILESGZIP:
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            GZIPOutputStream zipOut = new GZIPOutputStream(fos)) {
                        getGzip(zipOut, file.getInputStream());
                        ByteSource contents = fos.asByteSource();
                        writeFile(destination,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents);
                    }
                    break;
                case TARCOMPRESS: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, file);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE);
                                ZipOutputStream zipOut = new ZipOutputStream(zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            ByteSource contents = zipFos.asByteSource();

                            writeFile(destination,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    contents);
                        }
                    }
                }
                break;
                default:
                    break;
                }
            }
        }

        return sentFiles;
    }

    private void writeFile(FileLocation destination,
            PackagingSpecFormatType packagingSpecFormatType, String filename,
            List<String> sentFiles, ByteSource contents) throws IOException {
        String filenameWithExt = filename + packagingSpecFormatType.getExtension();
        writeFile(destination,
                contents.openStream(),
                contents.size(),
                filenameWithExt,
                packagingSpecFormatType.getContentType());
        sentFiles.add(filenameWithExt);
    }

    protected void writeFile(FileLocation destination, InputStream fileData, long size, String name,
            String contentType) throws IOException {
        CloseableHttpClient httpClient = null;
        String urlPath =
                protocol + "://" + destination.host_name + ":" + port + "/" + destination.path_name
                        + "/" + name;

        LOGGER.debug("Writing ordered file to URL: {}", urlPath);

        try {
            HttpPut putMethod = new HttpPut(urlPath);
            putMethod.addHeader(HTTP.CONTENT_TYPE, contentType);
            HttpEntity httpEntity = new InputStreamEntity(fileData, size);
            putMethod.setEntity(httpEntity);

            if (destination.user_name != null && destination.password != null) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(destination.host_name, port),
                        new UsernamePasswordCredentials(destination.user_name,
                                destination.password));
                httpClient = HttpClients.custom()
                        .setDefaultCredentialsProvider(credsProvider)
                        .build();
            } else {
                httpClient = HttpClients.createDefault();
            }

            httpClient.execute(putMethod);
            fileData.close();
            putMethod.releaseConnection();
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    private void getTar(TarOutputStream tarOut, List<ResourceContainer> files) throws IOException {
        long modTime = System.currentTimeMillis() / 1000;
        int permissions = DEFAULT_TAR_PERMISSION;

        for (ResourceContainer file : files) {
            TarHeader fileHeader = TarHeader.createHeader(file.getName(),
                    file.getSize(),
                    modTime,
                    false,
                    permissions);
            tarOut.putNextEntry(new TarEntry(fileHeader));
            IOUtils.copy(file.getInputStream(), tarOut);
        }

        tarOut.flush();
    }

    private void getTar(TarOutputStream tarOut, ResourceContainer file) throws IOException {
        long modTime = System.currentTimeMillis() / 1000;
        int permissions = DEFAULT_TAR_PERMISSION;

        TarHeader fileHeader = TarHeader.createHeader(file.getName(),
                file.getSize(),
                modTime,
                false,
                permissions);
        tarOut.putNextEntry(new TarEntry(fileHeader));
        IOUtils.copy(file.getInputStream(), tarOut);

        tarOut.flush();

    }

    private void getGzip(GZIPOutputStream zipOut, InputStream data) throws IOException {
        IOUtils.copy(data, zipOut);
        zipOut.flush();
    }

    private void getZip(ZipOutputStream zipOut, InputStream data, String name) throws IOException {

        ZipEntry zipEntry = new ZipEntry(name);
        zipOut.putNextEntry(zipEntry);
        IOUtils.copy(data, zipOut);

        zipOut.flush();

    }

    private void getZip(ZipOutputStream zipOut, List<ResourceContainer> files) throws IOException {

        List<String> addedFiles = new ArrayList<>();
        for (ResourceContainer file : files) {
            if (!addedFiles.contains(file.getName())) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);
                IOUtils.copy(file.getInputStream(), zipOut);
                addedFiles.add(file.getName());
            }
        }

        zipOut.flush();

    }

    class ResourceContainer {
        private InputStream inputStream;

        private String name;

        private String mimeTypeValue;

        private long size;

        public ResourceContainer(InputStream inputStream, String name, long size,
                String mimeTypeValue) {
            this.inputStream = inputStream;
            this.name = name;
            this.mimeTypeValue = mimeTypeValue;
            this.size = size;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getName() {
            return name;
        }

        public String getMimeTypeValue() {
            return mimeTypeValue;
        }

        public long getSize() {
            return size;
        }
    }

    class ResourceRequestCallable implements Callable<ResourceResponse> {
        ResourceRequest request;

        String sourceId;

        public ResourceRequestCallable(ResourceRequest request, String sourceId) {
            this.request = request;
            this.sourceId = sourceId;
        }

        @Override
        public ResourceResponse call()
                throws ResourceNotFoundException, IOException, ResourceNotSupportedException {
            return catalogFramework.getResource(request, sourceId);
        }
    }
}

