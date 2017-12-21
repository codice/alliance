/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.mockserver.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.codice.alliance.nsili.common.NsiliCommonUtils;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliImageryDecompressionTech;
import org.codice.alliance.nsili.common.NsiliImageryType;
import org.codice.alliance.nsili.common.NsiliVideoEncodingScheme;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.Coordinate2d;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Date;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.codice.alliance.nsili.common.UCO.Time;
import org.jgrapht.Graph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

public class DAGGenerator {

  private static final Map<String, String> partMap = getPartMap();

  public static final String ORGANIZATION = "Codice Foundation";

  public static final String XMPP = "XMPP";

  public static final String UNCLASSIFIED = "UNCLASSIFIED";

  public static final String NATO = "NATO";

  public static final String EU = "EU";

  public static final String NATO_EU = NATO + "/" + EU;

  public static final String PRODUCT_JPG_URL = "http://localhost:20002/data/product.jpg";

  public static final String IDENTIFIER_VALUE = "alliance-123";

  public static final String SOURCE_LIST = "AAF,MXF";

  public static final String PUBLISHER = "Mock Server";

  public static final String SOURCE_LIBRARY = "Mock Library";

  private static final int RESULT_DAGS_TO_GENERATE = partMap.size();

  // private static final Rectangle RECTANGLE = new Rectangle(new Coordinate2d(-6.753, 11.9764),
  //        new Coordinate2d(9.3383, 21.2157));

  private static final AbsTime TIME =
      new AbsTime(
          new Date((short) 2012, (short) 10, (short) 16),
          new Time((short) 10, (short) 0, (short) 0));

  public static int getResultHits() {
    return RESULT_DAGS_TO_GENERATE;
  }

  public static DAG[] generateDAGResultNSILAllView(ORB orb) {

    DAG[] metacards = new DAG[RESULT_DAGS_TO_GENERATE + 2];

    for (int i = 0; i < RESULT_DAGS_TO_GENERATE; i++) {
      DAG metacard =
          generateNSILDAG(
              orb,
              NsiliConstants.NSIL_IMAGERY,
              partMap.get(NsiliConstants.NSIL_IMAGERY),
              "CSD Sample Data " + i);
      metacards[i] = metacard;
    }

    metacards[RESULT_DAGS_TO_GENERATE] =
        generateNSILDAG(
            orb,
            NsiliConstants.NSIL_GMTI,
            partMap.get(NsiliConstants.NSIL_GMTI),
            "CSD Sample GMTI Data ");

    metacards[RESULT_DAGS_TO_GENERATE + 1] =
        generateNSILDAG(
            orb,
            NsiliConstants.NSIL_VIDEO,
            partMap.get(NsiliConstants.NSIL_VIDEO),
            "CSD Sample Video Data ");

    return metacards;
  }

