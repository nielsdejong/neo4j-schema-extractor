/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Niels
 */
public class EdgeType {

    public String label;
    public int edgeCount;
    public Map<String, PropertyType> properties;

    public EdgeType(String type) {
        this.label = type;
        this.edgeCount = 1;
        this.properties = new HashMap<>();
    }

    @Override
    public String toString() {
        return label;
    }
}
