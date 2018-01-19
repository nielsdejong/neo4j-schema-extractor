/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.util.ArrayList;
import me.niels.schemagenerator.schema.EdgeType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.niels.values.schemagenerator.distribution.NumericDistribution;

/**
 *
 * @author Niels
 */
public class NodeType {

    public String label;
    public int nodeCount;
    
    public Map<Entry<EdgeType, NodeType>, List<Integer>> outDistributionCounter;
    public Map<Entry<NodeType, EdgeType>, List<Integer>> inDistributionCounter;
    
    public Map<Entry<EdgeType, NodeType>, NumericDistribution> outDistributions;
    public Map<Entry<NodeType, EdgeType>, NumericDistribution> inDistributions;

    public Map<String, PropertyType> properties;
    public List<AssociationRule> associationRules;
    
    public NodeType(String label) {
        this.label = label;
        this.nodeCount = 1;
        this.outDistributionCounter = new HashMap<>();
        this.associationRules = new ArrayList<>();
        this.inDistributionCounter = new HashMap<>();
        this.inDistributions = new HashMap<>();
        this.outDistributions = new HashMap<>();
        this.properties = new HashMap<>();
    }

    @Override
    public String toString() {
        return label;
    }
}
