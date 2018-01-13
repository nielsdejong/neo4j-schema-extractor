/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.niels.schemagenerator.graph.Edge;
import me.niels.schemagenerator.schema.EdgeType;
import me.niels.schemagenerator.graph.Node;
import me.niels.schemagenerator.schema.NodeType;
import me.niels.schemagenerator.graph.Property;

/**
 *
 * @author Niels
 */
public class DataLoader {
    private GraphQueryer queryer;
    
    public DataLoader(GraphQueryer queryer){
        this.queryer = queryer;
    }
    /**
     * Loads node information from Neo$J database, including labels and
     * properties. Additionally, records the occurrence of each label.
     */
    public void loadNodeData(Map<Long, Node> nodes, Map<String, NodeType> nodeTypes) {
        // Query to get all nodes in the database.
        Iterator<Map<String, Object>> neoNodes = queryer.getNodes().iterator();

        // Iterate over query results and store node information.
        while (neoNodes.hasNext()) {
            Object[] data = neoNodes.next().values().toArray();

            // Node identifier
            long id = (long) ((List) data[0]).get(0);

            // Node label
            if(((List) data[0]).size() == 1){
                continue;
            }
            String label = (String) ((List) data[0]).get(1);
            if (!nodeTypes.keySet().contains(label)) {
                nodeTypes.put(label, new NodeType(label));
            } else {
                nodeTypes.get(label).nodeCount++;
            }
            // List of node properties
            Map<String, Object> propertyMap = (Map<String, Object>) data[1];

            // Create node object and store in our nodes list.
            Node n = new Node(id, nodeTypes.get(label), convertToPropertyList(propertyMap));
            nodes.put(n.id, n);
        }
    }

    /**
     * Loads edge information from Neo$J database, including types and
     * properties. Additionally, records the occurrence of each type of edge.
     */
    public void loadEdgeData(Map<Long, Edge> edges, Map<String, EdgeType> edgeTypes) {
        // Query to get all edges (with properties) in the database.
        Iterator<Map<String, Object>> neoEdges = queryer.getEdges().iterator();

        // Iterate over query results and store node information.
        while (neoEdges.hasNext()) {
            Object[] data = neoEdges.next().values().toArray();
            long id = Long.parseLong(
                    ((String) data[0]).split(" ")[0]);

            // Edge label (type)
            String type = ((String) data[0]).split(" ")[1];
            if (!edgeTypes.keySet().contains(type)) {
                edgeTypes.put(type, new EdgeType(type));
            } else {
                edgeTypes.get(type).edgeCount++;
            }

            // List of edge properties
            Map<String, Object> propertyMap = (Map<String, Object>) data[1];

            // Create node object and store in our nodes list.
            Edge e = new Edge(id, edgeTypes.get(type), convertToPropertyList(propertyMap));
            edges.put(e.id, e);
        }
    }

    public void loadStructureData(Map<Long, Node> nodes, Map <Long, Edge> edges) {
        Iterator<Map<String, Object>> neoStructure = queryer.getStructure().iterator();
        
        while (neoStructure.hasNext()) {
            Object[] data = neoStructure.next().values().toArray();
            long sourceID = (long) data[0];
            long edgeID = Long.parseLong(((String)data[1]).split(" ")[0]);
            long destID = Long.parseLong(((String)data[1]).split(" ")[1]);
            
            // Save the connections in our model
            nodes.get(sourceID).outEdges.add(edges.get(edgeID));
            nodes.get(destID).inEdges.add(edges.get(edgeID));
            edges.get(edgeID).origin = nodes.get(sourceID);
            edges.get(edgeID).destination = nodes.get(destID);
            
        }
    }

    /**
     * Converts a Map<String, Object> to a list of properties.
     *
     * @param map the map to be converted.
     * @return This map represented as a list of property objects.
     */
    private List<Property> convertToPropertyList(Map<String, Object> map) {
        List<Property> list = new ArrayList<>();

        for (String key : map.keySet()) {
            list.add(new Property(key, map.get(key)));
        }
        return list;
    }
}
