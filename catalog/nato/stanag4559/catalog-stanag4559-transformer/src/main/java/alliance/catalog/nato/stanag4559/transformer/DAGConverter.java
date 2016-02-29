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
package alliance.catalog.nato.stanag4559.transformer;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.joda.time.DateTime;
import org.omg.CORBA.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;

import alliance.catalog.nato.stanag4559.common.Stanag4559ApprovalStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559Classification;
import alliance.catalog.nato.stanag4559.common.Stanag4559ClassificationComparator;
import alliance.catalog.nato.stanag4559.common.Stanag4559Constants;
import alliance.catalog.nato.stanag4559.common.Stanag4559CxpMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559CxpStatusType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ExploitationSubQualCode;
import alliance.catalog.nato.stanag4559.common.Stanag4559GmtiMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559IRMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryDecompressionTech;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MessageMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MetadataEncodingScheme;
import alliance.catalog.nato.stanag4559.common.Stanag4559ProductType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ReportMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ReportPriority;
import alliance.catalog.nato.stanag4559.common.Stanag4559ReportType;
import alliance.catalog.nato.stanag4559.common.Stanag4559RfiMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559RfiStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559RfiWorkflowStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559SdsOpStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559Security;
import alliance.catalog.nato.stanag4559.common.Stanag4559TaskMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559TaskStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559TdlMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoCategoryType;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoEncodingScheme;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoMetacardType;
import alliance.catalog.nato.stanag4559.common.UCO.AbsTime;
import alliance.catalog.nato.stanag4559.common.UCO.AbsTimeHelper;
import alliance.catalog.nato.stanag4559.common.UCO.DAG;
import alliance.catalog.nato.stanag4559.common.UCO.Edge;
import alliance.catalog.nato.stanag4559.common.UCO.Node;
import alliance.catalog.nato.stanag4559.common.UCO.NodeType;
import alliance.catalog.nato.stanag4559.common.UCO.RectangleHelper;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;

