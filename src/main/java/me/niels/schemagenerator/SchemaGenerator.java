package me.niels.schemagenerator;

import me.niels.distribution.DistributionFitter;
import me.niels.schemagenerator.schema.EdgeType;
import me.niels.schemagenerator.schema.NodeType;
import me.niels.schemagenerator.graph.Edge;
import me.niels.schemagenerator.graph.Node;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.niels.distribution.Distribution;
import me.niels.schemagenerator.graph.Graph;
import me.niels.schemagenerator.schema.Schema;

public class SchemaGenerator {

    // Neo4J Querying service.
    public DataLoader dataLoader;
    public Graph graph;
    public Schema schema;
    public DistributionFitter distributionFitter;

    public static void main(String[] args) {

        String dbURL = "bolt://neo4j:1234@localhost";
        SchemaGenerator gen = new SchemaGenerator(dbURL);

        gen.generateSchema();
        /*
        for (long id : gen.nodes.keySet()) {
            System.out.println(gen.nodes.get(id));
        }
        for (long id : gen.edges.keySet()) {
            System.out.println(gen.edges.get(id));
        }
        System.out.println(gen.nodeTypes);
        System.out.println(gen.edgeTypes);
         */

 /*
        // Out distributions
        System.out.println("Out distributions");
        for (String label : gen.schema.nodeTypes.keySet()) {

            for (Entry<EdgeType, NodeType> key : gen.schema.nodeTypes.get(label).outDistributionCounter.keySet()) {
                System.out.println(label + "-" + key.getKey() + "->" + key.getValue());
                System.out.println(gen.schema.nodeTypes.get(label).outDistributionCounter.get(key));
            }

        }
        // In distributions
        System.out.println("In distributions");
        for (String label : gen.schema.nodeTypes.keySet()) {

            for (Entry<NodeType, EdgeType> key : gen.schema.nodeTypes.get(label).inDistributionCounter.keySet()) {
                System.out.println(key.getKey() + "-" + key.getValue() + "->" + label);
                System.out.println(gen.schema.nodeTypes.get(label).inDistributionCounter.get(key));
            }

        }
         */
    }

    private SchemaGenerator(String dbURL) {
        dataLoader = new DataLoader(new GraphQueryer(dbURL));
        distributionFitter = new DistributionFitter();
        graph = new Graph();
        schema = new Schema();
    }

    private void generateSchema() {
        Long startTime = System.currentTimeMillis();

        // Load basic graph information from Neo4J database.
        dataLoader.loadNodeData(graph.nodes, schema.nodeTypes);
        dataLoader.loadEdgeData(graph.edges, schema.edgeTypes);
        dataLoader.loadStructureData(graph.nodes, graph.edges);
        System.out.println("Loaded graph data from Neo4J database.");

        /* Count the number of in/outgoing edges for each node type. */
        graph.computeEdgeDistributions();
        // Use these counts to determine what the distribution of edges is 
        findEdgeDistributions();
        System.out.println("Schema generated. Total execution time: " + (System.currentTimeMillis() - startTime) + " ms");

    }

    private void findEdgeDistributions() {
        for (String label : schema.nodeTypes.keySet()) {
            NodeType type = schema.nodeTypes.get(label);
            for (Entry<EdgeType, NodeType> key : type.outDistributionCounter.keySet()) {
                // Find a distribution based on the number of outgoing edges of
                // each node of this type.
                List<Integer> values = type.outDistributionCounter.get(key);
                int maxValue = Collections.max(values);
                int minValue = Collections.min(values);
                int[] xValues = new int[maxValue - minValue + 1];
                int[] yValues = new int[maxValue - minValue + 1];
                for (int i = minValue; i <= maxValue; i++) {
                    xValues[i - minValue] = i;
                    yValues[i - minValue] = 0;
                }
                for (int value : values) {
                    yValues[value - minValue] += 1;
                }
                Distribution distribution = DistributionFitter.fit(xValues, yValues);
                schema.nodeTypes.get(label).outDistributions.put(key, distribution);

            }
        }
    }
}
