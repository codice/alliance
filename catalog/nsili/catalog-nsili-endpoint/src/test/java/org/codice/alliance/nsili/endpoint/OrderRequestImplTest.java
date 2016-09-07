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
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.codice.alliance.nsili.common.GIAS.DelayEstimate;
import org.codice.alliance.nsili.common.GIAS.DeliveryDetails;
import org.codice.alliance.nsili.common.GIAS.DeliveryManifestHolder;
import org.codice.alliance.nsili.common.GIAS.Destination;
import org.codice.alliance.nsili.common.GIAS.OrderContents;
import org.codice.alliance.nsili.common.GIAS.PackagingSpec;
import org.codice.alliance.nsili.common.GIAS.ProductDetails;
import org.codice.alliance.nsili.common.PackagingSpecFormatType;
import org.codice.alliance.nsili.common.UCO.FileLocation;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.RequestDescription;
import org.codice.alliance.nsili.common.UCO.Status;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.endpoint.managers.AccessManagerImpl;
import org.codice.alliance.nsili.endpoint.requests.OrderRequestImpl;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.NO_IMPLEMENT;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;

public class OrderRequestImplTest extends NsiliCommonTest {

    private static final String PROTOCOL = "http";

    private static final int PORT = 20080;

    private AccessManagerImpl accessManager = mock(AccessManagerImpl.class);

    private CatalogFramework mockCatalogFramework = mock(CatalogFramework.class);

    private Product mockProduct1 = mock(Product.class);

    private Product mockProduct2 = mock(Product.class);

    private ResourceResponse mockResourceResponse = mock(ResourceResponse.class);

    private Resource mockResource = mock(Resource.class);

    private String mockResName = "testresource.jpg";

    @Before
    public void setUp() throws Exception {
        setupCommonMocks();
        setupMocks();
    }

    private void setupMocks() throws Exception {
        doReturn(getTestMetacard()).when(accessManager)
                .getMetacard(any(Product.class));
        doReturn(mockResourceResponse).when(mockSubject)
                .execute(any(Callable.class));
        doReturn(mockResource).when(mockResourceResponse)
                .getResource();
        doReturn(mockResName).when(mockResource)
                .getName();
        when(mockResource.getName()).thenAnswer((invocation) -> UUID.randomUUID()
                .toString() + ".jpg");
        doReturn(Integer.valueOf(mockResName.getBytes().length)
                .longValue()).when(mockResource)
                .getSize();
        when(mockResource.getInputStream()).thenAnswer((invocation) -> new ByteArrayInputStream(
                mockResName.getBytes()));
        doReturn("image/jpg").when(mockResource)
                .getMimeTypeValue();
    }

