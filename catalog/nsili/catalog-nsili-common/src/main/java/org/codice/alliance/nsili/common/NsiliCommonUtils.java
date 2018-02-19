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
package org.codice.alliance.nsili.common;

import java.util.ArrayDeque;
import java.util.Deque;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;

public class NsiliCommonUtils {

  private NsiliCommonUtils() {}

  public static Node[] getNodeArrayFromGraph(Graph<Node, Edge> graph) {
    if (graph != null) {
      Object[] vertexSet = graph.vertexSet().toArray();

      Node[] result = new Node[vertexSet.length];

      for (int i = 0; i < vertexSet.length; i++) {
        result[i] = (Node) vertexSet[i];
      }

      return result;
    } else {
      return null;
    }
  }

  public static Edge[] getEdgeArrayFromGraph(Graph<Node, Edge> graph) {
    if (graph != null) {
      Object[] edgeSet = graph.edgeSet().toArray();

      Edge[] result = new Edge[edgeSet.length];

      for (int i = 0; i < edgeSet.length; i++) {
        result[i] = (Edge) edgeSet[i];
        result[i].relationship_type = "";
      }
      return result;
    } else {
      return null;
    }
  }

  /**
   * Set the UCO.Node IDs in DFS order to conform to the NSILI spec. The root of the node will be 0.
   *
   * @param graph - the graph representation of the DAG
   */
  public static void setUCOEdgeIds(Graph<Node, Edge> graph) {
    if (graph != null) {
      int id = 0;
      DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
      while (depthFirstIterator.hasNext()) {
        Node node = depthFirstIterator.next();
        node.id = id;
        id++;
      }
    }
  }

  /**
   * Set the UCO.Edges of the DAG according to the NSILI Spec. This requires the ids of the Nodes to
   * be set in DFS order.
   *
   * @param root - the root node of the graph (NSIL_PRODUCT)
   * @param graph - the graph representation of the DAG
   */
  public static void setUCOEdges(Node root, Graph<Node, Edge> graph) {
    if (graph != null) {
      Deque<Node> stack = new ArrayDeque<>();
      Deque<Node> visitorStack = new ArrayDeque<>();
      stack.push(root);

      while (!stack.isEmpty()) {
        Node currNode = stack.pop();
        if (!visitorStack.contains(currNode)) {
          visitorStack.push(currNode);
          for (Edge edge : graph.edgesOf(currNode)) {
            processEdges(graph, stack, edge);
          }
        }
      }
    }
  }

  private static void processEdges(Graph<Node, Edge> graph, Deque<Node> stack, Edge edge) {
    Node source = graph.getEdgeSource(edge);
    Node target = graph.getEdgeTarget(edge);

    if (edge != null && source != null && target != null) {
      edge.start_node = source.id;
      edge.end_node = target.id;
      stack.push(target);
    }
  }
}
