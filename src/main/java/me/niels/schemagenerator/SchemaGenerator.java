package me.niels.schemagenerator;

import java.util.AbstractMap;
import me.niels.values.schemagenerator.distribution.DistributionFitter;
import me.niels.schemagenerator.schema.EdgeType;
import me.niels.schemagenerator.schema.NodeType;
import me.niels.schemagenerator.graph.Edge;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import me.niels.schemagenerator.graph.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import me.geso.regexp_trie.RegexpTrie;
import me.niels.values.schemagenerator.distribution.NumericDistribution;
import me.niels.schemagenerator.graph.Graph;
import me.niels.schemagenerator.graph.Property;
import me.niels.schemagenerator.schema.PropertyType;
import me.niels.schemagenerator.schema.Schema;
import me.niels.schemagenerator.schema.ValueSchema;
import me.niels.schemagenerator.values.enumerated.EnumDistribution;
import me.niels.values.schemagenerator.distribution.GaussianDistribution;
import me.niels.values.schemagenerator.distribution.UniformDistribution;
import me.niels.values.schemagenerator.distribution.ZipfianDistribution;
import me.niels.values.schemagenerator.regex.RegularExpression;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class SchemaGenerator {

    public static final double ENUM_PERCENTAGE_LIMIT = 0.05;
    public static final double RULE_MIN_SUPPORT = 0.05;
    public static final double RULE_MIN_CONFIDENCE = 0.5;
    public static final int RULE_MINING_BINS = 8;
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
        // Determine Property Value Relationships
        computeAssociationRules();
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
                NumericDistribution distribution = DistributionFitter.fitIntegerList(values);
                schema.nodeTypes.get(label).outDistributions.put(key, distribution);
            }
            for (Entry<NodeType, EdgeType> key : type.inDistributionCounter.keySet()) {
                // Find a distribution based on the number of outgoing edges of
                // each node of this type.
                List<Integer> values = type.inDistributionCounter.get(key);
                NumericDistribution distribution = DistributionFitter.fitIntegerList(values);
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

        // Ensure that numbers encoded as strings in the database are still used as numbers.
        boolean number = true;
        if (className.equals("String")) {
            for (int a = 0; a < pType.values.size(); a++) {
                try {
                    Long.valueOf((String) pType.values.get(a));
                } catch (Exception e) {
                    number = false;
                    break;
                }
            }
            if (number == true) {
                className = "Long";
                for (int a = 0; a < pType.values.size(); a++) {
                    pType.values.set(a, Long.valueOf((String) pType.values.get(a)));
                }
                pType.className = className;
                return;
            }
        }

        // Ensure that numbers encoded as strings in the database are still used as numbers.
        number = true;
        if (className.equals("String")) {
            for (int a = 0; a < pType.values.size(); a++) {
                try {
                    Double.valueOf((String) pType.values.get(a));
                } catch (Exception e) {
                    number = false;
                    break;
                }
            }
            if (number == true) {
                className = "Double";
                for (int a = 0; a < pType.values.size(); a++) {
                    pType.values.set(a, Double.valueOf((String) pType.values.get(a)));
                }
                pType.className = className;
                return;
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
        if ("Long".equals(pType.className) || "Double".equals(pType.className) || "Integer".equals(pType.className)) {
            // We are dealing with numbers.
            List<Double> values = new ArrayList<>(pType.values.size());

            if ("Long".equals(pType.className)) {
                for (Object value : pType.values) {

                    values.add(((Long) value).doubleValue());
                }
            }
            if ("Double".equals(pType.className)) {
                for (Object value : pType.values) {

                    values.add((Double) value);
                }
            }

            pType.valueSchema = DistributionFitter.fit(values);
        } else {
            // We are dealing with non-numeric values.
            List<String> values = new ArrayList<>(pType.values.size());
            Map<String, Double> stringValueCounter = new HashMap<>();
            for (Object value : pType.values) {
                String str = value.toString();
                if (!str.equals("")) {
                    values.add((value.toString()));
                }

            }
            for (String value : values) {
                if (stringValueCounter.containsKey(value)) {
                    stringValueCounter.put(value, stringValueCounter.get(value) + 1.0 / values.size());
                } else {
                    stringValueCounter.put(value, 1.0 / values.size());
                }
            }
            if (stringValueCounter.size() < values.size() * ENUM_PERCENTAGE_LIMIT) {
                // We consider this property an ENUM.

                pType.valueSchema = new EnumDistribution(stringValueCounter);
            } else {
                // We consider this property a regex.
                RegexpTrie trie;
                trie = new RegexpTrie();
                for (String value : values) {
                    trie.add(value);
                }

                pType.valueSchema = new RegularExpression(trie.regexp());
            }
        }
    }

    private void computeAssociationRules() {

        for (NodeType nType : schema.nodeTypes.values()) {
            List<List<String>> training_data = new ArrayList<List<String>>();
            for (Node n : graph.nodes.values()) {
                if (n.type != nType) {
                    continue;
                }
                training_data.add(convertPropertiesToItemSet(n.type.label, true, n.properties));
            }
            AssociationRuleGenerator.generate(training_data, RULE_MIN_SUPPORT, RULE_MIN_CONFIDENCE);
        }
        for (EdgeType eType : schema.edgeTypes.values()) {
            List<List<String>> training_data = new ArrayList<List<String>>();
            for (Edge e : graph.edges.values()) {
                if (e.type != eType) {
                    continue;
                }
                training_data.add(convertPropertiesToItemSet(e.type.label, false, e.properties));
            }
            AssociationRuleGenerator.generate(training_data, RULE_MIN_SUPPORT, RULE_MIN_CONFIDENCE);
        }
    }

    private List<String> convertPropertiesToItemSet(String label, boolean isNode, List<Property> properties) {
        List<String> entry = new ArrayList<>();
        for (Property p : properties) {
         
            ValueSchema pValueSchema;
            if (isNode) {
                pValueSchema = schema.nodeTypes.get(label).properties.get(p.name).valueSchema;
            } else {
                pValueSchema = schema.edgeTypes.get(label).properties.get(p.name).valueSchema;
            }

            if (pValueSchema.getClass() == UniformDistribution.class || pValueSchema.getClass() == ZipfianDistribution.class || pValueSchema.getClass() == GaussianDistribution.class) {
                // Numeric Distribution
                NumericDistribution distribution = (NumericDistribution) pValueSchema;
                List<Double> bucketLimits = new ArrayList<>();
                if (distribution.getClass().equals(UniformDistribution.class)) {
                    bucketLimits = ((UniformDistribution) distribution).getBins(RULE_MINING_BINS);
                } else if (distribution.getClass().equals(GaussianDistribution.class)) {
                    bucketLimits = ((GaussianDistribution) distribution).getBins(RULE_MINING_BINS);
                } else {
                    bucketLimits = ((ZipfianDistribution) distribution).getBins(RULE_MINING_BINS, graph.nodes.size());
                }
                double value;
                if (p.values.getClass() == String.class) {
                    value = Double.parseDouble((String) p.values);
                } else if (p.values.getClass() == Long.class) {
                    value = (Long) p.values;
                } else {
                    value = (Double) p.values;
                }

                for (int i = 1; i < bucketLimits.size(); i++) {
                    if (value < bucketLimits.get(i)) {
                        entry.add(label + "." + p.name + "=" + bucketLimits.get(i - 1) + "-" + bucketLimits.get(i));
                        break;
                    }
                }

            } else if (pValueSchema.getClass() == EnumDistribution.class) {
                // Enum

                if (((EnumDistribution) pValueSchema).values.size() == 1) {
                    continue;
                }
                entry.add(label + "." + p.name + "=" + p.values);
            } else {
                // Regex
            }
        }
        return entry;
    }
}
