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
package org.codice.alliance.nsili.transformer;

import java.io.IOException;
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
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.nsili.common.NsiliApprovalStatus;
import org.codice.alliance.nsili.common.NsiliClassification;
import org.codice.alliance.nsili.common.NsiliClassificationComparator;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliCxpMetacardType;
import org.codice.alliance.nsili.common.NsiliCxpStatusType;
import org.codice.alliance.nsili.common.NsiliExploitationSubQualCode;
import org.codice.alliance.nsili.common.NsiliGmtiMetacardType;
import org.codice.alliance.nsili.common.NsiliIRMetacardType;
import org.codice.alliance.nsili.common.NsiliImageryDecompressionTech;
import org.codice.alliance.nsili.common.NsiliImageryMetacardType;
import org.codice.alliance.nsili.common.NsiliImageryType;
import org.codice.alliance.nsili.common.NsiliMessageMetacardType;
import org.codice.alliance.nsili.common.NsiliMetacardType;
import org.codice.alliance.nsili.common.NsiliMetadataEncodingScheme;
import org.codice.alliance.nsili.common.NsiliProductType;
import org.codice.alliance.nsili.common.NsiliReportMetacardType;
import org.codice.alliance.nsili.common.NsiliReportPriority;
import org.codice.alliance.nsili.common.NsiliReportType;
import org.codice.alliance.nsili.common.NsiliRfiMetacardType;
import org.codice.alliance.nsili.common.NsiliRfiStatus;
import org.codice.alliance.nsili.common.NsiliRfiWorkflowStatus;
import org.codice.alliance.nsili.common.NsiliSdsOpStatus;
import org.codice.alliance.nsili.common.NsiliSecurity;
import org.codice.alliance.nsili.common.NsiliTaskMetacardType;
import org.codice.alliance.nsili.common.NsiliTaskStatus;
import org.codice.alliance.nsili.common.NsiliTdlMetacardType;
import org.codice.alliance.nsili.common.NsiliVideoCategoryType;
import org.codice.alliance.nsili.common.NsiliVideoEncodingScheme;
import org.codice.alliance.nsili.common.NsiliVideoMetacardType;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.resource.ResourceReader;

public class DAGConverter {

