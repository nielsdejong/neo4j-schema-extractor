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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;

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
        JSONObject schemaJSON = new JSONObject();
        try {
            // add nodes to JSON
            JSONArray nodesJSON = new JSONArray();
            for (String name : nodeTypes.keySet()) {
                
                JSONObject node = new JSONObject();
                node.put("name", name);
                node.put("fraction", nodeTypes.get(name).nodeCount / (double) totalNodeCount);
                JSONObject outDistributions = new JSONObject();
                for (Entry<EdgeType, NodeType> outEdge : nodeTypes.get(name).outDistributions.keySet()) {
                    outDistributions.put("[" + name + "-" + outEdge.getKey() + "->" + outEdge.getValue() + "]", nodeTypes.get(name).outDistributions.get(outEdge).toString());
                }
                node.put("outdistributions", outDistributions);
                JSONObject inDistributions = new JSONObject();
                for (Entry<NodeType, EdgeType> inEdge : nodeTypes.get(name).inDistributions.keySet()) {
                    inDistributions.put("[" + inEdge.getKey() + "-" + inEdge.getValue() + "->" + name + "]", nodeTypes.get(name).inDistributions.get(inEdge).toString());
                }
                node.put("indistributions", inDistributions);
                JSONArray properties = new JSONArray();
                for (String propertyName : nodeTypes.get(name).properties.keySet()) {
                    JSONObject property = new JSONObject();
                    property.put("name", propertyName);
                    property.put("dist", nodeTypes.get(name).properties.get(propertyName).amountDistribution.toString());
                    property.put("class", nodeTypes.get(name).properties.get(propertyName).className.toString());
                    property.put("valuedist", nodeTypes.get(name).properties.get(propertyName).valueSchema.toString());
                    properties.put(property);
                }
                node.put("properties", properties);
                nodesJSON.put(node);
            }
            
            // Add edges to json
            JSONArray edgesJSON = new JSONArray();
            for (String name : edgeTypes.keySet()) {
                JSONObject edge = new JSONObject();
                edge.put("name", name);
                edge.put("fraction", edgeTypes.get(name).edgeCount / (double) totalEdgeCount);
               
                JSONArray properties = new JSONArray();
                for (String propertyName : edgeTypes.get(name).properties.keySet()) {
                    JSONObject property = new JSONObject();
                    property.put("name", propertyName);
                    property.put("dist", edgeTypes.get(name).properties.get(propertyName).amountDistribution.toString());
                    property.put("class", edgeTypes.get(name).properties.get(propertyName).className.toString());
                    property.put("valuedist", edgeTypes.get(name).properties.get(propertyName).valueSchema.toString());
                    properties.put(property);
                }
                edge.put("properties", properties);
                edgesJSON.put(edge);
            }
            
            JSONArray rulesJSON = new JSONArray();
            for (String rule : rules) {
                rulesJSON.put(rule);
            }
            schemaJSON.put("nodes", nodesJSON);
            schemaJSON.put("edges", edgesJSON);
            schemaJSON.put("rules", rulesJSON);
            return schemaJSON.toString(4);
        } catch (JSONException ex) {
            Logger.getLogger(Schema.class.getName()).log(Level.SEVERE, null, ex);
            return "{}";
        }
        
        
    }
}
