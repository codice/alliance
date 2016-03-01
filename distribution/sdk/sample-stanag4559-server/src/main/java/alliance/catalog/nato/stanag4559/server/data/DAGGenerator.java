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
package alliance.catalog.nato.stanag4559.server.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import alliance.catalog.nato.stanag4559.common.Stanag4559CommonUtils;
import alliance.catalog.nato.stanag4559.common.UCO.AbsTime;
import alliance.catalog.nato.stanag4559.common.UCO.AbsTimeHelper;
import alliance.catalog.nato.stanag4559.common.UCO.DAG;
import alliance.catalog.nato.stanag4559.common.UCO.Date;
import alliance.catalog.nato.stanag4559.common.UCO.Edge;
import alliance.catalog.nato.stanag4559.common.UCO.Node;
import alliance.catalog.nato.stanag4559.common.UCO.NodeType;
import alliance.catalog.nato.stanag4559.common.UCO.Time;
import org.jgrapht.Graph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.CrossComponentIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import alliance.catalog.nato.stanag4559.common.Stanag4559Constants;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryDecompressionTech;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryType;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoEncodingScheme;

public class DAGGenerator {

    /*  Left in as a reference
        List<String> part_nodes = Arrays.asList("NSIL_COMMON",
                "NSIL_COVERAGE",
                "NSIL_CXP",
                "NSIL_EXPLOITATION_INFO",
                "NSIL_GMTI",
                "NSIL_IMAGERY",
                "NSIL_MESSAGE",
                "NSIL_REPORT",
                "NSIL_RFI",
                "NSIL_SDS",
                "NSIL_TASK",
                "NSIL_TDL",
                "NSIL_VIDEO");
    */

    public static final String ORGANIZATION = "alliance";

    public static final String IMAGERY = "IMAGERY";

    // Attribute Values
    public static final String XMPP = "XMPP";

    public static final String UNCLASSIFIED = "UNCLASSIFIED";

    public static final String NATO = "NATO";

    public static final String EU = "EU";

    public static final String NATO_EU = NATO + "/" + EU;

    public static final String PRODUCT_JPG_URL = "http://localhost:20002/data/product.jpg";

    public static final String IDENTIFIER_VALUE = "alliance-123";

    public static final String SOURCE_LIST = "AAF,MXF";

    public static int getResultHits() {
        return 1;
    }

    public static DAG[] generateDAGResultNSILAllView(ORB orb) {

        DAG[] metacards = new DAG[1];

        DAG metacard = new DAG();

        Graph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node[] nodeRefs = constructNSILProduct(orb, graph, 1);
        constructNSILPart(nodeRefs[0], nodeRefs[1], nodeRefs[3], orb, graph, IMAGERY);
        constructNSILAssociation(nodeRefs[0], nodeRefs[2], orb, graph, 1);

        setUCOEdgeIds(graph);
        setUCOEdges(nodeRefs[0], graph);

        metacard.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);
        metacard.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        metacards[0] = metacard;

