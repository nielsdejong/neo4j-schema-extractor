/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.graph;

import me.niels.schemagenerator.schema.NodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niels
 */
public class Node {

    public long id;
    public NodeType type;
    public List<Property> properties;
    public List<Edge> outEdges;
    public List<Edge> inEdges;

    public Node(long id, NodeType type, List<Property> properties) {
        this.id = id;
        this.type = type;
        this.properties = properties;
        this.outEdges = new ArrayList<>();
        this.inEdges = new ArrayList<>();
    }

    @Override
    public String toString() {
        return type+" "+id+""+properties+"";
    }
}
