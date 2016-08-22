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

import java.io.Serializable;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Core;

public class ResultDAGConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultDAGConverter.class);

    private static final String CATALOG_SOURCE_PATH = "/catalog/sources";

    private static final String THUMBNAIL_TRANSFORMER = "thumbnail";

    private static final String ENCODING = StandardCharsets.UTF_8.name();

    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "([a-zA-Z0-9_:]+):([a-zA-Z0-9_]+).([a-zA-Z0-9]+)");

    public static DAG convertResult(Result result, ORB orb, POA poa, List<String> resultAttributes,
            Map<String, List<String>> mandatoryAttributes) throws DagParsingException {
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
                LOGGER.debug("Convert DAG : Unable to activate product impl object ({}): {}",
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

        List<String> addedAttributes = new ArrayList<>();
        addedAttributes.addAll(addCardNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes));
        addedAttributes.addAll(addFileNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes));
        addedAttributes.addAll(addSecurityNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes));
        addedAttributes.addAll(addMetadataSecurityNodeWithAttributes(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes));
        addedAttributes.addAll(addParts(graph,
                productNode,
                metacard,
                orb,
                attributeName + ":",
                resultAttributes));

        if (metacard.getThumbnail() != null && metacard.getThumbnail().length > 0) {
            addedAttributes.addAll(addThumbnailRelatedFile(graph,
                    productNode,
                    metacard,
                    orb,
                    attributeName + ":",
                    resultAttributes));
        }

        if (mandatoryAttributes != null && !mandatoryAttributes.isEmpty()) {
            final ThreadLocal<Boolean> dataIsValid = new ThreadLocal<>();
            dataIsValid.set(true);
            Map<String, List<String>> addedAttrMap = getAttrMap(addedAttributes);
            addedAttrMap.entrySet()
                    .stream()
                    .forEach(entry -> dataIsValid.set(
                            dataIsValid.get() && processEntry(entry.getKey(),
                                    mandatoryAttributes.get(entry.getKey()),
                                    entry.getValue())));

            if (!dataIsValid.get()) {
                throw new DagParsingException(
                        "One or more mandatory attributes is missing on outgoing data");
            }
        }

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        return dag;
    }

    public static List<String> addCardNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_CARD;

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER), resultAttributes)
                && metacard.getId() != null) {
            addStringAttribute(graph, cardNode, NsiliConstants.IDENTIFIER, metacard.getId(), orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER));
        }

        if (metacard.getCreatedDate() != null) {
            if (shouldAdd(buildAttr(attribute, NsiliConstants.SOURCE_DATE_TIME_MODIFIED),
                    resultAttributes)) {
                addDateAttribute(graph,
                        cardNode,
                        NsiliConstants.SOURCE_DATE_TIME_MODIFIED,
                        metacard.getCreatedDate(),
                        orb);
                addedAttributes.add(buildAttr(attribute, NsiliConstants.SOURCE_DATE_TIME_MODIFIED));
            }

            if (shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_MODIFIED),
                    resultAttributes)) {
                addDateAttribute(graph,
                        cardNode,
                        NsiliConstants.DATE_TIME_MODIFIED,
                        metacard.getCreatedDate(),
                        orb);
                addedAttributes.add(buildAttr(attribute, NsiliConstants.DATE_TIME_MODIFIED));
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.SOURCE_LIBRARY));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.STATUS), resultAttributes)) {
            String status = NsiliCardStatus.CHANGED.name();
            Attribute createdAttr = metacard.getAttribute(Core.METACARD_CREATED);
            Attribute modifiedAttr = metacard.getAttribute(Core.METACARD_MODIFIED);
            if (createdAttr != null && modifiedAttr != null) {
                Date createdDate = (Date) createdAttr.getValue();
                Date modifiedDate = (Date) modifiedAttr.getValue();
                if (createdDate.equals(modifiedDate)) {
                    status = NsiliCardStatus.NEW.name();
                }
            }

            Attribute versionAction = metacard.getAttribute(MetacardVersion.ACTION);

            if (versionAction != null) {
                for (Serializable action : versionAction.getValues()) {
                    if (action.toString()
                            .trim()
                            .equals(MetacardVersion.Action.DELETED.getKey())) {
                        status = NsiliCardStatus.OBSOLETE.name();
                        break;
                    }
                }
            }

            addStringAttribute(graph, cardNode, NsiliConstants.STATUS, status, orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.STATUS));
        }

        return addedAttributes;
    }

    public static List<String> addFileNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        String attribute = parentAttrName + NsiliConstants.NSIL_FILE;

        if (shouldAdd(buildAttr(attribute, NsiliConstants.ARCHIVED), resultAttributes)) {
            addBooleanAttribute(graph, fileNode, NsiliConstants.ARCHIVED, false, orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.ARCHIVED));
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.CREATOR));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.EXTENT), resultAttributes)) {
            if (metacard.getResourceSize() != null) {
                try {
                    Double resSize = Double.valueOf(metacard.getResourceSize());
                    Double resSizeMB = convertToMegabytes(resSize);
                    if (resSizeMB != null) {
                        addDoubleAttribute(graph, fileNode, NsiliConstants.EXTENT, resSizeMB, orb);
                        addedAttributes.add(buildAttr(attribute, NsiliConstants.EXTENT));
                    }
                } catch (NumberFormatException nfe) {
                    LOGGER.debug("Couldn't convert the resource size to double: {}",
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.FORMAT));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.FORMAT_VERSION), resultAttributes)
                && metacard.getContentTypeVersion() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT_VERSION,
                    metacard.getContentTypeVersion(),
                    orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.FORMAT_VERSION));
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
                    addedAttributes.add(buildAttr(attribute, NsiliConstants.PRODUCT_URL));
                }
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.TITLE), resultAttributes)
                && metacard.getTitle() != null) {
            addStringAttribute(graph, fileNode, NsiliConstants.TITLE, metacard.getTitle(), orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.TITLE));
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.IS_PRODUCT_LOCAL));
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.DATE_TIME_DECLARED));
        }
        return addedAttributes;
    }

    public static List<String> addSecurityNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.CLASSIFICATION));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.POLICY), resultAttributes)) {
            addStringAttribute(graph, securityNode, NsiliConstants.POLICY, "NATO", orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.POLICY));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.RELEASABILITY), resultAttributes)) {
            addStringAttribute(graph, securityNode, NsiliConstants.RELEASABILITY, "NATO", orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.RELEASABILITY));
        }
        return addedAttributes;
    }

    public static List<String> addMetadataSecurityNodeWithAttributes(
            DirectedAcyclicGraph<Node, Edge> graph, Node productNode, Metacard metacard, ORB orb,
            String parentAttrName, List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.CLASSIFICATION));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.POLICY), resultAttributes)) {
            addStringAttribute(graph, metadataSecurityNode, NsiliConstants.POLICY, "NATO", orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.POLICY));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.RELEASABILITY), resultAttributes)) {
            addStringAttribute(graph,
                    metadataSecurityNode,
                    NsiliConstants.RELEASABILITY,
                    "NATO",
                    orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.RELEASABILITY));
        }
        return addedAttributes;
    }

    public static List<String> addParts(DirectedAcyclicGraph<Node, Edge> graph, Node productNode,
            Metacard metacard, ORB orb, String parentAttrName, List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.PART_IDENTIFIER));
        }

        addedAttributes.addAll(addSecurityNodeWithAttributes(graph,
                partNode,
                metacard,
                orb,
                attribute + ":",
                resultAttributes));
        addedAttributes.addAll(addCoverageNodeWithAttributes(graph,
                partNode,
                metacard,
                orb,
                attribute + ":",
                resultAttributes));

        if (metacardContainsImageryData(metacard)) {
            type = NsiliProductType.IMAGERY.getSpecName();

            addedAttributes.addAll(addImageryPart(graph,
                    partNode,
                    metacard,
                    type,
                    orb,
                    partIdentifier,
                    attribute + ":",
                    resultAttributes));
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

        addedAttributes.addAll(addCommonNodeWithAttributes(graph,
                partNode,
                metacard,
                type,
                orb,
                attribute + ":",
                resultAttributes));

        return addedAttributes;
    }

    public static List<String> addImageryPart(DirectedAcyclicGraph<Node, Edge> graph, Node partNode,
            Metacard metacard, String type, ORB orb, String partIdentifier, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.CATEGORY));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.DECOMPRESSION_TECHNIQUE),
                resultAttributes)) {
            addStringAttribute(graph,
                    imageryNode,
                    NsiliConstants.DECOMPRESSION_TECHNIQUE,
                    NsiliImageryDecompressionTech.NC.name(),
                    orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.DECOMPRESSION_TECHNIQUE));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER), resultAttributes)) {
            addStringAttribute(graph, imageryNode, NsiliConstants.IDENTIFIER, partIdentifier, orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.NUMBER_OF_BANDS), resultAttributes)) {
            addIntegerAttribute(graph, imageryNode, NsiliConstants.NUMBER_OF_BANDS, 1, orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.NUMBER_OF_BANDS));
        }

        return addedAttributes;
    }

    public static List<String> addCommonNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node partNode, Metacard metacard, String type, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
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
                    addedAttributes.add(buildAttr(attribute, NsiliConstants.DESCRIPTION_ABSTRACT));
                }
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER_UUID), resultAttributes)) {
            String metacardId = getMetacardId(metacard);
            if (metacardId != null) {
                UUID uuid = getUUIDFromCard(metacardId);
                addStringAttribute(graph,
                        commonNode,
                        NsiliConstants.IDENTIFIER_UUID,
                        uuid.toString(),
                        orb);
                addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER_UUID));
            }
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.TYPE), resultAttributes)
                && type != null) {
            addStringAttribute(graph, commonNode, NsiliConstants.TYPE, type, orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.TYPE));
        }

        return addedAttributes;
    }

    public static List<String> addCoverageNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node partNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
        Any any = orb.create_any();

        if (metacardContainsGeoInfo(metacard)) {
            Node coverageNode = new Node(0,
                    NodeType.ENTITY_NODE,
                    NsiliConstants.NSIL_COVERAGE,
                    any);
            graph.addVertex(coverageNode);
            graph.addEdge(partNode, coverageNode);

            String attribute = parentAttrName + NsiliConstants.NSIL_COVERAGE;

            if (shouldAdd(buildAttr(attribute, NsiliConstants.ADVANCED_GEOSPATIAL),
                    resultAttributes)) {
                Attribute geoAttr = metacard.getAttribute(Metacard.GEOGRAPHY);
                if (geoAttr != null) {
                    String wktGeo = String.valueOf(geoAttr.getValue());
                    addStringAttribute(graph,
                            coverageNode,
                            NsiliConstants.ADVANCED_GEOSPATIAL,
                            wktGeo,
                            orb);
                    addedAttributes.add(buildAttr(attribute, NsiliConstants.ADVANCED_GEOSPATIAL));
                }
            }

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
                        addedAttributes.add(buildAttr(attribute,
                                NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX));
                    } catch (ParseException pe) {
                        LOGGER.debug("Unable to parse WKT for bounding box: {}", wktGeo, pe);
                    }
                }
            }
        }
        return addedAttributes;
    }

    public static List<String> addThumbnailRelatedFile(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb, String parentAttrName,
            List<String> resultAttributes) {
        List<String> addedAttributes = new ArrayList<>();
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.CREATOR));
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.DATE_TIME_DECLARED));
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
                    addedAttributes.add(buildAttr(attribute, NsiliConstants.EXTENT));
                } catch (NumberFormatException nfe) {
                    LOGGER.debug("Couldn't convert the thumbnail size to double: {}",
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
                addedAttributes.add(buildAttr(attribute, NsiliConstants.URL));
            } catch (URISyntaxException e) {
                LOGGER.debug("Unable to construct URI: ", e);
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
            addedAttributes.add(buildAttr(attribute, NsiliConstants.IS_FILE_LOCAL));
        }

        if (shouldAdd(buildAttr(attribute, NsiliConstants.FILE_TYPE), resultAttributes)) {
            addStringAttribute(graph,
                    relatedFileNode,
                    NsiliConstants.FILE_TYPE,
                    NsiliConstants.THUMBNAIL_TYPE,
                    orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.FILE_TYPE));
        }

        return addedAttributes;
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
        return new ArrayList<>(getAttributeMap(dag).keySet());
    }

    public static Map<String, String> getAttributeMap(DAG dag) {
        Map<String, String> attributes = new HashMap<>();

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
                    attributes.put(attribute, CorbaUtils.getNodeValue(node.value));
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
        boolean shouldAddAttribute = false;

        if (resultAttributes != null && !resultAttributes.isEmpty()) {
            if (resultAttributes.contains(attributeName)) {
                shouldAddAttribute = true;
            } else {
                if (attributeName.lastIndexOf(':') != -1) {
                    String nonScopeAttr = attributeName.substring(
                            attributeName.lastIndexOf(':') + 1);
                    if (resultAttributes.contains(nonScopeAttr)) {
                        shouldAddAttribute = true;
                    }
                }

                if (!shouldAddAttribute) {
                    int lastDot = attributeName.lastIndexOf(".");
                    if (lastDot != -1) {
                        String simpleAttrName = attributeName.substring(lastDot + 1);
                        if (resultAttributes.contains(simpleAttrName)) {
                            shouldAddAttribute = true;
                        }
                    }
                }
            }
        } else {
            shouldAddAttribute = true;
        }

        if (!shouldAddAttribute) {
            LOGGER.trace("Attribute is not supported in destination data model: {}", attributeName);
        }

        return shouldAddAttribute;
    }

    private static String buildAttr(String parentAttr, String attribute) {
        return parentAttr + "." + attribute;
    }

    private static Map<String, List<String>> getAttrMap(List<String> attributes) {
        return attributes.stream()
                .map(ATTRIBUTE_PATTERN::matcher)
                .filter(Matcher::matches)
                .collect(Collectors.groupingBy(m -> m.group(2),
                        Collectors.mapping(m -> m.group(3), Collectors.toList())));
    }

    private static boolean processEntry(String entryName, List<String> requiredAttrs,
            List<String> parsedAttrs) {
        final ThreadLocal<Boolean> dataIsValid = new ThreadLocal<>();
        dataIsValid.set(true);

        if (requiredAttrs != null) {
            List<String> missingAttrs = requiredAttrs.stream()
                    .filter(requiredAttr -> !parsedAttrs.contains(requiredAttr))
                    .collect(Collectors.toList());
            if (!missingAttrs.isEmpty()) {
                dataIsValid.set(false);
                LOGGER.debug("Node: {} is missing attributes: {}", entryName, missingAttrs);
            }
        }

        return dataIsValid.get();
    }

    public static String getMetacardId(Metacard metacard) {
        String id = metacard.getId();

        Attribute origIdAttr = metacard.getAttribute(MetacardVersion.VERSION_OF_ID);
        if (origIdAttr != null && origIdAttr.getValue().toString() != null) {
            id = origIdAttr.getValue()
                    .toString();
        }

        return id;
    }
}
