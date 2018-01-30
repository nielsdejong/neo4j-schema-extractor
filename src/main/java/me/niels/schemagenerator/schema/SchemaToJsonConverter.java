/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;

/**
 *
 * @author Niels
 */
public class SchemaToJsonConverter {

    public static String convert(Schema schema) {
        JSONObject schemaJSON = new JSONObject();
        try {
            // add nodes to JSON
            JSONArray nodesJSON = new JSONArray();
            for (String name : schema.nodeTypes.keySet()) {
                
                JSONObject node = new JSONObject();
                node.put("name", name);
                node.put("fraction", schema.nodeTypes.get(name).nodeCount / (double) schema.totalNodeCount);
                JSONObject outDistributions = new JSONObject();
                for (Map.Entry<EdgeType, NodeType> outEdge : schema.nodeTypes.get(name).outDistributions.keySet()) {
                    outDistributions.put("[" + name + "-" + outEdge.getKey() + "->" + outEdge.getValue() + "]", schema.nodeTypes.get(name).outDistributions.get(outEdge).toString());
                }
                node.put("outdistributions", outDistributions);
                JSONObject inDistributions = new JSONObject();
                for (Map.Entry<NodeType, EdgeType> inEdge : schema.nodeTypes.get(name).inDistributions.keySet()) {
                    inDistributions.put("[" + inEdge.getKey() + "-" + inEdge.getValue() + "->" + name + "]", schema.nodeTypes.get(name).inDistributions.get(inEdge).toString());
                }
                node.put("indistributions", inDistributions);
                JSONArray properties = new JSONArray();
                for (String propertyName : schema.nodeTypes.get(name).properties.keySet()) {
                    JSONObject property = new JSONObject();
                    property.put("name", propertyName);
                    property.put("dist", schema.nodeTypes.get(name).properties.get(propertyName).amountDistribution.toString());
                    property.put("class", schema.nodeTypes.get(name).properties.get(propertyName).className.toString());
                    property.put("valuedist", schema.nodeTypes.get(name).properties.get(propertyName).valueSchema.toString());
                    properties.put(property);
                }
                node.put("properties", properties);
                nodesJSON.put(node);
            }
            
            // Add edges to json
            JSONArray edgesJSON = new JSONArray();
            for (String name : schema.edgeTypes.keySet()) {
                JSONObject edge = new JSONObject();
                edge.put("name", name);
                edge.put("fraction", schema.edgeTypes.get(name).edgeCount / (double) schema.totalEdgeCount);
               
                JSONArray properties = new JSONArray();
                for (String propertyName : schema.edgeTypes.get(name).properties.keySet()) {
                    JSONObject property = new JSONObject();
                    property.put("name", propertyName);
                    property.put("dist", schema.edgeTypes.get(name).properties.get(propertyName).amountDistribution.toString());
                    property.put("class", schema.edgeTypes.get(name).properties.get(propertyName).className.toString());
                    property.put("valuedist", schema.edgeTypes.get(name).properties.get(propertyName).valueSchema.toString());
                    properties.put(property);
                }
                edge.put("properties", properties);
                edgesJSON.put(edge);
            }
            
            JSONArray rulesJSON = new JSONArray();
            for (String rule : schema.rules) {
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