    @Test
    public void testSingleUncompressedOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleCompressedOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESCOMPRESS.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleTarUncompressedOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARUNC.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleTarZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleFilesZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleTarGZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARGZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleFilesGZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESGZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testSingleTarCompressOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARCOMPRESS.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testMultipleUncompressedOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(2));
    }

    @Test
    public void testMultipleCompressedOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESCOMPRESS.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(2));
    }

    @Test
    public void testMultipleTarUncompressedOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARUNC.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testMultipleTarZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testMultipleFilesZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testMultipleTarGZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARGZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testMultipleFilesGZipOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESGZIP.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(2));
    }

    @Test
    public void testMultipleTarCompressOrder() throws SystemFault, ProcessingFault {
        OrderContents order = getMultipleUncompressedTestOrder();
        order.pSpec.packaging_format_and_compression = PackagingSpecFormatType.TARCOMPRESS.name();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test(expected = NO_IMPLEMENT.class)
    public void testUnsupportedDelivery() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        DeliveryDetails deliveryDetail = new DeliveryDetails();
        deliveryDetail.dests = getBadDestination();
        order.del_list = new DeliveryDetails[] {deliveryDetail};

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
    }

    @Test
    public void testUnsetOutputName() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.package_identifier=null;

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void testUnsetDestName() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec.package_identifier=null;
        DeliveryDetails deliveryDetail = new DeliveryDetails();
        deliveryDetail.dests = getBadHttpDestination();
        order.del_list = new DeliveryDetails[] {deliveryDetail};

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test (expected = BAD_OPERATION.class)
    public void testNoProduct() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.prod_list = null;

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
    }

    @Test
    public void testNoPSpec() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();
        order.pSpec = null;

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework) {

            @Override
            protected void writeFile(FileLocation destination, InputStream fileData, long size,
                    String name, String contentType) throws IOException {
            }
        };

        DeliveryManifestHolder holder = new DeliveryManifestHolder();
        orderRequest.complete(holder);
        assertThat(holder.value, notNullValue());
        assertThat(holder.value.elements[0].files.length, is(1));
    }

    @Test
    public void getRequestDescription() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework);

        RequestDescription requestDescription = orderRequest.get_request_description();
        assertThat(requestDescription, notNullValue());
    }

    @Test
    public void testSetUserInfo() throws InvalidInputParameter, SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework);

        orderRequest.set_user_info("test user");
    }

    @Test
    public void testGetStatus() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework);

        Status status = orderRequest.get_status();
        assertThat(status, notNullValue());
    }

    @Test
    public void getRemainingDelay() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework);

        DelayEstimate delayEstimate = orderRequest.get_remaining_delay();
        assertThat(delayEstimate, notNullValue());
    }

    @Test
    public void testCancel() throws SystemFault, ProcessingFault {
        OrderContents order = getUncompressedTestOrder();

        OrderRequestImpl orderRequest = new OrderRequestImpl(order,
                PROTOCOL,
                PORT,
                accessManager,
                mockCatalogFramework);

        orderRequest.cancel();
    }

    private Destination getTestDestination() {
        Destination destination = new Destination();
        FileLocation location = new FileLocation();
        location.host_name = "localhost";
        location.user_name = "user";
        location.password = "password";
        location.file_name = "test_file";
        location.path_name = "/images";
        destination.f_dest(location);
        return destination;
    }

    private Destination getBadHttpDestination() {
        Destination destination = new Destination();
        FileLocation location = new FileLocation();
        location.host_name = "localhost";
        location.user_name = "user";
        location.password = "password";
        location.path_name = "/images";
        location.file_name = "";
        destination.f_dest(location);
        return destination;
    }

    private Destination getBadDestination() {
        Destination destination = new Destination();
        destination.e_dest("no.one@example.com");
        return destination;
    }

    private OrderContents getUncompressedTestOrder() {
        OrderContents order = new OrderContents();
        PackagingSpec packagingSpec = new PackagingSpec();
        packagingSpec.package_identifier = "pkg1234";
        packagingSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESUNC.name();
        order.pSpec = packagingSpec;

        ProductDetails productDetail = new ProductDetails();
        productDetail.aProduct = mockProduct1;
        order.prod_list = new ProductDetails[] {productDetail};

        DeliveryDetails deliveryDetail = new DeliveryDetails();
        deliveryDetail.dests = getTestDestination();
        order.del_list = new DeliveryDetails[] {deliveryDetail};

        return order;
    }

    private OrderContents getMultipleUncompressedTestOrder() {
        OrderContents order = new OrderContents();
        PackagingSpec packagingSpec = new PackagingSpec();
        packagingSpec.package_identifier = "pkg1234";
        packagingSpec.packaging_format_and_compression = PackagingSpecFormatType.FILESUNC.name();
        order.pSpec = packagingSpec;

        ProductDetails productDetail1 = new ProductDetails();
        productDetail1.aProduct = mockProduct1;

        ProductDetails productDetail2 = new ProductDetails();
        productDetail2.aProduct = mockProduct2;

        order.prod_list = new ProductDetails[] {productDetail1, productDetail2};

        DeliveryDetails deliveryDetail = new DeliveryDetails();
        deliveryDetail.dests = getTestDestination();
        order.del_list = new DeliveryDetails[] {deliveryDetail};

        return order;
    }

    private Metacard getTestMetacard() throws URISyntaxException {
        MetacardImpl metacard = new MetacardImpl();
        metacard.setId("ABC123");
        metacard.setResourceURI(new URI("http://mock/resource/1234"));
        return metacard;
    }
}
