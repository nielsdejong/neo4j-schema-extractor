/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.graph;

import me.niels.schemagenerator.schema.EdgeType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Niels
 */
public class Edge {

    public long id;
    public EdgeType type;
    public List<Property> properties;
    public Node origin;
    public Node destination;

    public Edge(long id, EdgeType type, List<Property> properties) {
        this.id = id;
        this.type = type;
        this.properties = properties;

    }

    @Override
    public String toString() {
        return type + " " + id + "" + properties + "";
    }
}
