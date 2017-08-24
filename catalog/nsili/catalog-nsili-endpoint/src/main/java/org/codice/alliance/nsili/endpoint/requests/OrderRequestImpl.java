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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
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
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.State;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.endpoint.NsiliEndpoint;
import org.codice.alliance.nsili.endpoint.managers.AccessManagerImpl;
import org.codice.alliance.nsili.endpoint.managers.EmailConfiguration;
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
import ddf.security.service.SecurityServiceException;

public class OrderRequestImpl extends OrderRequestPOA {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OrderRequestImpl.class);

    private static final String FILE_COUNT_FORMAT = "%02d";

    private static final int DEFAULT_TAR_PERMISSION = 660;

    private static final int MB = 1024 * 1024;

    private static final int MAX_MEMORY_SIZE = 100 * MB;

    private final Function<Destination, Optional<DestinationSink>> destinationSinkFactory;

    private OrderContents order;

    private AccessManagerImpl accessManager;

    private CatalogFramework catalogFramework;

    /**
     * This constructor is only intended for unit testing.
     */
    OrderRequestImpl(OrderContents order, AccessManagerImpl accessManager,
            CatalogFramework catalogFramework,
            Function<Destination, Optional<DestinationSink>> destinationSinkFactory) {
        this.order = order;
        this.accessManager = accessManager;
        this.catalogFramework = catalogFramework;
        this.destinationSinkFactory = destinationSinkFactory;
    }

    public OrderRequestImpl(OrderContents order, String protocol, int port,
            AccessManagerImpl accessManager, CatalogFramework catalogFramework,
            EmailConfiguration emailConfiguration) {
        this(order, accessManager, catalogFramework, destination -> {
            switch (destination.discriminator()
                    .value()) {
            case DestinationType._FTP:
                return Optional.of(new FtpDestinationSink(destination.f_dest(), port, protocol));
            case DestinationType._EMAIL:
                return Optional.of(new EmailDestinationSink(destination.e_dest(),
                        emailConfiguration));
            default:
                return Optional.empty();
            }
        });
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
                            resourceResponse = NsiliEndpoint.getGuestSubject()
                                    .execute(resourceRequestCallable);

                            if (resourceResponse != null
                                    && resourceResponse.getResource() != null) {
                                Resource resource = resourceResponse.getResource();
                                ResourceContainer file =
                                        new ResourceContainer(resource.getInputStream(),
                                                resource.getName(),
                                                resource.getSize(),
                                                resource.getMimeTypeValue(),
                                                metacard);
                                files.add(file);
                                // Alterations aren't supported, so we will only return original content
                            }
                        } else {
                            LOGGER.debug("Order requested for a null product detail");
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

                        Optional<DestinationSink> destinationSink = destinationSinkFactory.apply(
                                destination);
                        if (destinationSink.isPresent()) {
                            List<String> filesSent = writeData(destinationSink.get(),
                                    packageFormatType,
                                    files,
                                    filename);
                            PackageElement packageElement = new PackageElement();
                            packageElement.files = filesSent.toArray(new String[filesSent.size()]);
                            packageElements.add(packageElement);
                        }

                    }
                }
            } catch (UnsupportedEncodingException | WrongAdapter | WrongPolicy e) {
                LOGGER.debug("Unable to get Metacard for product:", e);
            } catch (IOException | ExecutionException | SecurityServiceException e) {
                LOGGER.debug("Unable to retrieve resource:", e);
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
                Destination dest = deliveryDetails.dests;
                if (isFTP(dest) || isEmail(dest)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFTP(Destination dest) {
        return (dest.discriminator() == DestinationType.FTP) && (dest.f_dest() != null);
    }

    private boolean isEmail(Destination dest) {
        return (dest.discriminator() == DestinationType.EMAIL) && (dest.e_dest() != null);
    }

    private List<String> writeData(DestinationSink destinationSink,
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
                        destinationSink.writeFile(file.getInputStream(),
                                file.getSize(),
                                currFileName,
                                file.getMimeTypeValue(),
                                Collections.singletonList(file.getMetacard()));
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
                            try (InputStream inputStream = contents.openStream()) {
                                destinationSink.writeFile(inputStream,
                                        contents.size(),
                                        currFileName,
                                        packagingSpecFormatType.getContentType(),
                                        Collections.singletonList(file.getMetacard()));
                                sentFiles.add(currFileName);
                            }
                            currNum++;
                        }
                    }
                }
                break;
                case FILESZIP: {
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE); ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                        getZip(zipOut, files);
                        ByteSource zip = fos.asByteSource();
                        writeFile(destinationSink,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                zip,
                                files.stream()
                                        .map(ResourceContainer::getMetacard)
                                        .collect(Collectors.toList()));
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
                            try (InputStream inputStream = contents.openStream()) {
                                destinationSink.writeFile(inputStream,
                                        contents.size(),
                                        currFileName,
                                        packagingSpecFormatType.getContentType(),
                                        Collections.singletonList(file.getMetacard()));
                                sentFiles.add(currFileName);
                            }
                            currNum++;
                        }
                    }
                }
                break;
                case TARUNC: {
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE); TarOutputStream tarOut = new TarOutputStream(fos)) {
                        getTar(tarOut, files);
                        ByteSource tar = fos.asByteSource();
                        writeFile(destinationSink,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                tar,
                                files.stream()
                                        .map(ResourceContainer::getMetacard)
                                        .collect(Collectors.toList()));
                    }
                }
                break;
                case TARZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, files);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE); ZipOutputStream zipOut = new ZipOutputStream(
                                zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            ByteSource zip = zipFos.asByteSource();
                            writeFile(destinationSink,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    zip,
                                    files.stream()
                                            .map(ResourceContainer::getMetacard)
                                            .collect(Collectors.toList()));
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
                                MAX_MEMORY_SIZE); GZIPOutputStream zipOut = new GZIPOutputStream(
                                gzipFos)) {
                            getGzip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream());
                            ByteSource zip = gzipFos.asByteSource();
                            writeFile(destinationSink,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    zip,
                                    files.stream()
                                            .map(ResourceContainer::getMetacard)
                                            .collect(Collectors.toList()));
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
                                MAX_MEMORY_SIZE); ZipOutputStream zipOut = new ZipOutputStream(
                                zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            writeFile(destinationSink,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    zipFos.asByteSource(),
                                    files.stream()
                                            .map(ResourceContainer::getMetacard)
                                            .collect(Collectors.toList()));
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

                List<Metacard> metacards = Collections.singletonList(file.getMetacard());

                switch (packagingSpecFormatType) {
                case FILESUNC: {
                    destinationSink.writeFile(file.getInputStream(),
                            file.getSize(),
                            filename,
                            file.getMimeTypeValue(),
                            metacards);
                    sentFiles.add(filename);
                }
                break;
                case FILESCOMPRESS: {
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE); ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                        getZip(zipOut, file.getInputStream(), file.getName());
                        ByteSource contents = fos.asByteSource();

                        writeFile(destinationSink,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents,
                                metacards);
                    }
                }
                break;
                case TARUNC:
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE); TarOutputStream tarOut = new TarOutputStream(fos)) {
                        getTar(tarOut, file);
                        ByteSource contents = fos.asByteSource();
                        writeFile(destinationSink,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents,
                                metacards);
                    }
                    break;
                case TARZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, file);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE); ZipOutputStream zipOut = new ZipOutputStream(
                                zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            ByteSource contents = zipFos.asByteSource();

                            writeFile(destinationSink,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    contents,
                                    metacards);
                        }
                    }
                }
                break;
                case FILESZIP:
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE); GZIPOutputStream zipOut = new GZIPOutputStream(fos)) {
                        getGzip(zipOut, file.getInputStream());
                        ByteSource contents = fos.asByteSource();
                        writeFile(destinationSink,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents,
                                metacards);
                    }
                    break;
                case TARGZIP: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, file);
                        try (TemporaryFileBackedOutputStream gzipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE); GZIPOutputStream zipOut = new GZIPOutputStream(
                                gzipFos)) {
                            getGzip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream());
                            ByteSource contents = gzipFos.asByteSource();
                            writeFile(destinationSink,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    contents,
                                    metacards);
                        }
                    }
                }
                break;
                case FILESGZIP:
                    try (TemporaryFileBackedOutputStream fos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE); GZIPOutputStream zipOut = new GZIPOutputStream(fos)) {
                        getGzip(zipOut, file.getInputStream());
                        ByteSource contents = fos.asByteSource();
                        writeFile(destinationSink,
                                packagingSpecFormatType,
                                filename,
                                sentFiles,
                                contents,
                                metacards);
                    }
                    break;
                case TARCOMPRESS: {
                    try (TemporaryFileBackedOutputStream tarFos = new TemporaryFileBackedOutputStream(
                            MAX_MEMORY_SIZE);
                            TarOutputStream tarOut = new TarOutputStream(tarFos)) {
                        getTar(tarOut, file);
                        try (TemporaryFileBackedOutputStream zipFos = new TemporaryFileBackedOutputStream(
                                MAX_MEMORY_SIZE); ZipOutputStream zipOut = new ZipOutputStream(
                                zipFos)) {
                            getZip(zipOut,
                                    tarFos.asByteSource()
                                            .openStream(),
                                    filename + ".tar");
                            ByteSource contents = zipFos.asByteSource();

                            writeFile(destinationSink,
                                    packagingSpecFormatType,
                                    filename,
                                    sentFiles,
                                    contents,
                                    metacards);
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

    private void writeFile(DestinationSink destinationSink,
            PackagingSpecFormatType packagingSpecFormatType, String filename,
            List<String> sentFiles, ByteSource contents, List<Metacard> metacards)
            throws IOException {
        String filenameWithExt = filename + packagingSpecFormatType.getExtension();
        try (InputStream inputStream = contents.openStream()) {
            destinationSink.writeFile(inputStream,
                    contents.size(),
                    filenameWithExt,
                    packagingSpecFormatType.getContentType(),
                    metacards);
            sentFiles.add(filenameWithExt);
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

        private Metacard metacard;

        public ResourceContainer(InputStream inputStream, String name, long size,
                String mimeTypeValue, Metacard metacard) {
            this.inputStream = inputStream;
            this.name = name;
            this.mimeTypeValue = mimeTypeValue;
            this.size = size;
            this.metacard = metacard;
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

        public Metacard getMetacard() {
            return metacard;
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