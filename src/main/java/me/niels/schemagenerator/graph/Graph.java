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
}
