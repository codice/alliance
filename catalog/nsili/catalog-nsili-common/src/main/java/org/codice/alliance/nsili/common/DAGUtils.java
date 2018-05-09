package org.codice.alliance.nsili.common;

import java.util.Map;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;

public class DAGUtils {

  public static String printDAG(DAG dag) {
    if (dag.nodes == null || dag.edges == null) {
      return null;
    } else {
      StringBuilder sb = new StringBuilder();

      Map<Integer, Node> nodeMap = ResultDAGConverter.createNodeMap(dag.nodes);
      DirectedAcyclicGraph<Node, Edge> graph = getNodeEdgeDirectedAcyclicGraph(dag, nodeMap);

      DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
      Node rootNode = null;
      while (depthFirstIterator.hasNext()) {
        depthFirstIterator.setCrossComponentTraversal(false);
        Node node = depthFirstIterator.next();
        if (rootNode == null) {
          rootNode = node;
        }

        DijkstraShortestPath<Node, Edge> path = new DijkstraShortestPath<>(graph, rootNode, node);

        sb.append(printNode(node, (int) Math.round(path.getPathLength())));
      }

      return sb.toString();
    }
  }

  public static DirectedAcyclicGraph<Node, Edge> getNodeEdgeDirectedAcyclicGraph(
      DAG dag, Map<Integer, Node> nodeMap) {
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
    return graph;
  }

  private static String printNode(Node node, int offset) {
    String attrName = node.attribute_name;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < offset; i++) {
      sb.append("\t");
    }

    if (node.node_type == NodeType.ATTRIBUTE_NODE) {
      if (node.value != null && node.value.type() != null) {
        sb.append(attrName);
        sb.append("=");
        sb.append(CorbaUtils.getNodeValue(node.value));
      }
    } else {
      sb.append(attrName);
    }
    sb.append('\n');
    return sb.toString();
  }
}