        return metacards;
    }

    /**
     * Set the UCO.Node IDs in DFS order to conform to the STANAG 4459 spec.  The root of the node
     * will be 0.
     *
     * @param graph - the graph representation of the DAG
     */
    private static void setUCOEdgeIds(Graph<Node, Edge> graph) {
        int id = 0;
        CrossComponentIterator<Node, Edge, Boolean> depthFirstIterator = new DepthFirstIterator(
                graph);
        while (depthFirstIterator.hasNext()) {
            Node node = depthFirstIterator.next();
            node.id = id;
            id++;
        }
    }

    /**
     * Set the UCO.Edges of the DAG according to the STANAG 4459 Spec.  This requires the ids
     * of the Nodes to be set in DFS order.
     *
     * @param root  - the root node of the graph (NSIL_PRODUCT)
     * @param graph - the graph representation of the DAG
     */
    private static void setUCOEdges(Node root, Graph<Node, Edge> graph) {
        Stack<Node> stack = new Stack<>();
        Stack<Node> visitorStack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Node currNode = stack.pop();
            if (!visitorStack.contains(currNode)) {
                visitorStack.push(currNode);
                for (Edge edge : graph.edgesOf(currNode)) {

                    Node source = graph.getEdgeSource(edge);
                    Node target = graph.getEdgeTarget(edge);

                    // Remove if statement?
                    if (edge != null && source != null && target != null) {
                        edge.start_node = source.id;
                        edge.end_node = target.id;
                        stack.push(target);
                    }
                }
            }
        }
    }

    /**
     * Constructs the NSIL_PRODUCT subgraph of the NSIL_ALL_VIEW.  This method sets builds the NSIL_PRODUCT
     * with all optional nodes ( NSIL_APPROVAL, etc.) as well as all MANDATORY attributes for these NODES
     * according to the STANAG 4459 spec.
     *
     * @param orb            - a reference to the orb to create UCO objects
     * @param graph          - the graph representation of the DAG
     * @param numRelatedFile - the number of NSIL_RELATED_FILE's to create.  This number is unbounded
     *                       according to the specification.
     * @return a Node[] that contains a reference to the root, NSIL_SECURITY, and NSIL_CARD that are used
     * in other subgraphs.
     */
    private static Node[] constructNSILProduct(ORB orb, Graph<Node, Edge> graph,
            int numRelatedFile) {
        List<String> product_nodes = Arrays.asList(Stanag4559Constants.NSIL_APPROVAL,
                Stanag4559Constants.NSIL_FILE,
                Stanag4559Constants.NSIL_STREAM,
                Stanag4559Constants.NSIL_METADATA_SECURITY,
                Stanag4559Constants.NSIL_CARD,
                Stanag4559Constants.NSIL_SECURITY);
        List<Node> nodeProductNodes = getEntityListFromStringList(product_nodes, orb);

        Node[] nodeArray = new Node[4];

        Node root = constructRootNode(orb);
        nodeArray[0] = root;
        graph.addVertex(root);
        Node attribute;

        for (Node node : nodeProductNodes) {
            graph.addVertex(node);
            graph.addEdge(root, node);

            if (node.attribute_name.equals(Stanag4559Constants.NSIL_SECURITY)) {
                nodeArray[1] = node;
            } else if (node.attribute_name.equals(Stanag4559Constants.NSIL_CARD)) {
                nodeArray[2] = node;
            }

            switch (node.attribute_name) {
            case Stanag4559Constants.NSIL_FILE:
                attribute = constructAttributeNode(Stanag4559Constants.CREATOR, ORGANIZATION, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.DATE_TIME_DECLARED,
                        new AbsTime(new Date((short) 2, (short) 10, (short) 16),
                                new Time((short) 10, (short) 0, (short) 0)),
                        orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.PRODUCT_URL, PRODUCT_JPG_URL, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                break;

            case Stanag4559Constants.NSIL_METADATA_SECURITY:
                attribute = constructAttributeNode(Stanag4559Constants.CLASSIFICATION, UNCLASSIFIED, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.POLICY, NATO_EU, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.RELEASABILITY, NATO, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                break;

            case Stanag4559Constants.NSIL_SECURITY:
                attribute = constructAttributeNode(Stanag4559Constants.CLASSIFICATION, UNCLASSIFIED, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.POLICY, NATO_EU, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.RELEASABILITY, NATO, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                break;

            case Stanag4559Constants.NSIL_STREAM:
                attribute = constructAttributeNode(Stanag4559Constants.CREATOR, ORGANIZATION, orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                attribute = constructAttributeNode(Stanag4559Constants.DATE_TIME_DECLARED,
                        new AbsTime(new Date((short) 2, (short) 10, (short) 16),
                                new Time((short) 10, (short) 0, (short) 0)),
                        orb);
                graph.addVertex(attribute);
                graph.addEdge(node, attribute);
                break;
            }

        }

        for (int i = 0; i < numRelatedFile; i++) {
            Node node = constructEntityNode(Stanag4559Constants.NSIL_RELATED_FILE, orb);
            graph.addVertex(node);
            graph.addEdge(root, node);

            attribute = constructAttributeNode(Stanag4559Constants.CREATOR, ORGANIZATION, orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.DATE_TIME_DECLARED, new AbsTime(new Date((short) 2,
                    (short) 10,
                    (short) 16), new Time((short) 10, (short) 0, (short) 0)), orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
        }

        Node node = constructEntityNode(Stanag4559Constants.NSIL_COMMON, orb);
        graph.addVertex(node);
        attribute = constructAttributeNode(Stanag4559Constants.IDENTIFIER_UUID, new UUID(0, 100).toString(), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(Stanag4559Constants.TYPE, IMAGERY, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(Stanag4559Constants.IDENTIFIER_MISSION, IDENTIFIER_VALUE, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(Stanag4559Constants.SOURCE, SOURCE_LIST, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(Stanag4559Constants.TARGET_NUMBER, IDENTIFIER_VALUE, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);

        nodeArray[3] = node;


        return nodeArray;
    }

    /**
     * Constructs a NSIL_PART with all optional nodes, and all mandatory attributes of those nodes according to the STANAG 4459 spec.
     * A NISL_PRODUCT in NSIL_ALL_VIEW can contain 0...n NSIL_PARTS.  A NISL_PART will have an edge pointing to NSIL_SECURITY and NSIL_COMMON
     *
     * @param nsilProduct  - a reference to the root node to link to the DAG graph
     * @param nsilSecurity - a reference to NSIL_SECURITY to link to the NSIL_PART subgraph
     * @param orb          - a reference to the orb to create UCO objects
     * @param graph        - the graph representation of the DAG
     */
    private static void constructNSILPart(Node nsilProduct, Node nsilSecurity, Node nsilCommon, ORB orb,
            Graph<Node, Edge> graph, String partType) {

        Node root = constructEntityNode(Stanag4559Constants.NSIL_PART, orb);
        graph.addVertex(root);
        graph.addEdge(nsilProduct, root);
        graph.addEdge(root, nsilSecurity);
        graph.addEdge(root, nsilCommon);

        Node attribute;
        attribute = constructAttributeNode(Stanag4559Constants.PART_IDENTIFIER, "", orb);
        graph.addVertex(attribute);
        graph.addEdge(root, attribute);

        Node node = constructEntityNode(partType, orb);
        graph.addVertex(node);
        graph.addEdge(root, node);

        switch (partType) {

        case Stanag4559Constants.NSIL_GMTI:
            attribute = constructAttributeNode(Stanag4559Constants.IDENTIFIER_JOB, 123.1, orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.NUMBER_OF_TARGET_REPORTS, 1, orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            break;

        case Stanag4559Constants.NSIL_IMAGERY:
            attribute = constructAttributeNode(Stanag4559Constants.CATEGORY, Stanag4559ImageryType.VIS.toString(), orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.DECOMPRESSION_TECHNIQUE,
                    Stanag4559ImageryDecompressionTech.NC.toString(), orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.IDENTIFIER, ORGANIZATION + "1", orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.NUMBER_OF_BANDS, 1, orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            break;

        case Stanag4559Constants.NSIL_MESSAGE:
            attribute = constructAttributeNode(Stanag4559Constants.RECIPIENT, ORGANIZATION + "2", orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.MESSAGE_BODY, "This is a message", orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.MESSAGE_TYPE, XMPP, orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            break;

        case Stanag4559Constants.NSIL_VIDEO:
            attribute = constructAttributeNode(Stanag4559Constants.CATEGORY, Stanag4559ImageryType.IR.toString(), orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            attribute = constructAttributeNode(Stanag4559Constants.ENCODING_SCHEME,
                    Stanag4559VideoEncodingScheme.V264ON2.getSpecName(), orb);
            graph.addVertex(attribute);
            graph.addEdge(node, attribute);
            break;
        }
    }

    /**
     * Constructs a NSIL_ASSOCIATION subgraph with all optional nodes, as well as all mandatory attributes
     * for these nodes.  A NSIL_PRODUCT can contain 0...n NSIL_ASSOCIATIONS.  All NSIL_ASSOCIATIONS contain
     * an edge to the NSIL_CARD node.  A NSIL_ASSOCIATION can contain 1...n NSIL_DESTINATIONS.
     *
     * @param nsilProduct     - a reference to the root node to link to the DAG graph
     * @param nsilCard        - a reference to the NSIL_CARD to link to the NSIL_ASSOCIATION subgraph
     * @param orb             - a reference to the orb to create UCO objects
     * @param graph           - the graph representation of the DAG
     * @param numDestinations - the number of NSIL_DESTINATION nodes to create
     */
    private static void constructNSILAssociation(Node nsilProduct, Node nsilCard, ORB orb,
            Graph<Node, Edge> graph, int numDestinations) {

        List<String> association_nodes = Arrays.asList(Stanag4559Constants.NSIL_RELATION, Stanag4559Constants.NSIL_SOURCE);
        List<Node> nodePartNodes = getEntityListFromStringList(association_nodes, orb);

        Node root = constructEntityNode(Stanag4559Constants.NSIL_ASSOCIATION, orb);
        graph.addVertex(root);
        graph.addEdge(nsilProduct, root);
        graph.addEdge(root, nsilCard);

        for (Node n : nodePartNodes) {
            graph.addVertex(n);
        }

        graph.addEdge(root, nodePartNodes.get(1));

        graph.addEdge(nodePartNodes.get(1), nsilCard);

        for (int i = 0; i < numDestinations; i++) {
            Node nsilDestination = constructEntityNode(Stanag4559Constants.NSIL_DESTINATION, orb);
            graph.addVertex(nsilDestination);
            graph.addEdge(root, nsilDestination);
            graph.addEdge(nsilDestination, nsilCard);
        }
    }

    private static List<Node> getEntityListFromStringList(List<String> list, ORB orb) {
        List<Node> nodeList = new ArrayList<>();
        for (String string : list) {
            nodeList.add(constructEntityNode(string, orb));
        }
        return nodeList;
    }

    /*
        Construction methods use 0 as the node identifier and are set later according to
        the graph structure.
     */
    private static Node constructRootNode(ORB orb) {
        return new Node(0, NodeType.ROOT_NODE, Stanag4559Constants.NSIL_PRODUCT, orb.create_any());
    }

    private static Node constructEntityNode(String entityName, ORB orb) {
        return new Node(0, NodeType.ENTITY_NODE, entityName, orb.create_any());
    }

    private static Node constructAttributeNode(String attributeName, Object attributeValues,
            ORB orb) {
        Any any = orb.create_any();

        if (attributeValues.getClass().getCanonicalName().equals(String.class.getCanonicalName())) {
            any.insert_string((String) attributeValues);
        }
        else if (attributeValues.getClass().getCanonicalName().equals(Integer.class.getCanonicalName())) {
            any.insert_ulong((int) attributeValues);
        }
        else if (attributeValues.getClass().getCanonicalName().equals(Double.class.getCanonicalName())) {
            any.insert_double((double) attributeValues);
        }
        else if (attributeValues.getClass().getCanonicalName().equals(AbsTime.class.getCanonicalName())) {
            AbsTimeHelper.insert(any, (AbsTime) attributeValues);
        }
        return new Node(0, NodeType.ATTRIBUTE_NODE, attributeName, any);
    }
}
