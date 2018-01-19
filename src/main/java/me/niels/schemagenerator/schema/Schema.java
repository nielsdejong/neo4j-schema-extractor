/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Niels
 */
public class Schema {

    // Index node/edge types by label/type.
    public Map<String, NodeType> nodeTypes;
    public Map<String, EdgeType> edgeTypes;
    public int totalNodeCount = 0;
    public int totalEdgeCount = 0;
    public List<String> rules;
    public Schema() {
        nodeTypes = new HashMap<>();
        edgeTypes = new HashMap<>();
        rules = new ArrayList<>();
    }

    @Override
    public String toString() {
        String schema = "Schema: " + "\n";

        for (String name : nodeTypes.keySet()) {
            schema += "    Node: " + name + "\n";
            schema += "        fraction: " + nodeTypes.get(name).nodeCount / (double) totalNodeCount + "\n";

            schema += "        outdistributions:" + "\n";
            for (Entry<EdgeType, NodeType> outEdge : nodeTypes.get(name).outDistributions.keySet()) {
                schema += "            [" + name + "-" + outEdge.getKey() + "->" + outEdge.getValue() + "]: " + nodeTypes.get(name).outDistributions.get(outEdge) + "\n";
            }
            schema += "        indistributions:" + "\n";
            for (Entry<NodeType, EdgeType> inEdge : nodeTypes.get(name).inDistributions.keySet()) {
                schema += "            [" + inEdge.getKey() + "-" + inEdge.getValue() + "->" + name + "]: " + nodeTypes.get(name).inDistributions.get(inEdge) + "\n";
            }
            schema += "        properties: \n";
            for (String propertyName : nodeTypes.get(name).properties.keySet()) {
                schema += "            " + propertyName + "\n";
                schema += "                dist:" + nodeTypes.get(name).properties.get(propertyName).amountDistribution + "\n";
                schema += "                class:" + nodeTypes.get(name).properties.get(propertyName).className + "\n";
                schema += "                valuedist:" + nodeTypes.get(name).properties.get(propertyName).valueSchema + "\n";
            }
        }
        for (String name : edgeTypes.keySet()) {
            schema += "    Edge: " + name + "\n";
            schema += "        fraction: " + edgeTypes.get(name).edgeCount / (double) totalEdgeCount + "\n";
            schema += "        properties: \n";
            for (String propertyName : edgeTypes.get(name).properties.keySet()) {
                schema += "            " + propertyName + "\n";
                schema += "                dist:" + edgeTypes.get(name).properties.get(propertyName).amountDistribution + "\n";
                schema += "                class:" + edgeTypes.get(name).properties.get(propertyName).className + "\n";
                schema += "                valuedist:" + edgeTypes.get(name).properties.get(propertyName).valueSchema + "\n";
            }
        }
        schema += "    Association Rules: \n";
        for (String rule : rules) {
            schema += "        rule: " + rule + "\n";
        }
        return schema;
    }
}