    private static final long MEGABYTE = 1024L * 1024L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DAGConverter.class);

    private static final WKTWriter WKT_WRITER = new WKTWriter();

    private static String sourceId;

    private ResourceReader resourceReader;

    private String relatedFileType;

    private String relatedFileUrl;

    public DAGConverter(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    public MetacardImpl convertDAG(DAG dag, boolean swapCoordinates, String logSourceId) {
        MetacardImpl metacard = null;
        sourceId = logSourceId;

        //Need to have at least 2 nodes and an edge for anything useful
        if (dag.nodes != null && dag.edges != null) {
            Map<Integer, Node> nodeMap = ResultDAGConverter.createNodeMap(dag.nodes);
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

            metacard = parseGraph(graph, swapCoordinates);
            metacard.setSourceId(sourceId);
        }

        return metacard;
    }

    private MetacardImpl parseGraph(DirectedAcyclicGraph<Node, Edge> graph,
            boolean swapCoordinates) {
        MetacardImpl metacard = new MetacardImpl();

        NsiliSecurity security = new NsiliSecurity();
        List<Serializable> associatedCards = new ArrayList<>();

        //Traverse the graph
        DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
        Node parentEntity = null;
        Node assocNode = null;

        while (depthFirstIterator.hasNext()) {
            Node node = depthFirstIterator.next();

            if (node.node_type == NodeType.ROOT_NODE
                    && node.attribute_name.equals(NsiliConstants.NSIL_PRODUCT)) {
                //Nothing to process from root node
            } else if (node.node_type == NodeType.ENTITY_NODE) {
                parentEntity = node;
                if (node.attribute_name.equals(NsiliConstants.NSIL_ASSOCIATION)) {
                    assocNode = node;
                } else if (node.attribute_name.equals(NsiliConstants.NSIL_RELATED_FILE)) {
                    relatedFileType = "";
                    relatedFileUrl = "";
                } else {
                    if (assocNode != null && !isNodeChildOfStart(graph, assocNode, node)) {
                        assocNode = null;
                    }

                    //Handle Marker nodes
                    if (node.attribute_name.equals(NsiliConstants.NSIL_IR)) {
                        addNsilIRAttribute(metacard);
                    }
                }
            } else if (node.node_type == NodeType.RECORD_NODE) {
                //Nothing to process from record node
            } else if (parentEntity != null &&
                    node.node_type == NodeType.ATTRIBUTE_NODE &&
                    node.value != null) {
                switch (parentEntity.attribute_name) {
                case NsiliConstants.NSIL_APPROVAL:
                    addNsilApprovalAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_CARD:
                    if (assocNode != null) {
                        addNsilAssociation(associatedCards, node);
                    } else {
                        addNsilCardAttribute(metacard, node);
                    }
                    break;
                case NsiliConstants.NSIL_SECURITY:
                    addNsilSecurityAttribute(security, node);
                    break;
                case NsiliConstants.NSIL_METADATA_SECURITY:
                    addNsilSecurityAttribute(security, node);
                    break;
                case NsiliConstants.NSIL_COMMON:
                    addNsilCommonAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_COVERAGE:
                    addNsilCoverageAttribute(metacard, node, swapCoordinates);
                    break;
                case NsiliConstants.NSIL_CXP:
                    addNsilCxpAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_EXPLOITATION_INFO:
                    addNsilExploitationInfoAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_FILE:
                    addNsilFileAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_GMTI:
                    addNsilGmtiAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_IMAGERY:
                    addNsilImageryAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_MESSAGE:
                    addNsilMessageAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_REPORT:
                    addNsilReportAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_RFI:
                    addNsilRfiAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_SDS:
                    addNsilSdsAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_STREAM:
                    addNsilStreamAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_TASK:
                    addNsilTaskAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_TDL:
                    addNsilTdlAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_VIDEO:
                    addNsilVideoAttribute(metacard, node);
                    break;
                case NsiliConstants.NSIL_RELATED_FILE:
                    addNsilRelatedFile(metacard, node);
                default:
                    break;
                }
            }
        }

        addMergedSecurityDescriptor(metacard, security);
        setTopLevelMetacardAttributes(metacard);

        //Add associated data
        if (!associatedCards.isEmpty()) {
            boolean firstAssoc = true;
            AttributeImpl attribute = null;
            for (Serializable association : associatedCards) {
                if (firstAssoc) {
                    attribute = new AttributeImpl(NsiliMetacardType.ASSOCIATIONS, association);
                    metacard.setAttribute(attribute);
                    firstAssoc = false;
                } else {
                    attribute.addValue(association);
                }
            }

        }

        return metacard;
    }

    /**
     * Determines if the end node is a child of the start node.
     *
     * @param graph - The complete graph.
     * @param start - Starting node.
     * @param end   - Child node to check
     * @return true if the end node is a child of the start node in the provided graph.
     */
    private static boolean isNodeChildOfStart(DirectedAcyclicGraph<Node, Edge> graph, Node start,
            Node end) {
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

    private void addNsilApprovalAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.APPROVED_BY:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.APPROVAL_BY,
                    getString(node.value)));
            break;
        case NsiliConstants.DATE_TIME_MODIFIED:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.APPROVAL_DATETIME_MODIFIED,
                    convertDate(node.value)));
            break;
        case NsiliConstants.STATUS:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.APPROVAL_STATUS,
                    convertApprovalStatus(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilCardAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.IDENTIFIER:
            metacard.setId(getString(node.value));
            break;
        case NsiliConstants.SOURCE_DATE_TIME_MODIFIED:
            Date cardDate = convertDate(node.value);
            metacard.setCreatedDate(cardDate);
            break;
        case NsiliConstants.DATE_TIME_MODIFIED:
            metacard.setModifiedDate(convertDate(node.value));
            break;
        case NsiliConstants.PUBLISHER:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.PUBLISHER,
                    getString(node.value)));
            break;
        case NsiliConstants.SOURCE_LIBRARY:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SOURCE_LIBRARY,
                    getString(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilCommonAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.DESCRIPTION_ABSTRACT:
            metacard.setDescription(getString(node.value));
            break;
        case NsiliConstants.IDENTIFIER_MISSION:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.IDENTIFIER_MISSION,
                    getString(node.value)));
            break;
        case NsiliConstants.IDENTIFIER_UUID:
            metacard.setId(getString(node.value));
            break;
        case NsiliConstants.IDENTIFIER_JC3IEDM:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.ID_JC3IEDM,
                    getLong(node.value)));
            break;
        case NsiliConstants.LANGUAGE:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.LANGUAGE,
                    getString(node.value)));
            break;
        case NsiliConstants.SOURCE:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SOURCE,
                    getString(node.value)));
            break;
        case NsiliConstants.SUBJECT_CATEGORY_TARGET:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SUBJECT_CATEGORY_TARGET,
                    getString(node.value)));
            break;
        case NsiliConstants.TARGET_NUMBER:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.TARGET_NUMBER,
                    getString(node.value)));
            break;
        case NsiliConstants.TYPE:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.PRODUCT_TYPE,
                    convertProductType(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilCoverageAttribute(MetacardImpl metacard, Node node,
            boolean swapCoordinates) {
        switch (node.attribute_name) {
        case NsiliConstants.SPATIAL_COUNTRY_CODE:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.COUNTRY_CODE,
                    getString(node.value)));
            break;
        case NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX:
            metacard.setLocation(convertShape(node.value, swapCoordinates));
            break;
        case NsiliConstants.TEMPORAL_START:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.START_DATETIME,
                    convertDate(node.value)));
            break;
        case NsiliConstants.TEMPORAL_END:
            Date temporalEnd = convertDate(node.value);
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.END_DATETIME, temporalEnd));
            metacard.setEffectiveDate(temporalEnd);
            break;
        default:
            break;
        }
    }

    private void addNsilCxpAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliCxpMetacardType());
        metacard.setContentTypeName(NsiliProductType.COLLECTION_EXPLOITATION_PLAN.toString());

        switch (node.attribute_name) {
        case NsiliConstants.STATUS:
            metacard.setAttribute(new AttributeImpl(NsiliCxpMetacardType.STATUS,
                    convertCxpStatus(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilIRAttribute(MetacardImpl metacard) {
        metacard.setType(new NsiliIRMetacardType());
    }

    private void addNsilExploitationInfoAttribute(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.DESCRIPTION:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.EXPLOITATION_DESCRIPTION,
                    getString(node.value)));
            break;
        case NsiliConstants.LEVEL:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.EXPLOITATION_LEVEL,
                    getShort(node.value)));
            break;
        case NsiliConstants.AUTO_GENERATED:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.EXPLOITATION_AUTO_GEN,
                    node.value.extract_boolean()));
            break;
        case NsiliConstants.SUBJ_QUALITY_CODE:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.EXPLOITATION_SUBJ_QUAL_CODE,
                    convertExplSubQualCd(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilFileAttribute(MetacardImpl metacard, Node node) {

        switch (node.attribute_name) {
        case NsiliConstants.ARCHIVED:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.FILE_ARCHIVED,
                    node.value.extract_boolean()));
            break;
        case NsiliConstants.ARCHIVE_INFORMATION:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.FILE_ARCHIVED_INFO,
                    getString(node.value)));
            break;
        case NsiliConstants.CREATOR:
            metacard.setPointOfContact(getString(node.value));
            break;
        case NsiliConstants.DATE_TIME_DECLARED:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.PRODUCT_CREATE_TIME,
                    convertDate(node.value)));
            break;
        case NsiliConstants.EXTENT:
            metacard.setResourceSize(String.valueOf(convertMegabytesToBytes(node.value.extract_double())));
            break;
        case NsiliConstants.FORMAT:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.FILE_FORMAT,
                    getString(node.value)));
            break;
        case NsiliConstants.FORMAT_VERSION:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.FILE_FORMAT_VER,
                    getString(node.value)));
            break;
        case NsiliConstants.PRODUCT_URL:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.FILE_URL,
                    getString(node.value)));
            break;
        case NsiliConstants.TITLE:
            metacard.setTitle(getString(node.value));
            break;
        default:
            break;
        }
    }

    private void addNsilGmtiAttribute(MetacardImpl metacard, Node node) {
        //If any GMTI node is added, then we will set the MetacardType
        metacard.setType(new NsiliGmtiMetacardType());
        metacard.setContentTypeName(NsiliProductType.GMTI.toString());

        switch (node.attribute_name) {
        case NsiliConstants.IDENTIFIER_JOB:
            metacard.setAttribute(new AttributeImpl(NsiliGmtiMetacardType.JOB_ID,
                    node.value.extract_double()));
            break;
        case NsiliConstants.NUMBER_OF_TARGET_REPORTS:
            metacard.setAttribute(new AttributeImpl(NsiliGmtiMetacardType.NUM_TARGET_REPORTS,
                    getLong(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilImageryAttribute(MetacardImpl metacard, Node node) {
        //If any Imagery attribute is added, set the card type
        metacard.setType(new NsiliImageryMetacardType());
        metacard.setContentTypeName(NsiliProductType.IMAGERY.toString());

        switch (node.attribute_name) {
        case NsiliConstants.CATEGORY:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.IMAGERY_CATEGORY,
                    convertImageCategory(node.value)));
            break;
        case NsiliConstants.CLOUD_COVER_PCT:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.CLOUD_COVER_PCT,
                    getShort(node.value)));
            break;
        case NsiliConstants.COMMENTS:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.IMAGERY_COMMENTS,
                    getString(node.value)));
            break;
        case NsiliConstants.DECOMPRESSION_TECHNIQUE:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.DECOMPRESSION_TECHNIQUE,
                    convertDecompressionTechnique(node.value)));
            break;
        case NsiliConstants.IDENTIFIER:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.IMAGE_ID,
                    getString(node.value)));
            break;
        case NsiliConstants.NIIRS:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.NIIRS,
                    getShort(node.value)));
            break;
        case NsiliConstants.NUMBER_OF_BANDS:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.NUM_BANDS,
                    getLong(node.value)));
            break;
        case NsiliConstants.NUMBER_OF_ROWS:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.NUM_ROWS,
                    getLong(node.value)));
            break;
        case NsiliConstants.NUMBER_OF_COLS:
            metacard.setAttribute(new AttributeImpl(NsiliImageryMetacardType.NUM_COLS,
                    getLong(node.value)));
            break;
        case NsiliConstants.TITLE:
            metacard.setTitle(getString(node.value));
            break;
        default:
            break;
        }
    }

    private void addNsilMessageAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliMessageMetacardType());
        metacard.setContentTypeName(NsiliProductType.MESSAGE.toString());

        switch (node.attribute_name) {
        case NsiliConstants.RECIPIENT:
            metacard.setAttribute(new AttributeImpl(NsiliMessageMetacardType.RECIPIENT,
                    getString(node.value)));
            break;
        case NsiliConstants.SUBJECT:
            metacard.setAttribute(new AttributeImpl(NsiliMessageMetacardType.MESSAGE_SUBJECT,
                    getString(node.value)));
            break;
        case NsiliConstants.MESSAGE_BODY:
            metacard.setAttribute(new AttributeImpl(NsiliMessageMetacardType.MESSAGE_BODY,
                    getString(node.value)));
            break;
        case NsiliConstants.MESSAGE_TYPE:
            metacard.setAttribute(new AttributeImpl(NsiliMessageMetacardType.MESSAGE_TYPE,
                    getString(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilReportAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliReportMetacardType());
        metacard.setContentTypeName(NsiliProductType.REPORT.toString());
        switch (node.attribute_name) {
        case NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM:
            metacard.setAttribute(new AttributeImpl(NsiliReportMetacardType.ORIGINATOR_REQ_SERIAL_NUM,
                    getString(node.value)));
            break;
        case NsiliConstants.PRIORITY:
            metacard.setAttribute(new AttributeImpl(NsiliReportMetacardType.PRIORITY,
                    convertReportPriority(node.value)));
            break;
        case NsiliConstants.TYPE:
            metacard.setAttribute(new AttributeImpl(NsiliReportMetacardType.TYPE,
                    convertReportType(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilRfiAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliRfiMetacardType());
        metacard.setContentTypeName(NsiliProductType.RFI.toString());

        switch (node.attribute_name) {
        case NsiliConstants.FOR_ACTION:
            metacard.setAttribute(new AttributeImpl(NsiliRfiMetacardType.FOR_ACTION,
                    getString(node.value)));
            break;
        case NsiliConstants.FOR_INFORMATION:
            metacard.setAttribute(new AttributeImpl(NsiliRfiMetacardType.FOR_INFORMATION,
                    getString(node.value)));
            break;
        case NsiliConstants.SERIAL_NUMBER:
            metacard.setAttribute(new AttributeImpl(NsiliRfiMetacardType.SERIAL_NUMBER,
                    getString(node.value)));
            break;
        case NsiliConstants.STATUS:
            metacard.setAttribute(new AttributeImpl(NsiliRfiMetacardType.STATUS,
                    convertRfiStatus(node.value)));
            break;
        case NsiliConstants.WORKFLOW_STATUS:
            metacard.setAttribute(new AttributeImpl(NsiliRfiMetacardType.WORKFLOW_STATUS,
                    convertRfiWorkflowStatus(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilSdsAttribute(MetacardImpl metacard, Node node) {
        metacard.setContentTypeName(NsiliProductType.SYSTEM_DEPLOYMENT_STATUS.toString());

        switch (node.attribute_name) {
        case NsiliConstants.OPERATIONAL_STATUS:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SDS_OPERATIONAL_STATUS,
                    convertSdsOpStatus(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilSecurityAttribute(NsiliSecurity security, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.POLICY:
            String mergedPolicy = mergeSecurityPolicyString(security.getPolicy(),
                    getString(node.value));
            security.setPolicy(mergedPolicy);
            break;
        case NsiliConstants.RELEASABILITY:
            String mergedReleasability = mergeReleasabilityString(security.getReleasability(),
                    getString(node.value));
            security.setReleasability(mergedReleasability);
            break;
        case NsiliConstants.CLASSIFICATION:
            String classification = mergeClassificationString(security.getClassification(),
                    getString(node.value));
            security.setClassification(classification);
            break;
        default:
            break;
        }
    }

    private void addNsilStreamAttribute(MetacardImpl metacard, Node node) {

        switch (node.attribute_name) {
        case NsiliConstants.ARCHIVED:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_ARCHIVED,
                    node.value.extract_boolean()));
            break;
        case NsiliConstants.ARCHIVE_INFORMATION:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_ARCHIVAL_INFO,
                    getString(node.value)));
            break;
        case NsiliConstants.CREATOR:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_CREATOR,
                    getString(node.value)));
            break;
        case NsiliConstants.DATE_TIME_DECLARED:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_DATETIME_DECLARED,
                    convertDate(node.value)));
            break;
        case NsiliConstants.STANDARD:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_STANDARD,
                    getString(node.value)));
            break;
        case NsiliConstants.STANDARD_VERSION:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_STANDARD_VER,
                    getString(node.value)));
            break;
        case NsiliConstants.SOURCE_URL:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_SOURCE_URL,
                    getString(node.value)));
            break;
        case NsiliConstants.PROGRAM_ID:
            metacard.setAttribute(new AttributeImpl(NsiliMetacardType.STREAM_PROGRAM_ID,
                    getShort(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilTaskAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliTaskMetacardType());
        metacard.setContentTypeName(NsiliProductType.TASK.toString());
        switch (node.attribute_name) {
        case NsiliConstants.COMMENTS:
            metacard.setAttribute(new AttributeImpl(NsiliTaskMetacardType.COMMENTS,
                    getString(node.value)));
            break;
        case NsiliConstants.STATUS:
            metacard.setAttribute(new AttributeImpl(NsiliTaskMetacardType.STATUS,
                    convertTaskStatus(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilTdlAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliTdlMetacardType());
        metacard.setContentTypeName(NsiliProductType.TDL_DATA.toString());

        switch (node.attribute_name) {
        case NsiliConstants.ACTIVITY:
            metacard.setAttribute(new AttributeImpl(NsiliTdlMetacardType.ACTIVITY,
                    getShort(node.value)));
            break;

        case NsiliConstants.MESSAGE_NUM:
            metacard.setAttribute(new AttributeImpl(NsiliTdlMetacardType.MESSAGE_NUM,
                    getString(node.value)));
            break;
        case NsiliConstants.PLATFORM:
            metacard.setAttribute(new AttributeImpl(NsiliTdlMetacardType.PLATFORM,
                    getShort(node.value)));
            break;
        case NsiliConstants.TRACK_NUM:
            metacard.setAttribute(new AttributeImpl(NsiliTdlMetacardType.TRACK_NUM,
                    getString(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilVideoAttribute(MetacardImpl metacard, Node node) {
        metacard.setType(new NsiliVideoMetacardType());
        metacard.setContentTypeName(NsiliProductType.VIDEO.toString());

        switch (node.attribute_name) {
        case NsiliConstants.AVG_BIT_RATE:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.AVG_BIT_RATE,
                    node.value.extract_double()));
            break;
        case NsiliConstants.CATEGORY:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.CATEGORY,
                    convertVideoCategory(node.value)));
            break;
        case NsiliConstants.ENCODING_SCHEME:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.ENCODING_SCHEME,
                    convertVideoEncodingScheme(node.value)));
            break;
        case NsiliConstants.FRAME_RATE:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.FRAME_RATE,
                    node.value.extract_double()));
            break;
        case NsiliConstants.NUMBER_OF_ROWS:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.NUM_ROWS,
                    getLong(node.value)));
            break;
        case NsiliConstants.NUMBER_OF_COLS:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.NUM_COLS,
                    getLong(node.value)));
            break;
        case NsiliConstants.METADATA_ENC_SCHEME:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.METADATA_ENCODING_SCHEME,
                    convertMetadataEncScheme(node.value)));
            break;
        case NsiliConstants.MISM_LEVEL:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.MISM_LEVEL,
                    getShort(node.value)));
            break;
        case NsiliConstants.SCANNING_MODE:
            metacard.setAttribute(new AttributeImpl(NsiliVideoMetacardType.SCANNING_MODE,
                    getString(node.value)));
            break;
        default:
            break;
        }
    }

    private void addNsilAssociation(List<Serializable> associations, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.IDENTIFIER:
            associations.add(getString(node.value));
            break;
        default:
            break;
        }
    }

    private String addNsilRelatedFile(MetacardImpl metacard, Node node) {
        switch (node.attribute_name) {
        case NsiliConstants.FILE_TYPE:
            relatedFileType = getString(node.value);
            if (StringUtils.isNotBlank(relatedFileUrl)) {
                if (relatedFileType.equalsIgnoreCase(NsiliConstants.THUMBNAIL_TYPE)) {
                    metacard.setThumbnail(getThumbnail(relatedFileUrl));
                }
            }
            break;
        case NsiliConstants.URL:
            relatedFileUrl = getString(node.value);
            if (relatedFileType.equalsIgnoreCase(NsiliConstants.THUMBNAIL_TYPE)) {
                if (StringUtils.isNotBlank(relatedFileUrl)) {
                    metacard.setThumbnail(getThumbnail(relatedFileUrl));
                }
            }
            break;
        default:
            break;
        }

        return relatedFileType;
    }

    private void addMergedSecurityDescriptor(MetacardImpl metacard, NsiliSecurity security) {
        metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SECURITY_CLASSIFICATION,
                security.getClassification()));
        metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SECURITY_POLICY,
                security.getPolicy()));
        metacard.setAttribute(new AttributeImpl(NsiliMetacardType.SECURITY_RELEASABILITY,
                security.getReleasability()));
    }

    private void setTopLevelMetacardAttributes(MetacardImpl metacard) {
        //If file data available use that
        Attribute fileProductURLAttr = metacard.getAttribute(NsiliMetacardType.FILE_URL);
        if (fileProductURLAttr != null) {
            metacard.setResourceURI(convertURI(fileProductURLAttr.getValue()
                    .toString()));
            Attribute fileFormatVerAttr = metacard.getAttribute(NsiliMetacardType.FILE_FORMAT_VER);
            if (fileFormatVerAttr != null) {
                metacard.setContentTypeVersion(fileFormatVerAttr.getValue()
                        .toString());
            }
        } else {
            //Else use stream info
            Attribute streamURLAttr = metacard.getAttribute(NsiliMetacardType.STREAM_SOURCE_URL);
            if (streamURLAttr != null && streamURLAttr.getValue() != null) {
                metacard.setResourceURI(convertURI(streamURLAttr.getValue()
                        .toString()));

                Attribute streamFormatVerAttr =
                        metacard.getAttribute(NsiliMetacardType.STREAM_STANDARD_VER);
                if (streamFormatVerAttr != null) {
                    metacard.setContentTypeVersion(streamFormatVerAttr.getValue()
                            .toString());
                }
            }
        }

        if (NsiliMessageMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName())) {
            Attribute subjAttr = metacard.getAttribute(NsiliMessageMetacardType.MESSAGE_SUBJECT);
            if (subjAttr != null) {
                metacard.setTitle(subjAttr.getValue()
                        .toString());
            }

            Attribute bodyAttr = metacard.getAttribute(NsiliMessageMetacardType.MESSAGE_BODY);
            if (bodyAttr != null) {
                metacard.setDescription(bodyAttr.getValue()
                        .toString());
            }

            Attribute typeAttr = metacard.getAttribute(NsiliMessageMetacardType.MESSAGE_TYPE);
            if (typeAttr != null) {
                //Unset the version when we have a message
                metacard.setContentTypeVersion(null);
            }
        } else if (NsiliVideoMetacardType.class.getCanonicalName()
                .equals(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName())) {
            Attribute encodingSchemeAttr =
                    metacard.getAttribute(NsiliVideoMetacardType.ENCODING_SCHEME);
            if (encodingSchemeAttr != null) {
                //Unset the version as we don't know that here
                metacard.setContentTypeVersion(null);
            }
        }

        // Content Type Name Fall Back
        if (metacard.getContentTypeName() == null) {
            metacard.setContentTypeName(NsiliProductType.DOCUMENT.toString());
        }
    }

    public static Date convertDate(Any any) {
        AbsTime absTime = AbsTimeHelper.extract(any);
        org.codice.alliance.nsili.common.UCO.Date ucoDate = absTime.aDate;
        org.codice.alliance.nsili.common.UCO.Time ucoTime = absTime.aTime;

        DateTime dateTime = new DateTime((int) ucoDate.year,
                (int) ucoDate.month,
                (int) ucoDate.day,
                (int) ucoTime.hour,
                (int) ucoTime.minute,
                (int) ucoTime.second,
                0);
        return dateTime.toDate();
    }

    private NsiliProductType convertProductType(Any any) {
        String productTypeStr = getString(any);
        return NsiliProductType.fromSpecName(productTypeStr);
    }

    private String convertShape(Any any, boolean swapCoordinates) {
        org.codice.alliance.nsili.common.UCO.Rectangle rectangle = RectangleHelper.extract(any);
        org.codice.alliance.nsili.common.UCO.Coordinate2d upperLeft = rectangle.upper_left;
        org.codice.alliance.nsili.common.UCO.Coordinate2d lowerRight = rectangle.lower_right;

        Geometry geom;
        GeometryFactory geometryFactory = new GeometryFactory();

        if (upperLeft.x == lowerRight.x && upperLeft.y == lowerRight.y) {
            //Build a Point vs Polygon
            Coordinate pointCoord;
            if (swapCoordinates) {
                pointCoord = new Coordinate(upperLeft.y, upperLeft.x);
            } else {
                pointCoord = new Coordinate(upperLeft.x, upperLeft.y);
            }
            geom = geometryFactory.createPoint(pointCoord);
        } else {
            Coordinate[] coordinates = new Coordinate[5];
            Coordinate lowerLeftCoord;
            Coordinate upperLeftCoord;
            Coordinate upperRightCoord;
            Coordinate lowerRightCoord;

            if (swapCoordinates) {
                lowerLeftCoord = new Coordinate(upperLeft.y, lowerRight.x);
                upperLeftCoord = new Coordinate(upperLeft.y, upperLeft.x);
                upperRightCoord = new Coordinate(lowerRight.y, upperLeft.x);
                lowerRightCoord = new Coordinate(lowerRight.y, lowerRight.x);
            } else {
                lowerLeftCoord = new Coordinate(upperLeft.x, lowerRight.y);
                upperLeftCoord = new Coordinate(upperLeft.x, upperLeft.y);
                upperRightCoord = new Coordinate(lowerRight.x, upperLeft.y);
                lowerRightCoord = new Coordinate(lowerRight.x, lowerRight.y);
            }

            coordinates[0] = lowerLeftCoord;
            coordinates[1] = upperLeftCoord;
            coordinates[2] = upperRightCoord;
            coordinates[3] = lowerRightCoord;
            coordinates[4] = lowerLeftCoord;

            LinearRing shell = geometryFactory.createLinearRing(coordinates);
            geom = new Polygon(shell, null, geometryFactory);
        }

        return WKT_WRITER.write(geom);
    }

    public static int convertMegabytesToBytes(Double megabytes) {
        int bytes = 0;

        if (megabytes != null) {
            bytes = (int) (megabytes * MEGABYTE);
        }

        return bytes;
    }

    private static URI convertURI(String uriStr) {
        URI uri = null;

        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            LOGGER.warn("Unable to parse URI for metacard: " + uriStr, e);
        }

        return uri;
    }

    private NsiliImageryType convertImageCategory(Any any) {
        String imageryTypeStr = getString(any);
        return NsiliImageryType.valueOf(imageryTypeStr);
    }

    private NsiliImageryDecompressionTech convertDecompressionTechnique(Any any) {
        String decompressionTechStr = getString(any);
        return NsiliImageryDecompressionTech.valueOf(decompressionTechStr);
    }

    private NsiliVideoCategoryType convertVideoCategory(Any any) {
        String videoCategoryType = getString(any);
        return NsiliVideoCategoryType.valueOf(videoCategoryType);
    }

    private NsiliVideoEncodingScheme convertVideoEncodingScheme(Any any) {
        String videoEncSchemeStr = getString(any);
        return NsiliVideoEncodingScheme.fromSpecName(videoEncSchemeStr);
    }

    private NsiliMetadataEncodingScheme convertMetadataEncScheme(Any any) {
        String metadataEncSchemeStr = getString(any);
        return NsiliMetadataEncodingScheme.valueOf(metadataEncSchemeStr);
    }

    private NsiliCxpStatusType convertCxpStatus(Any any) {
        String cxpStatusStr = getString(any);
        return NsiliCxpStatusType.valueOf(cxpStatusStr);
    }

    private NsiliRfiStatus convertRfiStatus(Any any) {
        String rfiStatusStr = getString(any);
        return NsiliRfiStatus.valueOf(rfiStatusStr);
    }

    private NsiliRfiWorkflowStatus convertRfiWorkflowStatus(Any any) {
        String rfiWorkflowStatusStr = getString(any);
        return NsiliRfiWorkflowStatus.valueOf(rfiWorkflowStatusStr);
    }

    private NsiliTaskStatus convertTaskStatus(Any any) {
        String taskStatusStr = getString(any);
        return NsiliTaskStatus.valueOf(taskStatusStr);
    }

    private NsiliExploitationSubQualCode convertExplSubQualCd(Any any) {
        String explSubQualCodeStr = getString(any);
        return NsiliExploitationSubQualCode.valueOf(explSubQualCodeStr);
    }

    private NsiliSdsOpStatus convertSdsOpStatus(Any any) {
        String sdsOpStatusStr = getString(any);
        return NsiliSdsOpStatus.fromSpecName(sdsOpStatusStr);
    }

    private NsiliApprovalStatus convertApprovalStatus(Any any) {
        String approvalStr = getString(any);
        return NsiliApprovalStatus.fromSpecName(approvalStr);
    }

    private NsiliReportPriority convertReportPriority(Any any) {
        String reportPriorityStr = getString(any);
        return NsiliReportPriority.valueOf(reportPriorityStr);
    }

    private NsiliReportType convertReportType(Any any) {
        String reportTypeStr = getString(any);
        return NsiliReportType.valueOf(reportTypeStr);
    }

    public static String getString(Any any) {
        if (any.type()
                .kind() == TCKind.tk_wstring) {
            return any.extract_wstring();
        } else if (any.type()
                .kind() == TCKind.tk_string) {
            return any.extract_string();
        }
        return null;
    }

    public static Integer getLong(Any any) {
        if (any.type()
                .kind() == TCKind.tk_long) {
            return any.extract_long();
        } else if (any.type()
                .kind() == TCKind.tk_ulong) {
            return any.extract_ulong();
        }
        return null;
    }

    public static Short getShort(Any any) {
        if (any.type()
                .kind() == TCKind.tk_short) {
            return any.extract_short();
        } else if (any.type()
                .kind() == TCKind.tk_ushort) {
            return any.extract_ushort();
        }
        return null;
    }

    /**
     * Merge 2 space separated security policies into a single list. Merge is done additively.
     *
     * @param policy1 - Initial list of policies.
     * @param policy2 - Additional policies to add.
     * @return Non-duplicated policies space separated.
     */
    private String mergeSecurityPolicyString(String policy1, String policy2) {
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

        for (String policy : policy1Arr) {
            policyAttrs.add(policy);
        }

        for (String policy : policy2Arr) {
            policyAttrs.add(policy);
        }

        return collectionToString(policyAttrs);
    }

    /**
     * Merges releasabilities. Merge is done subtractively, so most restrictive applies.
     *
     * @param releasability1 - Initial list of releasabilities.
     * @param releasability2 - Releasabilities to merge.
     * @return - Non-duplicated releasabilities space separated.
     */
    private String mergeReleasabilityString(String releasability1, String releasability2) {
        if (releasability1 == null) {
            return releasability2;
        } else if (releasability2 == null) {
            return null;
        }

        Set<String> releasability = new HashSet<>();
        String[] releasebility1Arr = releasability1.split(" ");
        String[] releasability2Arr = releasability2.split(" ");
        for (String release1 : releasebility1Arr) {
            boolean found = false;
            for (String release2 : releasability2Arr) {
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
     *
     * @param classification1 - Initial classification
     * @param classification2 - Classification to check
     * @return Most restrictive classification
     */
    private String mergeClassificationString(String classification1, String classification2) {
        NsiliClassification class1 = NsiliClassification.fromSpecName(classification1);
        NsiliClassification class2 = NsiliClassification.fromSpecName(classification2);
        NsiliClassificationComparator classificationComparator =
                new NsiliClassificationComparator();

        int comparison = classificationComparator.compare(class1, class2);
        if (comparison <= 0) {
            return classification1;
        } else {
            return classification2;
        }
    }

    private static String collectionToString(Collection<String> collection) {
        return String.join(" ", collection);
    }

    public static void logMetacard(Metacard metacard, String id) {
        MetacardType metacardType = metacard.getMetacardType();
        LOGGER.trace("{} : ID : {}", id, metacard.getId());
        LOGGER.trace("{} :  Metacard Type : {}",
                id,
                metacardType.getClass()
                        .getCanonicalName());
        LOGGER.trace("{} :  Title : {}", id, metacard.getTitle());
        if (metacard instanceof MetacardImpl) {
            LOGGER.trace("{} :  Description : {}", id, ((MetacardImpl) metacard).getDescription());
        }
        LOGGER.trace("{} :  Content Type Name : {}", id, metacard.getContentTypeName());
        LOGGER.trace("{} :  Content Type Version : {}", id, metacard.getContentTypeVersion());
        LOGGER.trace("{} :  Created Date : {}", id, metacard.getCreatedDate());
        LOGGER.trace("{} :  Effective Date : {}", id, metacard.getEffectiveDate());
        LOGGER.trace("{} :  Location : {}", id, metacard.getLocation());
        LOGGER.trace("{} :  SourceID : {}", id, metacard.getSourceId());
        LOGGER.trace("{} :  Modified Date : {}", id, metacard.getModifiedDate());
        if (metacard.getResourceURI() != null) {
            LOGGER.trace("{} :  Resource URI : {}",
                    id,
                    metacard.getResourceURI()
                            .toString());
        }

        Set<AttributeDescriptor> descriptors = metacardType.getAttributeDescriptors();
        for (AttributeDescriptor descriptor : descriptors) {
            Attribute attribute = metacard.getAttribute(descriptor.getName());
            if (attribute != null) {
                if (attribute.getValues() != null) {
                    String valueStr = getValueString(attribute.getValues());
                    LOGGER.trace("{} : {} : {}", id, descriptor.getName(), valueStr);
                } else {
                    LOGGER.trace("{} : {} : {}", id, descriptor.getName(), attribute.getValue());
                }
            }
        }
    }

    public static String getValueString(Collection<Serializable> collection) {
        return collection.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private byte[] getThumbnail(String thumbnailUrlStr) {
        byte[] thumbnail = null;

        try {
            URI thumbnailURI = new URI(thumbnailUrlStr);
            ResourceResponse resourceResponse = null;
            try {
                resourceResponse = resourceReader.retrieveResource(thumbnailURI, new HashMap<>());
                thumbnail = resourceResponse.getResource()
                        .getByteArray();
            } catch (ResourceNotSupportedException e) {
                LOGGER.warn("Resource is not supported: {} ", thumbnailURI, e);
            }
        } catch (IOException | ResourceNotFoundException | URISyntaxException e) {
            LOGGER.warn("Unable to get thumbnail from URL {} : {}", thumbnailUrlStr, e);
            LOGGER.debug("Thumbnail retrieval error details", e);
        }

        return thumbnail;
    }

    public static void printDAG(DAG dag) {
        if (dag.nodes != null && dag.edges != null) {
            Map<Integer, Node> nodeMap = ResultDAGConverter.createNodeMap(dag.nodes);
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

            DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
            Node rootNode = null;
            while (depthFirstIterator.hasNext()) {
                depthFirstIterator.setCrossComponentTraversal(false);
                Node node = depthFirstIterator.next();
                if (rootNode == null) {
                    rootNode = node;
                }

                DijkstraShortestPath<Node, Edge> path = new DijkstraShortestPath<Node, Edge>(graph,
                        rootNode,
                        node);

                if (node.node_type == NodeType.ATTRIBUTE_NODE) {
                    printNode(node, (int) Math.round(path.getPathLength()));
                } else {
                    printNode(node, (int) Math.round(path.getPathLength()));
                }
            }
        }
    }

    public static void printNode(Node node, int offset) {
        String attrName = node.attribute_name;
        String value = "NOT PARSED";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < offset; i++) {
            sb.append("\t");
        }

        if (node.node_type == NodeType.ATTRIBUTE_NODE) {
            if (node.value != null && node.value.type() != null) {
                value = getNodeValue(node.value);
                sb.append(attrName);
                sb.append("=");
                sb.append(value);
            }
        } else {
            sb.append(attrName);
        }
        LOGGER.trace(sb.toString());
    }

    public static String getNodeValue(Any any) {
        String value = null;
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
        } else if (any.type()
                .kind() == TCKind.tk_boolean) {
            value = String.valueOf(any.extract_boolean());
        } else if (any.type()
                .kind() == TCKind.tk_double) {
            value = String.valueOf(any.extract_double());
        } else {
            try {
                if (any.type()
                        .name()
                        .equals(AbsTime.class.getSimpleName())) {
                    Date date = convertDate(any);
                    if (date != null) {
                        value = date.toString();
                    }
                }
            } catch (org.omg.CORBA.MARSHAL | IllegalFieldValueException | BadKind ignore) {
            }
        }

        return value;
    }
}
