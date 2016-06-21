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
package org.codice.alliance.nsili.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.common.UID.ProductHelper;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;

public class ResultDAGConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultDAGConverter.class);

    private static final String CATALOG_SOURCE_PATH = "/catalog/sources";

    private static final String THUMBNAIL_TRANSFORMER = "thumbnail";

    private static final String ENCODING = StandardCharsets.UTF_8.name();

    public static DAG convertResult(Result result, ORB orb, POA poa,
            List<String> resultAttributes) {
        Double distanceInMeters = result.getDistanceInMeters();
        Double resultScore = result.getRelevanceScore();
        Metacard metacard = result.getMetacard();

        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        ProductImpl productImpl = new ProductImpl();

        String id = result.getMetacard()
                .getId();

        if (!CorbaUtils.isIdActive(poa, id.getBytes(Charset.forName(ENCODING)))) {
            try {
                poa.activate_object_with_id(id.getBytes(Charset.forName(ENCODING)), productImpl);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.info("Convert DAG : Unable to activate product impl object ({}): {}",
                        result.getMetacard()
                                .getId(),
                        e.getLocalizedMessage());
            }
        }

        org.omg.CORBA.Object obj =
                poa.create_reference_with_id(id.getBytes(Charset.forName(ENCODING)),
                        ProductHelper.id());
        Product product = ProductHelper.narrow(obj);

        Node productNode = createRootNode(orb);
        String attributeName = NsiliConstants.NSIL_PRODUCT;

        Any productAny = orb.create_any();
        ProductHelper.insert(productAny, product);
        productNode.value = productAny;

        graph.addVertex(productNode);

        addCardNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes);
        addFileNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes);
        addSecurityNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes);
        addMetadataSecurityNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes);
        addParts(graph, productNode, metacard, orb, attributeName + ":", resultAttributes);

        if (metacard.getThumbnail() != null && metacard.getThumbnail().length > 0) {
            addThumbnailRelatedFile(graph,
                    productNode,
                    metacard,
                    orb,
                    attributeName + ":",
                    resultAttributes);
        }

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        return dag;
    }

    public static void addCardNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_CARD;

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER), resultAttributes)
                && metacard.getId() != null) {
            addStringAttribute(graph, cardNode, NsiliConstants.IDENTIFIER, metacard.getId(), orb);
        }

        if (metacard.getCreatedDate() != null) {
            if (shouldAdd(buildAttr(attribute, NsiliConstants.SOURCE_DATE_TIME_MODIFIED),
                    resultAttributes)) {
                addDateAttribute(graph,
                        cardNode,
                        NsiliConstants.SOURCE_DATE_TIME_MODIFIED,
                        metacard.getCreatedDate(),
                        orb);
            }

            if (shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_MODIFIED),
                    resultAttributes)) {
                addDateAttribute(graph,
                        cardNode,
                        NsiliConstants.DATE_TIME_MODIFIED,
                        metacard.getCreatedDate(),
                        orb);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.SOURCE_LIBRARY), resultAttributes)) {
            if (StringUtils.isNotBlank(metacard.getSourceId())) {
                addStringAttribute(graph,
                        cardNode,
                        NsiliConstants.SOURCE_LIBRARY,
                        metacard.getSourceId(),
                        orb);
            } else {
                addStringAttribute(graph,
                        cardNode,
                        NsiliConstants.SOURCE_LIBRARY,
                        NsiliConstants.UNKNOWN,
                        orb);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_MODIFIED), resultAttributes)) {
            addStringAttribute(graph,
                    cardNode,
                    NsiliConstants.STATUS,
                    NsiliCardStatus.CHANGED.name(),
                    orb);
        }
    }

    public static void addFileNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_FILE;

        if (shouldAdd(buildAttr(attribute, NsiliConstants.ARCHIVED), resultAttributes)) {
            addBooleanAttribute(graph, fileNode, NsiliConstants.ARCHIVED, false, orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.CREATOR), resultAttributes)) {
            Attribute pocAttr = metacard.getAttribute(Metacard.POINT_OF_CONTACT);
            if (pocAttr != null) {
                String pocString = String.valueOf(pocAttr.getValue());
                if (StringUtils.isNotBlank(pocString)) {
                    addStringAttribute(graph, fileNode, NsiliConstants.CREATOR, pocString, orb);
                } else {
                    addStringAttribute(graph,
                            fileNode,
                            NsiliConstants.CREATOR,
                            SystemInfo.getSiteName(),
                            orb);
                }
            } else {
                addStringAttribute(graph,
                        fileNode,
                        NsiliConstants.CREATOR,
                        SystemInfo.getSiteName(),
                        orb);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.EXTENT), resultAttributes)) {
            if (metacard.getResourceSize() != null) {
                try {
                    Double resSize = Double.valueOf(metacard.getResourceSize());
                    Double resSizeMB = convertToMegabytes(resSize);
                    if (resSizeMB != null) {
                        addDoubleAttribute(graph, fileNode, NsiliConstants.EXTENT, resSizeMB, orb);
                    }
                } catch (NumberFormatException nfe) {
                    LOGGER.warn("Couldn't convert the resource size to double: {}",
                            metacard.getResourceSize());
                }
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.FORMAT), resultAttributes)
                && metacard.getContentTypeName() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT,
                    metacard.getContentTypeName(),
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.FORMAT_VERSION), resultAttributes)
                && metacard.getContentTypeVersion() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT_VERSION,
                    metacard.getContentTypeVersion(),
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.PRODUCT_URL), resultAttributes)) {
            Attribute downloadUrlAttr = metacard.getAttribute(Metacard.RESOURCE_DOWNLOAD_URL);
            if (downloadUrlAttr != null) {
                String downloadUrl = String.valueOf(downloadUrlAttr.getValue());
                if (downloadUrl != null) {
                    downloadUrl = modifyUrl(downloadUrl, metacard.getTitle());
                    addStringAttribute(graph,
                            fileNode,
                            NsiliConstants.PRODUCT_URL,
                            downloadUrl,
                            orb);
                }
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.TITLE), resultAttributes)
                && metacard.getTitle() != null) {
            addStringAttribute(graph, fileNode, NsiliConstants.TITLE, metacard.getTitle(), orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IS_PRODUCT_LOCAL), resultAttributes)) {
            String siteName = SystemInfo.getSiteName();
            boolean productLocal = true;
            if (siteName != null && metacard.getSourceId() != null
                    && !siteName.equals(metacard.getSourceId())) {
                productLocal = false;
            }
            addBooleanAttribute(graph,
                    fileNode,
                    NsiliConstants.IS_PRODUCT_LOCAL,
                    productLocal,
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_DECLARED), resultAttributes)) {
            if (metacard.getCreatedDate() != null) {
                addDateAttribute(graph,
                        fileNode,
                        NsiliConstants.DATE_TIME_DECLARED,
                        metacard.getCreatedDate(),
                        orb);
            } else {
                addDateAttribute(graph,
                        fileNode,
                        NsiliConstants.DATE_TIME_DECLARED,
                        new Date(),
                        orb);
            }
        }
    }

    public static void addSecurityNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();
        Node securityNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_SECURITY, any);
        graph.addVertex(securityNode);
        graph.addEdge(productNode, securityNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_SECURITY;

        //TODO -- Once taxonomy is complete, set real values
        if (shouldAdd(buildAttr(attribute, NsiliConstants.CLASSIFICATION), resultAttributes)) {
            addStringAttribute(graph,
                    securityNode,
                    NsiliConstants.CLASSIFICATION,
                    NsiliClassification.UNCLASSIFIED.getSpecName(),
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.POLICY), resultAttributes)) {
            addStringAttribute(graph, securityNode, NsiliConstants.POLICY, "NATO", orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.RELEASABILITY), resultAttributes)) {
            addStringAttribute(graph, securityNode, NsiliConstants.RELEASABILITY, "NATO", orb);
        }
    }

    public static void addMetadataSecurityNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();
        Node metadataSecurityNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_METADATA_SECURITY,
                any);
        graph.addVertex(metadataSecurityNode);
        graph.addEdge(productNode, metadataSecurityNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_METADATA_SECURITY;

        //TODO -- Once taxonomy is complete, set real values
        if (shouldAdd(buildAttr(attribute, NsiliConstants.CLASSIFICATION), resultAttributes)) {
            addStringAttribute(graph,
                    metadataSecurityNode,
                    NsiliConstants.CLASSIFICATION,
                    NsiliClassification.UNCLASSIFIED.getSpecName(),
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.POLICY), resultAttributes)) {
            addStringAttribute(graph, metadataSecurityNode, NsiliConstants.POLICY, "NATO", orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.RELEASABILITY), resultAttributes)) {
            addStringAttribute(graph,
                    metadataSecurityNode,
                    NsiliConstants.RELEASABILITY,
                    "NATO",
                    orb);
        }
    }

    public static void addParts(DirectedAcyclicGraph<Node, Edge> graph, Node productNode,
            Metacard metacard, ORB orb, String parentAttrName, List<String> resultAttributes) {
        Any any = orb.create_any();
        Node partNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_PART, any);
        graph.addVertex(partNode);
        graph.addEdge(productNode, partNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_PART;

        //Determine if more than one part specific view is associated with data in this metacard
        boolean partAdded = false;
        String type = null;

        String partIdentifier = "1";
        if (shouldAdd(buildAttr(attribute, NsiliConstants.PART_IDENTIFIER), resultAttributes)) {
            addStringAttribute(graph,
                    partNode,
                    NsiliConstants.PART_IDENTIFIER,
                    partIdentifier,
                    orb);
        }

        addSecurityNodeWithAttributes(graph,
                partNode,
                metacard,
                orb,
                attribute + ":",
                resultAttributes);
        addCoverageNodeWithAttributes(graph,
                partNode,
                metacard,
                orb,
                attribute + ":",
                resultAttributes);

        if (metacardContainsImageryData(metacard)) {
            type = NsiliProductType.IMAGERY.getSpecName();

            addImageryPart(graph,
                    partNode,
                    metacard,
                    type,
                    orb,
                    partIdentifier,
                    attribute + ":",
                    resultAttributes);
        }

        if (metacardContainsGmtiData(metacard)) {
            type = NsiliProductType.GMTI.getSpecName();
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsMessageData(metacard)) {
            type = NsiliProductType.MESSAGE.getSpecName();
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsReportData(metacard)) {
            type = NsiliProductType.REPORT.getSpecName();
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsTdlData(metacard)) {
            type = NsiliProductType.TDL_DATA.getSpecName();
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsVideoData(metacard)) {
            type = NsiliProductType.VIDEO.getSpecName();
            if (partAdded) {

            } else {

            }
        }

        addCommonNodeWithAttributes(graph,
                partNode,
                metacard,
                type,
                orb,
                attribute + ":",
                resultAttributes);
    }

    public static void addImageryPart(DirectedAcyclicGraph<Node, Edge> graph, Node partNode,
            Metacard metacard, String type, ORB orb, String partIdentifier, String parentAttrName,
            List<String> resultAttributes) {
        Any imageryAny = orb.create_any();
        Node imageryNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_IMAGERY,
                imageryAny);
        graph.addVertex(imageryNode);
        graph.addEdge(partNode, imageryNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_IMAGERY;

        //TODO -- Once taxonomy is complete, set real values
        if (shouldAdd(buildAttr(attribute, NsiliConstants.CATEGORY), resultAttributes)) {
            addStringAttribute(graph,
                    imageryNode,
                    NsiliConstants.CATEGORY,
                    NsiliImageryType.VIS.name(),
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.DECOMPRESSION_TECHNIQUE),
                resultAttributes)) {
            addStringAttribute(graph,
                    imageryNode,
                    NsiliConstants.DECOMPRESSION_TECHNIQUE,
                    NsiliImageryDecompressionTech.NC.name(),
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER), resultAttributes)) {
            addStringAttribute(graph, imageryNode, NsiliConstants.IDENTIFIER, partIdentifier, orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.NUMBER_OF_BANDS), resultAttributes)) {
            addIntegerAttribute(graph, imageryNode, NsiliConstants.NUMBER_OF_BANDS, 1, orb);
        }
    }

    public static void addCommonNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node partNode, Metacard metacard, String type, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();
        Node commonNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_COMMON, any);
        graph.addVertex(commonNode);
        graph.addEdge(partNode, commonNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_COMMON;

        if (shouldAdd(buildAttr(attribute, NsiliConstants.DESCRIPTION_ABSTRACT),
                resultAttributes)) {
            Attribute descAttr = metacard.getAttribute(Metacard.DESCRIPTION);
            if (descAttr != null) {
                String descString = String.valueOf(descAttr.getValue());
                if (descString != null) {
                    addStringAttribute(graph,
                            commonNode,
                            NsiliConstants.DESCRIPTION_ABSTRACT,
                            descString,
                            orb);
                }
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER_UUID), resultAttributes)) {
            if (metacard.getId() != null) {
                UUID uuid = getUUIDFromCard(metacard.getId());
                addStringAttribute(graph,
                        commonNode,
                        NsiliConstants.IDENTIFIER_UUID,
                        uuid.toString(),
                        orb);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.TYPE), resultAttributes)
                && type != null) {
            addStringAttribute(graph, commonNode, NsiliConstants.TYPE, type, orb);
        }
    }

    public static void addCoverageNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node partNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();

        if (metacardContainsGeoInfo(metacard)) {
            Node coverageNode = new Node(0,
                    NodeType.ENTITY_NODE,
                    NsiliConstants.NSIL_COVERAGE,
                    any);
            graph.addVertex(coverageNode);
            graph.addEdge(partNode, coverageNode);

            String attribute = parentAttrName + NsiliConstants.NSIL_COVERAGE;

            if (shouldAdd(buildAttr(attribute, NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX),
                    resultAttributes)) {
                Attribute geoAttr = metacard.getAttribute(Metacard.GEOGRAPHY);
                if (geoAttr != null) {
                    String wktGeo = String.valueOf(geoAttr.getValue());
                    try {
                        Geometry boundingGeo = WKTUtil.getWKTBoundingRectangle(wktGeo);
                        Rectangle rect = NsiliGeomUtil.getRectangle(boundingGeo);
                        addGeomAttribute(graph,
                                coverageNode,
                                NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX,
                                rect,
                                orb);
                    } catch (ParseException pe) {
                        LOGGER.warn("Unable to parse WKT for bounding box: {}", wktGeo, pe);
                    }
                }
            }
        }
    }

    public static void addThumbnailRelatedFile(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        Any any = orb.create_any();
        Node relatedFileNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_RELATED_FILE,
                any);
        graph.addVertex(relatedFileNode);
        graph.addEdge(productNode, relatedFileNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_RELATED_FILE;

        if (shouldAdd(buildAttr(attribute, NsiliConstants.CREATOR), resultAttributes)) {
            Attribute pocAttr = metacard.getAttribute(Metacard.POINT_OF_CONTACT);
            if (pocAttr != null) {
                String pocString = String.valueOf(pocAttr.getValue());
                if (StringUtils.isNotBlank(pocString)) {
                    addStringAttribute(graph,
                            relatedFileNode,
                            NsiliConstants.CREATOR,
                            pocString,
                            orb);
                } else {
                    addStringAttribute(graph,
                            relatedFileNode,
                            NsiliConstants.CREATOR,
                            SystemInfo.getSiteName(),
                            orb);
                }
            } else {
                addStringAttribute(graph,
                        relatedFileNode,
                        NsiliConstants.CREATOR,
                        SystemInfo.getSiteName(),
                        orb);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_DECLARED), resultAttributes)) {
            if (metacard.getCreatedDate() != null) {
                addDateAttribute(graph,
                        relatedFileNode,
                        NsiliConstants.DATE_TIME_DECLARED,
                        metacard.getCreatedDate(),
                        orb);
            } else {
                addDateAttribute(graph,
                        relatedFileNode,
                        NsiliConstants.DATE_TIME_DECLARED,
                        new Date(),
                        orb);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.EXTENT), resultAttributes)) {
            if (metacard.getThumbnail() != null) {
                try {
                    Double resSize = (double) metacard.getThumbnail().length;
                    Double resSizeMB = convertToMegabytes(resSize);
                    addDoubleAttribute(graph,
                            relatedFileNode,
                            NsiliConstants.EXTENT,
                            resSizeMB,
                            orb);
                } catch (NumberFormatException nfe) {
                    LOGGER.warn("Couldn't convert the thumbnail size to double: {}",
                            metacard.getResourceSize());
                }
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.URL), resultAttributes)) {
            try {
                String thumbnailURL = new URI(SystemBaseUrl.constructUrl(
                        CATALOG_SOURCE_PATH + "/" + metacard.getSourceId() + "/" + metacard.getId()
                                + "?transform=" + THUMBNAIL_TRANSFORMER, true)).toASCIIString();
                addStringAttribute(graph, relatedFileNode, NsiliConstants.URL, thumbnailURL, orb);
            } catch (URISyntaxException e) {
                LOGGER.warn("Unable to construct URI: {}", e);
                LOGGER.debug("", e);
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IS_FILE_LOCAL), resultAttributes)) {
            String siteName = SystemInfo.getSiteName();

            boolean fileLocal = true;
            if (siteName != null && metacard.getSourceId() != null
                    && !siteName.equals(metacard.getSourceId())) {
                fileLocal = false;
            }
            addBooleanAttribute(graph,
                    relatedFileNode,
                    NsiliConstants.IS_FILE_LOCAL,
                    fileLocal,
                    orb);
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.FILE_TYPE), resultAttributes)) {
            addStringAttribute(graph,
                    relatedFileNode,
                    NsiliConstants.FILE_TYPE,
                    NsiliConstants.THUMBNAIL_TYPE,
                    orb);
        }
    }

    public static Node createRootNode(ORB orb) {
        return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
    }

    public static void addStringAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, String value, ORB orb) {
        Any any = orb.create_any();
        any.insert_string(value);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addIntegerAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Integer integer, ORB orb) {
        Any any = orb.create_any();
        any.insert_ulong(integer);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addShortAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Short shortVal, ORB orb) {
        Any any = orb.create_any();
        any.insert_short(shortVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addDoubleAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Double doubleVal, ORB orb) {
        Any any = orb.create_any();
        any.insert_double(doubleVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addBooleanAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Boolean boolVal, ORB orb) {
        Any any = orb.create_any();
        any.insert_boolean(boolVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addAnyAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Any any, ORB orb) {
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addDateAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Date date, ORB orb) {
        Any any = orb.create_any();
        AbsTimeHelper.insert(any, getAbsTime(date));
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addGeomAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Rectangle rectangle, ORB orb) {
        Any any = orb.create_any();
        RectangleHelper.insert(any, rectangle);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static Double convertToMegabytes(Double resSizeBytes) {
        if (resSizeBytes != null) {
            return resSizeBytes / (1024 * 1024);
        } else {
            return null;
        }
    }

    public static AbsTime getAbsTime(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        return new AbsTime(new org.codice.alliance.nsili.common.UCO.Date((short) cal.get(Calendar.YEAR),
                (short) (cal.get(Calendar.MONTH) + 1),
                (short) cal.get(Calendar.DAY_OF_MONTH)),
                new Time((short) cal.get(Calendar.HOUR_OF_DAY),
                        (short) cal.get(Calendar.MINUTE),
                        (short) cal.get(Calendar.SECOND)));
    }

    public static Map<Integer, Node> createNodeMap(Node[] nodes) {
        Map<Integer, Node> nodeMap = new HashMap<>();
        for (Node node : nodes) {
            nodeMap.put(node.id, node);
        }

        return nodeMap;
    }

    public static List<String> getAttributes(DAG dag) {
        List<String> attributes = new ArrayList<>();

        Map<Integer, Node> nodeMap = createNodeMap(dag.nodes);
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        for (Node node : dag.nodes) {
            graph.addVertex(node);
        }

        for (Edge edge : dag.edges) {
            Node node1 = nodeMap.get(edge.start_node);
            Node node2 = nodeMap.get(edge.end_node);
            if (node1 != null && node2 != null) {
                graph.addEdge(node1, node2);
            }
        }

        DepthFirstIterator<Node, Edge> graphIT = new DepthFirstIterator<>(graph, nodeMap.get(0));
        List<String> nodeStack = new ArrayList<>();

        graphIT.addTraversalListener(new TraversalListener<Node, Edge>() {
            @Override
            public void connectedComponentFinished(
                    ConnectedComponentTraversalEvent connectedComponentTraversalEvent) {

            }

            @Override
            public void connectedComponentStarted(
                    ConnectedComponentTraversalEvent connectedComponentTraversalEvent) {

            }

            @Override
            public void edgeTraversed(EdgeTraversalEvent<Node, Edge> edgeTraversalEvent) {

            }

            @Override
            public void vertexTraversed(VertexTraversalEvent<Node> vertexTraversalEvent) {
                Node node = vertexTraversalEvent.getVertex();
                if (node.node_type != NodeType.ATTRIBUTE_NODE) {
                    nodeStack.add(node.attribute_name);
                }
            }

            @Override
            public void vertexFinished(VertexTraversalEvent<Node> vertexTraversalEvent) {
                Node node = vertexTraversalEvent.getVertex();
                if (node.node_type == NodeType.ATTRIBUTE_NODE) {
                    String attribute = "";
                    int currEntry = 0;
                    int size = nodeStack.size();
                    for (String nodeEntry : nodeStack) {
                        attribute += nodeEntry;
                        if (currEntry < (size - 1)) {
                            attribute += ":";
                        } else {
                            attribute += ".";
                        }
                        currEntry++;
                    }
                    attribute += node.attribute_name;
                    attributes.add(attribute);
                } else {
                    int lastIdx = nodeStack.size() - 1;
                    nodeStack.remove(lastIdx);
                }
            }
        });

        Node rootNode = null;
        while (graphIT.hasNext()) {
            graphIT.setCrossComponentTraversal(false);

            Node node = graphIT.next();
            if (rootNode == null) {
                rootNode = node;
            }
        }

        return attributes;
    }

    private static boolean metacardContainsImageryData(Metacard metacard) {
        //TODO Implement
        return true;
    }

    private static boolean metacardContainsGmtiData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsMessageData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsReportData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsTdlData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsVideoData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsGeoInfo(Metacard metacard) {
        boolean foundGeoInfo = false;

        if (metacard.getAttribute(Metacard.GEOGRAPHY) != null) {
            foundGeoInfo = true;
        }

        return foundGeoInfo;
    }

    private static String modifyUrl(String url, String name) {
        return url + "&nsiliFilename=" + name;
    }

    private static UUID getUUIDFromCard(String id) {
        UUID uuid = null;
        try {
            if (id.contains("-")) {
                uuid = UUID.fromString(id);
            } else if (id.length() == 32) {
                //Attempt to parse as a UUID with no dashes
                StringBuilder sb = new StringBuilder(id);
                sb.insert(8, "-");
                sb.insert(13, "-");
                sb.insert(18, "-");
                sb.insert(23, "-");
                uuid = UUID.fromString(sb.toString());
            }
        } catch (Exception e) {
        }

        //If parsing fails, get a v3 UUID from the bytes of the metacard ID
        if (uuid == null) {
            uuid = UUID.nameUUIDFromBytes(id.getBytes());
        }

        return uuid;
    }

    private static boolean shouldAdd(String attributeName, List<String> resultAttributes) {
        boolean shouldAddAttribute = true;
        if (!resultAttributes.isEmpty()) {
            if (resultAttributes.contains(attributeName)) {
                shouldAddAttribute = true;
            } else {
                int lastDot = attributeName.lastIndexOf(".");
                if (lastDot != -1) {
                    String simpleAttrName = attributeName.substring(lastDot);
                    if (resultAttributes.contains(simpleAttrName)) {
                        shouldAddAttribute = true;
                    } else {
                        shouldAddAttribute = false;
                        LOGGER.trace("Attribute is not supported in destination data model: {}",
                                attributeName);
                    }
                }
            }
        }
        return shouldAddAttribute;
    }

    private static String buildAttr(String parentAttr, String attribute) {
        return parentAttr + "." + attribute;
    }
}
