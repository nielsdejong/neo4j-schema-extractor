package me.niels.schemagenerator;

import java.util.AbstractMap;
import me.niels.values.schemagenerator.distribution.DistributionFitter;
import me.niels.schemagenerator.schema.EdgeType;
import me.niels.schemagenerator.schema.NodeType;
import me.niels.schemagenerator.graph.Edge;
import me.niels.schemagenerator.graph.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import me.niels.values.schemagenerator.distribution.Distribution;
import me.niels.schemagenerator.graph.Graph;
import me.niels.schemagenerator.graph.Property;
import me.niels.schemagenerator.schema.PropertyType;
import me.niels.schemagenerator.schema.Schema;
import me.niels.schemagenerator.values.enumerated.EnumDistribution;

public class SchemaGenerator {
    public static int ENUM_COUNT_LIMIT = 256;
    
    // Neo4J Querying service.
    public DataLoader dataLoader;
    // Graph Model
    public Graph graph;
    // Schema Model
    public Schema schema;

    public static void main(String[] args) {
        String dbURL = "bolt://neo4j:1234@localhost";
        SchemaGenerator gen = new SchemaGenerator(dbURL);
        Long startTime = System.currentTimeMillis();
        gen.generateSchema();
        System.out.println("Schema generated. Total execution time: " + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println(gen.schema);
    }

    private SchemaGenerator(String dbURL) {
        dataLoader = new DataLoader(new GraphQueryer(dbURL));
        graph = new Graph();
        schema = new Schema();
    }

    private void generateSchema() {
        // Load basic graph information from Neo4J database.
        dataLoader.loadNodeData(graph.nodes, schema.nodeTypes);
        dataLoader.loadEdgeData(graph.edges, schema.edgeTypes);
        dataLoader.loadStructureData(graph.nodes, graph.edges);
        schema.totalNodeCount = graph.nodes.size();
        schema.totalEdgeCount = graph.edges.size();

        // Count the number of in/outgoing edges for each node type.
        countEdgeDistributions();

        // Use these counts to determine what the distribution of edges is.
        computeEdgeDistributions();

        // Find property types for each node type.
        findPropertyTypes();

        // Count the number of values for each property.
        countPropertyDistributions();

        // Compute distributions based on these counts.
        computePropertyDistributions();

        // Determine Property Value classes
        findPropertyValueClasses();

        // Determine Property Value distributions
        computePropertyValueSchemas();
    }

    public void countEdgeDistributions() {
        for (Node n : graph.nodes.values()) {

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

    private void computeEdgeDistributions() {
        for (String label : schema.nodeTypes.keySet()) {
            NodeType type = schema.nodeTypes.get(label);
            for (Entry<EdgeType, NodeType> key : type.outDistributionCounter.keySet()) {
                // Find a distribution based on the number of outgoing edges of
                // each node of this type.
                List<Integer> values = type.outDistributionCounter.get(key);
                Distribution distribution = DistributionFitter.fitIntegerList(values);
                schema.nodeTypes.get(label).outDistributions.put(key, distribution);
            }
            for (Entry<NodeType, EdgeType> key : type.inDistributionCounter.keySet()) {
                // Find a distribution based on the number of outgoing edges of
                // each node of this type.
                List<Integer> values = type.inDistributionCounter.get(key);
                Distribution distribution = DistributionFitter.fitIntegerList(values);
                schema.nodeTypes.get(label).inDistributions.put(key, distribution);
            }
        }
    }

    private void findPropertyTypes() {
        for (Node n : graph.nodes.values()) {
            NodeType type = schema.nodeTypes.get(n.type.label);
            for (Property p : n.properties) {
                if (!type.properties.containsKey(p.name)) {
                    type.properties.put(p.name, new PropertyType(p.name));
                }
                type.properties.get(p.name).values.add(p.values);
            }
        }
        for (Edge e : graph.edges.values()) {
            EdgeType type = schema.edgeTypes.get(e.type.label);
            for (Property p : e.properties) {
                if (!type.properties.containsKey(p.name)) {
                    type.properties.put(p.name, new PropertyType(p.name));
                }
                type.properties.get(p.name).values.add(p.values);
            }
        }
    }

    private void countPropertyDistributions() {
        for (Node n : graph.nodes.values()) {
            for (PropertyType pType : n.type.properties.values()) {
                countPropertyDistribution(pType, n.properties);
            }
        }
        for (Edge e : graph.edges.values()) {
            for (PropertyType pType : e.type.properties.values()) {
                countPropertyDistribution(pType, e.properties);
            }
        }
    }

    private void countPropertyDistribution(PropertyType pType, List<Property> properties) {
        int count = 0;
        for (Property p : properties) {
            if (p.name.equals(pType.name)) {
                if (p.values.getClass().equals(Set.class)) {
                    count = ((Set) p.values).size();
                } else if (p.values.getClass().equals(List.class)) {
                    count = ((List) p.values).size();
                } else {
                    count = 1;
                }
                break;
            }
        }
        pType.distributionCounter.add(count);
    }

    private void computePropertyDistributions() {
        for (NodeType n : schema.nodeTypes.values()) {
            for (PropertyType pType : n.properties.values()) {
                pType.amountDistribution = DistributionFitter.fitIntegerList(pType.distributionCounter);
            }
        }
        for (EdgeType e : schema.edgeTypes.values()) {
            for (PropertyType pType : e.properties.values()) {
                pType.amountDistribution = DistributionFitter.fitIntegerList(pType.distributionCounter);
            }
        }
    }

    private void findPropertyValueClasses() {
        for (NodeType n : schema.nodeTypes.values()) {
            for (PropertyType pType : n.properties.values()) {
                findPropertyValueClass(pType);
            }
        }
        for (EdgeType e : schema.edgeTypes.values()) {
            for (PropertyType pType : e.properties.values()) {
                findPropertyValueClass(pType);
            }
        }
    }

    private void findPropertyValueClass(PropertyType pType) {
        String className = null;
        for (Object pValue : pType.values) {
            if (className == null) {
                className = pValue.getClass().getSimpleName();
            } else if (!pValue.getClass().getSimpleName().equals(className)) {
                // If we find two values with different types, we set the class to String.
                className = "String";
                break;
            }
        }
        pType.className = className;
    }

    private void computePropertyValueSchemas() {
        for (NodeType n : schema.nodeTypes.values()) {
            for (PropertyType pType : n.properties.values()) {
                computePropertyValueSchema(pType);
            }
        }
        for (EdgeType e : schema.edgeTypes.values()) {
            for (PropertyType pType : e.properties.values()) {
                computePropertyValueSchema(pType);
            }
        }
    }

    private void computePropertyValueSchema(PropertyType pType) {
        if ("Long".equals(pType.className) || "Short".equals(pType.className) || "Integer".equals(pType.className)) {
            // We are dealing with numbers.
            List<Double> values = new ArrayList<>(pType.values.size());
            for (Object value : pType.values) {
                values.add(((Long) value).doubleValue());
            }
            pType.valueSchema = DistributionFitter.fit(values);
        } else {
            // We are dealing with non-numeric values.
            List<String> values = new ArrayList<>(pType.values.size());
            Map<String, Double> stringValueCounter = new HashMap<>();
            for (Object value : pType.values) {
                values.add((value.toString()));
            }
            for (String value : values) {
                if(stringValueCounter.containsKey(value)){
                    stringValueCounter.put(value, stringValueCounter.get(value)+1.0/values.size());
                }else{
                    stringValueCounter.put(value, 1.0/values.size());
                }
            }
            if(stringValueCounter.size() < ENUM_COUNT_LIMIT){
                // We consider this property an ENUM.
                pType.valueSchema = new EnumDistribution(stringValueCounter);
            }else{
                // We consider this property a regex.
                return;
            }
        }
    }
}
