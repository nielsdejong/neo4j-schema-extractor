/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.graph;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import me.niels.schemagenerator.schema.EdgeType;
import me.niels.schemagenerator.schema.NodeType;

/**
 *
 * @author Niels
 */
public class Graph {

    // Index the nodes/edges by their ID.
    public Map<Long, Node> nodes;
    public Map<Long, Edge> edges;

    public Graph() {
        nodes = new HashMap<>();
        edges = new HashMap<>();
    }

    public void computeEdgeDistributions() {
        for (Node n : nodes.values()) {

            // Record the amount of outgoing edges of every type for this node.
            Map<Map.Entry<EdgeType, NodeType>, Integer> outEdgeTypeCounts = new HashMap<>();
            for (Edge e : n.outEdges) {
                Map.Entry key = new AbstractMap.SimpleEntry<>(e.type, e.destination.type);

                if (!outEdgeTypeCounts.containsKey(key)) {
                    outEdgeTypeCounts.put(key, 1);
                } else {
                    outEdgeTypeCounts.put(key, outEdgeTypeCounts.get(key) + 1);
                }
            }
            for (Map.Entry key : outEdgeTypeCounts.keySet()) {
                if (!n.type.outDistributionCounter.containsKey(key)) {
                    n.type.outDistributionCounter.put(key, new ArrayList<>());
                }
                n.type.outDistributionCounter.get(key).add(outEdgeTypeCounts.get(key));
            }

            // Record the amount of ingoing edges of every type for this node.
            Map<Map.Entry<NodeType, EdgeType>, Integer> inEdgeTypeCounts = new HashMap<>();
            for (Edge e : n.inEdges) {
                Map.Entry key = new AbstractMap.SimpleEntry<>(e.origin.type, e.type);

                if (!inEdgeTypeCounts.containsKey(key)) {
                    inEdgeTypeCounts.put(key, 1);
                } else {
                    inEdgeTypeCounts.put(key, inEdgeTypeCounts.get(key) + 1);
                }
            }
            for (Map.Entry key : inEdgeTypeCounts.keySet()) {
                if (!n.type.inDistributionCounter.containsKey(key)) {
                    n.type.inDistributionCounter.put(key, new ArrayList<>());
                }
                n.type.inDistributionCounter.get(key).add(inEdgeTypeCounts.get(key));
            }
        }
    }
}