  private static DAG generateNSILDAG(ORB orb, String partType, String commonType, String title) {
    DAG metacard = new DAG();
    Graph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);
    Node[] nodeRefs = constructNSILProduct(orb, graph, 1, commonType);
    constructNSILPart(nodeRefs[0], nodeRefs[1], nodeRefs[3], orb, graph, partType, title);
    constructNSILPart(
        nodeRefs[0], nodeRefs[1], nodeRefs[3], orb, graph, NsiliConstants.NSIL_COVERAGE, title);
    constructNSILPart(
        nodeRefs[0],
        nodeRefs[1],
        nodeRefs[3],
        orb,
        graph,
        NsiliConstants.NSIL_EXPLOITATION_INFO,
        title);
    constructNSILAssociation(nodeRefs[0], nodeRefs[2], orb, graph, 1);
    NsiliCommonUtils.setUCOEdgeIds(graph);
    NsiliCommonUtils.setUCOEdges(nodeRefs[0], graph);
    metacard.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);
    metacard.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
    return metacard;
  }

  /**
   * Constructs the NSIL_PRODUCT subgraph of the NSIL_ALL_VIEW. This method sets builds the
   * NSIL_PRODUCT with all optional nodes ( NSIL_APPROVAL, etc.) as well as all MANDATORY attributes
   * for these NODES according to the STANAG 4459 spec.
   *
   * @param orb - a reference to the orb to create UCO objects
   * @param graph - the graph representation of the DAG
   * @param numRelatedFile - the number of NSIL_RELATED_FILE's to create. This number is unbounded
   *     according to the specification.
   * @return a Node[] that contains a reference to the root, NSIL_SECURITY, and NSIL_CARD that are
   *     used in other subgraphs.
   */
  private static Node[] constructNSILProduct(
      ORB orb, Graph<Node, Edge> graph, int numRelatedFile, String commonType) {
    List<String> product_nodes =
        Arrays.asList(
            NsiliConstants.NSIL_APPROVAL,
            NsiliConstants.NSIL_FILE,
            NsiliConstants.NSIL_STREAM,
            NsiliConstants.NSIL_METADATA_SECURITY,
            NsiliConstants.NSIL_CARD,
            NsiliConstants.NSIL_SECURITY);
    List<Node> nodeProductNodes = getEntityListFromStringList(product_nodes, orb);

    Node[] nodeArray = new Node[4];

    Node root = constructRootNode(orb);
    nodeArray[0] = root;
    graph.addVertex(root);
    Node attribute;

    for (Node node : nodeProductNodes) {
      graph.addVertex(node);
      graph.addEdge(root, node);

      if (node.attribute_name.equals(NsiliConstants.NSIL_SECURITY)) {
        nodeArray[1] = node;
      } else if (node.attribute_name.equals(NsiliConstants.NSIL_CARD)) {
        nodeArray[2] = node;
      }

      switch (node.attribute_name) {
        case NsiliConstants.NSIL_FILE:
          attribute = constructAttributeNode(NsiliConstants.CREATOR, ORGANIZATION, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.DATE_TIME_DECLARED, TIME, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.PRODUCT_URL, PRODUCT_JPG_URL, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          break;

        case NsiliConstants.NSIL_METADATA_SECURITY:
          attribute = constructAttributeNode(NsiliConstants.CLASSIFICATION, UNCLASSIFIED, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.POLICY, NATO_EU, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.RELEASABILITY, NATO, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          break;

        case NsiliConstants.NSIL_SECURITY:
          attribute = constructAttributeNode(NsiliConstants.CLASSIFICATION, UNCLASSIFIED, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.POLICY, NATO_EU, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.RELEASABILITY, NATO, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          break;

        case NsiliConstants.NSIL_STREAM:
          attribute = constructAttributeNode(NsiliConstants.CREATOR, ORGANIZATION, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          attribute = constructAttributeNode(NsiliConstants.DATE_TIME_DECLARED, TIME, orb);
          graph.addVertex(attribute);
          graph.addEdge(node, attribute);
          break;

        case NsiliConstants.NSIL_CARD:
          addCardAttributes(node, graph, orb);
          break;

        default:
          // no action
          break;
      }
    }

    for (int i = 0; i < numRelatedFile; i++) {
      Node node = constructEntityNode(NsiliConstants.NSIL_RELATED_FILE, orb);
      graph.addVertex(node);
      graph.addEdge(root, node);

      attribute = constructAttributeNode(NsiliConstants.CREATOR, ORGANIZATION, orb);
      graph.addVertex(attribute);
      graph.addEdge(node, attribute);
      attribute = constructAttributeNode(NsiliConstants.DATE_TIME_DECLARED, TIME, orb);
      graph.addVertex(attribute);
      graph.addEdge(node, attribute);
    }

    Node node = constructEntityNode(NsiliConstants.NSIL_COMMON, orb);
    graph.addVertex(node);
    attribute =
        constructAttributeNode(NsiliConstants.IDENTIFIER_UUID, UUID.randomUUID().toString(), orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.TYPE, commonType, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.IDENTIFIER_MISSION, IDENTIFIER_VALUE, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.SOURCE, SOURCE_LIST, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.TARGET_NUMBER, IDENTIFIER_VALUE, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);

    nodeArray[3] = node;

    return nodeArray;
  }

  private static void addCardAttributes(Node node, Graph<Node, Edge> graph, ORB orb) {
    Node attribute;
    attribute =
        constructAttributeNode(NsiliConstants.IDENTIFIER_UUID, UUID.randomUUID().toString(), orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.DATE_TIME_MODIFIED, TIME, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.SOURCE_DATE_TIME_MODIFIED, TIME, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.PUBLISHER, PUBLISHER, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
    attribute = constructAttributeNode(NsiliConstants.SOURCE_LIBRARY, SOURCE_LIBRARY, orb);
    graph.addVertex(attribute);
    graph.addEdge(node, attribute);
  }

  /**
   * Constructs a NSIL_PART with all optional nodes, and all mandatory attributes of those nodes
   * according to the STANAG 4459 spec. A NISL_PRODUCT in NSIL_ALL_VIEW can contain 0...n
   * NSIL_PARTS. A NISL_PART will have an edge pointing to NSIL_SECURITY and NSIL_COMMON
   *
   * @param nsilProduct - a reference to the root node to link to the DAG graph
   * @param nsilSecurity - a reference to NSIL_SECURITY to link to the NSIL_PART subgraph
   * @param orb - a reference to the orb to create UCO objects
   * @param graph - the graph representation of the DAG
   */
  private static void constructNSILPart(
      Node nsilProduct,
      Node nsilSecurity,
      Node nsilCommon,
      ORB orb,
      Graph<Node, Edge> graph,
      String partType,
      String title) {

    Node root = constructEntityNode(NsiliConstants.NSIL_PART, orb);
    graph.addVertex(root);
    graph.addEdge(nsilProduct, root);
    graph.addEdge(root, nsilSecurity);
    graph.addEdge(root, nsilCommon);

    Node attribute;
    attribute = constructAttributeNode(NsiliConstants.PART_IDENTIFIER, "", orb);
    graph.addVertex(attribute);
    graph.addEdge(root, attribute);

    Node node = constructEntityNode(partType, orb);
    graph.addVertex(node);
    graph.addEdge(root, node);

    switch (partType) {
      case NsiliConstants.NSIL_COVERAGE:
        attribute = constructAttributeNode(NsiliConstants.SPATIAL_COUNTRY_CODE, "USA", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);

        attribute =
            constructAttributeNode(
                NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX, getRandomRectangle(), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);

        attribute = constructAttributeNode(NsiliConstants.TEMPORAL_START, TIME, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.TEMPORAL_END, TIME, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_CXP:
        attribute = constructAttributeNode(NsiliConstants.STATUS, "CURRENT", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_EXPLOITATION_INFO:
        attribute = constructAttributeNode(NsiliConstants.LEVEL, new Short((short) 2), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.AUTO_GENERATED, new Boolean(false), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.SUBJ_QUALITY_CODE, "POOR", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_GMTI:
        attribute = constructAttributeNode(NsiliConstants.IDENTIFIER_JOB, 123.1, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.NUMBER_OF_TARGET_REPORTS, 1, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_IMAGERY:
        attribute =
            constructAttributeNode(NsiliConstants.CATEGORY, NsiliImageryType.VIS.toString(), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute =
            constructAttributeNode(
                NsiliConstants.DECOMPRESSION_TECHNIQUE,
                NsiliImageryDecompressionTech.NC.toString(),
                orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.IDENTIFIER, ORGANIZATION + "1", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.NUMBER_OF_BANDS, 1, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.TITLE, title, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_MESSAGE:
        attribute = constructAttributeNode(NsiliConstants.RECIPIENT, ORGANIZATION + "2", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.MESSAGE_BODY, "This is a message", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.MESSAGE_TYPE, XMPP, orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_REPORT:
        attribute = constructAttributeNode(NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM, "1234", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.PRIORITY, "IMMEDIATE", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.TYPE, "ISRSPOTREP", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_RFI:
        attribute = constructAttributeNode(NsiliConstants.FOR_ACTION, "Haiti", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute =
            constructAttributeNode(NsiliConstants.FOR_INFORMATION, "USA,Canada,Planet Mars", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.SERIAL_NUMBER, "12345", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.STATUS, "APPROVED", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.WORKFLOW_STATUS, "COMPLETED", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_TASK:
        attribute = constructAttributeNode(NsiliConstants.COMMENTS, "This is a comment", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.STATUS, "PLANNED", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_TDL:
        attribute = constructAttributeNode(NsiliConstants.ACTIVITY, new Short((short) 1), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.MESSAGE_NUM, "J2.2", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.PLATFORM, new Short((short) 2), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute = constructAttributeNode(NsiliConstants.TRACK_NUM, "EK627", orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      case NsiliConstants.NSIL_VIDEO:
        attribute =
            constructAttributeNode(NsiliConstants.CATEGORY, NsiliImageryType.IR.toString(), orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        attribute =
            constructAttributeNode(
                NsiliConstants.ENCODING_SCHEME,
                NsiliVideoEncodingScheme.V264ON2.getSpecName(),
                orb);
        graph.addVertex(attribute);
        graph.addEdge(node, attribute);
        break;

      default:
        // no action
        break;
    }
  }

  /**
   * Constructs a NSIL_ASSOCIATION subgraph with all optional nodes, as well as all mandatory
   * attributes for these nodes. A NSIL_PRODUCT can contain 0...n NSIL_ASSOCIATIONS. All
   * NSIL_ASSOCIATIONS contain an edge to the NSIL_CARD node. A NSIL_ASSOCIATION can contain 1...n
   * NSIL_DESTINATIONS.
   *
   * @param nsilProduct - a reference to the root node to link to the DAG graph
   * @param nsilCard - a reference to the NSIL_CARD to link to the NSIL_ASSOCIATION subgraph
   * @param orb - a reference to the orb to create UCO objects
   * @param graph - the graph representation of the DAG
   * @param numDestinations - the number of NSIL_DESTINATION nodes to create
   */
  private static void constructNSILAssociation(
      Node nsilProduct, Node nsilCard, ORB orb, Graph<Node, Edge> graph, int numDestinations) {

    List<String> association_nodes =
        Arrays.asList(NsiliConstants.NSIL_RELATION, NsiliConstants.NSIL_SOURCE);
    List<Node> nodePartNodes = getEntityListFromStringList(association_nodes, orb);

    Node root = constructEntityNode(NsiliConstants.NSIL_ASSOCIATION, orb);
    graph.addVertex(root);
    graph.addEdge(nsilProduct, root);

    for (Node n : nodePartNodes) {
      graph.addVertex(n);
    }

    graph.addEdge(root, nodePartNodes.get(1));

    for (int i = 0; i < numDestinations; i++) {
      Node nsilDestination = constructEntityNode(NsiliConstants.NSIL_DESTINATION, orb);
      graph.addVertex(nsilDestination);
      graph.addEdge(root, nsilDestination);

      Node card = constructEntityNode(NsiliConstants.NSIL_CARD, orb);
      graph.addVertex(card);
      graph.addEdge(nsilDestination, card);
      addCardAttributes(card, graph, orb);
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
    return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
  }

  private static Node constructEntityNode(String entityName, ORB orb) {
    return new Node(0, NodeType.ENTITY_NODE, entityName, orb.create_any());
  }

  private static Node constructAttributeNode(
      String attributeName, Object attributeValues, ORB orb) {
    Any any = orb.create_any();

    if (attributeValues.getClass().getCanonicalName().equals(String.class.getCanonicalName())) {
      any.insert_string((String) attributeValues);
    } else if (attributeValues
        .getClass()
        .getCanonicalName()
        .equals(Integer.class.getCanonicalName())) {
      any.insert_ulong((int) attributeValues);
    } else if (attributeValues
        .getClass()
        .getCanonicalName()
        .equals(Double.class.getCanonicalName())) {
      any.insert_double((double) attributeValues);
    } else if (attributeValues
        .getClass()
        .getCanonicalName()
        .equals(AbsTime.class.getCanonicalName())) {
      AbsTimeHelper.insert(any, (AbsTime) attributeValues);
    } else if (attributeValues
        .getClass()
        .getCanonicalName()
        .equals(Rectangle.class.getCanonicalName())) {
      RectangleHelper.insert(any, (Rectangle) attributeValues);
    } else if (attributeValues
        .getClass()
        .getCanonicalName()
        .equals(Boolean.class.getCanonicalName())) {
      any.insert_boolean((Boolean) attributeValues);
    } else if (attributeValues
        .getClass()
        .getCanonicalName()
        .equals(Short.class.getCanonicalName())) {
      any.insert_short((Short) attributeValues);
    }
    return new Node(0, NodeType.ATTRIBUTE_NODE, attributeName, any);
  }

  private static Map<String, String> getPartMap() {
    Map<String, String> map = new HashMap<>();
    map.put(NsiliConstants.NSIL_CXP, "COLLECTION/EXPLOITATION PLAN");
    map.put(NsiliConstants.NSIL_GMTI, "GMTI");
    map.put(NsiliConstants.NSIL_IMAGERY, "IMAGERY");
    map.put(NsiliConstants.NSIL_MESSAGE, "MESSAGE");
    map.put(NsiliConstants.NSIL_REPORT, "REPORT");
    map.put(NsiliConstants.NSIL_RFI, "RFI");
    map.put(NsiliConstants.NSIL_TASK, "TASK");
    map.put(NsiliConstants.NSIL_TDL, "TDL DATA");
    map.put(NsiliConstants.NSIL_VIDEO, "VIDEO");
    return map;
  }

  private static Rectangle getRandomRectangle() {

    int x = getRandomNumber(-75, 75);
    int y = getRandomNumber(-175, 175);

    Rectangle rectangle = new Rectangle(new Coordinate2d(x, y), new Coordinate2d(x + 5, y + 5));

    return rectangle;
  }

  private static int getRandomNumber(int a, int b) {
    return a + (int) ((1 + b - a) * Math.random());
  }
}
