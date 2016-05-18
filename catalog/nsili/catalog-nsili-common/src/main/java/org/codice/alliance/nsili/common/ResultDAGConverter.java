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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static DAG convertResult(Result result, ORB orb, POA poa) {
        Double distanceInMeters = result.getDistanceInMeters();
        Double resultScore = result.getRelevanceScore();
        Metacard metacard = result.getMetacard();

        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        ProductImpl productImpl = new ProductImpl();

        String id = result.getMetacard().getId();

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

        org.omg.CORBA.Object obj = poa.create_reference_with_id(result.getMetacard()
                .getId()
                .getBytes(Charset.forName(ENCODING)), ProductHelper.id());
        Product product = ProductHelper.narrow(obj);

        Node productNode = createRootNode(orb);

        Any productAny = orb.create_any();
        ProductHelper.insert(productAny, product);
        productNode.value = productAny;

        graph.addVertex(productNode);

        addCardNodeWithAttributes(graph, productNode, metacard, orb);
        addFileNodeWithAttributes(graph, productNode, metacard, orb);
        addParts(graph, productNode, metacard, orb);

        if (metacard.getThumbnail() != null && metacard.getThumbnail().length > 0) {
            addThumbnailRelatedFile(graph, productNode, metacard, orb);
        }

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        return dag;
    }

    public static void addCardNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        if (metacard.getId() != null) {
            addStringAttribute(graph, cardNode, NsiliConstants.IDENTIFIER, metacard.getId(), orb);
        }

        if (metacard.getCreatedDate() != null) {
            addDateAttribute(graph,
                    cardNode,
                    NsiliConstants.SOURCE_DATE_TIME_MODIFIED,
                    metacard.getCreatedDate(),
                    orb);
            addDateAttribute(graph,
                    cardNode,
                    NsiliConstants.DATE_TIME_MODIFIED,
                    metacard.getCreatedDate(),
                    orb);
        }
    }

    public static void addFileNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        addBooleanAttribute(graph, fileNode, NsiliConstants.ARCHIVED, false, orb);

        Attribute pocAttr = metacard.getAttribute(Metacard.POINT_OF_CONTACT);
        if (pocAttr != null) {
            String pocString = String.valueOf(pocAttr.getValue());
            if (pocString != null) {
                addStringAttribute(graph, fileNode, NsiliConstants.CREATOR, pocString, orb);
            }
        }

        if (metacard.getResourceSize() != null) {
            try {
                Double resSize = Double.valueOf(metacard.getResourceSize());
                Double resSizeMB = convertToMegabytes(resSize);
                addDoubleAttribute(graph, fileNode, NsiliConstants.EXTENT, resSizeMB, orb);
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Couldn't convert the resource size to double: {}",
                        metacard.getResourceSize());
            }
        }

        if (metacard.getContentTypeName() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT,
                    metacard.getContentTypeName(),
                    orb);
        }

        if (metacard.getContentTypeVersion() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT_VERSION,
                    metacard.getContentTypeVersion(),
                    orb);
        }

        Attribute downloadUrlAttr = metacard.getAttribute(Metacard.RESOURCE_DOWNLOAD_URL);
        if (downloadUrlAttr != null) {
            String downloadUrl = String.valueOf(downloadUrlAttr.getValue());
            if (downloadUrl != null) {
                downloadUrl = modifyUrl(downloadUrl, metacard.getTitle());
                addStringAttribute(graph, fileNode, NsiliConstants.PRODUCT_URL, downloadUrl, orb);
            }
        }

        if (metacard.getTitle() != null) {
            addStringAttribute(graph, fileNode, NsiliConstants.TITLE, metacard.getTitle(), orb);
        }

        String siteName = SystemInfo.getSiteName();

        boolean productLocal = true;
        if (siteName != null && metacard.getSourceId() != null
                && !siteName.equals(metacard.getSourceId())) {
            productLocal = false;
        }
        addBooleanAttribute(graph, fileNode, NsiliConstants.IS_PRODUCT_LOCAL, productLocal, orb);
    }

    public static void addParts(DirectedAcyclicGraph<Node, Edge> graph, Node productNode,
            Metacard metacard, ORB orb) {
        Any any = orb.create_any();
        Node partNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_PART, any);
        graph.addVertex(partNode);
        graph.addEdge(productNode, partNode);

        //Determine if more than one part specific view is associated with data in this metacard
        boolean partAdded = false;
        String type = null;

        addCoverageNodeWithAttributes(graph, partNode, metacard, orb);

        if (metacardContainsImageryData(metacard)) {
            type = NsiliProductType.IMAGERY.getSpecName();
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

        addCommonNodeWithAttributes(graph, partNode, metacard, type, orb);
    }

    public static void addCommonNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node partNode, Metacard metacard, String type, ORB orb) {
        Any any = orb.create_any();
        Node commonNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_COMMON, any);
        graph.addVertex(commonNode);
        graph.addEdge(partNode, commonNode);

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

        if (metacard.getId() != null) {
            addStringAttribute(graph,
                    commonNode,
                    NsiliConstants.IDENTIFIER_UUID,
                    metacard.getId(),
                    orb);
        }

        if (type != null) {
            addStringAttribute(graph, commonNode, NsiliConstants.TYPE, type, orb);
        }
    }

    public static void addCoverageNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node partNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();

        if (metacardContainsGeoInfo(metacard)) {
            Node coverageNode = new Node(0,
                    NodeType.ENTITY_NODE,
                    NsiliConstants.NSIL_COVERAGE,
                    any);
            graph.addVertex(coverageNode);
            graph.addEdge(partNode, coverageNode);

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
                    LOGGER.info("Unable to parse WKT for bounding box: {}", wktGeo, pe);
                }
            }
        }
    }

    public static void addThumbnailRelatedFile(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();
        Node relatedFileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_RELATED_FILE, any);
        graph.addVertex(relatedFileNode);
        graph.addEdge(productNode, relatedFileNode);

        Attribute pocAttr = metacard.getAttribute(Metacard.POINT_OF_CONTACT);
        if (pocAttr != null) {
            String pocString = String.valueOf(pocAttr.getValue());
            if (pocString != null) {
                addStringAttribute(graph, relatedFileNode, NsiliConstants.CREATOR, pocString, orb);
            }
        }

        addDateAttribute(graph,
                relatedFileNode,
                NsiliConstants.DATE_TIME_DECLARED,
                metacard.getCreatedDate(),
                orb);

        if (metacard.getResourceSize() != null) {
            try {
                Double resSize = (double)metacard.getThumbnail().length;
                Double resSizeMB = convertToMegabytes(resSize);
                addDoubleAttribute(graph, relatedFileNode, NsiliConstants.EXTENT, resSizeMB, orb);
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Couldn't convert the thumbnail size to double: {}",
                        metacard.getResourceSize());
            }
        }

        try {
            String thumbnailURL = new URI(SystemBaseUrl.constructUrl(
                    CATALOG_SOURCE_PATH + "/" + metacard.getSourceId() + "/" + metacard.getId() + "?transform="
                            + THUMBNAIL_TRANSFORMER, true)).toASCIIString();
            addStringAttribute(graph, relatedFileNode, NsiliConstants.URL, thumbnailURL, orb);
        } catch (URISyntaxException e) {
            LOGGER.warn("Unable to construct URI: {}", e);
            LOGGER.debug("", e);
        }

        String siteName = SystemInfo.getSiteName();

        boolean fileLocal = true;
        if (siteName != null && metacard.getSourceId() != null
                && !siteName.equals(metacard.getSourceId())) {
            fileLocal = false;
        }
        addBooleanAttribute(graph, relatedFileNode, NsiliConstants.IS_FILE_LOCAL, fileLocal, orb);
    }

    public static Node createRootNode(ORB orb) {
        return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
    }

    public static void addStringAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, String value, ORB orb) {
        Any any = orb.create_any();
        any.insert_wstring(value);
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

        return new AbsTime(new org.codice.alliance.nsili.common.UCO.Date((short) cal.get(
                Calendar.YEAR),
                (short) (cal.get(Calendar.MONTH) + 1),
                (short) cal.get(Calendar.DAY_OF_MONTH)),
                new Time((short) cal.get(Calendar.HOUR_OF_DAY),
                        (short) cal.get(Calendar.MINUTE),
                        (short) cal.get(Calendar.SECOND)));
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
        return url+"&nsiliFilename="+name;
    }
}