public class DAGConverter {
    private static final long MEGABYTE = 1024L * 1024L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DAGConverter.class);

    private static final WKTWriter WKT_WRITER = new WKTWriter();

    public static MetacardImpl convertDAG(DAG dag) {
        MetacardImpl metacard = null;

        //Need to have at least 2 nodes and an edge for anything useful
        if (dag.nodes != null && dag.edges != null) {
            Map<Integer, Node> nodeMap = createNodeMap(dag.nodes);
            DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

            //Build up the graph
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

            metacard = parseGraph(graph);
        }

        return metacard;
    }

    private static Map<Integer, Node> createNodeMap(Node[] nodes) {
        Map<Integer, Node> nodeMap = new HashMap<>();
        for (Node node : nodes) {
            nodeMap.put(node.id, node);
        }

        return nodeMap;
    }

    private static MetacardImpl parseGraph(DirectedAcyclicGraph<Node, Edge> graph) {
        MetacardImpl metacard = new MetacardImpl();

        Stanag4559Security security = new Stanag4559Security();
        List<Serializable> associatedCards = new ArrayList<>();
        List<Serializable> associatedProducts = new ArrayList<>();

        //Traverse the graph
        DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
        Node parentEntity = null;
        Node assocNode = null;

        while (depthFirstIterator.hasNext()) {
            Node node = depthFirstIterator.next();

            if (node.node_type == NodeType.ROOT_NODE && node.attribute_name.equals(
                    Stanag4559Constants.NSIL_PRODUCT)) {
                //Nothing to process from root node
            } else if (node.node_type == NodeType.ENTITY_NODE) {
                parentEntity = node;
                if (node.attribute_name.equals(Stanag4559Constants.NSIL_ASSOCIATION)) {
                    assocNode = node;
                } else {
                    if (assocNode != null && !isNodeChildOfStart(graph, assocNode, node)) {
                        assocNode = null;
                    }
                }

                //Handle Marker nodes
                if (node.attribute_name.equals(Stanag4559Constants.NSIL_IR)) {
                    addNsilIRAttribute(metacard);
                }
            } else if (node.node_type == NodeType.RECORD_NODE) {
                //Nothing to process from record node
            } else if (parentEntity != null &&
                       node.node_type == NodeType.ATTRIBUTE_NODE &&
                       node.value != null) {
                switch (parentEntity.attribute_name) {
                case Stanag4559Constants.NSIL_APPROVAL:
                    addNsilApprovalAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_CARD:
                    if (assocNode != null) {
                        addNsilAssociation(associatedCards, node);
                    } else {
                        addNsilCardAttribute(metacard, node);
                    }
                    break;
                case Stanag4559Constants.NSIL_SECURITY:
                    addNsilSecurityAttribute(security, node);
                    break;
                case Stanag4559Constants.NSIL_METADATA_SECURITY:
                    addNsilSecurityAttribute(security, node);
                    break;
                case Stanag4559Constants.NSIL_COMMON:
                    addNsilCommonAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_COVERAGE:
                    addNsilCoverageAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_CXP:
                    addNsilCxpAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_EXPLOITATION_INFO:
                    addNsilExploitationInfoAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_FILE:
                    addNsilFileAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_GMTI:
                    addNsilGmtiAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_IMAGERY:
                    addNsilImageryAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_MESSAGE:
                    addNsilMessageAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_REPORT:
                    addNsilReportAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_RFI:
                    addNsilRfiAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_SDS:
                    addNsilSdsAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_STREAM:
                    addNsilStreamAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_TASK:
                    addNsilTaskAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_TDL:
                    addNsilTdlAttribute(metacard, node);
                    break;
                case Stanag4559Constants.NSIL_VIDEO:
                    addNsilVideoAttribute(metacard, node);
                    break;
                default:
                    break;
                }
            }
        }

        addMergedSecurityDescriptor(metacard, security);
        setTopLevelMetacardAttributes(metacard);

        //Add associated data
        if (!associatedCards.isEmpty()) {
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.ASSOCIATIONS, associatedCards));
        }

        if (!associatedProducts.isEmpty()) {
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.RELATED_FILES, associatedProducts));
        }

        return metacard;
    }

    /**
     * Determines if the end node is a child of the start node.
     * @param graph - The complete graph.
     * @param start - Starting node.
     * @param end - Child node to check
     * @return true if the end node is a child of the start node in the provided graph.
     */
    public static boolean isNodeChildOfStart(DirectedAcyclicGraph<Node, Edge> graph, Node start, Node end) {
        boolean endNodeInTree = false;
        DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph, start);
        while (depthFirstIterator.hasNext()) {
            Node currNode = depthFirstIterator.next();
            if (currNode.id == end.id) {
                endNodeInTree = true;
            }
        }
        return endNodeInTree;
    }

    public static void addNsilApprovalAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.APPROVED_BY:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.APPROVAL_BY,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.DATE_TIME_MODIFIED:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.APPROVAL_DATETIME_MODIFIED,
                    convertDate(node.value)));
            break;
        case Stanag4559Constants.STATUS:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.APPROVAL_STATUS,
                    convertApprovalStatus(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilCardAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.IDENTIFIER:
            metacard.setId(node.value.extract_wstring());
            break;
        case Stanag4559Constants.SOURCE_DATE_TIME_MODIFIED:
            Date cardDate = convertDate(node.value);
            metacard.setCreatedDate(cardDate);
            metacard.setEffectiveDate(cardDate);
            break;
        case Stanag4559Constants.DATE_TIME_MODIFIED:
            metacard.setModifiedDate(convertDate(node.value));
            break;
        case Stanag4559Constants.PUBLISHER:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.PUBLISHER,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SOURCE_LIBRARY:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SOURCE_LIBRARY,
                    node.value.extract_wstring()));
            break;
        default:
            break;
        }
    }

    public static void addNsilCommonAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.DESCRIPTION_ABSTRACT:
            metacard.setDescription(node.value.extract_wstring());
            break;
        case Stanag4559Constants.IDENTIFIER_MISSION:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.IDENTIFIER_MISSION,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.IDENTIFIER_UUID:
            metacard.setId(node.value.extract_wstring());
            break;
        case Stanag4559Constants.IDENTIFIER_JC3IEDM:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.ID_JC3IEDM,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.LANGUAGE:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.LANGUAGE,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SOURCE:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SOURCE,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SUBJECT_CATEGORY_TARGET:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SUBJECT_CATEGORY_TARGET,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.TARGET_NUMBER:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.TARGET_NUMBER,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.TYPE:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.PRODUCT_TYPE,
                    convertProductType(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilCoverageAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.SPATIAL_COUNTRY_CODE:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.COUNTRY_CODE,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SPATIAL_GEOGRAPHIC_REF_BOX:
            metacard.setLocation(convertShape(node.value));
            break;
        case Stanag4559Constants.TEMPORAL_START:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.START_DATETIME,
                    convertDate(node.value)));
            break;
        case Stanag4559Constants.TEMPORAL_END:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.END_DATETIME,
                    convertDate(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilCxpAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559CxpMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.STATUS:
            metacard.setAttribute(new AttributeImpl(Stanag4559CxpMetacardType.STATUS,
                    convertCxpStatus(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilIRAttribute(MetacardImpl metacard) {
        metacard.setType(new Stanag4559IRMetacardType());
    }

    public static void addNsilExploitationInfoAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.DESCRIPTION:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.EXPLOITATION_DESCRIPTION,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.LEVEL:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.EXPLOITATION_LEVEL,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.AUTO_GENERATED:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.EXPLOITATION_AUTO_GEN,
                    node.value.extract_boolean()));
            break;
        case Stanag4559Constants.SUBJ_QUALITY_CODE:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.EXPLOITATION_SUBJ_QUAL_CODE,
                    convertExplSubQualCd(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilFileAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.ARCHIVED:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.FILE_ARCHIVED,
                    node.value.extract_boolean()));
            break;
        case Stanag4559Constants.ARCHIVE_INFORMATION:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.FILE_ARCHIVED_INFO,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.CREATOR:
            metacard.setPointOfContact(node.value.extract_wstring());
            break;
        case Stanag4559Constants.DATE_TIME_DECLARED:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.PRODUCT_CREATE_TIME,
                    convertDate(node.value)));
            break;
        case Stanag4559Constants.EXTENT:
            metacard.setResourceSize(String.valueOf(convertMegabytesToBytes(node.value.extract_double())));
            break;
        case Stanag4559Constants.FORMAT:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.FILE_FORMAT,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.FORMAT_VERSION:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.FILE_FORMAT_VER,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.PRODUCT_URL:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.FILE_URL,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.TITLE:
            metacard.setTitle(node.value.extract_wstring());
            break;
        default:
            break;
        }
    }

    public static void addNsilGmtiAttribute(MetacardImpl metacard, Node node) {
        //If any GMTI node is added, then we will set the MetacardType
        metacard.setType(new Stanag4559GmtiMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.IDENTIFIER_JOB:
            metacard.setAttribute(new AttributeImpl(Stanag4559GmtiMetacardType.JOB_ID,
                    node.value.extract_double()));
            break;
        case Stanag4559Constants.NUMBER_OF_TARGET_REPORTS:
            metacard.setAttribute(new AttributeImpl(Stanag4559GmtiMetacardType.NUM_TARGET_REPORTS,
                    node.value.extract_long()));
            break;
        default:
            break;
        }
    }

    public static void addNsilImageryAttribute(MetacardImpl metacard, Node node) {
        //If any Imagery attribute is added, set the card type
        metacard.setType(new Stanag4559ImageryMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.CATEGORY:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.IMAGERY_CATEGORY,
                    convertImageCategory(node.value)));
            break;
        case Stanag4559Constants.CLOUD_COVER_PCT:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.CLOUD_COVER_PCT,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.COMMENTS:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.IMAGERY_COMMENTS,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.DECOMPRESSION_TECHNIQUE:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.DECOMPRESSION_TECHNIQUE,
                    convertDecompressionTechnique(node.value)));
            break;
        case Stanag4559Constants.IDENTIFIER:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.IMAGE_ID,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.NIIRS:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.NIIRS,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.NUMBER_OF_BANDS:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.NUM_BANDS,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.NUMBER_OF_ROWS:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.NUM_ROWS,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.NUMBER_OF_COLS:
            metacard.setAttribute(new AttributeImpl(Stanag4559ImageryMetacardType.NUM_COLS,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.TITLE:
            metacard.setTitle(node.value.extract_wstring());
            break;
        default:
            break;
        }
    }

    public static void addNsilMessageAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559MessageMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.RECIPIENT:
            metacard.setAttribute(new AttributeImpl(Stanag4559MessageMetacardType.RECIPIENT,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SUBJECT:
            metacard.setAttribute(new AttributeImpl(Stanag4559MessageMetacardType.MESSAGE_SUBJECT,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.MESSAGE_BODY:
            metacard.setAttribute(new AttributeImpl(Stanag4559MessageMetacardType.MESSAGE_BODY,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.MESSAGE_TYPE:
            metacard.setAttribute(new AttributeImpl(Stanag4559MessageMetacardType.MESSAGE_TYPE,
                    node.value.extract_wstring()));
            break;
        default:
            break;
        }
    }

    public static void addNsilReportAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559ReportMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.ORIGINATORS_REQ_SERIAL_NUM:
            metacard.setAttribute(new AttributeImpl(Stanag4559ReportMetacardType.ORIGINATOR_REQ_SERIAL_NUM,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.PRIORITY:
            metacard.setAttribute(new AttributeImpl(Stanag4559ReportMetacardType.PRIORITY,
                    convertReportPriority(node.value)));
            break;
        case Stanag4559Constants.TYPE:
            metacard.setAttribute(new AttributeImpl(Stanag4559ReportMetacardType.TYPE,
                    convertReportType(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilRfiAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559RfiMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.FOR_ACTION:
            metacard.setAttribute(new AttributeImpl(Stanag4559RfiMetacardType.FOR_ACTION,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.FOR_INFORMATION:
            metacard.setAttribute(new AttributeImpl(Stanag4559RfiMetacardType.FOR_INFORMATION,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SERIAL_NUMBER:
            metacard.setAttribute(new AttributeImpl(Stanag4559RfiMetacardType.SERIAL_NUMBER,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.STATUS:
            metacard.setAttribute(new AttributeImpl(Stanag4559RfiMetacardType.STATUS,
                    convertRfiStatus(node.value)));
            break;
        case Stanag4559Constants.WORKFLOW_STATUS:
            metacard.setAttribute(new AttributeImpl(Stanag4559RfiMetacardType.WORKFLOW_STATUS,
                    convertRfiWorkflowStatus(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilSdsAttribute(MetacardImpl metacard, Node node) {
        switch(node.attribute_name) {
        case Stanag4559Constants.OPERATIONAL_STATUS:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SDS_OPERATIONAL_STATUS,
                    convertSdsOpStatus(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilSecurityAttribute(Stanag4559Security security, Node node) {
        switch(node.attribute_name) {
        case Stanag4559Constants.POLICY:
            String mergedPolicy = mergeSecurityPolicyString(security.getPolicy(), node.value.extract_wstring());
            security.setPolicy(mergedPolicy);
            break;
        case Stanag4559Constants.RELEASABILITY:
            String mergedReleasability = mergeReleasabilityString(security.getReleasability(), node.value.extract_wstring());
            security.setReleasability(mergedReleasability);
            break;
        case Stanag4559Constants.CLASSIFICATION:
            String classification = mergeClassificationString(security.getClassification(), node.value.extract_wstring());
            security.setClassification(classification);
            break;
        default:
            break;
        }
    }

    public static void addNsilStreamAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case Stanag4559Constants.ARCHIVED:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_ARCHIVED,
                    node.value.extract_boolean()));
            break;
        case Stanag4559Constants.ARCHIVE_INFORMATION:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_ARCHIVAL_INFO,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.CREATOR:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_CREATOR,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.DATE_TIME_DECLARED:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_DATETIME_DECLARED,
                    convertDate(node.value)));
            break;
        case Stanag4559Constants.STANDARD:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_STANDARD,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.STANDARD_VERSION:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_STANDARD_VER,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.SOURCE_URL:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_SOURCE_URL,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.PROGRAM_ID:
            metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.STREAM_PROGRAM_ID,
                    node.value.extract_long()));
            break;
        default:
            break;
        }
    }

    public static void addNsilTaskAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559TaskMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.COMMENTS:
            metacard.setAttribute(new AttributeImpl(Stanag4559TaskMetacardType.COMMENTS,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.STATUS:
            metacard.setAttribute(new AttributeImpl(Stanag4559TaskMetacardType.STATUS,
                    convertTaskStatus(node.value)));
            break;
        default:
            break;
        }
    }

    public static void addNsilTdlAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559TdlMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.ACTIVITY:
            metacard.setAttribute(new AttributeImpl(Stanag4559TdlMetacardType.ACTIVITY,
                    node.value.extract_long()));
            break;

        case Stanag4559Constants.MESSAGE_NUM:
            metacard.setAttribute(new AttributeImpl(Stanag4559TdlMetacardType.MESSAGE_NUM,
                    node.value.extract_wstring()));
            break;
        case Stanag4559Constants.PLATFORM:
            metacard.setAttribute(new AttributeImpl(Stanag4559TdlMetacardType.PLATFORM,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.TRACK_NUM:
            metacard.setAttribute(new AttributeImpl(Stanag4559TdlMetacardType.TRACK_NUM,
                    node.value.extract_wstring()));
            break;
        default:
            break;
        }
    }

    public static void addNsilVideoAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new Stanag4559VideoMetacardType());

        switch (node.attribute_name) {
        case Stanag4559Constants.AVG_BIT_RATE:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.AVG_BIT_RATE,
                    node.value.extract_double()));
            break;
        case Stanag4559Constants.CATEGORY:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.CATEGORY,
                    convertVideoCategory(node.value)));
            break;
        case Stanag4559Constants.ENCODING_SCHEME:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.ENCODING_SCHEME,
                    convertVideoEncodingScheme(node.value)));
            break;
        case Stanag4559Constants.FRAME_RATE:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.FRAME_RATE,
                    node.value.extract_double()));
            break;
        case Stanag4559Constants.NUMBER_OF_ROWS:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.NUM_ROWS,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.NUMBER_OF_COLS:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.NUM_COLS,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.METADATA_ENC_SCHEME:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.METADATA_ENCODING_SCHEME,
                    convertMetadataEncScheme(node.value)));
            break;
        case Stanag4559Constants.MISM_LEVEL:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.MISM_LEVEL,
                    node.value.extract_long()));
            break;
        case Stanag4559Constants.SCANNING_MODE:
            metacard.setAttribute(new AttributeImpl(Stanag4559VideoMetacardType.SCANNING_MODE,
                    node.value.extract_wstring()));
            break;
        default:
            break;
        }
    }

    public static void addNsilAssociation(List<Serializable> associations, Node node) {

        associations.add(node.value.extract_wstring());
    }

    public static void addMergedSecurityDescriptor(MetacardImpl metacard, Stanag4559Security security) {
        metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SECURITY_CLASSIFICATION,
                security.getClassification()));
        metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SECURITY_POLICY,
                security.getPolicy()));
        metacard.setAttribute(new AttributeImpl(Stanag4559MetacardType.SECURITY_RELEASABILITY,
                security.getReleasability()));
    }

    public static void setTopLevelMetacardAttributes(MetacardImpl metacard) {
        //If file data available use that
        Attribute fileProductURLAttr = metacard.getAttribute(Stanag4559MetacardType.FILE_URL);
        if (fileProductURLAttr != null) {
            metacard.setResourceURI(convertURI(fileProductURLAttr.getValue().toString()));

            Attribute fileFormatAttr = metacard.getAttribute(Stanag4559MetacardType.FILE_FORMAT);
            if (fileFormatAttr != null) {
                metacard.setContentTypeName(fileFormatAttr.getValue().toString());
            }

            Attribute fileFormatVerAttr = metacard.getAttribute(Stanag4559MetacardType.FILE_FORMAT_VER);
            if (fileFormatVerAttr != null) {
                metacard.setContentTypeVersion(fileFormatVerAttr.getValue().toString());
            }
        } else {
        //Else use stream info
            Attribute streamURLAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_SOURCE_URL);
            if (streamURLAttr != null) {
                metacard.setResourceURI(convertURI(streamURLAttr.getValue().toString()));

                Attribute streamFormatAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_STANDARD);
                if (streamFormatAttr != null) {
                    metacard.setContentTypeName(streamFormatAttr.getValue().toString());
                }

                Attribute streamFormatVerAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_STANDARD_VER);
                if (streamFormatVerAttr != null) {
                    metacard.setContentTypeVersion(streamFormatVerAttr.getValue().toString());
                }
            }
        }

        if (Stanag4559MessageMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName())) {
            Attribute subjAttr = metacard.getAttribute(Stanag4559MessageMetacardType.MESSAGE_SUBJECT);
            if (subjAttr != null) {
                metacard.setTitle(subjAttr.getValue().toString());
            }

            Attribute bodyAttr = metacard.getAttribute(Stanag4559MessageMetacardType.MESSAGE_BODY);
            if (bodyAttr != null) {
                metacard.setDescription(bodyAttr.getValue().toString());
            }

            Attribute typeAttr = metacard.getAttribute(Stanag4559MessageMetacardType.MESSAGE_TYPE);
            if (typeAttr != null) {
                metacard.setContentTypeName(typeAttr.getValue().toString());

                //Unset the version when we have a message
                metacard.setContentTypeVersion(null);
            }
        } else if (Stanag4559VideoMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName())) {
            Attribute encodingSchemeAttr = metacard.getAttribute(Stanag4559VideoMetacardType.ENCODING_SCHEME);
            if (encodingSchemeAttr != null) {
                metacard.setContentTypeName(encodingSchemeAttr.getValue().toString());

                //Unset the version as we don't know that here
                metacard.setContentTypeVersion(null);
            }
        }
    }


    public static Date convertDate(Any any) {
        AbsTime absTime = AbsTimeHelper.extract(any);
        alliance.catalog.nato.stanag4559.common.UCO.Date ucoDate = absTime.aDate;
        alliance.catalog.nato.stanag4559.common.UCO.Time ucoTime = absTime.aTime;

        DateTime dateTime = new DateTime((int) ucoDate.year,
                (int) ucoDate.month,
                (int) ucoDate.day,
                (int) ucoTime.hour,
                (int) ucoTime.minute,
                (int) ucoTime.second,
                0);
        return dateTime.toDate();
    }

    public static Stanag4559ProductType convertProductType(Any any) {
        String productTypeStr = any.extract_wstring();
        return Stanag4559ProductType.fromSpecName(productTypeStr);
    }

    public static String convertShape(Any any) {
        alliance.catalog.nato.stanag4559.common.UCO.Rectangle rectangle = RectangleHelper.extract(any);
        alliance.catalog.nato.stanag4559.common.UCO.Coordinate2d upperLeft = rectangle.upper_left;
        alliance.catalog.nato.stanag4559.common.UCO.Coordinate2d lowerRight = rectangle.lower_right;

        //UCO Specifies Coordinate2d.x = lat, Coordinate2d.y = lon
        //WKT Uses Coordinate.x = lon, Coordinate.y = lat
        //Need to swap x & y
        Coordinate[] coordinates = new Coordinate[5];
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate lowerLeftCoord = new Coordinate(upperLeft.y, lowerRight.x);
        Coordinate upperLeftCoord = new Coordinate(upperLeft.y, upperLeft.x);
        Coordinate upperRightCoord = new Coordinate(lowerRight.y, upperLeft.x);
        Coordinate lowerRightCoord = new Coordinate(lowerRight.y, lowerRight.x);

        coordinates[0] = lowerLeftCoord;
        coordinates[1] = upperLeftCoord;
        coordinates[2] = upperRightCoord;
        coordinates[3] = lowerRightCoord;
        coordinates[4] = lowerLeftCoord;

        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Geometry geom = new Polygon(shell, null, geometryFactory);

        return WKT_WRITER.write(geom);
    }

    public static int convertMegabytesToBytes(Double megabytes) {
        int bytes = 0;

        if (megabytes != null) {
            bytes = (int) (megabytes * MEGABYTE);
        }

        return bytes;
    }

    public static final URI convertURI(String uriStr) {
        URI uri = null;

        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            LOGGER.info("Unable to parse URI for metacard: " + uriStr, e);
        }

        return uri;
    }

    public static Stanag4559ImageryType convertImageCategory(Any any) {
        String imageryTypeStr = any.extract_wstring();
        return Stanag4559ImageryType.valueOf(imageryTypeStr);
    }

    public static Stanag4559ImageryDecompressionTech convertDecompressionTechnique(Any any) {
        String decompressionTechStr = any.extract_wstring();
        return Stanag4559ImageryDecompressionTech.valueOf(decompressionTechStr);
    }

    public static Stanag4559VideoCategoryType convertVideoCategory(Any any) {
        String videoCategoryType = any.extract_wstring();
        return Stanag4559VideoCategoryType.valueOf(videoCategoryType);
    }

    public static Stanag4559VideoEncodingScheme convertVideoEncodingScheme(Any any) {
        String videoEncSchemeStr = any.extract_wstring();
        return Stanag4559VideoEncodingScheme.fromSpecName(videoEncSchemeStr);
    }

    public static Stanag4559MetadataEncodingScheme convertMetadataEncScheme(Any any) {
        String metadataEncSchemeStr = any.extract_wstring();
        return Stanag4559MetadataEncodingScheme.valueOf(metadataEncSchemeStr);
    }

    public static Stanag4559CxpStatusType convertCxpStatus(Any any) {
        String cxpStatusStr = any.extract_wstring();
        return Stanag4559CxpStatusType.valueOf(cxpStatusStr);
    }

    public static Stanag4559RfiStatus convertRfiStatus(Any any) {
        String rfiStatusStr = any.extract_wstring();
        return Stanag4559RfiStatus.valueOf(rfiStatusStr);
    }

    public static Stanag4559RfiWorkflowStatus convertRfiWorkflowStatus(Any any) {
        String rfiWorkflowStatusStr = any.extract_wstring();
        return Stanag4559RfiWorkflowStatus.valueOf(rfiWorkflowStatusStr);
    }

    public static Stanag4559TaskStatus convertTaskStatus(Any any) {
        String taskStatusStr = any.extract_wstring();
        return Stanag4559TaskStatus.valueOf(taskStatusStr);
    }

    public static Stanag4559ExploitationSubQualCode convertExplSubQualCd(Any any) {
        String explSubQualCodeStr = any.extract_wstring();
        return Stanag4559ExploitationSubQualCode.valueOf(explSubQualCodeStr);
    }

    public static Stanag4559SdsOpStatus convertSdsOpStatus(Any any) {
        String sdsOpStatusStr = any.extract_wstring();
        return Stanag4559SdsOpStatus.fromSpecName(sdsOpStatusStr);
    }

    public static Stanag4559ApprovalStatus convertApprovalStatus(Any any) {
        String approvalStr = any.extract_wstring();
        return Stanag4559ApprovalStatus.fromSpecName(approvalStr);
    }

    public static Stanag4559ReportPriority convertReportPriority(Any any) {
        String reportPriorityStr = any.extract_wstring();
        return Stanag4559ReportPriority.valueOf(reportPriorityStr);
    }

    public static Stanag4559ReportType convertReportType(Any any) {
        String reportTypeStr = any.extract_wstring();
        return Stanag4559ReportType.valueOf(reportTypeStr);
    }

    /**
     * Merge 2 space separated security policies into a single list. Merge is done additively.
     * @param policy1 - Initial list of policies.
     * @param policy2 - Additional policies to add.
     * @return Non-duplicated policies space separated.
     */
    public static String mergeSecurityPolicyString(String policy1, String policy2) {
        if (policy1 == null && policy2 == null) {
            return null;
        } else if (policy1 != null && policy2 == null) {
            return policy1;
        } else if (policy1 == null && policy2 != null) {
            return policy2;
        }

        Set<String> policyAttrs = new HashSet<>();
        String[] policy1Arr = policy1.split(" ");
        String[] policy2Arr = policy2.split(" ");

        for (String policy:policy1Arr) {
            policyAttrs.add(policy);
        }

        for (String policy:policy2Arr) {
            policyAttrs.add(policy);
        }

        return collectionToString(policyAttrs);
    }

    /**
     * Merges releasabilities. Merge is done subtractively, so most restrictive applies.
     * @param releasability1 - Initial list of releasabilities.
     * @param releasability2 - Releasabilities to merge.
     * @return - Non-duplicated releasabilities space separated.
     */
    public static String mergeReleasabilityString(String releasability1, String releasability2) {
        if (releasability1 == null) {
            return releasability2;
        } else if (releasability2 == null) {
            return null;
        }

        Set<String> releasability = new HashSet<>();
        String[] releasebility1Arr = releasability1.split(" ");
        String[] releasability2Arr = releasability2.split(" ");
        for (String release1:releasebility1Arr) {
            boolean found = false;
            for (String release2:releasability2Arr) {
                if (release1.equalsIgnoreCase(releasability2)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                releasability.add(release1);
            }
        }

        return collectionToString(releasability);
    }

    /**
     * Determines most restrictive classification and returns that.
     * @param classification1 - Initial classification
     * @param classification2 - Classification to check
     * @return Most restrictive classification
     */
    public static String mergeClassificationString(String classification1, String classification2) {
        Stanag4559Classification class1 = Stanag4559Classification.fromSpecName(classification1);
        Stanag4559Classification class2 = Stanag4559Classification.fromSpecName(classification2);
        Stanag4559ClassificationComparator classificationComparator = new Stanag4559ClassificationComparator();

        int comparison = classificationComparator.compare(class1, class2);
        if (comparison <= 0) {
            return classification1;
        } else {
            return classification2;
        }
    }

    private static String collectionToString(Collection<String> collection) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item:collection) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(item);
            first = false;
        }
        return sb.toString();
    }
}
